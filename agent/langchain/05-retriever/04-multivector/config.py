"""
多向量 QA Demo 配置。
- 使用独立集合名与 docstore 文件，与 02-qa 互不干扰。
"""
import os
from pathlib import Path

# 本 demo 所在目录，用于默认 docstore 路径
_THIS_DIR = Path(__file__).resolve().parent

PG_CONNECTION = os.getenv(
    "PG_CONNECTION",
    "postgresql+psycopg://postgres:postgres@localhost:5432/agent",
)

# 多向量检索：向量库集合名、持久化 docstore 文件路径
MULTIVECTOR_COLLECTION = os.getenv(
    "QA_MULTIVECTOR_COLLECTION",
    "qa_multivector",
)
DOCSTORE_PATH = Path(
    os.getenv("QA_MULTIVECTOR_DOCSTORE", str(_THIS_DIR / "docstore.json"))
).resolve()

RETRIEVE_K = int(os.getenv("QA_MULTIVECTOR_RETRIEVE_K", "4"))
ID_KEY = "doc_id"
