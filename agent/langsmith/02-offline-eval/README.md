# LangSmith 离线评估 Demo

使用 LangSmith 的 `evaluate()` 在**本地**跑评测，不向 LangSmith 上传任何结果（`upload_results=False`），适合 CI、本地回归或无网环境。

## 要点

- **数据**：使用内存中的 `list[Example]` 作为 `data`，无需在 LangSmith 创建 dataset，也无需 `LANGSMITH_API_KEY`。
- **目标**：`target_fn` 接收 `example.inputs`，返回与 `example.outputs` 同结构的 dict，供评估器对比。
- **评估器**：
  - **行级**（`evaluators`）：对每条样本打分。包含规则类（非空、长度≥10）与 **LangChain 预置 LLM 裁判**：
    - `llm_judge_correctness`：带参考答案的准则评估（`labeled_criteria`），用 LLM 判断回答是否与 reference 一致（正确性）。
    - `llm_judge_conciseness`：无参考答案的准则评估（`criteria`），用 LLM 判断回答是否简洁。
  - **汇总级**（`summary_evaluators`）：对整个评测集算一个指标，如「平均长度」「通过率」。
- **结果**：通过 `results.to_pandas()` 得到每行结果，汇总指标在 `results._summary_results` 中。

## 环境变量

只需配置 LLM 所需 Key，无需 LangSmith：

| 变量 | 说明 |
|------|------|
| `DASHSCOPE_API_KEY` | 通义千问 API Key（本 demo 使用 qwen-turbo） |

不设置 `LANGSMITH_TRACING_V2` / `LANGSMITH_API_KEY` 即可完全离线。

## 运行

在项目根目录执行：

```bash
uv run python langsmith/02-offline-eval/main.py
```

输出包括：

- 每行：`inputs.*`、`outputs.*`（模型输出）、`reference.*`（参考答案）、`feedback.*`（各评估器得分）。
- 汇总：由 `summary_evaluators` 计算出的指标。

## 扩展

- **更多样本**：在 `make_in_memory_dataset()` 里追加 `Example(inputs=..., outputs=...)`。
- **更强评估器**：已集成 LangChain 的 `EvaluatorType.LABELED_CRITERIA` / `EvaluatorType.CRITERIA`，裁判 LLM 使用同项目中的通义千问（`judge_llm`）。可改用其他准则（如 `helpfulness`、`relevance`）或自定义 criteria 字典。也可在 `evaluators` 里接收 `(run, example)`，返回 `{"key": "...", "score": ..., "comment": "..."}`。
- **上传到 LangSmith**：需要 `LANGSMITH_API_KEY`，并设 `upload_results=True`，即可在 Smith 上查看实验与对比。
