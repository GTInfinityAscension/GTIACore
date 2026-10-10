"""Summarize GTCEu's generated ore-vein cache from a fresh test world.

Usage: python tools/qa_worldgen_cache.py run/<world>/data/gtceu_ore_vein_cache.dat
"""

import gzip
import math
import struct
import sys
from pathlib import Path


class NBTReader:
    def __init__(self, data):
        self.data = memoryview(data)
        self.pos = 0

    def take(self, size):
        result = self.data[self.pos:self.pos + size]
        if len(result) != size:
            raise ValueError("Truncated NBT")
        self.pos += size
        return result

    def number(self, fmt):
        return struct.unpack(">" + fmt, self.take(struct.calcsize(fmt)))[0]

    def string(self):
        return self.take(self.number("H")).tobytes().decode("utf-8")

    def payload(self, kind):
        if kind in (1, 2, 3, 4, 5, 6):
            return self.number({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[kind])
        if kind == 8:
            return self.string()
        if kind in (7, 11, 12):
            count = self.number("i")
            unit = {7: 1, 11: 4, 12: 8}[kind]
            values = self.take(count * unit)
            if kind == 7:
                return values.tobytes()
            return list(struct.unpack(">" + str(count) + ("i" if kind == 11 else "q"), values))
        if kind == 9:
            element_type = self.number("B")
            return [self.payload(element_type) for _ in range(self.number("i"))]
        if kind == 10:
            result = {}
            while (child_type := self.number("B")) != 0:
                name = self.string()
                result[name] = self.payload(child_type)
            return result
        raise ValueError(f"Unsupported NBT tag {kind}")

    def root(self):
        if self.number("B") != 10:
            raise ValueError("Expected compound root")
        self.string()
        return self.payload(10)


def read_veins(path):
    root = NBTReader(gzip.decompress(Path(path).read_bytes())).root()
    return [vein for group in root.get("data", {}).values() for vein in group]


def summarize(path):
    veins = read_veins(path)
    phase = [vein for vein in veins if vein.get("id") == "gtia:phase_matrix_deposit"]
    distances = [round(math.hypot(vein["center"][0], vein["center"][2])) for vein in phase]
    return len(veins), len(phase), min(distances) if distances else None


if __name__ == "__main__":
    for filename in sys.argv[1:]:
        total, phase, nearest = summarize(filename)
        print(f"{filename}: total_veins={total} phase_deposits={phase} nearest_center_blocks={nearest}")
