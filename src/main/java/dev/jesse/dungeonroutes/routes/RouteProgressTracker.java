package dev.jesse.dungeonroutes.routes;

import dev.jesse.dungeonroutes.model.Int3;

public final class RouteProgressTracker {
    private SecretRoute route;
    private int index;

    public void start(SecretRoute next) {
        if (next != route) { route = next; index = 0; }
    }

    public void clear() { route = null; index = 0; }
    public SecretRoute route() { return route; }
    public int index() { return index; }
    public boolean complete() { return route == null || index >= route.waypoints().size(); }
    public RouteWaypoint current() { return complete() ? null : route.waypoints().get(index); }
    public void next() { if (route != null && index < route.waypoints().size()) index++; }
    public void previous() { if (route != null && index > 0) index--; }
    public void reset() { index = 0; }

    public boolean recordInteraction(Int3 localPosition) {
        RouteWaypoint waypoint = current();
        if (waypoint == null || !"interact".equalsIgnoreCase(waypoint.completion())) return false;
        long dx = waypoint.x() - localPosition.x();
        long dy = waypoint.y() - localPosition.y();
        long dz = waypoint.z() - localPosition.z();
        if (dx * dx + dy * dy + dz * dz > 9) return false;
        next();
        return true;
    }
}
