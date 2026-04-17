"""
Agent Demo 配置：模型名、OpenWeather API 等。
从环境变量读取，与项目其他 demo 一致。
"""
import os

# 通义千问模型名
MODEL_NAME = os.getenv("AGENT_MODEL_NAME", "qwen-max")

# OpenWeather API Key（可选，未设置时天气工具会提示）
OPENWEATHER_API_KEY = os.getenv("OPENWEATHER_API_KEY", "")
