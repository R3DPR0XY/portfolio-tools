package com.r3dpr0xy.kagerov.mixin;

import com.r3dpr0xy.kagerov.client.book.UniqueSkillBookReader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookScreen.class)
public abstract class BookScreenMixin extends Screen {
    @Shadow
    private BookScreen.Contents contents;

    @Shadow
    private int pageIndex;

    protected BookScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void uniqueSkill$addReaderButton(CallbackInfo ci) {
        int left = (this.width - 192) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.reader.button"), button -> {
            if (this.client != null) {
                UniqueSkillBookReader.openPages(this.client, (Screen) (Object) this, this.title, this.contents.pages(), this.pageIndex);
            }
        }).dimensions(left - 94, 24, 88, 20).build());
    }
}

