package com.draxxlink.uniqueskill.entity;

public record AwarenessSnapshot(
    int hostileMobCount,
    int neutralMobCount,
    int nearbyPlayerCount,
    int spectatorPlayerCount
) {
    public static AwarenessSnapshot empty() {
        return new AwarenessSnapshot(0, 0, 0, 0);
    }
}
