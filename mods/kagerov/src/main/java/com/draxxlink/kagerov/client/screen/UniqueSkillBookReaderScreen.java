package com.draxxlink.kagerov.client.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UniqueSkillBookReaderScreen extends Screen {
    private static final int PANEL_WIDTH = 332;
    private static final int PANEL_HEIGHT = 232;
    private static final int CONTENT_X = 18;
    private static final int CONTENT_Y = 62;
    private static final int CONTENT_WIDTH = 296;
    private static final int CONTENT_HEIGHT = 126;
    private static final int LINES_PER_PAGE = 13;

    private final Screen parent;
    private final Text bookTitle;
    private final List<Text> pages;
    private final String librarySourceType;
    private int currentPage;

    private TextFieldWidget pageField;
    private TextFieldWidget searchField;

    public UniqueSkillBookReaderScreen(Screen parent, Text bookTitle, List<Text> pages, int currentPage) {
        this(parent, bookTitle, pages, currentPage, "manual");
    }

    public UniqueSkillBookReaderScreen(Screen parent, Text bookTitle, List<Text> pages, int currentPage, String librarySourceType) {
        super(Text.translatable("screen.kagerov.reader.title"));
        this.parent = parent;
        this.bookTitle = bookTitle;
        this.pages = pages.isEmpty() ? List.of(Text.empty()) : List.copyOf(pages);
        this.librarySourceType = librarySourceType == null || librarySourceType.isBlank() ? "manual" : librarySourceType;
        this.currentPage = Math.max(0, Math.min(currentPage, this.pages.size() - 1));
    }

    @Override
    protected void init() {
        this.clearChildren();

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        this.pageField = new TextFieldWidget(this.textRenderer, panelX + 18, panelY + 30, 42, 18, Text.empty());
        this.pageField.setMaxLength(4);
        this.pageField.setText(Integer.toString(this.currentPage + 1));
        this.addDrawableChild(this.pageField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.reader.jump"), button -> jumpToPage())
            .dimensions(panelX + 64, panelY + 30, 54, 18)
            .build());

        this.searchField = new TextFieldWidget(this.textRenderer, panelX + 126, panelY + 30, 116, 18, Text.empty());
        this.searchField.setMaxLength(64);
        this.addDrawableChild(this.searchField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.reader.search"), button -> searchNext())
            .dimensions(panelX + 246, panelY + 30, 68, 18)
            .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> previousPage())
            .dimensions(panelX + 18, panelY + 196, 24, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> nextPage())
            .dimensions(panelX + 46, panelY + 196, 24, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.library.save"), button -> openExportLibrary())
            .dimensions(panelX + 140, panelY + 196, 68, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.reader.close"), button -> close())
            .dimensions(panelX + 232, panelY + 196, 82, 20)
            .build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return switch (keyCode) {
            case 263 -> {
                previousPage();
                yield true;
            }
            case 262 -> {
                nextPage();
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xA0100808);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        int contentLeft = panelX + CONTENT_X;
        int contentTop = panelY + CONTENT_Y;

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xE0140808);
        context.drawBorder(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFF9B3A3A);
        context.drawText(this.textRenderer, this.title, panelX + 18, panelY + 12, 0xFFF7EDE8, false);
        context.drawText(this.textRenderer, this.bookTitle, panelX + 18, panelY + 20, 0xFFD9B8B0, false);

        context.fill(contentLeft - 6, contentTop - 6, contentLeft + CONTENT_WIDTH + 6, contentTop + CONTENT_HEIGHT + 6, 0xA60E0606);
        context.drawBorder(contentLeft - 6, contentTop - 6, CONTENT_WIDTH + 12, CONTENT_HEIGHT + 12, 0x996E2A2A);

        drawCurrentPage(context, this.textRenderer, contentLeft, contentTop);

        String indicator = (this.currentPage + 1) + " / " + this.pages.size();
        context.drawText(this.textRenderer, Text.literal(indicator), panelX + 90, panelY + 200, 0xFFD9B8B0, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void drawCurrentPage(DrawContext context, TextRenderer textRenderer, int x, int y) {
        Text page = this.pages.get(this.currentPage);
        List<OrderedText> wrapped = new ArrayList<>(textRenderer.wrapLines(page, CONTENT_WIDTH));
        int maxLines = Math.min(LINES_PER_PAGE, wrapped.size());

        for (int index = 0; index < maxLines; index++) {
            context.drawText(textRenderer, wrapped.get(index), x, y + (index * 9), 0xFFF4EBDD, false);
        }
    }

    private void previousPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.pageField.setText(Integer.toString(this.currentPage + 1));
        }
    }

    private void nextPage() {
        if (this.currentPage < this.pages.size() - 1) {
            this.currentPage++;
            this.pageField.setText(Integer.toString(this.currentPage + 1));
        }
    }

    private void jumpToPage() {
        try {
            int page = Integer.parseInt(this.pageField.getText()) - 1;
            if (page >= 0 && page < this.pages.size()) {
                this.currentPage = page;
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

        for (int index = this.currentPage + 1; index < this.pages.size(); index++) {
            if (this.pages.get(index).getString().toLowerCase(Locale.ROOT).contains(query)) {
                this.currentPage = index;
                this.pageField.setText(Integer.toString(this.currentPage + 1));
                return;
            }
        }

        for (int index = 0; index <= this.currentPage; index++) {
            if (this.pages.get(index).getString().toLowerCase(Locale.ROOT).contains(query)) {
                this.currentPage = index;
                this.pageField.setText(Integer.toString(this.currentPage + 1));
                return;
            }
        }
    }

    private void openExportLibrary() {
        if (this.client != null) {
            List<String> rawPages = this.pages.stream().map(Text::getString).toList();
            this.client.setScreen(new UniqueSkillBookLibraryScreen(this, this.bookTitle.getString(), rawPages, this.librarySourceType, storedBook -> {
            }));
        }
    }
}

