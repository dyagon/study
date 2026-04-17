"""
LangServe 服务：将思维链暴露为 REST API（/reasoner 的 invoke / stream / batch）。
启动: uv run uvicorn langservex.server:app --reload --port 8080
环境变量: DEEPSEEK_API_KEY
"""
from fastapi import FastAPI
from langserve import add_routes

from langservex.thinking_chain import chain, chain_with_thinking

app = FastAPI(
    title="LangServe Reasoner API",
    description="DeepSeek-R1 (deepseek-reasoner) 思维链 API",
    version="0.1.0",
)

add_routes(app, chain, path="/reasoner")
# 暴露「思考过程」：stream_events 可拿到 AIMessageChunk.reasoning_content
add_routes(app, chain_with_thinking, path="/reasoner_think")
