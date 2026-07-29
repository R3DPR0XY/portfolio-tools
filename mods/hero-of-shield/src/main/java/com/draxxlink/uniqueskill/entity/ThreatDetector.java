package com.draxxlink.uniqueskill.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.MaceItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class ThreatDetector {
    private static final int MAX_MELEE_LOOKAHEAD_TICKS = 12;
    private static final int MAX_PLAYER_LOOKAHEAD_TICKS = 10;
    private static final double MELEE_FACING_THRESHOLD = 0.20D;
    private static final double CLOSE_RANGE_FACING_THRESHOLD = 0.05D;

    private ThreatDetector() {
    }

    public static ThreatSnapshot scanThreats(PlayerEntity player, double range, long worldTick) {
        return scanThreats(player, range, worldTick, true);
    }

    public static ThreatSnapshot scanThreats(PlayerEntity player, double range, long worldTick, boolean includePlayers) {
        double searchRange = Math.max(2.5D, range);
        Box hostileSearchBox = player.getBoundingBox().expand(searchRange, searchRange, searchRange);

        List<LivingEntity> nearbyThreats = new ArrayList<>();
        LivingEntity nearestThreat = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        int incomingThreatCount = 0;
        long earliestImpactTick = -1L;

        List<MobEntity> scannedHostiles = player.getWorld().getEntitiesByClass(
            MobEntity.class,
            hostileSearchBox,
            mob -> isRelevantHostile(player, mob, searchRange)
        );

        for (MobEntity hostile : scannedHostiles) {
            nearbyThreats.add(hostile);

            double distanceSquared = player.squaredDistanceTo(hostile);
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestThreat = hostile;
            }

            long impactTick = estimateMeleeImpactTick(player, hostile, worldTick, searchRange);
            if (isIncomingThreat(worldTick, impactTick, MAX_MELEE_LOOKAHEAD_TICKS)) {
                incomingThreatCount++;
                earliestImpactTick = minImpactTick(earliestImpactTick, impactTick);
            }
        }

        if (includePlayers) {
            List<PlayerEntity> nearbyPlayers = player.getWorld().getEntitiesByClass(
                PlayerEntity.class,
                hostileSearchBox,
                otherPlayer -> isNearbyPlayerCandidate(player, otherPlayer, searchRange)
            );

            for (PlayerEntity nearbyPlayer : nearbyPlayers) {
                nearbyThreats.add(nearbyPlayer);

                double distanceSquared = player.squaredDistanceTo(nearbyPlayer);
                if (distanceSquared < nearestDistanceSquared) {
                    nearestDistanceSquared = distanceSquared;
                    nearestThreat = nearbyPlayer;
                }

                long impactTick = estimatePlayerImpactTick(player, nearbyPlayer, worldTick, searchRange);
                if (isIncomingThreat(worldTick, impactTick, MAX_PLAYER_LOOKAHEAD_TICKS)) {
                    incomingThreatCount++;
                    earliestImpactTick = minImpactTick(earliestImpactTick, impactTick);
                }
            }
        }

        return new ThreatSnapshot(List.copyOf(nearbyThreats), nearestThreat, incomingThreatCount, earliestImpactTick);
    }

    public static AwarenessSnapshot scanAwareness(PlayerEntity player, double range, long worldTick) {
        double searchRange = Math.max(2.5D, range);
        Box awarenessBox = player.getBoundingBox().expand(searchRange, searchRange, searchRange);
        int hostileMobCount = 0;
        int neutralMobCount = 0;
        List<MobEntity> mobs = player.getWorld().getEntitiesByClass(
            MobEntity.class,
            awarenessBox,
            mob -> isAwarenessCandidate(player, mob, searchRange)
        );
        for (MobEntity mob : mobs) {
            if (mob instanceof HostileEntity) {
                hostileMobCount++;
            } else {
                neutralMobCount++;
            }
        }

        int nearbyPlayerCount = player.getWorld().getEntitiesByClass(
            PlayerEntity.class,
            awarenessBox,
            otherPlayer -> isNearbyPlayerCandidate(player, otherPlayer, searchRange)
        ).size();
        int spectatorPlayerCount = player.getWorld().getEntitiesByClass(
            PlayerEntity.class,
            awarenessBox,
            otherPlayer -> isNearbySpectatorCandidate(player, otherPlayer, searchRange)
        ).size();

        return new AwarenessSnapshot(hostileMobCount, neutralMobCount, nearbyPlayerCount, spectatorPlayerCount);
    }

    public static boolean isPveHostile(LivingEntity entity) {
        return entity instanceof HostileEntity;
    }

    private static boolean isAwarenessCandidate(PlayerEntity player, MobEntity mob, double range) {
        return mob.isAlive()
            && !mob.isRemoved()
            && mob.squaredDistanceTo(player) <= range * range;
    }

    private static boolean isRelevantHostile(PlayerEntity player, MobEntity mob, double range) {
        if (!mob.isAlive() || mob.isRemoved() || !(mob instanceof HostileEntity)) {
            return false;
        }

        double distanceSquared = mob.squaredDistanceTo(player);
        if (distanceSquared > range * range) {
            return false;
        }

        if (mob.getTarget() == player) {
            return true;
        }

        double awarenessDistance = Math.min(range, getAwarenessDistance(mob, player));
        return distanceSquared <= awarenessDistance * awarenessDistance
            && mob.canSee(player)
            && isFacingPlayer(mob, player, false);
    }

    private static boolean isNearbyPlayerCandidate(PlayerEntity observer, PlayerEntity candidate, double range) {
        return candidate != observer
            && candidate.isAlive()
            && !candidate.isSpectator()
            && !candidate.isCreative()
            && !observer.isTeammate(candidate)
            && candidate.squaredDistanceTo(observer) <= range * range;
    }

    private static boolean isNearbySpectatorCandidate(PlayerEntity observer, PlayerEntity candidate, double range) {
        return candidate != observer
            && candidate.isAlive()
            && candidate.isSpectator()
            && !candidate.isRemoved()
            && candidate.squaredDistanceTo(observer) <= range * range;
    }

    private static boolean isRelevantCombatPlayer(PlayerEntity observer, PlayerEntity candidate, double range) {
        if (!isNearbyPlayerCandidate(observer, candidate, range)) {
            return false;
        }

        // Heuristic combat sense: weapon, pressure, facing and movement together
        // tell us much more than just "another player is nearby".
        double threatScore = 0.0D;
        if (isCombatItem(candidate.getMainHandStack().getItem())) {
            threatScore += 1.1D;
        }
        if (candidate.isSprinting()) {
            threatScore += 0.35D;
        }
        if (candidate.handSwinging) {
            threatScore += 0.75D;
        }
        if (candidate.canSee(observer)) {
            threatScore += 0.35D;
        }
        if (isFacingPlayer(candidate, observer, true)) {
            threatScore += 0.65D;
        }
        if (candidate.squaredDistanceTo(observer) <= 16.0D) {
            threatScore += 0.45D;
        }
        if (getClosingSpeedTowardsTarget(candidate, observer) > 0.05D) {
            threatScore += 0.35D;
        }

        return threatScore >= 1.65D;
    }

    private static long estimateMeleeImpactTick(PlayerEntity player, MobEntity mob, long worldTick, double range) {
        if (isRangedThreatSource(mob)) {
            return -1L;
        }

        if (mob.getTarget() != player || !mob.canSee(player) || !isFacingPlayer(mob, player, true)) {
            return -1L;
        }

        double horizontalDistance = getHorizontalDistance(mob.getPos(), player.getPos());
        if (horizontalDistance > range) {
            return -1L;
        }

        if (mob instanceof CreeperEntity creeper) {
            if (!creeper.isIgnited() && horizontalDistance > 2.6D) {
                return -1L;
            }

            return worldTick + Math.max(1, (int) Math.ceil(Math.max(0.0D, horizontalDistance - 2.25D) / 0.28D));
        }

        double attackReach = getMeleeReach(mob, player);
        double distanceToImpact = Math.max(0.0D, horizontalDistance - attackReach);
        if (distanceToImpact <= 0.12D) {
            return worldTick + 1L;
        }

        double closingSpeed = Math.max(getClosingSpeedTowardsTarget(mob, player), getAggressiveSpeedEstimate(mob));
        int travelTicks = (int) Math.ceil(distanceToImpact / closingSpeed);
        return worldTick + Math.max(1, travelTicks);
    }

    private static long estimatePlayerImpactTick(PlayerEntity observer, PlayerEntity combatPlayer, long worldTick, double range) {
        if (!isRelevantCombatPlayer(observer, combatPlayer, range)) {
            return -1L;
        }

        if (isRangedCombatItem(combatPlayer.getMainHandStack().getItem()) && combatPlayer.isUsingItem() && combatPlayer.canSee(observer)) {
            return worldTick + 6L;
        }

        if (!isFacingPlayer(combatPlayer, observer, true)) {
            return -1L;
        }

        double horizontalDistance = getHorizontalDistance(combatPlayer.getPos(), observer.getPos());
        double attackReach = 2.2D + (observer.getWidth() * 0.5D);
        double distanceToImpact = Math.max(0.0D, horizontalDistance - attackReach);
        if (distanceToImpact <= 0.1D) {
            return worldTick + (combatPlayer.handSwinging ? 1L : 2L);
        }

        double closingSpeed = Math.max(
            getClosingSpeedTowardsTarget(combatPlayer, observer),
            combatPlayer.isSprinting() ? 0.32D : 0.22D
        );
        int travelTicks = (int) Math.ceil(distanceToImpact / closingSpeed);
        if (combatPlayer.handSwinging) {
            travelTicks = Math.max(1, travelTicks - 1);
        }

        return worldTick + Math.max(1, travelTicks);
    }

    private static boolean isFacingPlayer(LivingEntity attacker, PlayerEntity player, boolean strict) {
        Vec3d toPlayer = player.getEyePos().subtract(attacker.getEyePos());
        if (toPlayer.lengthSquared() < 0.001D) {
            return true;
        }

        double facingDot = attacker.getRotationVec(1.0F).normalize().dotProduct(toPlayer.normalize());
        double threshold = strict ? MELEE_FACING_THRESHOLD : CLOSE_RANGE_FACING_THRESHOLD;
        if (attacker.squaredDistanceTo(player) <= 4.0D) {
            threshold = CLOSE_RANGE_FACING_THRESHOLD;
        }

        return facingDot >= threshold;
    }

    private static boolean isRangedThreatSource(MobEntity mob) {
        return mob instanceof AbstractSkeletonEntity
            || mob instanceof PillagerEntity
            || mob instanceof WitchEntity
            || mob instanceof BlazeEntity;
    }

    private static boolean isCombatItem(Item item) {
        return isSwordLikeItem(item)
            || item instanceof AxeItem
            || item instanceof MaceItem
            || item instanceof TridentItem
            || isRangedCombatItem(item);
    }

    private static boolean isSwordLikeItem(Item item) {
        return item.getTranslationKey().contains("sword");
    }

    private static boolean isRangedCombatItem(Item item) {
        return item instanceof BowItem || item instanceof CrossbowItem;
    }

    private static double getAwarenessDistance(MobEntity mob, PlayerEntity player) {
        if (mob instanceof CreeperEntity) {
            return 4.0D;
        }

        if (isRangedThreatSource(mob)) {
            return 7.0D;
        }

        return getMeleeReach(mob, player) + 1.2D;
    }

    private static double getMeleeReach(MobEntity mob, PlayerEntity player) {
        return 1.7D + (mob.getWidth() * 0.5D) + (player.getWidth() * 0.5D);
    }

    private static double getClosingSpeedTowardsTarget(LivingEntity attacker, PlayerEntity player) {
        Vec3d velocity = attacker.getVelocity();
        double speed = velocity.horizontalLength();
        if (speed < 0.01D) {
            return 0.0D;
        }

        Vec3d horizontalVelocity = new Vec3d(velocity.x, 0.0D, velocity.z).normalize();
        Vec3d toPlayer = player.getPos().subtract(attacker.getPos());
        if (toPlayer.horizontalLengthSquared() < 0.001D) {
            return speed;
        }

        Vec3d horizontalToPlayer = new Vec3d(toPlayer.x, 0.0D, toPlayer.z).normalize();
        return Math.max(0.0D, horizontalVelocity.dotProduct(horizontalToPlayer) * speed);
    }

    private static double getAggressiveSpeedEstimate(MobEntity mob) {
        double attributeSpeed = mob.getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
        return Math.max(0.18D, Math.min(0.38D, attributeSpeed * 1.15D));
    }

    private static double getHorizontalDistance(Vec3d from, Vec3d to) {
        double deltaX = to.x - from.x;
        double deltaZ = to.z - from.z;
        return Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
    }

    private static boolean isIncomingThreat(long worldTick, long impactTick, int lookaheadTicks) {
        return impactTick >= worldTick && impactTick - worldTick <= lookaheadTicks;
    }

    private static long minImpactTick(long currentValue, long candidateValue) {
        if (currentValue < 0L) {
            return candidateValue;
        }

        return Math.min(currentValue, candidateValue);
    }
}
