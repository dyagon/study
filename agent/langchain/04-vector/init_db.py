"""
用 Python 执行建表：创建 rag_hybrid（vector + tsvector）、索引与 rrf_score 函数。
依赖 psycopg、且库已启用 pgvector（dev/docker-compose 已包含）。
会先 DROP TABLE IF EXISTS，再建表，确保向量维度与当前 config 一致。

用法：uv run python langchain/03-rag/init_db.py
"""
import re
from pathlib import Path

import psycopg

from config import PG_CONNECTION, TABLE_NAME, EMBEDDING_DIM

SCHEMA_FILE = Path(__file__).resolve().parent / "schema.sql"


def _split_sql(sql: str) -> list[str]:
    """按顶层 ';' 分割 SQL，保留 $$...$$ 内的分号。"""
    out = []
    buf = []
    in_dollar = False
    i = 0
    while i < len(sql):
        if not in_dollar and sql[i : i + 2] == "$$":
            in_dollar = True
            buf.append("$$")
            i += 2
            continue
        if in_dollar and sql[i : i + 2] == "$$":
            in_dollar = False
            buf.append("$$")
            i += 2
            continue
        if not in_dollar and sql[i] == ";":
            stmt = "".join(buf).strip()
            # 只跳过空语句；含 -- 但后面有 CREATE/COMMENT 等的整段仍要执行
            if stmt:
                out.append(stmt)
            buf = []
            i += 1
            continue
        buf.append(sql[i])
        i += 1
    if buf:
        stmt = "".join(buf).strip()
        if stmt:
            out.append(stmt)
    return out


def main():
    sql = SCHEMA_FILE.read_text(encoding="utf-8")
    sql = sql.replace("rag_hybrid", TABLE_NAME)
    sql = re.sub(r"vector\(\d+\)", f"vector({EMBEDDING_DIM})", sql)

    with psycopg.connect(PG_CONNECTION) as conn:
        with conn.cursor() as cur:
            cur.execute(f"DROP TABLE IF EXISTS {TABLE_NAME} CASCADE")
            for stmt in _split_sql(sql):
                cur.execute(stmt)
        conn.commit()
    print(f"已建表 {TABLE_NAME}（embedding vector({EMBEDDING_DIM})，content_tsv tsvector）及索引、rrf_score 函数。")


if __name__ == "__main__":
    main()
