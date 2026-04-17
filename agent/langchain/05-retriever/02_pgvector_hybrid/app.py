"""
EnsembleRetriever 示例：向量检索 + 全文检索，RRF 融合。

依赖：先执行 langchain/03-embedding（或 03-vector）的 init_db.py 与 build_db.py <文档目录>。

用法：
  uv run python langchain/04-retriever/02_pgvector_ensemble/app.py "你的问题"
  uv run python langchain/04-retriever/02_pgvector_ensemble/app.py "你的问题" --weights 0.7 0.3  # 向量权重 0.7，全文 0.3
"""
import argparse

from langchain_community.embeddings import DashScopeEmbeddings

from config import PG_CONNECTION_PSYCOPG, TABLE_NAME, RETRIEVE_K, FETCH_K, RRF_K
from vector_retriever import VectorRetriever
from fts_retriever import FtsRetriever
from ensemble_retriever import EnsembleRetriever


def main():
    parser = argparse.ArgumentParser(description="EnsembleRetriever：向量 + 全文 RRF 融合")
    parser.add_argument("query", nargs="?", default="项目有哪些 demo", help="查询文本")
    parser.add_argument("-k", type=int, default=RETRIEVE_K, help="最终返回条数")
    parser.add_argument(
        "--weights",
        type=float,
        nargs=2,
        default=[0.5, 0.5],
        metavar=("W_VECTOR", "W_FTS"),
        help="向量检索与全文检索的权重，默认 0.5 0.5",
    )
    args = parser.parse_args()

    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    vector_retriever = VectorRetriever(
        connection_str=PG_CONNECTION_PSYCOPG,
        table_name=TABLE_NAME,
        k=FETCH_K,
        embeddings=embeddings,
    )
    fts_retriever = FtsRetriever(
        connection_str=PG_CONNECTION_PSYCOPG,
        table_name=TABLE_NAME,
        k=FETCH_K,
    )
    ensemble = EnsembleRetriever(
        retrievers=[vector_retriever, fts_retriever],
        weights=args.weights,
        k=RRF_K,
        top_k=args.k,
    )
    docs = ensemble.invoke(args.query)
    print("--- EnsembleRetriever（向量 + 全文 RRF）---\n")
    print(f"query: {args.query}")
    print(f"weights: 向量={args.weights[0]}, 全文={args.weights[1]}\n")
    for i, d in enumerate(docs, 1):
        print(f"[{i}] {d.page_content[:200].replace(chr(10), ' ')}...")
        print()


if __name__ == "__main__":
    main()
