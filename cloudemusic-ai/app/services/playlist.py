"""AI 智能歌单生成核心业务逻辑"""

import logging

import httpx

from app.config import settings
from app.services.bailian import call_llm_json

logger = logging.getLogger(__name__)

SYSTEM_SEARCH_TERMS = """你是一位音乐推荐系统的搜索策略专家。
根据用户输入的心情或场景关键词，生成3个适合在音乐平台上搜索的关键词。
每个关键词应简洁（2-8个字），覆盖不同的角度，如曲风、情绪、场景、歌词主题等。

只返回 JSON 格式：
{"search_terms": ["关键词1", "关键词2", "关键词3"]}"""

SYSTEM_CURATION = """你是一位专业的音乐策展人。根据用户的场景或心情关键词，从提供的歌曲列表中精选出最合适的歌曲组成歌单。

要求：
1. 选择与主题最相关的 6-10 首歌曲
2. 生成一个有创意的中文歌单名称（简短有力，8字以内）
3. 写一段吸引人的中文歌单描述（30-80字）
4. 为每首歌写一句入选理由（10-20字，说明与主题的关联）

只返回 JSON 格式（不要 markdown 代码块标记）：
{
  "playlist_name": "歌单名称",
  "description": "歌单描述文字...",
  "songs": [
    {"id": "歌曲ID", "name": "歌曲名", "artists": "歌手", "reason": "入选理由"}
  ]
}"""


def _normalize_artist(song: dict) -> str:
    """统一歌手字段格式（兼容 Netease API 不同命名）。"""
    if song.get("artists"):
        if isinstance(song["artists"], list):
            return "/".join(a.get("name", "") for a in song["artists"])
        return str(song["artists"])
    if song.get("ar"):
        if isinstance(song["ar"], list):
            return "/".join(a.get("name", "") for a in song["ar"])
        return str(song["ar"])
    return ""


def _normalize_cover(song: dict) -> str:
    """统一封面字段格式。"""
    if song.get("cover"):
        return song["cover"]
    if song.get("picUrl"):
        return song["picUrl"]
    if song.get("album") and isinstance(song["album"], dict):
        return song["album"].get("picUrl", "")
    if song.get("al") and isinstance(song["al"], dict):
        return song["al"].get("picUrl", "")
    return ""


def _dedup_songs(songs: list[dict]) -> list[dict]:
    """按歌曲 ID 去重。"""
    seen = set()
    result = []
    for s in songs:
        sid = str(s.get("id", ""))
        if sid and sid not in seen:
            seen.add(sid)
            result.append(s)
    return result


def _search_songs(keyword: str) -> list[dict]:
    """通过 Spring Boot（代理 Netease API）搜索歌曲。"""
    url = f"{settings.spring_boot_url}/api/cloude/music/search"
    params = {"keywords": keyword, "type": 1, "limit": 20}
    try:
        resp = httpx.get(url, params=params, timeout=10)
        resp.raise_for_status()
        body = resp.json()
        if body.get("code") == 200:
            data = body.get("data", {})
            raw_songs = (
                data.get("result", {}).get("songs", [])
                if isinstance(data, dict)
                else []
            )
            normalized = []
            for s in raw_songs:
                normalized.append({
                    "id": str(s.get("id", "")),
                    "name": s.get("name", ""),
                    "artists": _normalize_artist(s),
                    "cover": _normalize_cover(s),
                    "duration": s.get("duration", 0),
                })
            return normalized
    except Exception as e:
        logger.warning("Search '%s' failed: %s", keyword, e)
    return []


async def generate_playlist(keyword: str, favorites: list[dict] | None = None) -> dict:
    """AI 智能歌单生成完整流程。"""
    favorites = favorites or []

    # 1. LLM 生成搜索关键词
    logger.info("Step 1: Generating search terms for '%s'", keyword)
    search_result = call_llm_json(
        SYSTEM_SEARCH_TERMS,
        f"用户输入的心情或场景关键词是：「{keyword}」",
    )
    search_terms = search_result.get("search_terms", [keyword])
    logger.info("Search terms: %s", search_terms)

    # 2. 搜索真实歌曲
    logger.info("Step 2: Searching songs")
    all_songs = []
    for term in search_terms:
        songs = _search_songs(term)
        logger.info("  '%s' → %d songs", term, len(songs))
        all_songs.extend(songs)
    all_songs = _dedup_songs(all_songs)
    logger.info("Total unique songs: %d", len(all_songs))

    if not all_songs:
        raise ValueError(f"未找到与「{keyword}」相关的歌曲，请尝试其他关键词")

    # 3. LLM 策展歌单（结合搜索到的歌曲和用户收藏）
    logger.info("Step 3: LLM curation")
    song_list_text = "\n".join(
        f"  {i+1}. {s['name']} - {s['artists']}"
        for i, s in enumerate(all_songs)
    )

    fav_text = ""
    if favorites:
        fav_text = "\n用户收藏歌曲（优先考虑这些）：\n" + "\n".join(
            f"  {i+1}. {s.get('songName', s.get('name', ''))} - "
            f"{s.get('artist', s.get('artists', ''))}"
            for i, s in enumerate(favorites)
        )

    user_prompt = (
        f"用户场景/心情关键词：「{keyword}」\n"
        f"从以下搜索到的歌曲中精选推荐：\n"
        f"{song_list_text}"
        f"{fav_text}"
    )

    curation = call_llm_json(SYSTEM_CURATION, user_prompt)

    # 4. 组装最终结果（补全封面等信息）
    song_map = {s["id"]: s for s in all_songs}

    songs = []
    for item in curation.get("songs", []):
        sid = str(item.get("id", ""))
        matched = song_map.get(sid, {})
        songs.append({
            "id": sid,
            "name": item.get("name", matched.get("name", "")),
            "artists": item.get("artists", matched.get("artists", "")),
            "cover": matched.get("cover", ""),
            "duration": matched.get("duration", 0),
            "reason": item.get("reason", ""),
        })

    return {
        "playlist_name": curation.get("playlist_name", f"{keyword}歌单"),
        "description": curation.get("description", ""),
        "songs": songs,
    }
