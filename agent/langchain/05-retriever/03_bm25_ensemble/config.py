"""
03_bm25_ensemble 配置：与 04-vector/03-embedding 表 rag_hybrid 对齐。
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
FETCH_K = int(os.getenv("ENSEMBLE_FETCH_K", "10"))
RRF_K = 60
