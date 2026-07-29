package com.draxxlink.uniqueskill.client.effect;

import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public final class UniqueSkillBubbleEffect {
    private static final long PARTICLE_INTERVAL_TICKS = 2L;
    private static final int[] DENSITY_PARTICLE_COUNTS = {2, 3, 5};

    private UniqueSkillBubbleEffect() {
    }

    public static void tick(MinecraftClient client, PlayerEntity player, UniqueSkillConfig config, long worldTick) {
        if (!config.bubbleAuraEnabled || client.world == null || worldTick % PARTICLE_INTERVAL_TICKS != 0L) {
            return;
        }

        float[] color = rgbToFloatArray(resolveRgbColor(config, worldTick));
        Random random = player.getRandom();
        int densityIndex = MathHelper.clamp(config.bubbleDensity, 1, DENSITY_PARTICLE_COUNTS.length) - 1;
        int particleCount = DENSITY_PARTICLE_COUNTS[densityIndex] + (player.isSprinting() ? 1 : 0);

        for (int index = 0; index < particleCount; index++) {
            double angle = ((worldTick * 0.18D) + (index * 2.094D)) + (random.nextDouble() * 0.35D);
            double radius = 0.25D + (random.nextDouble() * 0.12D);
            double x = player.getX() + Math.cos(angle) * radius;
            double y = player.getBodyY(0.35D + (random.nextDouble() * 0.45D));
            double z = player.getZ() + Math.sin(angle) * radius;

            client.world.addParticleClient(
                TintedParticleEffect.create(ParticleTypes.ENTITY_EFFECT, color[0], color[1], color[2]),
                x,
                y,
                z,
                0.0D,
                0.0D,
                0.0D
            );
        }
    }

    public static int resolveRgbColor(UniqueSkillConfig config, long worldTick) {
        if (config.bubbleRgbMode) {
            return MathHelper.hsvToRgb((worldTick % 120L) / 120.0F, 0.85F, 1.0F);
        }

        return switch (config.bubbleColor) {
            case "RED" -> 0xFF6565;
            case "PURPLE" -> 0xB86DFF;
            default -> 0xFFCC63;
        };
    }

    private static float[] rgbToFloatArray(int rgb) {
        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;
        return new float[]{red, green, blue};
    }
}
