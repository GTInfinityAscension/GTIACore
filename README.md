<p align="center">
  <img src="docs/assets/logo.gif" width="160" alt="GregTech Infinity Ascension logo" />
</p>

# GTIACore

GTIACore is the core mod of the **GregTech Infinity Ascension (GTIA)** modpack, the way GTOCore and GTLCore are for their packs. It holds only what's unique to GTIA. Reusable machines and mechanics live in [GregTech Nexus Addon (GTNA)](https://github.com/GTInfinityAscension/GregTech-Nexus-Addon), which is a required dependency.

Started from the [GTCEu Modern addon template](https://github.com/GregTechCEu/GregTech-Addon-Template).

## Target

- Minecraft 1.20.1, Forge 47.4.23
- GTCEu Modern 7.5.3, GTNA 0.5.1+ (required on client and server)
- Java 17
- Mod ID: `gtia`

## What's in it

| Area | Content |
| --- | --- |
| Materials | Phase Matrix ore (Overworld, Y 40–60), Pulsating Alloy, Conductive Alloy (zero-loss 4 A wire), Draconium and Awakened Draconium, the MV chemistry materials (sodium aluminate, alumina, cryolite, V₂O₅...) |
| Extra forms | Double to quintuple ingots, triple to quintuple and superdense plates for the GTIA alloys |
| Steam machines | Wood Retort and Lime Kiln (primitive multiblocks built on the Coke Oven) |
| LV machines | Catalytic Dehydrator, Still, Fermentation Vat (LV to OpV), Diaphragm Cell |
| MV machines | Bayer Digester, Hall-Héroult Cell, Contact Converter, Ammonia Converter (Haber-Bosch), Solvay Tower |
| Tools | `RecipeExport`, which dumps every loaded recipe, item and fluid for the pack's quest checker |

Most multiblock structures are still provisional and have not been tested in the client yet.

## Build

```sh
./gradlew runData
./gradlew runGameTestServer   # 18 Forge GameTests
./gradlew assemble            # jar in build/libs/
python3 tools/qa_static.py    # lang, overlays and jar checks
```

GameTests live in `src/main/java/com/raishxn/gtia/gametest`, because `runGameTestServer` uses `sourceSets.main`.

## License

Code: LGPL-3.0, see [LICENSE.MD](LICENSE.MD). Original textures: CC BY-NC-SA 4.0. Third-party content: [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md). Contributing: [CONTRIBUTING.md](CONTRIBUTING.md).
