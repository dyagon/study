"""
基于 JSON 文件的持久化 docstore，供 MultiVectorRetriever 使用。
实现 mget/mset 接口（与 LangChain docstore 约定一致）。
"""
import json
from pathlib import Path
from typing import Optional, Sequence

from langchain_core.documents import Document


def _doc_to_dict(doc: Document) -> dict:
    return {"page_content": doc.page_content, "metadata": doc.metadata}


def _dict_to_doc(data: dict) -> Document:
    return Document(page_content=data["page_content"], metadata=data.get("metadata", {}))


class FileDocstore:
    """持久化到单文件的 Document 存储，支持 mget/mset。"""

    def __init__(self, path: Path) -> None:
        self._path = Path(path)
        self._data: dict[str, Document] = {}
        if self._path.exists():
            raw = json.loads(self._path.read_text(encoding="utf-8"))
            for k, v in raw.items():
                self._data[k] = _dict_to_doc(v)

    def mget(self, keys: Sequence[str]) -> list[Optional[Document]]:
        return [self._data.get(k) for k in keys]

    def mset(self, key_value_pairs: Sequence[tuple[str, Document]]) -> None:
        for k, v in key_value_pairs:
            self._data[k] = v
        self._save()

    def _save(self) -> None:
        out = {k: _doc_to_dict(v) for k, v in self._data.items()}
        self._path.parent.mkdir(parents=True, exist_ok=True)
        self._path.write_text(json.dumps(out, ensure_ascii=False, indent=0), encoding="utf-8")
