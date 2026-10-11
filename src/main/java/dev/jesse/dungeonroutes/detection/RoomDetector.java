package dev.jesse.dungeonroutes.detection;

import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.model.RoomRotation;
import dev.jesse.dungeonroutes.rooms.CoordinateTransformer;
import dev.jesse.dungeonroutes.rooms.RoomDatabase;
import dev.jesse.dungeonroutes.rooms.RoomDefinition;
import dev.jesse.dungeonroutes.rooms.RoomMatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RoomDetector {
    private static final int CANDIDATES_PER_TICK = 24;
    private final RoomDatabase database;
    private List<FingerprintMatcher.Candidate> candidates = List.of();
    private final List<RoomMatch> scored = new ArrayList<>();
    private final Map<Long, String> blockCache = new HashMap<>();
    private int candidateIndex;
    private int cellX = Integer.MIN_VALUE;
    private int cellZ = Integer.MIN_VALUE;
    private RoomMatch current;
    private String pendingKey;
    private int pendingConfirmations;
    private double lastConfidence;

    public RoomDetector(RoomDatabase database) { this.database = database; }

    public void tick(Minecraft client, boolean inDungeon) {
        if (!inDungeon || client.level == null || client.player == null || database.rooms().isEmpty()) {
            reset();
            return;
        }
        if (current != null && current.contains(client.player.getX(), client.player.getY(), client.player.getZ())) return;

        int nextCellX = RoomGrid.corner(client.player.getX());
        int nextCellZ = RoomGrid.corner(client.player.getZ());
        if (nextCellX != cellX || nextCellZ != cellZ || candidates.isEmpty()) {
            cellX = nextCellX;
            cellZ = nextCellZ;
            current = null;
            beginPass(client);
        }

        ClientLevel level = client.level;
        int end = Math.min(candidateIndex + CANDIDATES_PER_TICK, candidates.size());
        for (; candidateIndex < end; candidateIndex++) {
            scored.add(FingerprintMatcher.score(candidates.get(candidateIndex), (x, y, z) -> blockAtCached(level, x, y, z)));
        }
        if (candidateIndex >= candidates.size()) finishPass(client);
    }

    private void beginPass(Minecraft client) {
        List<FingerprintMatcher.Candidate> next = new ArrayList<>();
        int playerY = (int) Math.floor(client.player.getY());
        for (RoomDefinition room : database.rooms()) {
            int originY = playerY - room.floorY();
            for (RoomRotation rotation : RoomRotation.values()) {
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        Int3 origin = new Int3(cellX + dx * RoomGrid.CELL, originY, cellZ + dz * RoomGrid.CELL);
                        Int3 local = CoordinateTransformer.worldToLocal(
                            new Int3((int) Math.floor(client.player.getX()), playerY, (int) Math.floor(client.player.getZ())), origin, rotation);
                        if (room.containsLocal(local.x(), local.z())) {
                            next.add(new FingerprintMatcher.Candidate(room, origin, rotation));
                        }
                    }
                }
            }
        }
        candidates = next;
        scored.clear();
        blockCache.clear();
        candidateIndex = 0;
    }

    private void finishPass(Minecraft client) {
        List<RoomMatch> ranked = scored.stream()
            .filter(m -> m.checked() >= Math.min(8, m.room().fingerprint().size()))
            .sorted(Comparator.comparingDouble(RoomMatch::confidence).reversed()).toList();
        RoomMatch winner = ranked.isEmpty() ? null : ranked.getFirst();
        if (winner != null) {
            lastConfidence = winner.confidence();
            boolean unique = ranked.size() == 1 || winner.confidence() - ranked.get(1).confidence() >= FingerprintMatcher.DEFAULT_UNIQUENESS_MARGIN;
            if (!unique || winner.confidence() < winner.room().matchThreshold()) winner = null;
        } else lastConfidence = 0;

        String key = winner == null ? null : winner.room().roomId() + '|' + winner.origin() + '|' + winner.rotation();
        if (key != null && key.equals(pendingKey)) pendingConfirmations++;
        else { pendingKey = key; pendingConfirmations = key == null ? 0 : 1; }
        if (winner != null && pendingConfirmations >= 2) current = winner;
        if (current == null) beginPass(client);
    }

    private String blockAt(ClientLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.hasChunkAt(pos)) return null;
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
    }

    private String blockAtCached(ClientLevel level, int x, int y, int z) {
        long key = BlockPos.asLong(x, y, z);
        if (blockCache.containsKey(key)) return blockCache.get(key);
        String value = blockAt(level, x, y, z);
        if (value != null) blockCache.put(key, value);
        return value;
    }

    public RoomMatch current() { return current; }
    public double lastConfidence() { return lastConfidence; }
    public int candidatesRemaining() { return Math.max(0, candidates.size() - candidateIndex); }

    public void reset() {
        current = null;
        candidates = List.of();
        scored.clear();
        blockCache.clear();
        candidateIndex = 0;
        cellX = cellZ = Integer.MIN_VALUE;
        pendingKey = null;
        pendingConfirmations = 0;
        lastConfidence = 0;
    }
}
