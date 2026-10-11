package dev.jesse.dungeonroutes;

import dev.jesse.dungeonroutes.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    private KeyBindings() {}

    public static void init(ClientState state) {
        KeyMapping next = register("next_waypoint", GLFW.GLFW_KEY_RIGHT_BRACKET);
        KeyMapping previous = register("previous_waypoint", GLFW.GLFW_KEY_LEFT_BRACKET);
        KeyMapping reset = register("reset_route", GLFW.GLFW_KEY_BACKSLASH);
        KeyMapping toggle = register("toggle_rendering", GLFW.GLFW_KEY_UNKNOWN);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (next.consumeClick()) state.progress().next();
            while (previous.consumeClick()) state.progress().previous();
            while (reset.consumeClick()) state.progress().reset();
            while (toggle.consumeClick()) {
                ConfigManager.get().renderRoutes = !ConfigManager.get().renderRoutes;
                ConfigManager.save();
                client.gui.setOverlayMessage(Component.literal("Dungeon routes: " + (ConfigManager.get().renderRoutes ? "on" : "off")), false);
            }
        });
    }

    private static KeyMapping register(String name, int key) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.dungeonroutes." + name, key, KeyMapping.Category.MISC));
    }
}
