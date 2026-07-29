package com.draxxlink.uniqueskill.client.combat;

import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class AutoAttackHandler {
    private static boolean isAutoAttackActive = false;

    private AutoAttackHandler() {
    }

    public static void setAutoAttackActive(boolean active) {
        isAutoAttackActive = active;
    }

    public static boolean isAutoAttackActive() {
        return isAutoAttackActive;
    }

    public static void tick(MinecraftClient client, PlayerEntity player, long worldTick) {
        if (!isAutoAttackActive || client.world == null || client.interactionManager == null) {
            return;
        }

        if (player.isUsingItem()) {
            return;
        }

        float attackCooldown = player.getAttackCooldownProgress(0.0F);
        if (attackCooldown < 1.0F) {
            return;
        }

        Entity targetEntity = getBestTarget(client, player, UniqueSkillConfig.getInstance());
        if (targetEntity == null) {
            return;
        }

        client.interactionManager.attackEntity(player, targetEntity);
        player.swingHand(Hand.MAIN_HAND);
        UniqueSkillCombatTracker.recordHit(player, targetEntity, Hand.MAIN_HAND, worldTick);
    }

    private static Entity getBestTarget(MinecraftClient client, PlayerEntity player, UniqueSkillConfig config) {
        Entity crosshairTarget = getCrosshairTarget(client);
        if (isValidTarget(player, crosshairTarget, config, true)) {
            return crosshairTarget;
        }

        double attackRange = getAttackRange(player);
        double searchRange = attackRange + 1.0D;
        double toleranceRadians = Math.toRadians(config.autoAttackAimToleranceDegrees);
        double minimumDot = Math.cos(toleranceRadians);
        Vec3d playerEyePos = player.getEyePos();
        Vec3d lookDirection = player.getRotationVec(1.0F).normalize();
        Box searchBox = player.getBoundingBox().expand(searchRange);

        Entity bestEntity = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (LivingEntity entity : client.world.getEntitiesByClass(LivingEntity.class, searchBox, living -> living != null)) {
            if (!isValidTarget(player, entity, config, false)) {
                continue;
            }

            Vec3d toTarget = entity.getEyePos().subtract(playerEyePos);
            double distanceSquared = toTarget.lengthSquared();
            if (distanceSquared <= 0.0001D || distanceSquared > searchRange * searchRange) {
                continue;
            }

            Vec3d directionToTarget = toTarget.normalize();
            double alignment = lookDirection.dotProduct(directionToTarget);
            if (alignment < minimumDot) {
                continue;
            }

            double score = getTargetPriority(entity) * 1000.0D;
            score += alignment * 100.0D;
            score -= distanceSquared;

            if (score > bestScore) {
                bestScore = score;
                bestEntity = entity;
            }
        }

        return bestEntity;
    }

    private static Entity getCrosshairTarget(MinecraftClient client) {
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return null;
        }

        EntityHitResult entityHitResult = (EntityHitResult) client.crosshairTarget;
        return entityHitResult.getEntity();
    }

    private static boolean isValidTarget(PlayerEntity player, Entity entity, UniqueSkillConfig config, boolean allowBlockedSight) {
        if (entity == null || entity == player || !entity.isAlive() || entity.isRemoved()) {
            return false;
        }

        if (!entity.isAttackable()) {
            return false;
        }

        if (!allowBlockedSight && !player.canSee(entity)) {
            return false;
        }

        if (!(entity instanceof LivingEntity livingEntity) || livingEntity.isDead()) {
            return false;
        }

        if (entity instanceof PlayerEntity) {
            return config.autoAttackPlayers;
        }

        if (entity instanceof HostileEntity) {
            return config.autoAttackHostiles;
        }

        if (entity instanceof PassiveEntity) {
            return config.autoAttackPassives;
        }

        return config.autoAttackNeutrals;
    }

    private static int getTargetPriority(Entity entity) {
        if (entity instanceof HostileEntity) {
            return 3;
        }

        if (entity instanceof PassiveEntity) {
            return 1;
        }

        if (entity instanceof PlayerEntity) {
            return 0;
        }

        return 2;
    }

    private static double getAttackRange(PlayerEntity player) {
        return Math.max(3.0D, player.getEntityInteractionRange());
    }

    public static void reset() {
        isAutoAttackActive = false;
    }
}
