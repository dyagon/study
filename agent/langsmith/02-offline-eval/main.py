"""
LangSmith 离线评估 Demo

在本地运行评估，不向 LangSmith 上传结果（upload_results=False）。
- 使用内存中的「评测集」（list of Example）作为 data，无需 API 即可跑通。
- 定义行级评估器（evaluators）和汇总级评估器（summary_evaluators）。
- 评估结果在本地打印，并写入同目录下的 Markdown 文件（表格形式）。
"""
import os
import sys
from datetime import datetime, timezone
from pathlib import Path
from uuid import uuid4

# 离线评估不依赖 LangSmith 上传，但 LLM 仍需 DASHSCOPE_API_KEY
if not os.environ.get("DASHSCOPE_API_KEY"):
    print("请设置 DASHSCOPE_API_KEY 后重试。")
    sys.exit(0)

# 关闭追踪，避免离线评估时尝试连接 LangSmith（可选）
os.environ["LANGCHAIN_TRACING_V2"] = "false"

from langchain_community.chat_models import ChatTongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langsmith import Client
from langsmith.schemas import Example

# LangChain 预置评估器（LLM 作为裁判）
from langchain_classic.evaluation import EvaluatorType, load_evaluator


# ---------------------------------------------------------------------------
# 1. 构建待评估的应用（与 01-simple 同款链）
# ---------------------------------------------------------------------------
llm = ChatTongyi(model_name="qwen-turbo", temperature=0.3)
# 裁判用 LLM：温度 0 以保持评判稳定
judge_llm = ChatTongyi(model_name="qwen-turbo", temperature=0)
prompt = ChatPromptTemplate.from_messages([
    ("system", "你是一位精通中国传统文化的专家。请用简洁的语言回答。"),
    ("user", "请解释一下什么是：{concept}"),
])
chain = prompt | llm | StrOutputParser()


def target_fn(inputs: dict) -> dict:
    """评估目标：接收 example.inputs，返回与 example.outputs 同结构的 dict。"""
    answer = chain.invoke(inputs)
    return {"answer": answer}


# ---------------------------------------------------------------------------
# 2. 内存中的评测集（无需 LangSmith API）
# ---------------------------------------------------------------------------
def make_in_memory_dataset():
    """构造 in-memory 的 Example 列表，供 evaluate(data=...) 使用。"""
    rows = [
        {"concept": "二十四节气中的惊蛰", "answer": "惊蛰是春季第三个节气，标志着万物复苏。"},
        {"concept": "端午节的由来", "answer": "端午节与屈原有关，有吃粽子、赛龙舟等习俗。"},
        {"concept": "什么是京剧", "answer": "京剧是中国国粹之一，以北京为中心形成的戏曲剧种。"},
    ]
    examples = []
    for r in rows:
        examples.append(
            Example(
                id=uuid4(),
                dataset_id=uuid4(),
                inputs={"concept": r["concept"]},
                outputs={"answer": r["answer"]},
                created_at=datetime.now(timezone.utc),
                modified_at=None,
            )
        )
    return examples


# ---------------------------------------------------------------------------
# 3. 评估器
# ---------------------------------------------------------------------------

# LangChain 预置 LLM 裁判评估器（需在运行时按需构建，见下方工厂）
_labeled_criteria_eval = None
_criteria_eval = None


def _get_labeled_criteria_eval():
    """带参考答案的准则评估（如：正确性），LLM 对比 prediction 与 reference。"""
    global _labeled_criteria_eval
    if _labeled_criteria_eval is None:
        _labeled_criteria_eval = load_evaluator(
            EvaluatorType.LABELED_CRITERIA,
            llm=judge_llm,
            criteria="correctness",
        )
    return _labeled_criteria_eval


def _get_criteria_eval():
    """无参考答案的准则评估（如：简洁性），仅由 LLM 对输出打分。"""
    global _criteria_eval
    if _criteria_eval is None:
        _criteria_eval = load_evaluator(
            EvaluatorType.CRITERIA,
            llm=judge_llm,
            criteria="conciseness",
        )
    return _criteria_eval


def llm_judge_correctness(run, example) -> dict:
    """行级（LangChain）：用 LLM 判断模型回答是否与参考答案一致（正确性）。"""
    pred = (run.outputs or {}).get("answer") or ""
    ref = (example.outputs or {}).get("answer") or ""
    inp = str(example.inputs or {})
    try:
        out = _get_labeled_criteria_eval().evaluate_strings(
            prediction=pred,
            reference=ref,
            input=inp,
        )
        score = out.get("score")
        if score is None:
            score = 1 if (out.get("value") or "").upper() == "Y" else 0
        return {
            "key": "llm_correctness",
            "score": score,
            "comment": out.get("reasoning"),
        }
    except Exception as e:
        return {"key": "llm_correctness", "score": None, "comment": str(e)}


