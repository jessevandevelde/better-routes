package dev.jesse.dungeonroutes.rendering;

import dev.jesse.dungeonroutes.ClientState;
import dev.jesse.dungeonroutes.config.ConfigManager;
import dev.jesse.dungeonroutes.rooms.RoomMatch;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class DebugHud {
    private final ClientState state;
    public DebugHud(ClientState state) { this.state = state; }

    public void init() { HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("dungeonroutes", "debug"), this::render); }

    private void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!ConfigManager.get().debug) return;
        Minecraft client = Minecraft.getInstance();
        RoomMatch room = state.room();
        List<String> lines = new ArrayList<>();
        lines.add("Dungeon Routes");
        lines.add("dungeon: " + state.dungeonDetector().inDungeon());
        lines.add("room: " + (room == null ? "unknown" : room.room().roomId()));
        lines.add("origin: " + (room == null ? "-" : room.origin()));
        lines.add("rotation: " + (room == null ? "-" : room.rotation()));
        lines.add("entrance: " + (state.entrance() == null ? "fallback" : state.entrance()));
        lines.add("route: " + (state.progress().route() == null ? "none" : state.progress().route().capability()));
        lines.add("waypoint: " + (state.progress().route() == null ? "-" : (state.progress().index() + 1) + "/" + state.progress().route().waypoints().size()));
        lines.add(String.format("confidence: %.1f%% (%d candidates left)", state.roomDetector().lastConfidence() * 100, state.roomDetector().candidatesRemaining()));
        int y = 6;
        for (String line : lines) { graphics.text(client.font, line, 6, y, 0xFFFFFFFF, true); y += 10; }
    }
}
