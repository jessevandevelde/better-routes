package dev.jesse.dungeonroutes.routes;

import dev.jesse.dungeonroutes.model.CardinalDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RouteManager {
    private final Map<String, List<SecretRoute>> byRoom = new HashMap<>();

    public void replaceAll(List<SecretRoute> routes) {
        byRoom.clear();
        for (SecretRoute route : routes) byRoom.computeIfAbsent(route.roomId(), ignored -> new ArrayList<>()).add(route);
    }

    public Optional<SecretRoute> select(String roomId, CardinalDirection entrance, String capability) {
        List<SecretRoute> routes = byRoom.getOrDefault(roomId, List.of());
        String wantedCapability = capability == null ? "default" : capability;
        return routes.stream().filter(r -> r.entrance() == entrance && r.capability().equals(wantedCapability)).findFirst()
            .or(() -> routes.stream().filter(r -> r.entrance() == entrance && r.capability().equals("default")).findFirst())
            .or(() -> routes.stream().filter(r -> r.fallback() && r.capability().equals(wantedCapability)).findFirst())
            .or(() -> routes.stream().filter(r -> r.fallback() && r.capability().equals("default")).findFirst());
    }

    public int roomCount() { return byRoom.size(); }
}
