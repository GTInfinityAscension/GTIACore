package com.raishxn.gtia.mega.nidavellir;

/**
 * Physics of the Nidavellir Hyperforge (GTIA-D-323): alloys pressed with degenerate matter.
 *
 * <ul>
 * <li>Each batch is pressed under a layer of neutronium (degenerate neutron matter, ~4 × 10¹⁷ kg/m³): higher
 * compression packs more parallels into the press and spends more neutronium, 2^c per batch for 256 × 2^c
 * parallels.</li>
 * <li>Around a neutron star the forge rides its magnetosphere: fields of 10⁸–10¹¹ T do the pressing, four times as
 * fast.</li>
 * <li>From the Stellar Engine's pulsar vicinity on, the neutronium comes fresh from its wind: half the use.</li>
 * </ul>
 */
public final class NidavellirPhysics {

    public static final int COMPRESSION_MAX = 4;
    public static final double SPEED_NEUTRON_STAR = 4, SPEED = 1, EU_FACTOR = 1;

    private NidavellirPhysics() {}

    /** Highest compression the phases built allow: one step each, all four from phase 4. */
    public static int maxCompression(int phases) {
        return Math.max(0, Math.min(COMPRESSION_MAX, phases - 1));
    }

    public static int parallel(int compression) {
        return 256 << compression;
    }

    /** Neutronium dust each batch takes. */
    public static int neutronium(int compression, boolean pulsarWind) {
        int n = 1 << compression;
        return pulsarWind ? Math.max(1, n / 2) : n;
    }
}
