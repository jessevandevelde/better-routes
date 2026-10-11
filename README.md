# Dungeon Secret Routes

A client-only Fabric mod for Minecraft Java Edition **26.1.2**. It recognizes data-defined Hypixel SkyBlock dungeon rooms, determines their rotation and entry side, selects an entrance-specific secret route, and renders that route in the world.

The implementation is complete, builds, and is unit-tested. The repository intentionally contains **no claimed real-room fingerprints or secret coordinates**. Detection becomes useful in live Catacombs runs after verified room and route JSON is placed in the configuration directories described below.

## Requirements and installation

- Minecraft Java Edition 26.1.2
- Java 25
- Fabric Loader 0.19.2 or newer
- Fabric API for 26.1.2

Build with `./gradlew build` (`gradlew.bat build` on Windows), then copy the JAR from `build/libs/` into the Minecraft instance's `mods` directory. Fabric's 26.1 porting guide confirms Java 25, official Mojang names without a mappings dependency, and the new Loom plugin ID for this game line.

On first launch the mod creates:

```text
config/dungeonroutes/
├── config.json
├── room-metadata.json        # imported room names and possibleDoors overrides
├── rooms/     # one or more room database JSON files
└── routes/    # one route JSON file per room/capability set
```

## Controls and commands

Default controls can be changed in Minecraft's Controls screen:

