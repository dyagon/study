from __future__ import annotations

import argparse

from bailian_wanxiang import wanxiang_text_to_image


def main() -> int:
    parser = argparse.ArgumentParser(description="百炼·万象（文生图）demo")
    parser.add_argument("prompt", help="图像描述文本（prompt）")
    parser.add_argument("--negative", default=None, help="负向提示词（可选）")
    parser.add_argument("--size", default="1024*1024", help='图片尺寸，例如 "1024*1024"')
    parser.add_argument("--n", type=int, default=1, help="生成张数")
    parser.add_argument("--model", default="wanx-v1", help='模型名，例如 "wanx-v1"')
    args = parser.parse_args()

    paths = wanxiang_text_to_image(
        args.prompt,
        negative_prompt=args.negative,
        size=args.size,
        n=args.n,
        model=args.model,
    )
    print("已保存：")
    for p in paths:
        print(f"- {p}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

