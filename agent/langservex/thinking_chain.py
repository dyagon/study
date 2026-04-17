"""
思维链链：使用 DeepSeek-R1 (deepseek-reasoner) 的 LangChain LCEL 链。
供 LangServe 暴露为 API，或由 Chainlit 直接调用。
环境变量 DEEPSEEK_API_KEY 由运行环境注入。
"""
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_deepseek import ChatDeepSeek

SYSTEM_PROMPT = "你是一个乐于助人的助手，善于一步步推理后再给出结论。"
USER_PROMPT = "{input}"


def create_chain():
    llm = ChatDeepSeek(
        model="deepseek-reasoner",
        temperature=0,
        max_tokens=None,
    )
    prompt = ChatPromptTemplate.from_messages([
        ("system", SYSTEM_PROMPT),
        ("user", USER_PROMPT),
    ])
    return prompt | llm | StrOutputParser()


def create_chain_with_thinking():
    """返回 prompt | llm，不接 StrOutputParser，便于流式拿到 AIMessageChunk.reasoning_content。"""
    llm = ChatDeepSeek(
        model="deepseek-reasoner",
        temperature=0,
        max_tokens=None,
    )
    prompt = ChatPromptTemplate.from_messages([
        ("system", SYSTEM_PROMPT),
        ("user", USER_PROMPT),
    ])
    return prompt | llm


# 供 LangServe add_routes 使用的可调用链（输入: {"input": str}）
chain = create_chain()
# 供 stream_events 展示思考过程（输出 AIMessage，含 additional_kwargs.reasoning_content）
chain_with_thinking = create_chain_with_thinking()
