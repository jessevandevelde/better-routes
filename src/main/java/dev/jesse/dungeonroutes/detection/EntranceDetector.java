package dev.jesse.dungeonroutes.detection;

import dev.jesse.dungeonroutes.model.CardinalDirection;
import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.rooms.CoordinateTransformer;
import dev.jesse.dungeonroutes.rooms.RoomMatch;

import java.util.Optional;

public final class EntranceDetector {
    private EntranceDetector() {}

    public static Optional<CardinalDirection> detect(RoomMatch room, double previousX, double previousY, double previousZ,
                                                      double currentX, double currentY, double currentZ) {
        Int3 previous = CoordinateTransformer.worldToLocal(
            new Int3((int) Math.floor(previousX), (int) Math.floor(previousY), (int) Math.floor(previousZ)),
            room.origin(), room.rotation());
        Int3 current = CoordinateTransformer.worldToLocal(
            new Int3((int) Math.floor(currentX), (int) Math.floor(currentY), (int) Math.floor(currentZ)),
            room.origin(), room.rotation());

        CardinalDirection result = null;
        if (previous.z() < 0 && current.z() >= 0) result = CardinalDirection.NORTH;
        else if (previous.x() >= room.room().width() && current.x() < room.room().width()) result = CardinalDirection.EAST;
        else if (previous.z() >= room.room().length() && current.z() < room.room().length()) result = CardinalDirection.SOUTH;
        else if (previous.x() < 0 && current.x() >= 0) result = CardinalDirection.WEST;
        if (result == null) return Optional.empty();
        if (!room.room().possibleDoors().isEmpty()) {
            CardinalDirection crossingDirection = result;
            boolean matchesDoor = room.room().possibleDoors().stream().anyMatch(door -> door.matches(crossingDirection, current));
            return matchesDoor ? Optional.of(result) : Optional.empty();
        }
        if (!room.room().entrances().isEmpty() && !room.room().entrances().contains(result.name().toLowerCase())) return Optional.empty();
        return Optional.of(result);
    }
}
