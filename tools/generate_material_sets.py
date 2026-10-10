#!/usr/bin/env python3
"""
Gerador Universal de MaterialIconSets Animados — GregTech Infinity Ascension
============================================================================
Gera texturas animadas (16x128, 8 frames) + arquivos .png.mcmeta + modelos JSON
para os 4 MaterialIconSets autorais aprovados:
  1. phase_eternity       (Proposta 1  - Pulsating Alloy / Phase Matrix)
  2. solar_ruby           (Opção 2A-1  - Awakened Draconium c/ Pulso de Supernova)
  3. crystalline_damascus (Proposta 3A - Conductive Alloy / Damasco Tingível)
  4. astral_void          (Proposta 4  - Draconium / Vazio Arcano)

Uso:
  # Gerar apenas formas novas; arquivos existentes são protegidos contra sobrescrita:
  python3 tools/generate_material_sets.py --forms ingot plate

  # Regenerar formas existentes somente após revisão explícita da arte:
  python3 tools/generate_material_sets.py --forms ingot plate --overwrite-existing

  # Listar todas as formas disponíveis no jar do GTCEu 7.5.3:
  python3 tools/generate_material_sets.py --list-forms

Os diretórios padrão são resolvidos a partir deste script: arte privada em
design/arte/material_sets e recursos de jogo em
core/src/main/resources/assets/gtceu.
"""

import argparse
import glob
import io
import json
import math
import os
import zipfile
from pathlib import Path
from PIL import Image

DEFAULT_FORMS = [
    "ingot",
    "ingot_double",
    "plate",
    "plate_double",
    "plate_dense",
    "dust",
    "dust_small",
    "dust_tiny",
    "nugget",
    "rod",
    "rod_long",
    "gear",
    "foil",
    "ring",
    "bolt",
    "screw",
]

NUM_FRAMES = 8


def find_gtceu_jar():
    repo_root = Path(__file__).resolve().parents[1]
    versions = {}
    for line in (repo_root / "gradle.properties").read_text().splitlines():
        if "=" in line and not line.lstrip().startswith("#"):
            key, value = line.split("=", 1)
            versions[key.strip()] = value.strip()
    minecraft = versions["minecraft_version"]
    gtceu = versions["gtceu_version"]
    filename = f"gtceu-{minecraft}-{gtceu}-slim.jar"
    candidates = glob.glob(
        os.path.expanduser(
            f"~/.gradle/caches/modules-2/files-2.1/com.gregtechceu.gtceu/gtceu-{minecraft}/{gtceu}/**/{filename}"
        ),
        recursive=True,
    )
    return sorted(candidates)[0] if candidates else None


def lerp_color(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t),
    )


def multi_gradient(stops, t):
    t = max(0.0, min(1.0, t))
    for i in range(len(stops) - 1):
        p0, c0 = stops[i]
        p1, c1 = stops[i + 1]
        if p0 <= t <= p1:
            local_t = (t - p0) / (p1 - p0) if p1 > p0 else 0.0
            return lerp_color(c0, c1, local_t)
    return stops[-1][1]


# ==============================================================================
# 1. PHASE_ETERNITY (Proposta 1 - Nebulosa Esmeralda-Ciano + Estrelas de Fase)
# ==============================================================================
def shader_phase_eternity(x, y, lum, is_edge, spec_a, theta):
    dx = (x - 7.2) / 5.5
    dy = (y - 6.5) / 4.2
    dist = math.sqrt(dx * dx + dy * dy)
    breath = 0.08 * math.sin(theta)
    wave = 0.15 * math.sin(x * 1.1 - y * 0.9 + theta) + 0.11 * math.cos(x * 0.7 + y * 1.3 - theta)
    energy = max(0.0, min(1.0, (1.0 - dist * (0.75 - breath)) * 0.65 + lum * 0.45 + wave))
    stops = [
        (0.00, (14, 24, 42)),
        (0.28, (22, 72, 88)),
        (0.55, (42, 165, 140)),
        (0.80, (125, 248, 215)),
        (1.00, (245, 255, 252)),
    ]
    c = multi_gradient(stops, energy)
    star_spots = {(5, 6): 0.0, (9, 5): 1.6, (7, 6): 3.1, (11, 6): 4.7}
    if (x, y) in star_spots and not is_edge:
        twinkle = 0.45 + 0.50 * (0.5 + 0.5 * math.sin(theta + star_spots[(x, y)]))
        c = lerp_color(c, (255, 255, 245), twinkle)
    elif is_edge and (x + y) % 3 == 0:
        c = lerp_color(c, (105, 60, 150), 0.30 + 0.15 * math.sin(theta + x))
    return c


