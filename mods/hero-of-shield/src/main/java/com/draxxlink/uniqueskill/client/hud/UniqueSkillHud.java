package com.draxxlink.uniqueskill.client.hud;

import com.draxxlink.uniqueskill.client.combat.UniqueSkillCombatTracker;
import com.draxxlink.uniqueskill.client.inventory.UniqueSkillInventoryOverlay;
import com.draxxlink.uniqueskill.client.ui.UniqueSkillVisualTheme;
import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import com.draxxlink.uniqueskill.state.UniqueSkillState;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public final class UniqueSkillHud {
    private static final Identifier HUD_ID = Identifier.of("unique_skill", "status_hud");
    private static final int MARGIN = 8;
    private static final int LINE_HEIGHT = 13;
    private static final int PANEL_TOP = UniqueSkillVisualTheme.PANEL_TOP;
    private static final int PANEL_BOTTOM = UniqueSkillVisualTheme.PANEL_BOTTOM;
    private static final int PANEL_TEXT = UniqueSkillVisualTheme.PANEL_TEXT;
    private static final int PANEL_MUTED_TEXT = UniqueSkillVisualTheme.PANEL_MUTED_TEXT;
    private static final int PANEL_HOT_TEXT = UniqueSkillVisualTheme.PANEL_HOT_TEXT;
    private static final int PANEL_COOL_TEXT = UniqueSkillVisualTheme.PANEL_SOFT_TEXT;
    private static final int PANEL_ALERT_TEXT = UniqueSkillVisualTheme.PANEL_ALERT_TEXT;
    private static final int PANEL_SOFT_TEXT = UniqueSkillVisualTheme.PANEL_SOFT_TEXT;
    private static final int TELEMETRY_PANEL_X = MARGIN + 2;
    private static final int TELEMETRY_PANEL_Y = MARGIN + 4;
    private static final int TELEMETRY_PANEL_HEIGHT = 102;
    private static final int PANEL_GAP = 8;
    private static final int BLOCK_INSPECTOR_TEXT_START_Y = 24;
    private static final int BLOCK_INSPECTOR_LINE_HEIGHT = 11;
    private static final int BLOCK_INSPECTOR_BOTTOM_PADDING = 8;
    private static final int UNIQUE_SKILL_SECTION_HEADER_HEIGHT = 24;
    private static final int UNIQUE_SKILL_ITEM_HEIGHT = 58;
    private static final int UNIQUE_SKILL_EMPTY_HEIGHT = 34;
    private static final int UNIQUE_SKILL_PANEL_MIN_WIDTH = 176;
    private static final int BREEDING_PANEL_HEADER_HEIGHT = 24;
    private static final int BREEDING_PANEL_SECTION_HEIGHT = 66;
    private static final int BREEDING_PANEL_MIN_WIDTH = 188;
    private static final int RIGHT_PANEL_MARGIN = 8;
    private static final int INDICATOR_HOT = 0xFFFF726D;
    private static final int INDICATOR_SOFT = 0xFFFFB09A;

    private UniqueSkillHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(HUD_ID, UniqueSkillHud::render);
    }

    private static void render(DrawContext drawContext, RenderTickCounter tickCounter) {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        if (!config.modEnabled || !UniqueSkillState.isEnabled() || !config.showHud) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        renderTelemetryPanel(drawContext, textRenderer, client);
        renderBlockInspector(drawContext, textRenderer, client);
        renderUniqueSkillPanel(drawContext, textRenderer, client);
        renderAnimalBreedingPanel(drawContext, textRenderer, client);
        UniqueSkillFurnaceOverlay.render(drawContext, textRenderer, client);
        UniqueSkillInventoryOverlay.render(drawContext, textRenderer, client);
        renderPlayerWarnings(drawContext, textRenderer, client);
        renderEntityStatusOrbs(drawContext, textRenderer);
        renderDirectionalIndicators(drawContext, textRenderer);
    }

    private static void renderTelemetryPanel(DrawContext drawContext, TextRenderer textRenderer, MinecraftClient client) {
        BlockPos playerPos = client.player.getBlockPos();
        int fps = client.getCurrentFps();
        int worldTime = (int) (client.world.getTimeOfDay() % 24000L);
        int hours = ((worldTime / 1000) + 6) % 24;
        int minutes = (int) ((worldTime % 1000) * 0.06D);
        Text biomeName = getBiomeName(client, playerPos);
        Text moonPhase = getMoonPhaseLabel(client);
        Text weatherText = getWeatherText(client, playerPos);

        String fpsLabel = Text.translatable("hud.unique_skill.telemetry.fps_label").getString();
        String speedLabel = Text.translatable("hud.unique_skill.telemetry.speed_label").getString();
        String xyzLabel = Text.translatable("hud.unique_skill.telemetry.coords_label").getString();
        String biomeLabel = Text.translatable("hud.unique_skill.telemetry.biome_label").getString();
        String weatherLabel = Text.translatable("hud.unique_skill.telemetry.weather_label").getString();
        String timeLabel = Text.translatable("hud.unique_skill.telemetry.time_label").getString();
        String speedValue = getSpeedText(client.player.getVelocity());

        int x = TELEMETRY_PANEL_X;
        int y = TELEMETRY_PANEL_Y;
        int boxWidth = Math.max(
            144,
            58 + Math.max(
                Math.max(textRenderer.getWidth(biomeName), textRenderer.getWidth(speedValue) + 18),
                Math.max(textRenderer.getWidth(weatherText), textRenderer.getWidth(moonPhase) + 54)
            )
        );
        int boxHeight = TELEMETRY_PANEL_HEIGHT;

        drawAuraPanel(drawContext, x, y, boxWidth, boxHeight, PANEL_TOP, PANEL_BOTTOM);

        drawTelemetryTile(drawContext, x + 6, y + 6, new ItemStack(Items.CLOCK));
        drawTelemetryTile(drawContext, x + 31, y + 6, getBiomeIcon(client, playerPos));
        drawTelemetryTile(drawContext, x + 56, y + 6, getWeatherIcon(client, playerPos));

        int lineX = x + 8;
        int lineY = y + 31;
        int labelColor = PANEL_MUTED_TEXT;

        drawContext.drawText(textRenderer, fpsLabel, lineX, lineY, labelColor, false);
        drawContext.drawText(textRenderer, Integer.toString(fps), lineX + 28, lineY, PANEL_TEXT, false);

        int speedY = lineY + 12;
        drawContext.drawText(textRenderer, speedLabel, lineX, speedY, labelColor, false);
        drawContext.drawText(textRenderer, speedValue, lineX + 28, speedY, PANEL_ALERT_TEXT, false);

        int xyzY = speedY + 12;
        drawContext.drawText(textRenderer, xyzLabel, lineX, xyzY, labelColor, false);
        int valueX = lineX + 28;
        String xValue = Integer.toString(playerPos.getX());
        String yValue = Integer.toString(playerPos.getY());
        String zValue = Integer.toString(playerPos.getZ());
        drawContext.drawText(textRenderer, xValue, valueX, xyzY, PANEL_HOT_TEXT, false);
        valueX += textRenderer.getWidth(xValue) + 4;
        drawContext.drawText(textRenderer, yValue, valueX, xyzY, PANEL_TEXT, false);
        valueX += textRenderer.getWidth(yValue) + 4;
        drawContext.drawText(textRenderer, zValue, valueX, xyzY, 0xFFFFC1C1, false);

        int biomeY = xyzY + 12;
        drawContext.drawText(textRenderer, biomeLabel, lineX, biomeY, labelColor, false);
        drawContext.drawText(textRenderer, biomeName, lineX + 38, biomeY, PANEL_TEXT, false);

        int weatherY = biomeY + 12;
        drawContext.drawText(textRenderer, weatherLabel, lineX, weatherY, labelColor, false);
        drawContext.drawText(textRenderer, weatherText, lineX + 42, weatherY, 0xFFFFB3B3, false);

        int timeY = weatherY + 12;
        drawContext.drawText(textRenderer, timeLabel, lineX, timeY, labelColor, false);
        drawContext.drawText(textRenderer, String.format("%02d:%02d", hours, minutes), lineX + 34, timeY, PANEL_ALERT_TEXT, false);
        drawContext.drawText(textRenderer, moonPhase, lineX + 72, timeY, PANEL_SOFT_TEXT, false);
    }

    private static void renderBlockInspector(DrawContext drawContext, TextRenderer textRenderer, MinecraftClient client) {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        if (!config.blockInspectorEnabled) {
            return;
        }

        UniqueSkillBlockInspector.BlockInspectorData data = UniqueSkillBlockInspector.inspect(client);
        if (data == null) {
            return;
        }

        int width = drawContext.getScaledWindowWidth();
        int contentWidth = textRenderer.getWidth(data.title());
        for (UniqueSkillBlockInspector.StyledLine line : data.lines()) {
            contentWidth = Math.max(contentWidth, textRenderer.getWidth(line.text()));
        }

        int boxWidth = Math.min(width - 32, Math.max(150, contentWidth + 22));
        int boxHeight = BLOCK_INSPECTOR_TEXT_START_Y
            + (data.lines().size() * BLOCK_INSPECTOR_LINE_HEIGHT)
            + BLOCK_INSPECTOR_BOTTOM_PADDING;
        int x = TELEMETRY_PANEL_X;
        int y = TELEMETRY_PANEL_Y + TELEMETRY_PANEL_HEIGHT + PANEL_GAP;

        drawAuraPanel(drawContext, x, y, boxWidth, boxHeight, PANEL_TOP, PANEL_BOTTOM);
        drawContext.drawItemWithoutEntity(data.iconStack(), x + 5, y + 4);
        drawContext.drawText(textRenderer, data.title(), x + 25, y + 8, PANEL_TEXT, false);

        for (int i = 0; i < data.lines().size(); i++) {
            UniqueSkillBlockInspector.StyledLine line = data.lines().get(i);
            int lineY = y + BLOCK_INSPECTOR_TEXT_START_Y + (i * BLOCK_INSPECTOR_LINE_HEIGHT);
            drawContext.drawText(textRenderer, line.text(), x + 5, lineY, line.color(), false);
        }
    }

    private static void renderUniqueSkillPanel(DrawContext drawContext, TextRenderer textRenderer, MinecraftClient client) {
        long worldTick = client.world == null ? 0L : client.world.getTime();
        UniqueSkillCombatTracker.WeaponStatsView mainHandView = UniqueSkillCombatTracker.getView(client.player, Hand.MAIN_HAND, worldTick);

        int boxWidth = getUniqueSkillPanelWidth(textRenderer, mainHandView);
        int boxHeight = getUniqueSkillPanelHeight(mainHandView);
        int x = getRightPanelX(drawContext, boxWidth);
        int y = getRightColumnTop(client, drawContext, boxHeight);

        drawAuraPanel(drawContext, x, y, boxWidth, boxHeight, PANEL_TOP, PANEL_BOTTOM);
        drawContext.drawText(
            textRenderer,
            Text.translatable("hud.unique_skill.unique_skill.title"),
            x + 8,
            y + 6,
            PANEL_TEXT,
            false
        );
        drawContext.drawText(
            textRenderer,
            Text.translatable("hud.unique_skill.unique_skill.subtitle"),
            x + 8,
            y + 15,
            PANEL_MUTED_TEXT,
            false
        );

        int sectionY = y + UNIQUE_SKILL_SECTION_HEADER_HEIGHT;
        renderUniqueSkillSection(drawContext, textRenderer, x + 6, sectionY, boxWidth - 12, mainHandView, 0xFFE37272);
    }

    private static void renderAnimalBreedingPanel(DrawContext drawContext, TextRenderer textRenderer, MinecraftClient client) {
        UniqueSkillEntityInspector.BreedingInsight insight = UniqueSkillEntityInspector.inspectBreedingTarget(client);
        if (insight == null) {
            return;
        }

        long worldTick = client.world == null ? 0L : client.world.getTime();
        UniqueSkillCombatTracker.WeaponStatsView mainHandView = UniqueSkillCombatTracker.getView(client.player, Hand.MAIN_HAND, worldTick);
        int weaponHeight = getUniqueSkillPanelHeight(mainHandView);
        int boxWidth = getBreedingPanelWidth(textRenderer, insight);
        int boxHeight = BREEDING_PANEL_HEADER_HEIGHT + BREEDING_PANEL_SECTION_HEIGHT + 8;
        int x = getRightPanelX(drawContext, boxWidth);
        int y = getRightColumnTop(client, drawContext, weaponHeight) + weaponHeight + PANEL_GAP;
        y = Math.min(y, Math.max(8, drawContext.getScaledWindowHeight() - boxHeight - 8));

        drawAuraPanel(drawContext, x, y, boxWidth, boxHeight, PANEL_TOP, PANEL_BOTTOM);
        drawContext.drawText(
            textRenderer,
            Text.translatable("hud.unique_skill.breeding.title"),
            x + 8,
            y + 6,
            PANEL_TEXT,
            false
        );
        drawContext.drawText(textRenderer, insight.animalName(), x + 8, y + 15, PANEL_MUTED_TEXT, false);

        int sectionY = y + BREEDING_PANEL_HEADER_HEIGHT;
        UniqueSkillVisualTheme.drawSectionBox(drawContext, x + 6, sectionY, boxWidth - 12, BREEDING_PANEL_SECTION_HEIGHT, insight.accentColor());
        drawContext.drawItemWithoutEntity(insight.foodIcon(), x + 12, sectionY + 7);
        drawContext.drawText(textRenderer, insight.statusText(), x + 32, sectionY + 6, insight.accentColor(), false);
        drawContext.drawText(textRenderer, insight.timerText(), x + 32, sectionY + 16, PANEL_SOFT_TEXT, false);
        drawContext.drawText(textRenderer, insight.growthText(), x + 12, sectionY + 29, PANEL_HOT_TEXT, false);
        drawContext.drawText(textRenderer, insight.foodText(), x + 12, sectionY + 42, PANEL_TEXT, false);
        drawContext.drawText(textRenderer, insight.handText(), x + 12, sectionY + 52, PANEL_MUTED_TEXT, false);
    }

    private static int getRightPanelX(DrawContext drawContext, int boxWidth) {
        return Math.max(RIGHT_PANEL_MARGIN, drawContext.getScaledWindowWidth() - boxWidth - RIGHT_PANEL_MARGIN);
    }

    private static int getRightColumnTop(
        MinecraftClient client,
        DrawContext drawContext,
        int panelHeight
    ) {
        int topAnchor = 8;
        int inventoryBottom = getInventoryOverlayBottom(client);
        if (inventoryBottom > 0) {
            topAnchor = Math.max(topAnchor, inventoryBottom + PANEL_GAP);
        }

        int warningsBottom = getWarningsOverlayBottom(client);
        if (warningsBottom > 0) {
            topAnchor = Math.max(topAnchor, warningsBottom + PANEL_GAP);
        }

        return Math.min(topAnchor, Math.max(8, drawContext.getScaledWindowHeight() - panelHeight - 8));
    }

    private static int getUniqueSkillPanelWidth(TextRenderer textRenderer, UniqueSkillCombatTracker.WeaponStatsView view) {
        return Math.max(
            UNIQUE_SKILL_PANEL_MIN_WIDTH,
            getUniqueSkillSectionWidth(textRenderer, view) + 14
        );
    }

    private static int getUniqueSkillPanelHeight(UniqueSkillCombatTracker.WeaponStatsView view) {
        return UNIQUE_SKILL_SECTION_HEADER_HEIGHT + getUniqueSkillSectionHeight(view) + 8;
    }

    private static int getBreedingPanelWidth(TextRenderer textRenderer, UniqueSkillEntityInspector.BreedingInsight insight) {
        int contentWidth = textRenderer.getWidth(insight.animalName());
        contentWidth = Math.max(contentWidth, textRenderer.getWidth(insight.statusText()) + 20);
        contentWidth = Math.max(contentWidth, textRenderer.getWidth(insight.timerText()) + 20);
        contentWidth = Math.max(contentWidth, textRenderer.getWidth(insight.growthText()) + 20);
        contentWidth = Math.max(contentWidth, textRenderer.getWidth(insight.foodText()));
        contentWidth = Math.max(contentWidth, textRenderer.getWidth(insight.handText()) + 20);
        return Math.max(BREEDING_PANEL_MIN_WIDTH, contentWidth + 24);
    }

    private static int getInventoryOverlayBottom(MinecraftClient client) {
        if (!(client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen)) {
            return -1;
        }
        if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.CraftingScreen
            || client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.SmithingScreen
            || client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.CartographyTableScreen
            || client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen<?>) {
            return -1;
        }
        return 8 + 86;
    }

    private static int getWarningsOverlayBottom(MinecraftClient client) {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        if (!config.showPlayerWarnings || client.player == null) {
            return -1;
        }

        List<WarningLine> warnings = collectWarnings(client);
        if (warnings.isEmpty()) {
            return -1;
        }

        return 8 + 10 + (warnings.size() * 11);
    }

    private static int getUniqueSkillSectionWidth(TextRenderer textRenderer, UniqueSkillCombatTracker.WeaponStatsView view) {
        if (isEmptyWeaponView(view)) {
            int contentWidth = Math.max(
                textRenderer.getWidth(view.handLabel()),
                textRenderer.getWidth(view.weaponName())
            );
            contentWidth = Math.max(contentWidth, textRenderer.getWidth(Text.translatable("hud.unique_skill.unique_skill.empty_hint")));
            return contentWidth + 34;
        }

        int contentWidth = Math.max(
            textRenderer.getWidth(view.handLabel()),
            textRenderer.getWidth(view.weaponName())
        );
        int leftColumnWidth = Math.max(
            Math.max(
                getMetricLineWidth(textRenderer, "hud.unique_skill.unique_skill.total_hits_compact", Integer.toString(view.totalHits())),
                getMetricLineWidth(textRenderer, "hud.unique_skill.unique_skill.crit_chance_compact", String.format("%.1f%%", view.critChance()))
            ),
            getMetricLineWidth(textRenderer, "hud.unique_skill.unique_skill.time_since_crit_compact", view.timeSinceLastCrit())
        );
        int rightColumnWidth = Math.max(
            Math.max(
                getMetricLineWidth(textRenderer, "hud.unique_skill.unique_skill.critical_hits_compact", Integer.toString(view.criticalHits())),
                getMetricLineWidth(textRenderer, "hud.unique_skill.unique_skill.dps_compact", String.format("%.1f", view.dps()))
            ),
            getMetricLineWidth(textRenderer, "hud.unique_skill.unique_skill.dry_streak_compact", Integer.toString(view.dryStreak()))
        );
        contentWidth = Math.max(contentWidth, leftColumnWidth + rightColumnWidth + 18);
        contentWidth = Math.max(
            contentWidth,
            getMetricLineWidth(textRenderer, "hud.unique_skill.unique_skill.avg_hits_between_crits_compact", view.averageHitsBetweenCrits())
        );
        return contentWidth + 24;
    }

    private static int getMetricLineWidth(TextRenderer textRenderer, String translationKey, String value) {
        return textRenderer.getWidth(Text.translatable(translationKey, value));
    }

    private static void renderUniqueSkillSection(
        DrawContext drawContext,
        TextRenderer textRenderer,
        int x,
        int y,
        int width,
        UniqueSkillCombatTracker.WeaponStatsView view,
        int accentColor
    ) {
        int sectionHeight = getUniqueSkillSectionHeight(view);
        if (isEmptyWeaponView(view)) {
            int contentX = x + 8;
            drawContext.fill(x + 2, y + 1, x + width - 2, y + 2, 0x44A85E3E);
            drawContext.drawText(textRenderer, view.handLabel(), contentX, y + 5, accentColor, false);
            drawContext.drawText(
                textRenderer,
                Text.translatable("hud.unique_skill.unique_skill.empty_hint"),
                contentX,
                y + 17,
                PANEL_MUTED_TEXT,
                false
            );
            return;
        }

        UniqueSkillVisualTheme.drawSectionBox(drawContext, x, y, width, sectionHeight, accentColor);

        drawContext.drawItemWithoutEntity(view.iconStack(), x + 6, y + 5);
        drawContext.drawText(textRenderer, view.handLabel(), x + 26, y + 5, accentColor, false);
        drawContext.drawText(textRenderer, view.weaponName(), x + 26, y + 15, PANEL_TEXT, false);

        int leftX = x + 6;
        int rightX = x + (width / 2) + 2;
        int lineY = y + 28;
        drawMetricLine(drawContext, textRenderer, leftX, lineY, "hud.unique_skill.unique_skill.total_hits_compact", Integer.toString(view.totalHits()), PANEL_TEXT);
        drawMetricLine(drawContext, textRenderer, rightX, lineY, "hud.unique_skill.unique_skill.critical_hits_compact", Integer.toString(view.criticalHits()), PANEL_ALERT_TEXT);
        drawMetricLine(
            drawContext,
            textRenderer,
            leftX,
            lineY + 9,
            "hud.unique_skill.unique_skill.crit_chance_compact",
            String.format("%.1f%%", view.critChance()),
            accentColor
        );
        drawMetricLine(drawContext, textRenderer, rightX, lineY + 9, "hud.unique_skill.unique_skill.dps_compact", String.format("%.1f", view.dps()), PANEL_SOFT_TEXT);
        drawMetricLine(drawContext, textRenderer, leftX, lineY + 18, "hud.unique_skill.unique_skill.time_since_crit_compact", view.timeSinceLastCrit(), 0xFFFFC7C7);
        drawMetricLine(drawContext, textRenderer, rightX, lineY + 18, "hud.unique_skill.unique_skill.dry_streak_compact", Integer.toString(view.dryStreak()), 0xFFFFE1A6);
        drawMetricLine(
            drawContext,
            textRenderer,
            leftX,
            lineY + 27,
            "hud.unique_skill.unique_skill.avg_hits_between_crits_compact",
            view.averageHitsBetweenCrits(),
            PANEL_MUTED_TEXT
        );
    }

    private static int getUniqueSkillSectionHeight(UniqueSkillCombatTracker.WeaponStatsView view) {
        return isEmptyWeaponView(view) ? UNIQUE_SKILL_EMPTY_HEIGHT : UNIQUE_SKILL_ITEM_HEIGHT;
    }

    private static boolean isEmptyWeaponView(UniqueSkillCombatTracker.WeaponStatsView view) {
        return view.iconStack().isEmpty() && !view.hasCombatData();
    }

    private static void drawMetricLine(
        DrawContext drawContext,
        TextRenderer textRenderer,
        int x,
        int y,
        String translationKey,
        String value,
        int color
    ) {
        drawContext.drawText(textRenderer, Text.translatable(translationKey, value), x, y, color, false);
    }

    private static void renderEntityStatusOrbs(DrawContext drawContext, TextRenderer textRenderer) {
        if (!UniqueSkillConfig.getInstance().showPresenceHud) {
            return;
        }

        int orbSize = 10;
        int spacing = 12;
        int orbCount = 4;
        int contentWidth = (orbSize * orbCount) + (spacing * (orbCount - 1));
        int panelWidth = contentWidth + 24;
        int panelHeight = 58;
        int panelX = 10;
        int panelY = drawContext.getScaledWindowHeight() - panelHeight - 10;
        int baseX = panelX + 12;
        int orbY = panelY + 18;
        long worldTick = MinecraftClient.getInstance().world == null ? 0L : MinecraftClient.getInstance().world.getTime();
        boolean pulseActive = worldTick < UniqueSkillState.getPerceptionFeedbackUntilTick();
        int hostileCount = UniqueSkillState.getNearbyHostileMobCount();
        int neutralCount = UniqueSkillState.getNearbyNeutralMobCount();
        int playerCount = UniqueSkillState.getNearbyPlayerCount();
        int spectatorCount = UniqueSkillState.getNearbySpectatorCount();

        UniqueSkillVisualTheme.drawSectionBox(
            drawContext,
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            UniqueSkillVisualTheme.PANEL_GOLD_SOFT
        );
        drawContext.drawText(
            textRenderer,
            Text.translatable("hud.unique_skill.panel.presence"),
            panelX + 8,
            panelY + 4,
            PANEL_TEXT,
            false
        );

        drawOrb(
            drawContext,
            baseX,
            orbY,
            orbSize,
            hostileCount > 0 ? 0xFFE04545 : 0xFFF2E5C8,
            pulseActive
        );
        drawPresenceCount(drawContext, textRenderer, baseX + (orbSize / 2), orbY + orbSize + 4, hostileCount, 0xFFE04545);
        drawPresenceLabel(drawContext, textRenderer, baseX + (orbSize / 2), orbY + orbSize + 14, "H", hostileCount > 0 ? 0xFFFFB0B0 : PANEL_MUTED_TEXT);
        drawOrb(
            drawContext,
            baseX + orbSize + spacing,
            orbY,
            orbSize,
            neutralCount > 0 ? 0xFF68D878 : 0xFFF2E5C8,
            pulseActive
        );
        drawPresenceCount(
            drawContext,
            textRenderer,
            baseX + orbSize + spacing + (orbSize / 2),
            orbY + orbSize + 4,
            neutralCount,
            0xFF68D878
        );
        drawPresenceLabel(
            drawContext,
            textRenderer,
            baseX + orbSize + spacing + (orbSize / 2),
            orbY + orbSize + 14,
            "N",
            neutralCount > 0 ? 0xFFBDF2C4 : PANEL_MUTED_TEXT
        );
        drawOrb(
            drawContext,
            baseX + ((orbSize + spacing) * 2),
            orbY,
            orbSize,
            playerCount > 0 ? 0xFFE2B654 : 0xFFF2E5C8,
            pulseActive
        );
        drawPresenceCount(
            drawContext,
            textRenderer,
            baseX + ((orbSize + spacing) * 2) + (orbSize / 2),
            orbY + orbSize + 4,
            playerCount,
            0xFFE2B654
        );
        drawPresenceLabel(
            drawContext,
            textRenderer,
            baseX + ((orbSize + spacing) * 2) + (orbSize / 2),
            orbY + orbSize + 14,
            "P",
            playerCount > 0 ? 0xFFF5DC97 : PANEL_MUTED_TEXT
        );
        drawOrb(
            drawContext,
            baseX + ((orbSize + spacing) * 3),
            orbY,
            orbSize,
            spectatorCount > 0 ? 0xFFD18BFF : 0xFFF2E5C8,
            pulseActive
        );
        drawPresenceCount(
            drawContext,
            textRenderer,
            baseX + ((orbSize + spacing) * 3) + (orbSize / 2),
            orbY + orbSize + 4,
            spectatorCount,
            0xFFD18BFF
        );
        drawPresenceLabel(
            drawContext,
            textRenderer,
            baseX + ((orbSize + spacing) * 3) + (orbSize / 2),
            orbY + orbSize + 14,
            "T",
            spectatorCount > 0 ? 0xFFF0C8FF : PANEL_MUTED_TEXT
        );
    }

    private static void drawOrb(
        DrawContext drawContext,
        int x,
        int y,
        int orbSize,
        int baseFillColor,
        boolean pulseActive
    ) {
        int borderColor = pulseActive ? 0xEED8C47B : 0xCC5A3715;
        int fillColor = pulseActive ? brighten(baseFillColor) : baseFillColor;
        int highlightColor = pulseActive ? 0xFFFFF3CC : 0xFFFDF6E8;

        drawContext.fill(x + 2, y, x + orbSize - 2, y + 1, borderColor);
        drawContext.fill(x + 1, y + 1, x + orbSize - 1, y + 2, borderColor);
        drawContext.fill(x, y + 2, x + orbSize, y + orbSize - 2, borderColor);
        drawContext.fill(x + 1, y + orbSize - 2, x + orbSize - 1, y + orbSize - 1, borderColor);
        drawContext.fill(x + 2, y + orbSize - 1, x + orbSize - 2, y + orbSize, borderColor);

        drawContext.fill(x + 2, y + 1, x + orbSize - 2, y + 2, fillColor);
        drawContext.fill(x + 1, y + 2, x + orbSize - 1, y + orbSize - 2, fillColor);
        drawContext.fill(x + 2, y + orbSize - 2, x + orbSize - 2, y + orbSize - 1, fillColor);
        drawContext.fill(x + 2, y + 2, x + 4, y + 4, highlightColor);
    }

    private static void drawPresenceCount(
        DrawContext drawContext,
        TextRenderer textRenderer,
        int centerX,
        int y,
        int count,
        int activeColor
    ) {
        String label = Integer.toString(count);
        int color = count > 0 ? activeColor : PANEL_MUTED_TEXT;
        drawContext.drawText(textRenderer, label, centerX - (textRenderer.getWidth(label) / 2), y, color, false);
    }

    private static void drawPresenceLabel(
        DrawContext drawContext,
        TextRenderer textRenderer,
        int centerX,
        int y,
        String label,
        int color
    ) {
        drawContext.drawText(textRenderer, label, centerX - (textRenderer.getWidth(label) / 2), y, color, false);
    }

    private static void renderDirectionalIndicators(DrawContext drawContext, TextRenderer textRenderer) {
        int width = drawContext.getScaledWindowWidth();
        int height = drawContext.getScaledWindowHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int edgeInset = 20;

        drawIndicator(drawContext, textRenderer, UniqueSkillState.getOffscreenThreatTopCount(), "▲", "CIMA", centerX, edgeInset + 6, INDICATOR_HOT);
        drawIndicator(drawContext, textRenderer, UniqueSkillState.getOffscreenThreatBottomCount(), "▼", "BAIXO", centerX, height - edgeInset - 22, INDICATOR_HOT);
        drawIndicator(drawContext, textRenderer, UniqueSkillState.getOffscreenThreatLeftCount(), "◀", "ESQ", edgeInset + 20, centerY - 6, INDICATOR_SOFT);
        drawIndicator(drawContext, textRenderer, UniqueSkillState.getOffscreenThreatRightCount(), "▶", "DIR", width - edgeInset - 20, centerY - 6, INDICATOR_SOFT);
        drawIndicator(drawContext, textRenderer, UniqueSkillState.getOffscreenThreatBackCount(), "◆", "ATRAS", centerX, height - 76, 0xFFFFD08E);
    }

    private static void drawIndicator(
        DrawContext drawContext,
        TextRenderer textRenderer,
        int count,
        String glyph,
        String label,
        int x,
        int y,
        int color
    ) {
        if (count <= 0) {
            return;
        }

        String countLabel = Integer.toString(count);
        int glyphWidth = textRenderer.getWidth(glyph);
        int labelWidth = textRenderer.getWidth(label);
        int countWidth = textRenderer.getWidth(countLabel);
        int boxWidth = Math.max(36, Math.max(glyphWidth, labelWidth) + 16);
        int countBoxWidth = Math.max(16, countWidth + 8);
        int countX = x - (countBoxWidth / 2);
        int boxX = x - (boxWidth / 2);
        int boxY = y - 4;
        int countY = boxY - 11;

        UniqueSkillVisualTheme.drawIndicatorBox(drawContext, boxX, boxY, boxWidth, 24);
        UniqueSkillVisualTheme.drawIndicatorBox(drawContext, countX, countY, countBoxWidth, 10);
        drawContext.drawText(textRenderer, countLabel, x - (countWidth / 2), countY + 2, UniqueSkillVisualTheme.PANEL_TEXT, false);
        drawContext.drawText(textRenderer, glyph, x - (glyphWidth / 2), boxY + 3, color, true);
        drawContext.drawText(textRenderer, label, x - (labelWidth / 2), boxY + 14, PANEL_MUTED_TEXT, false);
    }

    private static int brighten(int color) {
        int red = Math.min(255, ((color >> 16) & 0xFF) + 26);
        int green = Math.min(255, ((color >> 8) & 0xFF) + 26);
        int blue = Math.min(255, (color & 0xFF) + 26);
        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    private static String getSpeedText(Vec3d velocity) {
        double blocksPerSecond = velocity.length() * 20.0D;
        return String.format("%.2f m/s", blocksPerSecond);
    }

    private static Text getBiomeName(MinecraftClient client, BlockPos playerPos) {
        return client.world.getBiome(playerPos).getKey()
            .map(key -> Text.translatable(Util.createTranslationKey("biome", key.getValue())))
            .orElse(Text.translatable("hud.unique_skill.telemetry.biome.unknown"));
    }

    private static Text getWeatherText(MinecraftClient client, BlockPos playerPos) {
        if (client.world.isThundering()) {
            return Text.translatable("hud.unique_skill.telemetry.weather.thunder");
        }

        if (!client.world.isRaining()) {
            return Text.translatable("hud.unique_skill.telemetry.weather.clear");
        }

        Biome.Precipitation precipitation = client.world.getBiome(playerPos).value().getPrecipitation(
            playerPos,
            client.world.getSeaLevel()
        );

        if (precipitation == Biome.Precipitation.SNOW) {
            return Text.translatable("hud.unique_skill.telemetry.weather.snow");
        }

        return Text.translatable("hud.unique_skill.telemetry.weather.rain");
    }

    private static ItemStack getWeatherIcon(MinecraftClient client, BlockPos playerPos) {
        if (client.world.isThundering()) {
            return new ItemStack(Items.LIGHTNING_ROD);
        }

        if (!client.world.isRaining()) {
            return new ItemStack(Items.SUNFLOWER);
        }

        Biome.Precipitation precipitation = client.world.getBiome(playerPos).value().getPrecipitation(
            playerPos,
            client.world.getSeaLevel()
        );

        return precipitation == Biome.Precipitation.SNOW ? new ItemStack(Items.SNOWBALL) : new ItemStack(Items.WATER_BUCKET);
    }

    private static ItemStack getBiomeIcon(MinecraftClient client, BlockPos playerPos) {
        Biome.Precipitation precipitation = client.world.getBiome(playerPos).value().getPrecipitation(
            playerPos,
            client.world.getSeaLevel()
        );
        if (precipitation == Biome.Precipitation.SNOW) {
            return new ItemStack(Items.SNOW_BLOCK);
        }

        return new ItemStack(Items.GRASS_BLOCK);
    }

    private static void drawTelemetryTile(DrawContext drawContext, int x, int y, ItemStack iconStack) {
        drawContext.fill(x, y, x + 20, y + 20, 0xD0130908);
        drawContext.fill(x + 1, y + 1, x + 19, y + 19, 0xB53B170E);
        drawContext.drawBorder(x, y, 20, 20, UniqueSkillVisualTheme.PANEL_GOLD_SOFT);
        UniqueSkillVisualTheme.drawSigil(drawContext, x + 10, y + 10, 3, 0x22281608);
        drawContext.drawItemWithoutEntity(iconStack, x + 2, y + 2);
    }

    private static Text getMoonPhaseLabel(MinecraftClient client) {
        return switch (client.world.getMoonPhase()) {
            case 0 -> Text.translatable("hud.unique_skill.telemetry.moon.full");
            case 1 -> Text.translatable("hud.unique_skill.telemetry.moon.waning_gibbous");
            case 2 -> Text.translatable("hud.unique_skill.telemetry.moon.last_quarter");
            case 3 -> Text.translatable("hud.unique_skill.telemetry.moon.waning_crescent");
            case 4 -> Text.translatable("hud.unique_skill.telemetry.moon.new");
            case 5 -> Text.translatable("hud.unique_skill.telemetry.moon.waxing_crescent");
            case 6 -> Text.translatable("hud.unique_skill.telemetry.moon.first_quarter");
            default -> Text.translatable("hud.unique_skill.telemetry.moon.waxing_gibbous");
        };
    }

    private static void renderPlayerWarnings(DrawContext drawContext, TextRenderer textRenderer, MinecraftClient client) {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        if (!config.showPlayerWarnings || client.player == null) {
            return;
        }

        List<WarningLine> warnings = collectWarnings(client);

        if (warnings.isEmpty()) {
            return;
        }

        int boxWidth = 120;
        for (WarningLine warning : warnings) {
            boxWidth = Math.max(boxWidth, textRenderer.getWidth(warning.text()) + 14);
        }
        int boxHeight = 10 + (warnings.size() * 11);
        int x = drawContext.getScaledWindowWidth() - boxWidth - 8;
        int y = 8;
        drawAuraPanel(drawContext, x, y, boxWidth, boxHeight, PANEL_TOP, PANEL_BOTTOM);

        for (int i = 0; i < warnings.size(); i++) {
            WarningLine warning = warnings.get(i);
            drawContext.drawText(textRenderer, warning.text(), x + 6, y + 3 + (i * 11), warning.color(), false);
        }
    }

    private static List<WarningLine> collectWarnings(MinecraftClient client) {
        List<WarningLine> warnings = new ArrayList<>();
        if (client.player == null) {
            return warnings;
        }

        if (client.player.getHealth() <= 6.0F) {
            warnings.add(new WarningLine(Text.translatable("hud.unique_skill.warning.health"), 0xFFF15D5D));
        }
        if (client.player.getHungerManager().getFoodLevel() <= 6) {
            warnings.add(new WarningLine(Text.translatable("hud.unique_skill.warning.hunger"), 0xFFFFC663));
        }
        if (client.player.getAir() <= 60) {
            warnings.add(new WarningLine(Text.translatable("hud.unique_skill.warning.air"), 0xFF78DFFF));
        }
        if (client.player.getStatHandler().getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(net.minecraft.stat.Stats.TIME_SINCE_REST)) >= 72000) {
            warnings.add(new WarningLine(Text.translatable("hud.unique_skill.warning.rest"), 0xFFD89DFF));
        }

        return warnings;
    }

    private static int getOffscreenThreatTotal() {
        return UniqueSkillState.getOffscreenThreatLeftCount()
            + UniqueSkillState.getOffscreenThreatRightCount()
            + UniqueSkillState.getOffscreenThreatTopCount()
            + UniqueSkillState.getOffscreenThreatBottomCount()
            + UniqueSkillState.getOffscreenThreatBackCount();
    }

    private static String auraStateKey(UniqueSkillConfig config) {
        if (!config.bubbleAuraEnabled) {
            return "hud.unique_skill.summary.aura.off";
        }

        return config.bubbleSharedMode ? "hud.unique_skill.summary.aura.shared" : "hud.unique_skill.summary.aura.local";
    }

    private static void drawAuraPanel(DrawContext drawContext, int x, int y, int width, int height, int topColor, int bottomColor) {
        UniqueSkillVisualTheme.drawArcanePanel(drawContext, x, y, width, height);
    }

    private record WarningLine(Text text, int color) {
    }
}
