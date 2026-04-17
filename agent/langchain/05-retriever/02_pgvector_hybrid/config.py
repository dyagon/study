"""
02_pgvector_ensemble 配置：与 03-embedding/03-vector 表 rag_hybrid 对齐。
"""
import os

PG_CONNECTION = os.getenv(
    "PG_CONNECTION",
    "postgresql://postgres:postgres@localhost:5432/agent",
)
if PG_CONNECTION.startswith("postgresql+psycopg"):
    PG_CONNECTION_PSYCOPG = PG_CONNECTION.replace("postgresql+psycopg://", "postgresql://", 1)
else:
    PG_CONNECTION_PSYCOPG = PG_CONNECTION

TABLE_NAME = os.getenv("RAG_HYBRID_TABLE", "rag_hybrid")
RETRIEVE_K = int(os.getenv("RETRIEVE_K", "5"))
# 每路检索各取多少条再 RRF 合并
FETCH_K = int(os.getenv("ENSEMBLE_FETCH_K", "10"))
RRF_K = 60  # RRF 公式 1/(rank + k) 中的 k
