package com.draxxlink.kagerov.mixin;

import com.draxxlink.kagerov.client.book.UniqueSkillBookClipboard;
import com.draxxlink.kagerov.client.book.UniqueSkillPaletteBridge;
import com.draxxlink.kagerov.client.book.UniqueSkillTextTokens;
import com.draxxlink.kagerov.client.book.UniqueSkillUnicodeTokens;
import com.draxxlink.kagerov.client.screen.UniqueSkillBookEditorScreen;
import com.draxxlink.kagerov.client.screen.UniqueSkillBookLibraryScreen;
import com.draxxlink.kagerov.client.screen.UniqueSkillUnicodeSelectorScreen;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {
    private static final Pattern UNIQUE_SKILL_FORMATTING = Pattern.compile("(?i)\u00A7[0-9A-FK-OR]");
    private static final int UNIQUE_SKILL_SYMBOLS_PER_PAGE = 14;

    @Shadow
    private int currentPage;

    @Shadow
    @Final
    private List<String> pages;

    @Shadow
    @Final
    private PlayerEntity player;

    @Shadow
    @Final
    private ItemStack stack;

    @Shadow
    private EditBoxWidget editBox;

    @Shadow
    protected abstract void updatePage();

    @Unique
    private final List<ButtonWidget> uniqueSkill$colorButtons = new ArrayList<>();

    @Unique
    private final List<ButtonWidget> uniqueSkill$symbolButtons = new ArrayList<>();

    @Unique
    private boolean uniqueSkill$showSymbols;

    @Unique
    private int uniqueSkill$symbolOffset;

    @Unique
    private Text uniqueSkill$preservedCustomName;

    protected BookEditScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void uniqueSkill$addBookTools(CallbackInfo ci) {
        if (this.uniqueSkill$preservedCustomName == null) {
            Text existingName = this.stack.getCustomName();
            this.uniqueSkill$preservedCustomName = existingName == null ? null : existingName.copy();
        }

        this.uniqueSkill$colorButtons.clear();
        this.uniqueSkill$symbolButtons.clear();

        int leftX = 4;
        int rightX = this.width - 150;
        int rowY = 16;
        int rowHeight = 24;
        int wideButton = 146;

        this.addDrawableChild(button(leftX, rowY, wideButton, "screen.kagerov.library.import_title", this::uniqueSkill$openImportLibrary));
        this.addDrawableChild(button(leftX, rowY + rowHeight, wideButton, "screen.kagerov.library.export_title", this::uniqueSkill$openExportLibrary));
        this.addDrawableChild(button(leftX, rowY + (rowHeight * 2), wideButton, "screen.kagerov.book.book_clear", this::uniqueSkill$clearBook));
        this.addDrawableChild(button(leftX, rowY + (rowHeight * 3), wideButton, "screen.kagerov.book.book_copy", this::uniqueSkill$copyBook));
        this.addDrawableChild(button(leftX, rowY + (rowHeight * 4), wideButton, "screen.kagerov.book.book_paste", this::uniqueSkill$pasteBook));
        this.addDrawableChild(button(leftX, rowY + (rowHeight * 5), wideButton, "screen.kagerov.editor.button", this::uniqueSkill$openAdvancedEditor));

        this.addDrawableChild(button(rightX, rowY, wideButton, "screen.kagerov.book.page_clear", this::uniqueSkill$clearPage));
        this.addDrawableChild(button(rightX, rowY + rowHeight, wideButton, "screen.kagerov.book.page_copy", this::uniqueSkill$copyPage));
        this.addDrawableChild(button(rightX, rowY + (rowHeight * 2), wideButton, "screen.kagerov.book.page_paste", this::uniqueSkill$pastePage));
        this.addDrawableChild(button(rightX, rowY + (rowHeight * 3), wideButton, "screen.kagerov.book.page_add", this::uniqueSkill$addPageAfter));
        this.addDrawableChild(button(rightX, rowY + (rowHeight * 4), wideButton, "screen.kagerov.book.page_remove", this::uniqueSkill$removePage));
        this.addDrawableChild(button(rightX, rowY + (rowHeight * 5), wideButton, "screen.kagerov.book.clipboard_paste", this::uniqueSkill$pasteClipboard));

        this.uniqueSkill$initInlineToolbar();
        this.uniqueSkill$updateInlineToolbarVisibility();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void uniqueSkill$renderBookTools(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.drawText(
            this.textRenderer,
            Text.literal("\u2764 Stendhal-style Book Tools"),
            7,
            4,
            0xFFFFFF,
            true
        );

        int charCount = this.editBox == null ? 0 : this.editBox.getText().length();
        int color = charCount > WritableBookContentComponent.MAX_PAGE_LENGTH ? 0xFF8080 : 0xA8FF9E;
        context.drawText(
            this.textRenderer,
            Text.literal(charCount + "/" + WritableBookContentComponent.MAX_PAGE_LENGTH),
            (this.width / 2) - 22,
            164,
            color,
            false
        );
    }

    @Unique
    private void uniqueSkill$initInlineToolbar() {
        int centerX = this.width / 2;
        int topRowY = this.height - 52;
        int bottomRowY = this.height - 30;

        ButtonWidget switchButton = ButtonWidget.builder(Text.literal(this.uniqueSkill$showSymbols ? "Simb." : "Cores"), button -> {
                this.uniqueSkill$showSymbols = !this.uniqueSkill$showSymbols;
                button.setMessage(Text.literal(this.uniqueSkill$showSymbols ? "Simb." : "Cores"));
                this.uniqueSkill$updateInlineToolbarVisibility();
            })
            .dimensions(centerX - 202, topRowY, 44, 20)
            .build();
        this.addDrawableChild(switchButton);

        for (int index = 0; index < UniqueSkillTextTokens.COLOR_VALUES.length; index++) {
            final int colorIndex = index;
            ButtonWidget colorButton = ButtonWidget.builder(
                    Text.literal("\u25A0").setStyle(Style.EMPTY.withColor(UniqueSkillTextTokens.COLOR_VALUES[index])),
                    button -> this.uniqueSkill$insertPaletteToken(UniqueSkillTextTokens.colorToken(colorIndex))
                )
                .dimensions(centerX - (176 - (22 * (index + 1))), topRowY, 20, 20)
                .build();
            this.uniqueSkill$colorButtons.add(this.addDrawableChild(colorButton));
        }

        ButtonWidget unicodeButton = ButtonWidget.builder(Text.literal("U+"), button -> this.uniqueSkill$openUnicodeSelector())
            .dimensions(centerX + 180, topRowY, 20, 20)
            .build();
        this.uniqueSkill$symbolButtons.add(this.addDrawableChild(unicodeButton));
        this.uniqueSkill$rebuildSymbolButtons();

        this.addDrawableChild(textButton(centerX - 154, bottomRowY, 20, 20, "B", "\u00A7l"));
        this.addDrawableChild(textButton(centerX - 132, bottomRowY, 20, 20, "I", "\u00A7o"));
        this.addDrawableChild(textButton(centerX - 110, bottomRowY, 20, 20, "U", "\u00A7n"));
        this.addDrawableChild(textButton(centerX - 88, bottomRowY, 20, 20, "S", "\u00A7m"));
        this.addDrawableChild(textButton(centerX - 66, bottomRowY, 20, 20, "M", "\u00A7k"));
        this.addDrawableChild(textButton(centerX - 44, bottomRowY, 108, 20, Text.translatable("screen.kagerov.book.format_reset").getString(), "\u00A7r"));
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.kagerov.book.page_strip_format"), button -> this.uniqueSkill$stripCurrentPageFormatting())
            .dimensions(centerX + 66, bottomRowY, 140, 20)
            .build());
    }

    @Unique
    private void uniqueSkill$rebuildSymbolButtons() {
        for (ButtonWidget button : this.uniqueSkill$symbolButtons) {
            button.visible = false;
            button.active = false;
        }

        int centerX = this.width / 2;
        int rowY = this.height - 52;

        if (this.uniqueSkill$symbolOffset > 0) {
            ButtonWidget previousButton = ButtonWidget.builder(Text.literal("<"), button -> {
                    this.uniqueSkill$symbolOffset = Math.max(0, this.uniqueSkill$symbolOffset - UNIQUE_SKILL_SYMBOLS_PER_PAGE);
                    this.uniqueSkill$rebuildSymbolButtons();
                    this.uniqueSkill$updateInlineToolbarVisibility();
                })
                .dimensions(centerX - 176, rowY, 20, 20)
                .build();
            this.uniqueSkill$symbolButtons.add(this.addDrawableChild(previousButton));
        }

        int start = this.uniqueSkill$symbolOffset;
        int end = Math.min(start + UNIQUE_SKILL_SYMBOLS_PER_PAGE, UniqueSkillUnicodeTokens.SAFE_TOKENS.length);
        int slot = this.uniqueSkill$symbolOffset > 0 ? 2 : 1;
        for (int index = start; index < end; index++) {
            final String symbol = UniqueSkillUnicodeTokens.SAFE_TOKENS[index];
            ButtonWidget symbolButton = ButtonWidget.builder(Text.literal(symbol), button -> this.uniqueSkill$insertPaletteToken(symbol))
                .dimensions(centerX - (176 - (22 * slot)), rowY, 20, 20)
                .build();
            this.uniqueSkill$symbolButtons.add(this.addDrawableChild(symbolButton));
            slot++;
        }

        if (this.uniqueSkill$symbolOffset + UNIQUE_SKILL_SYMBOLS_PER_PAGE < UniqueSkillUnicodeTokens.SAFE_TOKENS.length) {
            ButtonWidget nextButton = ButtonWidget.builder(Text.literal(">"), button -> {
                    this.uniqueSkill$symbolOffset = Math.min(
                        Math.max(0, UniqueSkillUnicodeTokens.SAFE_TOKENS.length - 1),
                        this.uniqueSkill$symbolOffset + UNIQUE_SKILL_SYMBOLS_PER_PAGE
                    );
                    this.uniqueSkill$rebuildSymbolButtons();
                    this.uniqueSkill$updateInlineToolbarVisibility();
                })
                .dimensions(centerX - (176 - (22 * slot)), rowY, 20, 20)
                .build();
            this.uniqueSkill$symbolButtons.add(this.addDrawableChild(nextButton));
        }
    }

    @Unique
    private void uniqueSkill$updateInlineToolbarVisibility() {
        for (ButtonWidget button : this.uniqueSkill$colorButtons) {
            button.visible = !this.uniqueSkill$showSymbols;
            button.active = !this.uniqueSkill$showSymbols;
        }
        for (ButtonWidget button : this.uniqueSkill$symbolButtons) {
            button.visible = this.uniqueSkill$showSymbols;
            button.active = this.uniqueSkill$showSymbols;
        }
    }

    @Unique
    private ButtonWidget button(int x, int y, int width, String key, Runnable action) {
        return ButtonWidget.builder(Text.translatable(key), button -> action.run())
            .dimensions(x, y, width, 20)
            .build();
    }

    @Unique
    private ButtonWidget textButton(int x, int y, int width, int height, String label, String token) {
        return ButtonWidget.builder(Text.literal(label), button -> this.uniqueSkill$insertPaletteToken(token))
            .dimensions(x, y, width, height)
            .build();
    }

    @Unique
    private void uniqueSkill$copyPage() {
        this.uniqueSkill$syncCurrentPage();
        UniqueSkillBookClipboard.copyPage(this.editBox.getText());
        if (this.client != null) {
            this.client.keyboard.setClipboard(this.editBox.getText());
        }
        uniqueSkill$notify("message.kagerov.book.page_copied");
    }

    @Unique
    private void uniqueSkill$pastePage() {
        this.editBox.setText(UniqueSkillBookClipboard.getCopiedPage());
        uniqueSkill$notify("message.kagerov.book.page_pasted");
    }

    @Unique
    private void uniqueSkill$clearPage() {
        this.editBox.setText("");
        uniqueSkill$notify("message.kagerov.book.page_cleared");
    }

    @Unique
    private void uniqueSkill$addPageAfter() {
        if (this.pages.size() >= WritableBookContentComponent.MAX_PAGE_COUNT) {
            uniqueSkill$notify("message.kagerov.book.page_limit");
            return;
        }

        this.uniqueSkill$syncCurrentPage();
        this.pages.add(this.currentPage + 1, "");
        this.currentPage++;
        this.updatePage();
        uniqueSkill$notify("message.kagerov.book.page_added");
    }

    @Unique
    private void uniqueSkill$removePage() {
        this.uniqueSkill$syncCurrentPage();
        if (this.pages.size() <= 1) {
            this.pages.set(0, "");
            this.currentPage = 0;
        } else {
            this.pages.remove(this.currentPage);
            if (this.currentPage >= this.pages.size()) {
                this.currentPage = this.pages.size() - 1;
            }
        }

        this.updatePage();
        uniqueSkill$notify("message.kagerov.book.page_removed");
    }

    @Unique
    private void uniqueSkill$copyBook() {
        this.uniqueSkill$syncCurrentPage();
        UniqueSkillBookClipboard.copyBook(new ArrayList<>(this.pages));
        if (this.client != null) {
            this.client.keyboard.setClipboard(String.join("\n\n", this.pages));
        }
        uniqueSkill$notify("message.kagerov.book.book_copied");
    }

    @Unique
    private void uniqueSkill$pasteBook() {
        this.pages.clear();
        this.pages.addAll(UniqueSkillBookClipboard.sanitizeBook(UniqueSkillBookClipboard.getCopiedBookPages()));
        this.currentPage = 0;
        this.updatePage();
        uniqueSkill$notify("message.kagerov.book.book_pasted");
    }

    @Unique
    private void uniqueSkill$clearBook() {
        this.pages.clear();
        this.pages.add("");
        this.currentPage = 0;
        this.updatePage();
        uniqueSkill$notify("message.kagerov.book.book_cleared");
    }

    @Unique
    private void uniqueSkill$pasteClipboard() {
        if (this.client == null) {
            return;
        }
        this.uniqueSkill$insertAtCursor(this.client.keyboard.getClipboard());
        uniqueSkill$notify("message.kagerov.book.clipboard_pasted");
    }

    @Unique
    private void uniqueSkill$insertPaletteToken(String token) {
        this.uniqueSkill$insertAtCursor(token);
    }

    @Unique
    private void uniqueSkill$stripCurrentPageFormatting() {
        this.uniqueSkill$syncCurrentPage();
        String cleaned = UNIQUE_SKILL_FORMATTING.matcher(this.editBox.getText()).replaceAll("");
        this.editBox.setText(cleaned);
    }

    @Unique
    private void uniqueSkill$openUnicodeSelector() {
        if (this.client != null) {
            UniqueSkillPaletteBridge.open((Screen) (Object) this, this::uniqueSkill$insertPaletteToken);
            this.client.setScreen(new UniqueSkillUnicodeSelectorScreen());
        }
    }

    @Unique
    private void uniqueSkill$openImportLibrary() {
        if (this.client != null) {
            this.uniqueSkill$syncCurrentPage();
            this.client.setScreen(new UniqueSkillBookLibraryScreen((Screen) (Object) this, book -> {
                this.pages.clear();
                this.pages.addAll(UniqueSkillBookClipboard.sanitizeBook(book.pages()));
                this.currentPage = 0;
                this.updatePage();
            }));
        }
    }

    @Unique
    private void uniqueSkill$openExportLibrary() {
        if (this.client != null) {
            this.uniqueSkill$syncCurrentPage();
            String suggestedTitle = this.stack.getName().getString();
            this.client.setScreen(new UniqueSkillBookLibraryScreen(
                (Screen) (Object) this,
                suggestedTitle == null || suggestedTitle.isBlank() ? "Livro sem titulo" : suggestedTitle,
                List.copyOf(this.pages),
                storedBook -> {
                }
            ));
        }
    }

    @Unique
    private void uniqueSkill$openAdvancedEditor() {
        if (this.client != null) {
            this.uniqueSkill$syncCurrentPage();
            this.client.setScreen(new UniqueSkillBookEditorScreen((Screen) (Object) this, new ArrayList<>(this.pages), this.currentPage, (updatedPages, selectedPage) -> {
                this.pages.clear();
                this.pages.addAll(UniqueSkillBookClipboard.sanitizeBook(updatedPages));
                this.currentPage = Math.max(0, Math.min(selectedPage, this.pages.size() - 1));
                this.updatePage();
            }));
        }
    }

    @Unique
    private void uniqueSkill$syncCurrentPage() {
        if (this.editBox != null && this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            this.pages.set(this.currentPage, UniqueSkillBookClipboard.trimToMaxLength(this.editBox.getText()));
        }
    }

    @Unique
    private void uniqueSkill$insertAtCursor(String token) {
        if (this.editBox == null || token == null || token.isEmpty()) {
            return;
        }

        EditBox innerEditBox = ((EditBoxWidgetAccessor) this.editBox).uniqueSkill$getEditBox();
        String currentText = innerEditBox.getText();

        if (token.indexOf('\u00A7') >= 0 && innerEditBox.hasSelection()) {
            int selectionStart = Math.min(
                ((EditBoxAccessor) innerEditBox).uniqueSkill$getCursorIndex(),
                ((EditBoxAccessor) innerEditBox).uniqueSkill$getSelectionEndIndex()
            );
            int selectionEnd = Math.max(
                ((EditBoxAccessor) innerEditBox).uniqueSkill$getCursorIndex(),
                ((EditBoxAccessor) innerEditBox).uniqueSkill$getSelectionEndIndex()
            );
            if (selectionStart >= 0 && selectionEnd >= selectionStart) {
                String restoreState = this.uniqueSkill$activeFormattingAt(currentText, selectionStart);
                String prefix = token.equals("\u00A7k") ? restoreState + token : token;
                String wrapped = currentText.substring(0, selectionStart)
                    + prefix
                    + currentText.substring(selectionStart, selectionEnd)
                    + "\u00A7r"
                    + restoreState
                    + currentText.substring(selectionEnd);
                if (!((EditBoxInvoker) innerEditBox).uniqueSkill$invokeExceedsMaxLines(wrapped)) {
                    this.editBox.setText(wrapped);
                    innerEditBox = ((EditBoxWidgetAccessor) this.editBox).uniqueSkill$getEditBox();
                    int restoredCursor = Math.min(wrapped.length(), selectionEnd + prefix.length());
                    ((EditBoxAccessor) innerEditBox).uniqueSkill$setCursorIndex(restoredCursor);
                    ((EditBoxAccessor) innerEditBox).uniqueSkill$setSelectionEndIndex(restoredCursor);
                    this.uniqueSkill$syncCurrentPage();
                }
                return;
            }
        }

        if (innerEditBox.hasSelection()) {
            innerEditBox.replaceSelection(token.equals("\u00A7k") ? this.uniqueSkill$activeFormattingAt(currentText, ((EditBoxAccessor) innerEditBox).uniqueSkill$getCursorIndex()) + token : token);
        } else {
            int cursor = innerEditBox.getCursor();
            String prefix = token.equals("\u00A7k") ? this.uniqueSkill$activeFormattingAt(currentText, cursor) + token : token;
            String replaced = currentText.substring(0, cursor) + prefix + currentText.substring(cursor);
            if (!((EditBoxInvoker) innerEditBox).uniqueSkill$invokeExceedsMaxLines(replaced)) {
                innerEditBox.replaceSelection(prefix);
            }
        }

        this.uniqueSkill$syncCurrentPage();
    }

    @Unique
    private String uniqueSkill$activeFormattingAt(String text, int index) {
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
                i++;
                continue;
            }

            if (code == 'r') {
                color = "";
                styles.setLength(0);
                i++;
                continue;
            }

            if ("klmno".indexOf(code) >= 0) {
                String styleToken = "\u00A7" + code;
                if (styles.indexOf(styleToken) < 0) {
                    styles.append(styleToken);
                }
                i++;
            }
        }

        return color + styles;
    }

    @Unique
    private void uniqueSkill$notify(String key) {
        this.player.sendMessage(Text.translatable(key), true);
    }

    @Inject(method = "writeNbtData", at = @At("TAIL"))
    private void uniqueSkill$reapplyCustomBookName(CallbackInfo ci) {
        if (this.uniqueSkill$preservedCustomName != null) {
            this.stack.set(DataComponentTypes.CUSTOM_NAME, this.uniqueSkill$preservedCustomName.copy());
        }
    }
}

