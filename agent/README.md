# agent-demo

用于学习 Agent / LLM 应用的示例项目。**每个子目录一个独立 demo，用单独命令即可运行。**

## 环境准备

- Python 3.12+（推荐用 [uv](https://github.com/astral-sh/uv) 管理）
- 环境变量（如 `DASHSCOPE_API_KEY`）由 uv 或运行环境注入，无需 .env

```bash
# 安装依赖（在项目根目录）
uv sync
```

## 项目结构

```
agent-demo/
├── README.md           # 本说明
├── main.py             # 列出所有 demo 及运行命令
├── pyproject.toml      # 项目与依赖
├── langchain/          # LangChain 示例
│   ├── 01-simple/           # LCEL 简单链
│   ├── 02-article-gen/      # Streamlit 文章生成
│   ├── 03-memory/           # 对话记忆（buffer / window / summary）
│   │   ├── 01_buffer/
│   │   ├── 02_window/
│   │   └── 03_summary/
│   ├── 04-vector/           # PGVector 混合检索（向量 + tsvector）
│   ├── 05-retriever/        # Retriever 示例（PGVector / 混合 / BM25 / 多向量）
│   │   ├── 01_pgvector_native/
│   │   ├── 02_pgvector_hybrid/
│   │   ├── 03_bm25_ensemble/
│   │   └── 04-multivector/
│   └── 06-agent/            # ReAct Agent（create_react_agent）
├── langgraph/          # LangGraph 示例
│   ├── 01-simple/           # StateGraph 多节点流程
│   ├── 02-qa/               # 文档 QA：PGVector + 对话式问答
│   └── 03-agent/            # 手写 ReAct Agent（条件边 + tools）
├── langservex/         # LangServe + Chainlit 思维链对话
└── langsmith/          # LangSmith 评测
    ├── 01-simple/
    └── 02-offline-eval/     # 离线 evaluate()
```

## 运行各 Demo

在**项目根目录**执行：

| Demo | 说明 | 命令 |
|------|------|------|
| langchain/01-simple | LangChain + Qwen 简单链 | `uv run python langchain/01-simple/main.py` |
| langchain/02-article-gen | Streamlit 文章生成（topic→标题→正文） | `uv run streamlit run langchain/02-article-gen/app.py` |
| langchain/03-memory | 对话记忆：buffer / window / summary | 见 `langchain/03-memory/README.md` |
| langchain/04-vector | PGVector 混合检索（向量 + 全文 RRF） | 见 `langchain/04-vector/README.md` |
| langchain/05-retriever | Retriever：PGVector 原生 / 混合 / BM25 / 多向量 | 见 `langchain/05-retriever/README.md` |
| langchain/06-agent | ReAct Agent（create_react_agent + Tools） | `uv run python langchain/06-agent/app.py` |
| langgraph/01-simple | LangGraph StateGraph 多节点 | `uv run python langgraph/01-simple/main.py` |
| langgraph/02-qa | 文档 QA：build_db → test_retrieval → streamlit | 见 `langgraph/02-qa/README.md` |
| langgraph/03-agent | 手写 ReAct Agent（条件边 + 工具循环） | `uv run python langgraph/03-agent/app.py` |
| langservex | Chainlit 思维链对话 / LangServe API | 见 `langservex/README.md` |
| langsmith/02-offline-eval | LangSmith 离线 evaluate() | `uv run python langsmith/02-offline-eval/main.py` |

新增 demo 时，在对应主题目录下新建子目录，放入 `main.py` 或 `app.py` 及可选 `README.md`，并在上表中补充一行运行命令即可。
