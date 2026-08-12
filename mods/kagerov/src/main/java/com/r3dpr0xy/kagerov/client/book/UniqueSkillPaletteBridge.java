package com.r3dpr0xy.kagerov.client.book;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public final class UniqueSkillPaletteBridge {
    private static Screen previousScreen;
    private static Inserter inserter;

    private UniqueSkillPaletteBridge() {
    }

    public static void open(Screen previous, Inserter callback) {
        previousScreen = previous;
        inserter = callback;
    }

    public static void insertAndReturn(String token) {
        if (inserter != null) {
            inserter.insert(token);
        }
        Screen screen = previousScreen;
        previousScreen = null;
        inserter = null;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.setScreen(screen);
        }
    }

    public static void closeToPrevious() {
        Screen screen = previousScreen;
        previousScreen = null;
        inserter = null;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.setScreen(screen);
        }
    }

    @FunctionalInterface
    public interface Inserter {
        void insert(String token);
    }
}

