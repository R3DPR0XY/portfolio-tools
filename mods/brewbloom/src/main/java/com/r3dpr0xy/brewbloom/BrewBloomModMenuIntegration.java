package com.r3dpr0xy.brewbloom;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class BrewBloomModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new BrewBloomConfigScreen(BrewBloomClient.config, parent);
    }
}
