package dev.jesse.dungeonroutes.routes;

import dev.jesse.dungeonroutes.model.Int3;

public record RouteWaypoint(int x, int y, int z, String type, String completion) {
    public RouteWaypoint {
        type = type == null ? "secret" : type;
        completion = completion == null ? "manual" : completion;
    }

    public Int3 position() { return new Int3(x, y, z); }
}
