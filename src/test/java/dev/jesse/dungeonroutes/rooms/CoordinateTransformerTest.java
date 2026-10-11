package dev.jesse.dungeonroutes.rooms;

import dev.jesse.dungeonroutes.model.CardinalDirection;
import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.model.RoomRotation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinateTransformerTest {
    @Test
    void allRotationsRoundTrip() {
        Int3 origin = new Int3(120, 60, -45);
        Int3 local = new Int3(7, 9, 13);
        for (RoomRotation rotation : RoomRotation.values()) {
            Int3 world = CoordinateTransformer.localToWorld(local, origin, rotation);
            assertEquals(local, CoordinateTransformer.worldToLocal(world, origin, rotation));
        }
    }

    @Test
    void directionsRotateWithRoom() {
        assertEquals(CardinalDirection.EAST, CoordinateTransformer.localToWorld(CardinalDirection.NORTH, RoomRotation.CLOCKWISE_90));
        assertEquals(CardinalDirection.NORTH, CoordinateTransformer.worldToLocal(CardinalDirection.EAST, RoomRotation.CLOCKWISE_90));
    }
}
