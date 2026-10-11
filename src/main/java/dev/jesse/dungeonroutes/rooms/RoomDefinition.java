package dev.jesse.dungeonroutes.rooms;

import java.util.List;
import java.util.Set;

public record RoomDefinition(
    String roomId,
    int width,
    int length,
    int height,
    int floorY,
    double matchThreshold,
    List<FingerprintSample> fingerprint,
    Set<String> entrances,
    List<RoomCell> cells
) {
    public RoomDefinition {
        fingerprint = fingerprint == null ? List.of() : List.copyOf(fingerprint);
        entrances = entrances == null ? Set.of() : Set.copyOf(entrances);
        cells = cells == null ? List.of() : List.copyOf(cells);
        if (matchThreshold <= 0 || matchThreshold > 1) matchThreshold = 0.88;
    }

    public RoomDefinition(String roomId, int width, int length, int height, int floorY, double matchThreshold,
                          List<FingerprintSample> fingerprint, Set<String> entrances) {
        this(roomId, width, length, height, floorY, matchThreshold, fingerprint, entrances, List.of());
    }

    public boolean containsLocal(int x, int z) {
        if (x < 0 || x >= width || z < 0 || z >= length) return false;
        if (cells.isEmpty()) return true;
        int cellX = Math.floorDiv(x, 32);
        int cellZ = Math.floorDiv(z, 32);
        return cells.contains(new RoomCell(cellX, cellZ));
    }

    public void validate() {
        if (roomId == null || roomId.isBlank()) throw new IllegalArgumentException("roomId is required");
        if (width < 1 || length < 1 || height < 1) throw new IllegalArgumentException(roomId + ": invalid bounds");
        if (fingerprint.size() < 4) throw new IllegalArgumentException(roomId + ": fingerprint needs at least 4 samples");
        for (FingerprintSample sample : fingerprint) {
            if (sample.block() == null || sample.block().isBlank()) throw new IllegalArgumentException(roomId + ": sample block is required");
            if (sample.x() < 0 || sample.x() >= width || sample.z() < 0 || sample.z() >= length || sample.y() < 0 || sample.y() >= height) {
                throw new IllegalArgumentException(roomId + ": sample outside room bounds: " + sample);
            }
        }
    }
}
