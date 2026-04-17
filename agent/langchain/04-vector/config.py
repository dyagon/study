"""
PGVector 混合检索（向量 + tsvector 全文）配置。
与 dev/docker-compose.yml 中 pgvector 服务一致。
"""
import os

# 纯 postgresql:// 用于 psycopg 直连（建表、原始 SQL 检索）
PG_CONNECTION = os.getenv(
    "PG_CONNECTION",
    "postgresql://postgres:postgres@localhost:5432/agent",
)
# 若使用 postgresql+psycopg:// 形式，可在此转为 psycopg 可用
if PG_CONNECTION.startswith("postgresql+psycopg"):
    PG_CONNECTION = PG_CONNECTION.replace("postgresql+psycopg://", "postgresql://", 1)

TABLE_NAME = os.getenv("RAG_HYBRID_TABLE", "rag_hybrid")
# DashScope text-embedding-v3 为 1024 维，与表结构需一致
EMBEDDING_DIM = int(os.getenv("RAG_EMBEDDING_DIM", "1024"))

CHUNK_SIZE = 800
CHUNK_OVERLAP = 150
RETRIEVE_K = 5
# 混合检索时每路取更多再 RRF 合并
HYBRID_FETCH_K = 20
RRF_K = 50
