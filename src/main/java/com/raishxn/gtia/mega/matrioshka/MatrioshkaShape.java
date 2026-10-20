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
 * Rings tilted about one hinge axis around the star, after the old Gigastructures Matrioshka; the geometry is our
 * own (design/estudos/matrioshka/gen_rings_pattern.py). The file stays out of git until the author of Gigastructures
 * is asked; without it the machine is not registered.
 *
 * <p>
 * Pattern axes follow {@code FactoryBlockPattern.start()}: char {@code k} goes left, string {@code j} up, aisle
 * {@code i} to the front. Grid x = k, y = j, z = i; the controller faces +z.
 */
public final class MatrioshkaShape {

    public static final String RESOURCE = "gtia_matrioshka/pattern.bin.gz", PARTS = "gtia_matrioshka/parts.json";

    /**
     * One letter of the pattern, from {@code gtia_matrioshka/parts.json}: which ring (or the station) it belongs to,
     * the phase, the block and, for turning rings, the track that moves it. Each ring has several letters (body,
     * rims, ribs, light strips) with different blocks. Phase 1 holds the controller: the fixed polar ring with a
     * station at each end of the hinge axis, and the two inner rings.
     */
    public record Part(char letter, String ring, int phase, String block, @Nullable String track, String kind) {

        public static final char STATION = 'S', HATCH = 'K';

        /** The part of a letter, or null for air, the controller and unknown letters. */
        @Nullable
        public static Part of(char letter) {
            var shape = get();
            return shape == null ? null : shape.parts.get(letter);
        }

        public boolean moving() {
            return track != null;
        }
    }

    /** Parts by letter, in file order. */
    public final java.util.Map<Character, Part> parts = new java.util.LinkedHashMap<>();

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
        try (var in = MatrioshkaShape.class.getClassLoader().getResourceAsStream(PARTS)) {
            if (in == null) throw new IOException("missing " + PARTS);
            var list = com.google.gson.JsonParser.parseReader(
                    new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonArray();
            for (var e : list) {
                var o = e.getAsJsonObject();
                char letter = o.get("letter").getAsString().charAt(0);
                parts.put(letter, new Part(letter, o.get("part").getAsString(), o.get("phase").getAsInt(),
                        o.get("block").getAsString(), o.get("track").isJsonNull() ? null : o.get("track").getAsString(),
                        o.get("kind").getAsString()));
            }
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
