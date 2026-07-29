package com.draxxlink.uniqueskill.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UniqueSkillConfig {
    private static final double[] DETECTION_RANGE_OPTIONS = {16.0D, 32.0D, 64.0D};
    public static final double DEFAULT_DETECTION_RANGE = 32.0D;
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("unique_skill.json");
    private static final Path PREVIOUS_CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("auralink.json");
    private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("hero_of_shield.json");

    private static UniqueSkillConfig instance;

    public boolean modEnabled = false;
    public int toggleKey = GLFW.GLFW_KEY_J;
    public boolean bubbleAuraEnabled = false;
    public boolean bubbleSharedMode = true;
    public int bubbleToggleKey = GLFW.GLFW_KEY_O;
    public String bubbleColor = "GOLD";
    public boolean bubbleRgbMode = false;
    public int bubbleDensity = 2;
    public double detectionRange = DEFAULT_DETECTION_RANGE;
    public boolean blockOutlineEnabled = true;
    public String blockOutlineColor = "CYAN";
    public float blockOutlineAlpha = 0.55F;
    public boolean blockInspectorEnabled = true;
    public boolean showPlayerWarnings = true;
    public boolean showPresenceHud = true;
    public boolean showFoodTooltip = true;
    public float ecolocationVolume = 0.55F;
    public float alertVolume = 0.55F;
    public boolean showMessages = true;
    public boolean showHud = true;
    public int detectionIntervalTicks = 2;
    public boolean autoAttackHostiles = true;
    public boolean autoAttackNeutrals = false;
    public boolean autoAttackPassives = false;
    public boolean autoAttackPlayers = true;
    public int autoAttackAimToleranceDegrees = 8;

    public static UniqueSkillConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static UniqueSkillConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            migrateLegacyConfig(PREVIOUS_CONFIG_PATH);
        }

        if (!Files.exists(CONFIG_PATH)) {
            migrateLegacyConfig(LEGACY_CONFIG_PATH);
        }

        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                UniqueSkillConfig loaded = GSON.fromJson(reader, UniqueSkillConfig.class);
                UniqueSkillConfig config = loaded == null ? new UniqueSkillConfig() : loaded;
                config.sanitize();
                config.save();
                return config;
            } catch (IOException exception) {
                System.err.println("[Habilidade Unica] Failed to load config: " + exception.getMessage());
            }
        }

        UniqueSkillConfig config = new UniqueSkillConfig();
        config.save();
        return config;
    }

    private static void migrateLegacyConfig(Path sourcePath) {
        if (!Files.exists(sourcePath)) {
            return;
        }

        try {
            Files.copy(sourcePath, CONFIG_PATH);
        } catch (IOException exception) {
            System.err.println("[Habilidade Unica] Failed to migrate legacy config: " + exception.getMessage());
        }
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            System.err.println("[Habilidade Unica] Failed to save config: " + exception.getMessage());
        }
    }

    public UniqueSkillConfig copy() {
        UniqueSkillConfig copy = new UniqueSkillConfig();
        copy.modEnabled = modEnabled;
        copy.toggleKey = toggleKey;
        copy.bubbleAuraEnabled = bubbleAuraEnabled;
        copy.bubbleSharedMode = bubbleSharedMode;
        copy.bubbleToggleKey = bubbleToggleKey;
        copy.bubbleColor = bubbleColor;
        copy.bubbleRgbMode = bubbleRgbMode;
        copy.bubbleDensity = bubbleDensity;
        copy.detectionRange = detectionRange;
        copy.blockOutlineEnabled = blockOutlineEnabled;
        copy.blockOutlineColor = blockOutlineColor;
        copy.blockOutlineAlpha = blockOutlineAlpha;
        copy.blockInspectorEnabled = blockInspectorEnabled;
        copy.showPlayerWarnings = showPlayerWarnings;
        copy.showPresenceHud = showPresenceHud;
        copy.showFoodTooltip = showFoodTooltip;
        copy.ecolocationVolume = ecolocationVolume;
        copy.alertVolume = alertVolume;
        copy.showMessages = showMessages;
        copy.showHud = showHud;
        copy.detectionIntervalTicks = detectionIntervalTicks;
        copy.autoAttackHostiles = autoAttackHostiles;
        copy.autoAttackNeutrals = autoAttackNeutrals;
        copy.autoAttackPassives = autoAttackPassives;
        copy.autoAttackPlayers = autoAttackPlayers;
        copy.autoAttackAimToleranceDegrees = autoAttackAimToleranceDegrees;
        return copy;
    }

    public void copyFrom(UniqueSkillConfig other) {
        modEnabled = other.modEnabled;
        toggleKey = other.toggleKey;
        bubbleAuraEnabled = other.bubbleAuraEnabled;
        bubbleSharedMode = other.bubbleSharedMode;
        bubbleToggleKey = other.bubbleToggleKey;
        bubbleColor = other.bubbleColor;
        bubbleRgbMode = other.bubbleRgbMode;
        bubbleDensity = other.bubbleDensity;
        detectionRange = other.detectionRange;
        blockOutlineEnabled = other.blockOutlineEnabled;
        blockOutlineColor = other.blockOutlineColor;
        blockOutlineAlpha = other.blockOutlineAlpha;
        blockInspectorEnabled = other.blockInspectorEnabled;
        showPlayerWarnings = other.showPlayerWarnings;
        showPresenceHud = other.showPresenceHud;
        showFoodTooltip = other.showFoodTooltip;
        ecolocationVolume = other.ecolocationVolume;
        alertVolume = other.alertVolume;
        showMessages = other.showMessages;
        showHud = other.showHud;
        detectionIntervalTicks = other.detectionIntervalTicks;
        autoAttackHostiles = other.autoAttackHostiles;
        autoAttackNeutrals = other.autoAttackNeutrals;
        autoAttackPassives = other.autoAttackPassives;
        autoAttackPlayers = other.autoAttackPlayers;
        autoAttackAimToleranceDegrees = other.autoAttackAimToleranceDegrees;
        sanitize();
    }

    public void resetToDefaults() {
        UniqueSkillConfig defaults = new UniqueSkillConfig();
        copyFrom(defaults);
    }

    private void sanitize() {
        toggleKey = sanitizeKeyCode(toggleKey, GLFW.GLFW_KEY_J);
        bubbleToggleKey = sanitizeKeyCode(bubbleToggleKey, GLFW.GLFW_KEY_O);
        bubbleColor = sanitizeBubbleColor(bubbleColor);
        bubbleDensity = clamp(bubbleDensity, 1, 3);
        detectionRange = sanitizeDetectionRange(detectionRange);
        blockOutlineColor = sanitizeOutlineColor(blockOutlineColor);
        blockOutlineAlpha = clamp(blockOutlineAlpha, 0.20F, 0.85F);
        ecolocationVolume = clamp(ecolocationVolume, 0.0F, 1.0F);
        alertVolume = clamp(alertVolume, 0.0F, 1.0F);
        detectionIntervalTicks = clamp(detectionIntervalTicks, 1, 5);
        autoAttackAimToleranceDegrees = clamp(autoAttackAimToleranceDegrees, 2, 20);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int sanitizeKeyCode(int keyCode, int fallback) {
        return keyCode >= GLFW.GLFW_KEY_SPACE ? keyCode : fallback;
    }

    private static double sanitizeDetectionRange(double value) {
        double closest = DETECTION_RANGE_OPTIONS[0];
        double smallestDistance = Math.abs(value - closest);
        for (int index = 1; index < DETECTION_RANGE_OPTIONS.length; index++) {
            double option = DETECTION_RANGE_OPTIONS[index];
            double distance = Math.abs(value - option);
            if (distance < smallestDistance) {
                closest = option;
                smallestDistance = distance;
            }
        }
        return closest;
    }

    private static String sanitizeBubbleColor(String value) {
        return switch (value == null ? "" : value.toUpperCase()) {
            case "GOLD", "RED", "PURPLE" -> value.toUpperCase();
            default -> "GOLD";
        };
    }

    private static String sanitizeOutlineColor(String value) {
        return switch (value == null ? "" : value.toUpperCase()) {
            case "CYAN", "GOLD", "MAGENTA", "WHITE" -> value.toUpperCase();
            default -> "CYAN";
        };
    }
}
