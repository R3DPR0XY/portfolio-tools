package com.draxxlink.saturationmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

public class SaturationMod implements ClientModInitializer {

	private static boolean enabled = false;
	private static KeyBinding toggleKey;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.saturationmod.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_H,
				"category.saturationmod"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				enabled = !enabled;

				if (client.player != null) {
					client.player.sendMessage(
						Text.literal("Saturation: " + (enabled ? "§aON" : "§cOFF")),
						true
					);
				}
			}

			onEndTick(client);
		});
	}

	private void onEndTick(MinecraftClient client) {
		if (!enabled) return;

		if (client == null || client.player == null) return;
		if (client.currentScreen != null) return;

		PlayerEntity player = client.player;

		// Dar saturação completa ao jogador
		player.getHungerManager().setFoodLevel(20);
		player.getHungerManager().setSaturation(20.0f);

		// Curar o jogador
		if (player.getHealth() < player.getMaxHealth()) {
			player.setHealth(player.getMaxHealth());
		}
	}
}
