package com.draxxlink.uniqueskill.server;

import com.draxxlink.uniqueskill.network.AuraSyncPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UniqueSkillServerSupport {
    private static final long PARTICLE_INTERVAL_TICKS = 2L;
    private static final int[] DENSITY_PARTICLE_COUNTS = {2, 3, 5};
    private static final Map<UUID, SharedAuraState> SHARED_AURAS = new ConcurrentHashMap<>();

    private UniqueSkillServerSupport() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(AuraSyncPayload.ID, (payload, context) -> {
            UUID playerId = context.player().getUuid();

            if (!payload.enabled()) {
                SHARED_AURAS.remove(playerId);
                return;
            }

            int density = MathHelper.clamp(payload.density(), 1, DENSITY_PARTICLE_COUNTS.length);
            SHARED_AURAS.put(playerId, new SharedAuraState(payload.colorRgb(), density));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SHARED_AURAS.remove(handler.player.getUuid()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long tick = server.getTicks();
            if (tick % PARTICLE_INTERVAL_TICKS != 0L || SHARED_AURAS.isEmpty()) {
                return;
            }

            for (Map.Entry<UUID, SharedAuraState> entry : SHARED_AURAS.entrySet()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());

                if (player == null || !player.isAlive()) {
                    SHARED_AURAS.remove(entry.getKey());
                    continue;
                }

                spawnSharedAura(player, entry.getValue(), tick);
            }
        });
    }

    private static void spawnSharedAura(ServerPlayerEntity player, SharedAuraState state, long tick) {
        ServerWorld world = (ServerWorld) player.getWorld();
        float red = ((state.colorRgb >> 16) & 0xFF) / 255.0F;
        float green = ((state.colorRgb >> 8) & 0xFF) / 255.0F;
        float blue = (state.colorRgb & 0xFF) / 255.0F;
        Random random = player.getRandom();
        int densityIndex = MathHelper.clamp(state.density, 1, DENSITY_PARTICLE_COUNTS.length) - 1;
        int particleCount = DENSITY_PARTICLE_COUNTS[densityIndex] + (player.isSprinting() ? 1 : 0);

        for (int index = 0; index < particleCount; index++) {
            double angle = ((tick * 0.18D) + (index * 2.094D)) + (random.nextDouble() * 0.35D);
            double radius = 0.45D + (random.nextDouble() * 0.22D);
            double x = player.getX() + Math.cos(angle) * radius;
            double y = player.getBodyY(0.35D + (random.nextDouble() * 0.45D));
            double z = player.getZ() + Math.sin(angle) * radius;

            world.spawnParticles(
                TintedParticleEffect.create(ParticleTypes.ENTITY_EFFECT, red, green, blue),
                x,
                y,
                z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
            );
        }
    }

    private record SharedAuraState(int colorRgb, int density) {
    }
}
