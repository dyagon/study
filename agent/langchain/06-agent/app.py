"""
LangChain Agent Demo：ReAct + Tools（LangGraph create_react_agent）
- 使用通义千问 + 自定义工具（计算器、当前时间）
- 支持交互式多轮或单次命令行提问
- 环境变量：DASHSCOPE_API_KEY
"""
import os
import sys
from langchain_community.chat_models import ChatTongyi
from langchain_core.messages import HumanMessage
from langgraph.prebuilt import create_react_agent

from tools import get_tools


def main():
    if not os.environ.get("DASHSCOPE_API_KEY"):
        print("请设置环境变量 DASHSCOPE_API_KEY（通义千问 API Key）")
        sys.exit(1)
    tools = get_tools()
    llm = ChatTongyi(model="qwen-max", temperature=0.2)
    # 绑定工具后交给 create_react_agent
    model_with_tools = llm.bind_tools(tools)
    agent = create_react_agent(model_with_tools, tools)

    # 单次提问：从命令行参数读取
    if len(sys.argv) > 1:
        question = " ".join(sys.argv[1:])
        messages = [HumanMessage(content=question)]
        result = agent.invoke({"messages": messages})
        last = result["messages"][-1]
        print(last.content if hasattr(last, "content") else last)
        return

    # 交互式
    print("LangChain Agent Demo（ReAct + 计算器/时间工具）")
    print("输入问题后回车，空行退出。")
    messages = []
    while True:
        try:
            line = input("\n你: ").strip()
        except (EOFError, KeyboardInterrupt):
            break
        if not line:
            break
        messages.append(HumanMessage(content=line))
        result = agent.invoke({"messages": messages})
        messages = result["messages"]
        last = messages[-1]
        text = last.content if hasattr(last, "content") else str(last)
        print("Agent:", text)


if __name__ == "__main__":
    main()
