package dev.jesse.dungeonroutes.routes;

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

public final class RouteRepository {
    private final RouteManager manager = new RouteManager();

    public void reload() {
        List<SecretRoute> loaded = new ArrayList<>();
        try (InputStream stream = RouteRepository.class.getResourceAsStream("/assets/dungeonroutes/data/example_route.json")) {
            if (stream != null) loaded.addAll(RouteLoader.load(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        } catch (Exception e) {
            DungeonRoutesClient.LOG.error("Could not load bundled route example", e);
        }
        Path directory = ConfigManager.root().resolve("routes");
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { loaded.addAll(RouteLoader.load(reader)); }
                catch (Exception e) { DungeonRoutesClient.LOG.error("Invalid route file {}", path, e); }
            }
        } catch (Exception e) {
            DungeonRoutesClient.LOG.error("Could not enumerate route files", e);
        }
        manager.replaceAll(loaded);
        DungeonRoutesClient.LOG.info("Loaded routes for {} rooms", manager.roomCount());
    }

    public RouteManager manager() { return manager; }
}
