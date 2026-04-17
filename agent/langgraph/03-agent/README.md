# 03-agent

使用 **LangGraph** 的 **StateGraph** 手写 ReAct Agent：agent 节点与 tools 节点通过**条件边**循环，直到模型不再调用工具为止。

## 与 langchain/06-agent 的区别

- **langchain/06-agent**：使用 `langgraph.prebuilt.create_react_agent(model, tools)`，一行组网。
- **本 demo**：用 LangGraph 的图 API 自己建节点与边，便于理解 ReAct 循环和后续扩展（例如加审核节点、多步规划等）。

## 图结构

- **状态**：`AgentState` 仅包含 `messages: Annotated[Sequence[BaseMessage], add_messages]`。
- **节点**：
  - **agent**：在消息前追加 system prompt，调用 `ChatTongyi.bind_tools(tools)`，得到可能带 `tool_calls` 的 AIMessage。
  - **tools**：根据上一条消息的 `tool_calls` 依次执行工具，拼出 ToolMessage 列表并追加到 state。
- **边**：
  - 入口 → **agent**。
  - **agent** 后**条件边**：若最后一条消息有 `tool_calls` → 进入 **tools**，否则 → **END**。
  - **tools** → **agent**（执行完工具后继续推理）。

## 工具

- `calculator(expression)`：计算数学表达式（仅数字与 + - * / ** ( )）。
- `get_current_time(timezone_name)`：返回指定时区当前时间。
- `get_weather(city)`：查询城市当前天气（OpenWeather API），需配置 `OPENWEATHER_API_KEY`。

## 运行

在项目根目录执行：

```bash
export DASHSCOPE_API_KEY=你的key

# 单次提问
uv run python langgraph/03-agent/app.py "3 的 5 次方是多少？"
uv run python langgraph/03-agent/app.py "现在北京和纽约各是几点？"

# 打印每步输入输出与工具调用（调试）
uv run python langgraph/03-agent/app.py -v "北京天气怎么样"
uv run python langgraph/03-agent/app.py --verbose "1+2*3"

# 交互式多轮（可先加 -v 再输入问题，该轮会打印节点流）
uv run python langgraph/03-agent/app.py
uv run python langgraph/03-agent/app.py -v
```

## 环境变量

- **DASHSCOPE_API_KEY**：必填，通义千问 API Key。
- **AGENT_MODEL_NAME**：可选，默认 `qwen-max`。
- **OPENWEATHER_API_KEY**：可选，OpenWeather API Key；未设置时天气工具会返回提示。申请见 [OpenWeather API](https://openweathermap.org/api)。

## 文件说明

| 文件       | 说明 |
|------------|------|
| `config.py` | 模型名等配置 |
| `tools.py`  | 计算器、当前时间等 @tool 定义 |
| `graph.py`  | StateGraph：agent / tools 节点与条件边 |
| `app.py`    | CLI：单次或交互式调用图 |
