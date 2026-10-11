package dev.jesse.dungeonroutes.routes;

import dev.jesse.dungeonroutes.model.CardinalDirection;

import java.util.List;

public record SecretRoute(String roomId, CardinalDirection entrance, String capability, List<RouteWaypoint> waypoints, boolean fallback) {
    public SecretRoute {
        waypoints = waypoints == null ? List.of() : List.copyOf(waypoints);
        capability = capability == null ? "default" : capability;
    }
}
