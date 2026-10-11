package dev.jesse.dungeonroutes.rooms;

import com.google.gson.Gson;
import dev.jesse.dungeonroutes.DungeonRoutesClient;
import dev.jesse.dungeonroutes.config.ConfigManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RoomDatabase {
    private static final Gson GSON = new Gson();
    private volatile List<RoomDefinition> rooms = List.of();

    public void reload() {
        List<RoomDefinition> loaded = new ArrayList<>();
        try (InputStream stream = RoomDatabase.class.getResourceAsStream("/assets/dungeonroutes/data/rooms.json")) {
            if (stream != null) load(new InputStreamReader(stream, StandardCharsets.UTF_8), loaded);
        } catch (Exception e) {
            DungeonRoutesClient.LOG.error("Could not load bundled room database", e);
        }
        Path directory = ConfigManager.root().resolve("rooms");
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { load(reader, loaded); }
                catch (Exception e) { DungeonRoutesClient.LOG.error("Invalid room file {}", path, e); }
            }
        } catch (Exception e) {
            DungeonRoutesClient.LOG.error("Could not enumerate room files", e);
        }
        rooms = List.copyOf(loaded);
        DungeonRoutesClient.LOG.info("Loaded {} room definitions", rooms.size());
    }

    private void load(Reader reader, List<RoomDefinition> output) {
        RoomDefinition[] definitions = GSON.fromJson(reader, RoomDefinition[].class);
        if (definitions == null) return;
        for (RoomDefinition definition : definitions) {
            definition.validate();
            output.removeIf(existing -> existing.roomId().equals(definition.roomId()));
            output.add(definition);
        }
    }

    public List<RoomDefinition> rooms() { return rooms; }
}
