"""
Demo: 摘要记忆（Summary Memory）

将“旧对话”用 LLM 压缩成一段摘要，只把「摘要 + 当前问题」传给模型，
既保留长期信息又控制 token。每轮结束后用 LLM 更新摘要。

用法：在项目根目录执行
  uv run python langchain/04-memory/03_summary/app.py
"""
from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

SUMMARY_PROMPT = """请将下面「已有摘要」与「最新一轮对话」合并，输出一段更新后的简短摘要。
要求：用 2～4 句话概括用户关心的事项、已提供的信息和助手的结论，不要照抄原文。

已有摘要：
{summary}

最新一轮对话：
用户：{human}
助手：{assistant}

更新后的摘要："""


def main():
    llm = ChatTongyi(model="qwen-max", temperature=0.3)
    summarizer = ChatPromptTemplate.from_template(SUMMARY_PROMPT) | llm | StrOutputParser()
    chat_prompt = ChatPromptTemplate.from_messages([
        ("system", "你是友好助手。下面是一段「此前对话的摘要」，请结合摘要与当前问题回答。若摘要为空则只根据当前问题回答。"),
        ("human", "此前对话摘要：\n{summary}\n\n当前问题：{question}"),
    ])
    chain = chat_prompt | llm | StrOutputParser()

    summary = ""
    turns = [
        "我叫小刚，在做 Python 后端开发。",
        "我想学一点异步编程，从哪开始？",
        "我还想了解一下 asyncio 和多线程的区别。",
        "你还记得我的名字和职业吗？",
    ]
    print("--- 摘要记忆（Summary Memory）演示 ---\n")
    for q in turns:
        print(f"用户: {q}")
        out = chain.invoke({"summary": summary or "（无）", "question": q})
        print(f"助手: {out}\n")
        # 用 LLM 更新摘要
        summary = summarizer.invoke({
            "summary": summary or "（无）",
            "human": q,
            "assistant": out,
        })
        print("[内部摘要已更新]\n")


if __name__ == "__main__":
    main()
