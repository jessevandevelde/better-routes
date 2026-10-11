package dev.jesse.dungeonroutes.model;

public enum RoomRotation {
    NONE(0), CLOCKWISE_90(1), CLOCKWISE_180(2), CLOCKWISE_270(3);

    private final int quarterTurns;

    RoomRotation(int quarterTurns) { this.quarterTurns = quarterTurns; }

    public int quarterTurns() { return quarterTurns; }
}
