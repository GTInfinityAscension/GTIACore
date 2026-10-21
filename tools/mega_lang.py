"""Copies the pt_br and zh_cn text of the megastructures (assets/gtia/mega_lang/*.json, key -> [en, pt, zh]) into
assets/gtia/lang/pt_br.json and zh_cn.json, keeping every other key and its order. English comes from datagen (MegaLang)."""
import json, pathlib

root = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources/assets/gtia"
texts = {}
for f in sorted((root / "mega_lang").glob("*.json")):
    for key, value in json.loads(f.read_text(encoding="utf-8")).items():
        assert len(value) == 3, f"{f.name}: {key} needs en, pt and zh"
        texts[key] = value
# Keys merged last time: those no longer in mega_lang leave the lang files too.
managed = pathlib.Path(__file__).resolve().parent / "mega_lang_keys.txt"
old = set(managed.read_text(encoding="utf-8").split()) if managed.exists() else set()
dropped = old - set(texts)
for index, lang in ((1, "pt_br"), (2, "zh_cn")):
    path = root / "lang" / f"{lang}.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    for key in dropped:
        data.pop(key, None)
    for key, value in texts.items():
        data[key] = value[index]
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
managed.write_text("\n".join(sorted(texts)) + "\n", encoding="utf-8")
print(len(texts), "keys,", len(dropped), "dropped")
