"""
Agent 可用工具：计算器、当前时间等，便于本地演示、无需外部 API。
"""
from datetime import datetime, timezone
from langchain_core.tools import tool


@tool
def calculator(expression: str) -> str:
    """
    计算数学表达式。只支持基本运算：+ - * / ** ( ) 与整数、小数。
    输入应为合法表达式，例如：2 + 3 * 4、(1.5 ** 2)
    """
    try:
        # 仅允许数字与运算符，避免执行任意代码
        allowed = set("0123456789+-*/.() ")
        if not all(c in allowed for c in expression):
            return "错误：表达式中只能包含数字和 + - * / . ( )"
        result = eval(expression)
        return str(result)
    except Exception as e:
        return f"计算错误: {e}"


@tool
def get_current_time(timezone_name: str = "Asia/Shanghai") -> str:
    """
    获取指定时区的当前日期时间。
    timezone_name: 时区名，如 Asia/Shanghai、America/New_York、UTC。默认为 Asia/Shanghai。
    """
    try:
        from zoneinfo import ZoneInfo
        tz = ZoneInfo(timezone_name)
    except Exception:
        tz = timezone.utc
    now = datetime.now(tz)
    return now.strftime("%Y-%m-%d %H:%M:%S %Z")


def get_tools():
    """返回 Agent 使用的工具列表。"""
    return [calculator, get_current_time]
