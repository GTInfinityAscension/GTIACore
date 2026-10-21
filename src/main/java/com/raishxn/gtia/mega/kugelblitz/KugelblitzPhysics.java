package com.raishxn.gtia.mega.kugelblitz;

/**
 * Physics of the Kugelblitz Containment Silo (GTIA-D-321): a black hole made of light.
 *
 * <ul>
 * <li>Enough light in a small enough volume collapses into a black hole of mass M = E / c².</li>
 * <li>Hawking (real values): T = 1.227 × 10²³ K / M; radiated power P = 3.56 × 10³² W / M² (M in kg); lifetime
 * t = 8.41 × 10⁻¹⁷ s × M³; Schwarzschild radius r_s = 1.485 × 10⁻²⁷ m × M.</li>
 * <li>In play the energy is scaled: {@link #EU_PER_KG} EU per kilogram and P = {@link #HAWKING_K} / M² EU/t, so a
 * hole of 10⁶ kg gives 2³⁰ EU/t and lasts about 200 days of play, one of 10⁴ kg is gone in under twenty seconds.
 * The ratios between mass, power and lifetime are Hawking's.</li>
 * </ul>
 */
public final class KugelblitzPhysics {

    public static final double EU_PER_KG = Math.pow(2, 40), HAWKING_K = Math.pow(2, 30) * 1e12;
    /** Below this mass the hole runs away and flashes out; the stabilisers (phase 3) hold a lighter one. */
    public static final double CRITICAL = 1e4, CRITICAL_STABILISED = 1e3;
    /**
     * The light collapses into a hole once it holds this much: a hole of 10⁵ kg lasts hours, so it does not flash
     * out the second it forms (a smaller one would evaporate faster than any laser can feed it).
     */
    public static final double FORM_MASS = 1e5;
    /** Charge rates the lasers can pour in, EU/t (phase 1 up to 2^40). */
    public static final double[] RATES = { 0, Math.pow(2, 36), Math.pow(2, 40), Math.pow(2, 44), Math.pow(2, 48) };
    /** Mass targets the charge stops at, kg. */
    public static final double[] TARGETS = { 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9 };
    /** Hawking radiation the petals (phase 5) catch as EU. */
    public static final double CATCH = 0.9;
    /** Mass the vault adds per item and per 1000 mB of fluid, kg. */
    // EN: A gram each, not a block's real mass: at 2^40 EU per kilogram a whole block would make cobblestone the best
    // fuel in the game. Balance choice, reviewed with the author.
    // PT: Um grama cada, não a massa real de um bloco: com 2^40 EU por quilo, um bloco inteiro faria do pedregulho o
    // melhor combustível do jogo. Escolha de balanço, a revisar com o autor.
    // ZH: 每件按一克计，而非方块的真实质量：按每千克 2^40 EU 计，整块会让圆石成为最好的燃料。平衡取值，待作者审阅。
    public static final double KG_PER_ITEM = 1e-3, KG_PER_BUCKET = 1e-3;
    public static final int INTEGRITY = 1000, REPAIR = 50;

    private KugelblitzPhysics() {}

    public static double critical(int phases) {
        return phases >= 3 ? CRITICAL_STABILISED : CRITICAL;
    }

    /** Hawking power in play, EU/t. */
    public static double hawkingEut(double mass) {
        return mass <= 0 ? 0 : HAWKING_K / (mass * mass);
    }

    /** Mass the hole loses to Hawking radiation in one second of play, kg. */
    public static double evaporated(double mass) {
        return hawkingEut(mass) * 20 / EU_PER_KG;
    }

    /** Seconds of play the hole would last with no feed: ∝ M³ (integral of dM/dt = -k/M²). */
    public static double lifetimeSeconds(double mass) {
        if (mass <= 0) return 0;
        double k = HAWKING_K * 20 / EU_PER_KG;
        return mass * mass * mass / (3 * k);
    }

    public static double hawkingKelvin(double mass) {
        return mass <= 0 ? 0 : 1.227e23 / mass;
    }

    public static double schwarzschildMetres(double mass) {
        return 1.485e-27 * mass;
    }

    /** The highest charge rate a phase allows. */
    public static int maxRate(int phases) {
        return phases >= 2 ? RATES.length - 1 : 2;
    }
}
