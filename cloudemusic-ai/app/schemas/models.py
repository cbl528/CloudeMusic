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


class DjRegisterRequest(BaseModel):
    song_id: int | str
    song_name: str = ""
    song_artists: str = ""


class DjRegisterResponse(BaseModel):
    song_id: str
    indexed: bool
    emotion: str = ""


class DjRecommendRequest(BaseModel):
    current_song_id: int | str
    current_song_name: str = ""
    current_song_artists: str = ""
    recent_ids: list[int | str] = []


class DjRecommendResponse(BaseModel):
    commentary: str
    next_song: SongItem
