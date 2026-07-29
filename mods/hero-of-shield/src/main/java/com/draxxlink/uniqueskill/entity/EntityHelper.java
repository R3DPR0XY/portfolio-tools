package com.draxxlink.uniqueskill.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

public final class EntityHelper {
    private EntityHelper() {
    }

    public static boolean isHoldingShield(PlayerEntity player) {
        return getShieldHand(player) != null;
    }

    public static Hand getShieldHand(PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof ShieldItem) {
            return Hand.MAIN_HAND;
        }

        if (player.getOffHandStack().getItem() instanceof ShieldItem) {
            return Hand.OFF_HAND;
        }

        return null;
    }

    public static boolean isUsingShield(PlayerEntity player) {
        return player.isUsingItem() && player.getActiveItem().getItem() instanceof ShieldItem;
    }
}
