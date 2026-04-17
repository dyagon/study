# 02-qa

基于 **PGVector** 与 **LangGraph** 的文档 QA demo：从指定目录的 Markdown 构建向量库，支持单次检索测试与**对话式**多轮问答。

## 依赖

- 环境变量 **DASHSCOPE_API_KEY**（用于 embedding 与 LLM）由 uv 或运行环境注入，无需 .env。
- 启动 pgvector（与 `dev/docker-compose.yml` 一致）：
  ```bash
  cd dev && docker compose up -d
  ```
- 可选环境变量：
  - **PG_CONNECTION**：PG 连接串，默认 `postgresql+psycopg://postgres:postgres@localhost:5432/agent`
  - **QA_COLLECTION_NAME**：向量集合名，默认 `qa_docs`

## 步骤一：构建向量库

在**项目根目录**执行，**文档目录由命令行指定**：

```bash
uv run python langgraph/02-qa/build_db.py <文档目录>
```

示例（使用本 demo 自带的 docs）：

```bash
uv run python langgraph/02-qa/build_db.py langgraph/02-qa/docs
```

会将指定目录下所有 `.md` 文件切块、embed 后写入 PGVector。

## 步骤二：测试检索与单次 QA

在**项目根目录**执行：

```bash
uv run python langgraph/02-qa/test_retrieval.py "项目里有哪些 demo？"
```

会先做相似检索并打印片段，再基于检索结果用 LLM 生成回答。

## 步骤三：对话式 QA（LangGraph + Streamlit）

在**项目根目录**执行：

```bash
uv run streamlit run langgraph/02-qa/app.py
```

在页面中可多轮提问，每轮会先检索再生成回答，并保留对话历史。

## 使用技术

### 文档切块（Build 阶段）

- **切块器**：`RecursiveCharacterTextSplitter`（LangChain），按分隔符优先级依次尝试切分，尽量保持段落/标题完整。
- **分隔符顺序**：`["\n## ", "\n### ", "\n\n", "\n", " "]`（二级标题 → 三级标题 → 段落 → 行 → 空格），适合 Markdown 结构。
- **参数**（在 `config.py` 中）：
  - `CHUNK_SIZE = 800`：单块最大字符数
  - `CHUNK_OVERLAP = 150`：块间重叠字符数，减少边界截断
- 切块后的文本经 **DashScope `text-embedding-v3`** 向量化后写入 **PGVector**，检索时按当前问题做相似度检索，取 top-**k**（默认 `RETRIEVE_K = 4`）作为上下文。

### 向量与检索

- **向量库**：PostgreSQL + pgvector 扩展（见 `dev/docker-compose.yml`），通过 **langchain-postgres** 的 `PGVector` 使用。
- **Embedding**：`DashScopeEmbeddings(model="text-embedding-v3")`。
- **检索**：`vector_store.as_retriever(search_kwargs={"k": RETRIEVE_K})`，按问题向量做相似检索，得到若干 `Document` 片段供生成阶段使用。

### LangGraph 图与会话记忆

- **图结构**：`StateGraph(QAState)`，两节点线性串联：
  - **retrieve**：根据当前问题从 PGVector 检索文档，写入 state 的 `retrieved_docs`。
  - **generate**：将 `retrieved_docs` 拼成参考上下文，与 **chat_history** 一起注入 system prompt，调用 LLM（通义 qwen-plus）生成回答，写入 state 的 `answer`。
- **状态定义**：`QAState` 包含 `question`、`chat_history`、`retrieved_docs`、`answer`。其中 **会话记忆** 由 `chat_history: list[tuple[str, str]]` 表示，即历史多轮 (用户问, 助手答) 列表。
- **记忆由谁维护**：图本身是**无状态**的，不持久化历史；每轮对话由**调用方**传入当轮 `question` 和当前 `chat_history`。在 Streamlit 中：
  - 使用 `st.session_state.chat_history` 保存历史；
  - 每次用户提问后 `invoke(QAState(..., chat_history=st.session_state.chat_history, ...))`，得到本轮 `answer` 再 `append((prompt, result["answer"]))` 并 `st.rerun()`。
- **生成时如何使用历史**：在 **generate** 节点里，将 `chat_history` 格式化为 `"用户: q\n助手: a\n..."` 字符串，放入 system prompt 的「当前对话历史」部分，使 LLM 能基于多轮上下文作答（例如指代、追问、澄清）。

## 文件说明

| 文件 | 说明 |
|------|------|
| `config.py` | 连接串、集合名、分块与检索参数 |
| `build_db.py` | 从命令行指定目录的 Markdown 构建 PGVector |
| `test_retrieval.py` | 单次检索 + RAG 回答测试 |
| `graph.py` | LangGraph：retrieve → generate |
| `app.py` | Streamlit 对话界面 |
| `docs/` | 示例 Markdown 文档目录（可任意指定其他目录） |
