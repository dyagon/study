"""
Demo: 滑动窗口记忆（Window Memory）

只保留最近 K 轮对话（每轮 = 用户 + 助手各一条），更早的消息不传入 prompt，
适合控制上下文长度与成本。通过自定义 BaseChatMessageHistory 包装实现。

用法：在项目根目录执行
  uv run python langchain/04-memory/02_window/app.py
"""
from collections.abc import Sequence

from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.messages import BaseMessage
from langchain_community.chat_models import ChatTongyi
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables.history import RunnableWithMessageHistory

# 每轮 = 1 条 human + 1 条 ai，保留最近 2 轮 => 最多 4 条消息
WINDOW_SIZE = 2


class WindowedChatMessageHistory(BaseChatMessageHistory):
    """只暴露最近 window_size 轮（2*window_size 条）消息的 History。"""

    def __init__(self, inner: ChatMessageHistory, window_size: int = 2):
        self.inner = inner
        self.window_size = window_size

    @property
    def messages(self) -> list[BaseMessage]:
        all_msgs = self.inner.messages
        keep = 2 * self.window_size
        if len(all_msgs) <= keep:
            return all_msgs
        return list(all_msgs[-keep:])

    def add_messages(self, messages: Sequence[BaseMessage]) -> None:
        self.inner.add_messages(messages)

    def clear(self) -> None:
        self.inner.clear()


store: dict[str, ChatMessageHistory] = {}


def get_session_history(session_id: str) -> BaseChatMessageHistory:
    """RunnableWithMessageHistory 会传入 configurable 中的值（如 session_id 字符串）。"""
    key = session_id or "default"
    if key not in store:
        store[key] = ChatMessageHistory()
    return WindowedChatMessageHistory(store[key], window_size=WINDOW_SIZE)


def main():
    llm = ChatTongyi(model="qwen-max", temperature=0.3)
    prompt = ChatPromptTemplate.from_messages([
        ("system", "你是友好助手。仅根据下面最近几轮对话回答。"),
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

    # 先说了名字，再问两轮别的，最后问“我叫什么”——窗口只保留最近 2 轮，可能已忘记名字
    turns = [
        "我叫小红，请记住。",
        "Python 里 list 和 tuple 有啥区别？",
        "那 dict 和 set 呢？",
        "你还记得我叫什么吗？",  # 若窗口只有 2 轮，可能答不出“小红”
    ]
    print("--- 滑动窗口记忆（最近 2 轮）演示 ---\n")
    for q in turns:
        print(f"用户: {q}")
        out = chain_with_history.invoke({"question": q}, config=config)
        print(f"助手: {out}\n")


if __name__ == "__main__":
    main()
