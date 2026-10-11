package dev.jesse.dungeonroutes.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.jesse.dungeonroutes.ClientState;
import dev.jesse.dungeonroutes.config.ConfigManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class DungeonRoutesCommand {
    private DungeonRoutesCommand() {}

    public static void init(ClientState state) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(ClientCommands.literal("dungeonroutes")
            .executes(ctx -> status(ctx, state))
            .then(ClientCommands.literal("status").executes(ctx -> status(ctx, state)))
            .then(ClientCommands.literal("reload").executes(ctx -> { ConfigManager.load(); state.reload(); reply(ctx, "data reloaded"); return 1; }))
            .then(ClientCommands.literal("toggle").executes(ctx -> { ConfigManager.get().renderRoutes = !ConfigManager.get().renderRoutes; ConfigManager.save(); reply(ctx, "rendering " + (ConfigManager.get().renderRoutes ? "enabled" : "disabled")); return 1; }))
            .then(ClientCommands.literal("debug").executes(ctx -> { ConfigManager.get().debug = !ConfigManager.get().debug; ConfigManager.save(); reply(ctx, "debug " + (ConfigManager.get().debug ? "enabled" : "disabled")); return 1; }))
            .then(ClientCommands.literal("next").executes(ctx -> { state.progress().next(); reply(ctx, "advanced waypoint"); return 1; }))
            .then(ClientCommands.literal("previous").executes(ctx -> { state.progress().previous(); reply(ctx, "revisited waypoint"); return 1; }))
            .then(ClientCommands.literal("reset").executes(ctx -> { state.progress().reset(); reply(ctx, "route reset"); return 1; }))));
    }

    private static int status(CommandContext<FabricClientCommandSource> ctx, ClientState state) {
        var room = state.room();
        reply(ctx, "dungeon=" + state.dungeonDetector().inDungeon()
            + ", room=" + (room == null ? "unknown" : room.room().roomId())
            + ", rotation=" + (room == null ? "-" : room.rotation())
            + ", entrance=" + (state.entrance() == null ? "fallback/unknown" : state.entrance())
            + ", waypoint=" + (state.progress().route() == null ? "-" : state.progress().index() + 1));
        return 1;
    }

    private static void reply(CommandContext<FabricClientCommandSource> ctx, String text) {
        ctx.getSource().sendFeedback(Component.literal("[Dungeon Routes] " + text));
    }
}
