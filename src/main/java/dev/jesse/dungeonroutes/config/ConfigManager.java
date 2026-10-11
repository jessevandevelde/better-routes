package dev.jesse.dungeonroutes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.jesse.dungeonroutes.DungeonRoutesClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path ROOT = FabricLoader.getInstance().getConfigDir().resolve("dungeonroutes");
    private static final Path FILE = ROOT.resolve("config.json");
    private static ModConfig config = new ModConfig();

    private ConfigManager() {}

    public static void load() {
        try {
            Files.createDirectories(ROOT.resolve("rooms"));
            Files.createDirectories(ROOT.resolve("routes"));
            copyBundledIfMissing("/assets/dungeonroutes/data/room-metadata.json", ROOT.resolve("room-metadata.json"));
            if (Files.exists(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                    ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                    if (loaded != null) config = loaded;
                }
            } else save();
        } catch (Exception e) {
            DungeonRoutesClient.LOG.error("Could not load configuration", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(ROOT);
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) { GSON.toJson(config, writer); }
        } catch (Exception e) {
            DungeonRoutesClient.LOG.error("Could not save configuration", e);
        }
    }

    public static ModConfig get() { return config; }
    public static Path root() { return ROOT; }

    private static void copyBundledIfMissing(String resource, Path destination) throws Exception {
        if (Files.exists(destination)) return;
        try (InputStream input = ConfigManager.class.getResourceAsStream(resource)) {
            if (input != null) Files.copy(input, destination);
        }
    }
}
