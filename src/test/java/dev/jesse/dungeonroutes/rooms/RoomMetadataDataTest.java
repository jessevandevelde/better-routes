package dev.jesse.dungeonroutes.rooms;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomMetadataDataTest {
    @Test
    void packagedMetadataKeepsOnlyTheImportedRoomNames() {
        var stream = RoomMetadataDataTest.class.getResourceAsStream("/assets/dungeonroutes/data/room-metadata.json");
        assertNotNull(stream);
        RoomMetadata[] metadata = new Gson().fromJson(
            new InputStreamReader(stream, StandardCharsets.UTF_8), RoomMetadata[].class);

        assertEquals(142, metadata.length);
        assertEquals(142, Arrays.stream(metadata).map(RoomMetadata::roomId).distinct().count());
        assertTrue(Arrays.stream(metadata).allMatch(room -> room.possibleDoors().isEmpty()));
    }
}
