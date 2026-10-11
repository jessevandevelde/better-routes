package dev.jesse.dungeonroutes;

import dev.jesse.dungeonroutes.commands.DungeonRoutesCommand;
import dev.jesse.dungeonroutes.config.ConfigManager;
import dev.jesse.dungeonroutes.rendering.DebugHud;
import dev.jesse.dungeonroutes.rendering.RouteRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DungeonRoutesClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("dungeonroutes");
    private static final ClientState STATE = new ClientState();

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        STATE.reload();
        ClientTickEvents.END_CLIENT_TICK.register(STATE::tick);
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide()) STATE.recordInteraction(hit.getBlockPos());
            return InteractionResult.PASS;
        });
        new RouteRenderer(STATE).init();
        new DebugHud(STATE).init();
        DungeonRoutesCommand.init(STATE);
        KeyBindings.init(STATE);
        LOG.info("Dungeon Secret Routes initialized");
    }
}
