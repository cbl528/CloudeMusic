import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    # 百炼平台配置
    bailian_api_key: str = os.getenv("BAILIAN_API_KEY", "")
    bailian_base_url: str = os.getenv(
        "BAILIAN_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1"
    )
    bailian_llm_model: str = os.getenv("BAILIAN_LLM_MODEL", "qwen-plus")
    bailian_embedding_model: str = os.getenv(
        "BAILIAN_EMBEDDING_MODEL", "text-embedding-v3"
    )

    # Spring Boot 后端地址
    spring_boot_url: str = os.getenv("SPRING_BOOT_URL", "http://localhost:8000")

    # FastAPI 服务端口
    ai_service_port: int = int(os.getenv("AI_SERVICE_PORT", "8001"))


settings = Settings()
