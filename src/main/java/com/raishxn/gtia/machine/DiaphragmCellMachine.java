package com.raishxn.gtia.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.data.GTIAAlloys;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;

/**
 * EN: A row of 3 to 9 Hooker cells in series (GTIA-D-302). Each cell is one parallel, still limited by the energy
 * the hatches give. GTCEu repeats single aisles only, so the machine tries one pattern per cell count.
 * PT: Fileira de 3 a 9 células Hooker em série (GTIA-D-302). Cada célula é um paralelo, ainda limitado pela energia
 * dos hatches. O GTCEu só repete um aisle por vez, então a máquina testa um padrão por número de células.
 * ZH: 3 至 9 个串联的胡克电解槽 (GTIA-D-302)。每个电解槽提供一个并行, 仍受能源仓供电限制。GTCEu 只能重复单个 aisle,
 * 所以按电解槽数量逐个尝试结构。
 */
public class DiaphragmCellMachine extends WorkableElectricMultiblockMachine {

    public static final int MIN_CELLS = 3;
    public static final int MAX_CELLS = 9;
    private static final BlockPattern[] PATTERNS = new BlockPattern[MAX_CELLS + 1];

    private volatile int cells = MIN_CELLS;

    public DiaphragmCellMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public int getCells() {
        return cells;
    }

    @Override
    public BlockPattern getPattern() {
        return pattern(getDefinition(), cells);
    }

