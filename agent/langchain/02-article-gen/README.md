# 02-article-gen

Streamlit + LangChain 文章生成工具：输入主题 → 生成文章标题 → 根据标题生成正文。

- 使用通义千问 (Qwen)，`DASHSCOPE_API_KEY` 由 uv 或运行环境注入

## 运行

在项目根目录执行：

```bash
uv run streamlit run langchain/02-article-gen/app.py
```

浏览器会自动打开 Streamlit 页面；在输入框填写主题后点击「生成文章」即可。
