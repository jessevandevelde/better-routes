package dev.jesse.dungeonroutes;

import dev.jesse.dungeonroutes.config.ConfigManager;
import dev.jesse.dungeonroutes.detection.DungeonDetector;
import dev.jesse.dungeonroutes.detection.EntranceDetector;
import dev.jesse.dungeonroutes.detection.RoomDetector;
import dev.jesse.dungeonroutes.model.CardinalDirection;
import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.rooms.CoordinateTransformer;
import dev.jesse.dungeonroutes.rooms.RoomDatabase;
import dev.jesse.dungeonroutes.rooms.RoomMatch;
import dev.jesse.dungeonroutes.routes.RouteProgressTracker;
import dev.jesse.dungeonroutes.routes.RouteRepository;
import dev.jesse.dungeonroutes.routes.RouteWaypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ClientState {
    private record Position(double x, double y, double z) {}

    private final DungeonDetector dungeonDetector = new DungeonDetector();
    private final RoomDatabase roomDatabase = new RoomDatabase();
    private final RouteRepository routes = new RouteRepository();
    private final RoomDetector roomDetector = new RoomDetector(roomDatabase);
    private final RouteProgressTracker progress = new RouteProgressTracker();
    private final Deque<Position> movement = new ArrayDeque<>();
    private Object levelIdentity;
    private RoomMatch observedRoom;
    private CardinalDirection entrance;
    private String blockBaseline;

    public void reload() {
        roomDatabase.reload();
        routes.reload();
        roomDetector.reset();
        observedRoom = null;
        entrance = null;
        progress.clear();
    }

    public void tick(Minecraft client) {
        if (client.level != levelIdentity) {
            levelIdentity = client.level;
            resetSession();
        }
        dungeonDetector.tick(client);
        if (!ConfigManager.get().enabled) return;
        roomDetector.tick(client, dungeonDetector.inDungeon());
        if (client.player == null) return;
        movement.addLast(new Position(client.player.getX(), client.player.getY(), client.player.getZ()));
        while (movement.size() > 80) movement.removeFirst();

        RoomMatch room = roomDetector.current();
        if (room != observedRoom) onRoomChanged(room);
        checkBlockChange(client);
    }

    private void onRoomChanged(RoomMatch room) {
        observedRoom = room;
        entrance = null;
        progress.clear();
        blockBaseline = null;
        if (room == null) return;
        Position[] history = movement.toArray(Position[]::new);
        for (int i = history.length - 2; i >= 0; i--) {
            Position prior = history[i];
            if (!room.contains(prior.x, prior.y, prior.z)) {
                Position inside = history[Math.min(i + 1, history.length - 1)];
                entrance = EntranceDetector.detect(room, prior.x, prior.y, prior.z, inside.x, inside.y, inside.z).orElse(null);
                break;
            }
        }
        routes.manager().select(room.room().roomId(), entrance, ConfigManager.get().capability).ifPresent(progress::start);
    }

    public void recordInteraction(BlockPos worldPosition) {
        if (!ConfigManager.get().automaticProgression || observedRoom == null) return;
        Int3 local = CoordinateTransformer.worldToLocal(
            new Int3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()), observedRoom.origin(), observedRoom.rotation());
        if (progress.recordInteraction(local)) blockBaseline = null;
    }

    private void checkBlockChange(Minecraft client) {
        if (!ConfigManager.get().automaticProgression || client.level == null || client.player == null || observedRoom == null) return;
        RouteWaypoint waypoint = progress.current();
        if (waypoint == null || !"block_change".equalsIgnoreCase(waypoint.completion())) { blockBaseline = null; return; }
        Int3 world = CoordinateTransformer.localToWorld(waypoint.position(), observedRoom.origin(), observedRoom.rotation());
        double dx = world.x() + 0.5 - client.player.getX();
        double dy = world.y() + 0.5 - client.player.getY();
        double dz = world.z() + 0.5 - client.player.getZ();
        if (dx * dx + dy * dy + dz * dz > 36) return;
        String block = BuiltInRegistries.BLOCK.getKey(client.level.getBlockState(new BlockPos(world.x(), world.y(), world.z())).getBlock()).toString();
        if (blockBaseline == null) blockBaseline = block;
        else if (!blockBaseline.equals(block)) { progress.next(); blockBaseline = null; }
    }

    public void resetSession() {
        dungeonDetector.reset();
        roomDetector.reset();
        progress.clear();
        movement.clear();
        observedRoom = null;
        entrance = null;
        blockBaseline = null;
    }

    public DungeonDetector dungeonDetector() { return dungeonDetector; }
    public RoomDetector roomDetector() { return roomDetector; }
    public RoomMatch room() { return observedRoom; }
    public CardinalDirection entrance() { return entrance; }
    public RouteProgressTracker progress() { return progress; }
}
