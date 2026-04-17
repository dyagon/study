"""
Demo 01: LangChain + 通义千问 (Qwen) 简单链式调用
- 使用 LCEL 构建 Prompt -> LLM -> OutputParser 链
- 支持流式输出（可选）
- 环境变量（如 DASHSCOPE_API_KEY）由 uv 等运行时注入
"""
from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

# 初始化 Qwen 模型
llm = ChatTongyi(
    model="qwen-max",
    temperature=0.7,
    streaming=True,
)

prompt = ChatPromptTemplate.from_messages([
    ("system", "你是一个精通 {topic} 的技术专家，请用简练的语言回答问题。"),
    ("user", "{question}"),
])
chain = prompt | llm | StrOutputParser()

def main():
    print("--- 正在思考 (Qwen) ---")
    question = "如何用一句话解释什么是 uv 包管理器？"
    response = chain.invoke({"topic": "Python 工程化", "question": question})
    print(response)

if __name__ == "__main__":
    main()
