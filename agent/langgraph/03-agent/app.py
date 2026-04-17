"""
ReAct Agent Demo：LangGraph StateGraph 方式（agent ↔ tools 条件边）。
支持单次命令行提问或交互式多轮，环境变量 DASHSCOPE_API_KEY。
加 -v/--verbose 可打印每步输入输出与工具调用，便于调试。
"""
import argparse
import os
import sys
from langchain_core.messages import AIMessage, HumanMessage, ToolMessage

from graph import build_agent_graph


def _message_summary(msg) -> str:
    """单条消息的简短描述，用于调试输出。"""
    if isinstance(msg, HumanMessage):
        return f"HumanMessage(content={msg.content[:80]!r}...)" if len(msg.content or "") > 80 else f"HumanMessage(content={msg.content!r})"
    if isinstance(msg, AIMessage):
        parts = []
        if msg.content:
            s = (msg.content[:60] + "…") if len(msg.content) > 60 else msg.content
            parts.append(f"content={s!r}")
        if getattr(msg, "tool_calls", None):
            for tc in msg.tool_calls:
                parts.append(f"tool_call({tc.get('name')}, args={tc.get('args')})")
        return "AIMessage(" + ", ".join(parts) + ")"
    if isinstance(msg, ToolMessage):
        content = (msg.content[:80] + "…") if len(msg.content or "") > 80 else (msg.content or "")
        return f"ToolMessage(name={msg.name}, content={content!r})"
    return type(msg).__name__


def _run_with_verbose(graph, inputs: dict):
    """
    带调试输出的单次运行：用 stream(updates) 打印每步节点输出，并从 updates 合并出最终 state，只跑一遍图。
    """
    print("\n--- 调试：按节点流式输出 ---")
    step = 0
    # 与 AgentState 的 add_messages 一致：新 messages 追加到列表
    messages = list(inputs["messages"])
    for chunk in graph.stream(inputs, stream_mode="updates"):
        for node_name, state_update in chunk.items():
            step += 1
            msgs = state_update.get("messages", [])
            if not msgs:
                print(f"  [{step}] 节点 {node_name!r}: (无 messages)")
                continue
            for m in msgs:
                print(f"  [{step}] 节点 {node_name!r} -> {_message_summary(m)}")
            messages.extend(msgs)
    print("--- 流式结束 ---\n")
    return {"messages": messages}


def main():
    if not os.environ.get("DASHSCOPE_API_KEY"):
        print("请设置环境变量 DASHSCOPE_API_KEY（通义千问 API Key）")
        sys.exit(1)

    parser = argparse.ArgumentParser(description="LangGraph ReAct Agent Demo")
    parser.add_argument("-v", "--verbose", action="store_true", help="打印每步输入输出与工具调用，便于调试")
    parser.add_argument("question", nargs="*", help="单次提问内容（不传则进入交互式）")
    args = parser.parse_args()
    verbose = args.verbose
    question_parts = args.question

    graph = build_agent_graph()

    # 单次提问
    if question_parts:
        question = " ".join(question_parts)
        inputs = {"messages": [HumanMessage(content=question)]}
        if verbose:
            result = _run_with_verbose(graph, inputs)
        else:
            result = graph.invoke(inputs)
        last = result["messages"][-1]
        print(last.content if hasattr(last, "content") else str(last))
        return

    # 交互式
    print("LangGraph ReAct Agent（计算器 / 当前时间 / 天气）")
    print("输入问题后回车，空行退出。加 -v 可先开启调试输出。")
    messages = []
    while True:
        try:
            line = input("\n你: ").strip()
        except (EOFError, KeyboardInterrupt):
            break
        if not line:
            break
        messages.append(HumanMessage(content=line))
        inputs = {"messages": messages}
        if verbose:
            result = _run_with_verbose(graph, inputs)
        else:
            result = graph.invoke(inputs)
        messages = result["messages"]
        last = messages[-1]
        text = last.content if hasattr(last, "content") else str(last)
        print("Agent:", text)


if __name__ == "__main__":
    main()
