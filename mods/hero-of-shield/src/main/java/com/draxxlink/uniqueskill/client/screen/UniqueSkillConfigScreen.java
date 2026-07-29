package com.draxxlink.uniqueskill.client.screen;

import com.draxxlink.uniqueskill.client.UniqueSkillClient;
import com.draxxlink.uniqueskill.client.ui.UniqueSkillVisualTheme;
import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class UniqueSkillConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 404;
    private static final int PANEL_TOP = 42;
    private static final int PANEL_BOTTOM_MARGIN = 36;
    private static final int PANEL_SIDE_PADDING = 12;
    private static final int SECTION_GAP = 12;
    private static final int ROW_HEIGHT = 24;
    private static final int FRAME_TOP = UniqueSkillVisualTheme.PANEL_TOP;
    private static final int FRAME_BOTTOM = UniqueSkillVisualTheme.PANEL_BOTTOM;
    private static final int TEXT_PRIMARY = UniqueSkillVisualTheme.PANEL_TEXT;
    private static final int TEXT_SECONDARY = UniqueSkillVisualTheme.PANEL_MUTED_TEXT;

    private static final List<Double> RANGE_VALUES = List.of(16.0D, 32.0D, 64.0D);
    private static final List<String> OUTLINE_COLOR_VALUES = List.of("CYAN", "GOLD", "MAGENTA", "WHITE");
    private static final List<Float> ECOLOCATION_VOLUME_VALUES = List.of(0.00F, 0.05F, 0.10F, 0.20F, 0.35F, 0.55F, 0.80F, 1.00F);
    private static final List<Float> ALERT_VOLUME_VALUES = List.of(0.00F, 0.05F, 0.10F, 0.20F, 0.35F, 0.55F, 0.80F, 1.00F);
    private static final List<Float> OUTLINE_ALPHA_VALUES = List.of(0.20F, 0.35F, 0.55F, 0.75F);
    private static final List<Integer> AUTO_ATTACK_TOLERANCE_VALUES = List.of(4, 6, 8, 10, 12, 16);

    private final Screen parent;
    private final UniqueSkillConfig draftConfig;

    private ButtonWidget modEnabledButton;
    private ButtonWidget toggleKeyButton;
    private ButtonWidget detectionRangeButton;
    private ButtonWidget showMessagesButton;
    private ButtonWidget showHudButton;
    private ButtonWidget ecolocationVolumeButton;
    private ButtonWidget alertVolumeButton;
    private ButtonWidget blockOutlineButton;
    private ButtonWidget outlineColorButton;
    private ButtonWidget outlineAlphaButton;
    private ButtonWidget blockInspectorButton;
    private ButtonWidget playerWarningsButton;
    private ButtonWidget presenceHudButton;
    private ButtonWidget foodTooltipButton;
    private ButtonWidget autoAttackToleranceButton;
    private ButtonWidget autoAttackHostilesButton;
    private ButtonWidget autoAttackNeutralsButton;
    private ButtonWidget autoAttackPassivesButton;
    private ButtonWidget autoAttackPlayersButton;
    private KeyCaptureTarget keyCaptureTarget = KeyCaptureTarget.NONE;

    public UniqueSkillConfigScreen(Screen parent) {
        super(Text.translatable("screen.unique_skill.config.title"));
        this.parent = parent;
        this.draftConfig = UniqueSkillConfig.getInstance().copy();
    }

    @Override
    protected void init() {
        this.clearChildren();

        int panelWidth = getPanelWidth();
        int panelX = (this.width / 2) - (panelWidth / 2);
        int innerWidth = panelWidth - (PANEL_SIDE_PADDING * 2);
        int columnWidth = (innerWidth - SECTION_GAP) / 2;
        int leftColumnX = panelX + PANEL_SIDE_PADDING;
        int rightColumnX = leftColumnX + columnWidth + SECTION_GAP;
        int y = PANEL_TOP + 24;

        this.modEnabledButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.modEnabled = !draftConfig.modEnabled;
            refreshLabels();
        });
        this.blockInspectorButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.blockInspectorEnabled = !draftConfig.blockInspectorEnabled;
            refreshLabels();
        });
        y += ROW_HEIGHT;

        this.toggleKeyButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            keyCaptureTarget = KeyCaptureTarget.MAIN_TOGGLE;
            refreshLabels();
        });
        this.blockOutlineButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.blockOutlineEnabled = !draftConfig.blockOutlineEnabled;
            refreshLabels();
        });
        y += ROW_HEIGHT;

        this.detectionRangeButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.detectionRange = nextValue(RANGE_VALUES, draftConfig.detectionRange);
            refreshLabels();
        });
        this.outlineColorButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.blockOutlineColor = nextValue(OUTLINE_COLOR_VALUES, draftConfig.blockOutlineColor);
            refreshLabels();
        });
        y += ROW_HEIGHT;

        this.showMessagesButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.showMessages = !draftConfig.showMessages;
            refreshLabels();
        });
        this.outlineAlphaButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.blockOutlineAlpha = nextValue(OUTLINE_ALPHA_VALUES, draftConfig.blockOutlineAlpha);
            refreshLabels();
        });
        y += ROW_HEIGHT;

        this.showHudButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.showHud = !draftConfig.showHud;
            refreshLabels();
        });
        this.playerWarningsButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.showPlayerWarnings = !draftConfig.showPlayerWarnings;
            refreshLabels();
        });
        y += ROW_HEIGHT;

        this.ecolocationVolumeButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.ecolocationVolume = nextValue(ECOLOCATION_VOLUME_VALUES, draftConfig.ecolocationVolume);
            refreshLabels();
        });
        this.presenceHudButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.showPresenceHud = !draftConfig.showPresenceHud;
            refreshLabels();
        });
        y += ROW_HEIGHT;

        this.alertVolumeButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.alertVolume = nextValue(ALERT_VOLUME_VALUES, draftConfig.alertVolume);
            refreshLabels();
        });
        this.foodTooltipButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.showFoodTooltip = !draftConfig.showFoodTooltip;
            refreshLabels();
        });
        this.autoAttackHostilesButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.autoAttackHostiles = !draftConfig.autoAttackHostiles;
            refreshLabels();
        });
        y += ROW_HEIGHT;

        this.autoAttackToleranceButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.autoAttackAimToleranceDegrees = nextValue(AUTO_ATTACK_TOLERANCE_VALUES, draftConfig.autoAttackAimToleranceDegrees);
            refreshLabels();
        });
        this.autoAttackPassivesButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.autoAttackPassives = !draftConfig.autoAttackPassives;
            refreshLabels();
        });
        y += ROW_HEIGHT;

        this.autoAttackNeutralsButton = addOptionButton(leftColumnX, y, columnWidth, () -> {
            draftConfig.autoAttackNeutrals = !draftConfig.autoAttackNeutrals;
            refreshLabels();
        });
        this.autoAttackPlayersButton = addOptionButton(rightColumnX, y, columnWidth, () -> {
            draftConfig.autoAttackPlayers = !draftConfig.autoAttackPlayers;
            refreshLabels();
        });
        y += ROW_HEIGHT;

        int actionY = this.height - 30;
        int actionAreaWidth = panelWidth - (PANEL_SIDE_PADDING * 2);
        int actionGap = 8;
        int resetWidth = 74;
        int primaryWidth = (actionAreaWidth - resetWidth - (actionGap * 2)) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.unique_skill.reset"), button -> resetDraft())
            .dimensions(panelX + PANEL_SIDE_PADDING, actionY, resetWidth, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.unique_skill.done"), button -> saveAndClose())
            .dimensions(panelX + PANEL_SIDE_PADDING + resetWidth + actionGap, actionY, primaryWidth, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.unique_skill.cancel"), button -> close())
            .dimensions(panelX + PANEL_SIDE_PADDING + resetWidth + actionGap + primaryWidth + actionGap, actionY, primaryWidth, 20)
            .build());

        refreshLabels();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCaptureTarget != KeyCaptureTarget.NONE) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                keyCaptureTarget = KeyCaptureTarget.NONE;
            } else {
                draftConfig.toggleKey = keyCode;
                keyCaptureTarget = KeyCaptureTarget.NONE;
            }

            refreshLabels();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = getPanelWidth();
        int panelX = (this.width / 2) - (panelWidth / 2);
        int panelHeight = this.height - PANEL_TOP - PANEL_BOTTOM_MARGIN;

        UniqueSkillVisualTheme.drawArcaneBackdrop(context, this.width, this.height);
        drawPanelFrame(context, panelX, PANEL_TOP, panelWidth, panelHeight, FRAME_TOP, FRAME_BOTTOM);
        context.fill(panelX + 12, PANEL_TOP + 28, panelX + panelWidth - 12, PANEL_TOP + 29, UniqueSkillVisualTheme.PANEL_ACCENT);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 18, TEXT_PRIMARY);
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("screen.unique_skill.config.subtitle"),
            this.width / 2,
            32,
            TEXT_SECONDARY
        );
    }

    private ButtonWidget addOptionButton(int x, int y, int width, Runnable action) {
        return this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> action.run())
            .dimensions(x, y, width, 20)
            .build());
    }

    private void saveAndClose() {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        config.copyFrom(draftConfig);
        config.save();
        UniqueSkillClient.applyRuntimeConfig(MinecraftClient.getInstance());
        close();
    }

    private void resetDraft() {
        draftConfig.resetToDefaults();
        keyCaptureTarget = KeyCaptureTarget.NONE;
        refreshLabels();
    }

    private void refreshLabels() {
        this.modEnabledButton.setMessage(optionText("screen.unique_skill.option.mod_enabled", booleanText(draftConfig.modEnabled)));
        this.toggleKeyButton.setMessage(optionText(
            "screen.unique_skill.option.toggle_key",
            keyCaptureTarget == KeyCaptureTarget.MAIN_TOGGLE
                ? Text.translatable("screen.unique_skill.press_key")
                : getKeyText(draftConfig.toggleKey)
        ));
        this.detectionRangeButton.setMessage(optionText(
            "screen.unique_skill.option.detection_range",
            Text.translatable("screen.unique_skill.value.blocks", formatDouble(draftConfig.detectionRange))
        ));
        this.showMessagesButton.setMessage(optionText("screen.unique_skill.option.show_messages", booleanText(draftConfig.showMessages)));
        this.showHudButton.setMessage(optionText("screen.unique_skill.option.show_hud", booleanText(draftConfig.showHud)));
        this.ecolocationVolumeButton.setMessage(optionText(
            "screen.unique_skill.option.ecolocation_volume",
            Text.translatable("screen.unique_skill.value.percent", Math.round(draftConfig.ecolocationVolume * 100.0F))
        ));
        this.alertVolumeButton.setMessage(optionText(
            "screen.unique_skill.option.alert_volume",
            Text.translatable("screen.unique_skill.value.percent", Math.round(draftConfig.alertVolume * 100.0F))
        ));
        this.blockOutlineButton.setMessage(optionText("screen.unique_skill.option.block_outline", booleanText(draftConfig.blockOutlineEnabled)));
        this.outlineColorButton.setMessage(optionText(
            "screen.unique_skill.option.outline_color",
            Text.translatable("screen.unique_skill.value.outline_color." + draftConfig.blockOutlineColor.toLowerCase())
        ));
        this.outlineAlphaButton.setMessage(optionText(
            "screen.unique_skill.option.outline_alpha",
            Text.translatable("screen.unique_skill.value.percent", Math.round(draftConfig.blockOutlineAlpha * 100.0F))
        ));
        this.blockInspectorButton.setMessage(optionText("screen.unique_skill.option.block_inspector", booleanText(draftConfig.blockInspectorEnabled)));
        this.playerWarningsButton.setMessage(optionText("screen.unique_skill.option.player_warnings", booleanText(draftConfig.showPlayerWarnings)));
        this.presenceHudButton.setMessage(optionText("screen.unique_skill.option.presence_hud", booleanText(draftConfig.showPresenceHud)));
        this.foodTooltipButton.setMessage(optionText("screen.unique_skill.option.food_tooltip", booleanText(draftConfig.showFoodTooltip)));
        this.autoAttackToleranceButton.setMessage(optionText(
            "screen.unique_skill.option.auto_attack_tolerance",
            Text.translatable("screen.unique_skill.value.degrees", draftConfig.autoAttackAimToleranceDegrees)
        ));
        this.autoAttackHostilesButton.setMessage(optionText("screen.unique_skill.option.auto_attack_hostiles", booleanText(draftConfig.autoAttackHostiles)));
        this.autoAttackNeutralsButton.setMessage(optionText("screen.unique_skill.option.auto_attack_neutrals", booleanText(draftConfig.autoAttackNeutrals)));
        this.autoAttackPassivesButton.setMessage(optionText("screen.unique_skill.option.auto_attack_passives", booleanText(draftConfig.autoAttackPassives)));
        this.autoAttackPlayersButton.setMessage(optionText("screen.unique_skill.option.auto_attack_players", booleanText(draftConfig.autoAttackPlayers)));
    }

    private Text optionText(String labelKey, Text value) {
        return Text.translatable(labelKey).append(Text.literal(": ")).append(value);
    }

    private Text booleanText(boolean value) {
        return Text.translatable(value ? "hud.unique_skill.on" : "hud.unique_skill.off");
    }

    private Text getKeyText(int keyCode) {
        return InputUtil.Type.KEYSYM.createFromCode(keyCode).getLocalizedText();
    }

    private static double nextValue(List<Double> values, double current) {
        int index = values.indexOf(current);
        return values.get((index + 1) % values.size());
    }

    private static String nextValue(List<String> values, String current) {
        int index = values.indexOf(current);
        return values.get((index + 1) % values.size());
    }

    private static float nextValue(List<Float> values, float current) {
        int index = indexOfClosest(values, current);
        return values.get((index + 1) % values.size());
    }

    private static int nextValue(List<Integer> values, int current) {
        int index = values.indexOf(current);
        return values.get((index + 1) % values.size());
    }

    private static String formatDouble(double value) {
        return value == Math.rint(value) ? Integer.toString((int) value) : Double.toString(value);
    }

    private static int indexOfClosest(List<Float> values, float current) {
        int bestIndex = 0;
        float bestDistance = Math.abs(values.getFirst() - current);
        for (int index = 1; index < values.size(); index++) {
            float distance = Math.abs(values.get(index) - current);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = index;
            }
        }
        return bestIndex;
    }

    private int getPanelWidth() {
        return Math.min(PANEL_WIDTH, this.width - 20);
    }

    private void drawPanelFrame(DrawContext context, int x, int y, int width, int height, int topColor, int bottomColor) {
        UniqueSkillVisualTheme.drawArcanePanel(context, x, y, width, height);
    }

    private enum KeyCaptureTarget {
        NONE,
        MAIN_TOGGLE
    }
}
