package com.draxxlink.uniqueskill.client.integration;

import com.draxxlink.uniqueskill.client.screen.UniqueSkillHomeScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class UniqueSkillModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return UniqueSkillHomeScreen::new;
    }
}
