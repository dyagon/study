"""
简易多向量检索器：按 query 在 vectorstore 中检索（得到带 doc_id 的子文档），
再从 docstore 用 doc_id 取回父文档并去重返回。与 LangChain MultiVectorRetriever 行为一致，
不依赖 langchain.retrievers 包。
"""
from langchain_core.documents import Document


class MultiVectorRetriever:
    """摘要/子文档向量检索 -> 按 id_key 取回父文档。"""

    def __init__(
        self,
        *,
        vectorstore,
        docstore,
        id_key: str = "doc_id",
        search_kwargs: dict | None = None,
    ):
        self.vectorstore = vectorstore
        self.docstore = docstore
        self.id_key = id_key
        self.search_kwargs = search_kwargs or {"k": 4}

    def invoke(self, query: str) -> list[Document]:
        k = self.search_kwargs.get("k", 4)
        sub_docs = self.vectorstore.similarity_search(query, k=k)
        doc_ids = []
        seen = set()
        for d in sub_docs:
            vid = d.metadata.get(self.id_key)
            if vid is not None and vid not in seen:
                seen.add(vid)
                doc_ids.append(vid)
        if not doc_ids:
            return []
        parents = self.docstore.mget(doc_ids)
        return [p for p in parents if p is not None]
