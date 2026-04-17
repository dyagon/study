---
name: commit-helper
description: Generates concise commit messages from git diff or staged changes. Use when the user asks for a commit message, help writing commits, or reviewing staged changes.
---

# Commit Message Helper

根据当前 git 变更（diff 或 staged）生成简短、规范的 commit message。

## 何时使用

- 用户说「写个 commit」「生成 commit message」「帮我想一句提交说明」
- 用户提到「staged changes」「git commit」「提交信息」

## 步骤

1. **获取变更**：优先用 `git diff --cached`（已暂存）；若无则用 `git diff`。
2. **归纳变更**：用一行概括改动类型和范围（feat/fix/docs/refactor/chore + 模块或文件）。
3. **写正文**：可选一句说明「做了什么」或「为什么」，保持简短。

## 格式

```
<type>(<scope>): <subject>

<body 可选，一行说明即可>
```

- **type**：`feat` | `fix` | `docs` | `refactor` | `chore` | `style` | `test`
- **scope**：受影响模块/目录，可省略
- **subject**：小写开头、无句号、约 50 字以内

## 示例

**输入**：新增 `ui/chainlit/chainlit_app.py`，接入 DeepSeek 流式对话

**输出**：
```
feat(ui): add Chainlit app with DeepSeek streaming

Integrate DeepSeek API for chat completions in Chainlit UI.
```

**输入**：修改 `.gitignore`，忽略 `dev/db`

**输出**：
```
chore: ignore dev/db in gitignore
```

## 注意

- 若没有 staged 或 unstaged 变更，先提示用户执行 `git add` 或说明要提交的内容。
- 用中文还是英文与用户当前对话语言一致；若用户用中文提问，subject 可用中文。
