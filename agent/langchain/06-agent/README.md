# 06-agent：LangChain Agent（ReAct + Tools）示例

基于 **LangGraph** 的 `create_react_agent` 与 **通义千问**，演示带工具的 ReAct Agent：模型可自主决定是否调用工具、多次推理与执行直到给出最终回答。

## 示例说明

- **ReAct**：Reasoning（推理）+ Acting（调用工具）+ Observation（观察结果），循环直到完成。
- **Tools**：本 demo 提供计算器、当前时间等简单工具，便于本地运行、无需额外 API。
- **LLM**：使用 `ChatTongyi`（qwen），需支持 tool calling。

## 运行

```bash
# 交互式：启动后输入多轮问题，输入空行或 Ctrl+C 退出
uv run python langchain/06-agent/app.py

# 单次提问
uv run python langchain/06-agent/app.py "3 的 5 次方是多少？"
uv run python langchain/06-agent/app.py "现在几点了？北京和纽约各是几点？"
```

## 环境

- `DASHSCOPE_API_KEY`：通义千问 API Key（与 01-simple、02-article-gen 等一致）

## 依赖

- `langgraph`（含 `langgraph-prebuilt` 的 `create_react_agent`）
- `langchain`、`langchain-community`（ChatTongyi、tools）