# ==============================================================================
# 2. SOLAR_RUBY (Opção 2A + Animação Opção 1: Pulso de Supernova & Coroa Solar)
# ==============================================================================
def shader_solar_ruby(x, y, lum, is_edge, spec_a, theta):
    dx = x - 7.3
    dy = (y - 6.8) * 1.25
    r = math.sqrt(dx * dx + dy * dy)
    angle = math.atan2(dy, dx)
    if is_edge:
        flare = max(0.0, math.cos(angle - theta)) ** 3
        t = max(0.0, min(1.0, lum * 0.95 + flare * 0.55))
        stops = [
            (0.00, (115, 48, 10)),
            (0.38, (215, 130, 20)),
            (0.72, (255, 210, 55)),
            (1.00, (255, 255, 225)),
        ]
        return multi_gradient(stops, t)
    else:
        wave = 0.5 + 0.5 * math.sin(r * 1.55 - theta)
        core_ignition = max(0.0, (1.0 - r / 2.6)) * (0.45 + 0.55 * (0.5 + 0.5 * math.cos(theta)))
        energy = max(0.0, min(1.0, (r / 4.8) * 0.62 + wave * 0.35 + core_ignition * 0.65))
        stops = [
            (0.00, (24, 4, 10)),
            (0.30, (98, 12, 24)),
            (0.58, (210, 42, 18)),
            (0.82, (255, 165, 30)),
            (1.00, (255, 252, 210)),
        ]
        return multi_gradient(stops, energy)


# ==============================================================================
# 3. CRYSTALLINE_DAMASCUS (Proposta 3A - Forja Cristalina Neutra Tingível)
# ==============================================================================
def shader_crystalline_damascus(x, y, lum, is_edge, spec_a, theta):
    band = math.sin((x * 1.4 + y * 1.8) + 0.5 * math.sin(theta)) + 0.6 * math.cos((x * 2.1 - y * 1.1) + theta)
    glint = 0.12 * math.sin((x - y) * 0.65 - theta)
    v = max(0.0, min(1.0, lum * 0.68 + (band * 0.20) + glint + (spec_a / 255.0) * 0.22))
    if is_edge:
        v *= 0.62
    stops = [
        (0.00, (28, 30, 34)),
        (0.30, (78, 82, 90)),
        (0.60, (145, 152, 162)),
        (0.85, (215, 222, 232)),
        (1.00, (255, 255, 255)),
    ]
    return multi_gradient(stops, v)


# ==============================================================================
# 4. ASTRAL_VOID (Proposta 4 - Vazio Roxo-Arcano + Fenda Magenta e Estrelas)
# ==============================================================================
def shader_astral_void(x, y, lum, is_edge, spec_a, theta):
    diag = ((x - 2) * 0.7 + (12 - y) * 0.5) / 11.0
    wave = 0.16 * math.sin(x * 0.9 + y * 1.2 - theta)
    pulse = 0.08 * math.cos(theta)
    t = max(0.0, min(1.0, diag * 0.55 + lum * 0.45 + wave + pulse))
    stops = [
        (0.00, (22, 8, 38)),
        (0.30, (68, 22, 112)),
        (0.58, (148, 52, 195)),
        (0.82, (235, 115, 210)),
        (1.00, (255, 235, 195)),
    ]
    c = multi_gradient(stops, t)
    stars = {(5, 8): 0.0, (8, 5): 2.1, (10, 7): 4.2}
    if (x, y) in stars and not is_edge:
        tw = 0.35 + 0.65 * (0.5 + 0.5 * math.sin(theta + stars[(x, y)]))
        c = lerp_color(c, (255, 242, 165), tw)
    return c


