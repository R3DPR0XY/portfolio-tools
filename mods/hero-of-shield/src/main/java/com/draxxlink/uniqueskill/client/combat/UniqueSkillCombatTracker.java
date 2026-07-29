package com.draxxlink.uniqueskill.client.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.EnumMap;
import java.util.Map;

public final class UniqueSkillCombatTracker {
    private static final Map<Hand, WeaponSessionStats> STATS_BY_HAND = new EnumMap<>(Hand.class);
    private static final long HIT_CONFIRMATION_TIMEOUT_TICKS = 6L;

    static {
        STATS_BY_HAND.put(Hand.MAIN_HAND, new WeaponSessionStats());
        STATS_BY_HAND.put(Hand.OFF_HAND, new WeaponSessionStats());
    }

    private UniqueSkillCombatTracker() {
    }

    public static void tick(PlayerEntity player) {
        resolvePendingHit(player, Hand.MAIN_HAND);
        resolvePendingHit(player, Hand.OFF_HAND);
    }

    public static void recordHit(PlayerEntity player, Entity target, Hand hand, long worldTick) {
        if (!(target instanceof LivingEntity livingTarget) || !target.isAlive()) {
            return;
        }

        WeaponSessionStats stats = syncWeapon(player, hand);
        int targetId = target.getId();
        if (stats.pendingTargetId == targetId && worldTick <= stats.pendingUntilTick) {
            return;
        }

        boolean criticalHit = isLikelyCriticalHit(player, livingTarget);
        double estimatedDamage = estimateDamage(player, criticalHit);
        if (wasHitApplied(livingTarget)) {
            applyConfirmedHit(stats, targetId, worldTick, criticalHit, estimatedDamage);
            return;
        }

        stats.pendingTargetId = targetId;
        stats.pendingUntilTick = worldTick + HIT_CONFIRMATION_TIMEOUT_TICKS;
        stats.pendingCriticalHit = criticalHit;
        stats.pendingEstimatedDamage = estimatedDamage;
        stats.pendingObservedHurtTime = livingTarget.hurtTime;
    }

    public static WeaponStatsView getView(PlayerEntity player, Hand hand, long worldTick) {
        WeaponSessionStats stats = syncWeapon(player, hand);
        ItemStack heldStack = player.getStackInHand(hand);
        ItemStack iconStack = heldStack.isEmpty() ? ItemStack.EMPTY : heldStack.copy();
        Text weaponName = heldStack.isEmpty()
            ? Text.translatable("hud.unique_skill.unique_skill.empty")
            : heldStack.getName().copy();
        Text handLabel = Text.translatable(hand == Hand.MAIN_HAND
            ? "hud.unique_skill.unique_skill.main_hand"
            : "hud.unique_skill.unique_skill.off_hand");

        double critChance = stats.totalHits == 0 ? 0.0D : (stats.criticalHits * 100.0D) / stats.totalHits;
        double combatDurationSeconds = stats.firstHitTick < 0L
            ? 0.0D
            : Math.max(0.05D, (worldTick - stats.firstHitTick + 1L) / 20.0D);
        double dps = combatDurationSeconds <= 0.0D ? 0.0D : stats.totalEstimatedDamage / combatDurationSeconds;
        String lastCritText = stats.lastCritTick < 0L
            ? "--"
            : formatSeconds((worldTick - stats.lastCritTick) / 20.0D);
        String averageHitsBetweenCrits = stats.criticalHits == 0
            ? "--"
            : String.format(
                "%.1f",
                stats.critGapSamples > 0
                    ? stats.totalHitsBetweenCrits / (double) stats.critGapSamples
                    : stats.totalHits / (double) stats.criticalHits
            );

        return new WeaponStatsView(
            hand,
            handLabel,
            weaponName,
            iconStack,
            stats.totalHits,
            stats.criticalHits,
            critChance,
            dps,
            lastCritText,
            stats.currentDryStreak,
            averageHitsBetweenCrits,
            stats.totalHits > 0
        );
    }

    public static void reset() {
        STATS_BY_HAND.values().forEach(WeaponSessionStats::reset);
    }

    private static void resolvePendingHit(PlayerEntity player, Hand hand) {
        WeaponSessionStats stats = syncWeapon(player, hand);
        if (stats.pendingTargetId == Integer.MIN_VALUE) {
            return;
        }

        long worldTick = player.getWorld().getTime();
        if (worldTick > stats.pendingUntilTick) {
            clearPendingHit(stats);
            return;
        }

        Entity entity = player.getWorld().getEntityById(stats.pendingTargetId);
        if (!(entity instanceof LivingEntity livingTarget) || !entity.isAlive()) {
            clearPendingHit(stats);
            return;
        }

        if (livingTarget.hurtTime > stats.pendingObservedHurtTime || wasHitApplied(livingTarget)) {
            applyConfirmedHit(
                stats,
                livingTarget.getId(),
                worldTick,
                stats.pendingCriticalHit,
                stats.pendingEstimatedDamage
            );
        }
    }

