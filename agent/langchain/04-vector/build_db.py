"""
从指定目录的 Markdown 构建混合检索表：切块、embedding 写入 rag_hybrid（含 content_tsv 自动生成）。
执行前请先运行 schema.sql 建表。
"""
import argparse
import json
from pathlib import Path

import psycopg
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.embeddings import DashScopeEmbeddings

from config import (
    PG_CONNECTION,
    TABLE_NAME,
    CHUNK_SIZE,
    CHUNK_OVERLAP,
)


def load_md_docs(dir_path: Path) -> list[Document]:
    docs = []
    for p in sorted(dir_path.rglob("*.md")):
        try:
            text = p.read_text(encoding="utf-8")
        except Exception as e:
            print(f"跳过 {p}: {e}")
            continue
        rel = str(p.relative_to(dir_path))
        docs.append(Document(page_content=text, metadata={"source": rel}))
    return docs


def main():
    parser = argparse.ArgumentParser(description="构建混合检索表（向量 + tsvector）")
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
    texts = [c.page_content for c in chunks]
    vecs = embeddings.embed_documents(texts)

    with psycopg.connect(PG_CONNECTION) as conn:
        with conn.cursor() as cur:
            for doc, vec in zip(chunks, vecs):
                vec_str = "[" + ",".join(str(x) for x in vec) + "]"
                meta = {k: v for k, v in doc.metadata.items() if isinstance(v, (str, int, float, bool))}
                cur.execute(
                    f"INSERT INTO {TABLE_NAME} (content, embedding, metadata) VALUES (%s, %s::vector, %s::jsonb)",
                    (doc.page_content, vec_str, json.dumps(meta)),
                )
    print(f"已写入表 {TABLE_NAME}，共 {len(chunks)} 条（content_tsv 由触发器自动生成）")


if __name__ == "__main__":
    main()
