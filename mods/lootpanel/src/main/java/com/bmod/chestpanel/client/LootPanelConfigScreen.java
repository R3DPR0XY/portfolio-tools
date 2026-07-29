package com.bmod.chestpanel.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class LootPanelConfigScreen extends Screen {
    private static final int CARD_WIDTH = 446;
    private static final int CARD_HEIGHT = 366;
    private static final int TEXT = 0xFFEAEAEA;
    private static final int MUTED = 0xFFB7BBC2;
    private static final int GREEN = 0xFF9BE564;
    private static final int RED = 0xFFFF6B6B;

    private final Screen parent;
    private final LootPanelConfig config;

    public LootPanelConfigScreen(Screen parent) {
        super(Text.literal("LootPanel"));
        this.parent = parent;
        this.config = LootPanelConfig.get();
    }

    @Override
    protected void init() {
        int cardX = (width - CARD_WIDTH) / 2;
        int cardY = (height - CARD_HEIGHT) / 2;
        int x = cardX + 16;
        int y = cardY + 58;
        int full = CARD_WIDTH - 32;
        int half = (full - 6) / 2;

        addToggle(x, y, full, "Painel", () -> config.enabled, value -> config.enabled = value);
        y += 24;
        addToggle(x, y, half, "Containers", () -> config.containerPanel, value -> config.containerPanel = value);
        addToggle(x + half + 6, y, half, "Fornalha", () -> config.furnacePanel, value -> config.furnacePanel = value);
        y += 24;
        addCycle(x, y, half, () -> "Tema: " + config.theme.label(), () -> config.theme = config.theme.next());
        addToggle(x + half + 6, y, half, "Animacao", () -> config.animations, value -> config.animations = value);
        y += 24;
        addToggle(x, y, half, "Som", () -> config.soundFeedback, value -> config.soundFeedback = value);
        addToggle(x + half + 6, y, half, "Destaque raro", () -> config.highlightRare, value -> config.highlightRare = value);
        y += 24;
        addToggle(x, y, half, "Busca inteligente", () -> config.smartSearch, value -> config.smartSearch = value);
        addToggle(x + half + 6, y, half, "Filtro favorito", () -> config.favoriteFilter, value -> config.favoriteFilter = value);
        y += 24;
        addCycle(x, y, half, () -> "Lado: " + config.panelSide.label(), () -> config.panelSide = config.panelSide.next());
        addToggle(x + half + 6, y, half, "Ignorar hotbar", () -> config.ignoreHotbar, value -> config.ignoreHotbar = value);
        y += 24;
        addCycle(x, y, half, () -> "Perfil: " + config.sortProfile.label(), () -> config.sortProfile = config.sortProfile.next());
        addCycle(x + half + 6, y, half, () -> "Protegidos: " + config.lockedSlots, () -> config.lockedSlots = config.lockedSlots >= 36 ? 0 : config.lockedSlots + 9);
        y += 24;
        addCycle(x, y, half, () -> "Tamanho: " + config.scalePercent + "%", () -> config.scalePercent = config.scalePercent >= 140 ? 75 : config.scalePercent + 5);
        addCycle(x + half + 6, y, half, () -> "Opacidade: " + config.opacityPercent + "%", () -> config.opacityPercent = config.opacityPercent >= 100 ? 35 : config.opacityPercent + 5);
        y += 24;
        addToggle(x, y, half, "Ao lado do bau", () -> config.anchorToHandledScreen, value -> config.anchorToHandledScreen = value);
        addToggle(x + half + 6, y, half, "Estimativas", () -> config.showFurnaceEstimates, value -> config.showFurnaceEstimates = value);
        y += 24;
        addToggle(x, y, half, "Combustivel", () -> config.showFuelDetails, value -> config.showFuelDetails = value);
        y += 28;
        addDrawableChild(ButtonWidget.builder(Text.literal("Restaurar padrao"), button -> {
            config.resetDefaults();
            LootPanelConfig.save();
            clearAndInit();
        }).dimensions(x, y, full, 20).build());
        y += 28;
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar"), button -> {
            LootPanelConfig.save();
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(x, y, full, 20).build());
    }

    @Override
    public void close() {
        LootPanelConfig.save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int cardX = (width - CARD_WIDTH) / 2;
        int cardY = (height - CARD_HEIGHT) / 2;
        int accent = config.theme.accent();

        context.fill(cardX + 3, cardY + 4, cardX + CARD_WIDTH + 3, cardY + CARD_HEIGHT + 4, 0x88000000);
        context.fill(cardX, cardY, cardX + CARD_WIDTH, cardY + CARD_HEIGHT, 0xE6171B22);
        context.fill(cardX, cardY, cardX + CARD_WIDTH, cardY + 38, 0xFF222B36);
        context.fill(cardX, cardY + 37, cardX + CARD_WIDTH, cardY + 39, accent);
        context.drawBorder(cardX, cardY, CARD_WIDTH, CARD_HEIGHT, 0xFF3B4652);

        context.drawCenteredTextWithShadow(textRenderer, "LootPanel", width / 2, cardY + 10, accent);
        context.drawCenteredTextWithShadow(textRenderer, "Painel, busca inteligente e estimativas", width / 2, cardY + 35, MUTED);
        context.drawTextWithShadow(textRenderer, config.enabled ? "Ativo" : "Desativado", cardX + CARD_WIDTH - 78, cardY + 10, config.enabled ? GREEN : RED);
        super.render(context, mouseX, mouseY, delta);
    }

    private void addToggle(int x, int y, int w, String label, BoolGetter getter, BoolSetter setter) {
        addCycle(x, y, w, () -> label + ": " + (getter.get() ? "Ligado" : "Desligado"), () -> setter.set(!getter.get()));
    }

    private void addCycle(int x, int y, int w, LabelSupplier label, Runnable action) {
        ButtonWidget[] holder = new ButtonWidget[1];
        holder[0] = ButtonWidget.builder(Text.literal(label.get()), button -> {
            action.run();
            LootPanelConfig.save();
            button.setMessage(Text.literal(label.get()));
        }).dimensions(x, y, w, 20).build();
        addDrawableChild(holder[0]);
    }

    private interface BoolGetter { boolean get(); }
    private interface BoolSetter { void set(boolean value); }
    private interface LabelSupplier { String get(); }
}
