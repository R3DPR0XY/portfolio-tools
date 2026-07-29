package com.draxxlink.kagerov.mixin;

import net.minecraft.client.gui.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditBox.class)
public interface EditBoxInvoker {
    @Invoker("exceedsMaxLines")
    boolean uniqueSkill$invokeExceedsMaxLines(String text);
}

