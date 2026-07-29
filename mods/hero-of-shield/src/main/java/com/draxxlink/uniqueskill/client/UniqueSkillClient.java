package com.draxxlink.uniqueskill.client;

import com.draxxlink.uniqueskill.client.audio.UniqueSkillActivationAudioPlayer;
import com.draxxlink.uniqueskill.client.combat.AutoAttackHandler;
import com.draxxlink.uniqueskill.client.combat.UniqueSkillCombatTracker;
import com.draxxlink.uniqueskill.client.effect.UniqueSkillAwakeningEffect;
import com.draxxlink.uniqueskill.client.hud.UniqueSkillHud;
import com.draxxlink.uniqueskill.client.render.UniqueSkillBlockOutlineRenderer;
import com.draxxlink.uniqueskill.client.tooltip.UniqueSkillTooltipEnhancer;
import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import com.draxxlink.uniqueskill.entity.AwarenessSnapshot;
import com.draxxlink.uniqueskill.entity.EntityHelper;
import com.draxxlink.uniqueskill.entity.ThreatDetector;
import com.draxxlink.uniqueskill.entity.ThreatSnapshot;
import com.draxxlink.uniqueskill.state.UniqueSkillState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class UniqueSkillClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static final String MOD_ID = "unique_skill";
    private static final int PERCEPTION_PULSE_INTERVAL_TICKS = 60;
    private static final int PERCEPTION_FEEDBACK_TICKS = 8;
    private static final int DIRECTIONAL_INDICATOR_HOLD_TICKS = 80;
    private static final int RANGE_ENTRY_ALERT_COOLDOWN_TICKS = 24;
    private static final SoundEvent PERCEPTION_PULSE_SOUND = SoundEvent.of(Identifier.of(MOD_ID, "perception_pulse"));

    private AwarenessSnapshot cachedAwarenessSnapshot = AwarenessSnapshot.empty();
    private ThreatSnapshot cachedThreatSnapshot = ThreatSnapshot.empty();
    private long nextThreatScanTick;
    private long nextAwarenessScanTick;
    private long nextPerceptionPulseTick;
    private long leftIndicatorHoldUntilTick;
    private long rightIndicatorHoldUntilTick;
    private long topIndicatorHoldUntilTick;
    private long bottomIndicatorHoldUntilTick;
    private long backIndicatorHoldUntilTick;
    private long nextRangeAlertTick;
    private int lastNearbyPlayerCount;
    private int lastNearbyHostileMobCount;
    private boolean wasAttackKeyPressed;
    private boolean wasShieldBlocking;

    @Override
    public void onInitializeClient() {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        UniqueSkillState.setEnabled(config.modEnabled);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.unique_skill.toggle",
            InputUtil.Type.KEYSYM,
            config.toggleKey,
            "category.unique_skill"
        ));
        UniqueSkillHud.register();
        UniqueSkillBlockOutlineRenderer.register();
        UniqueSkillTooltipEnhancer.register();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resetRuntimeState();
            UniqueSkillContentInsight.reset();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            resetRuntimeState();
            UniqueSkillContentInsight.reset();
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        PlayerEntity player = client.player;
        long worldTick = client.world.getTime();
        UniqueSkillCombatTracker.tick(player);

        while (toggleKey.wasPressed()) {
            toggleMod(client);
        }

        boolean isAttackButtonPressed = isAttackKeyPressed(client);
        AutoAttackHandler.setAutoAttackActive(isAttackButtonPressed);
        tryTrackManualAttack(client, player, worldTick, isAttackButtonPressed);
        AutoAttackHandler.tick(client, player, worldTick);
        wasAttackKeyPressed = isAttackButtonPressed;

        UniqueSkillContentInsight.tick(client, worldTick);
        UniqueSkillAwakeningEffect.tick(client, player, worldTick);

        if (!UniqueSkillState.isEnabled()) {
            UniqueSkillState.setNearbyThreatCount(0);
            UniqueSkillState.setNearbyHostileMobCount(0);
            UniqueSkillState.setNearbyNeutralMobCount(0);
            UniqueSkillState.setNearbyPlayerCount(0);
            UniqueSkillState.setNearbySpectatorCount(0);
            boolean shielding = EntityHelper.isUsingShield(player);
            UniqueSkillState.setShieldActive(shielding);
            wasShieldBlocking = shielding;
            updateDirectionalIndicators(player, ThreatSnapshot.empty());
            UniqueSkillState.setPerceptionFeedbackUntilTick(0L);
            return;
        }

        ThreatSnapshot threatSnapshot = getThreatSnapshot(player, config, worldTick);
        AwarenessSnapshot awarenessSnapshot = getAwarenessSnapshot(player, config, worldTick);
        processRangeEntryAlert(player, config, awarenessSnapshot, worldTick);
        UniqueSkillState.setNearbyThreatCount(threatSnapshot.nearbyThreats().size());
        UniqueSkillState.setNearbyHostileMobCount(awarenessSnapshot.hostileMobCount());
        UniqueSkillState.setNearbyNeutralMobCount(awarenessSnapshot.neutralMobCount());
        UniqueSkillState.setNearbyPlayerCount(awarenessSnapshot.nearbyPlayerCount());
        UniqueSkillState.setNearbySpectatorCount(awarenessSnapshot.spectatorPlayerCount());
        updateDirectionalIndicators(player, threatSnapshot);
        processPerceptionPulse(player, threatSnapshot, worldTick);
        processPerfectReactionWindow(player, worldTick);
    }

    private ThreatSnapshot getThreatSnapshot(PlayerEntity player, UniqueSkillConfig config, long worldTick) {
        if (worldTick < nextThreatScanTick) {
            return cachedThreatSnapshot;
        }

        cachedThreatSnapshot = ThreatDetector.scanThreats(player, config.detectionRange, worldTick, true);
        nextThreatScanTick = worldTick + (cachedThreatSnapshot.hasIncomingThreat() ? 1L : config.detectionIntervalTicks);
        return cachedThreatSnapshot;
    }

    private AwarenessSnapshot getAwarenessSnapshot(PlayerEntity player, UniqueSkillConfig config, long worldTick) {
        if (worldTick < nextAwarenessScanTick) {
            return cachedAwarenessSnapshot;
        }

        cachedAwarenessSnapshot = ThreatDetector.scanAwareness(player, config.detectionRange, worldTick);
        nextAwarenessScanTick = worldTick + config.detectionIntervalTicks;
        return cachedAwarenessSnapshot;
    }

    private void resetRuntimeState() {
        AutoAttackHandler.reset();
        UniqueSkillState.resetCombatState();
        cachedThreatSnapshot = ThreatSnapshot.empty();
        cachedAwarenessSnapshot = AwarenessSnapshot.empty();
        nextThreatScanTick = 0L;
        nextAwarenessScanTick = 0L;
        nextPerceptionPulseTick = 0L;
        leftIndicatorHoldUntilTick = 0L;
        rightIndicatorHoldUntilTick = 0L;
        topIndicatorHoldUntilTick = 0L;
        bottomIndicatorHoldUntilTick = 0L;
        backIndicatorHoldUntilTick = 0L;
        nextRangeAlertTick = 0L;
        lastNearbyPlayerCount = 0;
        lastNearbyHostileMobCount = 0;
        wasAttackKeyPressed = false;
        wasShieldBlocking = false;
        UniqueSkillCombatTracker.reset();
        UniqueSkillAwakeningEffect.reset();
    }

    private void sendToggleMessage(PlayerEntity player, boolean enabled) {
        MutableText prefix = Text.literal("[Habilidade Única] ").formatted(Formatting.GOLD);
        MutableText status = Text.translatable(
            enabled ? "message.unique_skill.enabled" : "message.unique_skill.disabled"
        ).formatted(enabled ? Formatting.GREEN : Formatting.RED);
        player.sendMessage(prefix.append(status), true);
    }

    private void updateDirectionalIndicators(PlayerEntity player, ThreatSnapshot threatSnapshot) {
        long worldTick = player.getWorld().getTime();
        int leftCount = 0;
        int rightCount = 0;
        int topCount = 0;
        int bottomCount = 0;
        int backCount = 0;
        final float horizontalFov = 70.0F;
        final float verticalFov = 42.0F;

        for (LivingEntity threat : threatSnapshot.nearbyThreats()) {
            if (!threat.isAlive()) {
                continue;
            }

            Vec3d toThreat = threat.getEyePos().subtract(player.getEyePos());
            if (toThreat.lengthSquared() < 0.001D) {
                continue;
            }

            double horizontalDistance = Math.sqrt((toThreat.x * toThreat.x) + (toThreat.z * toThreat.z));
            float targetYaw = (float) Math.toDegrees(Math.atan2(toThreat.z, toThreat.x)) - 90.0F;
            float targetPitch = (float) -Math.toDegrees(Math.atan2(toThreat.y, horizontalDistance));
            float yawDelta = MathHelper.wrapDegrees(targetYaw - player.getYaw());
            float pitchDelta = MathHelper.wrapDegrees(targetPitch - player.getPitch());

            if (Math.abs(yawDelta) <= horizontalFov * 0.5F && Math.abs(pitchDelta) <= verticalFov * 0.5F) {
                continue;
            }

            if (Math.abs(yawDelta) >= 145.0F) {
                backCount++;
                continue;
            }

            if (Math.abs(yawDelta) >= Math.abs(pitchDelta) || Math.abs(yawDelta) > horizontalFov * 0.5F) {
                if (yawDelta < 0.0F) {
                    leftCount++;
                } else {
                    rightCount++;
                }
            } else if (pitchDelta < 0.0F) {
                topCount++;
            } else {
                bottomCount++;
            }
        }

        UniqueSkillState.setOffscreenThreatLeftCount(trackDirectionalCount(leftCount, UniqueSkillState.getOffscreenThreatLeftCount(), worldTick, Direction.LEFT));
        UniqueSkillState.setOffscreenThreatRightCount(trackDirectionalCount(rightCount, UniqueSkillState.getOffscreenThreatRightCount(), worldTick, Direction.RIGHT));
        UniqueSkillState.setOffscreenThreatTopCount(trackDirectionalCount(topCount, UniqueSkillState.getOffscreenThreatTopCount(), worldTick, Direction.TOP));
        UniqueSkillState.setOffscreenThreatBottomCount(trackDirectionalCount(bottomCount, UniqueSkillState.getOffscreenThreatBottomCount(), worldTick, Direction.BOTTOM));
        UniqueSkillState.setOffscreenThreatBackCount(trackDirectionalCount(backCount, UniqueSkillState.getOffscreenThreatBackCount(), worldTick, Direction.BACK));
    }

    private void processPerceptionPulse(PlayerEntity player, ThreatSnapshot threatSnapshot, long worldTick) {
        if (worldTick < nextPerceptionPulseTick) {
            return;
        }

        nextPerceptionPulseTick = worldTick + PERCEPTION_PULSE_INTERVAL_TICKS;

        boolean threatActive = threatSnapshot.hasIncomingThreat() || !threatSnapshot.nearbyThreats().isEmpty();
        if (!threatActive) {
            return;
        }

        UniqueSkillState.setPerceptionFeedbackUntilTick(worldTick + PERCEPTION_FEEDBACK_TICKS);
    }

    private void processRangeEntryAlert(PlayerEntity player, UniqueSkillConfig config, AwarenessSnapshot awarenessSnapshot, long worldTick) {
        int playerDelta = Math.max(0, awarenessSnapshot.nearbyPlayerCount() - lastNearbyPlayerCount);
        int hostileDelta = Math.max(0, awarenessSnapshot.hostileMobCount() - lastNearbyHostileMobCount);

        lastNearbyPlayerCount = awarenessSnapshot.nearbyPlayerCount();
        lastNearbyHostileMobCount = awarenessSnapshot.hostileMobCount();

        if (worldTick < nextRangeAlertTick) {
            return;
        }

        if (playerDelta <= 0 && hostileDelta <= 0) {
            return;
        }

        nextRangeAlertTick = worldTick + RANGE_ENTRY_ALERT_COOLDOWN_TICKS;
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(PERCEPTION_PULSE_SOUND, configVolume(config, 1.0F), 1.0F));
    }

    private void processPerfectReactionWindow(PlayerEntity player, long worldTick) {
        boolean shielding = EntityHelper.isUsingShield(player);
        UniqueSkillState.setShieldActive(shielding);

        wasShieldBlocking = shielding;
    }

    private int trackDirectionalCount(int currentCount, int existingCount, long worldTick, Direction direction) {
        if (currentCount > 0) {
            long holdUntil = worldTick + DIRECTIONAL_INDICATOR_HOLD_TICKS;
            setDirectionalHoldUntil(direction, holdUntil);
            return currentCount;
        }

        return worldTick <= getDirectionalHoldUntil(direction) ? existingCount : 0;
    }

    private long getDirectionalHoldUntil(Direction direction) {
        return switch (direction) {
            case LEFT -> leftIndicatorHoldUntilTick;
            case RIGHT -> rightIndicatorHoldUntilTick;
            case TOP -> topIndicatorHoldUntilTick;
            case BOTTOM -> bottomIndicatorHoldUntilTick;
            case BACK -> backIndicatorHoldUntilTick;
        };
    }

    private void setDirectionalHoldUntil(Direction direction, long tick) {
        switch (direction) {
            case LEFT -> leftIndicatorHoldUntilTick = tick;
            case RIGHT -> rightIndicatorHoldUntilTick = tick;
            case TOP -> topIndicatorHoldUntilTick = tick;
            case BOTTOM -> bottomIndicatorHoldUntilTick = tick;
            case BACK -> backIndicatorHoldUntilTick = tick;
        }
    }

    private float configVolume(UniqueSkillConfig config, float baseVolume) {
        float normalizedVolume = MathHelper.clamp(config.alertVolume, 0.0F, 1.0F);
        float perceptualVolume = normalizedVolume * normalizedVolume;
        return MathHelper.clamp(baseVolume * perceptualVolume, 0.0F, 1.0F);
    }

    private float ecolocationVolume(UniqueSkillConfig config, float baseVolume) {
        float normalizedVolume = MathHelper.clamp(config.ecolocationVolume, 0.0F, 1.0F);
        float perceptualVolume = normalizedVolume * normalizedVolume;
        return MathHelper.clamp(baseVolume * perceptualVolume, 0.0F, 1.0F);
    }

    private void tryTrackManualAttack(MinecraftClient client, PlayerEntity player, long worldTick, boolean isAttackButtonPressed) {
        if (!isAttackButtonPressed || wasAttackKeyPressed || player.isUsingItem()) {
            return;
        }

        if (player.getAttackCooldownProgress(0.0F) < 1.0F) {
            return;
        }

        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return;
        }

        EntityHitResult entityHitResult = (EntityHitResult) client.crosshairTarget;
        UniqueSkillCombatTracker.recordHit(player, entityHitResult.getEntity(), Hand.MAIN_HAND, worldTick);
    }

    public static String getModId() {
        return MOD_ID;
    }

    public static void applyRuntimeConfig(MinecraftClient client) {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        UniqueSkillState.setEnabled(config.modEnabled);

        if (toggleKey != null) {
            toggleKey.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(config.toggleKey));
            KeyBinding.updateKeysByCode();
        }
    }

    public static void toggleMod(MinecraftClient client) {
        if (client == null || client.player == null) {
            return;
        }

        UniqueSkillConfig config = UniqueSkillConfig.getInstance();
        UniqueSkillClient runtime = new UniqueSkillClient();
        boolean wasEnabled = UniqueSkillState.isEnabled();
        boolean enabled = !wasEnabled;
        UniqueSkillState.setEnabled(enabled);
        config.modEnabled = enabled;
        config.save();
        runtime.resetRuntimeState();

        if (!wasEnabled && enabled) {
            UniqueSkillActivationAudioPlayer.play(runtime.ecolocationVolume(config, 1.0F));
            if (client.world != null) {
                UniqueSkillAwakeningEffect.triggerAwakening(client.world.getTime());
            }
        }

        if (config.showMessages) {
            runtime.sendToggleMessage(client.player, enabled);
        }
    }

    private enum Direction {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        BACK
    }

    private static boolean isAttackKeyPressed(MinecraftClient client) {
        if (client.currentScreen != null) {
            return false;
        }

        return client.options != null && client.options.attackKey.isPressed();
    }
}
