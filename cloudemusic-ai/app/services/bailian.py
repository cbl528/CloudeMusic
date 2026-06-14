"""百炼平台 API 封装（LLM 与 Embedding）"""

import json
import logging
from typing import Optional

from openai import OpenAI

from app.config import settings

logger = logging.getLogger(__name__)

_client: Optional[OpenAI] = None


def get_client() -> OpenAI:
    global _client
    if _client is None:
        _client = OpenAI(
            api_key=settings.bailian_api_key,
            base_url=settings.bailian_base_url,
        )
    return _client


def call_llm(
    system_prompt: str,
    user_prompt: str,
    model: Optional[str] = None,
    temperature: float = 0.7,
    max_retries: int = 2,
) -> str:
    """调用百炼 LLM，返回文本响应。"""
    client = get_client()
    model = model or settings.bailian_llm_model

    for attempt in range(max_retries + 1):
        try:
            resp = client.chat.completions.create(
                model=model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
                temperature=temperature,
            )
            return resp.choices[0].message.content or ""
        except Exception as e:
            logger.warning("LLM call attempt %d failed: %s", attempt + 1, e)
            if attempt == max_retries:
                raise


def call_llm_json(
    system_prompt: str,
    user_prompt: str,
    model: Optional[str] = None,
    temperature: float = 0.5,
) -> dict:
    """调用百炼 LLM，解析返回的 JSON。"""
    text = call_llm(system_prompt, user_prompt, model, temperature)

    # 尝试从 markdown 代码块中提取 JSON
    text = text.strip()
    if text.startswith("```"):
        # 移除 ```json 或 ``` 包围
        start = text.find("\n") + 1
        end = text.rfind("```")
        if end > start:
            text = text[start:end].strip()

    try:
        return json.loads(text)
    except json.JSONDecodeError:
        logger.error("Failed to parse LLM response as JSON: %s", text)
        raise


def create_embedding(text: str) -> list[float]:
    """将文本向量化。"""
    client = get_client()
    resp = client.embeddings.create(
        model=settings.bailian_embedding_model,
        input=text,
    )
    return resp.data[0].embedding
