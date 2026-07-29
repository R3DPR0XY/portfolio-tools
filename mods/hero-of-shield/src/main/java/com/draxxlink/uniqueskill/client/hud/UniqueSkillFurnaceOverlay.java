package com.draxxlink.uniqueskill.client.hud;

import com.draxxlink.uniqueskill.client.ui.UniqueSkillVisualTheme;
import com.draxxlink.uniqueskill.mixin.AbstractFurnaceScreenHandlerAccessor;
import com.draxxlink.uniqueskill.mixin.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.BlastFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.SmokerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.text.Text;

public final class UniqueSkillFurnaceOverlay {
    private UniqueSkillFurnaceOverlay() {
    }

    public static void render(DrawContext drawContext, TextRenderer textRenderer, MinecraftClient client) {
        if (!(client.currentScreen instanceof AbstractFurnaceScreen<?> furnaceScreen)) {
            return;
        }

        AbstractFurnaceScreenHandler handler = furnaceScreen.getScreenHandler();
        PropertyDelegate properties = ((AbstractFurnaceScreenHandlerAccessor) handler).unique_skill$getPropertyDelegate();
        if (properties == null || properties.size() < 4) {
            return;
        }

        int burnTime = properties.get(0);
        int fuelTime = properties.get(1);
        int cookTime = properties.get(2);
        int cookTimeTotal = properties.get(3);
        ItemStack inputStack = handler.getSlot(0).getStack();
        int progressPercent = cookTimeTotal > 0 ? Math.min(100, (cookTime * 100) / cookTimeTotal) : 0;
        int fuelItemsEstimate = cookTimeTotal > 0 ? burnTime / cookTimeTotal : 0;

        int remainingCurrentTicks = cookTimeTotal > 0 ? Math.max(0, cookTimeTotal - cookTime) : 0;
        int remainingBatchTicks = cookTimeTotal > 0 && !inputStack.isEmpty()
            ? Math.max(0, (inputStack.getCount() * cookTimeTotal) - cookTime)
            : 0;

        int width = 124;
        int height = 76;
        int x = anchoredPanelX(drawContext, furnaceScreen, width);
        int y = anchoredPanelY(furnaceScreen);
        ItemStack stationIcon = furnaceIcon(furnaceScreen);
        Text stationTitle = furnaceTitle(furnaceScreen);

        UniqueSkillVisualTheme.drawArcanePanel(drawContext, x, y, width, height);
        drawContext.drawItemWithoutEntity(stationIcon, x + 6, y + 6);
        drawContext.drawText(textRenderer, stationTitle, x + 26, y + 8, UniqueSkillVisualTheme.PANEL_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.furnace.progress", progressPercent), x + 6, y + 24, UniqueSkillVisualTheme.PANEL_GOLD, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.furnace.ready", formatSeconds(remainingCurrentTicks)), x + 6, y + 34, UniqueSkillVisualTheme.PANEL_ALERT_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.furnace.batch", formatSeconds(remainingBatchTicks)), x + 6, y + 44, UniqueSkillVisualTheme.PANEL_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.furnace.fuel", fuelTime > 0 ? formatSeconds(burnTime) : "--"), x + 6, y + 54, UniqueSkillVisualTheme.PANEL_SOFT_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.furnace.capacity", fuelItemsEstimate), x + 6, y + 64, UniqueSkillVisualTheme.PANEL_MUTED_TEXT, false);
    }

    private static String formatSeconds(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    private static int anchoredPanelX(DrawContext drawContext, AbstractFurnaceScreen<?> screen, int panelWidth) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        int preferredRight = accessor.unique_skill$getX() + accessor.unique_skill$getBackgroundWidth() + 8;
        int preferredLeft = accessor.unique_skill$getX() - panelWidth - 8;
        int maxX = drawContext.getScaledWindowWidth() - panelWidth - 8;

        if (preferredRight <= maxX) {
            return preferredRight;
        }
        if (preferredLeft >= 8) {
            return preferredLeft;
        }

        return Math.max(8, Math.min(preferredRight, maxX));
    }

    private static int anchoredPanelY(AbstractFurnaceScreen<?> screen) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        return accessor.unique_skill$getY() + 6;
    }

    private static ItemStack furnaceIcon(AbstractFurnaceScreen<?> screen) {
        if (screen instanceof SmokerScreen) {
            return new ItemStack(Items.SMOKER);
        }
        if (screen instanceof BlastFurnaceScreen) {
            return new ItemStack(Items.BLAST_FURNACE);
        }

        return new ItemStack(Items.FURNACE);
    }

    private static Text furnaceTitle(AbstractFurnaceScreen<?> screen) {
        if (screen instanceof SmokerScreen) {
            return Text.translatable("hud.unique_skill.furnace.title.smoker");
        }
        if (screen instanceof BlastFurnaceScreen) {
            return Text.translatable("hud.unique_skill.furnace.title.blast");
        }

        return Text.translatable("hud.unique_skill.furnace.title");
    }
}
