# 04-vector：PGVector 混合检索（向量 + 全文）

使用 **PGVector** 做**混合检索**：同一张表既有 **embedding 向量列**，又有 **tsvector 全文列**；支持仅向量检索与「向量 + 全文 RRF 融合」两种方式，命令行演示单向量 vs 混合的排序差异。

## 表结构（建表）

表内包含：
- `content`：原文
- `embedding`：向量（维度需与模型一致，如 1536）
- `content_tsv`：**tsvector**，由 `content` 自动生成，用于全文检索

```bash
uv run python langchain/04-vector/init_db.py
```

会按 `config.py` 中的 `TABLE_NAME`、`EMBEDDING_DIM` 创建表、索引及 `rrf_score` 函数。

## 建库

```bash
uv run python langchain/04-vector/build_db.py <文档目录>
```

示例：

```bash
uv run python langchain/04-vector/build_db.py langgraph/02-qa/docs
```

会将目录下 `.md` 切块、调用 DashScope 做 embedding，写入 `rag_hybrid`；`content_tsv` 由表上的 GENERATED 列自动维护。

## 查询演示：单向量 vs 混合

- **单向量检索**：只按 embedding 相似度排序。
- **混合检索**：向量相似度一路 + 全文（tsvector）一路，两路各取若干条后用 **RRF** 融合得分，再按融合分排序。

```bash
# 仅向量
uv run python langchain/04-vector/query.py "项目有哪些 demo" --mode vector

# 混合（向量 + 全文 RRF）
uv run python langchain/04-vector/query.py "项目有哪些 demo" --mode hybrid
```

可对同一句查询分别跑 `--mode vector` 和 `--mode hybrid`，对比返回顺序和内容差异（例如关键词精确匹配时，混合往往会把含该词的文档提前）。

可选参数：`-k N` 控制返回条数，默认 5。

## 技术要点

- **单向量**：`ORDER BY embedding <=> $query_vector LIMIT k`。
- **全文**：`WHERE content_tsv @@ plainto_tsquery('simple', $query)`，`ORDER BY ts_rank_cd(content_tsv, ...) DESC`。
- **混合**：两路各取 `HYBRID_FETCH_K` 条，按 RRF 公式 `1/(rank + rrf_k)` 合并得分后取 top-k。
- 环境变量：`DASHSCOPE_API_KEY` 用于 embedding；可选 `PG_CONNECTION`、`RAG_HYBRID_TABLE`、`RAG_EMBEDDING_DIM`。
