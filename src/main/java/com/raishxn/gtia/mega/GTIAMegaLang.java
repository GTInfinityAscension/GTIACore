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
    }
}
