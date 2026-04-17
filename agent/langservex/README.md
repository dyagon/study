# LangServe + DeepSeek-R1 思维链 Demo

使用 **deepseek-reasoner**（DeepSeek-R1）的 LangServe 示例：LCEL 链暴露为 REST API，可选 Chainlit 对话界面。

## 环境

- 环境变量 **DEEPSEEK_API_KEY**（[DeepSeek API Key](https://platform.deepseek.com/api_keys)）由 uv 或运行环境注入。

## 1. 启动 LangServe API

在**项目根目录**执行：

```bash
uv run uvicorn langservex.server:app --reload --port 8080
```

- 文档与探索：<http://localhost:8080/docs>
- 链路径：`/reasoner`（支持 `invoke`、`stream`、`batch`）
- 带思考过程：`/reasoner_think`（输出 AIMessage，流式含 `reasoning_content`；调用 `stream_events` 可拿到思考过程与正文）
- 请求体示例：`{"input": "用三步解释什么是 LangChain"}`

## 2. 启动 Chainlit 对话（可选）

在**项目根目录**另开终端：

```bash
uv run chainlit run langservex/chainlit_app.py -w --port 8050
```

浏览器打开 <http://localhost:8050>。界面会通过 **LangServe 的 stream_events** 展示「思考过程」与最终回答：
- **不设 LANGSERVE_URL**：使用本地 `chain_with_thinking.astream_events()`，同样展示思考步骤。
- **设置 LANGSERVE_URL**（需先启动上面的 API）：`export LANGSERVE_URL=http://localhost:8080`，Chainlit 会请求 `/reasoner_think/stream_events`，用接口返回的 `reasoning_content` 显示思考过程。

## 结构说明

| 文件 | 说明 |
|------|------|
| `thinking_chain.py` | `chain`：Prompt → LLM → StrOutputParser；`chain_with_thinking`：Prompt → LLM（用于 stream_events 展示 reasoning_content） |
| `server.py` | FastAPI + `add_routes`：`/reasoner`、`/reasoner_think` |
| `chainlit_app.py` | Chainlit 前端，用 stream_events 展示思考过程 + 流式正文 |
