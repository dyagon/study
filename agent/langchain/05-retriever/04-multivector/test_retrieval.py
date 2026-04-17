"""
测试多向量检索：MultiVectorRetriever 检索摘要后返回父文档，再 RAG 生成回答。
"""
import sys

from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

from config import DOCSTORE_PATH
from graph import get_retriever


def main():
    question = sys.argv[1] if len(sys.argv) > 1 else "项目里有哪些 demo？"
    retriever = get_retriever()
    docs = retriever.invoke(question)

    print("--- 检索到的父文档（多向量：按摘要命中，返回完整文档）---")
    for i, d in enumerate(docs, 1):
        src = d.metadata.get("source", "")
        print(f"[{i}] {src}")
        print(d.page_content[:300].replace("\n", " ") + ("..." if len(d.page_content) > 300 else ""))
        print()

    if not docs:
        print("未检索到文档，请先运行 build_db.py <文档目录> 并确保 docstore 存在:", DOCSTORE_PATH)
        return

    context = "\n\n".join(d.page_content for d in docs)
    llm = ChatTongyi(model="qwen-plus", temperature=0.3)
    prompt = ChatPromptTemplate.from_messages([
        ("system", "根据以下参考内容回答问题。若参考内容中无相关信息，请说明。\n\n参考内容：\n{context}"),
        ("user", "{question}"),
    ])
    chain = prompt | llm | StrOutputParser()
    answer = chain.invoke({"context": context, "question": question})
    print("--- 回答 ---")
    print(answer)


if __name__ == "__main__":
    main()
