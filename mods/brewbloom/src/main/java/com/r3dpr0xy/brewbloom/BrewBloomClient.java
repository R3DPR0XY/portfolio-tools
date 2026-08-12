package com.r3dpr0xy.brewbloom;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;

public final class BrewBloomClient implements ClientModInitializer {
    public static final String MOD_ID = "brewbloom";
    private static final SimpleParticleType BLOOM_PARTICLE = FabricParticleTypes.simple();
    private static final SimpleParticleType SOFT_PARTICLE = FabricParticleTypes.simple();
    private static final SimpleParticleType PIXEL_PARTICLE = FabricParticleTypes.simple();
    private static final SimpleParticleType STAR_PARTICLE = FabricParticleTypes.simple();
    private static final SimpleParticleType RING_PARTICLE = FabricParticleTypes.simple();
    private static final SimpleParticleType SPARKLE_PARTICLE = FabricParticleTypes.simple();

    private static final long PARTICLE_INTERVAL_TICKS = 2L;
    private static KeyBinding toggleKey;
    private static KeyBinding configKey;
    static BrewBloomConfig config;

    @Override
    public void onInitializeClient() {
        registerParticles();
        config = BrewBloomConfig.load();
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category." + MOD_ID
        ));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category." + MOD_ID
        ));

        ClientTickEvents.END_CLIENT_TICK.register(BrewBloomClient::tick);
    }

    private static void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "bloom_bubble"), BLOOM_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "soft_bubble"), SOFT_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "pixel_bubble"), PIXEL_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "star_bubble"), STAR_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "ring_bubble"), RING_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "sparkle_bubble"), SPARKLE_PARTICLE);
        ParticleFactoryRegistry.getInstance().register(BLOOM_PARTICLE, BrewBubbleParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SOFT_PARTICLE, BrewBubbleParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PIXEL_PARTICLE, BrewBubbleParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(STAR_PARTICLE, BrewBubbleParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(RING_PARTICLE, BrewBubbleParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SPARKLE_PARTICLE, BrewBubbleParticle.Factory::new);
    }

    private static void tick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            config.enabled = !config.enabled;
            config.save();

            if (client.player != null) {
                client.player.sendMessage(Text.translatable(config.enabled
                    ? "message." + MOD_ID + ".enabled"
                    : "message." + MOD_ID + ".disabled"), true);
            }
        }
        while (configKey.wasPressed()) {
            client.setScreen(new BrewBloomConfigScreen(config));
        }

        if (!config.enabled || client.world == null || client.player == null) {
            return;
        }

        long tick = client.world.getTime();
        if (tick % PARTICLE_INTERVAL_TICKS != 0L) {
            return;
        }

        spawnBubbles(client, client.player, tick);
    }

    private static void spawnBubbles(MinecraftClient client, PlayerEntity player, long tick) {
        Random random = player.getRandom();
        Collection<StatusEffectInstance> effects = player.getStatusEffects();
        if (effects.isEmpty()) {
            if (config.showWithoutEffects) {
                spawnPreviewBubbles(client, player, random, tick);
            }
            return;
        }

        int effectIndex = 0;
        int activeEffectCount = Math.min(effects.size(), config.effectLimit);
        int spawnedParticles = 0;

        for (StatusEffectInstance effect : effects) {
            if (effectIndex >= config.effectLimit) {
                break;
            }

            int crowdReduction = Math.max(0, activeEffectCount - config.crowdedEffectStart);
            int particleCount = Math.max(1, config.density + Math.min(effect.getAmplifier(), 2) - crowdReduction);
            particleCount = Math.min(particleCount, Math.max(0, config.totalBubbleLimit - spawnedParticles));
            if (particleCount <= 0) {
                break;
            }

            for (int particle = 0; particle < particleCount; particle++) {
                int color = resolveColor(effect, tick, effectIndex, particle);
                float red = ((color >> 16) & 0xFF) / 255.0F;
                float green = ((color >> 8) & 0xFF) / 255.0F;
                float blue = (color & 0xFF) / 255.0F;
                ParticlePosition position = resolvePosition(player, random, tick, effectIndex, particle);
                double velocityY = (0.004D + random.nextDouble() * 0.012D) * config.riseSpeed;

                addBubbleParticle(client, color, red, green, blue, position, velocityY);
                spawnedParticles++;
            }

            effectIndex++;
        }
    }

    private static void spawnPreviewBubbles(MinecraftClient client, PlayerEntity player, Random random, long tick) {
        int particleCount = Math.min(Math.max(1, config.density), config.totalBubbleLimit);
        for (int particle = 0; particle < particleCount; particle++) {
            int color = resolvePreviewColor(tick, 0, particle);
            float red = ((color >> 16) & 0xFF) / 255.0F;
            float green = ((color >> 8) & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            ParticlePosition position = resolvePosition(player, random, tick, 0, particle);

            addBubbleParticle(client, color, red, green, blue, position, (0.004D + random.nextDouble() * 0.012D) * config.riseSpeed);
        }
    }

    private static void addBubbleParticle(MinecraftClient client, int color, float red, float green, float blue, ParticlePosition position, double velocityY) {
        if ("vanilla".equals(config.textureMode)) {
            client.world.addParticleClient(
                TintedParticleEffect.create(ParticleTypes.ENTITY_EFFECT, red, green, blue),
                position.x,
                position.y,
                position.z,
                0.0D,
                velocityY,
                0.0D
            );
            return;
        }

        SimpleParticleType particleType = switch (config.textureMode) {
            case "soft" -> SOFT_PARTICLE;
            case "pixel" -> PIXEL_PARTICLE;
            case "star" -> STAR_PARTICLE;
            case "ring" -> RING_PARTICLE;
            case "sparkle" -> SPARKLE_PARTICLE;
            default -> BLOOM_PARTICLE;
        };

        client.world.addParticleClient(
            particleType,
            position.x,
            position.y,
            position.z,
            color & 0xFFFFFF,
            velocityY,
            0.0D
        );
    }

    private static ParticlePosition resolvePosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        return switch (config.particleStyle) {
            case "aura" -> auraPosition(player, random);
            case "halo" -> haloPosition(player, random, tick, effectIndex, particleIndex);
            case "trail" -> trailPosition(player, random);
            case "spiral" -> spiralPosition(player, random, tick, effectIndex, particleIndex);
            case "crown" -> crownPosition(player, random, tick, effectIndex, particleIndex);
            case "pulse" -> pulsePosition(player, random, tick, effectIndex, particleIndex);
            case "fountain" -> fountainPosition(player, random, tick, effectIndex, particleIndex);
            case "ring" -> ringPosition(player, random, tick, effectIndex, particleIndex);
            case "vortex" -> vortexPosition(player, random, tick, effectIndex, particleIndex);
            case "comet" -> cometPosition(player, random, tick, effectIndex, particleIndex);
            case "spark" -> sparkPosition(player, random);
            case "wave" -> wavePosition(player, random, tick, effectIndex, particleIndex);
            case "double_ring" -> doubleRingPosition(player, random, tick, effectIndex, particleIndex);
            case "rain" -> rainPosition(player, random);
            default -> orbitPosition(player, random, tick, effectIndex, particleIndex);
        };
    }

    private static ParticlePosition orbitPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double angle = (tick * 0.13D * config.swirlSpeed) + (effectIndex * 1.7D) + (particleIndex * 2.094D) + random.nextDouble() * 0.45D;
        double radius = config.radius + random.nextDouble() * 0.18D;
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.25D + random.nextDouble() * (player.getHeight() * config.heightScale),
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition auraPosition(PlayerEntity player, Random random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = random.nextDouble() * (config.radius + 0.2D);
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.15D + random.nextDouble() * (player.getHeight() * config.heightScale),
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition haloPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double angle = (tick * 0.12D * config.swirlSpeed) + effectIndex + particleIndex * 1.570D;
        double radius = Math.max(0.18D, config.radius * 0.75D);
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + player.getHeight() + 0.18D + random.nextDouble() * 0.08D,
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition trailPosition(PlayerEntity player, Random random) {
        double yaw = Math.toRadians(player.getYaw());
        double backX = Math.sin(yaw) * config.radius;
        double backZ = -Math.cos(yaw) * config.radius;
        return new ParticlePosition(
            player.getX() + backX + (random.nextDouble() - 0.5D) * 0.35D,
            player.getY() + 0.2D + random.nextDouble() * (player.getHeight() * Math.min(config.heightScale, 0.9D)),
            player.getZ() + backZ + (random.nextDouble() - 0.5D) * 0.35D
        );
    }

    private static ParticlePosition spiralPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double progress = ((tick + particleIndex * 5L + effectIndex * 9L) % 48L) / 48.0D;
        double angle = progress * Math.PI * 6.0D + tick * 0.05D * config.swirlSpeed;
        double radius = Math.max(0.12D, config.radius * (0.55D + progress * 0.55D));
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.12D + progress * (player.getHeight() * config.heightScale),
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition crownPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double angle = (particleIndex * 0.9D) + (effectIndex * 0.6D) + tick * 0.08D * config.swirlSpeed;
        double radius = Math.max(0.22D, config.radius * 0.9D);
        double bob = Math.sin((tick + particleIndex * 6L) * 0.14D) * 0.05D;
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + player.getHeight() + 0.08D + bob + random.nextDouble() * 0.05D,
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition pulsePosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double wave = (Math.sin((tick + effectIndex * 8L) * 0.16D * config.swirlSpeed) + 1.0D) * 0.5D;
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = Math.max(0.08D, config.radius * (0.35D + wave));
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.25D + random.nextDouble() * (player.getHeight() * config.heightScale),
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition fountainPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double heightProgress = ((tick + particleIndex * 7L + effectIndex * 11L) % 36L) / 36.0D;
        double radius = config.radius * heightProgress * 0.75D;
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.05D + heightProgress * (player.getHeight() * config.heightScale),
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition ringPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double angle = (particleIndex * 1.256D) + (effectIndex * 0.7D) + tick * 0.1D * config.swirlSpeed;
        double radius = Math.max(0.2D, config.radius);
        double height = player.getHeight() * (0.25D + ((effectIndex + particleIndex) % 3) * 0.22D);
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.12D + Math.min(player.getHeight() * config.heightScale, height),
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition vortexPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double progress = ((tick * 2L + particleIndex * 7L + effectIndex * 13L) % 64L) / 64.0D;
        double radius = Math.max(0.06D, config.radius * (1.15D - progress));
        double angle = tick * 0.18D * config.swirlSpeed + progress * Math.PI * 8.0D;
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.1D + progress * (player.getHeight() * config.heightScale),
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition cometPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double angle = tick * 0.11D * config.swirlSpeed + effectIndex * 1.2D;
        double trail = particleIndex * 0.07D + random.nextDouble() * 0.1D;
        double radius = config.radius + trail;
        return new ParticlePosition(
            player.getX() + Math.cos(angle - trail) * radius,
            player.getY() + 0.35D + random.nextDouble() * (player.getHeight() * Math.min(config.heightScale, 0.8D)),
            player.getZ() + Math.sin(angle - trail) * radius
        );
    }

    private static ParticlePosition sparkPosition(PlayerEntity player, Random random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = config.radius * (0.2D + random.nextDouble() * 1.4D);
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.1D + random.nextDouble() * (player.getHeight() * config.heightScale),
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition wavePosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double angle = particleIndex * 0.8D + effectIndex * 1.1D;
        double wave = Math.sin(tick * 0.12D * config.swirlSpeed + particleIndex) * 0.18D;
        double radius = Math.max(0.14D, config.radius + wave);
        double height = 0.25D + ((particleIndex + effectIndex) % 4) * player.getHeight() * 0.16D;
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + Math.min(player.getHeight() * config.heightScale, height) + random.nextDouble() * 0.08D,
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition doubleRingPosition(PlayerEntity player, Random random, long tick, int effectIndex, int particleIndex) {
        double angle = tick * 0.1D * config.swirlSpeed + particleIndex * 1.047D + effectIndex * 0.5D;
        double radius = Math.max(0.18D, config.radius * 0.95D);
        double height = (particleIndex % 2 == 0 ? 0.35D : 0.72D) * player.getHeight() * config.heightScale;
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + 0.12D + height + random.nextDouble() * 0.04D,
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static ParticlePosition rainPosition(PlayerEntity player, Random random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = random.nextDouble() * Math.max(0.18D, config.radius);
        return new ParticlePosition(
            player.getX() + Math.cos(angle) * radius,
            player.getY() + player.getHeight() * config.heightScale + 0.25D + random.nextDouble() * 0.25D,
            player.getZ() + Math.sin(angle) * radius
        );
    }

    private static int resolveColor(StatusEffectInstance effect, long tick, int effectIndex, int particleIndex) {
        return switch (config.colorMode) {
            case "rgb" -> MathHelper.hsvToRgb(((tick * config.colorCycleSpeed + effectIndex * 11L + particleIndex * 7L) % 180L) / 180.0F, 0.9F, 1.0F);
            case "rainbow" -> MathHelper.hsvToRgb(((tick * config.colorCycleSpeed + effectIndex * 18L + particleIndex * 12L) % 240L) / 240.0F, 0.82F, 1.0F);
            case "custom" -> resolveCustomColor(tick, effectIndex, particleIndex);
            default -> resolveEffectColor(effect);
        };
    }

    private static int resolvePreviewColor(long tick, int effectIndex, int particleIndex) {
        return switch (config.colorMode) {
            case "rgb" -> MathHelper.hsvToRgb(((tick * config.colorCycleSpeed + effectIndex * 11L + particleIndex * 7L) % 180L) / 180.0F, 0.9F, 1.0F);
            case "rainbow" -> MathHelper.hsvToRgb(((tick * config.colorCycleSpeed + effectIndex * 18L + particleIndex * 12L) % 240L) / 240.0F, 0.82F, 1.0F);
            case "custom" -> resolveCustomColor(tick, effectIndex, particleIndex);
            default -> 0xA3F7FF;
        };
    }

    private static int resolveCustomColor(long tick, int effectIndex, int particleIndex) {
        if (config.customColors.length == 0) {
            return 0xA3F7FF;
        }

        int index = Math.floorMod((int) (tick / Math.max(1, 26 - config.colorCycleSpeed)) + effectIndex + particleIndex, config.customColors.length);
        return config.customColors[index];
    }

    private static int resolveEffectColor(StatusEffectInstance effect) {
        int color = effect.getEffectType().value().getColor();
        return color == 0 ? 0xA3F7FF : color;
    }

    private static final class BrewBubbleParticle extends SpriteBillboardParticle {
        private final SpriteProvider spriteProvider;

        private BrewBubbleParticle(ClientWorld world, double x, double y, double z, double colorValue, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
            super(world, x, y, z, 0.0D, velocityY, 0.0D);
            this.spriteProvider = spriteProvider;
            int color = MathHelper.floor(colorValue) & 0xFFFFFF;
            red = ((color >> 16) & 0xFF) / 255.0F;
            green = ((color >> 8) & 0xFF) / 255.0F;
            blue = (color & 0xFF) / 255.0F;
            alpha = 0.88F;
            scale = 0.17F + random.nextFloat() * 0.05F;
            maxAge = 18 + random.nextInt(10);
            velocityX = (random.nextDouble() - 0.5D) * 0.006D;
            velocityY = Math.max(0.002D, velocityY);
            velocityZ = (random.nextDouble() - 0.5D) * 0.006D;
            setSpriteForAge(spriteProvider);
        }

        @Override
        public void tick() {
            super.tick();
            velocityY *= 0.98D;
            alpha = 0.88F * (1.0F - (age / (float) maxAge) * 0.55F);
            setSpriteForAge(spriteProvider);
        }

        @Override
        public ParticleTextureSheet getType() {
            return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
        }

        private record Factory(SpriteProvider spriteProvider) implements ParticleFactory<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
                return new BrewBubbleParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
            }
        }
    }

    private record ParticlePosition(double x, double y, double z) {
    }
}
