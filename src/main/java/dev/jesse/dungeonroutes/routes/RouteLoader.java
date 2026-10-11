package dev.jesse.dungeonroutes.routes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jesse.dungeonroutes.model.CardinalDirection;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RouteLoader {
    private static final Gson GSON = new Gson();
    private RouteLoader() {}

    public static List<SecretRoute> load(Reader reader) {
        JsonObject root = GSON.fromJson(reader, JsonObject.class);
        if (root == null || !root.has("roomId")) throw new IllegalArgumentException("route file needs roomId");
        String roomId = root.get("roomId").getAsString();
        String capability = root.has("capability") ? root.get("capability").getAsString() : "default";
        List<SecretRoute> result = new ArrayList<>();
        JsonObject routes = root.getAsJsonObject("routes");
        if (routes != null) {
            for (Map.Entry<String, JsonElement> entry : routes.entrySet()) {
                CardinalDirection direction = CardinalDirection.parse(entry.getKey());
                RouteWaypoint[] points = GSON.fromJson(entry.getValue(), RouteWaypoint[].class);
                result.add(new SecretRoute(roomId, direction, capability, List.of(points), false));
            }
        }
        if (root.has("fallback")) {
            RouteWaypoint[] points = GSON.fromJson(root.get("fallback"), RouteWaypoint[].class);
            result.add(new SecretRoute(roomId, null, capability, List.of(points), true));
        }
        return result;
    }
}
