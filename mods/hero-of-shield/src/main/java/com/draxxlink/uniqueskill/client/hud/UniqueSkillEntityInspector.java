package com.draxxlink.uniqueskill.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

public final class UniqueSkillEntityInspector {
    private static final ItemStack[] BREEDING_FOOD_CANDIDATES = new ItemStack[] {
        new ItemStack(Items.WHEAT),
        new ItemStack(Items.CARROT),
        new ItemStack(Items.POTATO),
        new ItemStack(Items.BEETROOT),
        new ItemStack(Items.WHEAT_SEEDS),
        new ItemStack(Items.BEETROOT_SEEDS),
        new ItemStack(Items.MELON_SEEDS),
        new ItemStack(Items.PUMPKIN_SEEDS),
        new ItemStack(Items.TORCHFLOWER_SEEDS),
        new ItemStack(Items.PITCHER_POD),
        new ItemStack(Items.SEAGRASS),
        new ItemStack(Items.BAMBOO),
        new ItemStack(Items.SWEET_BERRIES),
        new ItemStack(Items.GLOW_BERRIES),
        new ItemStack(Items.HONEYCOMB),
        new ItemStack(Items.COD),
        new ItemStack(Items.SALMON),
        new ItemStack(Items.TROPICAL_FISH),
        new ItemStack(Items.PUFFERFISH)
    };

    private UniqueSkillEntityInspector() {
    }

    public static BreedingInsight inspectBreedingTarget(MinecraftClient client) {
        if (client.world == null || client.player == null || !(client.crosshairTarget instanceof EntityHitResult entityHitResult)) {
            return null;
        }

        if (entityHitResult.getType() != HitResult.Type.ENTITY) {
            return null;
        }

        if (!(entityHitResult.getEntity() instanceof AnimalEntity animal)) {
            return null;
        }

        int breedingAge = animal.getBreedingAge();
        boolean baby = animal.isBaby();
        boolean inLove = animal.isInLove();
        boolean ready = !baby && breedingAge == 0;
        ItemStack foodStack = resolveBreedingFood(animal);
        boolean matchingFood = animal.isBreedingItem(client.player.getMainHandStack());
        Text statusText;
        Text timerText;
        Text growthText;
        int accentColor;

        if (inLove) {
            statusText = Text.translatable("hud.unique_skill.breeding.status.in_love");
            timerText = Text.translatable("hud.unique_skill.breeding.timer.partner");
            accentColor = 0xFFFF8E9A;
        } else if (ready) {
            statusText = Text.translatable("hud.unique_skill.breeding.status.ready");
            timerText = Text.translatable("hud.unique_skill.breeding.timer.ready_now");
            accentColor = 0xFF8CE19B;
        } else if (baby) {
            statusText = Text.translatable("hud.unique_skill.breeding.status.growing");
            timerText = Text.translatable("hud.unique_skill.breeding.timer.adult_in", formatTicks(Math.abs(breedingAge)));
            accentColor = 0xFFFFC98C;
        } else {
            statusText = Text.translatable("hud.unique_skill.breeding.status.cooldown");
            timerText = Text.translatable("hud.unique_skill.breeding.timer.ready_in", formatTicks(breedingAge));
            accentColor = 0xFFE7B06A;
        }

        if (baby) {
            growthText = Text.translatable("hud.unique_skill.breeding.growth.growing", formatTicks(Math.abs(breedingAge)));
        } else {
            growthText = Text.translatable("hud.unique_skill.breeding.growth.adult");
        }

        Text foodText = Text.translatable(
            "hud.unique_skill.breeding.food",
            foodStack.isEmpty() ? Text.translatable("hud.unique_skill.breeding.food_unknown") : foodStack.getName()
        );
        Text handText = Text.translatable(
            matchingFood ? "hud.unique_skill.breeding.hand_match" : "hud.unique_skill.breeding.hand_miss"
        );

        return new BreedingInsight(
            animal.getDisplayName(),
            statusText,
            timerText,
            growthText,
            foodText,
            handText,
            foodStack.isEmpty() ? new ItemStack(Items.WHEAT) : foodStack.copyWithCount(1),
            accentColor
        );
    }

    private static ItemStack resolveBreedingFood(AnimalEntity animal) {
        for (ItemStack candidate : BREEDING_FOOD_CANDIDATES) {
            if (animal.isBreedingItem(candidate)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    private static String formatTicks(int ticks) {
        int totalSeconds = Math.max(1, MathHelper.ceil(ticks / 20.0F));
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes <= 0) {
            return String.format(Locale.ROOT, "%ss", totalSeconds);
        }
        return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
    }

    public record BreedingInsight(
        Text animalName,
        Text statusText,
        Text timerText,
        Text growthText,
        Text foodText,
        Text handText,
        ItemStack foodIcon,
        int accentColor
    ) {
    }
}
