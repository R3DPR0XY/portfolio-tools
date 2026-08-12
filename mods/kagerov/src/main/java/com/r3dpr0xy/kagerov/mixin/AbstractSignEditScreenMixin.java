package com.r3dpr0xy.kagerov.mixin;

import com.r3dpr0xy.kagerov.client.book.UniqueSkillSignClipboard;
import com.r3dpr0xy.kagerov.client.screen.UniqueSkillTextPaletteScreen;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {
    @Shadow
    @Final
    protected SignBlockEntity blockEntity;

    @Shadow
    @Final
    private String[] messages;

    @Shadow
    private int currentRow;

    protected AbstractSignEditScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void uniqueSkill$addSignTools(CallbackInfo ci) {
        int toolbarWidth = 92;
        int toolbarX = Math.max(6, (this.width / 2) - 150);
        int toolbarY = Math.max(8, (this.height / 2) - 50);
        int rowHeight = 20;

        this.addDrawableChild(button(toolbarX, toolbarY, toolbarWidth, "screen.kagerov.sign.copy", this::uniqueSkill$copySign));
        this.addDrawableChild(button(toolbarX, toolbarY + rowHeight, toolbarWidth, "screen.kagerov.sign.paste", this::uniqueSkill$pasteSign));
        this.addDrawableChild(button(toolbarX, toolbarY + (rowHeight * 2), toolbarWidth, "screen.kagerov.sign.clear", this::uniqueSkill$clearSign));
        this.addDrawableChild(button(toolbarX, toolbarY + (rowHeight * 3), toolbarWidth, "screen.kagerov.sign.clipboard", this::uniqueSkill$pasteClipboard));
        this.addDrawableChild(button(toolbarX, toolbarY + (rowHeight * 4), toolbarWidth, "screen.kagerov.sign.palette", this::uniqueSkill$openPalette));
    }

    @Unique
    private ButtonWidget button(int x, int y, int width, String key, Runnable action) {
        return ButtonWidget.builder(Text.translatable(key), button -> action.run())
            .dimensions(x, y, width, 18)
            .build();
    }

    @Unique
    private void uniqueSkill$copySign() {
        UniqueSkillSignClipboard.copy(this.messages);
        uniqueSkill$notify("message.kagerov.sign.copied");
    }

    @Unique
    private void uniqueSkill$pasteSign() {
        String[] copiedLines = UniqueSkillSignClipboard.getCopiedLines();
        for (int index = 0; index < this.messages.length && index < copiedLines.length; index++) {
            this.messages[index] = uniqueSkill$trimToSignWidth(copiedLines[index]);
        }
        uniqueSkill$notify("message.kagerov.sign.pasted");
    }

    @Unique
    private void uniqueSkill$clearSign() {
        for (int index = 0; index < this.messages.length; index++) {
            this.messages[index] = "";
        }
        this.currentRow = 0;
        uniqueSkill$notify("message.kagerov.sign.cleared");
    }

    @Unique
    private void uniqueSkill$pasteClipboard() {
        if (this.client == null || this.currentRow < 0 || this.currentRow >= this.messages.length) {
            return;
        }

        this.messages[this.currentRow] = uniqueSkill$trimToSignWidth(this.client.keyboard.getClipboard());
        uniqueSkill$notify("message.kagerov.sign.clipboard_pasted");
    }

    @Unique
    private String uniqueSkill$trimToSignWidth(String text) {
        if (this.textRenderer == null) {
            return text == null ? "" : text;
        }
        return this.textRenderer.trimToWidth(text == null ? "" : text, this.blockEntity.getMaxTextWidth());
    }

    @Unique
    private void uniqueSkill$notify(String key) {
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.translatable(key), true);
        }
    }

    @Unique
    private void uniqueSkill$openPalette() {
        if (this.client != null) {
            this.client.setScreen(new UniqueSkillTextPaletteScreen((Screen) (Object) this, this::uniqueSkill$insertPaletteToken));
        }
    }

    @Unique
    private void uniqueSkill$insertPaletteToken(String token) {
        if (this.currentRow < 0 || this.currentRow >= this.messages.length) {
            return;
        }

        this.messages[this.currentRow] = uniqueSkill$trimToSignWidth(this.messages[this.currentRow] + token);
    }
}

