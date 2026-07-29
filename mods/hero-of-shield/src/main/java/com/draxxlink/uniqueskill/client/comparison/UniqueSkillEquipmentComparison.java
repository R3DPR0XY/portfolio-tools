package com.draxxlink.uniqueskill.client.comparison;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class UniqueSkillEquipmentComparison {
    private UniqueSkillEquipmentComparison() {
    }

    public static ComparisonReport compare(MinecraftClient client, ItemStack candidate, ItemStack baseHint) {
        if (client.player == null || candidate.isEmpty()) {
            return ComparisonReport.empty();
        }

        ItemStack current = resolveCurrentItem(client.player, candidate, baseHint);
        Score candidateScore = score(candidate);
        Score currentScore = score(current);
        int delta = candidateScore.total() - currentScore.total();

        List<Text> lines = new ArrayList<>();
        addLine(lines, "hud.unique_skill.compare.defense", candidateScore.defense - currentScore.defense);
        addLine(lines, "hud.unique_skill.compare.toughness", candidateScore.toughness - currentScore.toughness);
        addLine(lines, "hud.unique_skill.compare.attack", candidateScore.attack - currentScore.attack);
        addLine(lines, "hud.unique_skill.compare.durability", candidateScore.durability - currentScore.durability);
        addLine(lines, "hud.unique_skill.compare.knockback_resistance", candidateScore.knockbackResistance - currentScore.knockbackResistance);
        addLine(lines, "hud.unique_skill.compare.lore", candidateScore.lore - currentScore.lore);

        String verdictKey;
        if (delta >= 3) {
            verdictKey = "hud.unique_skill.compare.verdict.better";
        } else if (delta <= -2) {
            verdictKey = "hud.unique_skill.compare.verdict.equal";
        } else {
            verdictKey = "hud.unique_skill.compare.verdict.situational";
        }

        return new ComparisonReport(current, lines, Text.translatable(verdictKey));
    }

    private static void addLine(List<Text> lines, String key, int delta) {
        if (delta == 0) {
            return;
        }

        lines.add(Text.translatable(key, delta > 0 ? "+" + delta : Integer.toString(delta)));
    }

    private static ItemStack resolveCurrentItem(PlayerEntity player, ItemStack candidate, ItemStack baseHint) {
        if (!baseHint.isEmpty()) {
            return baseHint;
        }

        Item item = candidate.getItem();
        // Assume armor for now
        // if (item instanceof ArmorItem armorItem) {
        //     return player.getEquippedStack(armorItem.getSlotType());
        // }
        if (item instanceof ShieldItem) {
            return player.getOffHandStack();
        }
        return player.getMainHandStack();
    }

    private static Score score(ItemStack stack) {
        if (stack.isEmpty()) {
            return Score.EMPTY;
        }

        Item item = stack.getItem();
        int defense = 0;
        int toughness = 0;
        int knockbackResistance = 0;
        int attack = 0;

        // Armor scoring commented out due to class issues
        // if (item instanceof ArmorItem armorItem) {
        //     defense = armorItem.getProtection();
        //     toughness = Math.round(armorItem.getMaterial().value().toughness());
        //     knockbackResistance = Math.round(armorItem.getMaterial().value().knockbackResistance() * 10.0F);
        // }
        // if (item instanceof SwordItem swordItem) {
        //     attack = Math.round(swordItem.getAttackDamage());
        // } // else if (item instanceof ToolItem toolItem) {
        //     attack = Math.round(toolItem.getAttackDamage());
        // } else if (item instanceof TridentItem) {
        //     attack = 8;
        // }

        int durability = stack.isDamageable() ? (stack.getMaxDamage() - stack.getDamage()) / 25 : 0;
        int lore = loreScore(stack);

        return new Score(defense, toughness, attack, durability, knockbackResistance, lore);
    }

    private static int loreScore(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        return lore == null ? 0 : lore.lines().size();
    }

    private record Score(int defense, int toughness, int attack, int durability, int knockbackResistance, int lore) {
        private static final Score EMPTY = new Score(0, 0, 0, 0, 0, 0);

        private int total() {
            return defense + toughness + attack + durability + knockbackResistance + lore;
        }
    }

    public record ComparisonReport(ItemStack current, List<Text> lines, Text verdict) {
        private static ComparisonReport empty() {
            return new ComparisonReport(ItemStack.EMPTY, List.of(), Text.translatable("hud.unique_skill.compare.verdict.equal"));
        }
    }
}
