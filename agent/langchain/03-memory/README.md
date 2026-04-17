# 03-memory

LangChain 对话记忆（Memory）若干示例，均基于 **通义千问** 与 **RunnableWithMessageHistory / 自定义 History**。

| 示例 | 说明 |
|------|------|
| [01_buffer](01_buffer/) | **完整历史**：`RunnableWithMessageHistory` + `ChatMessageHistory`，会话内保留全部对话 |
| [02_window](02_window/) | **滑动窗口**：仅保留最近 K 轮对话，避免上下文过长 |
| [03_summary](03_summary/) | **摘要记忆**：用 LLM 将旧对话压缩为摘要，只保留摘要 + 最近一两轮原文 |

## 环境

- `DASHSCOPE_API_KEY` 由 uv 或运行环境注入
- 在项目根目录执行各 demo 的入口脚本

## 运行

```bash
# 完整历史（多轮对话，问“我叫什么”会记住之前说的名字）
uv run python langchain/03-memory/01_buffer/app.py

# 滑动窗口（只保留最近 2 轮，更早的会被丢弃）
uv run python langchain/03-memory/02_window/app.py

# 摘要记忆（旧对话被压缩成一段摘要，再与最新一轮一起喂给模型）
uv run python langchain/03-memory/03_summary/app.py
```

## 依赖

与项目主依赖一致：`langchain`、`langchain-community`（含 `ChatTongyi`、`ChatMessageHistory`）、`langchain-core`（`RunnableWithMessageHistory`、`MessagesPlaceholder`）。
