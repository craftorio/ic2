#!/usr/bin/env python3
"""
基于中文翻译(zh_cn.json)为基准, 使用本地 llama.cpp 服务将缺失条目翻译为英文,
并补充到 en_us.json 中。每次运行后输出翻译总结。

用法:
    python translate_i18n.py                     # 使用默认配置
    python translate_i18n.py --dry-run           # 预览模式, 不实际写入
    python translate_i18n.py --base-url http://localhost:8080/v1
    python translate_i18n.py --batch-size 10     # 每批翻译条数
"""

import json
import sys
import time
import argparse
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import URLError

# ---------- 默认配置 ----------
DEFAULT_BASE_URL = "http://localhost:8080/v1"
DEFAULT_MODEL = "local-model"
DEFAULT_BATCH_SIZE = 15
DEFAULT_TEMPERATURE = 0.2
DEFAULT_MAX_TOKENS = 256
REQUEST_TIMEOUT = 120  # 秒

# 脚本所在目录 -> 项目根目录
PROJECT_ROOT = Path(__file__).resolve().parent.parent
ZH_CN_PATH = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "ic2" / "lang" / "zh_cn.json"
EN_US_PATH = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "ic2" / "lang" / "en_us.json"

# llama.cpp 的 OpenAI 兼容端点
CHAT_ENDPOINT = "/chat/completions"

SYSTEM_PROMPT = """You are a translator for Minecraft Industrial Craft 2 (IC2) mod localization.
Translate Simplified Chinese to English for the en_us.json language file.

Rules:
1. Output ONLY valid JSON object: {"key1": "translation1", "key2": "translation2", ...}
2. Follow established IC2 naming conventions (e.g., use "BatBox", "MFE", "MFSU", "UU-Matter", "CF Powder", etc.)
3. Preserve all format specifiers exactly: %s, %1$s, %2$s, %d, %%, etc.
4. For machine/block names, use concise descriptive English names
5. For crops, use the standard English crop names already established in the mod
6. For advancements, match the witty/playful tone of existing IC2 English advancements
7. Keep button/UI labels short and clear
8. Do NOT translate the JSON keys - only the values"""


def load_json(path: Path) -> dict:
    if not path.exists():
        print(f"[错误] 文件不存在: {path}")
        sys.exit(1)
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def save_json(path: Path, data: dict):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent="\t")
    print(f"[保存] 已写入 {path}")


def find_missing_keys(zh: dict, en: dict) -> dict:
    """找出 zh_cn 中存在但 en_us 中缺失的条目"""
    missing = {}
    for key, zh_value in zh.items():
        if key not in en:
            missing[key] = zh_value
    return missing


def translate_batch(base_url: str, model: str, batch: dict,
                    temperature: float, max_tokens: int) -> dict:
    """调用 llama.cpp API 翻译一批条目, 返回 {key: english_value, ...}"""
    url = base_url.rstrip("/") + CHAT_ENDPOINT

    # 构造用户消息: 每行一个 "key": "中文值"
    lines = []
    for key, value in batch.items():
        lines.append(f'"{key}": "{value}"')
    user_input = "\n".join(lines)

    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": (
                "Translate these Chinese Minecraft IC2 mod strings to English. "
                "Return ONLY a JSON object with the same keys and English values:\n\n"
                + user_input
            )},
        ],
        "temperature": temperature,
        "max_tokens": max_tokens,
        "response_format": {"type": "json_object"},
    }

    body = json.dumps(payload).encode("utf-8")
    req = Request(url, data=body, headers={
        "Content-Type": "application/json",
    })

    for attempt in range(3):
        try:
            with urlopen(req, timeout=REQUEST_TIMEOUT) as resp:
                result = json.loads(resp.read().decode("utf-8"))
                content = result["choices"][0]["message"]["content"].strip()
                # 清理可能的 markdown 代码块标记
                if content.startswith("```"):
                    lines_list = content.split("\n")
                    lines_list = [l for l in lines_list if not l.startswith("```")]
                    content = "\n".join(lines_list)
                translated = json.loads(content)
                return translated
        except (json.JSONDecodeError, KeyError, IndexError) as e:
            if attempt < 2:
                time.sleep(2 * (attempt + 1))
            else:
                print(f"  [警告] JSON 解析失败 (已重试3次): {e}")
                print(f"  原始返回: {content[:500]}")
                return {}
        except URLError as e:
            if attempt < 2:
                time.sleep(3 * (attempt + 1))
            else:
                print(f"  [错误] 连接失败: {e}")
                return {}

    return {}


