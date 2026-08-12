package com.r3dpr0xy.brewbloom;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class BrewBloomStyleScreen extends Screen {
    private static final String[] STYLES = {
        "orbit", "aura", "halo", "trail", "spiral",
        "crown", "pulse", "fountain", "ring", "vortex",
        "comet", "spark", "wave", "double_ring", "rain"
    };
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 236;
    private static final int RED = 0xFFE0182D;
    private static final int WHITE = 0xFFF5F5F5;
    private static final int MUTED = 0xFF9C9CA3;

    private final BrewBloomConfig config;
    private final Screen parent;
    private int panelX;
    private int panelY;

    public BrewBloomStyleScreen(BrewBloomConfig config, Screen parent) {
        super(Text.translatable("screen.brewbloom.style_picker"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - PANEL_HEIGHT) / 2;

        for (int index = 0; index < STYLES.length; index++) {
            String style = STYLES[index];
            int column = index % 3;
            int row = index / 3;
            addDrawableChild(new StyleButton(
                panelX + 24 + column * 124,
                panelY + 50 + row * 30,
                112,
                22,
                Text.translatable("screen.brewbloom.style." + style),
                button -> {
                    config.setParticleStyle(style);
                    close();
                },
                style
            ));
        }

        addDrawableChild(new StyleButton(panelX + PANEL_WIDTH - 96, panelY + PANEL_HEIGHT - 32, 72, 20, Text.translatable("screen.brewbloom.back"), button -> close(), ""));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB8000000);
        context.fill(panelX - 5, panelY - 5, panelX + PANEL_WIDTH + 5, panelY + PANEL_HEIGHT + 5, 0x99000000);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xF0060609);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 4, RED);
        context.fill(panelX + 1, panelY + 1, panelX + PANEL_WIDTH - 1, panelY + PANEL_HEIGHT - 1, 0x44FFFFFF);
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.style_picker"), panelX + 24, panelY + 18, WHITE);
        context.drawTextWithShadow(textRenderer, Text.translatable("screen.brewbloom.style_picker_help"), panelX + 24, panelY + 31, MUTED);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
            return;
        }

        super.close();
    }

    private final class StyleButton extends ButtonWidget {
        private final String style;

        private StyleButton(int x, int y, int width, int height, Text message, PressAction pressAction, String style) {
            super(x, y, width, height, message, pressAction, DEFAULT_NARRATION_SUPPLIER);
            this.style = style;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean selected = !style.isEmpty() && style.equals(config.particleStyle);
            int x = getX();
            int y = getY();
            int right = x + getWidth();
            int bottom = y + getHeight();
            int border = selected ? RED : isHovered() ? 0xFFAAAAAA : 0xFF55555A;
            int fill = selected ? 0xFF340008 : isHovered() ? 0xFF1B1B20 : 0xFF050507;

            context.fill(x, y, right, bottom, border);
            context.fill(x + 1, y + 1, right - 1, bottom - 1, fill);
            context.fill(x + 2, y + 2, right - 2, y + 4, selected ? RED : 0xFF2C2C32);
            context.drawCenteredTextWithShadow(textRenderer, getMessage(), x + getWidth() / 2, y + (getHeight() - 8) / 2, selected || isHovered() ? WHITE : 0xFFE6E6EA);
        }
    }
}
