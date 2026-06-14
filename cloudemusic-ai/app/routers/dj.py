"""AI DJ 情感接续播放路由"""

import logging

import httpx
from fastapi import APIRouter, HTTPException

from app.config import settings
from app.schemas.models import (
    DjRegisterRequest,
    DjRegisterResponse,
    DjRecommendRequest,
    DjRecommendResponse,
    SongItem,
)
from app.services.dj import generate_dj_commentary
from app.services.emotion import get_index

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/ai/dj", tags=["AI DJ"])


def _parse_lyric(lrc_text: str) -> str:
    """从 LRC 格式歌词中提取纯文本（去除时间标签）。"""
    if not lrc_text:
        return ""
    lines = []
    for line in lrc_text.split("\n"):
        parts = line.split("]", 1)
        if len(parts) == 2 and parts[1].strip():
            lines.append(parts[1].strip())
    return "\n".join(lines)


async def _fetch_lyric(song_id: str) -> str:
    """通过 Spring Boot 获取歌词文本。"""
    url = f"{settings.spring_boot_url}/api/cloude/music/song/lyric"
    async with httpx.AsyncClient(timeout=10) as client:
        resp = await client.get(url, params={"id": song_id})
    body = resp.json()
    if body.get("code") != 200:
        logger.warning("Lyric fetch failed for %s: code=%s", song_id, body.get("code"))
        return ""
    data = body.get("data", {}) or {}
    lrc = (data.get("lrc", {}) or {}).get("lyric", "")
    return _parse_lyric(lrc)


async def _fetch_song_detail(song_id: str) -> dict:
    """通过 Spring Boot 获取歌曲详情。"""
    url = f"{settings.spring_boot_url}/api/cloude/music/song/detail"
    async with httpx.AsyncClient(timeout=10) as client:
        resp = await client.get(url, params={"ids": song_id})
    body = resp.json()
    if body.get("code") != 200:
        return {}
    data = body.get("data", {}) or {}
    songs = data.get("songs", [])
    if not songs:
        return {}
    detail = songs[0]
    al = detail.get("al", {}) or {}
    ar_list = detail.get("ar", []) or []
    return {
        "name": detail.get("name", ""),
        "artists": "/".join(a.get("name", "") for a in ar_list),
        "cover": al.get("picUrl", ""),
        "duration": detail.get("dt", 0),
    }


@router.post("/register", response_model=DjRegisterResponse)
async def register_song(req: DjRegisterRequest):
    """注册歌曲歌词到情感向量库（幂等，可重复调用）。

    由前端在每次播放歌曲时静默调用（fire-and-forget）。
    """
    song_id = str(req.song_id)
    index = get_index()

    if index.has(song_id):
        return DjRegisterResponse(
            song_id=song_id, indexed=True, emotion=index.get(song_id).emotion
        )

    # 获取歌词 + 歌曲详情
    lyric_text = await _fetch_lyric(song_id)

    if not lyric_text or len(lyric_text.strip()) < 20:
        detail = await _fetch_song_detail(song_id)
        name = req.song_name or detail.get("name", "")
        artists = req.song_artists or detail.get("artists", "")
        index.register(song_id, name, artists, name or "纯音乐")
    else:
        name = req.song_name or ""
        artists = req.song_artists or ""
        if not name or not artists:
            detail = await _fetch_song_detail(song_id)
            if not name:
                name = detail.get("name", "")
            if not artists:
                artists = detail.get("artists", "")
        index.register(song_id, name, artists, lyric_text)

    se = index.get(song_id)
    return DjRegisterResponse(
        song_id=song_id,
        indexed=True,
        emotion=se.emotion if se else "未分类",
    )


@router.post("/recommend", response_model=DjRecommendResponse)
async def recommend(req: DjRecommendRequest):
    """AI DJ 推荐：情感分析 + 相似检索 + 解说词生成。

    需在前端播放进度超过 60% 时调用。
    """
    current_id = str(req.current_song_id)
    index = get_index()

    # 1. 确保当前歌曲已注册
    if not index.has(current_id):
        try:
            lyric_text = await _fetch_lyric(current_id)
            detail = await _fetch_song_detail(current_id)
            name = req.current_song_name or detail.get("name", "")
            artists = req.current_song_artists or detail.get("artists", "")
            if lyric_text:
                index.register(current_id, name, artists, lyric_text)
            else:
                index.register(current_id, name, artists, name or "未知歌曲")
        except Exception as e:
            logger.error("Failed to register current song %s: %s", current_id, e)
            raise HTTPException(
                status_code=500, detail="当前歌曲情感分析失败，请稍后重试"
            )

    current_se = index.get(current_id)
    if not current_se:
        raise HTTPException(status_code=404, detail="当前歌曲未找到情感数据")

    # 2. 向量库歌曲数不足时返回提示
    if index.count() < 3:
        raise HTTPException(
            status_code=404,
            detail="情感库歌曲太少（<3首），播放更多歌曲后自动激活DJ推荐",
        )

    # 3. 查找同情感歌曲
    candidates = index.recommend(
        current_id,
        exclude_ids=[str(sid) for sid in req.recent_ids] if req.recent_ids else None,
        top_n=3,
    )

    if not candidates:
        raise HTTPException(status_code=404, detail="未找到情感匹配的歌曲")

    next_se, score = candidates[0]
    logger.info(
        "Recommended %s (%s - %s) score=%.3f",
        next_se.song_id,
        next_se.name,
        next_se.artists,
        score,
    )

    # 4. 生成解说词
    commentary = await generate_dj_commentary(
        song_name=current_se.name,
        artists=current_se.artists,
        emotion_tag=current_se.emotion,
        emotion_desc=current_se.description,
        next_name=next_se.name,
        next_artists=next_se.artists,
        next_emotion_tag=next_se.emotion,
    )

    return DjRecommendResponse(
        commentary=commentary,
        next_song=SongItem(
            id=next_se.song_id,
            name=next_se.name,
            artists=next_se.artists,
            cover="",
            duration=0,
            reason=f"情感相近（{current_se.emotion} → {next_se.emotion}）",
        ),
    )
