"""
测试向量库检索：连接 PGVector，对单个问题做相似检索并用 LLM 生成回答。
用于验证 build_db 后的数据与检索效果。环境变量由 uv 等运行时注入。
"""
import sys

from langchain_community.embeddings import DashScopeEmbeddings
from langchain_postgres import PGVector
from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

from config import PG_CONNECTION, COLLECTION_NAME, RETRIEVE_K


def get_vector_store():
    embeddings = DashScopeEmbeddings(model="text-embedding-v3")
    return PGVector(
        embeddings=embeddings,
        collection_name=COLLECTION_NAME,
        connection=PG_CONNECTION,
        use_jsonb=True,
    )


def main():
    question = sys.argv[1] if len(sys.argv) > 1 else "项目里有哪些 demo？"

    vs = get_vector_store()
    docs = vs.similarity_search(question, k=RETRIEVE_K)
    print("--- 检索到的片段 ---")
    for i, d in enumerate(docs, 1):
        print(f"[{i}] {d.metadata.get('source', '')}")
        print(d.page_content[:200].replace("\n", " ") + "..." if len(d.page_content) > 200 else d.page_content)
        print()

    if not docs:
        print("未检索到相关文档，请先运行 build_db.py <文档目录> 构建向量库。")
        return

    # 简单 RAG：上下文 + 问题 -> LLM
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
