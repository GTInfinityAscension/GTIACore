package com.raishxn.gtia.data.rocket;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIARecipeTypes;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;

/**
 * Rocket Assembler (HV, GTIA-D-274): a 15 x 15 x 20 launch complex approved by the author on 2026-10-17. Two service
 * towers, a frame scaffold around the rocket, umbilical arms, a crane bridge, fuel and oxidiser tanks.
 */
public final class GTIARocketAssembler {

    /**
     * Front aisle (controller) to back aisle; in each aisle, one string per layer from the ground up.
     * C casing (hatches allowed), S solid steel, D grating, H/L hermetic HV, E hermetic LV, P launch pad,
     * F stainless steel frame, T steel pipe casing, G stainless gearbox, K controller, . anything.
     */
    public static final String[][] AISLES = {
            { ".....SDDDS.....", "......CCC......", "......CKC......", "......CCC......", "......GGG......",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { "....SSDDDSS....", "......CCC......", "......CCC......", "......CCC......", "......GGG......",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { "...SSSSSSSSS...", ".....CDDDC.....", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { "..SSSSSSSSSSS..", "....CCDDDCC....", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { ".SSSSSSSSSSSSS.", "...CCCDDDCCC...", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { "SSSSSSSSSSSSSSS", ".CCCCCDDDCCCCC.", ".CCH.......HCC.", ".CCH.F...F.HCC.", ".CCH.F...F.HCC.",
                    ".CCH.F...F.HCC.", ".CCH.F...F.HCC.", ".CCH.F...F.HCC.", ".CCH.F...F.HCC.", ".....FFFFF.....",
                    ".....F...F.....", ".....F...F.....", ".....F...F.....", ".....F...F.....", ".....F...F.....",
                    ".....FFFFF.....", "...............", "...............", "...............", "..............." },
            { "SSSSSSSSSSSSSSS", ".CCCCCCCCCCCCC.", ".CCC..PPP..CCC.", ".CCC.......CCC.", ".CCC.......CCC.",
                    ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.F...F.CCC.",
                    ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.......CCC.", "...............",
                    ".....F...F.....", "...............", "...............", "...............", "..............." },
            { "SSSSSSSSSSSSSSS", ".CCCCCCCCCCCCC.", ".CCG..PPP..GCC.", ".CCG.......GCC.", ".CCGTT...TTGCC.",
                    ".CCG.......GCC.", ".CCG.......GCC.", ".CCGTT...TTGCC.", ".CCG.......GCC.", ".CCG.F...F.GCC.",
                    ".CCGTT...TTGCC.", ".CCG.......GCC.", ".CCG.......GCC.", ".CCGTT...TTGCC.", "...CC.....CC...",
                    "...CCF...FCC...", "...CC..T..CC...", "...FFFFGFFFF...", "...F.......F...", "...F.......F..." },
            { "SSSSSSSSSSSSSSS", ".CCCCCCCCCCCCC.", ".CCC..PPP..CCC.", ".CCC.......CCC.", ".CCC.......CCC.",
                    ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.F...F.CCC.",
                    ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.......CCC.", ".CCC.......CCC.", "...............",
                    ".....F...F.....", "...............", "...............", "...............", "..............." },
            { "SSSSSSSSSSSSSSS", ".CCCCCDDDCCCCC.", ".CCH.......HCC.", ".CCH.F...F.HCC.", ".CCH.F...F.HCC.",
                    ".CCH.F...F.HCC.", ".CCH.F...F.HCC.", ".CCH.F...F.HCC.", ".CCH.F...F.HCC.", ".....FFFFF.....",
                    ".....F...F.....", ".....F...F.....", ".....F...F.....", ".....F...F.....", ".....F...F.....",
                    ".....FFFFF.....", "...............", "...............", "...............", "..............." },
            { ".SSSSSSSSSSSSS.", "...CCCDDDCCC...", "....T.....T....", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { "..SSSSSSSSSSS..", "....ECDDDCL....", "....T.....T....", "....E.....L....", "....E.....L....",
                    "....E.....L....", "....E.....L....", "....E.....L....", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { "...SSSSSSSSS...", "...EEEDDDLLL...", "...EEE...LLL...", "...EEE...LLL...", "...EEE...LLL...",
                    "...EEE...LLL...", "...EEE...LLL...", "...EEE...LLL...", "....G.....G....", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { "....SSDDDSS....", "....E.....L....", "....E.....L....", "....E.....L....", "....E.....L....",
                    "....E.....L....", "....E.....L....", "....E.....L....", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
            { ".....SDDDS.....", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "...............",
                    "...............", "...............", "...............", "...............", "..............." },
    };

    public static final MultiblockMachineDefinition ROCKET_ASSEMBLER = GTIAMod.REGISTRATE
            .multiblock("rocket_assembler", WorkableElectricMultiblockMachine::new)
            .langValue("Rocket Assembler")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.ROCKET_ASSEMBLY)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK)
            .appearanceBlock(GTIARocketBlocks.ROCKET_ASSEMBLY_CASING)
            .pattern(definition -> {
                var builder = FactoryBlockPattern.start();
                for (String[] aisle : AISLES) builder.aisle(aisle);
                return builder
                        .where('K', Predicates.controller(blocks(definition.getBlock())))
                        .where('C', blocks(GTIARocketBlocks.ROCKET_ASSEMBLY_CASING.get()).setMinGlobalLimited(250)
                                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                .or(Predicates.autoAbilities(true, false, false)))
                        .where('S', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                        .where('D', blocks(GTBlocks.CASING_GRATE.get()))
                        .where('H', blocks(GTBlocks.HERMETIC_CASING_HV.get()))
                        .where('L', blocks(GTBlocks.HERMETIC_CASING_HV.get()))
                        .where('E', blocks(GTBlocks.HERMETIC_CASING_LV.get()))
                        .where('P', launchPad())
                        .where('F', Predicates.frames(GTMaterials.StainlessSteel))
                        .where('T', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                        .where('G', blocks(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get()))
                        .where('.', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTIAMod.id("block/casings/rocket_assembly_casing"),
                    GTCEu.id("block/multiblock/gcym/large_assembler"))
            .tooltips(Component.translatable("gtia.machine.rocket_assembler.tooltip.purpose"),
                    Component.translatable("gtia.machine.rocket_assembler.tooltip.pad"))
            .register();

    /**
     * EN: The Ad Astra launch pad; GTIACore does not depend on Ad Astra, so without it (the dev and GameTest runtime)
     * the pad slot takes solid steel casing instead.
     * PT: A plataforma de lançamento do Ad Astra; o GTIACore não depende do Ad Astra, então sem ele (runtime de dev e
     * de
     * GameTest) o lugar da plataforma aceita casing de aço sólido.
     * ZH: Ad Astra 的发射台；GTIACore 不依赖 Ad Astra，因此在没有它时（开发和 GameTest 环境）该位置改用坚固钢外壳。
     */
    private static TraceabilityPredicate launchPad() {
        Block pad = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ad_astra", "launch_pad"));
        return pad != null && pad != net.minecraft.world.level.block.Blocks.AIR ? blocks(pad) :
                blocks(GTBlocks.CASING_STEEL_SOLID.get());
    }

    private GTIARocketAssembler() {}

    public static void init() {}
}
