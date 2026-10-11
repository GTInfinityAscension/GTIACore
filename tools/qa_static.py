"""Small, dependency-free checks for resources shipped with the GTIACore mod."""

import json
import struct
import sys
import tomllib
from zipfile import ZipFile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/gtia"
OVERLAYS = ASSETS / "textures/block/machines/catalytic_dehydrator"
REQUIRED_MATERIALS = {
    "material.gtia.pulsating_alloy",
    "material.gtia.conductive_alloy",
    "material.gtia.draconium",
    "material.gtia.awakened_draconium",
    "material.gtia.phase_matrix",
    "gtia:phase_matrix_deposit",
}


def check_languages() -> None:
    languages = {}
    for code in ("en_us", "pt_br"):
        path = (ROOT / "src/generated/resources/assets/gtia/lang/en_us.json"
                if code == "en_us" else ASSETS / "lang/pt_br.json")
        data = json.loads(path.read_text(encoding="utf-8"))
        if not isinstance(data, dict) or any(not isinstance(value, str) or not value for value in data.values()):
            raise ValueError(f"Invalid translation values: {path}")
        missing = REQUIRED_MATERIALS - data.keys()
        if missing:
            raise ValueError(f"Missing translations in {code}: {sorted(missing)}")
        languages[code] = set(data)
    if languages["en_us"] != languages["pt_br"]:
        raise ValueError("en_us and pt_br have different translation keys")


def check_overlays() -> None:
    expected = {
        f"overlay_{face}{suffix}.png"
        for face in ("front", "side", "top")
        for suffix in ("", "_active", "_active_emissive")
    }
    found = {path.name for path in OVERLAYS.glob("*.png")}
    if found != expected:
        raise ValueError(f"Overlay set differs: missing={sorted(expected - found)}, extra={sorted(found - expected)}")
    for path in OVERLAYS.glob("*.png"):
        data = path.read_bytes()
        if data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
            raise ValueError(f"Invalid PNG header: {path}")
        width, height, bit_depth, color_type = struct.unpack(">IIBB", data[16:26])
        if (width, height, bit_depth, color_type) != (16, 16, 8, 6):
            raise ValueError(f"Expected 16x16 RGBA overlay: {path}")


def check_built_jar() -> None:
    jars = sorted((ROOT / "build/libs").glob("gtia-*.jar"))
    if not jars:
        raise ValueError("No built GTIA jar found")
    with ZipFile(jars[-1]) as jar:
        metadata = tomllib.loads(jar.read("META-INF/mods.toml").decode("utf-8"))
        dependencies = {entry["modId"]: entry for entry in metadata["dependencies"]["gtia"]}
        gtna = dependencies.get("gtna", {})
        if (gtna.get("mandatory") is not True or gtna.get("versionRange") != "[0.5.3,)"
                or gtna.get("ordering") != "AFTER" or gtna.get("side") != "BOTH"):
            raise ValueError("Distribution jar must require GTNA 0.5.3+ on both sides, loaded before GTIACore")
        packed_lang = json.loads(jar.read("assets/gtia/lang/en_us.json"))
        source_lang = json.loads((ROOT / "src/generated/resources/assets/gtia/lang/en_us.json")
                                 .read_text(encoding="utf-8"))
        if packed_lang != source_lang:
            raise ValueError("Built jar has stale English translations; run build again after runData")
        jar.read("assets/gtia/models/block/machine/lv_catalytic_dehydrator.json")
        jar.read("assets/gtia/blockstates/lv_catalytic_dehydrator.json")
        if 'modId="enderio"' in jar.read("META-INF/mods.toml").decode("utf-8"):
            raise ValueError("Distribution jar still declares an EnderIO dependency")
        if 'modId = "draconicevolution"' in jar.read("META-INF/mods.toml").decode("utf-8"):
            raise ValueError("Distribution jar still declares a Draconic Evolution dependency")
        for icon_set in ("bright", "dull", "metallic", "rough", "shiny"):
            jar.read(f"assets/gtceu/textures/item/material_sets/{icon_set}/ingot_triple.png")
        jar.read("assets/gtceu/textures/item/material_sets/radioactive/ingot_triple_secondary.png")
        for icon_set in ("phase_eternity", "crystalline_damascus"):
            for form in ("ingot_triple", "ingot_quadruple", "ingot_quintuple", "plate_triple",
                         "plate_quadruple", "plate_quintuple", "plate_superdense"):
                jar.read(f"assets/gtceu/textures/item/material_sets/{icon_set}/{form}.png")
                jar.read(f"assets/gtceu/models/item/material_sets/{icon_set}/{form}.json")


def main() -> int:
    try:
        check_languages()
        check_overlays()
        check_built_jar()
    except (OSError, ValueError, KeyError, json.JSONDecodeError) as exc:
        print(f"QA failed: {exc}", file=sys.stderr)
        return 1
    print("QA static passed: translations, Dehydrator overlays, phase assets, and built jar")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
