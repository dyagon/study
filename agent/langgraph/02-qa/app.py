"""
对话式 QA：Streamlit + LangGraph（retrieve -> generate），多轮提问。
使用前请先运行 build_db.py <文档目录> 构建向量库，并确保 dev 下 pgvector 已启动。
环境变量由 uv 等运行时注入。
"""
import streamlit as st
from graph import build_qa_graph, QAState


@st.cache_resource
def get_graph():
    return build_qa_graph()


def main():
    st.set_page_config(page_title="文档 QA", page_icon="📚")
    st.title("📚 文档 QA（LangGraph + PGVector）")
    st.caption("基于本地 Markdown 文档的检索与对话式问答")

    if "chat_history" not in st.session_state:
        st.session_state.chat_history = []

    for q, a in st.session_state.chat_history:
        with st.chat_message("user"):
            st.markdown(q)
        with st.chat_message("assistant"):
            st.markdown(a)

    if prompt := st.chat_input("输入问题..."):
        with st.chat_message("user"):
            st.markdown(prompt)

        with st.chat_message("assistant"):
            with st.spinner("检索并生成回答…"):
                graph = get_graph()
                result = graph.invoke(
                    QAState(
                        question=prompt,
                        chat_history=st.session_state.chat_history,
                        retrieved_docs=[],
                        answer="",
                    )
                )
            st.markdown(result["answer"])

        st.session_state.chat_history.append((prompt, result["answer"]))
        st.rerun()


if __name__ == "__main__":
    main()
