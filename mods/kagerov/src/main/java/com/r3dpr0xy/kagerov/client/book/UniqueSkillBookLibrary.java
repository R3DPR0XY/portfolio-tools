package com.r3dpr0xy.kagerov.client.book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class UniqueSkillBookLibrary {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);
    private static final String DEFAULT_SOURCE_TYPE = "manual";

    private UniqueSkillBookLibrary() {
    }

    public static Path getLibraryDirectory() throws IOException {
        Path directory = FabricLoader.getInstance().getConfigDir().resolve("kagerov").resolve("books");
        Files.createDirectories(directory);
        return directory;
    }

    public static List<StoredBook> listBooks() {
        try {
            Path directory = getLibraryDirectory();
            List<StoredBook> books = new ArrayList<>();
            try (var stream = Files.list(directory)) {
                stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .forEach(path -> {
                        try {
                            books.add(loadBook(path));
                        } catch (IOException ignored) {
                        }
                    });
            }
            return books;
        } catch (IOException exception) {
            return List.of();
        }
    }

    public static StoredBook saveBook(String title, List<String> pages) throws IOException {
        return saveBook(title, pages, null, SaveMode.NEW_COPY, DEFAULT_SOURCE_TYPE);
    }

    public static StoredBook saveBook(String title, List<String> pages, StoredBook existingBook, SaveMode saveMode, String sourceType) throws IOException {
        String safeTitle = title == null || title.isBlank() ? "Livro sem titulo" : title.trim();
        String baseName = slugify(safeTitle);
        if (baseName.isBlank()) {
            baseName = "book-" + FILE_STAMP.format(LocalDateTime.now());
        }

        Path directory = getLibraryDirectory();
        Path target = resolveSaveTarget(directory, baseName, existingBook, saveMode);
        StoredBook previous = existingBook != null && Files.exists(existingBook.path()) ? loadBook(existingBook.path()) : null;
        String now = FILE_STAMP.format(LocalDateTime.now());
        String createdAt = previous != null ? previous.createdAt() : now;

        JsonObject root = new JsonObject();
        root.addProperty("title", safeTitle);
        root.addProperty("sourceType", sourceType == null || sourceType.isBlank() ? DEFAULT_SOURCE_TYPE : sourceType);
        root.addProperty("createdAt", createdAt);
        root.addProperty("updatedAt", now);
        JsonArray pagesArray = new JsonArray();
        for (String page : pages) {
            pagesArray.add(page == null ? "" : page);
        }
        root.add("pages", pagesArray);

        try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }

        return loadBook(target);
    }

    public static StoredBook renameBook(StoredBook book, String newTitle) throws IOException {
        if (book == null) {
            throw new IOException("Book is required.");
        }

        String safeTitle = newTitle == null || newTitle.isBlank() ? book.title() : newTitle.trim();
        String baseName = slugify(safeTitle);
        if (baseName.isBlank()) {
            baseName = "book-" + FILE_STAMP.format(LocalDateTime.now());
        }

        Path directory = getLibraryDirectory();
        Path target = uniquePath(directory, baseName, book.path());
        Files.move(book.path(), target);
        return saveBook(safeTitle, book.pages(), new StoredBook(book.id(), safeTitle, book.pages(), target, book.sourceType(), book.createdAt(), book.updatedAt()), SaveMode.OVERWRITE_SELECTED, book.sourceType());
    }

    public static StoredBook duplicateBook(StoredBook book, String preferredTitle) throws IOException {
        if (book == null) {
            throw new IOException("Book is required.");
        }

        String duplicateTitle = preferredTitle == null || preferredTitle.isBlank()
            ? book.title() + " copia"
            : preferredTitle.trim();
        return saveBook(duplicateTitle, book.pages(), null, SaveMode.NEW_COPY, book.sourceType());
    }

    public static boolean deleteBook(StoredBook book) throws IOException {
        return book != null && Files.deleteIfExists(book.path());
    }

    public static StoredBook loadBook(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            String title = root != null && root.has("title") ? root.get("title").getAsString() : path.getFileName().toString();
            String sourceType = root != null && root.has("sourceType") ? root.get("sourceType").getAsString() : DEFAULT_SOURCE_TYPE;
            String createdAt = root != null && root.has("createdAt") ? root.get("createdAt").getAsString() : "";
            String updatedAt = root != null && root.has("updatedAt") ? root.get("updatedAt").getAsString() : createdAt;
            List<String> pages = new ArrayList<>();
            if (root != null && root.has("pages") && root.get("pages").isJsonArray()) {
                for (var element : root.getAsJsonArray("pages")) {
                    pages.add(element.getAsString());
                }
            }
            if (pages.isEmpty()) {
                pages.add("");
            }
            String id = stripExtension(path.getFileName().toString());
            return new StoredBook(id, title, List.copyOf(pages), path, sourceType, createdAt, updatedAt);
        }
    }

    private static Path resolveSaveTarget(Path directory, String baseName, StoredBook existingBook, SaveMode saveMode) {
        if (saveMode == SaveMode.OVERWRITE_SELECTED && existingBook != null) {
            return existingBook.path();
        }
        return uniquePath(directory, baseName, null);
    }

    private static Path uniquePath(Path directory, String baseName, Path keepPath) {
        Path target = directory.resolve(baseName + ".json");
        if (keepPath != null && target.equals(keepPath)) {
            return target;
        }
        if (!Files.exists(target)) {
            return target;
        }

        return directory.resolve(baseName + "-" + FILE_STAMP.format(LocalDateTime.now()) + ".json");
    }

    private static String slugify(String value) {
        return value.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+|-+$)", "");
    }

    private static String stripExtension(String name) {
        int index = name.lastIndexOf('.');
        return index >= 0 ? name.substring(0, index) : name;
    }

    public enum SaveMode {
        NEW_COPY,
        OVERWRITE_SELECTED
    }

    public record StoredBook(String id, String title, List<String> pages, Path path, String sourceType, String createdAt, String updatedAt) {
    }
}

