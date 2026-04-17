"""
Agent 可用工具：计算器、当前时间、OpenWeather 天气查询等。
"""
import json
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from langchain_core.tools import tool

from config import OPENWEATHER_API_KEY


@tool
def calculator(expression: str) -> str:
    """
    计算数学表达式。只支持基本运算：+ - * / ** ( ) 与整数、小数。
    输入应为合法表达式，例如：2 + 3 * 4、(1.5 ** 2)
    """
    try:
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


def _fetch_openweather(city: str) -> str:
    """调用 OpenWeather 当前天气接口，返回人类可读描述。未配置 API Key 时返回提示。"""
    if not OPENWEATHER_API_KEY:
        return "未配置 OPENWEATHER_API_KEY，无法查询天气。请在环境变量中设置 OpenWeather API Key（https://openweathermap.org/api）。"
    url = "https://api.openweathermap.org/data/2.5/weather"
    params = {
        "q": city.strip(),
        "appid": OPENWEATHER_API_KEY,
        "units": "metric",
        "lang": "zh_cn",
    }
    req = urllib.request.Request(
        f"{url}?{urllib.parse.urlencode(params)}",
        headers={"Accept": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        if e.code == 401:
            return "OpenWeather API Key 无效或未激活，请检查 OPENWEATHER_API_KEY。"
        if e.code == 404:
            return f"未找到城市：{city}。请尝试英文名或「城市名,国家代码」如 Beijing,CN。"
        return f"请求天气接口失败：HTTP {e.code}。"
    except Exception as e:
        return f"请求天气接口失败：{e}"

    w = data.get("weather", [{}])[0]
    main = data.get("main", {})
    wind = data.get("wind", {})
    name = data.get("name", city)
    desc = w.get("description", "")
    temp = main.get("temp")
    feels = main.get("feels_like")
    humidity = main.get("humidity")
    speed = wind.get("speed")
    parts = [f"{name}：{desc}"]
    if temp is not None:
        parts.append(f"气温 {temp}°C")
    if feels is not None:
        parts.append(f"体感 {feels}°C")
    if humidity is not None:
        parts.append(f"湿度 {humidity}%")
    if speed is not None:
        parts.append(f"风速 {speed} m/s")
    return "，".join(parts)


@tool
def get_weather(city: str) -> str:
    """
    查询指定城市的当前天气（使用 OpenWeather API）。
    city: 城市名，如 Beijing、上海、London。可写「城市名,国家代码」提高准确性，如 Tokyo,JP。
    需要设置环境变量 OPENWEATHER_API_KEY。
    """
    return _fetch_openweather(city)


def get_tools():
    """返回 Agent 使用的工具列表。"""
    return [calculator, get_current_time, get_weather]
