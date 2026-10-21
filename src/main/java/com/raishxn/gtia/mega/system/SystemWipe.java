package com.raishxn.gtia.mega.system;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerAboutToStartEvent;

import com.raishxn.gtia.GTIAMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Erasing a disintegrated star system's terrain (GTIA-D-330). Region files cannot be deleted while the server runs,
 * so the slot is written to {@code gtia_wipe.txt} in the world folder and its dimension folder is removed at the next
 * start, before any level loads. Making a new star in the slot first cancels the wipe.
 */
public final class SystemWipe {

    private static final String FILE = "gtia_wipe.txt";

    private SystemWipe() {}

    private static Path file(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve(FILE);
    }

    private static TreeSet<Integer> read(Path file) {
        var out = new TreeSet<Integer>();
        try {
            if (Files.exists(file))
                for (String line : Files.readAllLines(file)) if (!line.isBlank()) out.add(Integer.parseInt(line.trim()));
        } catch (IOException | NumberFormatException e) {
            GTIAMod.LOGGER.warn("Unreadable {}", file, e);
        }
        return out;
    }

    private static void write(Path file, TreeSet<Integer> slots) {
        try {
            if (slots.isEmpty()) Files.deleteIfExists(file);
            else Files.write(file, slots.stream().map(String::valueOf).toList());
        } catch (IOException e) {
            GTIAMod.LOGGER.warn("Could not write {}", file, e);
        }
    }

    public static void request(MinecraftServer server, int slot) {
        var f = file(server);
        var slots = read(f);
        slots.add(slot);
        write(f, slots);
    }

    public static void cancel(MinecraftServer server, int slot) {
        var f = file(server);
        var slots = read(f);
        if (slots.remove(slot)) write(f, slots);
    }

    public static boolean pending(MinecraftServer server, int slot) {
        return read(file(server)).contains(slot);
    }

    /** At start, before the levels load: removes the folders of the slots marked for wiping. */
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        var server = event.getServer();
        var f = file(server);
        var slots = read(f);
        if (slots.isEmpty()) return;
        Path root = server.getWorldPath(LevelResource.ROOT);
        for (int slot : slots) {
            Path dir = DimensionType.getStorageFolder(StarSystems.slot(slot), root);
            try {
                if (Files.isDirectory(dir))
                    try (var walk = Files.walk(dir)) {
                        walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
                    }
                GTIAMod.LOGGER.info("Disintegrated system {} wiped: {}", slot + 1, dir);
            } catch (IOException e) {
                GTIAMod.LOGGER.warn("Could not wipe {}", dir, e);
            }
        }
        write(f, new TreeSet<>());
    }
}
