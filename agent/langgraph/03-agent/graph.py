"""
LangGraph ReAct Agent：用 StateGraph 手写 agent / tools 两节点 + 条件边。
- agent 节点：调用 LLM（绑定 tools），返回 AIMessage（可能含 tool_calls）
- tools 节点：执行 tool_calls，返回 ToolMessage 列表
- 条件边：若 last_message 有 tool_calls 则走 tools，否则 END
- 边 tools -> agent：执行完工具后回到 agent 继续推理
"""
import json
from typing import Annotated, Sequence, TypedDict

from langchain_core.messages import BaseMessage, SystemMessage, ToolMessage
from langchain_core.runnables import RunnableConfig
from langgraph.graph import StateGraph, END
from langgraph.graph.message import add_messages

from langchain_community.chat_models import ChatTongyi
from config import MODEL_NAME
from tools import get_tools


class AgentState(TypedDict):
    """ReAct 状态：仅用 messages 列表，由 add_messages 做归并。"""
    messages: Annotated[Sequence[BaseMessage], add_messages]


def should_continue(state: AgentState) -> str:
    """条件边：有 tool_calls 则继续执行 tools，否则结束。"""
    last = state["messages"][-1]
    return "continue" if getattr(last, "tool_calls", None) else "end"


def build_agent_graph():
    """构建并编译 ReAct 图：agent <-> tools，条件分支到 END。"""
    tools = get_tools()
    model = ChatTongyi(model=MODEL_NAME, temperature=0.2).bind_tools(tools)
    tools_by_name = {t.name: t for t in tools}

    def call_model(state: AgentState, config: RunnableConfig) -> dict:
        system = SystemMessage(
            content="你是一个有帮助的助手。可以按需使用计算器、查询当前时间或查询城市天气（OpenWeather）来回答用户。"
        )
        response = model.invoke([system] + list(state["messages"]), config)
        return {"messages": [response]}

    def tool_node(state: AgentState) -> dict:
        last = state["messages"][-1]
        outputs = []
        for tool_call in last.tool_calls:
            result = tools_by_name[tool_call["name"]].invoke(tool_call["args"])
            content = result if isinstance(result, str) else json.dumps(result)
            outputs.append(
                ToolMessage(
                    content=content,
                    name=tool_call["name"],
                    tool_call_id=tool_call["id"],
                )
            )
        return {"messages": outputs}

    workflow = StateGraph(AgentState)
    workflow.add_node("agent", call_model)
    workflow.add_node("tools", tool_node)
    workflow.set_entry_point("agent")
    workflow.add_conditional_edges("agent", should_continue, {
        "continue": "tools",
        "end": END,
    })
    workflow.add_edge("tools", "agent")
    return workflow.compile()
