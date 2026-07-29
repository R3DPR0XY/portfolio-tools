package com.draxxlink.uniqueskill.client.network;

import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import com.draxxlink.uniqueskill.network.AuraSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class UniqueSkillClientNetworking {
    private static final long SYNC_INTERVAL_TICKS = 10L;

    private static boolean lastEnabled;
    private static int lastColorRgb = -1;
    private static int lastDensity = -1;
    private static long nextSyncTick;

    private UniqueSkillClientNetworking() {
    }

    public static void register() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    }

    public static void syncAuraState(MinecraftClient client, UniqueSkillConfig config, long worldTick, int colorRgb) {
        if (client.world == null || client.player == null) {
            return;
        }

        boolean shouldShare = config.modEnabled && config.bubbleAuraEnabled && config.bubbleSharedMode;
        if (!ClientPlayNetworking.canSend(AuraSyncPayload.ID)) {
            return;
        }

        boolean stateChanged = shouldShare != lastEnabled || colorRgb != lastColorRgb || config.bubbleDensity != lastDensity;
        if (!stateChanged && worldTick < nextSyncTick) {
            return;
        }

        ClientPlayNetworking.send(new AuraSyncPayload(shouldShare, colorRgb, config.bubbleDensity));
        lastEnabled = shouldShare;
        lastColorRgb = colorRgb;
        lastDensity = config.bubbleDensity;
        nextSyncTick = worldTick + SYNC_INTERVAL_TICKS;
    }

    public static void reset() {
        lastEnabled = false;
        lastColorRgb = -1;
        lastDensity = -1;
        nextSyncTick = 0L;
    }
}
