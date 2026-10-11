package dev.jesse.dungeonroutes.routes;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OdinWaypointImporterTest {
    @Test
    void preservesRoomsAndConvertsObfuscatedBlockPositions() {
        String json = """
            {
              "Temple": [{"blockPos":{"field_11175":19,"field_11174":77,"field_11173":26},"color":"#2234A875"}],
              "Empty Room": []
            }
            """;
        var routes = OdinWaypointImporter.load(new StringReader(json));
        assertEquals(2, routes.size());
        assertEquals("Temple", routes.getFirst().roomId());
        assertTrue(routes.getFirst().fallback());
        assertEquals(new RouteWaypoint(19, 77, 26, "odin_waypoint", "manual"), routes.getFirst().waypoints().getFirst());
        assertTrue(routes.get(1).waypoints().isEmpty());
    }
}
