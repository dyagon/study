"""
QA Demo 配置：向量库连接、集合名等。
从环境变量读取，未设置时使用与 dev/docker-compose.yml 一致的默认值。
文档目录由 build_db 命令行参数指定，不在此配置。
"""
import os

# PG 连接串（与 dev/docker-compose.yml 中 pgvector 服务一致）
# 格式: postgresql+psycopg://user:password@host:port/dbname
PG_CONNECTION = os.getenv(
    "PG_CONNECTION",
    "postgresql+psycopg://postgres:postgres@localhost:5432/agent",
)

# PGVector 集合名
COLLECTION_NAME = os.getenv("QA_COLLECTION_NAME", "qa_docs")

# 分块与检索
CHUNK_SIZE = 800
CHUNK_OVERLAP = 150
RETRIEVE_K = 4
