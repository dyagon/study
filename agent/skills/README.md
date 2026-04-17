# Agent Skill 使用 Demo

本目录说明本仓库中 **Agent Skill** 的用法与示例。

## 什么是 Agent Skill？

Agent Skill 是 Cursor 里的一种「技能」：用 Markdown 写成的 `SKILL.md`，教 AI 在特定场景下按固定流程、格式或规范完成任务，例如：

- 根据 git diff 生成 commit message
- 按团队规范做 Code Review
- 按模板生成 PR 描述或 Changelog

技能放在 **项目级** `.cursor/skills/<skill-name>/` 或 **用户级** `~/.cursor/skills/<skill-name>/`，Agent 会根据描述（description）在合适时机自动选用。

## 本仓库的 Demo Skill

| 技能名     | 路径                           | 作用 |
|------------|--------------------------------|------|
| commit-helper | `.cursor/skills/commit-helper/` | 根据当前 git 变更生成规范的 commit message |

### 如何触发 commit-helper

在 Cursor 对话里用自然语言即可，例如：

- 「帮我想一句 commit message」
- 「根据当前改动写个提交说明」
- 「我改了几个文件，生成一下 commit message」

Agent 会读取 `commit-helper` 的 SKILL.md，按其中的步骤（看 diff、归纳 type/scope、写 subject/body）生成一条 commit message。

### 建议自测步骤

1. 在项目里做一点修改并暂存：
   ```bash
   echo "# test" >> README.md
   git add README.md
   ```
2. 在 Cursor 里对 Agent 说：「根据当前 staged 的改动，生成一条 commit message」。
3. 检查生成的格式是否符合 SKILL 里的约定（如 `feat(...): ...` 或 `chore: ...`）。

## 技能存放位置说明

- **项目技能**：`.cursor/skills/`（随仓库共享，所有人都能用）
- **个人技能**：`~/.cursor/skills/`（仅本机、所有项目可用）

本 demo 使用 **项目技能**，克隆仓库后即可在对话中触发上述技能。

## 参考

- 创建新技能：可参考 [Cursor 的 create-skill 指南](https://docs.cursor.com) 或项目内 `.cursor/skills/commit-helper/SKILL.md` 的结构。
- 规范要点：`SKILL.md` 需有 YAML frontmatter（`name`、`description`），description 要写清「做什么」和「何时用」，便于 Agent 匹配。
