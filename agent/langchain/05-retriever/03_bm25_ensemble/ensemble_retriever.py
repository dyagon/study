"""多路 Retriever 加权 RRF 融合。"""
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever


def _rrf_score(rank: int, k: int = 60) -> float:
    return 1.0 / (rank + k)


class EnsembleRetriever(BaseRetriever):
    retrievers: list[BaseRetriever]
    weights: list[float] | None = None
    k: int = 60
    top_k: int = 5

    def _get_relevant_documents(self, query: str) -> list[Document]:
        weights = self.weights
        if not weights or len(weights) != len(self.retrievers):
            weights = [1.0] * len(self.retrievers)
        fetch_k = max(self.top_k * 2, 10)
        scores: dict[str, tuple[Document, float]] = {}
        for retriever, w in zip(self.retrievers, weights):
            docs = retriever.invoke(query)[:fetch_k]
            for rank, doc in enumerate(docs, start=1):
                key = doc.metadata.get("id") or ("content:" + str(hash(doc.page_content[:500])))
                rrf = w * _rrf_score(rank, self.k)
                if key in scores:
                    scores[key] = (scores[key][0], scores[key][1] + rrf)
                else:
                    scores[key] = (doc, rrf)
        sorted_docs = sorted(scores.values(), key=lambda x: -x[1])
        return [doc for doc, _ in sorted_docs[: self.top_k]]
