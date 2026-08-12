package com.r3dpr0xy.kagerov.client.book;

import net.minecraft.component.type.WritableBookContentComponent;

import java.util.ArrayList;
import java.util.List;

public final class UniqueSkillBookClipboard {
    private static String copiedPage = "";
    private static List<String> copiedBookPages = List.of("");

    private UniqueSkillBookClipboard() {
    }

    public static void copyPage(String pageText) {
        copiedPage = trimToMaxLength(pageText);
    }

    public static String getCopiedPage() {
        return copiedPage;
    }

    public static void copyBook(List<String> pages) {
        List<String> sanitized = new ArrayList<>();
        for (String page : pages) {
            if (sanitized.size() >= WritableBookContentComponent.MAX_PAGE_COUNT) {
                break;
            }
            sanitized.add(trimToMaxLength(page));
        }

        copiedBookPages = sanitized.isEmpty() ? List.of("") : List.copyOf(sanitized);
    }

    public static List<String> getCopiedBookPages() {
        return copiedBookPages;
    }

    public static List<String> sanitizeBook(List<String> pages) {
        List<String> sanitized = new ArrayList<>();
        for (String page : pages) {
            if (sanitized.size() >= WritableBookContentComponent.MAX_PAGE_COUNT) {
                break;
            }
            sanitized.add(trimToMaxLength(page));
        }
        return sanitized.isEmpty() ? List.of("") : sanitized;
    }

    public static String trimToMaxLength(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= WritableBookContentComponent.MAX_PAGE_LENGTH
            ? text
            : text.substring(0, WritableBookContentComponent.MAX_PAGE_LENGTH);
    }
}

