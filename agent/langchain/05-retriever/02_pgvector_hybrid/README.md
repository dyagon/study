# 02_pgvector_hybrid：EnsembleRetriever

使用 **EnsembleRetriever** 将多路检索结果用 **RRF（Reciprocal Rank Fusion）** 合并。

- **向量检索**：查 `rag_hybrid` 的 `embedding` 列（余弦相似）
- **全文检索**：查 `rag_hybrid` 的 `content_tsv` 列（tsvector）
- **融合**：两路各取若干条，按加权 RRF 得分排序后取 top-k

## 数据准备

与 [04-vector](../../04-vector/) 共用表，请先建表并灌数：

```bash
uv run python langchain/04-vector/init_db.py
uv run python langchain/04-vector/build_db.py <文档目录>
```

## 运行

```bash
# 默认权重 0.5 / 0.5
uv run python langchain/05-retriever/02_pgvector_hybrid/app.py "你的问题"

# 自定义权重（向量 0.7，全文 0.3）
uv run python langchain/05-retriever/02_pgvector_hybrid/app.py "你的问题" --weights 0.7 0.3

# 指定返回条数
uv run python langchain/05-retriever/02_pgvector_hybrid/app.py "你的问题" -k 8
```

## 实现说明

当前 LangChain 安装中未提供 `langchain.retrievers.EnsembleRetriever`，本 demo 在 `ensemble_retriever.py` 中实现了等价的 RRF 融合逻辑，与官方文档中的 EnsembleRetriever 行为一致。
