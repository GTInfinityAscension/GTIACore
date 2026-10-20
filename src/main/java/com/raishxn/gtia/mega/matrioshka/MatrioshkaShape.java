package com.raishxn.gtia.mega.matrioshka;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * The Matrioshka Brain's block layout, read from {@code gtia_matrioshka/pattern.bin.gz}.
 *
 * <p>
 * The shape is the Gigastructural mod's model turned into blocks (design/estudos/matrioshka/gen_pattern.py). Until
 * that mod's author allows it, the file is kept out of git; without it the machine is not registered.
 *
 * <p>
 * Pattern axes follow {@code FactoryBlockPattern.start()}: char {@code k} goes left, string {@code j} up, aisle
 * {@code i} to the front. Grid x = k, y = j, z = i; the controller faces +z.
 */
public final class MatrioshkaShape {

    public static final String RESOURCE = "gtia_matrioshka/pattern.bin.gz";

    /** Letter of each part. */
    public enum Part {

        PLATFORM('A', 1, null, "gtna:singularity_reinforced_stellar_shielding_casing"),
        GYRO_OUTER('B', 2, "gyro_outer", "gtna:graviton_field_constraint_casing"),
        GYRO_INNER('C', 2, "gyro_inner", "gtna:transcendentally_amplified_magnetic_confinement_casing"),
        ANCHORAGE('D', 3, null, "gtna:space_elevator_mechanical_casing"),
        COORDINATION('E', 3, null, "gtna:graviton_computer_casing"),
        DECOMPRESSOR('F', 4, null, "gtna:dimensionally_transcendent_casing"),
        DECOMP_TOP('G', 4, "decomp_top", "gtna:infinite_spacetime_energy_boundary_casing"),
        DECOMP_BOTTOM('H', 4, "decomp_bottom", "gtna:infinite_spacetime_energy_boundary_casing"),
        RELAYS('I', 5, null, "gtna:three_proof_computer_casing"),
        CENTRAL_RING('J', 5, null, "gtna:force_field_glass"),
        HATCH('K', 1, null, "gtna:singularity_reinforced_stellar_shielding_casing");

        public final char letter;
        public final int phase;
        /** Moving part drawn by the controller while it runs, or null. */
        @Nullable
        public final String track;
        public final String block;

        Part(char letter, int phase, @Nullable String track, String block) {
            this.letter = letter;
            this.phase = phase;
            this.track = track;
            this.block = block;
        }

        @Nullable
        public static Part of(char letter) {
            for (Part p : values()) if (p.letter == letter) return p;
            return null;
        }
    }

    public final int nx, ny, nz;
    /** Controller cell (k, j, i). */
    public final int ck, cj, ci;
    /** Model origin in grid cells: the moving parts turn about it. */
    public final float pivotX, pivotY, pivotZ;
    /** Mean distance of each phase's blocks from the origin, in blocks (index 0 = phase 1). */
    public final float[] radius = new float[MatrioshkaShells.PHASES];
    /** Letters by [aisle i][string j][char k]. */
    private final byte[] cells;

    private MatrioshkaShape(InputStream raw) throws IOException {
        try (var in = new DataInputStream(new GZIPInputStream(raw))) {
            byte[] magic = new byte[4];
            in.readFully(magic);
            if (!"GTMB".equals(new String(magic, java.nio.charset.StandardCharsets.US_ASCII)))
                throw new IOException("not a Matrioshka pattern");
            nx = in.readInt();
            ny = in.readInt();
            nz = in.readInt();
            ck = in.readInt();
            cj = in.readInt();
            ci = in.readInt();
            pivotX = in.readFloat();
            pivotY = in.readFloat();
            pivotZ = in.readFloat();
            for (int p = 0; p < radius.length; p++) radius[p] = in.readFloat();
            cells = new byte[nx * ny * nz];
            in.readFully(cells);
        }
    }

    private static volatile boolean loaded;
    @Nullable
    private static MatrioshkaShape shape;

    /** The shape, or null when the data file is not in the jar. */
    @Nullable
    public static MatrioshkaShape get() {
        if (!loaded) {
            synchronized (MatrioshkaShape.class) {
                if (!loaded) {
                    try (var in = MatrioshkaShape.class.getClassLoader().getResourceAsStream(RESOURCE)) {
                        shape = in == null ? null : new MatrioshkaShape(in);
                    } catch (IOException e) {
                        throw new IllegalStateException("Broken " + RESOURCE, e);
                    }
                    loaded = true;
                }
            }
        }
        return shape;
    }

    public static boolean present() {
        return get() != null;
    }

    public char letter(int k, int j, int i) {
        return (char) (cells[(i * ny + j) * nx + k] & 0xFF);
    }

    /** One aisle as pattern strings, bottom row first. */
    public String[] aisle(int i) {
        String[] rows = new String[ny];
        for (int j = 0; j < ny; j++) {
            var row = new StringBuilder(nx);
            for (int k = 0; k < nx; k++) row.append(letter(k, j, i));
            rows[j] = row.toString();
        }
        return rows;
    }

    /**
     * World position of a cell, as GTCEu lays the pattern out for a controller facing {@code front} (horizontal,
     * upwards facing UP, not flipped): left, up and front from the controller.
     */
    public BlockPos world(BlockPos controller, Direction front, int k, int j, int i) {
        Direction left = front.getCounterClockWise();
        int dk = k - ck, dj = j - cj, di = i - ci;
        return controller.offset(left.getStepX() * dk + front.getStepX() * di, dj,
                left.getStepZ() * dk + front.getStepZ() * di);
    }

    /**
     * Grid axes in world space, as columns of a 3x3 matrix (row-major): grid x goes left, y up and z to the front.
     */
    public static float[] axes(Direction front) {
        Direction left = front.getCounterClockWise();
        return new float[] {
                left.getStepX(), 0, front.getStepX(),
                0, 1, 0,
                left.getStepZ(), 0, front.getStepZ() };
    }
}
