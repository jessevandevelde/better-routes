package dev.jesse.dungeonroutes.routes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Imports Odin's room -> waypoint-list format without assigning unverified secret semantics. */
public final class OdinWaypointImporter {
    private static final Gson GSON = new Gson();

    private OdinWaypointImporter() {}

    public static List<SecretRoute> load(Reader reader) {
        JsonObject root = GSON.fromJson(reader, JsonObject.class);
        List<SecretRoute> result = new ArrayList<>();
        if (root == null) return result;
        for (Map.Entry<String, JsonElement> room : root.entrySet()) {
            if (!room.getValue().isJsonArray()) continue;
            List<RouteWaypoint> waypoints = new ArrayList<>();
            for (JsonElement element : room.getValue().getAsJsonArray()) {
                if (!element.isJsonObject()) continue;
                JsonObject position = element.getAsJsonObject().getAsJsonObject("blockPos");
                if (position == null || !hasCoordinates(position)) continue;
                waypoints.add(new RouteWaypoint(
                    position.get("field_11175").getAsInt(),
                    position.get("field_11174").getAsInt(),
                    position.get("field_11173").getAsInt(),
                    "odin_waypoint",
                    "manual"));
            }
            result.add(new SecretRoute(room.getKey(), null, "default", waypoints, true));
        }
        return result;
    }

    private static boolean hasCoordinates(JsonObject position) {
        return position.has("field_11175") && position.has("field_11174") && position.has("field_11173");
    }
}
