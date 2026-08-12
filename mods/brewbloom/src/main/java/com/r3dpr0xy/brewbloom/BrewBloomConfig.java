package com.r3dpr0xy.brewbloom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BrewBloomConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int[] DEFAULT_CUSTOM_COLORS = {0xFF2A2A, 0xFFFFFF, 0x7A0000};
    private static final String[] COLOR_MODES = {"effect", "rgb", "rainbow", "custom"};
    private static final String[] PARTICLE_STYLES = {
        "orbit", "aura", "halo", "trail", "spiral", "crown", "pulse", "fountain", "ring",
        "vortex", "comet", "spark", "wave", "double_ring", "rain"
    };
    private static final String[] TEXTURE_MODES = {"vanilla", "bloom", "soft", "pixel", "star", "ring", "sparkle"};
    private static final String[] MENU_SOUND_STYLES = {"click", "crystal", "xp", "allay", "ender", "blaze", "sculk", "evoker"};
    private static final Pattern LEGACY_RGB_PATTERN = Pattern.compile("(?i)(?:[&\\u00A7]x)((?:[&\\u00A7][0-9a-f]){6})");
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)(?:&#|<#|#)?([0-9a-f]{6})(?:>)?");
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("brewbloom.json");

    public boolean enabled = true;
    public String colorMode = "effect";
    public String particleStyle = "orbit";
    public String textureMode = "vanilla";
    public String[] colors = {"#FF2A2A", "#FFFFFF", "#7A0000"};
    public double radius = 0.38D;
    public int density = 2;
    public int effectLimit = 4;
    public int totalBubbleLimit = 12;
    public int crowdedEffectStart = 3;
    public double heightScale = 0.9D;
    public double riseSpeed = 1.0D;
    public double swirlSpeed = 1.0D;
    public int colorCycleSpeed = 8;
    public boolean showWithoutEffects = false;
    public boolean menuSounds = true;
    public String menuSoundStyle = "crystal";
    public transient int[] customColors = DEFAULT_CUSTOM_COLORS;

    public static BrewBloomConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            BrewBloomConfig config = new BrewBloomConfig();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            BrewBloomConfig config = GSON.fromJson(reader, BrewBloomConfig.class);
            return config == null ? new BrewBloomConfig() : config.clamped();
        } catch (IOException exception) {
            return new BrewBloomConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(clamped(), writer);
            }
        } catch (IOException ignored) {
            // The mod can keep running with in-memory defaults if config writing fails.
        }
    }

    BrewBloomConfig clamped() {
        if (totalBubbleLimit <= 0) {
            totalBubbleLimit = 12;
        }
        if (crowdedEffectStart <= 0) {
            crowdedEffectStart = 3;
        }

        radius = clamp(radius, 0.15D, 1.25D);
        density = clamp(density, 1, 8);
        effectLimit = clamp(effectLimit, 1, 8);
        totalBubbleLimit = clamp(totalBubbleLimit, 3, 48);
        crowdedEffectStart = clamp(crowdedEffectStart, 1, 8);
        heightScale = clamp(heightScale, 0.25D, 1.5D);
        riseSpeed = clamp(riseSpeed, 0.25D, 2.0D);
        swirlSpeed = clamp(swirlSpeed, 0.25D, 2.5D);
        colorCycleSpeed = clamp(colorCycleSpeed, 2, 24);
        colorMode = normalizeOption(colorMode, COLOR_MODES, "effect");
        particleStyle = normalizeOption(particleStyle, PARTICLE_STYLES, "orbit");
        textureMode = normalizeOption(textureMode, TEXTURE_MODES, "vanilla");
        menuSoundStyle = normalizeOption(menuSoundStyle, MENU_SOUND_STYLES, "crystal");
        customColors = parseCustomColors(colors);
        return this;
    }

    void setColorMode(String value) {
        colorMode = normalizeOption(value, COLOR_MODES, "effect");
        save();
    }

    void cycleParticleStyle() {
        particleStyle = nextOption(particleStyle, PARTICLE_STYLES, "orbit");
        save();
    }

    void setParticleStyle(String value) {
        particleStyle = normalizeOption(value, PARTICLE_STYLES, "orbit");
        save();
    }

    void cycleTextureMode() {
        textureMode = nextOption(textureMode, TEXTURE_MODES, "vanilla");
        save();
    }

    void cycleMenuSoundStyle() {
        menuSoundStyle = nextOption(menuSoundStyle, MENU_SOUND_STYLES, "crystal");
        save();
    }

    void resetDefaults() {
        enabled = true;
        colorMode = "effect";
        particleStyle = "orbit";
        textureMode = "vanilla";
        colors = new String[]{"#FF2A2A", "#FFFFFF", "#7A0000"};
        radius = 0.38D;
        density = 2;
        effectLimit = 4;
        totalBubbleLimit = 12;
        crowdedEffectStart = 3;
        heightScale = 0.9D;
        riseSpeed = 1.0D;
        swirlSpeed = 1.0D;
        colorCycleSpeed = 8;
        showWithoutEffects = false;
        menuSounds = true;
        menuSoundStyle = "crystal";
        save();
    }

    String exportProfile() {
        String json = GSON.toJson(clamped());
        return "BB1:" + Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    boolean importProfile(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String trimmed = value.trim();
        if (trimmed.startsWith("BB1:")) {
            try {
                String json = new String(Base64.getDecoder().decode(trimmed.substring(4)), java.nio.charset.StandardCharsets.UTF_8);
                BrewBloomConfig imported = GSON.fromJson(json, BrewBloomConfig.class);
                if (imported == null) {
                    return false;
                }

                copyFrom(imported.clamped());
                save();
                return true;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }

        List<String> importedColors = extractColors(trimmed);
        if (importedColors.isEmpty()) {
            return false;
        }

        setColors(importedColors);
        return true;
    }

    private void copyFrom(BrewBloomConfig source) {
        enabled = source.enabled;
        colorMode = source.colorMode;
        particleStyle = source.particleStyle;
        textureMode = source.textureMode;
        colors = Arrays.copyOf(source.colors, source.colors.length);
        radius = source.radius;
        density = source.density;
        effectLimit = source.effectLimit;
        totalBubbleLimit = source.totalBubbleLimit;
        crowdedEffectStart = source.crowdedEffectStart;
        heightScale = source.heightScale;
        riseSpeed = source.riseSpeed;
        swirlSpeed = source.swirlSpeed;
        colorCycleSpeed = source.colorCycleSpeed;
        showWithoutEffects = source.showWithoutEffects;
        menuSounds = source.menuSounds;
        menuSoundStyle = source.menuSoundStyle;
        customColors = parseCustomColors(colors);
    }

    void applyPreset(String preset) {
        switch (preset) {
            case "neon" -> {
                colorMode = "custom";
                colors = new String[]{"#00E5FF", "#FF2AD4", "#42FF3F"};
                density = 4;
                radius = 0.5D;
                riseSpeed = 1.2D;
                swirlSpeed = 1.6D;
                colorCycleSpeed = 14;
                particleStyle = "orbit";
            }
            case "dark" -> {
                colorMode = "custom";
                colors = new String[]{"#FF2A2A", "#0A0A0A", "#7A0000"};
                density = 2;
                radius = 0.34D;
                riseSpeed = 0.85D;
                swirlSpeed = 0.9D;
                colorCycleSpeed = 7;
                particleStyle = "aura";
            }
            case "potion" -> {
                colorMode = "effect";
                density = 3;
                radius = 0.4D;
                riseSpeed = 1.0D;
                swirlSpeed = 1.0D;
                colorCycleSpeed = 8;
                particleStyle = "aura";
            }
            case "calm" -> {
                colorMode = "custom";
                colors = new String[]{"#BFD7FF", "#E6D1FF", "#FFD5E5"};
                density = 1;
                radius = 0.3D;
                riseSpeed = 0.65D;
                swirlSpeed = 0.5D;
                colorCycleSpeed = 4;
                particleStyle = "halo";
            }
            default -> {
            }
        }

        save();
    }

    void adjustRadius(double amount) {
        radius += amount;
        save();
    }

    void adjustDensity(int amount) {
        density += amount;
        save();
    }

    void adjustEffectLimit(int amount) {
        effectLimit += amount;
        save();
    }

    void adjustTotalBubbleLimit(int amount) {
        totalBubbleLimit += amount;
        save();
    }

    void adjustCrowdedEffectStart(int amount) {
        crowdedEffectStart += amount;
        save();
    }

    void adjustHeightScale(double amount) {
        heightScale += amount;
        save();
    }

    void adjustRiseSpeed(double amount) {
        riseSpeed += amount;
        save();
    }

    void adjustSwirlSpeed(double amount) {
        swirlSpeed += amount;
        save();
    }

    void adjustColorCycleSpeed(int amount) {
        colorCycleSpeed += amount;
        save();
    }

    void setColor(int index, String value) {
        setColor(index, value, true);
    }

    void setColorDraft(int index, String value) {
        setColor(index, value, false);
    }

    private void setColor(int index, String value, boolean persist) {
        String normalized = normalizeColorInput(value);
        if (index < 0 || index >= 3 || normalized == null) {
            return;
        }

        ensureColorSlots();
        colors[index] = normalized;
        if (persist) {
            save();
        } else {
            clamped();
        }
    }

    void setColors(List<String> values) {
        if (values.isEmpty()) {
            return;
        }

        if (values.size() == 1) {
            setColor(0, values.get(0));
        } else if (values.size() == 2) {
            setGradient(values.get(0), values.get(1));
        } else {
            for (int index = 0; index < 3; index++) {
                setColor(index, values.get(index));
            }
        }
    }

    void setGradient(String startValue, String endValue) {
        String startHex = normalizeColorInput(startValue);
        String endHex = normalizeColorInput(endValue);
        if (startHex == null || endHex == null) {
            return;
        }

        int start = parseHexColor(startHex);
        int end = parseHexColor(endHex);
        setColor(0, startHex);
        setColor(1, String.format(Locale.ROOT, "#%02X%02X%02X",
            (((start >> 16) & 0xFF) + ((end >> 16) & 0xFF)) / 2,
            (((start >> 8) & 0xFF) + ((end >> 8) & 0xFF)) / 2,
            ((start & 0xFF) + (end & 0xFF)) / 2
        ));
        setColor(2, endHex);
    }

    void adjustColorComponent(int index, int component, int amount) {
        int color = colorAt(index);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        if (component == 0) {
            red = clamp(red + amount, 0, 255);
        } else if (component == 1) {
            green = clamp(green + amount, 0, 255);
        } else {
            blue = clamp(blue + amount, 0, 255);
        }

        setColor(index, String.format(Locale.ROOT, "#%02X%02X%02X", red, green, blue));
    }

    int colorAt(int index) {
        if (colors == null || index < 0 || index >= colors.length || !isValidHexColor(colors[index])) {
            return DEFAULT_CUSTOM_COLORS[Math.max(0, Math.min(index, DEFAULT_CUSTOM_COLORS.length - 1))];
        }

        return parseHexColor(colors[index]);
    }

    static boolean isValidHexColor(String value) {
        return normalizeColorInput(value) != null;
    }

    static String normalizeColorInput(String value) {
        List<String> colors = extractColors(value);
        return colors.isEmpty() ? null : colors.get(0);
    }

    static String normalizeHexColor(String value) {
        String normalized = normalizeColorInput(value);
        return normalized == null ? "#FFFFFF" : normalized;
    }

    static List<String> extractColors(String value) {
        List<String> colors = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return colors;
        }

        Matcher legacyMatcher = LEGACY_RGB_PATTERN.matcher(value);
        while (legacyMatcher.find()) {
            addColor(colors, legacyMatcher.group(1).replace("&", "").replace("\u00A7", ""));
        }

        Matcher hexMatcher = HEX_PATTERN.matcher(value);
        while (hexMatcher.find()) {
            addColor(colors, hexMatcher.group(1));
        }

        return colors;
    }

    private void ensureColorSlots() {
        if (colors == null || colors.length != 3) {
            colors = new String[]{"#FF2A2A", "#FFFFFF", "#7A0000"};
        }
    }

    private static void addColor(List<String> colors, String value) {
        if (value != null && value.matches("(?i)[0-9a-f]{6}")) {
            String normalized = "#" + value.toUpperCase(Locale.ROOT);
            if (!colors.contains(normalized)) {
                colors.add(normalized);
            }
        }
    }

    private static String normalizeOption(String value, String[] values, String fallback) {
        if (value == null) {
            return fallback;
        }

        String normalized = value.toLowerCase(Locale.ROOT).trim();
        return Arrays.asList(values).contains(normalized) ? normalized : fallback;
    }

    private static String nextOption(String current, String[] values, String fallback) {
        String normalized = normalizeOption(current, values, fallback);
        for (int index = 0; index < values.length; index++) {
            if (values[index].equals(normalized)) {
                return values[(index + 1) % values.length];
            }
        }

        return fallback;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int[] parseCustomColors(String[] values) {
        if (values == null || values.length == 0) {
            return DEFAULT_CUSTOM_COLORS;
        }

        int[] parsed = Arrays.stream(values)
            .limit(3)
            .map(BrewBloomConfig::parseHexColor)
            .filter(color -> color >= 0)
            .mapToInt(Integer::intValue)
            .toArray();

        return parsed.length == 0 ? DEFAULT_CUSTOM_COLORS : parsed;
    }

    private static int parseHexColor(String value) {
        if (value == null) {
            return -1;
        }

        String normalized = value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        if (!normalized.matches("[0-9a-fA-F]{6}")) {
            return -1;
        }

        return Integer.parseInt(normalized, 16);
    }
}