    private static WeaponSessionStats syncWeapon(PlayerEntity player, Hand hand) {
        WeaponSessionStats stats = STATS_BY_HAND.get(hand);
        String signature = createSignature(player.getStackInHand(hand));
        if (!signature.equals(stats.weaponSignature)) {
            stats.reset();
            stats.weaponSignature = signature;
        }
        return stats;
    }

    private static String createSignature(ItemStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        String customName = stack.contains(DataComponentTypes.CUSTOM_NAME)
            ? stack.get(DataComponentTypes.CUSTOM_NAME).getString()
            : "";
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        String enchantmentSignature = enchantments == null ? "" : enchantments.toString();
        return itemId + "|" + customName + "|" + enchantmentSignature;
    }

    private static boolean isLikelyCriticalHit(PlayerEntity player, LivingEntity target) {
        return target.isAlive()
            && player.fallDistance > 0.0F
            && !player.isOnGround()
            && !player.isClimbing()
            && !player.isTouchingWater()
            && !player.hasVehicle()
            && !player.isSprinting()
            && !player.hasStatusEffect(StatusEffects.BLINDNESS)
            && player.getAttackCooldownProgress(0.5F) > 0.9F;
    }

    private static double estimateDamage(PlayerEntity player, boolean criticalHit) {
        double baseDamage = player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        double cooledDamage = baseDamage * (0.2D + (Math.pow(player.getAttackCooldownProgress(0.5F), 2.0D) * 0.8D));
        return criticalHit ? cooledDamage * 1.5D : cooledDamage;
    }

    private static boolean wasHitApplied(LivingEntity target) {
        return target.hurtTime > 0 || target.timeUntilRegen > 0;
    }

    private static void applyConfirmedHit(
        WeaponSessionStats stats,
        int targetId,
        long worldTick,
        boolean criticalHit,
        double estimatedDamage
    ) {
        if (stats.lastRecordedTick == worldTick && stats.lastRecordedTargetId == targetId) {
            clearPendingHit(stats);
            return;
        }

        stats.lastRecordedTick = worldTick;
        stats.lastRecordedTargetId = targetId;
        stats.totalHits++;
        stats.lastHitTick = worldTick;
        if (stats.firstHitTick < 0L) {
            stats.firstHitTick = worldTick;
        }

        if (criticalHit) {
            stats.criticalHits++;
            if (stats.lastCritTick >= 0L) {
                stats.totalHitsBetweenCrits += stats.currentDryStreak + 1;
                stats.critGapSamples++;
            }
            stats.currentDryStreak = 0;
            stats.lastCritTick = worldTick;
        } else {
            stats.currentDryStreak++;
        }

        stats.totalEstimatedDamage += estimatedDamage;
        clearPendingHit(stats);
    }

    private static void clearPendingHit(WeaponSessionStats stats) {
        stats.pendingTargetId = Integer.MIN_VALUE;
        stats.pendingUntilTick = -1L;
        stats.pendingCriticalHit = false;
        stats.pendingEstimatedDamage = 0.0D;
        stats.pendingObservedHurtTime = 0;
    }

    private static String formatSeconds(double seconds) {
        if (seconds < 10.0D) {
            return String.format("%.1fs", seconds);
        }
        return String.format("%.0fs", seconds);
    }

    public record WeaponStatsView(
        Hand hand,
        Text handLabel,
        Text weaponName,
        ItemStack iconStack,
        int totalHits,
        int criticalHits,
        double critChance,
        double dps,
        String timeSinceLastCrit,
        int dryStreak,
        String averageHitsBetweenCrits,
        boolean hasCombatData
    ) {
    }

    private static final class WeaponSessionStats {
        private String weaponSignature = "";
        private int totalHits;
        private int criticalHits;
        private double totalEstimatedDamage;
        private long firstHitTick = -1L;
        private long lastHitTick = -1L;
        private long lastCritTick = -1L;
        private int currentDryStreak;
        private int totalHitsBetweenCrits;
        private int critGapSamples;
        private long lastRecordedTick = -1L;
        private int lastRecordedTargetId = Integer.MIN_VALUE;
        private int pendingTargetId = Integer.MIN_VALUE;
        private long pendingUntilTick = -1L;
        private boolean pendingCriticalHit;
        private double pendingEstimatedDamage;
        private int pendingObservedHurtTime;

        private void reset() {
            totalHits = 0;
            criticalHits = 0;
            totalEstimatedDamage = 0.0D;
            firstHitTick = -1L;
            lastHitTick = -1L;
            lastCritTick = -1L;
            currentDryStreak = 0;
            totalHitsBetweenCrits = 0;
            critGapSamples = 0;
            lastRecordedTick = -1L;
            lastRecordedTargetId = Integer.MIN_VALUE;
            pendingTargetId = Integer.MIN_VALUE;
            pendingUntilTick = -1L;
            pendingCriticalHit = false;
            pendingEstimatedDamage = 0.0D;
            pendingObservedHurtTime = 0;
        }
    }
}
