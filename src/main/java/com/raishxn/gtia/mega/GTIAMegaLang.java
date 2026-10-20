package com.raishxn.gtia.mega;

import com.raishxn.gtia.GTIAMod;

/** English text of the megastructures and the Ψ era (GTIA-D-311, D-312); pt_br and zh_cn are in lang. */
public final class GTIAMegaLang {

    private GTIAMegaLang() {}

    public static void init() {
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.matrioshka_brain.tooltip.0",
                "Shells of computers around the star of the Nexus System, built in five phases. Works only there.");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.matrioshka_brain.tooltip.1",
                "Each phase adds a shell: 2^(21+n) CWU/t at 300 K, more on cold outer shells (Landauer's limit).");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.matrioshka_brain.tooltip.2",
                "Sends CWU through Optical Data Transmission Hatches; CWU nobody asks for becomes Ψ.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.needs_star",
                "No star here: the Matrioshka Brain only works in the Nexus System.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.phases",
                "Phases built: %s of %s");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.shell",
                "Shell %s: %s K, %s CWU/t");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.cwut",
                "Computation in use: %s of %s CWU/t");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.psi",
                "Ψ of the owner: %s thought cycles");
        GTIAMod.REGISTRATE.addRawLang("dimension.gtia.nexus_system",
                "Nexus System");
        GTIAMod.REGISTRATE.addRawLang("planet.gtia.nexus_system",
                "Nexus System");
        GTIAMod.REGISTRATE.addRawLang("solar_system.gtia.nexus_system",
                "Nexus System");
        // Matrioshka screen
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_clock_down",
                "Clock -");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_clock_down.tip",
                "Lower the clock by 25% (less CWU, cooler shells).");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_clock_up",
                "Clock +");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_clock_up.tip",
                "Raise the clock by 25% (more CWU, hotter shells).");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_shells",
                "Shells");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_shells.tip",
                "Table of the five shells: radius, temperature, CWU, state.");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_manual",
                "Manual");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_manual.tip",
                "How the brain works, with its formulas.");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_shells_title",
                "Shells");
        GTIAMod.REGISTRATE.addRawLang("gtna.ui.o.mb_manual_title",
                "Field manual - Matrioshka Brain");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.star",
                "STAR");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.cool",
                "COOL");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.phases",
                "Phases %s / %s");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.clock",
                "clock %s%%");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.no_star",
                "No star here: build it in the Nexus System.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.cwu",
                "Computation");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.coolant",
                "Liquid helium");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.hottest",
                "Hottest shell");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.psi",
                "Ψ of the owner: %s");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.psi_rate",
                "+%s Ψ/s from idle CWU");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.overheat_warning",
                "%s shell(s) overheated: lower the clock or add helium");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.col_shell",
                "Shell");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.col_radius",
                "Radius");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.col_temp",
                "Temp.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.col_cwu",
                "CWU/t");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.col_state",
                "State");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.not_built",
                "not built");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.overheat",
                "overheated");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.ok",
                "computing");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.shells_hint",
                "Unbuilt shells show what they would give at this clock and coolant.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.0",
                "The Matrioshka Brain wraps the Nexus star in shells of computers. Each finished phase is one shell, computing with the starlight that reaches it.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.1",
                "= T = 278.6 K / sqrt(r / 100 blocks) × clock^(1/4) × (1 - 0.4 × coolant)");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.2",
                "A shell at radius r sits in the star's light. Radiating more power (a higher clock) needs a hotter shell; liquid helium carries part of the heat away.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.3",
                "= CWU/t of shell n = 2^(21+n) × clock × 300 K / T");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.4",
                "Landauer's limit: erasing a bit costs k·T·ln 2, so a colder shell computes more with the same energy. The cold outer shells give the most.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.5",
                "= T > 500 K → the shell overheats and computes nothing");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.6",
                "Raise the clock for more CWU and watch the hottest shell: the inner shells overheat first.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.7",
                "= Helium = 250 mB/s × phases × clock");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.8",
                "Feed liquid helium through a Fluid Input Hatch on the station. A full supply makes every shell 40% colder.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.9",
                "= Ψ/s = (max CWU/t × 20 - CWU used in the second) / 2^20");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.10",
                "CWU nobody asks for becomes Ψ (thought cycles), kept for the owner like the Nexus network's EU.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.11",
                "Send CWU through up to 2 Optical Data Transmission Hatches on the station. The brain only works in the Nexus System, around its star.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.12",
                "Phases 2 to 5 can come later: a phase counts once all its blocks and those of the phases before it are in place.");
        GTIAMod.REGISTRATE.addRawLang("gtia.matrioshka.ui.manual.13",
                "While it runs, every ring but the polar one leaves the world and turns; switching off puts them back.");
    }
}
