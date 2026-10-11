package dev.jesse.dungeonroutes.detection;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

import java.util.Locale;

public final class DungeonDetector {
    private boolean inDungeon;
    private int ticks;

    public void tick(Minecraft client) {
        if (++ticks % 20 != 0) return;
        inDungeon = detect(client);
    }

    private boolean detect(Minecraft client) {
        if (client.level == null || client.player == null) return false;
        String ip = client.getCurrentServer() == null ? "" : client.getCurrentServer().ip.toLowerCase(Locale.ROOT);
        String brand = client.player.connection == null || client.player.connection.serverBrand() == null
            ? "" : client.player.connection.serverBrand();
        if (!ip.contains("hypixel.net") && !ip.contains("hypixel.io") && !brand.contains("Hypixel")) return false;

        Scoreboard scoreboard = client.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return false;
        if (containsDungeonText(objective.getDisplayName().getString())) return true;
        for (ScoreHolder holder : scoreboard.getTrackedPlayers()) {
            if (!scoreboard.listPlayerScores(holder).containsKey(objective)) continue;
            PlayerTeam team = scoreboard.getPlayersTeam(holder.getScoreboardName());
            if (team == null) continue;
            String line = team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString();
            if (containsDungeonText(line)) return true;
        }
        return false;
    }

    private boolean containsDungeonText(String value) {
        String plain = ChatFormatting.stripFormatting(value);
        return plain != null && (plain.contains("The Catacombs") || plain.contains("Master Mode Catacombs"));
    }

    public boolean inDungeon() { return inDungeon; }
    public void reset() { inDungeon = false; ticks = 0; }
}
