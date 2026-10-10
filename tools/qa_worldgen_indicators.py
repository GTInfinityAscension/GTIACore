"""Count phase surface indicators in fully generated chunks and inspect blocks above them."""

import math
import sys
from pathlib import Path
from qa_worldgen_blocks import chunks
from qa_worldgen_cache import read_veins

world = Path(sys.argv[1])
centers = [v['center'] for v in read_veins(world / 'data/gtceu_ore_vein_cache.dat') if v.get('id') == 'gtia:phase_matrix_deposit']
found = []
def block_name(section, x, y, z):
    if section is None:
        return 'minecraft:air'
    states = section.get('block_states', {})
    palette = states.get('palette', [])
    if not palette:
        return 'minecraft:air'
    if len(palette) == 1:
        return palette[0]['Name']
    bits = max(4, (len(palette) - 1).bit_length())
    per_long = 64 // bits
    data = states['data']
    index = (y % 16) * 256 + (z % 16) * 16 + (x % 16)
    pi = ((data[index // per_long] & ((1 << 64) - 1)) >> ((index % per_long) * bits)) & ((1 << bits) - 1)
    return palette[pi]['Name']
for region in (world / 'region').glob('*.mca'):
    for chunk in chunks(region):
        if chunk.get('Status') != 'minecraft:full':
            continue
        sections = {s['Y']: s for s in chunk.get('sections', [])}
        for section in sections.values():
            if not any(s.get('Name') == 'gtia:phase_matrix_indicator' for s in section.get('block_states', {}).get('palette', [])):
                continue
            for y0 in range(16):
                y = section['Y'] * 16 + y0
                for z0 in range(16):
                    for x0 in range(16):
                        if block_name(section,x0,y,z0) != 'gtia:phase_matrix_indicator':
                            continue
                        x = chunk['xPos'] * 16 + x0
                        z = chunk['zPos'] * 16 + z0
                        above = block_name(sections.get((y+1)//16),x0,y+1,z0)
                        distance, deposit = min((round(math.hypot(x-c[0],z-c[2])),i) for i,c in enumerate(centers))
                        found.append((x,y,z,deposit,distance,above))
from collections import Counter
print(world, 'indicators=', len(found), 'above=', dict(Counter(p[5] for p in found)), 'deposits=', dict(Counter(p[3] for p in found)))
if '--positions' in sys.argv:
    print('positions=', found)
