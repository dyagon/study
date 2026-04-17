# 04-multivector：多向量检索 + 对话式 QA

**多向量检索** demo：每个文档存两种表示——**摘要向量**（用于检索）与**父文档**（用于返回）。使用 LangChain 的 `MultiVectorRetriever`：按摘要相似度检索，再根据 `doc_id` 从 docstore 取回完整父文档，供 RAG 生成回答。

## 与 02_pgvector_hybrid / langgraph/02-qa 的区别

| 项目 | 块向量 / 02-qa | 04-multivector |
|------|----------------|----------------|
| 向量来源 | 文档切块后直接对**块**做 embedding | 对每个**父文档**生成摘要，对**摘要**做 embedding |
| 检索结果 | 返回若干**块** | 按摘要相似度命中后，返回对应**父文档**（完整） |
| 存储 | 仅 PGVector | PGVector（摘要向量）+ 持久化 docstore（父文档） |
| 适用场景 | 细粒度匹配、长文档按块检索 | 希望“命中即得整篇”、摘要更稳时 |

## 依赖

- 环境变量 **DASHSCOPE_API_KEY** 由 uv 或运行环境注入。
- 先启动 pgvector：`cd dev && docker compose up -d`。
- 可选环境变量：`PG_CONNECTION`、`QA_MULTIVECTOR_COLLECTION`（默认 `qa_multivector`）、`QA_MULTIVECTOR_DOCSTORE`（默认 `langchain/05-retriever/04-multivector/docstore.json`）、`QA_MULTIVECTOR_RETRIEVE_K`（默认 4）。

## 步骤一：构建（摘要 + 父文档）

每个 .md 会经 LLM 生成简短摘要，摘要向量写入 PGVector，父文档写入本地 docstore 文件：

```bash
uv run python langchain/05-retriever/04-multivector/build_db.py <文档目录>
```

示例：

```bash
uv run python langchain/05-retriever/04-multivector/build_db.py langgraph/02-qa/docs
```

## 步骤二：测试检索

```bash
uv run python langchain/05-retriever/04-multivector/test_retrieval.py "项目里有哪些 demo？"
```

会打印按摘要检索到的**父文档**片段，再基于这些文档做一次 RAG 回答。

## 步骤三：对话式 QA

```bash
uv run streamlit run langchain/05-retriever/04-multivector/app.py
```

多轮提问；底层使用 `MultiVectorRetriever`，返回的是父文档。

## 技术要点

- **MultiVectorRetriever**：`vectorstore`（PGVector，存摘要向量）+ `docstore`（本 demo 为基于 JSON 文件的 `FileDocstore`）+ `id_key="doc_id"` 关联摘要与父文档。
- **建库**：父文档 = 每个 .md 文件；子表示 = 该文档的 LLM 摘要；摘要 embedding 入 PGVector，metadata 带 `doc_id`；父文档入 docstore，key 为同一 `doc_id`。
- **检索**：对 query 做向量检索 → 得到带 `doc_id` 的摘要文档 → 用 docstore 的 `mget(doc_ids)` 取回父文档列表 → 作为 RAG 上下文。
