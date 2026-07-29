package com.draxxlink.uniqueskill.client.inventory;

import com.draxxlink.uniqueskill.client.UniqueSkillContentInsight;
import com.draxxlink.uniqueskill.client.ui.UniqueSkillVisualTheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.CartographyTableScreen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public final class UniqueSkillInventoryOverlay {
    private UniqueSkillInventoryOverlay() {
    }

    public static void render(DrawContext drawContext, TextRenderer textRenderer, MinecraftClient client) {
        if (!(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
            return;
        }
        if (client.currentScreen instanceof CraftingScreen
            || client.currentScreen instanceof SmithingScreen
            || client.currentScreen instanceof CartographyTableScreen
            || client.currentScreen instanceof AbstractFurnaceScreen<?>) {
            return;
        }

        InventoryStats stats = scan(handledScreen);
        int width = 148;
        int height = 86;
        int x = Math.max(8, drawContext.getScaledWindowWidth() - width - 8);
        int y = 8;

        UniqueSkillVisualTheme.drawArcanePanel(drawContext, x, y, width, height);
        drawContext.drawItemWithoutEntity(new ItemStack(Items.CHEST), x + 6, y + 6);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.inventory.title"), x + 26, y + 8, UniqueSkillVisualTheme.PANEL_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.inventory.free_slots", stats.freeSlots), x + 6, y + 24, UniqueSkillVisualTheme.PANEL_GOLD, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.inventory.partial_stacks", stats.partialStacks), x + 6, y + 34, UniqueSkillVisualTheme.PANEL_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.inventory.duplicates", stats.duplicateGroups), x + 6, y + 44, UniqueSkillVisualTheme.PANEL_SOFT_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.inventory.custom", stats.customStacks), x + 6, y + 54, UniqueSkillVisualTheme.PANEL_MUTED_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.inventory.best_item", stats.bestItemName), x + 6, y + 64, UniqueSkillVisualTheme.PANEL_ALERT_TEXT, false);
        drawContext.drawText(textRenderer, Text.translatable("hud.unique_skill.inventory.summary_button"), x + 6, y + 74, 0xFFD8B46F, false);
    }

    private static InventoryStats scan(HandledScreen<?> screen) {
        int freeSlots = 0;
        int partialStacks = 0;
        int customStacks = 0;
        Map<String, Integer> itemCounts = new HashMap<>();
        int bestScore = Integer.MIN_VALUE;
        String bestItemName = "--";

        for (Slot slot : screen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                freeSlots++;
                continue;
            }

            String itemId = UniqueSkillContentInsight.stackIdString(stack);
            if (itemId != null) {
                itemCounts.merge(itemId, 1, Integer::sum);
            }

            if (stack.getCount() < stack.getMaxCount()) {
                partialStacks++;
            }
            if (UniqueSkillContentInsight.isCustomStack(stack)) {
                customStacks++;
            }

            int score = valueScore(stack);
            if (score > bestScore) {
                bestScore = score;
                bestItemName = stack.getName().getString();
            }
        }

        int duplicateGroups = 0;
        for (int count : itemCounts.values()) {
            if (count > 1) {
                duplicateGroups++;
            }
        }

        return new InventoryStats(freeSlots, partialStacks, duplicateGroups, customStacks, bestItemName);
    }

    private static int valueScore(ItemStack stack) {
        int score = 0;
        if (stack.hasEnchantments()) {
            score += 8;
        }
        if (UniqueSkillContentInsight.isCustomStack(stack)) {
            score += 6;
        }
        score += stack.getRarity().ordinal() * 3;
        if (stack.isDamageable()) {
            score += Math.max(0, (stack.getMaxDamage() - stack.getDamage()) / 50);
        }
        return score;
    }

    private record InventoryStats(int freeSlots, int partialStacks, int duplicateGroups, int customStacks, String bestItemName) {
    }
}
