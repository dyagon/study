# 03_bm25_ensemble：向量 + BM25 EnsembleRetriever

使用 **EnsembleRetriever** 将 **PGVector 向量检索** 与 **BM25 关键词检索** 结果按 RRF 融合。

- **向量检索**：查 `rag_hybrid` 的 `embedding` 列（DashScope embedding）。
- **BM25 检索**：从 `rag_hybrid` 加载全部文档到内存，用 `rank_bm25` 建 BM25 索引，按关键词相似度检索。
- **融合**：两路各取若干条，加权 RRF 后取 top-k。

## 依赖

- 数据：与 [04-vector](../../04-vector/) 共用表，先执行 `init_db.py` 与 `build_db.py <文档目录>`。
- Python 包：`rank_bm25`（BM25Retriever 依赖）。若未安装：
  ```bash
  uv add rank_bm25
  ```

## 运行

```bash
# 默认权重 0.5 / 0.5
uv run python langchain/05-retriever/03_bm25_ensemble/app.py "你的问题"

# 向量 0.7，BM25 0.3
uv run python langchain/05-retriever/03_bm25_ensemble/app.py "你的问题" --weights 0.7 0.3

# 指定返回条数
uv run python langchain/05-retriever/03_bm25_ensemble/app.py "你的问题" -k 8
```

## 说明

- BM25 在启动时从 `rag_hybrid` 全表加载文档建索引，适合中小规模语料；表过大时考虑用 02_pgvector_hybrid（向量 + 全文 FTS）代替 BM25 一路。
- 中文默认按空格分词；如需更好效果可传入自定义 `preprocess_func`（如按字或 jieba 分词）给 `BM25Retriever.from_documents`。
