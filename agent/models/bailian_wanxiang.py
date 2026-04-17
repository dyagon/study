from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable
from urllib.request import urlretrieve

from dashscope import ImageSynthesis


def _iter_result_urls(results: Iterable[Any]) -> list[str]:
    urls: list[str] = []
    for r in results:
        url = None
        if isinstance(r, dict):
            url = r.get("url")
        else:
            url = getattr(r, "url", None)
        if url:
            urls.append(url)
    return urls


@dataclass(frozen=True)
class WanxiangTextToImage:
    """
    百炼·万象（文生图）最小封装。

    依赖环境变量：
    - DASHSCOPE_API_KEY
    """

    model: str = "wan2.6-t2i"
    size: str = "1024*1024"
    n: int = 1

    def generate(
        self,
        prompt: str,
        *,
        negative_prompt: str | None = None,
        out_dir: str | Path = "outputs/wanxiang",
        filename_prefix: str = "wanxiang",
        extra_input: dict[str, Any] | None = None,
    ) -> list[Path]:
        if not prompt or not prompt.strip():
            raise ValueError("prompt 不能为空")
        if self.n < 1:
            raise ValueError("n 必须 >= 1")

        out_dir = Path(out_dir)
        out_dir.mkdir(parents=True, exist_ok=True)

        merged_extra_input: dict[str, Any] = {"size": self.size, "n": self.n}
        if extra_input:
            merged_extra_input.update(extra_input)

        rsp = ImageSynthesis.call(
            model=self.model,
            prompt=prompt,
            negative_prompt=negative_prompt,
            extra_input=merged_extra_input,
        )
        rsp = ImageSynthesis.wait(rsp)

        output = getattr(rsp, "output", None)
        if not output:
            raise RuntimeError(f"万象返回异常：{rsp}")

        results = getattr(output, "results", None) or []
        urls = _iter_result_urls(results)
        if not urls:
            task_status = getattr(output, "task_status", None)
            raise RuntimeError(f"未拿到图片结果（task_status={task_status}）：{rsp}")

        saved: list[Path] = []
        for i, url in enumerate(urls, start=1):
            target = out_dir / f"{filename_prefix}-{i}.png"
            urlretrieve(url, target)
            saved.append(target)
        return saved


def wanxiang_text_to_image(
    prompt: str,
    *,
    negative_prompt: str | None = None,
    out_dir: str | Path = "outputs/wanxiang",
    size: str = "1024*1024",
    n: int = 1,
    model: str = "wanx-v1",
    extra_input: dict[str, Any] | None = None,
) -> list[Path]:
    return WanxiangTextToImage(model=model, size=size, n=n).generate(
        prompt,
        negative_prompt=negative_prompt,
        out_dir=out_dir,
        extra_input=extra_input,
    )

