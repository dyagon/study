"""
多向量检索建库：为每个父文档生成摘要，摘要向量存入 PGVector，父文档存入持久化 docstore。
检索时按摘要相似度命中，返回对应父文档（多向量 = 摘要向量 + 父文档存储）。
"""
import argparse
import uuid
from pathlib import Path

from langchain_core.documents import Document
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_postgres import PGVector

from config import (
    PG_CONNECTION,
    MULTIVECTOR_COLLECTION,
    DOCSTORE_PATH,
    ID_KEY,
)
from file_docstore import FileDocstore


def load_md_docs(dir_path: Path) -> list[Document]:
    """递归加载目录下所有 .md 文件为 Document。"""
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


def summarize_doc(doc: Document) -> str:
    """用 LLM 生成单文档简短摘要（用于向量化）。"""
    llm = ChatTongyi(model="qwen-plus", temperature=0.2)
    prompt = ChatPromptTemplate.from_messages([
        ("system", "请用 1～3 句话概括下面文档的核心内容，用于后续检索匹配。只输出摘要正文，不要标题或解释。"),
        ("user", "{text}"),
    ])
    # 避免过长
    text = doc.page_content[:4000] if len(doc.page_content) > 4000 else doc.page_content
    chain = prompt | llm | StrOutputParser()
    return chain.invoke({"text": text}).strip()


def main():
    parser = argparse.ArgumentParser(description="多向量检索：从 Markdown 目录构建摘要向量 + docstore")
    parser.add_argument("docs_dir", type=Path, help="Markdown 文档所在目录")
    args = parser.parse_args()
    docs_dir = args.docs_dir.resolve()

    if not docs_dir.exists() or not docs_dir.is_dir():
        print(f"目录不存在或不是目录: {docs_dir}")
        return

    parents = load_md_docs(docs_dir)
    if not parents:
        print(f"在 {docs_dir} 下未找到 .md 文件")
        return

    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    vector_store = PGVector(
        embeddings=embeddings,
        collection_name=MULTIVECTOR_COLLECTION,
        connection=PG_CONNECTION,
        use_jsonb=True,
    )
    docstore = FileDocstore(DOCSTORE_PATH)

    # 每个父文档：生成摘要 -> 摘要向量入 PGVector，父文档入 docstore
    for i, parent in enumerate(parents):
        doc_id = str(uuid.uuid4())
        parent.metadata[ID_KEY] = doc_id
        summary = summarize_doc(parent)
        child = Document(page_content=summary, metadata={ID_KEY: doc_id})
        vector_store.add_documents([child])
        docstore.mset([(doc_id, parent)])
        print(f"[{i+1}/{len(parents)}] {parent.metadata.get('source', doc_id)} -> 摘要已向量化")

    print(f"已写入 PGVector 集合 {MULTIVECTOR_COLLECTION}，docstore: {DOCSTORE_PATH}")


if __name__ == "__main__":
    main()