ICON_SETS = [
    ("phase_eternity", shader_phase_eternity, True, 3),
    ("solar_ruby", shader_solar_ruby, True, 3),
    ("crystalline_damascus", shader_crystalline_damascus, False, 4),
    ("astral_void", shader_astral_void, True, 3),
]


def load_mask_from_jar(zf, form_name):
    # Try shiny first, fallback to metallic, then dull
    for parent_set in ["shiny", "metallic", "dull"]:
        p0 = f"assets/gtceu/textures/item/material_sets/{parent_set}/{form_name}.png"
        if p0 in zf.namelist():
            l0 = Image.open(io.BytesIO(zf.read(p0))).convert("RGBA").crop((0, 0, 16, 16))
            p1 = f"assets/gtceu/textures/item/material_sets/{parent_set}/{form_name}_secondary.png"
            l1 = (
                Image.open(io.BytesIO(zf.read(p1))).convert("RGBA").crop((0, 0, 16, 16))
                if p1 in zf.namelist()
                else Image.new("RGBA", (16, 16), (0, 0, 0, 0))
            )
            p2 = f"assets/gtceu/textures/item/material_sets/{parent_set}/{form_name}_overlay.png"
            l2 = (
                Image.open(io.BytesIO(zf.read(p2))).convert("RGBA").crop((0, 0, 16, 16))
                if p2 in zf.namelist()
                else Image.new("RGBA", (16, 16), (0, 0, 0, 0))
            )
            return l0, l1, l2
    return None


def build_strip(l0, l1, l2, shader_fn):
    strip = Image.new("RGBA", (16, 16 * NUM_FRAMES), (0, 0, 0, 0))
    for frame_idx in range(NUM_FRAMES):
        theta = (2.0 * math.pi * frame_idx) / NUM_FRAMES
        y_off = frame_idx * 16
        for y in range(16):
            for x in range(16):
                r0, g0, b0, a0 = l0.getpixel((x, y))
                if a0 == 0:
                    continue
                _, _, _, a1 = l1.getpixel((x, y))
                _, _, _, a2 = l2.getpixel((x, y))
                lum = r0 / 255.0
                if a1 > 0:
                    lum *= 0.68
                if a2 > 0:
                    lum = min(1.0, lum + (a2 / 255.0) * 0.28)
                rgb = shader_fn(x, y, lum, a1 > 0, a2, theta)
                strip.putpixel((x, y_off + y), (rgb[0], rgb[1], rgb[2], a0))
    return strip


