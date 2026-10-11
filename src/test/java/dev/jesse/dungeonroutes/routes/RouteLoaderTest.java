package dev.jesse.dungeonroutes.routes;

import dev.jesse.dungeonroutes.model.CardinalDirection;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class RouteLoaderTest {
    @Test
    void parsesEntranceRoutesAndFallback() {
        String json = """
            {"roomId":"test","routes":{"west":[{"x":1,"y":2,"z":3,"type":"secret","completion":"interact"}]},
             "fallback":[{"x":4,"y":5,"z":6,"type":"secret"}]}
            """;
        var routes = RouteLoader.load(new StringReader(json));
        assertEquals(2, routes.size());
        assertEquals(CardinalDirection.WEST, routes.getFirst().entrance());
        assertEquals("interact", routes.getFirst().waypoints().getFirst().completion());
        assertTrue(routes.get(1).fallback());
    }
}
