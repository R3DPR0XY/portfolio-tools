package com.draxxlink.kagerov.client.screen;

import com.draxxlink.kagerov.client.book.UniqueSkillPaletteBridge;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.minecraft.client.gui.screen.Screen;

public class UniqueSkillTextPaletteScreen extends CottonClientScreen {
    public UniqueSkillTextPaletteScreen(Screen parent, UniqueSkillPaletteBridge.Inserter inserter) {
        super(new UniqueSkillTextPaletteGui());
        UniqueSkillPaletteBridge.open(parent, inserter);
    }
}

