"""全文检索 Retriever：查 rag_hybrid 的 content_tsv 列。"""
import re
import psycopg
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever


class FtsRetriever(BaseRetriever):
    """基于 rag_hybrid 表 content_tsv 的全文检索。"""

    connection_str: str
    table_name: str
    k: int = 5

    def _get_relevant_documents(self, query: str) -> list[Document]:
        words = re.sub(r"\s+", " ", query.strip()).split()
        if not words:
            return []
        q = " & ".join(words)
        docs = []
        with psycopg.connect(self.connection_str) as conn:
            with conn.cursor() as cur:
                cur.execute(
                    f"""
                    SELECT id::text, content, metadata
                    FROM {self.table_name}
                    WHERE content_tsv @@ plainto_tsquery('simple', %s)
                    ORDER BY ts_rank_cd(content_tsv, plainto_tsquery('simple', %s)) DESC
                    LIMIT %s
                    """,
                    (q, q, self.k),
                )
                for row in cur.fetchall():
                    doc_id, content, metadata = row[0], row[1], row[2] or {}
                    if not isinstance(metadata, dict):
                        metadata = {}
                    metadata["id"] = doc_id
                    docs.append(Document(page_content=content, metadata=metadata))
        return docs
