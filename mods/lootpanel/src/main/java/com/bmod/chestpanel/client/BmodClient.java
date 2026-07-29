package com.bmod.chestpanel.client;

import net.fabricmc.api.ClientModInitializer;

public final class BmodClient implements ClientModInitializer {
    public static final String MOD_ID = "lootpanel";

    @Override
    public void onInitializeClient() {
        LootPanelConfig.load();
    }
}
