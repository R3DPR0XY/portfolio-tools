package com.bmod.chestpanel.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LootPanelConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("lootpanel.json");

    private static LootPanelConfig instance;

    public int configVersion = 2;
    public boolean enabled = true;
    public boolean containerPanel = true;
    public boolean furnacePanel = true;
    public boolean showTitle = true;
    public boolean showTotal = true;
    public boolean showEmptyMessage = true;
    public boolean compactCounts = true;
    public boolean showFurnaceEstimates = true;
    public boolean showFuelDetails = true;
    public boolean smartSearch = true;
    public boolean favoriteFilter = false;
    public boolean ignoreHotbar = true;
    public boolean anchorToHandledScreen = true;
    public boolean highlightRare = true;
    public boolean soundFeedback = false;
    public boolean animations = true;
    public int visibleRows = 6;
    public int panelWidth = 220;
    public int opacityPercent = 90;
    public int scalePercent = 100;
    public int lockedSlots = 0;
    public PanelSide panelSide = PanelSide.RIGHT;
    public Theme theme = Theme.GOLD;
    public SortProfile sortProfile = SortProfile.QUANTITY;
    public List<String> favoriteItems = new ArrayList<>();
    public List<String> searchHistory = new ArrayList<>();

    public static LootPanelConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            instance = new LootPanelConfig();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            LootPanelConfig loaded = GSON.fromJson(reader, LootPanelConfig.class);
            instance = loaded == null ? new LootPanelConfig() : loaded;
            instance.normalize();
        } catch (IOException | RuntimeException ignored) {
            instance = new LootPanelConfig();
            save();
        }
    }

    public static void save() {
        LootPanelConfig config = get();
        config.normalize();

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public void resetDefaults() {
        enabled = true;
        containerPanel = true;
        furnacePanel = true;
        showTitle = true;
        showTotal = true;
        showEmptyMessage = true;
        compactCounts = true;
        showFurnaceEstimates = true;
        showFuelDetails = true;
        smartSearch = true;
        favoriteFilter = false;
        ignoreHotbar = true;
        anchorToHandledScreen = true;
        highlightRare = true;
        soundFeedback = false;
        animations = true;
        visibleRows = 6;
        panelWidth = 220;
        opacityPercent = 90;
        scalePercent = 100;
        lockedSlots = 0;
        panelSide = PanelSide.RIGHT;
        theme = Theme.GOLD;
        sortProfile = SortProfile.QUANTITY;
        favoriteItems.clear();
        searchHistory.clear();
        configVersion = 2;
        normalize();
    }

    private void normalize() {
        if (configVersion < 2) {
            smartSearch = true;
            configVersion = 2;
        }
        visibleRows = clamp(visibleRows, 3, 10);
        panelWidth = clamp(panelWidth, 190, 260);
        opacityPercent = clamp(opacityPercent, 35, 100);
        scalePercent = clamp(scalePercent, 75, 140);
        lockedSlots = clamp(lockedSlots, 0, 36);
        if (panelSide == null) panelSide = PanelSide.RIGHT;
        if (theme == null) theme = Theme.GOLD;
        if (sortProfile == null) sortProfile = SortProfile.QUANTITY;
        if (favoriteItems == null) favoriteItems = new ArrayList<>();
        if (searchHistory == null) searchHistory = new ArrayList<>();
        while (searchHistory.size() > 8) {
            searchHistory.remove(searchHistory.size() - 1);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public enum PanelSide {
        LEFT("Esquerda"),
        RIGHT("Direita");

        private final String label;

        PanelSide(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public PanelSide next() {
            return this == LEFT ? RIGHT : LEFT;
        }
    }

    public enum Theme {
        GOLD("Dourado", 0xFFE8B450),
        CYAN("Ciano", 0xFF66D9EF),
        EMERALD("Esmeralda", 0xFF9BE564),
        ROSE("Rosa", 0xFFFF79A8);

        private final String label;
        private final int accent;

        Theme(String label, int accent) {
            this.label = label;
            this.accent = accent;
        }

        public String label() {
            return label;
        }

        public int accent() {
            return accent;
        }

        public Theme next() {
            Theme[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    public enum SortProfile {
        QUANTITY("Quantidade"),
        NAME("Nome"),
        SLOT("Slots");

        private final String label;

        SortProfile(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public SortProfile next() {
            SortProfile[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}
