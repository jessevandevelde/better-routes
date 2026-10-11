package dev.jesse.dungeonroutes.detection;

import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.model.RoomRotation;
import dev.jesse.dungeonroutes.rooms.CoordinateTransformer;
import dev.jesse.dungeonroutes.rooms.FingerprintSample;
import dev.jesse.dungeonroutes.rooms.RoomDefinition;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FingerprintMatcherTest {
    private static final List<FingerprintSample> SAMPLES = List.of(
        new FingerprintSample(1,1,1,"a"), new FingerprintSample(3,1,2,"b"),
        new FingerprintSample(5,2,4,"c"), new FingerprintSample(7,3,8,"d"),
        new FingerprintSample(9,4,2,"e"), new FingerprintSample(11,5,6,"f"),
        new FingerprintSample(13,2,10,"g"), new FingerprintSample(15,1,12,"h")
    );
    private static final RoomDefinition ROOM = new RoomDefinition("unit", 31,31,20,1,0.90,SAMPLES, Set.of());

    @Test
    void recognizesEquivalentRoomAtEveryPositionAndRotation() {
        for (RoomRotation rotation : RoomRotation.values()) {
            Int3 origin = new Int3(100 + rotation.ordinal() * 50, 70, -80);
            Map<Int3, String> world = new HashMap<>();
            for (FingerprintSample sample : SAMPLES) world.put(CoordinateTransformer.localToWorld(sample.position(), origin, rotation), sample.block());
            var match = FingerprintMatcher.score(new FingerprintMatcher.Candidate(ROOM, origin, rotation),
                (x,y,z) -> world.getOrDefault(new Int3(x,y,z), "minecraft:air"));
            assertEquals(1.0, match.confidence());
            assertEquals(ROOM.roomId(), match.room().roomId());
        }
    }

    @Test
    void rejectsUnknownRoom() {
        var candidate = new FingerprintMatcher.Candidate(ROOM, new Int3(0,0,0), RoomRotation.NONE);
        assertTrue(FingerprintMatcher.bestMatch(List.of(candidate), (x,y,z) -> "wrong").isEmpty());
    }

    @Test
    void ambiguityFailsClosed() {
        var a = new FingerprintMatcher.Candidate(ROOM, new Int3(0,0,0), RoomRotation.NONE);
        var b = new FingerprintMatcher.Candidate(ROOM, new Int3(0,0,0), RoomRotation.NONE);
        Map<Int3,String> world = new HashMap<>();
        for (FingerprintSample sample : SAMPLES) world.put(sample.position(), sample.block());
        assertTrue(FingerprintMatcher.bestMatch(List.of(a,b), (x,y,z) -> world.get(new Int3(x,y,z))).isEmpty());
    }
}
