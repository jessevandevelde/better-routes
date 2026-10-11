package dev.jesse.dungeonroutes.rooms;

import java.util.List;
import java.util.Set;

public record RoomMetadata(String roomId, Set<String> entrances, List<PossibleDoor> possibleDoors) {
    public RoomMetadata {
        entrances = entrances == null ? Set.of() : Set.copyOf(entrances);
        possibleDoors = possibleDoors == null ? List.of() : List.copyOf(possibleDoors);
    }
}
