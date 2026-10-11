package dev.jesse.dungeonroutes.rooms;

import dev.jesse.dungeonroutes.model.Int3;

public record FingerprintSample(int x, int y, int z, String block) {
    public Int3 position() { return new Int3(x, y, z); }
}