def main():
    parser = argparse.ArgumentParser(
        description="使用 llama.cpp 将中文 IC2 翻译条目转为英文并补充到 en_us.json"
    )
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL,
                        help=f"llama.cpp OpenAI 兼容 API 地址 (默认: {DEFAULT_BASE_URL})")
    parser.add_argument("--model", default=DEFAULT_MODEL,
                        help=f"模型名称 (默认: {DEFAULT_MODEL})")
    parser.add_argument("--batch-size", type=int, default=DEFAULT_BATCH_SIZE,
                        help=f"每批翻译条数 (默认: {DEFAULT_BATCH_SIZE})")
    parser.add_argument("--temperature", type=float, default=DEFAULT_TEMPERATURE,
                        help=f"温度参数 (默认: {DEFAULT_TEMPERATURE})")
    parser.add_argument("--max-tokens", type=int, default=DEFAULT_MAX_TOKENS,
                        help=f"最大输出 token 数 (默认: {DEFAULT_MAX_TOKENS})")
    parser.add_argument("--dry-run", action="store_true",
                        help="预览模式, 只显示缺失条目, 不翻译不写入")
    parser.add_argument("--limit", type=int, default=0,
                        help="限制翻译条数, 用于测试 (0=全部)")
    args = parser.parse_args()

    # ---------- 加载 ----------
    print("=" * 60)
    print("IC2 翻译补全工具 (llama.cpp)")
    print("=" * 60)

    zh = load_json(ZH_CN_PATH)
    en = load_json(EN_US_PATH)

    print(f"[信息] zh_cn.json: {len(zh)} 条")
    print(f"[信息] en_us.json: {len(en)} 条")

    # ---------- 找出缺失 ----------
    missing = find_missing_keys(zh, en)
    print(f"[信息] 英文缺失: {len(missing)} 条")

    if not missing:
        print("[完成] 没有缺失条目, 无需翻译。")
        return

    if args.limit > 0:
        missing = dict(list(missing.items())[:args.limit])
        print(f"[信息] 限制只翻译前 {args.limit} 条")

    if args.dry_run:
        print("\n" + "-" * 60)
        print("预览缺失条目 (前 30 条):")
        print("-" * 60)
        for i, (key, value) in enumerate(missing.items()):
            if i >= 30:
                print(f"... 及另外 {len(missing) - 30} 条")
                break
            print(f"  {key}")
            print(f"    zh: {value}")
        return

    # ---------- 分批翻译 ----------
    print(f"[信息] API 地址: {args.base_url}")
    print(f"[信息] 模型: {args.model}")
    print(f"[信息] 每批: {args.batch_size} 条")
    print()

    keys_list = list(missing.keys())
    total_batches = (len(keys_list) + args.batch_size - 1) // args.batch_size

    new_translations = {}
    failed = {}

    for batch_idx in range(total_batches):
        start = batch_idx * args.batch_size
        end = min(start + args.batch_size, len(keys_list))
        batch_keys = keys_list[start:end]
        batch = {k: missing[k] for k in batch_keys}

        print(f"[批次 {batch_idx + 1}/{total_batches}] 翻译 {len(batch)} 条...", end=" ", flush=True)

        result = translate_batch(
            args.base_url, args.model, batch,
            args.temperature, args.max_tokens
        )

        if result:
            added = 0
            for key in batch_keys:
                if key in result and result[key]:
                    new_translations[key] = result[key]
                    added += 1
                else:
                    failed[key] = batch[key]
            print(f"成功 {added}/{len(batch)}")
        else:
            for k in batch_keys:
                failed[k] = batch[k]
            print("全部失败")

        # 批间短暂停顿, 避免压垮服务
        if batch_idx < total_batches - 1:
            time.sleep(0.5)

    # ---------- 合并写入 ----------
    if new_translations:
        en.update(new_translations)
        save_json(EN_US_PATH, en)
        print(f"\n[写入] 新增 {len(new_translations)} 条到 en_us.json")

    # ---------- 总结 ----------
    print("\n" + "=" * 60)
    print("翻译总结")
    print("=" * 60)
    print(f"  中文总条目:     {len(zh)}")
    print(f"  英文原有条目:   {len(en) - len(new_translations)}")
    print(f"  本次翻译新增:   {len(new_translations)}")
    print(f"  翻译失败:       {len(failed)}")
    print(f"  英文现有条目:   {len(en)}")

    if new_translations:
        print(f"\n  {'─' * 50}")
        print(f"  新增翻译明细:")
        print(f"  {'─' * 50}")
        for key, value in sorted(new_translations.items()):
            print(f"  [+] {key}")
            print(f"      zh: {missing[key]}")
            print(f"      en: {value}")

    if failed:
        print(f"\n  {'─' * 50}")
        print(f"  翻译失败条目 (需人工翻译):")
        print(f"  {'─' * 50}")
        for key, value in sorted(failed.items()):
            print(f"  [-] {key}")
            print(f"      zh: {value}")

    print(f"\n[完成] en_us.json 已更新。")


if __name__ == "__main__":
    main()
