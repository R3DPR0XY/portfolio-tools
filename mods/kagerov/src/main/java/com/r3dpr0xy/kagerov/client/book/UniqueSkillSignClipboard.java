package com.r3dpr0xy.kagerov.client.book;

import java.util.Arrays;

public final class UniqueSkillSignClipboard {
    public static final int LINE_COUNT = 4;
    private static final String[] copiedLines = new String[] {"", "", "", ""};

    private UniqueSkillSignClipboard() {
    }

    public static void copy(String[] lines) {
        Arrays.fill(copiedLines, "");
        for (int index = 0; index < Math.min(LINE_COUNT, lines.length); index++) {
            copiedLines[index] = lines[index] == null ? "" : lines[index];
        }
    }

    public static String[] getCopiedLines() {
        return Arrays.copyOf(copiedLines, LINE_COUNT);
    }
}

