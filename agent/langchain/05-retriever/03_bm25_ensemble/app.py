"""
EnsembleRetriever 示例：向量检索 + BM25 检索，RRF 融合。

BM25 基于从 rag_hybrid 加载的文档构建（内存索引），适合中小规模语料。
依赖：04-vector（或 03-embedding）的 init_db + build_db 已执行；rank_bm25 已加入项目依赖（uv sync 即可）。

用法：
  uv run python langchain/05-retriever/03_bm25_ensemble/app.py "你的问题"
  uv run python langchain/05-retriever/03_bm25_ensemble/app.py "你的问题" --weights 0.7 0.3
"""
import argparse
import sys
from pathlib import Path

_root = Path(__file__).resolve().parent
if str(_root) not in sys.path:
    sys.path.insert(0, str(_root))

import psycopg
from langchain_core.documents import Document
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_community.retrievers import BM25Retriever

from config import PG_CONNECTION_PSYCOPG, TABLE_NAME, RETRIEVE_K, FETCH_K, RRF_K
from vector_retriever import VectorRetriever
from ensemble_retriever import EnsembleRetriever


def load_docs_from_table(connection_str: str, table_name: str) -> list[Document]:
    """从 rag_hybrid 表加载全部文档，用于构建 BM25 索引。"""
    docs = []
    with psycopg.connect(connection_str) as conn:
        with conn.cursor() as cur:
            cur.execute(
                f"SELECT id::text, content, metadata FROM {table_name}",
            )
            for row in cur.fetchall():
                doc_id, content, metadata = row[0], row[1], row[2] or {}
                if not isinstance(metadata, dict):
                    metadata = {}
                metadata["id"] = doc_id
                docs.append(
                    Document(page_content=content or "", metadata=metadata, id=doc_id)
                )
    return docs


def main():
    parser = argparse.ArgumentParser(description="EnsembleRetriever：向量 + BM25 RRF 融合")
    parser.add_argument("query", nargs="?", default="项目有哪些 demo", help="查询文本")
    parser.add_argument("-k", type=int, default=RETRIEVE_K, help="最终返回条数")
    parser.add_argument(
        "--weights",
        type=float,
        nargs=2,
        default=[0.5, 0.5],
        metavar=("W_VECTOR", "W_BM25"),
        help="向量与 BM25 的权重，默认 0.5 0.5",
    )
    args = parser.parse_args()

    print("从 rag_hybrid 加载文档构建 BM25 索引…")
    all_docs = load_docs_from_table(PG_CONNECTION_PSYCOPG, TABLE_NAME)
    if not all_docs:
        print("表为空，请先执行 init_db 与 build_db。")
        sys.exit(1)
    bm25_retriever = BM25Retriever.from_documents(all_docs, k=FETCH_K)

    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    vector_retriever = VectorRetriever(
        connection_str=PG_CONNECTION_PSYCOPG,
        table_name=TABLE_NAME,
        k=FETCH_K,
        embeddings=embeddings,
    )
    ensemble = EnsembleRetriever(
        retrievers=[vector_retriever, bm25_retriever],
        weights=args.weights,
        k=RRF_K,
        top_k=args.k,
    )
    docs = ensemble.invoke(args.query)
    print("--- EnsembleRetriever（向量 + BM25 RRF）---\n")
    print(f"query: {args.query}")
    print(f"weights: 向量={args.weights[0]}, BM25={args.weights[1]}\n")
    for i, d in enumerate(docs, 1):
        print(f"[{i}] {d.page_content[:200].replace(chr(10), ' ')}...")
        print()


if __name__ == "__main__":
    main()
