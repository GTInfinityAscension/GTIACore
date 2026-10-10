"""Extend existing GTIA icon sets using GTNA's shared masks for compound forms."""
from pathlib import Path
import importlib.util
import io
import json
import zipfile
from PIL import Image

root=Path(__file__).resolve().parents[1]
gtna=Path('/home/raishxn/MineProjects/GregTech-Nexus-Addon')
spec=importlib.util.spec_from_file_location('base_generator',root/'tools/generate_material_sets.py')
base=importlib.util.module_from_spec(spec);spec.loader.exec_module(base)
forms=['ingot_double','ingot_triple','ingot_quadruple','ingot_quintuple',
       'plate_triple','plate_quadruple','plate_quintuple','plate_superdense']
sets=[('phase_eternity','shiny',base.shader_phase_eternity,True,4),
      ('crystalline_damascus','metallic',base.shader_crystalline_damascus,False,4)]
with zipfile.ZipFile(base.find_gtceu_jar()) as zf:
 for name,parent,shader,full_color,frametime in sets:
  for form in forms:
   src=gtna/'src/main/resources/assets/gtceu/textures/item/material_sets'/parent
   def mask(suffix):
    p=src/(form+suffix+'.png')
    if p.exists():return Image.open(p).convert('RGBA').crop((0,0,16,16))
    j=f'assets/gtceu/textures/item/material_sets/{parent}/{form}{suffix}.png'
    return Image.open(io.BytesIO(zf.read(j))).convert('RGBA').crop((0,0,16,16)) if j in zf.namelist() else Image.new('RGBA',(16,16))
   l0,l1,l2=(mask(''),mask('_secondary'),mask('_overlay'))
   if not l0.getbbox():raise RuntimeError(f'No mask for {parent}/{form}')
   strip=base.build_strip(l0,l1,l2,shader)
   tex=root/'src/main/resources/assets/gtceu/textures/item/material_sets'/name
   mod=root/'src/main/resources/assets/gtceu/models/item/material_sets'/name
   tex.mkdir(parents=True,exist_ok=True);mod.mkdir(parents=True,exist_ok=True)
   paths=[tex/(form+'.png'),mod/(form+'.json')]
   if full_color:paths.extend([tex/(form+'_overlay.png'),tex/(form+'_overlay.png.mcmeta')])
   else:paths.append(tex/(form+'.png.mcmeta'))
   if all(p.exists() for p in paths):
    continue
   if any(p.exists() for p in paths):
    raise RuntimeError(f'Partial existing art requires review: {name}/{form}')
   mcmeta={'animation':{'interpolate':True,'frametime':frametime}}
   if full_color:
    l0.save(tex/(form+'.png'))
    strip.save(tex/(form+'_overlay.png'))
    (tex/(form+'_overlay.png.mcmeta')).write_text(json.dumps(mcmeta,indent=2)+'\n')
    layers={'layer0':f'gtceu:item/material_sets/{name}/{form}','layer1':'gtceu:item/void',
            'layer2':f'gtceu:item/material_sets/{name}/{form}_overlay'}
   else:
    strip.save(tex/(form+'.png'))
    (tex/(form+'.png.mcmeta')).write_text(json.dumps(mcmeta,indent=2)+'\n')
    layers={'layer0':f'gtceu:item/material_sets/{name}/{form}'}
   (mod/(form+'.json')).write_text(json.dumps({'parent':'item/generated','textures':layers},indent=2)+'\n')
print('Added 16 compound forms to the two existing GTIA icon sets')
