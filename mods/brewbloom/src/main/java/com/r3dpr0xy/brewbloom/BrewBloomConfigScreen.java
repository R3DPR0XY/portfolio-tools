package com.r3dpr0xy.brewbloom;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BrewBloomConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 604;
    private static final int PANEL_HEIGHT = 500;
    private static final int PICKER_SIZE = 72;
    private static final int PICKER_STEP = 4;
    private static final int HUE_WIDTH = 12;
    private static final int RED = 0xFFE0182D;
    private static final int RED_HOVER = 0xFFFF4055;
    private static final int DARK_RED = 0xFF4B0008;
    private static final int BLACK = 0xFF050507;
    private static final int SURFACE = 0xF0060609;
    private static final int SURFACE_SOFT = 0xD914151A;
    private static final int WHITE = 0xFFF5F5F5;
    private static final int MUTED = 0xFF9C9CA3;
    private static final int PANEL_DARK = 0xE60B0C10;
    private static final int PANEL_MID = 0xAA1B1C22;
    private static final int PANEL_EDGE = 0xFF565860;
    private static final int PANEL_HIGHLIGHT = 0x44FFFFFF;
    private static final String[] PRESET_COLORS = {
        "#FF0000", "#FF7A00", "#FFD400", "#42FF3F", "#00E5FF", "#2A5BFF",
        "#8B2AFF", "#FF2AD4", "#FFFFFF", "#0A0A0A", "#7A0000", "#6E6E76",
        "#FF5A5A", "#D8D8D8", "#00FF88", "#00A3FF", "#B000FF", "#A00012"
    };

    private final BrewBloomConfig config;
    private final Screen parent;
    private final List<TextFieldWidget> colorFields = new ArrayList<>();
    private final List<ButtonWidget> manualColorWidgets = new ArrayList<>();
    private TextFieldWidget importField;

    private int panelX;
    private int panelY;
    private int selectedColor = 0;
    private float pickerHue = 0.0F;
    private Text hoverDescription = Text.empty();
    private boolean syncingFields = false;
    private boolean dirtyColorDrag = false;

    public BrewBloomConfigScreen(BrewBloomConfig config) {
        this(config, null);
    }

    public BrewBloomConfigScreen(BrewBloomConfig config, Screen parent) {
        super(Text.translatable("screen.brewbloom.title"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        colorFields.clear();
        manualColorWidgets.clear();
        panelX = Math.max(8, (width - PANEL_WIDTH) / 2);
        panelY = Math.max(6, (height - PANEL_HEIGHT) / 2);

        addDrawableChild(button(panelX + PANEL_WIDTH - 316, panelY + 16, 68, 18, textureText(), button -> {
            config.cycleTextureMode();
            button.setMessage(textureText());
        }));
        addDrawableChild(button(panelX + PANEL_WIDTH - 240, panelY + 16, 68, 18, soundStyleText(), button -> {
            config.cycleMenuSoundStyle();
            button.setMessage(soundStyleText());
        }));
        addDrawableChild(button(panelX + PANEL_WIDTH - 164, panelY + 16, 64, 18, soundText(), button -> {
            config.menuSounds = !config.menuSounds;
            config.save();
            button.setMessage(soundText());
        }));
        addDrawableChild(button(panelX + PANEL_WIDTH - 92, panelY + 16, 64, 18, Text.translatable("screen.brewbloom.done"), button -> close()));

        addModeTab(panelX + 28, panelY + 56, 108, "effect", Text.translatable("screen.brewbloom.tab_effect"));
        addModeTab(panelX + 142, panelY + 56, 94, "rgb", Text.translatable("screen.brewbloom.tab_neon"));
        addModeTab(panelX + 242, panelY + 56, 124, "rainbow", Text.translatable("screen.brewbloom.tab_rainbow"));
        addModeTab(panelX + 372, panelY + 56, 112, "custom", Text.translatable("screen.brewbloom.tab_manual"));

        addStepper(panelX + 28, panelY + 112, 206, () -> config.adjustRadius(-0.05D), () -> config.adjustRadius(0.05D));
        addStepper(panelX + 28, panelY + 142, 206, () -> config.adjustDensity(-1), () -> config.adjustDensity(1));
        addStepper(panelX + 28, panelY + 172, 206, () -> config.adjustEffectLimit(-1), () -> config.adjustEffectLimit(1));
        addStepper(panelX + 28, panelY + 202, 206, () -> config.adjustTotalBubbleLimit(-1), () -> config.adjustTotalBubbleLimit(1));
        addDrawableChild(button(panelX + 28, panelY + 232, 126, 20, styleText(), button -> {
            if (client != null) {
                client.setScreen(new BrewBloomStyleScreen(config, this));
            }
        }));
        addDrawableChild(button(panelX + 160, panelY + 232, 74, 20, testModeText(), button -> {
            config.showWithoutEffects = !config.showWithoutEffects;
            config.save();
            button.setMessage(testModeText());
        }));

        addStepper(panelX + 332, panelY + 112, 236, () -> config.adjustHeightScale(-0.05D), () -> config.adjustHeightScale(0.05D));
        addStepper(panelX + 332, panelY + 142, 236, () -> config.adjustRiseSpeed(-0.05D), () -> config.adjustRiseSpeed(0.05D));
        addStepper(panelX + 332, panelY + 172, 236, () -> config.adjustSwirlSpeed(-0.05D), () -> config.adjustSwirlSpeed(0.05D));
        addStepper(panelX + 332, panelY + 202, 236, () -> config.adjustColorCycleSpeed(-1), () -> config.adjustColorCycleSpeed(1));
        addStepper(panelX + 332, panelY + 226, 236, () -> config.adjustCrowdedEffectStart(-1), () -> config.adjustCrowdedEffectStart(1));

        for (int index = 0; index < 3; index++) {
            TextFieldWidget field = new TextFieldWidget(
                textRenderer,
                panelX + 58,
                panelY + 326 + index * 34,
                120,
                20,
                Text.literal("Color " + (index + 1))
            );
            field.setMaxLength(64);
            field.setText(colorAt(index));
            field.setEditableColor(0xFFFFFF);
            field.setUneditableColor(0x9C9CA3);
            field.setEditable("custom".equals(config.colorMode));
            int colorIndex = index;
            field.setChangedListener(value -> {
                if (!syncingFields && BrewBloomConfig.isValidHexColor(value)) {
                    selectedColor = colorIndex;
                    config.setColor(colorIndex, value);
                }
            });
            colorFields.add(field);
            addDrawableChild(field);
            ButtonWidget slotButton = button(panelX + 184, panelY + 326 + index * 34, 70, 20, Text.translatable("screen.brewbloom.color_slot", index + 1), button -> {
                selectedColor = colorIndex;
                colorFields.get(colorIndex).setFocused(true);
                pickerHue = rgbToHsv(config.colorAt(selectedColor))[0];
            });
            manualColorWidgets.add(slotButton);
            addDrawableChild(slotButton);
        }

        addColorStepper(panelX + 272, panelY + 326, 0, Text.literal("R"));
        addColorStepper(panelX + 272, panelY + 360, 1, Text.literal("G"));
        addColorStepper(panelX + 272, panelY + 394, 2, Text.literal("B"));

        for (int index = 0; index < PRESET_COLORS.length; index++) {
            int row = index / 9;
            int column = index % 9;
            String color = PRESET_COLORS[index];
            ButtonWidget presetButton = button(panelX + 428 + column * 17, panelY + 414 + row * 16, 14, 12, Text.literal(""), button -> applyPreset(color));
            manualColorWidgets.add(presetButton);
            addDrawableChild(presetButton);
        }

        importField = new TextFieldWidget(textRenderer, panelX + 28, panelY + 470, 300, 18, Text.translatable("screen.brewbloom.profile_field"));
        importField.setMaxLength(2048);
        importField.setEditableColor(0xFFFFFF);
        importField.setUneditableColor(0x77777D);
        addDrawableChild(importField);
        ButtonWidget importButton = button(panelX + 336, panelY + 470, 58, 18, Text.translatable("screen.brewbloom.import"), button -> importProfile());
        ButtonWidget gradientButton = button(panelX + 402, panelY + 470, 66, 18, Text.translatable("screen.brewbloom.gradient"), button -> applyGradient());
        ButtonWidget copyButton = button(panelX + 476, panelY + 470, 58, 18, Text.translatable("screen.brewbloom.copy_profile"), button -> copyProfile());
        ButtonWidget resetButton = button(panelX + 542, panelY + 470, 34, 18, Text.translatable("screen.brewbloom.reset"), button -> resetDefaults());
        manualColorWidgets.add(gradientButton);
        addDrawableChild(importButton);
        addDrawableChild(gradientButton);
        addDrawableChild(copyButton);
        addDrawableChild(resetButton);
        updateColorEditState();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        config.clamped();
        hoverDescription = Text.empty();
        drawPanel(context);
        drawBloomPreview(context, delta);
        drawColorPicker(context);
        super.render(context, mouseX, mouseY, delta);
        drawLabels(context, mouseX, mouseY);
        drawColorPreviews(context);
        drawPresetSwatches(context);
        drawHoverDescription(context);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        config.save();
        if (client != null && parent != null) {
            client.setScreen(parent);
            return;
        }

        super.close();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleColorPicker(mouseX, mouseY)) {
            playMenuSound();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (handleColorPicker(mouseX, mouseY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dirtyColorDrag) {
            dirtyColorDrag = false;
            config.save();
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void addStepper(int x, int y, int width, Runnable minus, Runnable plus) {
        addDrawableChild(button(x, y + 12, 28, 20, Text.literal("-"), button -> minus.run()));
        addDrawableChild(button(x + width - 28, y + 12, 28, 20, Text.literal("+"), button -> plus.run()));
    }

    private void drawPanel(DrawContext context) {
        context.fill(0, 0, width, height, 0xB8000000);
        context.fill(panelX - 5, panelY - 5, panelX + PANEL_WIDTH + 5, panelY + PANEL_HEIGHT + 5, 0x99000000);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, SURFACE);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 4, RED);
        context.fill(panelX, panelY + PANEL_HEIGHT - 2, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, DARK_RED);
        context.fill(panelX + 1, panelY + 1, panelX + PANEL_WIDTH - 1, panelY + PANEL_HEIGHT - 1, PANEL_HIGHLIGHT);
        drawSection(context, panelX + 20, panelY + 48, PANEL_WIDTH - 40, 44);
        drawSection(context, panelX + 20, panelY + 102, 232, 154);
        drawSection(context, panelX + 316, panelY + 102, PANEL_WIDTH - 336, 154);
        drawSection(context, panelX + 20, panelY + 294, 244, 156);
        drawSection(context, panelX + 268, panelY + 294, 146, 156);
        drawSection(context, panelX + 420, panelY + 294, PANEL_WIDTH - 440, 156);
        drawSection(context, panelX + 20, panelY + 456, PANEL_WIDTH - 40, 38);
        context.fill(panelX + 18, panelY + 46, panelX + PANEL_WIDTH - 18, panelY + 47, 0x66E0182D);
        context.fill(panelX + 296, panelY + 102, panelX + 297, panelY + 258, 0x33FFFFFF);
        context.fill(panelX + 20, panelY + 262, panelX + PANEL_WIDTH - 20, panelY + 263, 0x33FFFFFF);
        context.fill(panelX + 20, panelY + 266, panelX + PANEL_WIDTH - 20, panelY + 267, 0x33FFFFFF);
        context.fill(panelX + 20, panelY + 452, panelX + PANEL_WIDTH - 20, panelY + 453, 0x33FFFFFF);
    }

    private void drawSection(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, PANEL_EDGE);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_DARK);
        context.fill(x + 2, y + 2, x + width - 2, y + 13, PANEL_MID);
        context.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, 0xAA050507);
        context.fill(x + 2, y + 2, x + width - 2, y + 3, 0x33FFFFFF);
    }

    private void drawLabels(DrawContext context, int mouseX, int mouseY) {
        drawGradientText(context, Text.literal("BrewBloom"), panelX + 28, panelY + 18, 0xFFFFFFFF, 0xFFFF4055);
        drawGradientText(context, Text.literal("BLOOM CONTROL"), panelX + 28, panelY + 31, RED, 0xFFFFE6EA);
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.subtitle"), panelX + 164, panelY + 24, MUTED);

        drawGradientText(context, Text.translatable("screen.brewbloom.mode"), panelX + 28, panelY + 50, RED, 0xFFFFE6EA);
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.mode.help"), panelX + 28, panelY + 82, MUTED);
        drawGradientText(context, Text.translatable("screen.brewbloom.appearance"), panelX + 28, panelY + 102, RED, 0xFFFFE6EA);
        drawStepperLabel(context, panelX + 28, panelY + 112, Text.translatable("screen.brewbloom.radius"), radiusText());
        drawStepperLabel(context, panelX + 28, panelY + 142, Text.translatable("screen.brewbloom.density"), Text.literal(String.valueOf(config.density)));
        drawStepperLabel(context, panelX + 28, panelY + 172, Text.translatable("screen.brewbloom.effect_limit"), Text.literal(String.valueOf(config.effectLimit)));
        drawStepperLabel(context, panelX + 28, panelY + 202, Text.translatable("screen.brewbloom.total_bubbles"), Text.literal(String.valueOf(config.totalBubbleLimit)));
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.style"), panelX + 28, panelY + 222, MUTED);
        describeRow(mouseX, mouseY, panelX + 28, panelY + 112, 206, Text.translatable("screen.brewbloom.desc.radius"));
        describeRow(mouseX, mouseY, panelX + 28, panelY + 142, 206, Text.translatable("screen.brewbloom.desc.density"));
        describeRow(mouseX, mouseY, panelX + 28, panelY + 172, 206, Text.translatable("screen.brewbloom.desc.effect_limit"));
        describeRow(mouseX, mouseY, panelX + 28, panelY + 202, 206, Text.translatable("screen.brewbloom.desc.total_bubbles"));
        describeRow(mouseX, mouseY, panelX + 28, panelY + 222, 206, Text.translatable("screen.brewbloom.desc.style"));

        drawGradientText(context, Text.translatable("screen.brewbloom.motion"), panelX + 332, panelY + 102, RED, 0xFFFFE6EA);
        drawStepperLabel(context, panelX + 332, panelY + 112, Text.translatable("screen.brewbloom.height"), percentText(config.heightScale), 118);
        drawStepperLabel(context, panelX + 332, panelY + 142, Text.translatable("screen.brewbloom.rise"), percentText(config.riseSpeed), 118);
        drawStepperLabel(context, panelX + 332, panelY + 172, Text.translatable("screen.brewbloom.swirl"), percentText(config.swirlSpeed), 118);
        drawStepperLabel(context, panelX + 332, panelY + 202, Text.translatable("screen.brewbloom.color_speed"), Text.literal(String.valueOf(config.colorCycleSpeed)), 118);
        drawStepperLabel(context, panelX + 332, panelY + 226, Text.translatable("screen.brewbloom.crowd_start"), Text.literal(String.valueOf(config.crowdedEffectStart)), 118);
        describeRow(mouseX, mouseY, panelX + 332, panelY + 112, 236, Text.translatable("screen.brewbloom.desc.height"));
        describeRow(mouseX, mouseY, panelX + 332, panelY + 142, 236, Text.translatable("screen.brewbloom.desc.rise"));
        describeRow(mouseX, mouseY, panelX + 332, panelY + 172, 236, Text.translatable("screen.brewbloom.desc.swirl"));
        describeRow(mouseX, mouseY, panelX + 332, panelY + 202, 236, Text.translatable("screen.brewbloom.desc.color_speed"));
        describeRow(mouseX, mouseY, panelX + 332, panelY + 226, 236, Text.translatable("screen.brewbloom.desc.crowd_start"));

        drawGradientText(context, Text.translatable("screen.brewbloom.custom_colors"), panelX + 28, panelY + 292, RED, 0xFFFFE6EA);
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.custom_colors.help"), panelX + 28, panelY + 302, MUTED);
        drawGradientText(context, Text.translatable("screen.brewbloom.rgb_controls"), panelX + 272, panelY + 296, 0xFFBFC0C7, 0xFFFF4055);
        drawGradientText(context, Text.translatable("screen.brewbloom.presets"), panelX + 428, panelY + 404, 0xFFBFC0C7, 0xFFFF4055);
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.profile_help"), panelX + 28, panelY + 458, MUTED);
        describeRow(mouseX, mouseY, panelX + 28, panelY + 298, 230, Text.translatable("screen.brewbloom.desc.custom_colors"));
        describeRow(mouseX, mouseY, panelX + 272, panelY + 326, 116, Text.translatable("screen.brewbloom.desc.rgb_controls"));
        describeRow(mouseX, mouseY, pickerX(), pickerY(), PICKER_SIZE + HUE_WIDTH + 12, Text.translatable("screen.brewbloom.desc.picker"));
    }

    private void drawStepperLabel(DrawContext context, int x, int y, Text label, Text value) {
        drawStepperLabel(context, x, y, label, value, 88);
    }

    private void drawStepperLabel(DrawContext context, int x, int y, Text label, Text value, int centerOffset) {
        context.drawTextWithShadow(textRenderer, label, x, y, MUTED);
        context.drawCenteredTextWithShadow(textRenderer, value, x + centerOffset, y + 18, WHITE);
    }

    private void drawGradientText(DrawContext context, Text text, int x, int y, int startColor, int endColor) {
        String value = text.getString();
        int cursor = x;
        int length = Math.max(1, value.length() - 1);
        for (int index = 0; index < value.length(); index++) {
            String character = value.substring(index, index + 1);
            float progress = index / (float) length;
            int color = lerpColor(startColor, endColor, progress);
            context.drawTextWithShadow(textRenderer, Text.literal(character), cursor, y, color);
            cursor += textRenderer.getWidth(character);
        }
    }

    private int lerpColor(int startColor, int endColor, float progress) {
        int startRed = (startColor >> 16) & 0xFF;
        int startGreen = (startColor >> 8) & 0xFF;
        int startBlue = startColor & 0xFF;
        int endRed = (endColor >> 16) & 0xFF;
        int endGreen = (endColor >> 8) & 0xFF;
        int endBlue = endColor & 0xFF;
        int red = Math.round(startRed + (endRed - startRed) * progress);
        int green = Math.round(startGreen + (endGreen - startGreen) * progress);
        int blue = Math.round(startBlue + (endBlue - startBlue) * progress);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private void drawColorPreviews(DrawContext context) {
        for (int index = 0; index < colorFields.size(); index++) {
            TextFieldWidget field = colorFields.get(index);
            int color = BrewBloomConfig.isValidHexColor(field.getText())
                ? Integer.parseInt(BrewBloomConfig.normalizeHexColor(field.getText()).substring(1), 16)
                : 0x303846;

            int x = panelX + 28;
            int y = panelY + 326 + index * 34;
            context.fill(x - 1, y - 1, x + 25, y + 21, 0xFF565860);
            context.fill(x, y, x + 24, y + 20, 0xFF000000 | color);
            context.fill(x + 2, y + 2, x + 11, y + 5, 0x55FFFFFF);
            if (index == selectedColor) {
                context.fill(x - 3, y - 3, x + 156, y - 1, RED);
                context.fill(x - 3, y + 23, x + 156, y + 25, RED);
            }
        }
    }

    private void drawPresetSwatches(DrawContext context) {
        for (int index = 0; index < PRESET_COLORS.length; index++) {
            int row = index / 9;
            int column = index % 9;
            int color = Integer.parseInt(PRESET_COLORS[index].substring(1), 16);
            int x = panelX + 431 + column * 17;
            int y = panelY + 418 + row * 16;
            context.fill(x, y, x + 8, y + 4, 0xFF000000 | color);
        }
    }

    private void drawColorPicker(DrawContext context) {
        int squareX = pickerX();
        int squareY = pickerY();
        int hueX = squareX + PICKER_SIZE + 10;
        int color = config.colorAt(selectedColor);
        float[] hsv = rgbToHsv(color);
        pickerHue = hsv[0];

        context.fill(squareX - 2, squareY - 2, hueX + HUE_WIDTH + 2, squareY + PICKER_SIZE + 2, 0xFF000000);
        for (int x = 0; x < PICKER_SIZE; x += PICKER_STEP) {
            float saturation = x / (float) (PICKER_SIZE - 1);
            for (int y = 0; y < PICKER_SIZE; y += PICKER_STEP) {
                float value = 1.0F - (y / (float) (PICKER_SIZE - 1));
                context.fill(squareX + x, squareY + y, squareX + x + PICKER_STEP, squareY + y + PICKER_STEP, 0xFF000000 | MathHelper.hsvToRgb(pickerHue, saturation, value));
            }
        }

        for (int y = 0; y < PICKER_SIZE; y += 2) {
            float hue = y / (float) (PICKER_SIZE - 1);
            context.fill(hueX, squareY + y, hueX + HUE_WIDTH, squareY + y + 2, 0xFF000000 | MathHelper.hsvToRgb(hue, 1.0F, 1.0F));
        }

        int cursorX = squareX + Math.round(hsv[1] * (PICKER_SIZE - 1));
        int cursorY = squareY + Math.round((1.0F - hsv[2]) * (PICKER_SIZE - 1));
        int hueY = squareY + Math.round(pickerHue * (PICKER_SIZE - 1));
        context.fill(cursorX - 2, cursorY - 2, cursorX + 3, cursorY + 3, WHITE);
        context.fill(hueX - 2, hueY - 1, hueX + HUE_WIDTH + 2, hueY + 2, WHITE);

        if (!customColorsEnabled()) {
            context.fill(squareX - 2, squareY - 2, hueX + HUE_WIDTH + 2, squareY + PICKER_SIZE + 2, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.locked_manual"), squareX + 47, squareY + 32, MUTED);
        }
    }

    private void drawBloomPreview(DrawContext context, float delta) {
        int centerX = panelX + 534;
        int centerY = panelY + 70;
        long time = System.currentTimeMillis();

        context.fill(centerX - 42, centerY - 20, centerX + 42, centerY + 18, 0xEE050507);
        context.fill(centerX - 42, centerY - 20, centerX + 42, centerY - 17, 0xFFE0182D);
        context.fill(centerX - 40, centerY - 16, centerX + 40, centerY + 16, 0xAA111217);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.preview"), centerX, centerY + 8, MUTED);

        for (int index = 0; index < 3; index++) {
            double angle = (time / 420.0D) + index * 2.094D + delta * 0.05D;
            int color = previewColor(time, index);
            int x = centerX + (int) Math.round(Math.cos(angle) * 18.0D);
            int y = centerY - 4 + (int) Math.round(Math.sin(angle) * 7.0D);
            context.fill(x - 4, y - 4, x + 5, y + 5, 0xFF000000 | color);
            context.fill(x - 2, y - 2, x + 3, y + 3, 0x88FFFFFF);
        }
    }

    private int previewColor(long time, int index) {
        long tick = time / 50L;
        return switch (config.colorMode) {
            case "rgb" -> MathHelper.hsvToRgb(((tick * config.colorCycleSpeed + index * 11L) % 180L) / 180.0F, 0.92F, 1.0F);
            case "rainbow" -> MathHelper.hsvToRgb(((tick * config.colorCycleSpeed + index * 18L) % 240L) / 240.0F, 0.82F, 1.0F);
            case "custom" -> config.colorAt(index);
            default -> 0xA3F7FF;
        };
    }

    private void drawHoverDescription(DrawContext context) {
        String description = hoverDescription.getString();
        if (description.isEmpty()) {
            return;
        }

        int x = panelX + 28;
        int y = panelY + 256;
        context.fill(x - 4, y - 4, panelX + PANEL_WIDTH - 24, y + 13, 0xCC050507);
        context.drawTextWithShadow(textRenderer, hoverDescription, x, y, MUTED);
    }

    private void describeRow(int mouseX, int mouseY, int x, int y, int width, Text description) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 32) {
            hoverDescription = description;
        }
    }

    private void applyPreset(String color) {
        if (colorFields.isEmpty()) {
            return;
        }

        if (!customColorsEnabled()) {
            return;
        }

        updateColorEditState();
        int target = Math.max(0, Math.min(selectedColor, colorFields.size() - 1));
        colorFields.get(target).setText(color);
        colorFields.get(target).setFocused(true);
    }

    private void addColorStepper(int x, int y, int component, Text label) {
        ButtonWidget minus = button(x, y + 12, 28, 18, Text.literal("-"), button -> syncSelectedColor(component, -8));
        ButtonWidget plus = button(x + 88, y + 12, 28, 18, Text.literal("+"), button -> syncSelectedColor(component, 8));
        ButtonWidget componentButton = button(x + 32, y + 12, 52, 18, label, button -> syncSelectedColor(component, 32));
        manualColorWidgets.add(minus);
        manualColorWidgets.add(plus);
        manualColorWidgets.add(componentButton);
        addDrawableChild(minus);
        addDrawableChild(plus);
        addDrawableChild(componentButton);
    }

    private void syncSelectedColor(int component, int amount) {
        if (!customColorsEnabled()) {
            return;
        }

        config.adjustColorComponent(selectedColor, component, amount);
        if (selectedColor >= 0 && selectedColor < colorFields.size()) {
            colorFields.get(selectedColor).setText(colorAt(selectedColor));
            colorFields.get(selectedColor).setFocused(true);
        }
    }

    private boolean handleColorPicker(double mouseX, double mouseY) {
        if (!customColorsEnabled()) {
            return false;
        }

        int squareX = pickerX();
        int squareY = pickerY();
        int hueX = squareX + PICKER_SIZE + 10;
        if (mouseX >= squareX && mouseX <= squareX + PICKER_SIZE && mouseY >= squareY && mouseY <= squareY + PICKER_SIZE) {
            float saturation = (float) ((mouseX - squareX) / (double) PICKER_SIZE);
            float value = 1.0F - (float) ((mouseY - squareY) / (double) PICKER_SIZE);
            setSelectedColor(MathHelper.hsvToRgb(pickerHue, MathHelper.clamp(saturation, 0.0F, 1.0F), MathHelper.clamp(value, 0.0F, 1.0F)));
            return true;
        }

        if (mouseX >= hueX && mouseX <= hueX + HUE_WIDTH && mouseY >= squareY && mouseY <= squareY + PICKER_SIZE) {
            pickerHue = MathHelper.clamp((float) ((mouseY - squareY) / (double) PICKER_SIZE), 0.0F, 1.0F);
            float[] hsv = rgbToHsv(config.colorAt(selectedColor));
            setSelectedColor(MathHelper.hsvToRgb(pickerHue, hsv[1], hsv[2]));
            return true;
        }

        return false;
    }

    private void importProfile() {
        if (importField == null) {
            return;
        }

        if (!config.importProfile(importField.getText())) {
            return;
        }

        selectedColor = 0;
        syncFields();
        updateColorEditState();
    }

    private void applyGradient() {
        if (!customColorsEnabled()) {
            return;
        }

        if (colorFields.size() < 3) {
            return;
        }

        config.setGradient(colorFields.get(0).getText(), colorFields.get(2).getText());
        syncFields();
    }

    private void copyProfile() {
        if (client == null) {
            return;
        }

        String profile = config.exportProfile();
        client.keyboard.setClipboard(profile);
        if (importField != null) {
            importField.setText(profile);
        }
    }

    private void resetDefaults() {
        config.resetDefaults();
        selectedColor = 0;
        syncFields();
        updateColorEditState();
        if (importField != null) {
            importField.setText("");
        }
    }

    private void syncFields() {
        syncingFields = true;
        for (int index = 0; index < colorFields.size(); index++) {
            colorFields.get(index).setText(colorAt(index));
        }
        syncingFields = false;
    }

    private void setSelectedColor(int color) {
        if (!customColorsEnabled()) {
            return;
        }

        config.setColorDraft(selectedColor, String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF));
        dirtyColorDrag = true;
        if (selectedColor >= 0 && selectedColor < colorFields.size()) {
            syncingFields = true;
            colorFields.get(selectedColor).setText(colorAt(selectedColor));
            syncingFields = false;
            colorFields.get(selectedColor).setFocused(true);
        }
    }

    private int pickerX() {
        return panelX + 428;
    }

    private int pickerY() {
        return panelY + 278;
    }

    private void playMenuSound() {
        if (config.menuSounds && client != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(menuSoundEvent(), menuSoundPitch()));
        }
    }

    private SoundEvent menuSoundEvent() {
        return switch (config.menuSoundStyle) {
            case "crystal" -> SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
            case "xp" -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            case "allay" -> SoundEvents.ENTITY_ALLAY_ITEM_GIVEN;
            case "ender" -> SoundEvents.ENTITY_ENDERMAN_TELEPORT;
            case "blaze" -> SoundEvents.ENTITY_BLAZE_SHOOT;
            case "sculk" -> SoundEvents.BLOCK_SCULK_CATALYST_BLOOM;
            case "evoker" -> SoundEvents.ENTITY_EVOKER_CAST_SPELL;
            default -> SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON;
        };
    }

    private float menuSoundPitch() {
        return switch (config.menuSoundStyle) {
            case "crystal" -> 1.55F;
            case "xp" -> 1.25F;
            case "allay" -> 1.35F;
            case "ender" -> 1.2F;
            case "blaze" -> 1.7F;
            case "sculk" -> 1.45F;
            case "evoker" -> 1.35F;
            default -> 1.0F;
        };
    }

    private static float[] rgbToHsv(int color) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;
        float hue;

        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == red) {
            hue = ((green - blue) / delta) % 6.0F;
        } else if (max == green) {
            hue = ((blue - red) / delta) + 2.0F;
        } else {
            hue = ((red - green) / delta) + 4.0F;
        }

        hue /= 6.0F;
        if (hue < 0.0F) {
            hue += 1.0F;
        }

        float saturation = max == 0.0F ? 0.0F : delta / max;
        return new float[]{hue, saturation, max};
    }

    private Text soundText() {
        return Text.translatable(config.menuSounds ? "screen.brewbloom.sound_on" : "screen.brewbloom.sound_off");
    }

    private Text textureText() {
        return Text.translatable("screen.brewbloom.texture." + config.textureMode);
    }

    private Text soundStyleText() {
        return Text.translatable("screen.brewbloom.sound_value", Text.translatable("screen.brewbloom.sound." + config.menuSoundStyle));
    }

    private Text styleText() {
        return Text.translatable("screen.brewbloom.style_value", Text.translatable("screen.brewbloom.style." + config.particleStyle));
    }

    private Text testModeText() {
        return Text.translatable(config.showWithoutEffects ? "screen.brewbloom.test_on" : "screen.brewbloom.test_off");
    }

    private Text radiusText() {
        return Text.literal(String.format(Locale.ROOT, "%.2f", config.radius));
    }

    private Text percentText(double value) {
        return Text.literal(String.format(Locale.ROOT, "%d%%", Math.round(value * 100.0D)));
    }

    private ButtonWidget button(int x, int y, int width, int height, Text message, ButtonWidget.PressAction pressAction) {
        return new BrewBloomButton(x, y, width, height, message, pressAction, Text.empty());
    }

    private ButtonWidget describedButton(int x, int y, int width, int height, Text message, Text description, ButtonWidget.PressAction pressAction) {
        return new BrewBloomButton(x, y, width, height, message, pressAction, description);
    }

    private void addModeTab(int x, int y, int width, String mode, Text label) {
        addDrawableChild(new ModeTabButton(x, y, width, 22, label, button -> {
            config.setColorMode(mode);
            updateColorEditState();
        }, mode, Text.translatable("screen.brewbloom.desc.mode_" + mode)));
    }

    private boolean customColorsEnabled() {
        return "custom".equals(config.colorMode);
    }

    private void updateColorEditState() {
        boolean enabled = customColorsEnabled();
        for (TextFieldWidget field : colorFields) {
            field.setEditable(enabled);
        }
        if (importField != null) {
            importField.setEditable(enabled);
        }
        for (ButtonWidget widget : manualColorWidgets) {
            widget.active = enabled;
        }
    }

    private String colorAt(int index) {
        if (config.colors == null || index >= config.colors.length || !BrewBloomConfig.isValidHexColor(config.colors[index])) {
            return "#FFFFFF";
        }

        return BrewBloomConfig.normalizeHexColor(config.colors[index]);
    }

    private class BrewBloomButton extends ButtonWidget {
        protected final Text description;

        private BrewBloomButton(int x, int y, int width, int height, Text message, PressAction pressAction, Text description) {
            super(x, y, width, height, message, pressAction, DEFAULT_NARRATION_SUPPLIER);
            this.description = description;
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            if (config.menuSounds) {
                playMenuSound();
            }
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int x = getX();
            int y = getY();
            int right = x + getWidth();
            int bottom = y + getHeight();
            int border = !active ? 0xFF34343A : isHovered() ? RED : 0xFF66666D;
            int fill = !active ? 0xFF08080B : isHovered() ? 0xFF240006 : BLACK;

            context.fill(x, y, right, bottom, border);
            context.fill(x + 1, y + 1, right - 1, bottom - 1, fill);
            context.fill(x + 2, y + 2, right - 2, y + 3, !active ? 0xFF18181D : isHovered() ? RED_HOVER : 0xFF2C2C32);

            if (!getMessage().getString().isEmpty()) {
                int textColor = !active ? 0xFF68686F : isHovered() ? WHITE : 0xFFE6E6EA;
                context.drawCenteredTextWithShadow(textRenderer, getMessage(), x + getWidth() / 2, y + (getHeight() - 8) / 2, textColor);
            }

            if (isHovered() && !description.getString().isEmpty()) {
                hoverDescription = description;
            }
        }
    }

    private final class ModeTabButton extends BrewBloomButton {
        private final String mode;

        private ModeTabButton(int x, int y, int width, int height, Text message, PressAction pressAction, String mode, Text description) {
            super(x, y, width, height, message, pressAction, description);
            this.mode = mode;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int x = getX();
            int y = getY();
            int right = x + getWidth();
            int bottom = y + getHeight();
            boolean selected = mode.equals(config.colorMode);
            int border = selected ? RED : isHovered() ? 0xFFAAAAAA : 0xFF55555A;
            int fill = selected ? 0xFF340008 : isHovered() ? 0xFF1B1B20 : BLACK;

            context.fill(x, y, right, bottom, border);
            context.fill(x + 1, y + 1, right - 1, bottom - 1, fill);
            context.fill(x + 2, y + 2, right - 2, y + 4, selected ? RED : 0xFF2C2C32);
            context.drawCenteredTextWithShadow(textRenderer, getMessage(), x + getWidth() / 2, y + (getHeight() - 8) / 2, selected ? WHITE : 0xFFE6E6EA);
            if (isHovered() && !description.getString().isEmpty()) {
                hoverDescription = description;
            }
        }
    }
}
