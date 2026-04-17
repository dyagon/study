"""
05-retriever 配置：PG 连接、表名、集合名等，与 03-vector 对齐。
"""
import os

# 纯 postgresql:// 用于 psycopg 直连（自定义 Retriever 查 rag_hybrid）
PG_CONNECTION = os.getenv(
    "PG_CONNECTION",
    "postgresql://postgres:postgres@localhost:5432/agent",
)
if PG_CONNECTION.startswith("postgresql+psycopg"):
    PG_CONNECTION_PSYCOPG = PG_CONNECTION.replace("postgresql+psycopg://", "postgresql://", 1)
else:
    PG_CONNECTION_PSYCOPG = PG_CONNECTION

# langchain_postgres.PGVector 需要 postgresql+psycopg://
if PG_CONNECTION.startswith("postgresql+psycopg"):
    PG_CONNECTION_LC = PG_CONNECTION
else:
    PG_CONNECTION_LC = PG_CONNECTION.replace("postgresql://", "postgresql+psycopg://", 1)

# 03-vector 表名（01、03 使用）
TABLE_NAME = os.getenv("RAG_HYBRID_TABLE", "rag_hybrid")
EMBEDDING_DIM = int(os.getenv("RAG_EMBEDDING_DIM", "1024"))

# PGVector 集合名（02 使用）
COLLECTION_NAME = os.getenv("RETRIEVER_COLLECTION", "retriever_docs")

RETRIEVE_K = int(os.getenv("RETRIEVE_K", "5"))
