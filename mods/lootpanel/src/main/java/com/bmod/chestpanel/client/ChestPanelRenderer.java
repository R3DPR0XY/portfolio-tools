package com.bmod.chestpanel.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChestPanelRenderer {
    private static final int MARGIN = 7;
    private static final int HEADER_HEIGHT = 34;
    private static final int TOOLBAR_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 48;
    private static final int ROW_HEIGHT = 22;
    private static final int PANEL_BG = 0xE613151B;
    private static final int PANEL_HEADER = 0xFF202A36;
    private static final int PANEL_BORDER = 0xFF435060;
    private static final int PANEL_BORDER_DARK = 0xAA0A0D12;
    private static final int ACCENT = 0xFF66D9EF;
    private static final int TEXT = 0xFFEAEAEA;
    private static final int MUTED = 0xFF9AA0A6;
    private static final int COUNT = 0xFF9BE564;
    private static final int WARN = 0xFFFFD166;
    private static final int FIRE = 0xFFFF8A3D;

    private static ScreenHandler lastHandler;
    private static int scrollOffset;
    private static int maxScroll;
    private static int panelX;
    private static int panelY;
    private static int panelW;
    private static int panelH;
    private static boolean scrollable;
    private static boolean searchActive;
    private static String searchQuery = "";
    private static int historyIndex = -1;
    private static List<LootEntry> lastEntries = List.of();
    private static final List<PanelButton> panelButtons = new ArrayList<>();
    private static final List<RowButton> rowButtons = new ArrayList<>();

    private ChestPanelRenderer() {
    }

    public static void render(
        DrawContext context,
        ScreenHandler handler,
        int screenWidth,
        int screenHeight,
        int guiX,
        int guiY,
        int guiWidth,
        int guiHeight,
        String screenTitle,
        int mouseX,
        int mouseY
    ) {
        LootPanelConfig config = LootPanelConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();
        if (!config.enabled || client.player == null || handler == null || handler instanceof PlayerScreenHandler) {
            reset(null);
            return;
        }

        if (lastHandler != handler) {
            reset(handler);
        }

        MachineKind machineKind = detectMachineKind(handler, screenTitle);
        if (machineKind != null) {
            clearBounds();
            return;
        }

        if (!config.containerPanel) {
            clearBounds();
            return;
        }

        List<LootEntry> entries = filterEntries(collectContainerItems(handler, client.player.getInventory()));
        lastEntries = entries;
        if (entries.isEmpty() && !config.showEmptyMessage) {
            clearBounds();
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        int width = fitPanelWidth(screenWidth, guiX, guiWidth, config);
        int visibleRows = clamp(config.visibleRows, 3, 10);
        int height = HEADER_HEIGHT + TOOLBAR_HEIGHT + FOOTER_HEIGHT + visibleRows * ROW_HEIGHT;
        PanelPos pos = choosePosition(screenWidth, screenHeight, guiX, guiY, guiWidth, guiHeight, width, height, config);

        maxScroll = Math.max(0, entries.size() - visibleRows);
        scrollOffset = clamp(scrollOffset, 0, maxScroll);
        setBounds(pos.x, pos.y, width, height, maxScroll > 0);

        drawPanel(context, pos.x, pos.y, width, height, false);
        drawHeader(context, textRenderer, pos.x, pos.y, width, entries, config);
        drawToolbar(context, textRenderer, pos.x, pos.y, width);

        if (entries.isEmpty()) {
            context.drawTextWithShadow(textRenderer, searchQuery.isBlank() ? "Container vazio" : "Nenhum item encontrado", pos.x + 14, pos.y + HEADER_HEIGHT + TOOLBAR_HEIGHT + 18, MUTED);
            drawFooter(context, textRenderer, pos.x, pos.y, width, height, entries.size());
            return;
        }

        int first = scrollOffset;
        int last = Math.min(entries.size(), first + visibleRows);
        int rowY = pos.y + HEADER_HEIGHT + TOOLBAR_HEIGHT + 4;
        for (int index = first; index < last; index++) {
            drawRow(context, textRenderer, entries.get(index), pos.x + 10, rowY, width - 20, config);
            rowY += ROW_HEIGHT;
        }

        drawFooter(context, textRenderer, pos.x, pos.y, width, height, entries.size());
        if (inside(mouseX, mouseY, pos.x, pos.y, width, height)) {
            context.drawTooltip(textRenderer, Text.literal("LootPanel"), mouseX, mouseY);
        }
    }

    public static boolean onMouseScrolled(ScreenHandler handler, double mouseX, double mouseY, double verticalAmount) {
        if (handler == null || handler != lastHandler || !scrollable) {
            return false;
        }
        if (!inside((int) mouseX, (int) mouseY, panelX, panelY, panelW, panelH)) {
            return false;
        }

        int oldOffset = scrollOffset;
        scrollOffset = clamp(scrollOffset + (verticalAmount > 0 ? -1 : 1), 0, maxScroll);
        return oldOffset != scrollOffset;
    }

    public static boolean onMouseClicked(ScreenHandler handler, double mouseX, double mouseY, int button) {
        if (handler == null || handler != lastHandler || button != 0) {
            return false;
        }
        int x = (int) mouseX;
        int y = (int) mouseY;
        for (RowButton rowButton : rowButtons) {
            if (inside(x, y, rowButton.x(), rowButton.y(), rowButton.width(), rowButton.height())) {
                toggleFavorite(rowButton.itemId());
                return true;
            }
        }
        for (PanelButton panelButton : panelButtons) {
            if (inside(x, y, panelButton.x(), panelButton.y(), panelButton.width(), panelButton.height())) {
                panelButton.action().run();
                return true;
            }
        }
        if (inside(x, y, panelX, panelY, panelW, panelH)) {
            searchActive = false;
            return true;
        }
        return false;
    }

    private static MachineKind detectMachineKind(ScreenHandler handler, String screenTitle) {
        if (handler instanceof BlastFurnaceScreenHandler) {
            return MachineKind.BLAST_FURNACE;
        }
        if (handler instanceof SmokerScreenHandler) {
            return MachineKind.SMOKER;
        }
        if (handler instanceof AbstractFurnaceScreenHandler) {
            return MachineKind.FURNACE;
        }

        String title = normalizeSearch(screenTitle == null ? "" : screenTitle);
        if (title.contains("alto forno") || title.contains("blast furnace")) {
            return MachineKind.BLAST_FURNACE;
        }
        if (title.contains("defumador") || title.contains("smoker")) {
            return MachineKind.SMOKER;
        }
        if (title.contains("fornalha") || title.contains("furnace")) {
            return MachineKind.FURNACE;
        }
        return null;
    }

    public static void renderFurnaceOverlay(
        DrawContext context,
        AbstractFurnaceScreenHandler handler,
        int screenWidth,
        int screenHeight,
        int guiX,
        int guiY,
        int guiWidth,
        int guiHeight,
        String screenTitle,
        int mouseX,
        int mouseY
    ) {
        LootPanelConfig config = LootPanelConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();
        if (!config.enabled || !config.furnacePanel || client.player == null || handler == null) {
            clearBounds();
            return;
        }

        if (lastHandler != handler) {
            reset(handler);
        }

        MachineKind kind = detectMachineKind(handler, screenTitle);
        renderFurnacePanel(context, handler, kind == null ? MachineKind.FURNACE : kind, screenWidth, screenHeight, guiX, guiY, guiWidth, guiHeight, mouseX, mouseY, config);
    }

    private static void renderFurnacePanel(
        DrawContext context,
        AbstractFurnaceScreenHandler handler,
        MachineKind kind,
        int screenWidth,
        int screenHeight,
        int guiX,
        int guiY,
        int guiWidth,
        int guiHeight,
        int mouseX,
        int mouseY,
        LootPanelConfig config
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer tr = client.textRenderer;
        FurnaceStats stats = readFurnaceStats(handler, kind);
        int width = Math.max(fitPanelWidth(screenWidth, guiX, guiWidth, config), 210);
        int height = config.showFurnaceEstimates ? 204 : 146;
        PanelPos pos = choosePosition(screenWidth, screenHeight, guiX, guiY, guiWidth, guiHeight, width, height, config);
        setBounds(pos.x, pos.y, width, height, false);

        drawPanel(context, pos.x, pos.y, width, height, true);
        context.drawTextWithShadow(tr, stats.title, pos.x + 12, pos.y + 8, TEXT);
        context.drawTextWithShadow(tr, stats.burning ? "Acesa" : "Apagada", pos.x + width - 58, pos.y + 8, stats.burning ? FIRE : MUTED);
        context.drawTextWithShadow(tr, stats.status, pos.x + 12, pos.y + 22, stats.statusColor);

        int y = pos.y + 42;
        drawStackLine(context, tr, "Entrada", stats.input, pos.x + 12, y, width - 24);
        y += 22;
        drawStackLine(context, tr, "Combustivel", stats.fuel, pos.x + 12, y, width - 24);
        y += 25;

        drawMetric(context, tr, "Fogo restante", stats.burnRemainingText, pos.x + 12, y, width - 24, stats.burning ? FIRE : MUTED);
        y += 13;
        drawProgress(context, pos.x + 12, y, width - 24, 9, stats.fuelProgress, FIRE);
        y += 18;
        drawMetric(context, tr, "Cozimento", Math.round(stats.cookProgress * 100.0f) + "%", pos.x + 12, y, width - 24, stats.hasInput ? COUNT : MUTED);
        y += 13;
        drawProgress(context, pos.x + 12, y, width - 24, 9, stats.cookProgress, COUNT);
        y += 18;

        if (config.showFurnaceEstimates) {
            y += 4;
            drawMetric(context, tr, stats.currentBurnLabel, stats.itemsFromCurrentBurn, pos.x + 12, y, width - 24, stats.burning ? COUNT : MUTED);
            y += 13;
            drawMetric(context, tr, stats.fuelStackLabel, stats.itemsFromFuelStack, pos.x + 12, y, width - 24, stats.hasFuel ? COUNT : MUTED);
            y += 13;
            if (config.showFuelDetails) {
                drawMetric(context, tr, "Tempo por combustivel", stats.fuelUnitTime, pos.x + 12, y, width - 24, MUTED);
            }
        }

        if (inside(mouseX, mouseY, pos.x, pos.y, width, height)) {
            context.drawTooltip(tr, Text.literal("Estimativas client-side"), mouseX, mouseY);
        }
    }

    private static void renderMachineFallbackPanel(
        DrawContext context,
        ScreenHandler handler,
        MachineKind kind,
        int screenWidth,
        int screenHeight,
        int guiX,
        int guiY,
        int guiWidth,
        int guiHeight,
        int mouseX,
        int mouseY,
        LootPanelConfig config
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer tr = client.textRenderer;
        ItemStack input = stack(handler, 0);
        ItemStack fuel = stack(handler, 1);
        ItemStack output = stack(handler, 2);
        int unitFuelTicks = estimateFuelTicks(fuel);
        int itemsFromFuelStack = fuel.isEmpty() ? 0 : Math.max(0, (unitFuelTicks * fuel.getCount()) / kind.cookTicks());
        int width = Math.max(fitPanelWidth(screenWidth, guiX, guiWidth, config), 210);
        int height = config.showFurnaceEstimates ? 166 : 120;
        PanelPos pos = choosePosition(screenWidth, screenHeight, guiX, guiY, guiWidth, guiHeight, width, height, config);
        setBounds(pos.x, pos.y, width, height, false);

        drawPanel(context, pos.x, pos.y, width, height, true);
        context.drawTextWithShadow(tr, kind.title(), pos.x + 12, pos.y + 8, TEXT);
        context.drawTextWithShadow(tr, "Painel ativo", pos.x + 12, pos.y + 22, COUNT);

        int y = pos.y + 42;
        drawStackLine(context, tr, kind == MachineKind.SMOKER ? "Comida" : "Entrada", input, pos.x + 12, y, width - 24);
        y += 22;
        drawStackLine(context, tr, "Combustivel", fuel, pos.x + 12, y, width - 24);
        y += 22;
        drawStackLine(context, tr, "Saida", output, pos.x + 12, y, width - 24);
        y += 26;

        if (config.showFurnaceEstimates) {
            String label = kind == MachineKind.SMOKER ? "Comidas com combustivel" : kind == MachineKind.BLAST_FURNACE ? "Minerios com combustivel" : "Itens com combustivel";
            drawMetric(context, tr, label, String.valueOf(itemsFromFuelStack), pos.x + 12, y, width - 24, fuel.isEmpty() ? MUTED : COUNT);
            y += 13;
            drawMetric(context, tr, "Tempo por combustivel", ticksToClock(unitFuelTicks), pos.x + 12, y, width - 24, MUTED);
        }

        if (inside(mouseX, mouseY, pos.x, pos.y, width, height)) {
            context.drawTooltip(tr, Text.literal("Estimativa por slots"), mouseX, mouseY);
        }
    }

    private static FurnaceStats readFurnaceStats(AbstractFurnaceScreenHandler handler, MachineKind kind) {
        ItemStack input = stack(handler, 0);
        ItemStack fuel = stack(handler, 1);
        ItemStack output = stack(handler, 2);
        boolean hasInput = !input.isEmpty();
        boolean hasFuel = !fuel.isEmpty();
        boolean burning = handler.isBurning();
        float fuelProgress = clamp01(handler.getFuelProgress());
        float cookProgress = clamp01(handler.getCookProgress());
        boolean blastFurnace = kind == MachineKind.BLAST_FURNACE;
        boolean smoker = kind == MachineKind.SMOKER;
        int cookTotal = kind.cookTicks();
        int unitFuelTicks = estimateFuelTicks(fuel);
        int burnRemaining = burning ? Math.max(1, Math.round(unitFuelTicks * fuelProgress)) : 0;
        int itemsFromCurrentBurn = burnRemaining / cookTotal;
        int itemsFromFuelStack = hasFuel ? Math.max(0, (unitFuelTicks * fuel.getCount()) / cookTotal) : 0;

        String title = kind.title();
        String currentBurnLabel = smoker ? "Comidas com fogo atual" : blastFurnace ? "Minerios com fogo atual" : "Itens com fogo atual";
        String fuelStackLabel = smoker ? "Comidas com combustivel" : blastFurnace ? "Minerios com combustivel" : "Itens com combustivel";
        String status;
        int statusColor;
        if (!output.isEmpty() && !hasInput) {
            status = "Resultado pronto";
            statusColor = COUNT;
        } else if (hasInput && burning) {
            status = smoker ? "Assando comida" : blastFurnace ? "Fundindo minerio" : "Queimando agora";
            statusColor = COUNT;
        } else if (hasInput && hasFuel) {
            status = smoker ? "Pronto para assar" : blastFurnace ? "Pronta para fundir" : "Pronta para acender";
            statusColor = WARN;
        } else if (hasInput) {
            status = "Sem combustivel";
            statusColor = WARN;
        } else {
            status = burning ? (smoker ? "Fogo ativo, sem comida" : "Fogo ativo, sem item") : smoker ? "Sem comida para assar" : "Sem item para queimar";
            statusColor = MUTED;
        }

        return new FurnaceStats(
            title,
            input,
            fuel,
            hasInput,
            hasFuel,
            burning,
            fuelProgress,
            cookProgress,
            ticksToClock(burnRemaining),
            String.valueOf(itemsFromCurrentBurn),
            String.valueOf(itemsFromFuelStack),
            ticksToClock(unitFuelTicks),
            currentBurnLabel,
            fuelStackLabel,
            status,
            statusColor
        );
    }

    private static void drawStackLine(DrawContext context, TextRenderer tr, String label, ItemStack stack, int x, int y, int width) {
        context.drawTextWithShadow(tr, label + ":", x, y + 5, MUTED);
        int valueX = x + 82;
        if (stack.isEmpty()) {
            context.drawTextWithShadow(tr, "Nenhum", valueX, y + 5, MUTED);
            return;
        }
        context.drawItem(stack, valueX, y);
        context.drawStackOverlay(tr, stack, valueX, y, stack.getCount() > 1 ? String.valueOf(stack.getCount()) : null);
        context.drawTextWithShadow(tr, shorten(tr, stack.getName().getString(), width - 106), valueX + 22, y + 5, TEXT);
    }

    private static void drawMetric(DrawContext context, TextRenderer tr, String label, String value, int x, int y, int width, int color) {
        int valueWidth = tr.getWidth(value);
        String safeLabel = shorten(tr, label, Math.max(20, width - valueWidth - 8));
        context.drawTextWithShadow(tr, safeLabel, x, y, MUTED);
        context.drawTextWithShadow(tr, value, x + width - valueWidth, y, color);
    }

    private static void drawProgress(DrawContext context, int x, int y, int width, int height, float progress, int color) {
        context.fill(x, y, x + width, y + height, 0x77000000);
        context.drawBorder(x, y, width, height, 0x66435060);
        int fill = Math.round((width - 2) * clamp01(progress));
        if (fill > 0) {
            context.fill(x + 1, y + 1, x + 1 + fill, y + height - 1, color);
            context.fill(x + 1, y + 1, x + 1 + fill, y + 3, 0x55FFFFFF);
        }
    }


    public static boolean onKeyPressed(ScreenHandler handler, int keyCode) {
        if (handler == null || handler != lastHandler || !searchActive) {
            return false;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
                pushSearchHistory(searchQuery);
            }
            searchActive = false;
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_UP) {
            navigateHistory(1);
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN) {
            navigateHistory(-1);
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            scrollOffset = 0;
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE) {
            searchQuery = "";
            scrollOffset = 0;
            historyIndex = -1;
            return true;
        }
        String typed = keyName(keyCode);
        if (!typed.isEmpty() && searchQuery.length() < 40) {
            searchQuery += typed;
            scrollOffset = 0;
            historyIndex = -1;
            return true;
        }
        return false;
    }

    private static String keyName(int keyCode) {
        if (keyCode >= org.lwjgl.glfw.GLFW.GLFW_KEY_A && keyCode <= org.lwjgl.glfw.GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('a' + keyCode - org.lwjgl.glfw.GLFW.GLFW_KEY_A));
        }
        if (keyCode >= org.lwjgl.glfw.GLFW.GLFW_KEY_0 && keyCode <= org.lwjgl.glfw.GLFW.GLFW_KEY_9) {
            return String.valueOf((char) ('0' + keyCode - org.lwjgl.glfw.GLFW.GLFW_KEY_0));
        }
        if (keyCode >= org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0 && keyCode <= org.lwjgl.glfw.GLFW.GLFW_KEY_KP_9) {
            return String.valueOf((char) ('0' + keyCode - org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0));
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) {
            return " ";
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS) {
            return "-";
        }
        return "";
    }

    private static List<LootEntry> filterEntries(List<LootEntry> entries) {
        if (searchQuery.isBlank()) {
            return entries;
        }
        String query = LootPanelConfig.get().smartSearch ? normalizeSearch(searchQuery) : searchQuery.toLowerCase(java.util.Locale.ROOT).trim();
        List<LootEntry> filtered = new ArrayList<>();
        LootPanelConfig config = LootPanelConfig.get();
        for (LootEntry entry : entries) {
            if (config.favoriteFilter && !isFavorite(entry.itemId())) {
                continue;
            }
            boolean matches = config.smartSearch
                ? smartMatches(query, entry.name) || smartMatches(query, entry.itemId())
                : entry.name.toLowerCase(java.util.Locale.ROOT).contains(query) || entry.itemId().toLowerCase(java.util.Locale.ROOT).contains(query);
            if (matches) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    private static boolean smartMatches(String query, String itemName) {
        String target = normalizeSearch(itemName);
        if (query.isEmpty() || target.contains(query)) {
            return true;
        }

        for (String token : target.split(" ")) {
            if (token.isEmpty()) {
                continue;
            }
            int allowedMistakes = Math.max(1, Math.min(3, query.length() / 3));
            if (levenshteinDistance(query, token, allowedMistakes) <= allowedMistakes) {
                return true;
            }
        }
        return levenshteinDistance(query, target.replace(" ", ""), Math.max(1, Math.min(4, query.length() / 3))) <= Math.max(1, Math.min(4, query.length() / 3));
    }

    private static String normalizeSearch(String value) {
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(java.util.Locale.ROOT)
            .replace('_', ' ')
            .replace('-', ' ')
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
        return normalized;
    }

    private static int levenshteinDistance(String a, String b, int maxDistance) {
        if (Math.abs(a.length() - b.length()) > maxDistance) {
            return maxDistance + 1;
        }

        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            int rowBest = current[0];
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
                rowBest = Math.min(rowBest, current[j]);
            }
            if (rowBest > maxDistance) {
                return maxDistance + 1;
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    private static String buildCopyText() {
        StringBuilder builder = new StringBuilder("LootPanel\n");
        for (LootEntry entry : lastEntries) {
            builder.append(entry.name).append(" x").append(entry.count).append('\n');
        }
        return builder.toString().trim();
    }

    private static void toggleFavorite(String itemId) {
        LootPanelConfig config = LootPanelConfig.get();
        if (config.favoriteItems.contains(itemId)) {
            config.favoriteItems.remove(itemId);
            message("LootPanel: removido dos favoritos.");
        } else {
            config.favoriteItems.add(itemId);
            message("LootPanel: favoritado.");
        }
        LootPanelConfig.save();
    }

    private static boolean isFavorite(String itemId) {
        return LootPanelConfig.get().favoriteItems.contains(itemId);
    }

    private static void pushSearchHistory(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        LootPanelConfig config = LootPanelConfig.get();
        String normalized = value.trim();
        config.searchHistory.remove(normalized);
        config.searchHistory.add(0, normalized);
        while (config.searchHistory.size() > 8) {
            config.searchHistory.remove(config.searchHistory.size() - 1);
        }
        LootPanelConfig.save();
    }

    private static void navigateHistory(int direction) {
        LootPanelConfig config = LootPanelConfig.get();
        if (config.searchHistory.isEmpty()) {
            return;
        }
        historyIndex = historyIndex < 0 ? 0 : clamp(historyIndex + (direction > 0 ? 1 : -1), 0, config.searchHistory.size() - 1);
        searchQuery = config.searchHistory.get(historyIndex);
        scrollOffset = 0;
    }

    private static boolean importantItem(LootEntry entry) {
        String id = entry.itemId();
        return id.contains("diamond") || id.contains("netherite") || id.contains("emerald") || id.contains("elytra") || id.contains("totem") || id.contains("shulker");
    }

    private static void reset(ScreenHandler handler) {
        lastHandler = handler;
        scrollOffset = 0;
        maxScroll = 0;
        historyIndex = -1;
        clearBounds();
    }

    private static void clearBounds() {
        panelX = 0;
        panelY = 0;
        panelW = 0;
        panelH = 0;
        scrollable = false;
        panelButtons.clear();
        rowButtons.clear();
    }

    private static void setBounds(int x, int y, int width, int height, boolean hasScroll) {
        panelX = x;
        panelY = y;
        panelW = width;
        panelH = height;
        scrollable = hasScroll;
    }

    private static int fitPanelWidth(int screenWidth, int guiX, int guiWidth, LootPanelConfig config) {
        int preferred = clamp(config.panelWidth, 190, 260);
        int rightSpace = screenWidth - (guiX + guiWidth) - MARGIN * 2;
        int leftSpace = guiX - MARGIN * 2;
        int sideSpace = config.panelSide == LootPanelConfig.PanelSide.RIGHT ? rightSpace : leftSpace;
        int fallbackSpace = config.panelSide == LootPanelConfig.PanelSide.RIGHT ? leftSpace : rightSpace;

        if (sideSpace >= 190) {
            return clamp(preferred, 190, Math.min(260, sideSpace));
        }
        if (fallbackSpace >= 190) {
            return clamp(preferred, 190, Math.min(260, fallbackSpace));
        }
        return clamp(Math.min(preferred, screenWidth - MARGIN * 2), 160, 260);
    }

    private static PanelPos choosePosition(int screenWidth, int screenHeight, int guiX, int guiY, int guiWidth, int guiHeight, int width, int height, LootPanelConfig config) {
        int y = clamp(guiY + 4, MARGIN, Math.max(MARGIN, screenHeight - height - MARGIN));
        if (!config.anchorToHandledScreen) {
            int x = config.panelSide == LootPanelConfig.PanelSide.LEFT ? MARGIN : screenWidth - width - MARGIN;
            return new PanelPos(x, y);
        }

        int rightX = guiX + guiWidth + MARGIN;
        int leftX = guiX - width - MARGIN;
        if (config.panelSide == LootPanelConfig.PanelSide.RIGHT) {
            if (rightX + width <= screenWidth - MARGIN) {
                return new PanelPos(rightX, y);
            }
            if (leftX >= MARGIN) {
                return new PanelPos(leftX, y);
            }
        } else {
            if (leftX >= MARGIN) {
                return new PanelPos(leftX, y);
            }
            if (rightX + width <= screenWidth - MARGIN) {
                return new PanelPos(rightX, y);
            }
        }
        return new PanelPos(clamp(screenWidth - width - MARGIN, MARGIN, screenWidth - width), y);
    }

    private static void drawPanel(DrawContext context, int x, int y, int width, int height, boolean furnace) {
        context.fill(x + 3, y + 4, x + width + 3, y + height + 4, 0x88000000);
        context.fill(x, y, x + width, y + height, PANEL_BG);
        context.fill(x, y, x + width, y + HEADER_HEIGHT, furnace ? 0xFF2D2635 : PANEL_HEADER);
        context.fill(x, y + HEADER_HEIGHT - 1, x + width, y + HEADER_HEIGHT + 1, furnace ? FIRE : ACCENT);
        context.drawBorder(x, y, width, height, PANEL_BORDER);
        context.drawBorder(x + 1, y + 1, width - 2, height - 2, PANEL_BORDER_DARK);
    }

    private static void drawHeader(DrawContext context, TextRenderer textRenderer, int x, int y, int width, List<LootEntry> entries, LootPanelConfig config) {
        context.drawTextWithShadow(textRenderer, config.showTitle ? "LootPanel" : "Loot", x + 12, y + 8, TEXT);
        if (config.showTotal) {
            int totalItems = entries.stream().mapToInt(entry -> entry.count).sum();
            String total = totalItems + " itens";
            context.drawTextWithShadow(textRenderer, total, x + width - 12 - textRenderer.getWidth(total), y + 8, COUNT);
        }
        String hint = searchActive || !searchQuery.isEmpty() ? "Buscar: " + searchQuery + (searchActive ? "_" : "") : "Use os botoes abaixo";
        if (config.favoriteFilter) {
            hint = "Favoritos | " + hint;
        }
        context.drawTextWithShadow(textRenderer, hint, x + 12, y + 21, MUTED);
    }

    private static void drawToolbar(DrawContext context, TextRenderer textRenderer, int x, int y, int width) {
        panelButtons.clear();
        rowButtons.clear();
        int toolbarY = y + HEADER_HEIGHT + 5;
        addPanelButton(context, textRenderer, x + 12, toolbarY, width - 24, 18, searchActive ? "Pesquisar: " + searchQuery + "_" : searchQuery.isBlank() ? "Pesquisar item..." : "Pesquisar: " + searchQuery, () -> searchActive = true);
    }

    private static void addPanelButton(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, String label, Runnable action) {
        panelButtons.add(new PanelButton(x, y, width, height, action));
        context.fill(x, y, x + width, y + height, 0xAA26313D);
        context.drawBorder(x, y, width, height, 0xFF5C6B78);
        int color = searchActive && label.startsWith("Pesquisar:") ? ACCENT : TEXT;
        context.drawTextWithShadow(textRenderer, shorten(textRenderer, label, width - 8), x + 4, y + 5, color);
    }

    private static void drawRow(DrawContext context, TextRenderer textRenderer, LootEntry entry, int x, int y, int width, LootPanelConfig config) {
        context.fill(x, y, x + width, y + ROW_HEIGHT - 2, 0x331E2630);
        context.drawItem(entry.stack, x + 4, y + 2);

        boolean favorite = isFavorite(entry.itemId());
        if (config.highlightRare && importantItem(entry)) {
            context.drawBorder(x, y, width, ROW_HEIGHT - 2, WARN);
        }

        String count = config.compactCounts ? compact(entry.count) : String.valueOf(entry.count);
        String countText = "x" + count;
        int countWidth = textRenderer.getWidth(countText);
        int starX = x + width - 13;
        int nameWidth = width - 50 - countWidth - 8;

        context.drawTextWithShadow(textRenderer, shorten(textRenderer, entry.name, nameWidth), x + 26, y + 3, TEXT);
        context.drawTextWithShadow(textRenderer, countText, starX - countWidth - 6, y + 7, COUNT);
        context.drawTextWithShadow(textRenderer, favorite ? "*" : "+", starX, y + 5, favorite ? WARN : MUTED);
        rowButtons.add(new RowButton(starX - 2, y + 2, 12, 14, entry.itemId()));
    }

    private static void drawFooter(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, int totalRows) {
        int footerY = y + height - FOOTER_HEIGHT;
        context.fill(x + 1, footerY, x + width - 1, y + height - 1, 0x5510151D);

        if (maxScroll > 0) {
            String label = "Scroll " + (scrollOffset + 1) + "/" + (maxScroll + 1);
            context.drawTextWithShadow(textRenderer, label, x + 62, footerY + 5, MUTED);
            drawScrollBar(context, x + width - 10, y + HEADER_HEIGHT + TOOLBAR_HEIGHT + 6, height - HEADER_HEIGHT - TOOLBAR_HEIGHT - FOOTER_HEIGHT - 12, totalRows);
        } else {
            context.drawTextWithShadow(textRenderer, "Tudo visivel", x + 62, footerY + 5, MUTED);
        }

        int gap = 4;
        int buttonY = footerY + 22;
        int buttonHeight = 18;
        int compactWidth = 44;
        int clearWidth = 42;
        int storeWidth = 48;
        int favWidth = 20;
        int copyWidth = Math.max(38, width - 24 - compactWidth - clearWidth - storeWidth - favWidth - gap * 4);
        int buttonX = x + 12;
        ScreenHandler handler = lastHandler;
        addPanelButton(context, textRenderer, buttonX, buttonY, compactWidth, buttonHeight, "Juntar", () -> compactStacks(handler));
        buttonX += compactWidth + gap;
        addPanelButton(context, textRenderer, buttonX, buttonY, clearWidth, buttonHeight, "Limpar", () -> moveContainerToInventory(handler));
        buttonX += clearWidth + gap;
        addPanelButton(context, textRenderer, buttonX, buttonY, storeWidth, buttonHeight, "Guardar", () -> moveInventoryToContainer(handler));
        buttonX += storeWidth + gap;
        addPanelButton(context, textRenderer, buttonX, buttonY, favWidth, buttonHeight, LootPanelConfig.get().favoriteFilter ? "*" : "+", () -> {
            LootPanelConfig config = LootPanelConfig.get();
            config.favoriteFilter = !config.favoriteFilter;
            LootPanelConfig.save();
            scrollOffset = 0;
        });
        buttonX += favWidth + gap;
        addPanelButton(context, textRenderer, buttonX, buttonY, copyWidth, buttonHeight, "Copiar", () -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.keyboard != null) {
                client.keyboard.setClipboard(buildCopyText());
            }
        });

        if (searchActive || !searchQuery.isEmpty()) {
            addPanelButton(context, textRenderer, x + 12, footerY + 4, 44, 14, "Limpar", () -> {
                searchQuery = "";
                searchActive = false;
                scrollOffset = 0;
            });
        } else {
            addPanelButton(context, textRenderer, x + 12, footerY + 4, 44, 14, "Org", () -> {
                LootPanelConfig config = LootPanelConfig.get();
                config.sortProfile = config.sortProfile.next();
                LootPanelConfig.save();
                scrollOffset = 0;
            });
        }
    }

    private static void drawScrollBar(DrawContext context, int x, int y, int height, int totalRows) {
        context.fill(x, y, x + 3, y + height, 0x77000000);
        int thumbHeight = clamp(Math.round((float) LootPanelConfig.get().visibleRows / Math.max(1, totalRows) * height), 12, height);
        int travel = Math.max(1, height - thumbHeight);
        int thumbY = y + Math.round((float) scrollOffset / Math.max(1, maxScroll) * travel);
        context.fill(x, thumbY, x + 3, thumbY + thumbHeight, ACCENT);
    }

    private static void compactStacks(ScreenHandler handler) {
        int count = getContainerSlotCount(handler);
        if (!validPlayer() || count <= 0) {
            return;
        }
        if (!handler.getCursorStack().isEmpty()) {
            message("LootPanel: solte o item do cursor.");
            return;
        }

        int start = clamp(LootPanelConfig.get().lockedSlots, 0, count);
        int moves = 0;
        for (int target = start; target < count; target++) {
            ItemStack targetStack = stack(handler, target);
            if (targetStack.isEmpty() || targetStack.getCount() >= targetStack.getMaxCount()) {
                continue;
            }
            for (int source = target + 1; source < count; source++) {
                ItemStack sourceStack = stack(handler, source);
                if (sourceStack.isEmpty() || !sameItem(targetStack, sourceStack)) {
                    continue;
                }
                click(handler, source, SlotActionType.PICKUP);
                click(handler, target, SlotActionType.PICKUP);
                if (!handler.getCursorStack().isEmpty()) {
                    click(handler, source, SlotActionType.PICKUP);
                }
                if (++moves >= 96) {
                    message("LootPanel: stacks juntadas.");
                    return;
                }
            }
        }
        message(moves > 0 ? "LootPanel: stacks juntadas." : "LootPanel: nada para juntar.");
    }

    private static void moveContainerToInventory(ScreenHandler handler) {
        int count = getContainerSlotCount(handler);
        if (!validPlayer() || count <= 0) {
            return;
        }
        int moved = 0;
        int start = clamp(LootPanelConfig.get().lockedSlots, 0, count);
        for (int i = count - 1; i >= start; i--) {
            if (stack(handler, i).isEmpty()) {
                continue;
            }
            click(handler, i, SlotActionType.QUICK_MOVE);
            moved++;
        }
        message(moved > 0 ? "LootPanel: bau limpo." : "LootPanel: bau vazio.");
    }

    private static void moveInventoryToContainer(ScreenHandler handler) {
        int count = getContainerSlotCount(handler);
        if (!validPlayer() || count <= 0) {
            return;
        }
        int moved = 0;
        int total = handler.slots.size();
        int hotbarStart = Math.max(count, total - 9);
        for (int i = count; i < total; i++) {
            if (LootPanelConfig.get().ignoreHotbar && i >= hotbarStart) {
                continue;
            }
            if (stack(handler, i).isEmpty()) {
                continue;
            }
            click(handler, i, SlotActionType.QUICK_MOVE);
            moved++;
        }
        message(moved > 0 ? "LootPanel: inventario guardado." : "LootPanel: nada para guardar.");
    }

    private static int getContainerSlotCount(ScreenHandler handler) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return 0;
        }
        PlayerInventory playerInventory = client.player.getInventory();
        int count = 0;
        for (Slot slot : handler.slots) {
            if (slot.inventory == playerInventory) {
                break;
            }
            count++;
        }
        return count;
    }

    private static boolean validPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.interactionManager != null;
    }

    private static void click(ScreenHandler handler, int slotIndex, SlotActionType actionType) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null || slotIndex < 0 || slotIndex >= handler.slots.size()) {
            return;
        }
        client.interactionManager.clickSlot(handler.syncId, slotIndex, 0, actionType, client.player);
    }

    private static boolean sameItem(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        return a.getRegistryEntry().equals(b.getRegistryEntry()) && a.getComponents().equals(b.getComponents());
    }

    private static void message(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(text), true);
        }
    }

    private static List<LootEntry> collectContainerItems(ScreenHandler handler, PlayerInventory playerInventory) {
        Map<String, LootEntryBuilder> totals = new LinkedHashMap<>();

        for (Slot slot : handler.slots) {
            if (!isContainerSlot(slot, playerInventory)) {
                continue;
            }

            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                continue;
            }

            String itemId = stack.getRegistryEntry().getIdAsString();
            String key = itemId + "|" + stack.getComponents();
            totals.computeIfAbsent(key, ignored -> new LootEntryBuilder(copySingle(stack), stack.getName().getString(), itemId, slot.id))
                .add(stack.getCount(), slot.id);
        }

        List<LootEntry> entries = new ArrayList<>();
        for (LootEntryBuilder builder : totals.values()) {
            entries.add(builder.build());
        }
        LootPanelConfig.SortProfile profile = LootPanelConfig.get().sortProfile;
        if (profile == LootPanelConfig.SortProfile.NAME) {
            entries.sort(Comparator.comparing(LootEntry::name));
        } else if (profile == LootPanelConfig.SortProfile.QUANTITY) {
            entries.sort(Comparator.comparingInt(LootEntry::count).reversed().thenComparing(LootEntry::name));
        } else if (profile == LootPanelConfig.SortProfile.SLOT) {
            entries.sort(Comparator.comparingInt(LootEntry::firstSlot));
        }
        return entries;
    }

    private static boolean isContainerSlot(Slot slot, PlayerInventory playerInventory) {
        Inventory inventory = slot.inventory;
        return inventory != playerInventory;
    }

    private static ItemStack stack(ScreenHandler handler, int index) {
        if (index < 0 || index >= handler.slots.size()) {
            return ItemStack.EMPTY;
        }
        return handler.slots.get(index).getStack();
    }

    private static ItemStack copySingle(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static int estimateFuelTicks(ItemStack fuelStack) {
        if (fuelStack.isEmpty()) {
            return 1600;
        }
        Item item = fuelStack.getItem();
        if (item == Items.LAVA_BUCKET) return 20000;
        if (item == Items.COAL_BLOCK) return 16000;
        if (item == Items.BLAZE_ROD) return 2400;
        if (item == Items.COAL || item == Items.CHARCOAL) return 1600;
        if (item == Items.DRIED_KELP_BLOCK) return 4000;
        if (item == Items.BAMBOO) return 50;
        if (item == Items.STICK) return 100;
        String id = fuelStack.getRegistryEntry().getIdAsString();
        if (id.contains("planks") || id.contains("log") || id.contains("wood") || id.contains("slab") || id.contains("stairs")) {
            return 300;
        }
        return 1600;
    }

    private static String ticksToClock(int ticks) {
        if (ticks <= 0) {
            return "0s";
        }
        int seconds = Math.max(1, Math.round(ticks / 20.0f));
        int minutes = seconds / 60;
        int rest = seconds % 60;
        return minutes > 0 ? minutes + "m " + rest + "s" : rest + "s";
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static String compact(int count) {
        if (count >= 1_000_000) return count / 1_000_000 + "M";
        if (count >= 1_000) return count / 1_000 + "K";
        return String.valueOf(count);
    }

    private static String shorten(TextRenderer textRenderer, String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }
        String shortened = value;
        while (shortened.length() > 1 && textRenderer.getWidth(shortened + "...") > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + "...";
    }

    private record PanelPos(int x, int y) {
    }

    private record LootEntry(ItemStack stack, String name, String itemId, int count, int stacks, int firstSlot) {
    }

    private record PanelButton(int x, int y, int width, int height, Runnable action) {
    }

    private record RowButton(int x, int y, int width, int height, String itemId) {
    }

    private enum MachineKind {
        FURNACE("Fornalha", 200),
        BLAST_FURNACE("Alta fornalha", 100),
        SMOKER("Defumador", 100);

        private final String title;
        private final int cookTicks;

        MachineKind(String title, int cookTicks) {
            this.title = title;
            this.cookTicks = cookTicks;
        }

        private String title() {
            return title;
        }

        private int cookTicks() {
            return cookTicks;
        }
    }

    private record FurnaceStats(
        String title,
        ItemStack input,
        ItemStack fuel,
        boolean hasInput,
        boolean hasFuel,
        boolean burning,
        float fuelProgress,
        float cookProgress,
        String burnRemainingText,
        String itemsFromCurrentBurn,
        String itemsFromFuelStack,
        String fuelUnitTime,
        String currentBurnLabel,
        String fuelStackLabel,
        String status,
        int statusColor
    ) {
    }

    private static final class LootEntryBuilder {
        private final ItemStack stack;
        private final String name;
        private final String itemId;
        private final int firstSlot;
        private int count;
        private int stacks;

        private LootEntryBuilder(ItemStack stack, String name, String itemId, int firstSlot) {
            this.stack = stack;
            this.name = name;
            this.itemId = itemId;
            this.firstSlot = firstSlot;
        }

        private void add(int amount, int slotId) {
            count += amount;
            stacks++;
        }

        private LootEntry build() {
            return new LootEntry(stack, name, itemId, count, stacks, firstSlot);
        }
    }
}
