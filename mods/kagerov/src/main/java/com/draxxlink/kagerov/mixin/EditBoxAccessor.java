package com.draxxlink.kagerov.mixin;

import net.minecraft.client.gui.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditBox.class)
public interface EditBoxAccessor {
    @Accessor("cursor")
    int uniqueSkill$getCursorIndex();

    @Accessor("cursor")
    void uniqueSkill$setCursorIndex(int cursor);

    @Accessor("selectionEnd")
    int uniqueSkill$getSelectionEndIndex();

    @Accessor("selectionEnd")
    void uniqueSkill$setSelectionEndIndex(int selectionEnd);
}

