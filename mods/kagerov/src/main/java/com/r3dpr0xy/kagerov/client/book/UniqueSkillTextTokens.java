package com.r3dpr0xy.kagerov.client.book;

public final class UniqueSkillTextTokens {
    public static final String[] STYLE_TOKENS = new String[] {
        "\u00A7l", "\u00A7o", "\u00A7n", "\u00A7m", "\u00A7k", "\u00A7r"
    };

    public static final String[] STYLE_LABELS = new String[] {"B", "I", "U", "S", "M", "R"};

    public static final int[] COLOR_VALUES = new int[] {
        0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
        0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
        0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
        0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    public static final String[] SYMBOL_TOKENS = new String[] {
        "\u2764", "\u279c", "\u2605", "\u2620", "\u26a0", "\u2600",
        "\u263a", "\u2639", "\u2709", "\u2602", "\u2718", "\u266a",
        "\u266c", "\u2669", "\u266b", "\u2604", "\u2740", "\u273f",
        "\u2726", "\u23cf", "\u23e9", "\u23ea", "\u23ed", "\u23ee",
        "\u23ef", "\u2693", "\u26e8", "\u26cf", "\u2714", "\u2048",
        "\u2049", "\u203c", "\u26a5", "\u2640", "\u2642", "\u2660",
        "\u2663", "\u2666", "\u2680", "\u2681", "\u2682", "\u2683"
    };

    public static final String[] LINE_TOKENS = new String[] {
        "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
        "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550",
        "\u2501\u2501\u2501\u2501\u2501\u2501\u2501",
        "\u2022 \u2022 \u2022 \u2022 \u2022",
        "\u2605\u2500\u2500\u2500\u2500\u2605",
        "\u2726\u2550\u2550\u2550\u2550\u2726"
    };

    private UniqueSkillTextTokens() {
    }

    public static String colorToken(int index) {
        return "\u00A7" + Integer.toHexString(index);
    }
}