- `]`: next waypoint
- `[`: previous waypoint
- `\`: reset the current route
- Toggle rendering is unbound by default

Commands:

```text
/dungeonroutes status
/dungeonroutes reload
/dungeonroutes toggle
/dungeonroutes debug
/dungeonroutes next
/dungeonroutes previous
/dungeonroutes reset
```

Debug mode shows dungeon state, room ID, origin, rotation, entrance, selected route, waypoint, confidence, and remaining scan candidates.

## Room database format

Room coordinates use a local origin. `floorY` is the local Y coordinate of the normal walkable floor. `width`, `length`, and `height` are local bounds. `cells` is optional; use it for non-rectangular multi-cell rooms, with each entry naming a 32-block grid cell in local X/Z. If omitted, the rectangular bounds are used.

`possibleDoors` declares the doorway centers that are valid entrances for this room. Coordinates and directions are room-local, so they rotate automatically with the detected room. Multiple doors may use the same direction. `horizontalTolerance` and `verticalTolerance` are optional and default to 3 and 4 blocks. The older `entrances` string list remains supported as a direction-only fallback when `possibleDoors` is absent.

```json
[
  {
    "roomId": "verified_room_id",
    "width": 63,
    "length": 63,
    "height": 40,
    "floorY": 6,
    "matchThreshold": 0.9,
    "possibleDoors": [
      {"id": "north_center", "direction": "north", "x": 15, "y": 6, "z": 0},
      {"id": "east_upper", "direction": "east", "x": 62, "y": 8, "z": 20,
       "horizontalTolerance": 2.0, "verticalTolerance": 3.0}
    ],
    "cells": [{"x": 0, "z": 0}, {"x": 1, "z": 0}, {"x": 0, "z": 1}],
    "fingerprint": [
      {"x": 2, "y": 0, "z": 3, "block": "minecraft:bedrock"},
      {"x": 9, "y": 7, "z": 14, "block": "minecraft:stone_bricks"}
    ]
  }
]
```

Use numerous distinctive, stable blocks distributed across the room. Avoid containers, redstone state, doors, torches, skulls, and blocks that Hypixel changes during a run. A definition needs at least four samples; eight or more loaded samples are required before a live match is accepted. The matcher checks every rotation, requires the per-room confidence threshold, requires a margin over the runner-up, and confirms the same result twice. Ambiguous and unknown rooms fail closed.

Door centers must sit on their declared local wall: north uses `z: 0`, east uses `x: width - 1`, south uses `z: length - 1`, and west uses `x: 0`. When `possibleDoors` is present, a wall crossing that is not close to one of its entries is treated as an unknown entrance and therefore selects only the route fallback.

### Imported room names

The 142 room names from the supplied Odin configuration are retained in `room-metadata.json`. No Odin waypoint coordinates, ordering, colors, or routes are stored or packaged. On first launch the metadata file is copied to the mod configuration directory so it remains user-editable.

`room-metadata.json` contains an entry like this for every imported room:

```json
{
  "roomId": "Temple",
  "possibleDoors": [
    {"id": "north_center", "direction": "north", "x": 15, "y": 68, "z": 0}
  ]
}
```

The generated door lists are empty because the source data does not contain doorway evidence. Add only doorway coordinates you have verified. Metadata is merged onto a matching fingerprint definition from `rooms/`; a room name by itself cannot identify a room and does not create a route.

## Route format

Routes are deliberately independent for each entrance; the mod never assumes that reversing one is optimal. `capability` may identify sets such as `default` or `etherwarp`. If the detected entrance is unavailable, only the explicit `fallback` is considered.

```json
{
  "roomId": "verified_room_id",
  "capability": "default",
  "routes": {
    "north": [
      {"x": 5, "y": 8, "z": 8, "type": "secret", "completion": "interact"},
      {"x": 12, "y": 10, "z": 15, "type": "secret", "completion": "block_change"}
    ],
    "south": [
      {"x": 12, "y": 10, "z": 15, "type": "secret", "completion": "manual"}
    ]
  },
  "fallback": [
    {"x": 5, "y": 8, "z": 8, "type": "secret", "completion": "manual"}
  ]
}
```

Completion evidence:

- `interact`: advances only after a use-block action within three blocks of the waypoint.
- `block_change`: records the target block while the player is nearby and advances only after its state changes.
- `manual`: never advances automatically.

Proximity alone never completes a waypoint. Because client-observed interaction or block changes are still not an authoritative Hypixel secret-award signal, manual controls remain available.

## How detection works

The scoreboard and server identity gate all scanning to Hypixel Catacombs. Player coordinates select the local 32-block dungeon grid neighborhood. A bounded queue incrementally tests only room origins whose declared shape contains the player. Block reads happen on the client thread, at most 24 candidate configurations per tick, and are cached for the current pass. The accepted room is cached until the player leaves its transformed shape. World changes clear every cache and progression state.

Room fingerprints stay in local coordinates. For each candidate rotation, the transformer maps each sample to the candidate world origin and compares the actual registry ID. Route waypoints use the same transform. The movement history supplies a segment crossing from outside to inside the accepted room; that crossing is converted back into room-local coordinates to identify north/east/south/west. If delayed chunk loading makes that unreliable, selection uses the explicit fallback rather than guessing.

Rendering uses Fabric's 26.1 `LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN`, batches all route lines and marker boxes into two buffers, highlights the next waypoint, and optionally draws allocation-light seven-segment waypoint numbers. Rendering is depth-tested to keep the helper conservative.

## Research notes

The implementation was informed by algorithms in these repositories, but contains original Java and no imported room/route database:

- **cryptkit** (GPL-3.0-only): [`RoomMatch.java`](https://github.com/Froggy-Lord/cryptkit/blob/main/src/main/java/dev/froggylord/cryptkit/core/RoomMatch.java) floods the 32-block topology, filters rotation candidates with sparse fingerprints, performs a reverse confidence check, caches an accepted footprint, and confirms matches before committing. [`RoomGrid.java`](https://github.com/Froggy-Lord/cryptkit/blob/main/src/main/java/dev/froggylord/cryptkit/core/RoomGrid.java) documents the grid/corner formula; [`RoomTransform.java`](https://github.com/Froggy-Lord/cryptkit/blob/main/src/main/java/dev/froggylord/cryptkit/core/RoomTransform.java) demonstrates local/world rotation transforms. Its build files also provided a concrete 26.1.2 API reference. No GPL source or data was copied into this MIT project.
- **DungeonRoomsMod** (GPL-3.0): [`RoomDetection.java`](https://github.com/Quantizr/DungeonRoomsMod/blob/3.x/src/main/java/io/github/quantizr/dungeonrooms/dungeons/catacombs/RoomDetection.java) combines map-derived room segments with line-of-sight block samples, progressively eliminates room/rotation candidates, and processes comparison work separately from world reads. [`MapUtils.java`](https://github.com/Quantizr/DungeonRoomsMod/blob/3.x/src/main/java/io/github/quantizr/dungeonrooms/utils/MapUtils.java) calculates multi-cell shapes, possible rotations, physical corners, and local/world transforms. This project adopts the concepts of sparse stable samples and progressive elimination, not its code or data.
- **Skytils Catlas** (AGPL-3.0): [`DungeonScanner.kt`](https://github.com/Skytils/SkytilsMod/blob/1.x/src/main/kotlin/gg/skytils/skytilsmod/features/impl/dungeons/catlas/handlers/DungeonScanner.kt) scans a bounded dungeon grid into room/door tiles, while [`MapUpdater.kt`](https://github.com/Skytils/SkytilsMod/blob/1.x/src/main/kotlin/gg/skytils/skytilsmod/features/impl/dungeons/catlas/handlers/MapUpdater.kt) updates cached map state. The current Skytils default branch is an in-progress modern rewrite; Catlas is in its established `1.x` source. No AGPL code was copied.

## Tests

`gradlew test` covers:

- round-trip local/world transforms for all four rotations;
- local/world entrance direction rotation;
- position- and rotation-independent fingerprint recognition;
- ambiguity and unknown-room rejection;
- all four entrance directions plus a rotated room;
- exact, default-capability, and fallback route selection;
- route JSON parsing.

`gradlew build` runs the tests and packages the client mod.

## Known limitations and fair-play note

- The bundled database is intentionally empty. Live room recognition and routes require independently verified, legally distributable room data.
- The detector assumes Hypixel's established 32-block Catacombs grid and declared room metadata. A layout/protocol change may require data or algorithm updates.
- Interaction and block-change evidence is client-observed, not an authoritative server acknowledgement. Use manual progression if a room uses entities, item pickups, bats, or another secret mechanism.
- This is a visual assistant only: it does not move, click, fight, send gameplay packets, or automate interactions.
- Hypixel policies and enforcement can change. Review the current server rules before use; no mod can promise that use is risk-free.
