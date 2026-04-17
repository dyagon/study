"""
使用 langchain_postgres.PGVector 建库：从指定目录的 Markdown 切块、embedding 后写入集合。
供 02_pgvector_native 使用。运行前请确保 pgvector 已启动（如 dev/docker-compose up -d）。
"""
import argparse
from pathlib import Path

from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_postgres import PGVector

_root = Path(__file__).resolve().parent
if str(_root) not in __import__("sys").path:
    __import__("sys").path.insert(0, str(_root))
from config import PG_CONNECTION_LC, COLLECTION_NAME

# 与 03-vector 一致
CHUNK_SIZE = 800
CHUNK_OVERLAP = 150


def load_md_docs(dir_path: Path) -> list[Document]:
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
    parser = argparse.ArgumentParser(description="为 PGVector 集合灌数")
    parser.add_argument("docs_dir", type=Path, help="Markdown 文档目录")
    args = parser.parse_args()
    docs_dir = args.docs_dir.resolve()
    if not docs_dir.exists() or not docs_dir.is_dir():
        print(f"目录不存在或不是目录: {docs_dir}")
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
    print(f"加载 {len(raw)} 个文件，切分为 {len(chunks)} 个片段")

    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    vector_store = PGVector(
        embeddings=embeddings,
        collection_name=COLLECTION_NAME,
        connection=PG_CONNECTION_LC,
        use_jsonb=True,
    )
    vector_store.add_documents(chunks)
    print(f"已写入 PGVector 集合: {COLLECTION_NAME}")


if __name__ == "__main__":
    main()
