"""
单向量检索 vs 混合检索 演示：命令行传入查询与模式。
用法:
  uv run python langchain/03-rag/query.py "项目有哪些 demo"           # 默认单向量
  uv run python langchain/03-rag/query.py "项目有哪些 demo" --mode vector
  uv run python langchain/03-rag/query.py "项目有哪些 demo" --mode hybrid
"""
import argparse
import re

import psycopg
from langchain_community.embeddings import DashScopeEmbeddings

from config import (
    PG_CONNECTION,
    TABLE_NAME,
    RETRIEVE_K,
    HYBRID_FETCH_K,
    RRF_K,
)


def rrf_score(rank: int, rrf_k: int = RRF_K) -> float:
    if rank is None or rank <= 0:
        return 0.0
    return 1.0 / (rank + rrf_k)


def run_vector_search(cur, query_embedding: list[float], k: int) -> list[tuple[str, str, int]]:
    """返回 (id, content, rank)。"""
    vec_str = "[" + ",".join(str(x) for x in query_embedding) + "]"
    cur.execute(
        f"""
        SELECT id::text, content, row_number() OVER (ORDER BY embedding <=> %s::vector) AS r
        FROM {TABLE_NAME}
        ORDER BY embedding <=> %s::vector
        LIMIT %s
        """,
        (vec_str, vec_str, k),
    )
    return [(r[0], r[1], r[2]) for r in cur.fetchall()]


def run_fts_search(cur, query_text: str, k: int) -> list[tuple[str, str, int]]:
    """全文检索，返回 (id, content, rank)。"""
    # 简单分词：用空格拆，避免 SQL 注入；plainto_tsquery 会处理
    words = re.sub(r"\s+", " ", query_text.strip()).split()
    if not words:
        return []
    q = " & ".join(words)
    cur.execute(
        f"""
        SELECT id::text, content, row_number() OVER (ORDER BY ts_rank_cd(content_tsv, plainto_tsquery('simple', %s)) DESC) AS r
        FROM {TABLE_NAME}
        WHERE content_tsv @@ plainto_tsquery('simple', %s)
        LIMIT %s
        """,
        (q, q, k),
    )
    return [(r[0], r[1], r[2]) for r in cur.fetchall()]


def merge_rrf(
    vector_results: list[tuple[str, str, int]],
    fts_results: list[tuple[str, str, int]],
    top_k: int,
) -> list[tuple[str, str, float]]:
    """按 RRF 合并两路排序，返回 (id, content, score)。"""
    scores: dict[str, tuple[str, float]] = {}
    for id_, content, rank in vector_results:
        scores[id_] = (content, scores.get(id_, (content, 0.0))[1] + rrf_score(rank))
    for id_, content, rank in fts_results:
        if id_ not in scores:
            scores[id_] = (content, 0.0)
        c, s = scores[id_]
        scores[id_] = (c, s + rrf_score(rank))
    out = [(id_, c, sc) for id_, (c, sc) in scores.items()]
    out.sort(key=lambda x: -x[2])
    return out[:top_k]


def main():
    parser = argparse.ArgumentParser(description="单向量检索 / 混合检索演示")
    parser.add_argument("query", type=str, help="查询文本")
    parser.add_argument("--mode", choices=("vector", "hybrid"), default="vector", help="vector=仅向量, hybrid=向量+全文 RRF 融合")
    parser.add_argument("-k", type=int, default=RETRIEVE_K, help="返回条数")
    args = parser.parse_args()

    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    query_embedding = embeddings.embed_query(args.query)

    with psycopg.connect(PG_CONNECTION) as conn:
        with conn.cursor() as cur:
            if args.mode == "vector":
                print("--- 单向量检索（仅 embedding 相似度）---\n")
                rows = run_vector_search(cur, query_embedding, args.k)
                for i, (id_, content, rank) in enumerate(rows, 1):
                    print(f"[{i}] rank={rank} id={id_[:8]}...")
                    print(content[:200].replace("\n", " ") + ("..." if len(content) > 200 else ""))
                    print()
            else:
                print("--- 混合检索（向量 + 全文，RRF 融合）---\n")
                vec_rows = run_vector_search(cur, query_embedding, HYBRID_FETCH_K)
                fts_rows = run_fts_search(cur, args.query, HYBRID_FETCH_K)
                merged = merge_rrf(vec_rows, fts_rows, args.k)
                for i, (id_, content, score) in enumerate(merged, 1):
                    print(f"[{i}] score={score:.4f} id={id_[:8]}...")
                    print(content[:200].replace("\n", " ") + ("..." if len(content) > 200 else ""))
                    print()
                print("（对比：仅向量时可用 --mode vector 查看排序差异）")


if __name__ == "__main__":
    main()
