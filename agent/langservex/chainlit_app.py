"""
Chainlit 前端：通过 LangServe 的 stream_events 接口展示思考过程（reasoning_content）。
- 若设置 LANGSERVE_URL（如 http://localhost:8080），则调用远端 /reasoner_think/stream_events；
- 否则使用本地 chain_with_thinking.astream_events()，同样展示思考步骤。
运行: uv run chainlit run langservex/chainlit_app.py -w --port 8050
环境变量: DEEPSEEK_API_KEY；可选 LANGSERVE_URL
"""
import os

import chainlit as cl

LANGSERVE_URL = os.getenv("LANGSERVE_URL", "").rstrip("/")


def _get_reasoning_runnable():
    if LANGSERVE_URL:
        from langserve import RemoteRunnable
        return RemoteRunnable(url=f"{LANGSERVE_URL}/reasoner_think")
    from thinking_chain import chain_with_thinking
    return chain_with_thinking


def _chunk_content(chunk):
    """从 event data chunk 中取出 content 与 reasoning_content（兼容对象或 dict）。"""
    if chunk is None:
        return "", ""
    if isinstance(chunk, dict):
        kwargs = chunk.get("kwargs") or chunk
        content = kwargs.get("content") or ""
        reasoning = (kwargs.get("additional_kwargs") or {}).get("reasoning_content") or ""
    else:
        content = getattr(chunk, "content", "") or ""
        extra = getattr(chunk, "additional_kwargs", None) or {}
        reasoning = extra.get("reasoning_content", "") if isinstance(extra, dict) else ""
    return content if isinstance(content, str) else "", reasoning if isinstance(reasoning, str) else ""


def _is_chat_model_stream(event):
    kind = event.get("event") or ""
    return "chat_model" in kind and "stream" in kind


@cl.on_message
async def on_message(msg: cl.Message):
    runnable = _get_reasoning_runnable()
    answer_msg = cl.Message(content="")

    async with cl.Step(name="思考过程") as step:
        async for event in runnable.astream_events(
            {"input": msg.content},
            version="v2",
        ):
            if not _is_chat_model_stream(event):
                continue
            data = event.get("data") or {}
            chunk = data.get("chunk")
            content, reasoning = _chunk_content(chunk)
            if reasoning:
                await step.stream_token(reasoning)
            if content:
                answer_msg.content += content
                await answer_msg.update()

    await answer_msg.send()
