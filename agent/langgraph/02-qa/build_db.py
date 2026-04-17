"""
基于指定目录下的 Markdown 文档构建 PGVector 向量库。
运行前请确保 dev 下已启动 pgvector：cd dev && docker compose up -d
环境变量（如 DASHSCOPE_API_KEY）由 uv 等运行时注入，无需 .env。
"""
import argparse
from pathlib import Path

from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_postgres import PGVector

from config import (
    PG_CONNECTION,
    COLLECTION_NAME,
    CHUNK_SIZE,
    CHUNK_OVERLAP,
)


def load_md_docs(dir_path: Path) -> list[Document]:
    """递归加载目录下所有 .md 文件为 Document。"""
    docs = []
    for p in sorted(dir_path.rglob("*.md")):
        try:
            text = p.read_text(encoding="utf-8")
        except Exception as e:
            print(f"跳过 {p}: {e}")
            continue
        docs.append(
            Document(
                page_content=text,
                metadata={"source": str(p.relative_to(dir_path))},
            )
        )
    return docs


def main():
    parser = argparse.ArgumentParser(description="从 Markdown 目录构建 PGVector 向量库")
    parser.add_argument("docs_dir", type=Path, help="Markdown 文档所在目录（绝对或相对路径）")
    args = parser.parse_args()
    docs_dir = args.docs_dir.resolve()

    if not docs_dir.exists():
        print(f"文档目录不存在: {docs_dir}")
        return
    if not docs_dir.is_dir():
        print(f"请指定目录: {docs_dir}")
        return

    raw = load_md_docs(docs_dir)
    if not raw:
        print(f"在 {docs_dir} 下未找到 .md 文件")
        return

    splitter = RecursiveCharacterTextSplitter(
        chunk_size=CHUNK_SIZE,
        chunk_overlap=CHUNK_OVERLAP,
        separators=["\n## ", "\n### ", "\n\n", "\n", " "],
    )
    chunks = splitter.split_documents(raw)
    print(f"加载 {len(raw)} 个文件，切分为 {len(chunks)} 个片段（目录: {docs_dir}）")

    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    vector_store = PGVector(
        embeddings=embeddings,
        collection_name=COLLECTION_NAME,
        connection=PG_CONNECTION,
        use_jsonb=True,
    )
    vector_store.add_documents(chunks)
    print(f"已写入 PGVector，集合名: {COLLECTION_NAME}")


if __name__ == "__main__":
    main()
