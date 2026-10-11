package dev.jesse.dungeonroutes.rooms;

import dev.jesse.dungeonroutes.model.CardinalDirection;
import dev.jesse.dungeonroutes.model.Int3;

public record PossibleDoor(
    String id,
    String direction,
    int x,
    int y,
    int z,
    double horizontalTolerance,
    double verticalTolerance
) {
    public PossibleDoor {
        id = id == null ? "" : id;
        horizontalTolerance = horizontalTolerance <= 0 ? 3.0 : horizontalTolerance;
        verticalTolerance = verticalTolerance <= 0 ? 4.0 : verticalTolerance;
    }

    public PossibleDoor(String id, String direction, int x, int y, int z) {
        this(id, direction, x, y, z, 3.0, 4.0);
    }

    public CardinalDirection parsedDirection() { return CardinalDirection.parse(direction); }

    public boolean matches(CardinalDirection crossingDirection, Int3 crossing) {
        if (parsedDirection() != crossingDirection) return false;
        double horizontalDistance = switch (crossingDirection) {
            case NORTH, SOUTH -> Math.abs(crossing.x() - x);
            case EAST, WEST -> Math.abs(crossing.z() - z);
        };
        return horizontalDistance <= horizontalTolerance && Math.abs(crossing.y() - y) <= verticalTolerance;
    }
}
