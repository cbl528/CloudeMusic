"""歌词情感分类与向量化存储（内存 + JSON 持久化）"""

import json
import logging
import threading
from datetime import datetime
from pathlib import Path

import numpy as np

from app.services.bailian import call_llm_json, create_embedding

logger = logging.getLogger(__name__)

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
INDEX_FILE = DATA_DIR / "emotion_index.json"

SYSTEM_EMOTION_CLASSIFY = """你是一位歌词情感分析专家。
分析歌词表达的核心情感，从以下标签中选择最匹配的一个（仅返回一个词）：
激励、治愈、悲伤、欢乐、浪漫、安静、激昂、思念

同时给出 0-1 的情感强度值。

只返回 JSON 格式：
{"emotion": "悲伤", "intensity": 0.8}"""

SYSTEM_EMOTION_DESC = """用一句话描述这段歌词传递的情感氛围（10-30字）。
只返回 JSON 格式：
{"description": "一种淡淡忧伤中带着释怀的夜晚思绪"}"""


class SongEmotion:
    """单首歌曲的情感数据"""

    def __init__(self, song_id: str, name: str, artists: str,
                 emotion: str, intensity: float, description: str,
                 vector: list[float], lyric_snippet: str):
        self.song_id = song_id
        self.name = name
        self.artists = artists
        self.emotion = emotion
        self.intensity = intensity
        self.description = description
        self.vector = vector
        self.lyric_snippet = lyric_snippet
        self.created_at = datetime.now().isoformat()

    def to_dict(self) -> dict:
        return {
            "song_id": self.song_id,
            "name": self.name,
            "artists": self.artists,
            "emotion": self.emotion,
            "intensity": self.intensity,
            "description": self.description,
            "vector": self.vector,
            "lyric_snippet": self.lyric_snippet,
            "created_at": self.created_at,
        }

    @classmethod
    def from_dict(cls, d: dict) -> "SongEmotion":
        obj = cls(
            song_id=d["song_id"],
            name=d.get("name", ""),
            artists=d.get("artists", ""),
            emotion=d["emotion"],
            intensity=d.get("intensity", 0.5),
            description=d.get("description", ""),
            vector=d["vector"],
            lyric_snippet=d.get("lyric_snippet", ""),
        )
        obj.created_at = d.get("created_at", "")
        return obj


def _cosine_similarity(a: list[float], b: list[float]) -> float:
    """计算余弦相似度。"""
    arr_a = np.array(a)
    arr_b = np.array(b)
    dot = np.dot(arr_a, arr_b)
    norm_a = np.linalg.norm(arr_a)
    norm_b = np.linalg.norm(arr_b)
    if norm_a == 0 or norm_b == 0:
        return 0
    return float(dot / (norm_a * norm_b))


class EmotionIndex:
    """歌词情感向量库（内存 + JSON 文件持久化）。

    - 以 song_id 为唯一键（天然去重）
    - 情感标签 + 向量余弦相似度混合评分
    - 线程安全写操作
    """

    def __init__(self):
        self._songs: dict[str, SongEmotion] = {}
        self._lock = threading.Lock()
        self._load()

    # ---- 持久化 ----

    def _load(self):
        """从 JSON 文件加载已有索引。"""
        if not INDEX_FILE.exists():
            DATA_DIR.mkdir(parents=True, exist_ok=True)
            return
        try:
            with open(INDEX_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
            for item in data:
                se = SongEmotion.from_dict(item)
                self._songs[se.song_id] = se
            logger.info("Loaded %d songs from emotion index", len(self._songs))
        except Exception as e:
            logger.warning("Failed to load emotion index: %s", e)

    def _save(self):
        """持久化到 JSON 文件。"""
        try:
            DATA_DIR.mkdir(parents=True, exist_ok=True)
            data = [se.to_dict() for se in self._songs.values()]
            with open(INDEX_FILE, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.warning("Failed to save emotion index: %s", e)

    # ---- 查询接口 ----

    def has(self, song_id: str) -> bool:
        return song_id in self._songs

    def get(self, song_id: str) -> SongEmotion | None:
        return self._songs.get(song_id)

    def count(self) -> int:
        return len(self._songs)

    def get_all_emotions(self) -> dict[str, int]:
        """统计各类情感的歌曲数量。"""
        counts: dict[str, int] = {}
        for se in self._songs.values():
            counts[se.emotion] = counts.get(se.emotion, 0) + 1
        return counts

    # ---- 索引 ----

    def register(self, song_id: str, name: str, artists: str, lyric_text: str) -> SongEmotion:
        """对歌词做情感分类 + 向量化并入库（幂等，重复 song_id 跳过）。"""
        if self.has(song_id):
            return self._songs[song_id]

        lyric_text = (lyric_text or "").strip()
        if not lyric_text or len(lyric_text) < 20:
            emotion = "未分类"
            intensity = 0.0
            description = "纯音乐或歌词过短"
            snippet = lyric_text[:100] or "纯音乐"
            try:
                vector = create_embedding(snippet)
            except Exception as e:
                logger.warning("Embedding fallback failed for %s: %s", song_id, e)
                vector = []
        else:
            text_for_analysis = lyric_text[:500]
            snippet = lyric_text[:200]

            try:
                emotion_data = call_llm_json(SYSTEM_EMOTION_CLASSIFY, text_for_analysis)
                emotion = emotion_data.get("emotion", "未分类")
                intensity = float(emotion_data.get("intensity", 0.5))
            except Exception as e:
                logger.warning("Emotion classify failed for %s: %s", song_id, e)
                emotion = "未分类"
                intensity = 0.0

            try:
                desc_data = call_llm_json(SYSTEM_EMOTION_DESC, text_for_analysis)
                description = desc_data.get("description", "")
            except Exception:
                description = ""

            try:
                vector = create_embedding(text_for_analysis)
            except Exception as e:
                logger.warning("Embedding failed for %s: %s", song_id, e)
                vector = []

        se = SongEmotion(
            song_id=song_id, name=name, artists=artists,
            emotion=emotion, intensity=intensity, description=description,
            vector=vector, lyric_snippet=snippet,
        )

        with self._lock:
            self._songs[song_id] = se
            self._save()

        logger.info("Registered song %s (%s - %s) as emotion=%s intensity=%.2f",
                    song_id, name, artists, emotion, intensity)
        return se

    # ---- 推荐 ----

    def recommend(self, song_id: str, exclude_ids: list[str] | None = None,
                  top_n: int = 5) -> list[tuple[SongEmotion, float]]:
        """推荐与指定歌曲情感相似的歌曲（按综合相似度降序）。"""
        exclude = set(exclude_ids or [])
        exclude.add(song_id)

        current = self._songs.get(song_id)
        if not current:
            return []

        scored: list[tuple[SongEmotion, float]] = []
        for sid, se in self._songs.items():
            if sid in exclude:
                continue

            score = 0.0
            # 情感标签匹配 (0-0.6)
            if se.emotion == current.emotion:
                score += 0.6
            # 强度接近度 (0-0.1)
            score += 0.1 * (1 - abs(se.intensity - current.intensity))
            # 向量相似度 (0-0.3)
            if se.vector and current.vector:
                sim = _cosine_similarity(se.vector, current.vector)
                score += 0.3 * max(0, sim)

            scored.append((se, score))

        scored.sort(key=lambda x: x[1], reverse=True)
        return scored[:top_n]


# 模块级单例
_index: EmotionIndex | None = None


def get_index() -> EmotionIndex:
    global _index
    if _index is None:
        _index = EmotionIndex()
    return _index
