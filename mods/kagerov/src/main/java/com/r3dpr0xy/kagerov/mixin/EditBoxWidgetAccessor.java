package com.r3dpr0xy.kagerov.mixin;

import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.widget.EditBoxWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditBoxWidget.class)
public interface EditBoxWidgetAccessor {
    @Accessor("editBox")
    EditBox uniqueSkill$getEditBox();
}