    @Override
    public boolean checkPattern() {
        // The last count that formed first, then the others.
        if (pattern(getDefinition(), cells).checkPatternAt(getMultiblockState(), false)) return true;
        for (int n = MIN_CELLS; n <= MAX_CELLS; n++) {
            if (n != cells && pattern(getDefinition(), n).checkPatternAt(getMultiblockState(), false)) {
                cells = n;
                return true;
            }
        }
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) textList.add(Component.translatable("gtia.machine.diaphragm_cell.cells", cells));
    }

    /** One parallel per cell, cut to what the inputs and the energy allow. */
    public static @NotNull ModifierFunction cellParallel(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof DiaphragmCellMachine cell) || !cell.isFormed()) return ModifierFunction.IDENTITY;
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, cell.cells);
        if (parallels <= 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels)
                .parallels(parallels)
                .build();
    }

    public static List<MultiblockShapeInfo> shapes(MultiblockMachineDefinition definition) {
        List<MultiblockShapeInfo> shapes = new ArrayList<>();
        for (int n : new int[] { MIN_CELLS, 5, MAX_CELLS }) {
            int[] once = new int[layout(n).length];
            Arrays.fill(once, 1);
            shapes.add(new MultiblockShapeInfo(pattern(definition, n).getPreview(once)));
        }
        return shapes;
    }

    public static synchronized BlockPattern pattern(MultiblockMachineDefinition definition, int n) {
        if (PATTERNS[n] == null) PATTERNS[n] = build(definition, n);
        return PATTERNS[n];
    }

    private static BlockPattern build(MultiblockMachineDefinition definition, int n) {
        var energy = Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)
                .setPreviewCount(1);
        var rectifier = blocks(GTBlocks.MACHINE_CASING_LV.get());
        var steel = blocks(GTBlocks.CASING_STEEL_SOLID.get());
        var pipe = blocks(GTBlocks.CASING_STEEL_PIPE.get());
        var factory = FactoryBlockPattern.start();
        for (String[] aisle : layout(n)) factory.aisle(aisle);
        return factory
                .where('C', Predicates.controller(blocks(definition.getBlock())))
                .where('M', steel)
                .where('W', blocks(GTBlocks.LIGHT_CONCRETE.get()))
                .where('G', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                .where('D', blocks(Blocks.IRON_BARS))
                .where('U', Predicates.frames(GTIAAlloys.CONDUCTIVE_ALLOY))
                .where('P', pipe)
                .where('Z', Predicates.frames(GTMaterials.Steel))
                .where('L', rectifier)
                .where('e', energy.or(rectifier))
                .where('m', Predicates.autoAbilities(true, false, false).or(rectifier))
                .where('i', Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1)
                        .setPreviewCount(1).or(steel))
                .where('o', Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)
                        .setPreviewCount(3).or(pipe))
                .where('b', Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1)
                        .setPreviewCount(1).or(steel))
                .where('#', Predicates.air())
                .where(' ', Predicates.any())
                .build();
    }

    /**
     * The row as FactoryBlockPattern aisles (chars along +x, strings up, aisles toward the front). Same grid as
     * design/estudos/multiblocos_quimica01/designs.py cell_row(n): brine tank, n cells with busbars between them,
     * Cl2 collector on top, H2 behind, liquor in front, rectifier with the controller at the end.
     */
    static String[][] layout(int n) {
        int end = 4 * n, x0 = -3, width = end + 6;
        char[][][] g = new char[width][6][7];
        for (char[][] plane : g) for (char[] row : plane) Arrays.fill(row, ' ');
        Grid b = (x, y, z, c) -> g[x - x0][y][z + 1] = c;
        for (int k = 0; k < n; k++) {
            int x = 1 + 4 * k;
            box(b, x, 0, 0, x + 2, 2, 4, 'M');
            box(b, x + 1, 1, 1, x + 1, 2, 3, '#');
            box(b, x + 1, 1, 2, x + 1, 2, 2, 'D');
            box(b, x + 1, 1, 4, x + 1, 2, 4, 'G');
            box(b, x + 1, 1, 0, x + 1, 2, 0, 'G');
            box(b, x, 3, 0, x + 2, 3, 4, 'W');
            b.set(x + 1, 4, 2, 'P');
            if (k < n - 1) box(b, x + 3, 0, 1, x + 3, 2, 3, 'U');
            b.set(x + 2, 0, 4, 'b');
        }
        box(b, 1, 5, 2, end - 1, 5, 2, 'P');
        box(b, end, 4, 2, end, 5, 2, 'P');
        b.set(end, 5, 2, 'o');
        box(b, 1, 2, -1, end - 1, 2, -1, 'P');
        b.set(end, 2, -1, 'o');
        box(b, 1, 0, 5, end - 1, 0, 5, 'P');
        b.set(end, 0, 5, 'o');
        box(b, 0, 0, 1, 0, 2, 3, 'U');
        box(b, end, 0, 1, end, 2, 3, 'U');
        box(b, end + 1, 0, 1, end + 2, 2, 3, 'L');
        b.set(end + 2, 1, 2, 'e');
        b.set(end + 1, 1, 3, 'C');
        b.set(end + 2, 0, 3, 'm');
        for (int x : new int[] { -3, -1 }) for (int z : new int[] { 0, 4 }) box(b, x, 0, z, x, 5, z, 'Z');
        box(b, -3, 0, 1, -1, 0, 3, 'M');
        box(b, -3, 1, 1, -1, 4, 3, 'G');
        box(b, -2, 1, 0, -2, 4, 0, 'G');
        box(b, -2, 1, 4, -2, 4, 4, 'G');
        box(b, -3, 5, 1, -1, 5, 3, 'M');
        b.set(-2, 5, 2, 'i');
        box(b, -2, 3, -1, 0, 3, -1, 'P');
        String[][] aisles = new String[7][6];
        for (int z = 0; z < 7; z++)
            for (int y = 0; y < 6; y++) {
                var row = new StringBuilder();
                for (int x = 0; x < width; x++) row.append(g[x][y][z]);
                aisles[z][y] = row.toString();
            }
        return aisles;
    }

    private interface Grid {

        void set(int x, int y, int z, char c);
    }

    private static void box(Grid b, int x0, int y0, int z0, int x1, int y1, int z1, char c) {
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) b.set(x, y, z, c);
    }
}
