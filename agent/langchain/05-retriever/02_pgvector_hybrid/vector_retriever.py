"""向量检索 Retriever：查 rag_hybrid 的 embedding 列。"""
import psycopg
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from langchain_community.embeddings import DashScopeEmbeddings


class VectorRetriever(BaseRetriever):
    """基于 rag_hybrid 表的向量相似检索。"""

    connection_str: str
    table_name: str
    k: int = 5
    embeddings: DashScopeEmbeddings = None

    def _get_relevant_documents(self, query: str) -> list[Document]:
        vec = self.embeddings.embed_query(query)
        vec_str = "[" + ",".join(str(x) for x in vec) + "]"
        docs = []
        with psycopg.connect(self.connection_str) as conn:
            with conn.cursor() as cur:
                cur.execute(
                    f"""
                    SELECT id::text, content, metadata
                    FROM {self.table_name}
                    ORDER BY embedding <=> %s::vector
                    LIMIT %s
                    """,
                    (vec_str, self.k),
                )
                for row in cur.fetchall():
                    doc_id, content, metadata = row[0], row[1], row[2] or {}
                    if not isinstance(metadata, dict):
                        metadata = {}
                    metadata["id"] = doc_id
                    docs.append(Document(page_content=content, metadata=metadata))
        return docs
