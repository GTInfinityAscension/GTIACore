package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

public final class GTIAMaterialIconSets {

    /**
     * Proposta 1: Nebulosa Esmeralda-Ciano com borda petróleo/violeta e estrelas (Full-Color em layer2).
     */
    public static final MaterialIconSet PHASE_ETERNITY = new MaterialIconSet("phase_eternity", MaterialIconSet.SHINY);

    /**
     * Opção 2A: Polaridade Invertida — Borda de Ouro Solar Metálico + Núcleo Rubi-Abissal (Full-Color em layer2).
     */
    public static final MaterialIconSet SOLAR_RUBY = new MaterialIconSet("solar_ruby", MaterialIconSet.BRIGHT);

    /**
     * Proposta 3A: Forja Cristalina / Damasco em prata neutra (Tingível via .color() em layer0).
     */
    public static final MaterialIconSet CRYSTALLINE_DAMASCUS = new MaterialIconSet("crystalline_damascus",
            MaterialIconSet.METALLIC);

    /**
     * Proposta 4: Vazio Roxo-Arcano com fenda diagonal magenta e pontos dourados (Full-Color em layer2).
     */
    public static final MaterialIconSet ASTRAL_VOID = new MaterialIconSet("astral_void", MaterialIconSet.SHINY);

    /**
     * Phase Crystal (Real AE2 Opt 3 + Animação 2: Ping-Pong de Realidade / Quartzo Turquesa com dupla faceta
     * deslocada).
     */
    public static final MaterialIconSet PHASE_CRYSTAL = new MaterialIconSet("phase_crystal", MaterialIconSet.CERTUS);

    private GTIAMaterialIconSets() {}

    public static void init() {}
}
