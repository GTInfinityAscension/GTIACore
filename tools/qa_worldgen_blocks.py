"""Count phase ore blocks in generated Minecraft region files of a test world."""

import gzip
import math
import sys
import zlib
from pathlib import Path

from qa_worldgen_cache import NBTReader, read_veins


def chunks(region_path):
    data = region_path.read_bytes()
    for index in range(1024):
        header = data[index * 4:index * 4 + 4]
        sector = int.from_bytes(header[:3], "big")
        if not sector:
            continue
        start = sector * 4096
        length = int.from_bytes(data[start:start + 4], "big")
        compression = data[start + 4]
        payload = data[start + 5:start + 4 + length]
        if compression == 1:
            payload = gzip.decompress(payload)
        elif compression == 2:
            payload = zlib.decompress(payload)
        elif compression != 3:
            raise ValueError(f"Unknown chunk compression {compression}")
        yield NBTReader(payload).root()


def scan(world):
    centers = [vein["center"] for vein in read_veins(world / "data/gtceu_ore_vein_cache.dat")
               if vein.get("id") == "gtia:phase_matrix_deposit"]
    per_deposit = [0] * len(centers)
    full_chunks = set()
    chunk_count = 0
    ore_count = 0
    ore_chunks = 0
    nearest = None
    for region in (world / "region").glob("*.mca"):
        for chunk in chunks(region):
            chunk_count += 1
            if chunk.get("Status") == "minecraft:full":
                full_chunks.add((chunk["xPos"], chunk["zPos"]))
            this_chunk = 0
            for section in chunk.get("sections", []):
                states = section.get("block_states", {})
                palette = states.get("palette", [])
                targets = {i for i, state in enumerate(palette)
                           if state.get("Name", "").startswith("gtia:") and
                           "phase_matrix_ore" in state.get("Name", "")}
                if not targets:
                    continue
                if len(palette) == 1:
                    this_chunk += 4096
                    continue
                bits = max(4, (len(palette) - 1).bit_length())
                per_long = 64 // bits
                mask = (1 << bits) - 1
                packed = states["data"]
                for block_index in range(4096):
                    word = packed[block_index // per_long] & ((1 << 64) - 1)
                    palette_index = (word >> ((block_index % per_long) * bits)) & mask
                    if palette_index not in targets:
                        continue
                    this_chunk += 1
                    x = chunk["xPos"] * 16 + (block_index & 15)
                    z = chunk["zPos"] * 16 + ((block_index >> 4) & 15)
                    distance = round(math.hypot(x, z))
                    nearest = distance if nearest is None else min(nearest, distance)
                    if centers:
                        deposit = min(range(len(centers)), key=lambda i:
                                      (x - centers[i][0]) ** 2 + (z - centers[i][2]) ** 2)
                        per_deposit[deposit] += 1
            if this_chunk:
                ore_chunks += 1
                ore_count += this_chunk
    coverage = []
    for x, _, z in centers:
        cx, cz = x // 16, z // 16
        coverage.append(all((cx + dx, cz + dz) in full_chunks
                            for dx in (-1, 0, 1) for dz in (-1, 0, 1)))
    return chunk_count, len(full_chunks), ore_chunks, ore_count, nearest, per_deposit, coverage


if __name__ == "__main__":
    for filename in sys.argv[1:]:
        scanned, full, bearing, ore, nearest, per_deposit, coverage = scan(Path(filename))
        print(f"{filename}: chunks={scanned} full_chunks={full} ore_chunks={bearing} "
              f"phase_ore_blocks={ore} nearest_ore_blocks={nearest} "
              f"blocks_per_deposit={per_deposit} fully_generated_deposits={coverage}")
