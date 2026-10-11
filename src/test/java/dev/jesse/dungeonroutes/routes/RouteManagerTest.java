package dev.jesse.dungeonroutes.routes;

import dev.jesse.dungeonroutes.model.CardinalDirection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteManagerTest {
    private static SecretRoute route(CardinalDirection entrance, String capability, boolean fallback) {
        return new SecretRoute("room", entrance, capability, List.of(new RouteWaypoint(1,2,3,"secret","manual")), fallback);
    }

    @Test
    void selectsExactEntranceAndCapability() {
        RouteManager manager = new RouteManager();
        manager.replaceAll(List.of(route(CardinalDirection.NORTH,"default",false), route(CardinalDirection.NORTH,"etherwarp",false)));
        assertEquals("etherwarp", manager.select("room", CardinalDirection.NORTH, "etherwarp").orElseThrow().capability());
    }

    @Test
    void usesFallbackInsteadOfGuessingEntrance() {
        RouteManager manager = new RouteManager();
        manager.replaceAll(List.of(route(CardinalDirection.NORTH,"default",false), route(null,"default",true)));
        assertTrue(manager.select("room", null, "default").orElseThrow().fallback());
        assertTrue(manager.select("unknown", null, "default").isEmpty());
    }
}
