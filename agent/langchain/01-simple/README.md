# 01-simple

LangChain + 通义千问 (Qwen) 简单链式调用示例。

- 使用 LCEL 构建 `Prompt -> LLM -> OutputParser` 链
- `DASHSCOPE_API_KEY` 由 uv 或运行环境注入

## 运行

在项目根目录执行：

```bash
uv run python langchain/01-simple/main.py
```
