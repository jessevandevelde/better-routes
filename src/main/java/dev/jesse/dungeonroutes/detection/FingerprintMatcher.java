package dev.jesse.dungeonroutes.detection;

import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.model.RoomRotation;
import dev.jesse.dungeonroutes.rooms.CoordinateTransformer;
import dev.jesse.dungeonroutes.rooms.FingerprintSample;
import dev.jesse.dungeonroutes.rooms.RoomDefinition;
import dev.jesse.dungeonroutes.rooms.RoomMatch;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class FingerprintMatcher {
    public static final double DEFAULT_UNIQUENESS_MARGIN = 0.06;

    @FunctionalInterface
    public interface BlockLookup { String blockAt(int x, int y, int z); }

    public record Candidate(RoomDefinition room, Int3 origin, RoomRotation rotation) {}

    private FingerprintMatcher() {}

    public static RoomMatch score(Candidate candidate, BlockLookup blocks) {
        int matched = 0;
        int checked = 0;
        for (FingerprintSample sample : candidate.room().fingerprint()) {
            Int3 world = CoordinateTransformer.localToWorld(sample.position(), candidate.origin(), candidate.rotation());
            String actual = blocks.blockAt(world.x(), world.y(), world.z());
            if (actual == null) continue; // unloaded blocks are not evidence either way
            checked++;
            if (sample.block().equals(actual)) matched++;
        }
        double confidence = checked == 0 ? 0 : (double) matched / checked;
        return new RoomMatch(candidate.room(), candidate.origin(), candidate.rotation(), confidence, matched, checked);
    }

    public static Optional<RoomMatch> bestMatch(List<Candidate> candidates, BlockLookup blocks) {
        List<RoomMatch> ranked = candidates.stream()
            .map(candidate -> score(candidate, blocks))
            .filter(match -> match.checked() >= Math.min(8, match.room().fingerprint().size()))
            .sorted(Comparator.comparingDouble(RoomMatch::confidence).reversed())
            .toList();
        if (ranked.isEmpty()) return Optional.empty();
        RoomMatch best = ranked.getFirst();
        if (best.confidence() < best.room().matchThreshold()) return Optional.empty();
        if (ranked.size() > 1 && best.confidence() - ranked.get(1).confidence() < DEFAULT_UNIQUENESS_MARGIN) return Optional.empty();
        return Optional.of(best);
    }
}
