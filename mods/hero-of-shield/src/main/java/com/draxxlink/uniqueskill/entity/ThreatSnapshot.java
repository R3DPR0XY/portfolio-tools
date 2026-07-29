package com.draxxlink.uniqueskill.entity;

import net.minecraft.entity.LivingEntity;

import java.util.List;

public record ThreatSnapshot(
    List<LivingEntity> nearbyThreats,
    LivingEntity nearestThreat,
    int incomingThreatCount,
    long earliestImpactTick
) {
    private static final long NO_IMPACT_TICK = -1L;

    public static ThreatSnapshot empty() {
        return new ThreatSnapshot(List.of(), null, 0, NO_IMPACT_TICK);
    }

    public boolean hasIncomingThreat() {
        return incomingThreatCount > 0 && earliestImpactTick >= 0L;
    }
}
