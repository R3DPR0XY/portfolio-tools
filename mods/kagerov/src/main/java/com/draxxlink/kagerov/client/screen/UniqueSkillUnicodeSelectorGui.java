package com.draxxlink.kagerov.client.screen;

import com.draxxlink.kagerov.client.book.UniqueSkillPaletteBridge;
import com.draxxlink.kagerov.client.book.UniqueSkillUnicodeTokens;
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
import net.minecraft.text.Text;

public class UniqueSkillUnicodeSelectorGui extends LightweightGuiDescription {
    public UniqueSkillUnicodeSelectorGui() {
        WPlainPanel root = new WPlainPanel();
        root.setInsets(new Insets(7, 7, 7, 7));
        root.setSize(320, 220);
        this.setRootPanel((WPanel) root);

        root.add((WWidget) new WLabel(Text.translatable("screen.kagerov.unicode.title")), 0, 0, 180, 20);
        root.add((WWidget) new WLabel(Text.translatable("screen.kagerov.unicode.subtitle")), 0, 12, 220, 20);

        WButton close = new WButton(Text.translatable("screen.kagerov.palette.back"));
        close.setOnClick(UniqueSkillPaletteBridge::closeToPrevious);
        root.add((WWidget) close, 250, 0, 60, 20);

        WGridPanel grid = new WGridPanel(20);
        WScrollPanel scrollPanel = new WScrollPanel((WWidget) grid);
        root.add((WWidget) scrollPanel, 0, 28, 320, 192);
        scrollPanel.addPainters();

        int gridX = 0;
        int gridY = 0;
        for (String symbol : UniqueSkillUnicodeTokens.SAFE_TOKENS) {
            WButton button = new WButton(Text.literal(symbol));
            button.setOnClick(() -> UniqueSkillPaletteBridge.insertAndReturn(symbol));
            grid.add((WWidget) button, gridX, gridY, 1, 1);
            if (++gridX > 13) {
                gridX = 0;
                gridY++;
            }
        }

        root.validate((GuiDescription) this);
    }
}

