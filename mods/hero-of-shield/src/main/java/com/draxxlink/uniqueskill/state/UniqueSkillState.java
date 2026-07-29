package com.draxxlink.uniqueskill.state;

public final class UniqueSkillState {
    private static boolean enabled;
    private static boolean shieldActive;
    private static int nearbyThreatCount;
    private static int nearbyHostileMobCount;
    private static int nearbyNeutralMobCount;
    private static int nearbyPlayerCount;
    private static int nearbySpectatorCount;
    private static int offscreenThreatLeftCount;
    private static int offscreenThreatRightCount;
    private static int offscreenThreatTopCount;
    private static int offscreenThreatBottomCount;
    private static int offscreenThreatBackCount;
    private static long perceptionFeedbackUntilTick;

    private UniqueSkillState() {
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isShieldActive() {
        return shieldActive;
    }

    public static void setShieldActive(boolean value) {
        shieldActive = value;
    }

    public static int getNearbyThreatCount() {
        return nearbyThreatCount;
    }

    public static void setNearbyThreatCount(int count) {
        nearbyThreatCount = Math.max(0, count);
    }

    public static int getNearbyHostileMobCount() {
        return nearbyHostileMobCount;
    }

    public static void setNearbyHostileMobCount(int count) {
        nearbyHostileMobCount = Math.max(0, count);
    }

    public static int getNearbyPlayerCount() {
        return nearbyPlayerCount;
    }

    public static int getNearbyNeutralMobCount() {
        return nearbyNeutralMobCount;
    }

    public static void setNearbyNeutralMobCount(int count) {
        nearbyNeutralMobCount = Math.max(0, count);
    }

    public static void setNearbyPlayerCount(int count) {
        nearbyPlayerCount = Math.max(0, count);
    }

    public static int getNearbySpectatorCount() {
        return nearbySpectatorCount;
    }

    public static void setNearbySpectatorCount(int count) {
        nearbySpectatorCount = Math.max(0, count);
    }

    public static int getOffscreenThreatLeftCount() {
        return offscreenThreatLeftCount;
    }

    public static void setOffscreenThreatLeftCount(int count) {
        offscreenThreatLeftCount = Math.max(0, count);
    }

    public static int getOffscreenThreatRightCount() {
        return offscreenThreatRightCount;
    }

    public static void setOffscreenThreatRightCount(int count) {
        offscreenThreatRightCount = Math.max(0, count);
    }

    public static int getOffscreenThreatTopCount() {
        return offscreenThreatTopCount;
    }

    public static void setOffscreenThreatTopCount(int count) {
        offscreenThreatTopCount = Math.max(0, count);
    }

    public static int getOffscreenThreatBottomCount() {
        return offscreenThreatBottomCount;
    }

    public static void setOffscreenThreatBottomCount(int count) {
        offscreenThreatBottomCount = Math.max(0, count);
    }

    public static int getOffscreenThreatBackCount() {
        return offscreenThreatBackCount;
    }

    public static void setOffscreenThreatBackCount(int count) {
        offscreenThreatBackCount = Math.max(0, count);
    }

    public static long getPerceptionFeedbackUntilTick() {
        return perceptionFeedbackUntilTick;
    }

    public static void setPerceptionFeedbackUntilTick(long tick) {
        perceptionFeedbackUntilTick = Math.max(0L, tick);
    }

    public static void resetCombatState() {
        shieldActive = false;
        nearbyThreatCount = 0;
        nearbyHostileMobCount = 0;
        nearbyNeutralMobCount = 0;
        nearbyPlayerCount = 0;
        nearbySpectatorCount = 0;
        offscreenThreatLeftCount = 0;
        offscreenThreatRightCount = 0;
        offscreenThreatTopCount = 0;
        offscreenThreatBottomCount = 0;
        offscreenThreatBackCount = 0;
        perceptionFeedbackUntilTick = 0L;
    }
}
