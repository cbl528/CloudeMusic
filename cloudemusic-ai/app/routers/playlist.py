"""AI 智能歌单生成路由"""

import logging

from fastapi import APIRouter, HTTPException

from app.schemas.models import (
    PlaylistGenerateRequest,
    PlaylistGenerateResponse,
    SongItem,
)
from app.services.playlist import generate_playlist

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/ai/playlist", tags=["AI Playlist"])


@router.post("/generate", response_model=PlaylistGenerateResponse)
async def generate(req: PlaylistGenerateRequest):
    """AI 智能歌单生成

    根据用户输入的心情/场景关键词，AI 搜索曲库并策展推荐歌单。
    """
    if not req.keyword or not req.keyword.strip():
        raise HTTPException(status_code=400, detail="请输入心情或场景关键词")

    try:
        result = await generate_playlist(
            keyword=req.keyword.strip(),
            favorites=[f.model_dump() for f in req.favorites],
        )
        return PlaylistGenerateResponse(
            playlist_name=result["playlist_name"],
            description=result["description"],
            songs=[SongItem(**s) for s in result["songs"]],
        )
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        logger.exception("Playlist generation failed")
        raise HTTPException(status_code=500, detail=f"生成失败：{str(e)}")