def main():
    parser = argparse.ArgumentParser(description="Gerador Universal de MaterialIconSets do GTIA")
    parser.add_argument("--forms", nargs="+", default=DEFAULT_FORMS, help="Formas a gerar (ex: ingot plate plate_double)")
    parser.add_argument("--list-forms", action="store_true", help="Lista todas as formas disponíveis no GTCEu")
    repo_root = Path(__file__).resolve().parents[1]
    parser.add_argument(
        "--ws-out",
        default=str(repo_root.parent / "design" / "arte" / "material_sets"),
        help="Pasta de saída da arte privada",
    )
    parser.add_argument(
        "--core-out", "--addon-out", dest="core_out",
        default=str(repo_root / "src" / "main" / "resources" / "assets" / "gtceu"),
        help="Pasta assets/gtceu de saída no GTIACore (--addon-out é alias antigo)",
    )
    parser.add_argument("--overwrite-existing", action="store_true",
                        help="Permite substituir arquivos de arte já existentes; usar somente após revisão do autor")
    args = parser.parse_args()

    jar_path = find_gtceu_jar()
    if not jar_path:
        raise RuntimeError("Não foi possível localizar no cache o jar slim do GTCEu definido em gradle.properties")

    with zipfile.ZipFile(jar_path, "r") as zf:
        if args.list_forms:
            avail = set()
            for name in zf.namelist():
                if name.startswith("assets/gtceu/textures/item/material_sets/dull/") and name.endswith(".png"):
                    base = os.path.basename(name)[:-4]
                    if not base.endswith("_overlay") and not base.endswith("_secondary"):
                        avail.add(base)
            print("Formas disponíveis no GTCEu:", ", ".join(sorted(avail)))
            return

        if not Path(args.ws_out).is_dir() or not Path(args.core_out).is_dir():
            parser.error("As pastas --ws-out e --core-out precisam existir antes de gerar texturas")

        if not args.overwrite_existing:
            collisions = []
            for set_name, _, _, _ in ICON_SETS:
                for form in args.forms:
                    for target in (
                        Path(args.ws_out) / set_name / f"{form}.png",
                        Path(args.ws_out) / set_name / f"{form}.png.mcmeta",
                        Path(args.core_out) / "textures/item/material_sets" / set_name / f"{form}.png",
                        Path(args.core_out) / "textures/item/material_sets" / set_name / f"{form}.png.mcmeta",
                        Path(args.core_out) / "textures/item/material_sets" / set_name / f"{form}_overlay.png",
                        Path(args.core_out) / "textures/item/material_sets" / set_name / f"{form}_overlay.png.mcmeta",
                        Path(args.core_out) / "models/item/material_sets" / set_name / f"{form}.json",
                    ):
                        if target.exists():
                            collisions.append(target)
            if collisions:
                parser.error(f"A geração substituiria {len(collisions)} arquivos existentes; "
                             "revise a arte e use --overwrite-existing somente com autorização explícita. "
                             f"Primeiro conflito: {collisions[0]}")

        for set_name, shader_fn, is_full_color, frametime in ICON_SETS:
            ws_target = os.path.join(args.ws_out, set_name)
            tex_target = os.path.join(args.core_out, "textures/item/material_sets", set_name)
            mod_target = os.path.join(args.core_out, "models/item/material_sets", set_name)
            os.makedirs(ws_target, exist_ok=True)
            os.makedirs(tex_target, exist_ok=True)
            os.makedirs(mod_target, exist_ok=True)

            mcmeta = {"animation": {"interpolate": True, "frametime": frametime}}

            for form in args.forms:
                masks = load_mask_from_jar(zf, form)
                if masks is None:
                    print(f"[AVISO] Forma '{form}' não encontrada no GTCEu, pulando...")
                    continue
                l0, l1, l2 = masks
                strip = build_strip(l0, l1, l2, shader_fn)

                strip.save(os.path.join(ws_target, f"{form}.png"))
                with open(os.path.join(ws_target, f"{form}.png.mcmeta"), "w") as mf:
                    json.dump(mcmeta, mf, indent=2)

                if is_full_color:
                    l0.save(os.path.join(tex_target, f"{form}.png"))
                    strip.save(os.path.join(tex_target, f"{form}_overlay.png"))
                    with open(os.path.join(tex_target, f"{form}_overlay.png.mcmeta"), "w") as mf:
                        json.dump(mcmeta, mf, indent=2)
                    model_data = {
                        "parent": "item/generated",
                        "textures": {
                            "layer0": f"gtceu:item/material_sets/{set_name}/{form}",
                            "layer1": "gtceu:item/void",
                            "layer2": f"gtceu:item/material_sets/{set_name}/{form}_overlay",
                        },
                    }
                else:
                    strip.save(os.path.join(tex_target, f"{form}.png"))
                    with open(os.path.join(tex_target, f"{form}.png.mcmeta"), "w") as mf:
                        json.dump(mcmeta, mf, indent=2)
                    model_data = {
                        "parent": "item/generated",
                        "textures": {
                            "layer0": f"gtceu:item/material_sets/{set_name}/{form}",
                        },
                    }

                with open(os.path.join(mod_target, f"{form}.json"), "w") as jf:
                    json.dump(model_data, jf, indent=2)

    print(f"Gerados {len(ICON_SETS)} MaterialIconSets com sucesso para as formas: {', '.join(args.forms)}")


if __name__ == "__main__":
    main()
