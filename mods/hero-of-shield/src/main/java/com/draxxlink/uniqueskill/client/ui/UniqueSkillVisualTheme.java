package com.draxxlink.uniqueskill.client.ui;

import net.minecraft.client.gui.DrawContext;

public final class UniqueSkillVisualTheme {
    public static final int PANEL_BORDER = 0xD99A2028;
    public static final int PANEL_INNER_BORDER = 0x88510A11;
    public static final int PANEL_TOP = 0xF00D0104;
    public static final int PANEL_BOTTOM = 0xF0040001;
    public static final int PANEL_ACCENT = 0x66B31223;
    public static final int PANEL_ACCENT_SOFT = 0x3330080D;
    public static final int PANEL_CORNER = 0xFFFF5566;
    public static final int PANEL_TEXT = 0xFFFDECEF;
    public static final int PANEL_MUTED_TEXT = 0xFFDA9AA4;
    public static final int PANEL_HOT_TEXT = 0xFFFF6F83;
    public static final int PANEL_ALERT_TEXT = 0xFFFFA1AE;
    public static final int PANEL_SOFT_TEXT = 0xFFF0B7BF;
    public static final int PANEL_GOLD = 0xFFFF5A6C;
    public static final int PANEL_GOLD_SOFT = 0xFFC02A39;
    public static final int PANEL_RED = 0xFFFF3047;
    public static final int PANEL_RED_SOFT = 0xFFB0152B;
    public static final int PANEL_BLACK_VEIL = 0x7A040001;
    public static final int BACKDROP_TOP = 0xFF0B0002;
    public static final int BACKDROP_BOTTOM = 0xFF010001;

    private UniqueSkillVisualTheme() {
    }

    public static void drawArcaneBackdrop(DrawContext context, int width, int height) {
        context.fillGradient(0, 0, width, height, BACKDROP_TOP, BACKDROP_BOTTOM);

        int centerX = width / 2;
        int centerY = height / 2;
        int outerWidth = Math.min(220, width / 3);
        int outerHeight = Math.min(220, height / 3);
        int innerWidth = Math.max(outerWidth - 36, 28);
        int innerHeight = Math.max(outerHeight - 36, 28);

        drawDiamondFrame(context, centerX, centerY, outerWidth, outerHeight, 0x22341519);
        drawDiamondFrame(context, centerX, centerY, innerWidth, innerHeight, 0x33A93535);
        context.fill(centerX - 70, centerY, centerX + 70, centerY + 1, 0x22D65E57);
        context.fill(centerX, centerY - 70, centerX + 1, centerY + 70, 0x22D65E57);
        drawSigil(context, centerX, centerY, 9, PANEL_RED);
    }

    public static void drawArcanePanel(DrawContext context, int x, int y, int width, int height) {
        context.fillGradient(x, y, x + width, y + height, PANEL_TOP, PANEL_BOTTOM);
        context.drawBorder(x, y, width, height, PANEL_BORDER);
        if (width > 4 && height > 4) {
            context.drawBorder(x + 1, y + 1, width - 2, height - 2, PANEL_INNER_BORDER);
        }
        if (width > 28 && height > 14) {
            context.fill(x + 9, y + 6, x + width - 9, y + 7, PANEL_ACCENT);
            context.fill(x + 9, y + height - 7, x + width - 9, y + height - 6, PANEL_ACCENT_SOFT);
        }

        drawPanelCorners(context, x, y, width, height);
        drawPanelSigils(context, x, y, width, height);
    }

