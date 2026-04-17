"""
Demo: 完整对话历史（Buffer Memory）

使用 RunnableWithMessageHistory + ChatMessageHistory 保存会话内全部消息，
多轮对话时模型能引用之前说过的内容（例如先告诉名字，再问“我叫什么”）。

用法：在项目根目录执行
  uv run python langchain/04-memory/01_buffer/app.py
"""
from langchain_community.chat_models import ChatTongyi
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables.history import RunnableWithMessageHistory

# 内存中的会话存储：session_id -> ChatMessageHistory
store: dict[str, ChatMessageHistory] = {}


def get_session_history(session_id: str) -> ChatMessageHistory:
    """RunnableWithMessageHistory 会传入 configurable 中的值（如 session_id 字符串）。"""
    key = session_id or "default"
    if key not in store:
        store[key] = ChatMessageHistory()
    return store[key]


def main():
    llm = ChatTongyi(model="qwen-max", temperature=0.3)
    prompt = ChatPromptTemplate.from_messages([
        ("system", "你是一个友好的助手。请根据对话历史回答，若用户曾提到过名字、偏好等信息要记得使用。"),
        MessagesPlaceholder(variable_name="chat_history"),
        ("human", "{question}"),
    ])
    chain = prompt | llm | StrOutputParser()
    chain_with_history = RunnableWithMessageHistory(
        chain,
        get_session_history,
        input_messages_key="question",
        history_messages_key="chat_history",
    )
    config = {"configurable": {"session_id": "user-01"}}

    # 预定义几轮对话，演示“记住名字”
    turns = [
        "你好，我叫小明。",
        "我最近在学 Python，你有什么入门建议？",
        "我叫什么名字？",  # 期望回答“小明”
    ]
    print("--- 完整历史 Buffer Memory 演示 ---\n")
    for q in turns:
        print(f"用户: {q}")
        out = chain_with_history.invoke({"question": q}, config=config)
        print(f"助手: {out}\n")


if __name__ == "__main__":
    main()
