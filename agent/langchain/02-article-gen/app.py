"""
Demo 02: Streamlit + LangChain 文章生成工具
- 输入 topic → 生成文章标题 → 根据标题生成文章正文
- 环境变量由 uv 等运行时注入
"""
import streamlit as st
from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

# 初始化 LLM（仅一次）
@st.cache_resource
def get_llm():
    return ChatTongyi(model="qwen-max", temperature=0.7)

def build_title_chain(llm):
    prompt = ChatPromptTemplate.from_messages([
        ("system", "你是一位擅长起标题的编辑。根据用户给出的主题，生成一个简洁、吸引人的文章标题。只输出标题本身，不要解释或序号。"),
        ("user", "主题：{topic}"),
    ])
    return prompt | llm | StrOutputParser()

def build_content_chain(llm):
    prompt = ChatPromptTemplate.from_messages([
        ("system", "你是一位专业写作者。根据给定的文章标题，写一篇结构清晰、内容充实的文章。使用 Markdown 格式，可包含小标题、列表等。"),
        ("user", "文章标题：{title}\n\n请撰写正文。"),
    ])
    return prompt | llm | StrOutputParser()

def main():
    st.set_page_config(page_title="文章生成", page_icon="✍️")
    st.title("✍️ 文章生成工具")
    st.caption("输入主题 → 生成标题 → 生成正文（Streamlit + LangChain + 通义千问）")

    topic = st.text_input("输入主题（topic）", placeholder="例如：Python 异步编程入门")
    if not topic.strip():
        st.info("请输入一个主题后点击生成。")
        return

    if st.button("生成文章", type="primary"):
        llm = get_llm()
        title_chain = build_title_chain(llm)
        content_chain = build_content_chain(llm)

        with st.spinner("正在生成标题…"):
            title = title_chain.invoke({"topic": topic.strip()}).strip()
        st.subheader("📌 文章标题")
        st.write(title)

        with st.spinner("正在生成正文…"):
            content = content_chain.invoke({"title": title}).strip()
        st.subheader("📄 文章正文")
        st.markdown(content)

if __name__ == "__main__":
    main()
