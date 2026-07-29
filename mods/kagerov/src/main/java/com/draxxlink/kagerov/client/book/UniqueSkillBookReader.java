package com.draxxlink.kagerov.client.book;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import com.draxxlink.kagerov.client.screen.UniqueSkillBookReaderScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class UniqueSkillBookReader {
    private static final int LORE_LINES_PER_PAGE = 10;
    private static final int CUSTOM_LINES_PER_PAGE = 9;

    private UniqueSkillBookReader() {
    }

    public static boolean openHeldBook(MinecraftClient client) {
        if (client == null || client.player == null || client.currentScreen != null) {
            return false;
        }

        for (Hand hand : Hand.values()) {
            ItemStack stack = client.player.getStackInHand(hand);
            if (!isBookLike(stack)) {
                continue;
            }

            List<Text> pages = extractPages(stack);
            if (pages.isEmpty()) {
                continue;
            }

            openPages(client, null, stack.getName(), pages, 0, describeSourceType(stack));
            return true;
        }

        return false;
    }

    public static boolean isBookLike(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        if (stack.isOf(Items.WRITTEN_BOOK)
            || stack.isOf(Items.WRITABLE_BOOK)
            || stack.isOf(Items.ENCHANTED_BOOK)
            || stack.isOf(Items.KNOWLEDGE_BOOK)
            || stack.isOf(Items.BOOK)) {
            return true;
        }

        return item.toString().toLowerCase().contains("book");
    }

    public static List<Text> extractPages(ItemStack stack) {
        WrittenBookContentComponent written = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (written != null) {
            List<Text> pages = new ArrayList<>(written.getPages(false));
            if (!pages.isEmpty()) {
                return pages;
            }
        }

        WritableBookContentComponent writable = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        if (writable != null) {
            List<Text> pages = writable.stream(false)
                .map(Text::literal)
                .map(Text.class::cast)
                .toList();
            if (!pages.isEmpty()) {
                return pages;
            }
        }

        List<Text> customPages = extractCustomDisplayPages(stack);
        if (!customPages.isEmpty()) {
            return customPages;
        }

        return extractFallbackPage(stack);
    }

    public static void openPages(MinecraftClient client, Screen parent, Text title, List<Text> pages, int currentPage) {
        openPages(client, parent, title, pages, currentPage, "manual");
    }

    public static void openPages(MinecraftClient client, Screen parent, Text title, List<Text> pages, int currentPage, String sourceType) {
        if (client == null || pages.isEmpty()) {
            return;
        }
        client.setScreen(new UniqueSkillBookReaderScreen(parent, title, pages, currentPage, sourceType));
    }

    private static List<Text> extractCustomDisplayPages(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        Text displayName = stack.getName();
        Text baseName = stack.getItem().getName().copy().formatted(Formatting.GRAY);
        boolean hasCustomName = stack.getCustomName() != null
            && !stack.getCustomName().getString().equals(displayName.getString());

        List<Text> visibleLines = new ArrayList<>();
        if (!displayName.getString().isBlank()) {
            visibleLines.add(displayName.copy());
        }
        if (hasCustomName) {
            visibleLines.add(stack.getCustomName().copy().formatted(Formatting.WHITE));
        }
        if (!baseName.getString().equals(displayName.getString())) {
            visibleLines.add(baseName);
        }
        if (lore != null) {
            visibleLines.addAll(lore.lines());
        }

        if (visibleLines.size() <= 1) {
            return List.of();
        }

        return packLinesIntoPages(visibleLines, CUSTOM_LINES_PER_PAGE);
    }

    private static List<Text> extractFallbackPage(ItemStack stack) {
        List<Text> pages = new ArrayList<>();
        MutableText page = stack.getName().copy();

        Text customName = stack.getCustomName();
        if (customName != null && !customName.getString().equals(stack.getName().getString())) {
            page.append(Text.literal("\n")).append(customName.copy());
        }

        if (!page.getString().isBlank()) {
            pages.add(page);
        }

        return pages;
    }

    private static String describeSourceType(ItemStack stack) {
        if (stack.isOf(Items.WRITTEN_BOOK)) {
            return "written_book";
        }
        if (stack.isOf(Items.WRITABLE_BOOK)) {
            return "writable_book";
        }
        if (stack.isOf(Items.ENCHANTED_BOOK) || stack.isOf(Items.BOOK) || stack.isOf(Items.KNOWLEDGE_BOOK)) {
            return "server_custom";
        }
        return "manual";
    }

    private static List<Text> packLinesIntoPages(List<Text> lines, int maxLinesPerPage) {
        List<Text> pages = new ArrayList<>();
        MutableText currentPage = Text.empty();
        int lineCount = 0;

        for (Text line : lines) {
            if (line == null || line.getString().isBlank()) {
                if (lineCount > 0) {
                    currentPage.append(Text.literal("\n"));
                    lineCount++;
                }
                continue;
            }

            if (lineCount >= maxLinesPerPage) {
                pages.add(currentPage);
                currentPage = Text.empty();
                lineCount = 0;
            }

            if (lineCount > 0) {
                currentPage.append(Text.literal("\n"));
            }
            currentPage.append(line.copy());
            lineCount++;
        }

        if (!currentPage.getString().isBlank()) {
            pages.add(currentPage);
        }
        return pages;
    }
}

