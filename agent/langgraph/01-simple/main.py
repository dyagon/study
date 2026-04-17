"""
LangGraph 简单示例：StateGraph 多节点流程
- 流程：START -> 生成回答 -> 润色总结 -> END
- 状态在节点间传递，演示图的基本用法
"""
from typing import TypedDict
from langgraph.graph import StateGraph, START, END
from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

# 图的状态：可随节点更新
class State(TypedDict):
    question: str
    answer: str
    summary: str


def answer_node(state: State) -> State:
    """节点 1：根据问题调用 LLM 生成回答"""
    llm = ChatTongyi(model="qwen-plus", temperature=0.5)
    prompt = ChatPromptTemplate.from_messages([
        ("user", "请用一两句话回答：{question}"),
    ])
    chain = prompt | llm | StrOutputParser()
    answer = chain.invoke({"question": state["question"]}).strip()
    return {**state, "answer": answer}


def summarize_node(state: State) -> State:
    """节点 2：对回答做一句话总结"""
    llm = ChatTongyi(model="qwen-plus", temperature=0.3)
    prompt = ChatPromptTemplate.from_messages([
        ("user", "用一句话总结下面内容：\n\n{answer}"),
    ])
    chain = prompt | llm | StrOutputParser()
    summary = chain.invoke({"answer": state["answer"]}).strip()
    return {**state, "summary": summary}


def main():
    # 构建图：节点 + 边
    graph = StateGraph(State)
    graph.add_node("answer", answer_node)
    graph.add_node("summarize", summarize_node)
    graph.add_edge(START, "answer")
    graph.add_edge("answer", "summarize")
    graph.add_edge("summarize", END)

    app = graph.compile()

    # 运行
    initial = State(question="什么是 LangGraph？", answer="", summary="")
    result = app.invoke(initial)

    print("问题:", result["question"])
    print("回答:", result["answer"])
    print("总结:", result["summary"])


if __name__ == "__main__":
    main()
