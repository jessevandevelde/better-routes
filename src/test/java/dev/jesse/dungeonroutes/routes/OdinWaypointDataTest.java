package dev.jesse.dungeonroutes.routes;

import com.google.gson.Gson;
import dev.jesse.dungeonroutes.rooms.RoomMetadata;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OdinWaypointDataTest {
    @Test
    void packagedDataKeepsEveryImportedRoomAndWaypoint() throws Exception {
        var waypointStream = OdinWaypointDataTest.class.getResourceAsStream("/assets/dungeonroutes/data/odin-waypoints.json");
        var metadataStream = OdinWaypointDataTest.class.getResourceAsStream("/assets/dungeonroutes/data/room-metadata.json");
        assertNotNull(waypointStream);
        assertNotNull(metadataStream);

        var routes = OdinWaypointImporter.load(new InputStreamReader(waypointStream, StandardCharsets.UTF_8));
        RoomMetadata[] metadata = new Gson().fromJson(
            new InputStreamReader(metadataStream, StandardCharsets.UTF_8), RoomMetadata[].class);

        assertEquals(142, routes.size());
        assertEquals(308, routes.stream().mapToInt(route -> route.waypoints().size()).sum());
        assertEquals(142, metadata.length);
        Set<String> routeRooms = routes.stream().map(SecretRoute::roomId).collect(Collectors.toSet());
        Set<String> metadataRooms = java.util.Arrays.stream(metadata).map(RoomMetadata::roomId).collect(Collectors.toSet());
        assertEquals(routeRooms, metadataRooms);
    }
}
