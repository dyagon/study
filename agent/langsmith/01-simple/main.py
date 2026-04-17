import os
from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

# 开启 LangSmith 自动追踪功能
os.environ["LANGCHAIN_PROJECT"] = "LangSmith-Tongyi-Project" 
os.environ["LANGCHAIN_TRACING_V2"] = "true"

llm = ChatTongyi(
    model_name="qwen-turbo", 
    temperature=0.7
)

# 创建 Prompt 模板
prompt = ChatPromptTemplate.from_messages([
    ("system", "你是一位精通中国传统文化的专家。请用优美、诗意的语言回答用户的问题。"),
    ("user", "请解释一下什么是：{concept}")
])

# 创建输出解析器
parser = StrOutputParser()

# 拼接成 Chain
chain = prompt | llm | parser

# ==========================================
# 3. 运行应用
# ==========================================
print("正在调用通义千问，请稍候...\n")
response = chain.invoke({"concept": "二十四节气中的'惊蛰'"})

print("🤖 模型回复：")
print(response)
