package dev.jesse.dungeonroutes.rooms;

import dev.jesse.dungeonroutes.model.CardinalDirection;
import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.model.RoomRotation;

public final class CoordinateTransformer {
    private CoordinateTransformer() {}

    /** The origin is the physical corner about which the local room was rotated. */
    public static Int3 localToWorld(Int3 local, Int3 origin, RoomRotation rotation) {
        return switch (rotation) {
            case NONE -> new Int3(origin.x() + local.x(), origin.y() + local.y(), origin.z() + local.z());
            case CLOCKWISE_90 -> new Int3(origin.x() - local.z(), origin.y() + local.y(), origin.z() + local.x());
            case CLOCKWISE_180 -> new Int3(origin.x() - local.x(), origin.y() + local.y(), origin.z() - local.z());
            case CLOCKWISE_270 -> new Int3(origin.x() + local.z(), origin.y() + local.y(), origin.z() - local.x());
        };
    }

    public static Int3 worldToLocal(Int3 world, Int3 origin, RoomRotation rotation) {
        int dx = world.x() - origin.x();
        int dy = world.y() - origin.y();
        int dz = world.z() - origin.z();
        return switch (rotation) {
            case NONE -> new Int3(dx, dy, dz);
            case CLOCKWISE_90 -> new Int3(dz, dy, -dx);
            case CLOCKWISE_180 -> new Int3(-dx, dy, -dz);
            case CLOCKWISE_270 -> new Int3(-dz, dy, dx);
        };
    }

    public static CardinalDirection localToWorld(CardinalDirection local, RoomRotation rotation) {
        return local.rotateClockwise(rotation.quarterTurns());
    }

    public static CardinalDirection worldToLocal(CardinalDirection world, RoomRotation rotation) {
        return world.rotateClockwise(-rotation.quarterTurns());
    }
}
