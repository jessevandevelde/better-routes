package dev.jesse.dungeonroutes.rooms;

import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.model.RoomRotation;

public record RoomMatch(RoomDefinition room, Int3 origin, RoomRotation rotation, double confidence, int matched, int checked) {
    public boolean contains(double worldX, double worldY, double worldZ) {
        Int3 local = CoordinateTransformer.worldToLocal(
            new Int3((int) Math.floor(worldX), (int) Math.floor(worldY), (int) Math.floor(worldZ)), origin, rotation);
        return room.containsLocal(local.x(), local.z())
            && local.y() >= -4 && local.y() < room.height() + 4;
    }
}
