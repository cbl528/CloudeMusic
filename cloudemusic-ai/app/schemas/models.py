from pydantic import BaseModel


class SongItem(BaseModel):
    id: int | str
    name: str
    artists: str
    cover: str = ""
    duration: int = 0
    reason: str = ""


class FavoriteItem(BaseModel):
    songId: int | str = ""
    songName: str = ""
    artist: str = ""
    cover: str = ""


class PlaylistGenerateRequest(BaseModel):
    keyword: str
    favorites: list[FavoriteItem] = []


class PlaylistGenerateResponse(BaseModel):
    playlist_name: str
    description: str
    songs: list[SongItem]


class DjCommentaryRequest(BaseModel):
    current_song_id: int | str
    user_id: int | None = None


class DjCommentaryResponse(BaseModel):
    commentary: str
    next_song: SongItem | None = None
