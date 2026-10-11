package dev.jesse.dungeonroutes.detection;

import dev.jesse.dungeonroutes.model.CardinalDirection;
import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.model.RoomRotation;
import dev.jesse.dungeonroutes.rooms.RoomDefinition;
import dev.jesse.dungeonroutes.rooms.RoomMatch;
import dev.jesse.dungeonroutes.rooms.PossibleDoor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntranceDetectorTest {
    private static RoomMatch room(RoomRotation rotation) {
        return new RoomMatch(new RoomDefinition("r",31,31,20,1,.8,List.of(
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(0,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(1,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(2,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(3,0,0,"a")), Set.of()),
            new Int3(0,0,0), rotation, 1,4,4);
    }

    @Test
    void detectsAllLocalEntrances() {
        RoomMatch room = room(RoomRotation.NONE);
        assertEquals(CardinalDirection.NORTH, EntranceDetector.detect(room,10,1,-1,10,1,0).orElseThrow());
        assertEquals(CardinalDirection.EAST, EntranceDetector.detect(room,31,1,10,30,1,10).orElseThrow());
        assertEquals(CardinalDirection.SOUTH, EntranceDetector.detect(room,10,1,31,10,1,30).orElseThrow());
        assertEquals(CardinalDirection.WEST, EntranceDetector.detect(room,-1,1,10,0,1,10).orElseThrow());
    }

    @Test
    void rotatedRoomStillReturnsLocalEntrance() {
        RoomMatch room = room(RoomRotation.CLOCKWISE_90);
        assertEquals(CardinalDirection.NORTH, EntranceDetector.detect(room,1,1,10,0,1,10).orElseThrow());
    }

    @Test
    void possibleDoorsAcceptOnlyDeclaredDoorwayPosition() {
        var definition = new RoomDefinition("doors",31,31,20,1,.8,List.of(
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(0,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(1,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(2,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(3,0,0,"a")), Set.of(), List.of(), List.of(
                new PossibleDoor("north_center", "north", 15, 1, 0, 2, 2),
                new PossibleDoor("east_upper", "east", 30, 1, 8, 2, 2)));
        definition.validate();
        RoomMatch room = new RoomMatch(definition, new Int3(0,0,0), RoomRotation.NONE, 1,4,4);

        assertEquals(CardinalDirection.NORTH, EntranceDetector.detect(room,15,1,-1,15,1,0).orElseThrow());
        assertEquals(CardinalDirection.EAST, EntranceDetector.detect(room,31,1,8,30,1,8).orElseThrow());
        assertTrue(EntranceDetector.detect(room,4,1,-1,4,1,0).isEmpty());
        assertTrue(EntranceDetector.detect(room,-1,1,15,0,1,15).isEmpty());
    }

    @Test
    void possibleDoorCoordinatesRotateWithRoom() {
        var definition = new RoomDefinition("rotated_doors",31,31,20,1,.8,List.of(
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(0,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(1,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(2,0,0,"a"),
            new dev.jesse.dungeonroutes.rooms.FingerprintSample(3,0,0,"a")), Set.of(), List.of(),
            List.of(new PossibleDoor("north_center", "north", 15, 1, 0)));
        RoomMatch room = new RoomMatch(definition, new Int3(100,0,100), RoomRotation.CLOCKWISE_90, 1,4,4);

        assertEquals(CardinalDirection.NORTH, EntranceDetector.detect(room,101,1,115,100,1,115).orElseThrow());
    }
}
