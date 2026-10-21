package com.raishxn.gtia.mega.common.client;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.raishxn.gtia.GTIAMod;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/** Core shaders of the megastructure effects: the effect program (opaque and additive) and the lens pass. */
public final class MegaFxShaders {

    @Nullable
    static ShaderInstance fx, fxAdd, lens;

    private MegaFxShaders() {}

    public static void register(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GTIAMod.id("mega_fx"),
                    DefaultVertexFormat.POSITION_TEX), s -> fx = s);
            // Same program with additive blending: a ShaderInstance applies its own blend mode.
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GTIAMod.id("mega_fx_add"),
                    DefaultVertexFormat.POSITION_TEX), s -> fxAdd = s);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), GTIAMod.id("mega_lens"),
                    DefaultVertexFormat.POSITION_TEX), s -> lens = s);
        } catch (IOException e) {
            GTIAMod.LOGGER.error("Megastructure shaders failed to load", e);
        }
    }
}
