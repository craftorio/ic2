#!/usr/bin/env python3
"""Sync IC2 lang files from bundles partial translations + i18n/out chunks.

Usage:
  python3 scripts/sync_lang.py import-bundles     # pull ru from bundles, write i18n state
  python3 scripts/sync_lang.py prepare            # create work chunks for uk/pl (+ ru gaps)
  python3 scripts/sync_lang.py assemble           # merge all -> assets/ic2/lang/*.json

Bundles source: ../bundles/clients/DieHard/i18n/out/ic2.ru_ru.*.json
Extra chunks:    i18n/out/{lang}/*.json
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_DIR = ROOT / "src" / "main" / "resources" / "assets" / "ic2" / "lang"
I18N_DIR = ROOT / "i18n"
OUT_DIR = I18N_DIR / "out"
WORK_DIR = I18N_DIR / "work"
BUNDLES_OUT = ROOT.parent / "bundles" / "clients" / "DieHard" / "i18n" / "out"

TARGET_LANGS = ("ru_ru", "uk_ua", "pl_pl")
CHUNK_SIZE = 400

ADV_ALIAS = {
	"buildBatBox": "build_batbox",
	"buildDDrill": "build_diamond_drill",
	"buildDrill": "build_drill",
	"buildElecFurnace": "build_electric_furnace",
	"buildIndFurnace": "build_induction_furnace",
	"buildIDrill": "build_iridium_drill",
	"buildMFE": "build_mfe",
	"buildMFSU": "build_mfsu",
	"buildMatterGen": "build_matter_gen",
	"buildMiningLaser": "build_mining_laser",
	"buildNanoSaber": "build_nano_saber",
	"buildNanoSuit": "build_nano_suit",
	"buildNuclearReactor": "build_nuclear_reactor",
	"buildQArmor": "build_quantum_suits",
	"buildTeleporter": "build_teleporter",
	"buildTerraformer": "build_terraformer",
	"acquireRefinedIron": "acquire_steel",
	"acquireResin": "acquire_resin",
	"acquireMatter": "acquire_matter",
	"mineOre": "mine_ore",
	"compressUranium": "compress_uranium",
	"dieFromOwnNuke": "die_from_own_nuke",
	"explodeMachine": "explode_machine",
	"fallWithJetpack": "fall_with_jetpack",
	"getZapped": "get_zapped",
	"killCreeperWithChainsaw": "kill_creeper_with_chainsaw",
	"killDragonWithMiningLaser": "kill_dragon_with_mining_laser",
	"makeNuclearReactorExplode": "make_nuclear_reactor_explode",
	"replicateObject": "replicate_object",
	"starveWithQuantumHelmet": "starve_with_quantum_helmet",
	"teleportFarAway": "teleport_far_away",
	"terraformEndCultivation": "terraform_end_cultivation",
}


def camel_to_snake(name: str) -> str:
	s1 = re.sub(r"(.)([A-Z][a-z]+)", r"\1_\2", name)
	return re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", s1).lower()


def load_json(path: Path) -> dict:
	with open(path, "r", encoding="utf-8") as f:
		return json.load(f)


def save_json(path: Path, data: dict) -> None:
	path.parent.mkdir(parents=True, exist_ok=True)
	with open(path, "w", encoding="utf-8") as f:
		json.dump(dict(sorted(data.items())), f, ensure_ascii=False, indent="\t")
	print(f"[saved] {path} ({len(data)} keys)")


def load_en_us() -> dict:
	return load_json(LANG_DIR / "en_us.json")


def import_bundles_ru(en: dict) -> dict:
	"""Import partial ru from bundles with advancement key remapping."""
	if not BUNDLES_OUT.exists():
		print(f"WARN: bundles out dir missing: {BUNDLES_OUT}", file=sys.stderr)
		return {}

	ru_old: dict = {}
	for path in sorted(BUNDLES_OUT.glob("ic2.ru_ru.*.json")):
		ru_old.update(load_json(path))

	ru: dict = {}
	for key, value in ru_old.items():
		targets = [key]
		if key.startswith("advancements.ic2."):
			suffix = key.split(".", 2)[2]
			if suffix.endswith(".desc"):
				base = suffix[:-5]
				new_base = ADV_ALIAS.get(base, camel_to_snake(base))
				targets.append(f"advancements.ic2.{new_base}.desc")
			else:
				new_base = ADV_ALIAS.get(suffix, camel_to_snake(suffix))
				targets.append(f"advancements.ic2.{new_base}")
		for target in targets:
			if target in en:
				ru[target] = value
	return ru


def load_out_chunks(lang: str) -> dict:
	merged: dict = {}
	lang_dir = OUT_DIR / lang
	if not lang_dir.exists():
		return merged
	for path in sorted(lang_dir.glob("*.json")):
		data = load_json(path)
		if isinstance(data, dict):
			merged.update(data)
	return merged


def cmd_import_bundles(_: argparse.Namespace) -> None:
	en = load_en_us()
	ru = import_bundles_ru(en)
	I18N_DIR.mkdir(parents=True, exist_ok=True)
	state_path = I18N_DIR / "bundles_ru.json"
	save_json(state_path, ru)
	missing = len(en) - len(ru)
	print(f"Imported {len(ru)} ru keys from bundles ({missing} still missing vs en_us)")


def cmd_prepare(_: argparse.Namespace) -> None:
	en = load_en_us()
	WORK_DIR.mkdir(parents=True, exist_ok=True)
	OUT_DIR.mkdir(parents=True, exist_ok=True)

	ru_base = import_bundles_ru(en)
	ru_base.update(load_out_chunks("ru_ru"))

	for lang in TARGET_LANGS:
		existing = dict(ru_base) if lang == "ru_ru" else {}
		existing.update(load_out_chunks(lang))
		missing = {k: en[k] for k in en if k not in existing}
		if not missing:
			print(f"{lang}: complete ({len(existing)} keys)")
			continue

		items = sorted(missing.items())
		parts = (len(items) + CHUNK_SIZE - 1) // CHUNK_SIZE
		for i in range(0, len(items), CHUNK_SIZE):
			part = dict(items[i : i + CHUNK_SIZE])
			part_num = i // CHUNK_SIZE + 1
			work_path = WORK_DIR / f"ic2.{lang}.{part_num:03d}.json"
			chunk = {
				"id": f"ic2.{lang}.{part_num:03d}",
				"lang": lang,
				"part": part_num,
				"parts_total": parts,
				"mod_context": "Industrial Craft 2 — industrial machines, EU energy, ore processing, nuclear power, electric tools.",
				"entries": part,
			}
			save_json(work_path, chunk)
		print(f"{lang}: prepared {parts} chunk(s), {len(missing)} keys missing")


def cmd_assemble(_: argparse.Namespace) -> None:
	en = load_en_us()
	for lang in TARGET_LANGS:
		merged: dict = {}
		if lang == "ru_ru":
			merged.update(import_bundles_ru(en))
		merged.update(load_out_chunks(lang))

		result = dict(en)
		for key, value in merged.items():
			if key in result:
				result[key] = value

		untranslated = sum(1 for k in en if result.get(k) == en[k] and k not in merged)
		save_json(LANG_DIR / f"{lang}.json", result)
		if untranslated:
			print(f"  {lang}: {untranslated} keys still identical to en_us")


def main() -> None:
	parser = argparse.ArgumentParser(description="IC2 lang sync from bundles + i18n/out")
	sub = parser.add_subparsers(dest="cmd", required=True)
	sub.add_parser("import-bundles").set_defaults(func=cmd_import_bundles)
	sub.add_parser("prepare").set_defaults(func=cmd_prepare)
	sub.add_parser("assemble").set_defaults(func=cmd_assemble)
	args = parser.parse_args()
	args.func(args)


if __name__ == "__main__":
	main()
