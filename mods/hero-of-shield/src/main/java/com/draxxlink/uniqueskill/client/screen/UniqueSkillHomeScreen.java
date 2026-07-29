package com.draxxlink.uniqueskill.client.screen;

import com.draxxlink.uniqueskill.client.UniqueSkillClient;
import com.draxxlink.uniqueskill.client.ui.UniqueSkillVisualTheme;
import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class UniqueSkillHomeScreen extends Screen {
    private static final int FRAME_WIDTH = 388;
    private static final int FRAME_HEIGHT = 274;
    private static final int SIDE_PADDING = 18;
    private static final int FRAME_TOP = UniqueSkillVisualTheme.PANEL_TOP;
    private static final int FRAME_BOTTOM = UniqueSkillVisualTheme.PANEL_BOTTOM;
    private static final int TEXT_PRIMARY = UniqueSkillVisualTheme.PANEL_TEXT;
    private static final int TEXT_SECONDARY = UniqueSkillVisualTheme.PANEL_MUTED_TEXT;
    private static final int TEXT_ACCENT = UniqueSkillVisualTheme.PANEL_GOLD;
    private static final int TEXT_SOFT = UniqueSkillVisualTheme.PANEL_SOFT_TEXT;

    private final Screen parent;

    private ButtonWidget modEnabledButton;
    private ButtonWidget hudButton;
    private ButtonWidget messagesButton;

    public UniqueSkillHomeScreen(Screen parent) {
        super(Text.translatable("screen.unique_skill.home.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();

        int centerX = this.width / 2;
        int frameWidth = getFrameWidth();
        int frameY = getFrameY();
        int frameX = centerX - (frameWidth / 2);
        int innerWidth = frameWidth - (SIDE_PADDING * 2);
        int spacing = 8;
        int smallWidth = (innerWidth - (spacing * 2)) / 3;
        int buttonY = frameY + 170;

        this.modEnabledButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            UniqueSkillClient.toggleMod(MinecraftClient.getInstance());
            refreshButtons();
        }).dimensions(frameX + SIDE_PADDING, buttonY, smallWidth, 22).build());

        this.hudButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            UniqueSkillConfig config = UniqueSkillConfig.getInstance();
            config.showHud = !config.showHud;
            config.save();
            refreshButtons();
        }).dimensions(frameX + SIDE_PADDING + smallWidth + spacing, buttonY, smallWidth, 22).build());

        this.messagesButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            UniqueSkillConfig config = UniqueSkillConfig.getInstance();
            config.showMessages = !config.showMessages;
            config.save();
            refreshButtons();
        }).dimensions(frameX + SIDE_PADDING + ((smallWidth + spacing) * 2), buttonY, smallWidth, 22).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.unique_skill.home.open_studio"),
            button -> this.client.setScreen(new UniqueSkillConfigScreen(this))
        ).dimensions(frameX + SIDE_PADDING, frameY + 200, innerWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.unique_skill.home.reset_profile"),
            button -> {
                UniqueSkillConfig config = UniqueSkillConfig.getInstance();
                config.resetToDefaults();
                config.save();
                UniqueSkillClient.applyRuntimeConfig(MinecraftClient.getInstance());
                refreshButtons();
            }
        ).dimensions(frameX + SIDE_PADDING, frameY + 231, (innerWidth - spacing) / 2, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.unique_skill.home.close"),
            button -> close()
        ).dimensions(frameX + SIDE_PADDING + ((innerWidth - spacing) / 2) + spacing, frameY + 231, (innerWidth - spacing) / 2, 20).build());

        refreshButtons();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        int centerX = this.width / 2;
        int frameWidth = getFrameWidth();
        int frameX = centerX - (frameWidth / 2);
        int frameY = getFrameY();
        int cardGap = 16;
        int cardWidth = (frameWidth - (SIDE_PADDING * 2) - cardGap) / 2;
        int compactGap = 10;
        int compactWidth = (frameWidth - (SIDE_PADDING * 2) - (compactGap * 2)) / 3;

        UniqueSkillVisualTheme.drawArcaneBackdrop(context, this.width, this.height);
        drawPanelFrame(context, frameX, frameY, frameWidth, FRAME_HEIGHT, FRAME_TOP, FRAME_BOTTOM);
        context.fill(frameX + 14, frameY + 44, frameX + frameWidth - 14, frameY + 45, UniqueSkillVisualTheme.PANEL_ACCENT);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, frameY + 14, TEXT_PRIMARY);
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("screen.unique_skill.home.subtitle"),
            centerX,
            frameY + 30,
            TEXT_SECONDARY
        );

        drawCard(
            context,
            frameX + SIDE_PADDING,
            frameY + 52,
            cardWidth,
            80,
            0xC01B0909,
            UniqueSkillVisualTheme.PANEL_RED,
            new ItemStack(Items.ENDER_EYE),
            Text.translatable("screen.unique_skill.home.card.field"),
            Text.translatable("screen.unique_skill.home.card.field.line_1", onOff(config.modEnabled)),
            Text.translatable("screen.unique_skill.home.card.field.line_2", onOff(config.showHud)),
            Text.translatable("screen.unique_skill.home.card.field.line_3", onOff(config.showMessages))
        );

        drawCard(
            context,
            frameX + SIDE_PADDING + cardWidth + cardGap,
            frameY + 52,
            cardWidth,
            80,
            0xC015090B,
            UniqueSkillVisualTheme.PANEL_RED,
            new ItemStack(Items.COMPASS),
            Text.translatable("screen.unique_skill.home.card.tools"),
            Text.translatable("screen.unique_skill.home.card.tools.line_1", onOff(config.blockInspectorEnabled)),
            Text.translatable("screen.unique_skill.home.card.tools.line_2", onOff(config.blockOutlineEnabled)),
            Text.translatable("screen.unique_skill.home.card.tools.line_3", onOff(config.showFoodTooltip))
        );

        drawCompactInfo(
            context,
            frameX + SIDE_PADDING,
            frameY + 146,
            new ItemStack(Items.SPYGLASS),
            Text.translatable("screen.unique_skill.home.info.range"),
            Text.translatable("screen.unique_skill.value.blocks", Integer.toString((int) config.detectionRange)),
            TEXT_PRIMARY
        );
        drawCompactInfo(
            context,
            frameX + SIDE_PADDING + compactWidth + compactGap,
            frameY + 146,
            new ItemStack(Items.BELL),
            Text.translatable("screen.unique_skill.home.info.alert"),
            Text.translatable("screen.unique_skill.value.percent", Math.round(config.ecolocationVolume * 100.0F)),
            TEXT_ACCENT
        );
        drawCompactInfo(
            context,
            frameX + SIDE_PADDING + ((compactWidth + compactGap) * 2),
            frameY + 146,
            new ItemStack(Items.SHIELD),
            Text.translatable("screen.unique_skill.home.info.awareness"),
            onOff(config.showPlayerWarnings),
            TEXT_SOFT
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawCard(
        DrawContext context,
        int x,
        int y,
        int width,
        int height,
        int backgroundColor,
        int accentColor,
        ItemStack icon,
        Text title,
        Text lineOne,
        Text lineTwo,
        Text lineThree
    ) {
        drawPanelFrame(context, x, y, width, height, backgroundColor, darken(backgroundColor));
        context.fill(x + 8, y + 6, x + width - 8, y + 7, accentColor);
        UniqueSkillVisualTheme.drawSigil(context, x + width - 14, y + 14, 4, 0x44FF8478);
        context.drawItemWithoutEntity(icon, x + 10, y + 9);
        context.drawText(this.textRenderer, title, x + 30, y + 12, TEXT_PRIMARY, false);
        context.drawText(this.textRenderer, lineOne, x + 10, y + 34, accentColor, false);
        context.drawText(this.textRenderer, lineTwo, x + 10, y + 47, TEXT_SECONDARY, false);
        context.drawText(this.textRenderer, lineThree, x + 10, y + 60, TEXT_SECONDARY, false);
    }

    private void drawCompactInfo(DrawContext context, int x, int y, ItemStack icon, Text label, Text value, int valueColor) {
        int width = Math.max(112, getFrameWidth() / 3 - 18);
        drawPanelFrame(context, x, y, width, 22, 0xC0150908, 0xC0060303);
        context.drawItemWithoutEntity(icon, x + 2, y + 1);
        context.drawText(this.textRenderer, label, x + 22, y + 3, TEXT_SECONDARY, false);
        context.drawText(this.textRenderer, value, x + 22, y + 13, valueColor, false);
    }

    private void refreshButtons() {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        this.modEnabledButton.setMessage(Text.translatable("screen.unique_skill.home.quick.mod", onOff(config.modEnabled)));
        this.hudButton.setMessage(Text.translatable("screen.unique_skill.home.quick.hud", onOff(config.showHud)));
        this.messagesButton.setMessage(Text.translatable("screen.unique_skill.home.quick.feed", onOff(config.showMessages)));
    }

    private Text onOff(boolean enabled) {
        return Text.translatable(enabled ? "hud.unique_skill.on" : "hud.unique_skill.off");
    }

    private int getFrameY() {
        return Math.max(28, (this.height / 2) - (FRAME_HEIGHT / 2));
    }

    private int getFrameWidth() {
        return Math.min(FRAME_WIDTH, this.width - 24);
    }

    private void drawPanelFrame(DrawContext context, int x, int y, int width, int height, int topColor, int bottomColor) {
        UniqueSkillVisualTheme.drawArcanePanel(context, x, y, width, height);
    }

    private int darken(int color) {
        int alpha = (color >> 24) & 0xFF;
        int red = Math.max(0, ((color >> 16) & 0xFF) - 10);
        int green = Math.max(0, ((color >> 8) & 0xFF) - 10);
        int blue = Math.max(0, (color & 0xFF) - 10);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
