package com.draxxlink.kagerov.client.screen;

import com.draxxlink.kagerov.client.book.UniqueSkillBookLibrary;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UniqueSkillBookLibraryScreen extends Screen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 286;
    private static final int VISIBLE_ROWS = 8;
    private static final int ROW_HEIGHT = 22;

    private final Screen parent;
    private final Mode mode;
    private final List<String> sourcePages;
    private final String exportSourceType;
    private final ImportCallback importCallback;
    private final ExportCallback exportCallback;
    private final String suggestedTitle;

    private final List<UniqueSkillBookLibrary.StoredBook> allBooks = new ArrayList<>();
    private final List<UniqueSkillBookLibrary.StoredBook> filteredBooks = new ArrayList<>();
    private int scrollOffset;
    private String statusMessage = "";
    private boolean suppressFieldRefresh;

    private UniqueSkillBookLibrary.StoredBook selectedBook;
    private TextFieldWidget searchField;
    private TextFieldWidget titleField;

    public UniqueSkillBookLibraryScreen(Screen parent, ImportCallback importCallback) {
        super(Text.translatable("screen.kagerov.library.import_title"));
        this.parent = parent;
        this.mode = Mode.IMPORT;
        this.sourcePages = List.of();
        this.exportSourceType = "manual";
        this.importCallback = importCallback;
        this.exportCallback = null;
        this.suggestedTitle = "";
    }

    public UniqueSkillBookLibraryScreen(Screen parent, String suggestedTitle, List<String> pages, ExportCallback exportCallback) {
        this(parent, suggestedTitle, pages, "manual", exportCallback);
    }

    public UniqueSkillBookLibraryScreen(Screen parent, String suggestedTitle, List<String> pages, String exportSourceType, ExportCallback exportCallback) {
        super(Text.translatable("screen.kagerov.library.export_title"));
        this.parent = parent;
        this.mode = Mode.EXPORT;
        this.sourcePages = List.copyOf(pages);
        this.exportSourceType = exportSourceType == null || exportSourceType.isBlank() ? "manual" : exportSourceType;
        this.importCallback = null;
        this.exportCallback = exportCallback;
        this.suggestedTitle = suggestedTitle == null || suggestedTitle.isBlank() ? "Livro sem titulo" : suggestedTitle;
    }

    @Override
    protected void init() {
        this.clearChildren();
        reloadBooks();

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        this.searchField = new TextFieldWidget(this.textRenderer, panelX + 16, panelY + 32, 170, 18, Text.empty());
        this.searchField.setMaxLength(64);
        this.searchField.setChangedListener(text -> {
            if (!this.suppressFieldRefresh) {
                applyFilterAndRefresh();
            }
        });
        this.addDrawableChild(this.searchField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.refresh"), button -> reloadAndRefresh())
            .dimensions(panelX + 190, panelY + 32, 70, 18)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.close"), button -> close())
            .dimensions(panelX + 334, panelY + 32, 80, 18)
            .build());

        this.titleField = new TextFieldWidget(this.textRenderer, panelX + 16, panelY + 56, 244, 18, Text.empty());
        this.titleField.setMaxLength(64);
        this.titleField.setText(this.selectedBook != null ? this.selectedBook.title() : this.suggestedTitle);
        this.addDrawableChild(this.titleField);

        if (this.mode == Mode.EXPORT) {
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.save_new"), button -> saveAsNew())
                .dimensions(panelX + 264, panelY + 56, 74, 18)
                .build());
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.overwrite"), button -> overwriteSelected())
                .dimensions(panelX + 342, panelY + 56, 72, 18)
                .build());
        } else {
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.import_action"), button -> importSelected())
                .dimensions(panelX + 264, panelY + 56, 74, 18)
                .build());
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.select"), button -> selectFirstIfMissing())
                .dimensions(panelX + 342, panelY + 56, 72, 18)
                .build());
        }

        buildBottomActions(panelX, panelY);
        buildBookRows(panelX, panelY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.filteredBooks.size() > VISIBLE_ROWS) {
            int maxOffset = this.filteredBooks.size() - VISIBLE_ROWS;
            this.scrollOffset = Math.max(0, Math.min(maxOffset, this.scrollOffset - (int) Math.signum(verticalAmount)));
            this.clearAndInit();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xA0100808);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xE0140808);
        context.drawBorder(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFF9B3A3A);
        context.drawText(this.textRenderer, this.title, panelX + 16, panelY + 12, 0xFFF7EDE8, false);
        context.drawText(this.textRenderer, Text.translatable("screen.kagerov.library.subtitle"), panelX + 16, panelY + 22, 0xFFD9B8B0, false);

        int listTop = panelY + 86;
        int listHeight = (VISIBLE_ROWS * ROW_HEIGHT) + 4;
        context.fill(panelX + 14, listTop - 4, panelX + PANEL_WIDTH - 14, listTop + listHeight, 0x9A0E0606);
        context.drawBorder(panelX + 14, listTop - 4, PANEL_WIDTH - 28, listHeight, 0x996E2A2A);

        if (this.selectedBook != null) {
            context.drawText(this.textRenderer, Text.literal(bookMetaLabel(this.selectedBook)), panelX + 16, panelY + 264, 0xFFD9B8B0, false);
        }
        if (!this.statusMessage.isBlank()) {
            context.drawText(this.textRenderer, Text.literal(trimStatus(this.statusMessage)), panelX + 16, panelY + 274, 0xFFE9CF9A, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void buildBookRows(int panelX, int panelY) {
        int listTop = panelY + 86;
        int rowStart = Math.min(this.scrollOffset, Math.max(0, this.filteredBooks.size() - VISIBLE_ROWS));
        int rowEnd = Math.min(this.filteredBooks.size(), rowStart + VISIBLE_ROWS);

        for (int index = rowStart; index < rowEnd; index++) {
            UniqueSkillBookLibrary.StoredBook book = this.filteredBooks.get(index);
            int y = listTop + ((index - rowStart) * ROW_HEIGHT);
            boolean selected = this.selectedBook != null && this.selectedBook.id().equals(book.id());
            String title = (selected ? "> " : "") + trimTitle(book.title());
            String meta = book.pages().size() + "p [" + sourceLabel(book.sourceType()) + "]";

            this.addDrawableChild(ButtonWidget.builder(Text.literal(title), button -> selectBook(book))
                .dimensions(panelX + 18, y, 246, 20)
                .build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal(meta), button -> selectBook(book))
                .dimensions(panelX + 268, y, 82, 20)
                .build());
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.select"), button -> selectBook(book))
                .dimensions(panelX + 354, y, 60, 20)
                .build());
        }
    }

    private void buildBottomActions(int panelX, int panelY) {
        int actionY = panelY + 238;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.rename"), button -> renameSelected())
            .dimensions(panelX + 16, actionY, 80, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.duplicate"), button -> duplicateSelected())
            .dimensions(panelX + 100, actionY, 80, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.delete"), button -> deleteSelected())
            .dimensions(panelX + 184, actionY, 80, 20)
            .build());

        if (this.mode == Mode.IMPORT) {
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.import_action"), button -> importSelected())
                .dimensions(panelX + 268, actionY, 146, 20)
                .build());
        } else {
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.save_new"), button -> saveAsNew())
                .dimensions(panelX + 268, actionY, 70, 20)
                .build());
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.overwrite"), button -> overwriteSelected())
                .dimensions(panelX + 342, actionY, 72, 20)
                .build());
        }
    }

    private void reloadBooks() {
        String selectedId = this.selectedBook == null ? null : this.selectedBook.id();
        this.allBooks.clear();
        this.allBooks.addAll(UniqueSkillBookLibrary.listBooks());
        applyFilter();
        if (selectedId != null) {
            for (UniqueSkillBookLibrary.StoredBook book : this.filteredBooks) {
                if (book.id().equals(selectedId)) {
                    this.selectedBook = book;
                    break;
                }
            }
        }
        if (this.selectedBook == null && !this.filteredBooks.isEmpty() && this.mode == Mode.IMPORT) {
            this.selectedBook = this.filteredBooks.get(0);
        }
    }

    private void reloadAndRefresh() {
        String search = this.searchField == null ? "" : this.searchField.getText();
        String title = this.titleField == null ? "" : this.titleField.getText();
        reloadBooks();
        rerender(search, title);
    }

    private void applyFilter() {
        String query = this.searchField == null ? "" : this.searchField.getText().trim().toLowerCase(Locale.ROOT);
        this.filteredBooks.clear();
        for (UniqueSkillBookLibrary.StoredBook book : this.allBooks) {
            boolean matches = query.isEmpty()
                || book.title().toLowerCase(Locale.ROOT).contains(query)
                || book.pages().stream().anyMatch(page -> page.toLowerCase(Locale.ROOT).contains(query));
            if (matches) {
                this.filteredBooks.add(book);
            }
        }
        if (this.selectedBook != null && this.filteredBooks.stream().noneMatch(book -> book.id().equals(this.selectedBook.id()))) {
            this.selectedBook = null;
        }
        this.scrollOffset = 0;
    }

    private void applyFilterAndRefresh() {
        String query = this.searchField == null ? "" : this.searchField.getText();
        String title = this.titleField == null ? "" : this.titleField.getText();
        applyFilter();
        rerender(query, title);
    }

    private void selectBook(UniqueSkillBookLibrary.StoredBook book) {
        this.selectedBook = book;
        this.statusMessage = bookMetaLabel(book);
        String search = this.searchField == null ? "" : this.searchField.getText();
        rerender(search, book.title());
    }

    private void selectFirstIfMissing() {
        if (this.selectedBook == null && !this.filteredBooks.isEmpty()) {
            selectBook(this.filteredBooks.get(0));
        }
    }

    private void importSelected() {
        if (this.importCallback == null || this.selectedBook == null) {
            this.statusMessage = Text.translatable("screen.kagerov.library.pick_book").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
            return;
        }
        this.importCallback.importBook(this.selectedBook);
        close();
    }

    private void saveAsNew() {
        if (this.exportCallback == null || this.titleField == null) {
            return;
        }
        try {
            UniqueSkillBookLibrary.StoredBook storedBook = UniqueSkillBookLibrary.saveBook(
                this.titleField.getText(),
                this.sourcePages,
                null,
                UniqueSkillBookLibrary.SaveMode.NEW_COPY,
                this.exportSourceType
            );
            this.selectedBook = storedBook;
            this.exportCallback.exported(storedBook);
            this.statusMessage = Text.translatable("screen.kagerov.library.saved_new", storedBook.title()).getString();
            reloadAndRefresh();
        } catch (IOException exception) {
            this.statusMessage = Text.translatable("screen.kagerov.library.error_action").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
        }
    }

    private void overwriteSelected() {
        if (this.exportCallback == null || this.titleField == null || this.selectedBook == null) {
            this.statusMessage = Text.translatable("screen.kagerov.library.pick_book").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
            return;
        }
        try {
            UniqueSkillBookLibrary.StoredBook storedBook = UniqueSkillBookLibrary.saveBook(
                this.titleField.getText(),
                this.sourcePages,
                this.selectedBook,
                UniqueSkillBookLibrary.SaveMode.OVERWRITE_SELECTED,
                this.exportSourceType
            );
            this.selectedBook = storedBook;
            this.exportCallback.exported(storedBook);
            this.statusMessage = Text.translatable("screen.kagerov.library.overwritten", storedBook.title()).getString();
            reloadAndRefresh();
        } catch (IOException exception) {
            this.statusMessage = Text.translatable("screen.kagerov.library.error_action").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
        }
    }

    private void renameSelected() {
        if (this.selectedBook == null || this.titleField == null) {
            this.statusMessage = Text.translatable("screen.kagerov.library.pick_book").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
            return;
        }
        try {
            this.selectedBook = UniqueSkillBookLibrary.renameBook(this.selectedBook, this.titleField.getText());
            this.statusMessage = Text.translatable("screen.kagerov.library.renamed", this.selectedBook.title()).getString();
            reloadAndRefresh();
        } catch (IOException exception) {
            this.statusMessage = Text.translatable("screen.kagerov.library.error_action").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
        }
    }

    private void duplicateSelected() {
        if (this.selectedBook == null) {
            this.statusMessage = Text.translatable("screen.kagerov.library.pick_book").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
            return;
        }
        try {
            String duplicateTitle = this.titleField == null ? this.selectedBook.title() : this.titleField.getText();
            this.selectedBook = UniqueSkillBookLibrary.duplicateBook(this.selectedBook, duplicateTitle);
            this.statusMessage = Text.translatable("screen.kagerov.library.duplicated", this.selectedBook.title()).getString();
            reloadAndRefresh();
        } catch (IOException exception) {
            this.statusMessage = Text.translatable("screen.kagerov.library.error_action").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
        }
    }

    private void deleteSelected() {
        if (this.selectedBook == null) {
            this.statusMessage = Text.translatable("screen.kagerov.library.pick_book").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
            return;
        }
        try {
            String deletedTitle = this.selectedBook.title();
            UniqueSkillBookLibrary.deleteBook(this.selectedBook);
            this.selectedBook = null;
            this.statusMessage = Text.translatable("screen.kagerov.library.deleted", deletedTitle).getString();
            reloadAndRefresh();
        } catch (IOException exception) {
            this.statusMessage = Text.translatable("screen.kagerov.library.error_action").getString();
            rerender(this.searchField == null ? "" : this.searchField.getText(), this.titleField == null ? "" : this.titleField.getText());
        }
    }

    private void rerender(String searchText, String titleText) {
        this.clearAndInit();
        this.suppressFieldRefresh = true;
        if (this.searchField != null) {
            this.searchField.setText(searchText == null ? "" : searchText);
        }
        if (this.titleField != null) {
            String resolvedTitle = titleText == null || titleText.isBlank()
                ? (this.selectedBook != null ? this.selectedBook.title() : this.suggestedTitle)
                : titleText;
            this.titleField.setText(resolvedTitle);
        }
        this.suppressFieldRefresh = false;
    }

    private String trimTitle(String value) {
        return value.length() <= 24 ? value : value.substring(0, 23) + "...";
    }

    private String trimStatus(String value) {
        return value.length() <= 58 ? value : value.substring(0, 57) + "...";
    }

    private String bookMetaLabel(UniqueSkillBookLibrary.StoredBook book) {
        return Text.translatable(
            "screen.kagerov.library.meta",
            book.pages().size(),
            sourceLabel(book.sourceType()),
            book.updatedAt().isBlank() ? "-" : book.updatedAt()
        ).getString();
    }

    private String sourceLabel(String sourceType) {
        return switch (sourceType) {
            case "server_custom" -> Text.translatable("screen.kagerov.library.source.server").getString();
            case "written_book" -> Text.translatable("screen.kagerov.library.source.written").getString();
            case "writable_book" -> Text.translatable("screen.kagerov.library.source.writable").getString();
            default -> Text.translatable("screen.kagerov.library.source.manual").getString();
        };
    }

    public interface ImportCallback {
        void importBook(UniqueSkillBookLibrary.StoredBook book);
    }

    public interface ExportCallback {
        void exported(UniqueSkillBookLibrary.StoredBook book);
    }

    private enum Mode {
        IMPORT,
        EXPORT
    }
}

