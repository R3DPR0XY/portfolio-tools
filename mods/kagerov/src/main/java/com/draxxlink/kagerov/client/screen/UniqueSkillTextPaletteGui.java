package com.draxxlink.kagerov.client.screen;

import com.draxxlink.kagerov.client.book.UniqueSkillPaletteBridge;
import com.draxxlink.kagerov.client.book.UniqueSkillTextTokens;
import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class UniqueSkillTextPaletteGui extends LightweightGuiDescription {
    public UniqueSkillTextPaletteGui() {
        WPlainPanel root = new WPlainPanel();
        root.setInsets(new Insets(7, 7, 7, 7));
        root.setSize(320, 220);
        this.setRootPanel((WPanel) root);

        root.add((WWidget) new WLabel(Text.translatable("screen.kagerov.palette.title")), 0, 0, 180, 20);
        root.add((WWidget) new WLabel(Text.translatable("screen.kagerov.palette.subtitle")), 0, 12, 250, 20);

        WButton unicodeButton = new WButton(Text.translatable("screen.kagerov.unicode.button"));
        unicodeButton.setOnClick(() -> MinecraftClient.getInstance().setScreen(new UniqueSkillUnicodeSelectorScreen()));
        root.add((WWidget) unicodeButton, 208, 0, 52, 20);

        WButton closeButton = new WButton(Text.translatable("screen.kagerov.palette.back"));
        closeButton.setOnClick(UniqueSkillPaletteBridge::closeToPrevious);
        root.add((WWidget) closeButton, 262, 0, 50, 20);

        WGridPanel grid = new WGridPanel(18);
        WScrollPanel scrollPanel = new WScrollPanel((WWidget) grid);
        root.add((WWidget) scrollPanel, 0, 28, 320, 192);
        scrollPanel.addPainters();

        int row = 0;
        row = addSection(grid, row, Text.translatable("screen.kagerov.book.sidebar_styles"), UniqueSkillTextTokens.STYLE_TOKENS, UniqueSkillTextTokens.STYLE_LABELS);
        row = addColorSection(grid, row + 1);
        row = addSection(grid, row + 1, Text.translatable("screen.kagerov.book.sidebar_lines"), UniqueSkillTextTokens.LINE_TOKENS, UniqueSkillTextTokens.LINE_TOKENS);
        addSection(grid, row + 2, Text.translatable("screen.kagerov.book.sidebar_symbols"), UniqueSkillTextTokens.SYMBOL_TOKENS, UniqueSkillTextTokens.SYMBOL_TOKENS);

        root.validate((GuiDescription) this);
    }

    private int addSection(WGridPanel grid, int startRow, Text title, String[] tokens, String[] labels) {
        grid.add((WWidget) new WLabel(title), 0, startRow, 10, 1);
        int row = startRow + 1;
        int column = 0;
        for (int index = 0; index < tokens.length; index++) {
            final String token = tokens[index];
            String label = labels[index];
            WButton button = new WButton(Text.literal(label));
            button.setOnClick(() -> UniqueSkillPaletteBridge.insertAndReturn(token));
            grid.add((WWidget) button, column, row, 1, 1);
            column++;
            if (column > 7) {
                column = 0;
                row++;
            }
        }
        return row + (column == 0 ? 0 : 1);
    }

    private int addColorSection(WGridPanel grid, int startRow) {
        grid.add((WWidget) new WLabel(Text.translatable("screen.kagerov.book.sidebar_colors")), 0, startRow, 10, 1);
        int row = startRow + 1;
        int column = 0;
        for (int index = 0; index < UniqueSkillTextTokens.COLOR_VALUES.length; index++) {
            final String token = UniqueSkillTextTokens.colorToken(index);
            WButton button = new WButton(Text.literal("\u25A0"));
            button.setOnClick(() -> UniqueSkillPaletteBridge.insertAndReturn(token));
            grid.add((WWidget) button, column, row, 1, 1);
            column++;
            if (column > 7) {
                column = 0;
                row++;
            }
        }
        return row + (column == 0 ? 0 : 1);
    }
}

