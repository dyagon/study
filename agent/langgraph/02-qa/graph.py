"""
LangGraph QA 图：retrieve -> generate，支持多轮对话（由调用方维护历史）。
环境变量由 uv 等运行时注入。
"""
from typing import TypedDict
from langgraph.graph import StateGraph, START, END
from langchain_core.documents import Document
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_postgres import PGVector
from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

from config import PG_CONNECTION, COLLECTION_NAME, RETRIEVE_K


class QAState(TypedDict):
    question: str
    chat_history: list[tuple[str, str]]
    retrieved_docs: list[Document]
    answer: str


def get_retriever():
    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    vs = PGVector(
        embeddings=embeddings,
        collection_name=COLLECTION_NAME,
        connection=PG_CONNECTION,
        use_jsonb=True,
    )
    return vs.as_retriever(search_kwargs={"k": RETRIEVE_K})


def retrieve_node(state: QAState) -> QAState:
    retriever = get_retriever()
    docs = retriever.invoke(state["question"])
    return {**state, "retrieved_docs": docs}


def generate_node(state: QAState) -> QAState:
    context = "\n\n".join(d.page_content for d in state["retrieved_docs"])
    history_str = "\n".join(
        f"用户: {q}\n助手: {a}" for q, a in state["chat_history"]
    ) or "（无）"
    llm = ChatTongyi(model="qwen-plus", temperature=0.3)
    prompt = ChatPromptTemplate.from_messages([
        (
            "system",
            "你是一个基于参考内容回答问题的助手。若参考内容中无相关信息，请如实说明。\n\n"
            "参考内容：\n{context}\n\n"
            "当前对话历史：\n{history}",
        ),
        ("user", "{question}"),
    ])
    chain = prompt | llm | StrOutputParser()
    answer = chain.invoke({
        "context": context,
        "history": history_str,
        "question": state["question"],
    })
    return {**state, "answer": answer.strip()}


def build_qa_graph():
    graph = StateGraph(QAState)
    graph.add_node("retrieve", retrieve_node)
    graph.add_node("generate", generate_node)
    graph.add_edge(START, "retrieve")
    graph.add_edge("retrieve", "generate")
    graph.add_edge("generate", END)
    return graph.compile()