    public static void drawSectionBox(DrawContext context, int x, int y, int width, int height, int accentColor) {
        context.fillGradient(x, y, x + width, y + height, 0xD521090B, 0xD00A0204);
        context.drawBorder(x, y, width, height, 0x99A04848);
        context.fill(x + 1, y + 1, x + width - 1, y + 2, accentColor);
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0x44461718);
        drawSideRunes(context, x, y, width, height, accentColor);
    }

    public static void drawIndicatorBox(DrawContext context, int x, int y, int width, int height) {
        context.fillGradient(x, y, x + width, y + height, 0xE0250A0D, 0xE0090204);
        context.drawBorder(x, y, width, height, 0xB9C25B59);
        drawIndicatorCorners(context, x, y, width, height);
    }

    public static void drawSigil(DrawContext context, int centerX, int centerY, int radius, int color) {
        context.fill(centerX, centerY - radius, centerX + 1, centerY + radius + 1, color);
        context.fill(centerX - radius, centerY, centerX + radius + 1, centerY + 1, color);
        drawDiamondFrame(context, centerX, centerY, radius, radius, color);
    }

    private static void drawPanelSigils(DrawContext context, int x, int y, int width, int height) {
        int centerX = x + (width / 2);
        if (width > 48) {
            drawSigil(context, centerX, y + 7, 3, PANEL_RED);
            drawSigil(context, centerX, y + height - 8, 3, PANEL_RED_SOFT);
        }
    }

    private static void drawPanelCorners(DrawContext context, int x, int y, int width, int height) {
        if (width < 10 || height < 10) {
            return;
        }

        context.fill(x + 2, y + 2, x + 6, y + 3, PANEL_CORNER);
        context.fill(x + 2, y + 2, x + 3, y + 6, PANEL_CORNER);
        context.fill(x + width - 6, y + 2, x + width - 2, y + 3, PANEL_CORNER);
        context.fill(x + width - 3, y + 2, x + width - 2, y + 6, PANEL_CORNER);
        context.fill(x + 2, y + height - 3, x + 6, y + height - 2, PANEL_CORNER);
        context.fill(x + 2, y + height - 6, x + 3, y + height - 2, PANEL_CORNER);
        context.fill(x + width - 6, y + height - 3, x + width - 2, y + height - 2, PANEL_CORNER);
        context.fill(x + width - 3, y + height - 6, x + width - 2, y + height - 2, PANEL_CORNER);
    }

    private static void drawIndicatorCorners(DrawContext context, int x, int y, int width, int height) {
        context.fill(x + 1, y + 1, x + 4, y + 2, PANEL_RED);
        context.fill(x + 1, y + height - 2, x + 4, y + height - 1, PANEL_RED);
        context.fill(x + width - 4, y + 1, x + width - 1, y + 2, PANEL_RED);
        context.fill(x + width - 4, y + height - 2, x + width - 1, y + height - 1, PANEL_RED);
    }

    private static void drawSideRunes(DrawContext context, int x, int y, int width, int height, int accentColor) {
        if (height < 20) {
            return;
        }

        int centerY = y + (height / 2);
        context.fill(x + 3, centerY - 5, x + 4, centerY + 6, 0x55D46A68);
        context.fill(x + width - 4, centerY - 5, x + width - 3, centerY + 6, 0x55D46A68);
        context.fill(x + 3, centerY, x + 8, centerY + 1, accentColor);
        context.fill(x + width - 8, centerY, x + width - 3, centerY + 1, accentColor);
    }

    private static void drawDiamondFrame(DrawContext context, int centerX, int centerY, int radiusX, int radiusY, int color) {
        for (int step = 0; step <= radiusX; step++) {
            int offsetY = (int) Math.round((1.0D - (step / (double) Math.max(radiusX, 1))) * radiusY);
            context.fill(centerX - step, centerY - offsetY, centerX - step + 1, centerY - offsetY + 1, color);
            context.fill(centerX + step, centerY - offsetY, centerX + step + 1, centerY - offsetY + 1, color);
            context.fill(centerX - step, centerY + offsetY, centerX - step + 1, centerY + offsetY + 1, color);
            context.fill(centerX + step, centerY + offsetY, centerX + step + 1, centerY + offsetY + 1, color);
        }
    }
}
