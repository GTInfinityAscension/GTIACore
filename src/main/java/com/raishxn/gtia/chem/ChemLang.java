package com.raishxn.gtia.chem;

import com.raishxn.gtia.GTIAMod;

/** English recipe type names of the chemistry plants (GTIA-D-281). */
public final class ChemLang {

    private ChemLang() {}

    public static void init() {
        GTIAMod.REGISTRATE.addRawLang("gtia.ostwald_converter", "Ostwald Converter");
        GTIAMod.REGISTRATE.addRawLang("gtia.claus_unit", "Claus Unit");
        GTIAMod.REGISTRATE.addRawLang("gtia.kroll_reactor", "Kroll Reactor");
        GTIAMod.REGISTRATE.addRawLang("gtia.siemens_reactor", "Siemens Reactor");
    }
}
