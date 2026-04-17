#!/usr/bin/env python3
"""
agent-demo 入口：列出所有 demo 及运行方式。

每个 demo 独立运行，请在项目根目录执行对应命令，例如：
  uv run python langchain/01-simple/main.py

详见 README.md。
"""
from pathlib import Path

# 按主题组织的 demo 根目录（可扩展，如 openai 等）
DEMO_ROOTS = [
    Path(__file__).resolve().parent / "langchain",
    Path(__file__).resolve().parent / "langgraph",
]


def main():
    print("可用 demo（在项目根目录执行）：\n")
    found = False
    for demo_root in DEMO_ROOTS:
        if not demo_root.exists():
            continue
        for d in sorted(demo_root.iterdir()):
            if not d.is_dir() or d.name.startswith("."):
                continue
            main_py = d / "main.py"
            app_py = d / "app.py"
            if app_py.exists():
                rel = app_py.relative_to(demo_root.parent)
                print(f"  uv run streamlit run {rel}")
                found = True
            elif main_py.exists():
                rel = main_py.relative_to(demo_root.parent)
                print(f"  uv run python {rel}")
                found = True
    if not found:
        print("  暂无 demo，请在 langchain/ 等目录下添加。")
    print("\n详见 README.md。")


if __name__ == "__main__":
    main()
