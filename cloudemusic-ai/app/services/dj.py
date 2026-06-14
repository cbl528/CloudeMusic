"""AI DJ 情感接续解说词生成。"""
import logging

from app.services.bailian import call_llm_json

logger = logging.getLogger(__name__)

SYSTEM_DJ = """你是一位情感细腻的中文音乐电台主持人"小云"。
用户刚听完一首歌，需要你用温暖自然的口吻推荐下一首，让情感接续。

要求：
1. 生成 40-80 字的口语化电台解说词
2. 自然衔接上一首歌的情绪，引出下一首歌
3. 带出下一首歌名和歌手，不做作
4. 语气温暖自然，像真实电台主持人
5. 不要用「亲爱的」「听众朋友们」等刻板用语
6. 解说词只说给"你"（用户），保持亲切一对一的氛围

只返回 JSON（不要 markdown 代码块标记）：
{"commentary": "解说词内容"}"""


async def generate_dj_commentary(
    song_name: str,
    artists: str,
    emotion_tag: str,
    emotion_desc: str,
    next_name: str,
    next_artists: str,
    next_emotion_tag: str,
) -> str:
    """生成 DJ 情感接续解说词。"""
    user_prompt = (
        f"用户刚听完的歌：\n"
        f"- 歌名：{song_name}\n"
        f"- 歌手：{artists}\n"
        f"- 情感标签：{emotion_tag}\n"
        f"- 情感描述：{emotion_desc}\n\n"
        f"推荐的下一首歌：\n"
        f"- 歌名：{next_name}\n"
        f"- 歌手：{next_artists}\n"
        f"- 情感标签：{next_emotion_tag}\n"
    )

    try:
        result = call_llm_json(SYSTEM_DJ, user_prompt, temperature=0.8)
        commentary = result.get("commentary", "").strip()
        if commentary:
            return commentary
    except Exception as e:
        logger.warning("DJ commentary generation failed: %s", e)

    # 降级：返回简洁的推荐语
    return f"接下来为你推荐{next_name}，来自{next_artists}的歌声，希望你会喜欢。"
