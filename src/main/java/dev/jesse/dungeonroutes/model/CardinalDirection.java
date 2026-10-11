package dev.jesse.dungeonroutes.model;

import java.util.Locale;

public enum CardinalDirection {
    NORTH, EAST, SOUTH, WEST;

    public static CardinalDirection parse(String value) {
        return value == null ? null : valueOf(value.toUpperCase(Locale.ROOT));
    }

    public CardinalDirection rotateClockwise(int quarterTurns) {
        return values()[Math.floorMod(ordinal() + quarterTurns, 4)];
    }
}
