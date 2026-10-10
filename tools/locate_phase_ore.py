import sys
from pathlib import Path
sys.path.insert(0,'tools')
from qa_worldgen_blocks import chunks
from qa_worldgen_cache import read_veins
w=Path(sys.argv[1]); centers=[v['center'] for v in read_veins(w/'data/gtceu_ore_vein_cache.dat') if v.get('id')=='gtia:phase_matrix_deposit']
print('centers',list(enumerate(centers)))
ore=[]
for p in (w/'region').glob('*.mca'):
 for c in chunks(p):
  if c.get('Status')!='minecraft:full':continue
  for s in c.get('sections',[]):
   bs=s.get('block_states',{}); palette=bs.get('palette',[])
   targets={i for i,state in enumerate(palette) if 'phase_matrix_ore' in state.get('Name','')}
   if not targets:continue
   bits=max(4,(len(palette)-1).bit_length()); per=64//bits; data=bs.get('data',[])
   for j in range(4096):
    pi=0 if len(palette)==1 else ((data[j//per]&((1<<64)-1))>>((j%per)*bits))&((1<<bits)-1)
    if pi not in targets:continue
    x=c['xPos']*16+(j&15); y=s['Y']*16+((j>>8)&15); z=c['zPos']*16+((j>>4)&15)
    idx=min(range(len(centers)),key=lambda i:(x-centers[i][0])**2+(z-centers[i][2])**2)
    ore.append((idx,x,y,z,palette[pi]['Name']))
for i in range(len(centers)):
 print(i,[v[1:] for v in ore if v[0]==i])
