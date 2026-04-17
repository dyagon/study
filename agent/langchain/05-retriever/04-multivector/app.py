"""
多向量检索 + 对话式 QA：Streamlit + LangGraph，MultiVectorRetriever 返回父文档。
"""
import streamlit as st
from graph import build_qa_graph, QAState


@st.cache_resource
def get_graph():
    return build_qa_graph()


def main():
    st.set_page_config(page_title="文档 QA（多向量）", page_icon="📚")
    st.title("📚 文档 QA（多向量检索）")
    st.caption("摘要向量检索 → 返回父文档 → 对话式问答（MultiVectorRetriever + LangGraph）")

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
