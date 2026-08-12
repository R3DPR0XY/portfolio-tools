package com.r3dpr0xy.kagerov.mixin;

import com.r3dpr0xy.kagerov.client.book.UniqueSkillPaletteBridge;
import com.r3dpr0xy.kagerov.client.book.UniqueSkillUnicodeTokens;
import com.r3dpr0xy.kagerov.client.screen.UniqueSkillUnicodeSelectorScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    private static final int UNIQUE_SKILL_CHAT_SYMBOLS = 14;

    @Shadow
    protected TextFieldWidget chatField;

    @Unique
    private final List<ButtonWidget> uniqueSkill$chatButtons = new ArrayList<>();

    @Unique
    private int uniqueSkill$chatOffset;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void uniqueSkill$initChatButtons(CallbackInfo ci) {
        this.uniqueSkill$rebuildChatButtons();
    }

    @Unique
    private void uniqueSkill$rebuildChatButtons() {
        for (ButtonWidget button : this.uniqueSkill$chatButtons) {
            this.remove(button);
        }
        this.uniqueSkill$chatButtons.clear();

        int totalVisible = Math.min(UNIQUE_SKILL_CHAT_SYMBOLS, UniqueSkillUnicodeTokens.SAFE_TOKENS.length);
        int length = totalVisible * 12 + 4 * (totalVisible + 2);
        int startX = Math.max(16, (this.width / 2) - (length / 2));
        int y = this.height - 26;
        int slot = 0;

        if (this.uniqueSkill$chatOffset > 0) {
            this.uniqueSkill$chatButtons.add(this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> {
                    this.uniqueSkill$chatOffset = Math.max(0, this.uniqueSkill$chatOffset - UNIQUE_SKILL_CHAT_SYMBOLS);
                    this.uniqueSkill$rebuildChatButtons();
                })
                .dimensions(startX + (slot * 16), y, 12, 12)
                .build()));
            slot++;
        }

        int end = Math.min(this.uniqueSkill$chatOffset + UNIQUE_SKILL_CHAT_SYMBOLS, UniqueSkillUnicodeTokens.SAFE_TOKENS.length);
        for (int index = this.uniqueSkill$chatOffset; index < end; index++) {
            final String symbol = UniqueSkillUnicodeTokens.SAFE_TOKENS[index];
            this.uniqueSkill$chatButtons.add(this.addDrawableChild(ButtonWidget.builder(Text.literal(symbol), button -> this.uniqueSkill$insertChatToken(symbol))
                .dimensions(startX + (slot * 16), y, 12, 12)
                .build()));
            slot++;
        }

        if (end < UniqueSkillUnicodeTokens.SAFE_TOKENS.length) {
            this.uniqueSkill$chatButtons.add(this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> {
                    this.uniqueSkill$chatOffset = Math.min(UniqueSkillUnicodeTokens.SAFE_TOKENS.length - 1, this.uniqueSkill$chatOffset + UNIQUE_SKILL_CHAT_SYMBOLS);
                    this.uniqueSkill$rebuildChatButtons();
                })
                .dimensions(startX + (slot * 16), y, 12, 12)
                .build()));
            slot++;
        }

        this.uniqueSkill$chatButtons.add(this.addDrawableChild(ButtonWidget.builder(Text.literal("U+"), button -> this.uniqueSkill$openUnicodeSelector())
            .dimensions(startX + (slot * 16), y, 20, 12)
            .build()));
    }

    @Unique
    private void uniqueSkill$insertChatToken(String token) {
        if (this.chatField != null) {
            this.chatField.write(token);
            this.setFocused(this.chatField);
            this.chatField.setFocused(true);
        }
    }

    @Unique
    private void uniqueSkill$openUnicodeSelector() {
        if (this.client != null) {
            UniqueSkillPaletteBridge.open((Screen) (Object) this, this::uniqueSkill$insertChatToken);
            this.client.setScreen(new UniqueSkillUnicodeSelectorScreen());
        }
    }
}