def llm_judge_conciseness(run, example) -> dict:
    """行级（LangChain）：用 LLM 判断模型回答是否简洁。"""
    pred = (run.outputs or {}).get("answer") or ""
    inp = str(example.inputs or {})
    try:
        out = _get_criteria_eval().evaluate_strings(
            prediction=pred,
            input=inp,
        )
        score = out.get("score")
        if score is None:
            score = 1 if (out.get("value") or "").upper() == "Y" else 0
        return {
            "key": "llm_conciseness",
            "score": score,
            "comment": out.get("reasoning"),
        }
    except Exception as e:
        return {"key": "llm_conciseness", "score": None, "comment": str(e)}


def answer_not_too_short(run, example) -> dict:
    """行级：模型回答不能过短。"""
    pred = (run.outputs or {}).get("answer") or ""
    return {"score": len(pred.strip()) >= 10}


def answer_has_content(run, example) -> dict:
    """行级：回答非空。"""
    pred = (run.outputs or {}).get("answer") or ""
    return {"score": bool(pred.strip())}


def avg_answer_length(runs, examples) -> dict:
    """汇总级：平均回答长度。"""
    lengths = []
    for run in runs:
        pred = (run.outputs or {}).get("answer") or ""
        lengths.append(len(pred.strip()))
    return {"score": sum(lengths) / len(lengths) if lengths else 0}


def pass_rate(runs, examples) -> dict:
    """汇总级：通过率（这里用「非空且长度>=10」作为通过）。"""
    n = len(runs)
    if n == 0:
        return {"score": 0.0}
    passed = 0
    for run in runs:
        pred = (run.outputs or {}).get("answer") or ""
        if pred.strip() and len(pred.strip()) >= 10:
            passed += 1
    return {"score": passed / n}


# ---------------------------------------------------------------------------
# 4. 将 DataFrame 转为 Markdown 表格并写入文件
# ---------------------------------------------------------------------------
def _cell_for_md(s: str, max_len: int = 200) -> str:
    """单格内容转 Markdown 安全字符串：转义 | 与换行，过长截断。"""
    s = str(s).replace("|", "\\|").replace("\n", " ")
    return (s[:max_len] + "…") if len(s) > max_len else s


def _dataframe_to_markdown_table(df, max_cell_len: int = 200) -> str:
    """将 DataFrame 转为 Markdown 表格字符串。"""
    df = df.fillna("")
    cols = list(df.columns)
    lines = [
        "| " + " | ".join(str(c) for c in cols) + " |",
        "| " + " | ".join("---" for _ in cols) + " |",
    ]
    for _, row in df.iterrows():
        lines.append(
            "| " + " | ".join(_cell_for_md(row[c], max_cell_len) for c in cols) + " |"
        )
    return "\n".join(lines)


def _write_eval_report_md(df, summary: dict, out_path: Path) -> None:
    """把每行结果与汇总指标写入 Markdown 文件。"""
    parts = [
        "# LangSmith 离线评估结果",
        "",
        f"生成时间：{datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S UTC')}",
        "",
        "## 每行评估结果",
        "",
        _dataframe_to_markdown_table(df),
        "",
    ]
    summary_results = summary.get("results") or []
    if summary_results:
        parts.append("## 汇总指标")
        parts.append("")
        parts.append("| 指标 | 值 |")
        parts.append("| --- | --- |")
        for r in summary_results:
            key = getattr(r, "key", None) or (r.get("key") if isinstance(r, dict) else "score")
            score = getattr(r, "score", None)
            if score is None and isinstance(r, dict):
                score = r.get("score") or r.get("value")
            parts.append(f"| {key} | {score} |")
        parts.append("")
    parts.append("---")
    parts.append("*由 LangSmith 离线评估生成，未上传至 LangSmith。*")
    out_path.write_text("\n".join(parts), encoding="utf-8")


# ---------------------------------------------------------------------------
# 5. 运行离线评估并输出到 MD
# ---------------------------------------------------------------------------
def main():
    dataset = make_in_memory_dataset()
    client = Client()

    print("运行离线评估（upload_results=False）...")
    results = client.evaluate(
        target_fn,
        data=dataset,
        evaluators=[
            answer_not_too_short,
            answer_has_content,
            llm_judge_correctness,
            llm_judge_conciseness,
        ],
        summary_evaluators=[avg_answer_length, pass_rate],
        experiment_prefix="offline-eval",
        description="LangSmith 离线评估 demo",
        upload_results=False,
        blocking=True,
    )

    df = results.to_pandas()
    summary = getattr(results, "_summary_results", None) or {}

    # 控制台打印
    print("\n--- 每行评估结果 ---")
    print(df.to_string())
    if summary.get("results"):
        print("\n--- 汇总指标 ---")
        for r in summary["results"]:
            key = getattr(r, "key", None) or (r.get("key") if isinstance(r, dict) else "score")
            score = getattr(r, "score", None)
            if score is None and isinstance(r, dict):
                score = r.get("score") or r.get("value")
            print(f"  {key}: {score}")

    # 写入 Markdown 文件（与脚本同目录）
    out_path = Path(__file__).resolve().parent / "eval_results.md"
    _write_eval_report_md(df, summary, out_path)
    print(f"\n评估结果已写入：{out_path}")
    print("未向 LangSmith 上传（upload_results=False）。")


if __name__ == "__main__":
    main()
