package com.draxxlink.uniqueskill.client.tooltip;

import com.draxxlink.uniqueskill.client.UniqueSkillContentInsight;
import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Locale;

public final class UniqueSkillTooltipEnhancer {
    private UniqueSkillTooltipEnhancer() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register(UniqueSkillTooltipEnhancer::appendTooltip);
    }

    private static void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        boolean addedSection = false;

        if (UniqueSkillContentInsight.isCustomStack(stack)) {
            lines.add(Text.empty());
            lines.add(Text.translatable("tooltip.unique_skill.source.header").formatted(Formatting.RED));
            lines.add(UniqueSkillContentInsight.stackSourceText(stack).copy().formatted(Formatting.GRAY));
            lines.add(UniqueSkillContentInsight.stackIdText(stack).copy().formatted(Formatting.DARK_GRAY));
            addedSection = true;
        }

        if (!config.showFoodTooltip) {
            return;
        }

        FoodComponent food = stack.get(DataComponentTypes.FOOD);
        if (food == null) {
            return;
        }

        if (!addedSection) {
            lines.add(Text.empty());
        }
        lines.add(Text.translatable("tooltip.unique_skill.food.header").formatted(Formatting.GOLD));
        lines.add(Text.translatable("tooltip.unique_skill.food.hunger", food.nutrition()).formatted(Formatting.GREEN));
        lines.add(Text.translatable("tooltip.unique_skill.food.saturation", formatDecimal(food.saturation())).formatted(Formatting.AQUA));
        if (food.canAlwaysEat()) {
            lines.add(Text.translatable("tooltip.unique_skill.food.always_edible").formatted(Formatting.LIGHT_PURPLE));
        }
    }

    private static String formatDecimal(float value) {
        return value == Math.rint(value) ? Integer.toString((int) value) : String.format(Locale.ROOT, "%.1f", value);
    }
}
