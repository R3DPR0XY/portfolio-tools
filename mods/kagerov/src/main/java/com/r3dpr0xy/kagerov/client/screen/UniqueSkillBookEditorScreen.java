package com.r3dpr0xy.kagerov.client.screen;

import com.r3dpr0xy.kagerov.client.book.UniqueSkillBookClipboard;
import com.r3dpr0xy.kagerov.client.book.UniqueSkillTextTokens;
import com.r3dpr0xy.kagerov.client.book.UniqueSkillUnicodeTokens;
import com.r3dpr0xy.kagerov.mixin.EditBoxAccessor;
import com.r3dpr0xy.kagerov.mixin.EditBoxWidgetAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class UniqueSkillBookEditorScreen extends Screen {
    private static final int PANEL_WIDTH = 572;
    private static final int PANEL_HEIGHT = 392;
    private static final int EDIT_WIDTH = 238;
    private static final int PREVIEW_WIDTH = 238;
    private static final int EDIT_HEIGHT = 146;
    private static final int MAX_LINES = 14;
    private static final int SYMBOLS_PER_ROW = 10;
    private static final Pattern FORMATTING_PATTERN = Pattern.compile("(?i)\u00A7[0-9A-FK-OR]");
    private static final List<String> FAVORITE_SYMBOLS = List.of("\u2764", "\u2605", "\u2709", "\u2726", "\u2694", "\u2620", "\u2192", "\u2666", "\u2600", "\u2727");
    private static final List<Snippet> SNIPPETS = List.of(
        new Snippet("Carta", "Prezado(a),\n\nEscrevo para informar que...\n\nAtenciosamente,"),
        new Snippet("Contrato", "==========\nClausula I\n\nClausula II\n\n=========="),
        new Snippet("Grimorio", "\u2726 Titulo do Ritual \u2726\n\nIngredientes:\n- \n- \n\nEfeito:\n"),
        new Snippet("Aviso", "\u26A0 AVISO \u26A0\n\nLocal:\nHorario:\nDetalhes:\n")
    );

    private final Screen parent;
    private final ApplyChangesCallback applyChangesCallback;
    private final List<String> workingPages;
    private final List<EditorState> history = new ArrayList<>();
    private final Set<String> activeStyles = new LinkedHashSet<>();
    private int currentPage;
    private int historyIndex = -1;
    private int unicodeOffset;
    private String activeColor = "";
    private boolean suppressHistory;

    private EditBoxWidget editBox;
    private TextFieldWidget pageField;
    private TextFieldWidget searchField;

    public UniqueSkillBookEditorScreen(Screen parent, List<String> pages, int currentPage, ApplyChangesCallback applyChangesCallback) {
        super(Text.translatable("screen.kagerov.editor.title"));
        this.parent = parent;
        this.applyChangesCallback = applyChangesCallback;
        this.workingPages = new ArrayList<>(pages.isEmpty() ? List.of("") : pages);
        this.currentPage = Math.max(0, Math.min(currentPage, this.workingPages.size() - 1));
    }

    @Override
    protected void init() {
        this.clearChildren();

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        this.pageField = new TextFieldWidget(this.textRenderer, panelX + 16, panelY + 28, 40, 18, Text.empty());
        this.pageField.setMaxLength(4);
        this.pageField.setText(Integer.toString(this.currentPage + 1));
        this.addDrawableChild(this.pageField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.editor.jump"), button -> jumpToPage())
            .dimensions(panelX + 60, panelY + 28, 44, 18)
            .build());

        this.searchField = new TextFieldWidget(this.textRenderer, panelX + 114, panelY + 28, 92, 18, Text.empty());
        this.searchField.setMaxLength(64);
        this.addDrawableChild(this.searchField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.editor.search"), button -> searchNext())
            .dimensions(panelX + 210, panelY + 28, 54, 18)
            .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Undo"), button -> undo())
            .dimensions(panelX + 382, panelY + 28, 48, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Redo"), button -> redo())
            .dimensions(panelX + 434, panelY + 28, 48, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.import_action"), button -> openImportLibrary())
            .dimensions(panelX + 486, panelY + 28, 38, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.save"), button -> openExportLibrary())
            .dimensions(panelX + 528, panelY + 28, 28, 18)
            .build());

        this.editBox = EditBoxWidget.builder()
            .x(panelX + 16)
            .y(panelY + 58)
            .placeholder(Text.translatable("screen.kagerov.editor.placeholder"))
            .textColor(0xFFF4EBDD)
            .textShadow(false)
            .hasBackground(true)
            .hasOverlay(true)
            .build(this.textRenderer, EDIT_WIDTH, EDIT_HEIGHT, Text.empty());
        this.editBox.setMaxLength(WritableBookContentComponent.MAX_PAGE_LENGTH);
        this.editBox.setMaxLines(MAX_LINES);
        this.editBox.setChangeListener(this::handleEditBoxChange);
        this.addDrawableChild(this.editBox);
        this.setFocused(this.editBox);
        this.editBox.setFocused(true);
        neutralizeMovementKeys();

        int pageControlsY = panelY + 214;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> previousPage())
            .dimensions(panelX + 16, pageControlsY, 24, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> nextPage())
            .dimensions(panelX + 44, pageControlsY, 24, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.book.page_add"), button -> addPage())
            .dimensions(panelX + 74, pageControlsY, 78, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.book.page_remove"), button -> removePage())
            .dimensions(panelX + 156, pageControlsY, 92, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.book.page_copy"), button -> copyPage())
            .dimensions(panelX + 252, pageControlsY, 82, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.book.page_paste"), button -> pastePage())
            .dimensions(panelX + 338, pageControlsY, 82, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.book.page_clear"), button -> clearPage())
            .dimensions(panelX + 424, pageControlsY, 82, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.editor.apply"), button -> applyAndClose())
            .dimensions(panelX + 510, pageControlsY, 46, 18)
            .build());

        initStyleToolbar(panelX, panelY);
        initFavorites(panelX, panelY);
        initSnippets(panelX, panelY);

        if (this.history.isEmpty()) {
            pushHistory();
        }
        refreshPage();
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.editBox != null && this.editBox.isFocused() && !Character.isISOControl(chr)) {
            String activePrefix = getActivePrefix();
            if (!activePrefix.isEmpty()) {
                insertTypedText(Character.toString(chr));
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((modifiers & 2) != 0) {
            if (keyCode == 90) {
                undo();
                return true;
            }
            if (keyCode == 89) {
                redo();
                return true;
            }
        }
        if (isGameplayKey(keyCode, scanCode)) {
            neutralizeMovementKeys();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (isGameplayKey(keyCode, scanCode)) {
            neutralizeMovementKeys();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xA0100808);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        int previewX = panelX + 304;
        int previewY = panelY + 58;

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xE0140808);
        context.drawBorder(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFF9B3A3A);
        context.drawText(this.textRenderer, this.title, panelX + 16, panelY + 10, 0xFFF7EDE8, false);
        context.drawText(this.textRenderer, Text.translatable("screen.kagerov.editor.page_indicator", this.currentPage + 1, this.workingPages.size()), panelX + 16, panelY + 18, 0xFFD9B8B0, false);

        int charCount = this.workingPages.get(this.currentPage).length();
        context.drawText(this.textRenderer, Text.translatable("screen.kagerov.editor.char_count", charCount, WritableBookContentComponent.MAX_PAGE_LENGTH), panelX + 166, panelY + 18, charCount > 220 ? 0xFFFF9C9C : 0xFFD9B8B0, false);
        context.drawText(this.textRenderer, Text.translatable("screen.kagerov.editor.preview"), previewX, panelY + 44, 0xFFF7EDE8, false);
        context.drawText(this.textRenderer, Text.translatable("screen.kagerov.editor.active_style", readableActiveStyle()), panelX + 304, panelY + 266, 0xFFD9B8B0, false);

        context.fill(previewX - 6, previewY - 6, previewX + PREVIEW_WIDTH + 6, previewY + EDIT_HEIGHT + 6, 0x8F0F0707);
        context.drawBorder(previewX - 6, previewY - 6, PREVIEW_WIDTH + 12, EDIT_HEIGHT + 12, 0x996E2A2A);
        renderPreview(context, previewX, previewY);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        neutralizeMovementKeys();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void initStyleToolbar(int panelX, int panelY) {
        int colorY = panelY + 284;
        int formatY = panelY + 308;
        int colorStartX = panelX + 16;

        for (int index = 0; index < UniqueSkillTextTokens.COLOR_VALUES.length; index++) {
            final int colorIndex = index;
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("\u25A0").setStyle(Style.EMPTY.withColor(UniqueSkillTextTokens.COLOR_VALUES[index])),
                    button -> selectColor(UniqueSkillTextTokens.colorToken(colorIndex))
                )
                .dimensions(colorStartX + (index * 22), colorY, 20, 20)
                .build());
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("B"), button -> toggleStyle("\u00A7l"))
            .dimensions(panelX + 16, formatY, 20, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("I"), button -> toggleStyle("\u00A7o"))
            .dimensions(panelX + 40, formatY, 20, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("U"), button -> toggleStyle("\u00A7n"))
            .dimensions(panelX + 64, formatY, 20, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("S"), button -> toggleStyle("\u00A7m"))
            .dimensions(panelX + 88, formatY, 20, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("M"), button -> toggleStyle("\u00A7k"))
            .dimensions(panelX + 112, formatY, 20, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.book.format_reset"), button -> resetFormatting())
            .dimensions(panelX + 140, formatY, 110, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.book.page_strip_format"), button -> stripFormatting())
            .dimensions(panelX + 254, formatY, 120, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Unicode+"), button -> openPalette())
            .dimensions(panelX + 378, formatY, 86, 20).build());
    }

    private void initFavorites(int panelX, int panelY) {
        int y = panelY + 336;
        for (int index = 0; index < FAVORITE_SYMBOLS.size(); index++) {
            final String symbol = FAVORITE_SYMBOLS.get(index);
            this.addDrawableChild(ButtonWidget.builder(Text.literal(symbol), button -> insertRawAtCursor(symbol))
                .dimensions(panelX + 16 + (index * 22), y, 20, 20)
                .build());
        }

        for (int index = 0; index < SYMBOLS_PER_ROW; index++) {
            int tokenIndex = this.unicodeOffset + index;
            if (tokenIndex >= UniqueSkillUnicodeTokens.SAFE_TOKENS.length) {
                break;
            }
            final String symbol = UniqueSkillUnicodeTokens.SAFE_TOKENS[tokenIndex];
            this.addDrawableChild(ButtonWidget.builder(Text.literal(symbol), button -> insertRawAtCursor(symbol))
                .dimensions(panelX + 252 + (index * 22), y, 20, 20)
                .build());
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> shiftUnicode(-SYMBOLS_PER_ROW))
            .dimensions(panelX + 472, y, 20, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> shiftUnicode(SYMBOLS_PER_ROW))
            .dimensions(panelX + 496, y, 20, 20).build());
    }

    private void initSnippets(int panelX, int panelY) {
        int y = panelY + 240;
        for (int index = 0; index < SNIPPETS.size(); index++) {
            Snippet snippet = SNIPPETS.get(index);
            this.addDrawableChild(ButtonWidget.builder(Text.literal(snippet.label()), button -> insertRawAtCursor(snippet.content()))
                .dimensions(panelX + 304 + (index * 62), y, 58, 18)
                .build());
        }
    }

    private int pageControlsY(int panelY) {
        return panelY + 288;
    }

    private void handleEditBoxChange(String text) {
        this.workingPages.set(this.currentPage, UniqueSkillBookClipboard.trimToMaxLength(text));
        if (!this.suppressHistory) {
            pushHistory();
        }
    }

    private void jumpToPage() {
        try {
            int page = Integer.parseInt(this.pageField.getText()) - 1;
            if (page >= 0 && page < this.workingPages.size()) {
                saveCurrentPage();
                this.currentPage = page;
                refreshPage();
            }
        } catch (NumberFormatException ignored) {
        }
        this.pageField.setText(Integer.toString(this.currentPage + 1));
    }

    private void searchNext() {
        String query = this.searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return;
        }
        for (int index = this.currentPage + 1; index < this.workingPages.size(); index++) {
            if (this.workingPages.get(index).toLowerCase(Locale.ROOT).contains(query)) {
                saveCurrentPage();
                this.currentPage = index;
                refreshPage();
                return;
            }
        }
        for (int index = 0; index <= this.currentPage; index++) {
            if (this.workingPages.get(index).toLowerCase(Locale.ROOT).contains(query)) {
                saveCurrentPage();
                this.currentPage = index;
                refreshPage();
                return;
            }
        }
    }

    private void previousPage() {
        if (this.currentPage > 0) {
            saveCurrentPage();
            this.currentPage--;
            refreshPage();
        }
    }

    private void nextPage() {
        if (this.currentPage < this.workingPages.size() - 1) {
            saveCurrentPage();
            this.currentPage++;
            refreshPage();
        }
    }

    private void addPage() {
        if (this.workingPages.size() >= WritableBookContentComponent.MAX_PAGE_COUNT) {
            return;
        }
        saveCurrentPage();
        this.workingPages.add(this.currentPage + 1, "");
        this.currentPage++;
        pushHistory();
        refreshPage();
    }

    private void removePage() {
        if (this.workingPages.size() <= 1) {
            this.workingPages.set(0, "");
            this.currentPage = 0;
        } else {
            this.workingPages.remove(this.currentPage);
            if (this.currentPage >= this.workingPages.size()) {
                this.currentPage = this.workingPages.size() - 1;
            }
        }
        pushHistory();
        refreshPage();
    }

    private void copyPage() {
        saveCurrentPage();
        UniqueSkillBookClipboard.copyPage(this.workingPages.get(this.currentPage));
    }

    private void pastePage() {
        setCurrentPageText(UniqueSkillBookClipboard.getCopiedPage());
    }

    private void clearPage() {
        setCurrentPageText("");
    }

    private void openPalette() {
        if (this.client != null) {
            saveCurrentPage();
            this.client.setScreen(new UniqueSkillTextPaletteScreen(this, this::insertRawAtCursor));
        }
    }

    private void selectColor(String colorToken) {
        this.activeColor = colorToken;
        if (hasSelection()) {
            applyActiveFormattingToSelection();
        }
    }

    private void toggleStyle(String styleToken) {
        if (this.activeStyles.contains(styleToken)) {
            this.activeStyles.remove(styleToken);
        } else {
            this.activeStyles.add(styleToken);
        }
        if (hasSelection()) {
            applyActiveFormattingToSelection();
        }
    }

    private void resetFormatting() {
        this.activeColor = "";
        this.activeStyles.clear();
        applyFormattingToken("\u00A7r");
    }

    private void stripFormatting() {
        setCurrentPageText(FORMATTING_PATTERN.matcher(this.editBox.getText()).replaceAll(""));
    }

    private void insertTypedText(String typed) {
        String prefix = getActivePrefix();
        EditBox inner = innerEditBox();
        String currentText = inner.getText();
        int cursor = cursorIndex(inner);
        String currentState = activeFormattingAt(currentText, cursor);
        insertRawAtCursor((prefix.equals(currentState) ? "" : prefix) + typed);
    }

    private void insertRawAtCursor(String token) {
        if (this.editBox == null || token == null || token.isEmpty()) {
            return;
        }
        EditBox inner = innerEditBox();
        String currentText = inner.getText();
        int cursor = cursorIndex(inner);
        int selectionEnd = selectionEndIndex(inner);
        int selectionStart = Math.min(cursor, selectionEnd);
        int selectionStop = Math.max(cursor, selectionEnd);

        String nextText = currentText.substring(0, selectionStart) + token + currentText.substring(selectionStop);
        if (nextText.length() > WritableBookContentComponent.MAX_PAGE_LENGTH) {
            nextText = nextText.substring(0, WritableBookContentComponent.MAX_PAGE_LENGTH);
        }

        setCurrentPageText(nextText);
        EditBox refreshed = innerEditBox();
        int targetCursor = Math.min(nextText.length(), selectionStart + token.length());
        ((EditBoxAccessor) refreshed).uniqueSkill$setCursorIndex(targetCursor);
        ((EditBoxAccessor) refreshed).uniqueSkill$setSelectionEndIndex(targetCursor);
    }

    private void applyFormattingToken(String token) {
        if (this.editBox == null) {
            return;
        }
        EditBox inner = innerEditBox();
        String currentText = inner.getText();
        int cursor = cursorIndex(inner);
        int selectionEnd = selectionEndIndex(inner);
        int selectionStart = Math.min(cursor, selectionEnd);
        int selectionStop = Math.max(cursor, selectionEnd);

        if (selectionStart == selectionStop) {
            if (token.equals("\u00A7r")) {
                insertRawAtCursor(token);
            }
            return;
        }

        String restoreState = activeFormattingAt(currentText, selectionStart);
        String prefix = token.equals("\u00A7k") ? restoreState + token : token;
        String wrapped = currentText.substring(0, selectionStart)
            + prefix
            + currentText.substring(selectionStart, selectionStop)
            + "\u00A7r"
            + restoreState
            + currentText.substring(selectionStop);
        setCurrentPageText(wrapped);
    }

    private void applyActiveFormattingToSelection() {
        String prefix = getActivePrefix();
        if (prefix.isEmpty()) {
            applyFormattingToken("\u00A7r");
            return;
        }
        applyFormattingToken(prefix);
    }

    private boolean hasSelection() {
        EditBox inner = innerEditBox();
        return cursorIndex(inner) != selectionEndIndex(inner);
    }

    private String getActivePrefix() {
        StringBuilder builder = new StringBuilder();
        if (!this.activeColor.isEmpty()) {
            builder.append(this.activeColor);
        }
        for (String style : this.activeStyles) {
            builder.append(style);
        }
        return builder.toString();
    }

    private String readableActiveStyle() {
        StringBuilder builder = new StringBuilder();
        if (!this.activeColor.isEmpty()) {
            builder.append(this.activeColor).append(" ");
        }
        if (this.activeStyles.isEmpty()) {
            builder.append("padrao");
        } else {
            this.activeStyles.forEach(builder::append);
        }
        return builder.toString().trim();
    }

    private void openImportLibrary() {
        if (this.client != null) {
            saveCurrentPage();
            this.client.setScreen(new UniqueSkillBookLibraryScreen(this, book -> {
                this.workingPages.clear();
                this.workingPages.addAll(book.pages());
                this.currentPage = 0;
                pushHistory();
                refreshPage();
            }));
        }
    }

    private void openExportLibrary() {
        if (this.client != null) {
            saveCurrentPage();
            this.client.setScreen(new UniqueSkillBookLibraryScreen(
                this,
                "Livro sem titulo",
                List.copyOf(this.workingPages),
                "writable_book",
                storedBook -> {
                }
            ));
        }
    }

    private void applyAndClose() {
        saveCurrentPage();
        this.applyChangesCallback.apply(List.copyOf(this.workingPages), this.currentPage);
        close();
    }

    private void saveCurrentPage() {
        this.workingPages.set(this.currentPage, UniqueSkillBookClipboard.trimToMaxLength(this.editBox.getText()));
    }

    private void refreshPage() {
        this.pageField.setText(Integer.toString(this.currentPage + 1));
        this.suppressHistory = true;
        this.editBox.setText(this.workingPages.get(this.currentPage));
        this.suppressHistory = false;
    }

    private void renderPreview(DrawContext context, int x, int y) {
        Text previewText = parseLegacyFormatting(this.workingPages.get(this.currentPage));
        List<OrderedText> wrapped = new ArrayList<>(this.textRenderer.wrapLines(previewText, PREVIEW_WIDTH));
        int maxLines = Math.min(MAX_LINES, wrapped.size());
        for (int index = 0; index < maxLines; index++) {
            context.drawText(this.textRenderer, wrapped.get(index), x, y + (index * 9), 0xFFF4EBDD, false);
        }
    }

    private void pushHistory() {
        EditorState snapshot = new EditorState(this.currentPage, List.copyOf(this.workingPages));
        if (this.historyIndex >= 0 && this.history.get(this.historyIndex).equals(snapshot)) {
            return;
        }
        while (this.history.size() > this.historyIndex + 1) {
            this.history.remove(this.history.size() - 1);
        }
        this.history.add(snapshot);
        this.historyIndex = this.history.size() - 1;
    }

    private void undo() {
        if (this.historyIndex <= 0) {
            return;
        }
        this.historyIndex--;
        restoreHistory(this.history.get(this.historyIndex));
    }

    private void redo() {
        if (this.historyIndex >= this.history.size() - 1) {
            return;
        }
        this.historyIndex++;
        restoreHistory(this.history.get(this.historyIndex));
    }

    private void restoreHistory(EditorState state) {
        this.workingPages.clear();
        this.workingPages.addAll(state.pages());
        this.currentPage = Math.max(0, Math.min(state.currentPage(), this.workingPages.size() - 1));
        refreshPage();
    }

    private void shiftUnicode(int amount) {
        int maxOffset = Math.max(0, UniqueSkillUnicodeTokens.SAFE_TOKENS.length - SYMBOLS_PER_ROW);
        this.unicodeOffset = Math.max(0, Math.min(maxOffset, this.unicodeOffset + amount));
        this.clearAndInit();
    }

    private void setCurrentPageText(String text) {
        this.suppressHistory = true;
        this.editBox.setText(UniqueSkillBookClipboard.trimToMaxLength(text));
        this.suppressHistory = false;
        this.workingPages.set(this.currentPage, UniqueSkillBookClipboard.trimToMaxLength(text));
        pushHistory();
    }

    private boolean isGameplayKey(int keyCode, int scanCode) {
        if (this.client == null) {
            return false;
        }
        return matches(this.client.options.forwardKey, keyCode, scanCode)
            || matches(this.client.options.backKey, keyCode, scanCode)
            || matches(this.client.options.leftKey, keyCode, scanCode)
            || matches(this.client.options.rightKey, keyCode, scanCode)
            || matches(this.client.options.jumpKey, keyCode, scanCode)
            || matches(this.client.options.sneakKey, keyCode, scanCode)
            || matches(this.client.options.sprintKey, keyCode, scanCode);
    }

    private boolean matches(KeyBinding binding, int keyCode, int scanCode) {
        return binding != null && binding.matchesKey(keyCode, scanCode);
    }

    private void neutralizeMovementKeys() {
        if (this.client == null) {
            return;
        }
        this.client.options.forwardKey.setPressed(false);
        this.client.options.backKey.setPressed(false);
        this.client.options.leftKey.setPressed(false);
        this.client.options.rightKey.setPressed(false);
        this.client.options.jumpKey.setPressed(false);
        this.client.options.sneakKey.setPressed(false);
        this.client.options.sprintKey.setPressed(false);
    }

    private String activeFormattingAt(String text, int index) {
        String color = "";
        StringBuilder styles = new StringBuilder();
        for (int i = 0; i < text.length() - 1 && i < index; i++) {
            if (text.charAt(i) != '\u00A7') {
                continue;
            }
            char code = Character.toLowerCase(text.charAt(i + 1));
            if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) {
                color = "\u00A7" + code;
                styles.setLength(0);
            } else if (code == 'r') {
                color = "";
                styles.setLength(0);
            } else if ("klmno".indexOf(code) >= 0) {
                String styleToken = "\u00A7" + code;
                if (styles.indexOf(styleToken) < 0) {
                    styles.append(styleToken);
                }
            }
            i++;
        }
        return color + styles;
    }

    private EditBox innerEditBox() {
        return ((EditBoxWidgetAccessor) this.editBox).uniqueSkill$getEditBox();
    }

    private int cursorIndex(EditBox inner) {
        return ((EditBoxAccessor) inner).uniqueSkill$getCursorIndex();
    }

    private int selectionEndIndex(EditBox inner) {
        return ((EditBoxAccessor) inner).uniqueSkill$getSelectionEndIndex();
    }

    private Text parseLegacyFormatting(String rawText) {
        MutableText root = Text.empty();
        Style style = Style.EMPTY.withColor(0x000000);
        StringBuilder chunk = new StringBuilder();

        for (int index = 0; index < rawText.length(); index++) {
            char current = rawText.charAt(index);
            if (current == '\u00A7' && index + 1 < rawText.length()) {
                if (!chunk.isEmpty()) {
                    root.append(Text.literal(chunk.toString()).setStyle(style));
                    chunk.setLength(0);
                }

                char code = Character.toLowerCase(rawText.charAt(index + 1));
                style = applyLegacyCode(style, code);
                index++;
                continue;
            }
            chunk.append(current);
        }

        if (!chunk.isEmpty()) {
            root.append(Text.literal(chunk.toString()).setStyle(style));
        }
        return root;
    }

    private Style applyLegacyCode(Style currentStyle, char code) {
        Integer color = switch (code) {
            case '0' -> 0x000000;
            case '1' -> 0x0000AA;
            case '2' -> 0x00AA00;
            case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000;
            case '5' -> 0xAA00AA;
            case '6' -> 0xFFAA00;
            case '7' -> 0xAAAAAA;
            case '8' -> 0x555555;
            case '9' -> 0x5555FF;
            case 'a' -> 0x55FF55;
            case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555;
            case 'd' -> 0xFF55FF;
            case 'e' -> 0xFFFF55;
            case 'f' -> 0xFFFFFF;
            default -> null;
        };

        if (color != null) {
            return Style.EMPTY.withColor(color);
        }

        return switch (code) {
            case 'k' -> currentStyle.withObfuscated(true);
            case 'l' -> currentStyle.withBold(true);
            case 'm' -> currentStyle.withStrikethrough(true);
            case 'n' -> currentStyle.withUnderline(true);
            case 'o' -> currentStyle.withItalic(true);
            case 'r' -> Style.EMPTY.withColor(0x000000);
            default -> currentStyle;
        };
    }

    @FunctionalInterface
    public interface ApplyChangesCallback {
        void apply(List<String> pages, int currentPage);
    }

    private record EditorState(int currentPage, List<String> pages) {
    }

    private record Snippet(String label, String content) {
    }
}

