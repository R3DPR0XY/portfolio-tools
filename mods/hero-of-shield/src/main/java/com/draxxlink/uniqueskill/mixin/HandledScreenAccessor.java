package com.draxxlink.uniqueskill.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int unique_skill$getX();

    @Accessor("y")
    int unique_skill$getY();

    @Accessor("backgroundWidth")
    int unique_skill$getBackgroundWidth();
}
