"""
使用 langchain_postgres.PGVector.as_retriever()：相似检索与 MMR 检索示例。

需先执行：uv run python langchain/05-retriever/build_db.py <文档目录>

用法：
  uv run python langchain/05-retriever/02_pgvector_native/app.py "你的问题"
  uv run python langchain/05-retriever/02_pgvector_native/app.py "你的问题" --mmr   # MMR 检索
"""
import argparse

from langchain_community.embeddings import DashScopeEmbeddings
from langchain_postgres import PGVector

from config import PG_CONNECTION_LC, COLLECTION_NAME, RETRIEVE_K


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("query", nargs="?", default="项目有哪些 demo", help="查询文本")
    parser.add_argument("--mmr", action="store_true", help="使用 MMR 检索（否则为普通相似检索）")
    parser.add_argument("-k", type=int, default=RETRIEVE_K, help="返回条数")
    args = parser.parse_args()

    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    vector_store = PGVector(
        embeddings=embeddings,
        collection_name=COLLECTION_NAME,
        connection=PG_CONNECTION_LC,
        use_jsonb=True,
    )
    if args.mmr:
        retriever = vector_store.as_retriever(
            search_type="mmr",
            search_kwargs={"k": args.k, "fetch_k": min(20, args.k * 4), "lambda_mult": 0.7},
        )
        label = "MMR"
    else:
        retriever = vector_store.as_retriever(search_kwargs={"k": args.k})
        label = "相似检索"

    docs = retriever.invoke(args.query)
    print(f"--- PGVector.as_retriever（{label}） query: {args.query} ---\n")
    for i, d in enumerate(docs, 1):
        print(f"[{i}] {d.page_content[:200].replace(chr(10), ' ')}...")
        print()


if __name__ == "__main__":
    main()
