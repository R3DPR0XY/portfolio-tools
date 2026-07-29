package com.daich.betterchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ContactBook {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final Map<String, Contact> contacts = new LinkedHashMap<>();
    private final List<ChatRecord> history = new ArrayList<>();
    private final Map<String, Integer> unreadCounts = new LinkedHashMap<>();
    private String focusedContact;
    private String lastContact;
    private String privateMessageCommand = "msg";
    private boolean playSounds = true;
    private boolean closeAfterSend;
    private boolean keepDraft = true;
    private boolean showChatMenuHud = false;
    private String chatMenuHudPosition = "right";

    public ContactBook() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.path = configDir.resolve("betterchat-contacts.json");
    }

    public void load() {
        contacts.clear();
        history.clear();
        unreadCounts.clear();
        if (!Files.exists(path)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            SaveData data = GSON.fromJson(reader, SaveData.class);
            if (data == null || data.contacts == null) {
                return;
            }
            focusedContact = data.focusedContact;
            lastContact = data.lastContact;
            if (data.privateMessageCommand != null && !data.privateMessageCommand.isBlank()) {
                privateMessageCommand = sanitizeCommand(data.privateMessageCommand);
            }
            playSounds = data.playSounds;
            closeAfterSend = data.closeAfterSend;
            keepDraft = data.keepDraft;
            showChatMenuHud = false;
            if (data.chatMenuHudPosition != null && !data.chatMenuHudPosition.isBlank()) {
                chatMenuHudPosition = normalizeHudPosition(data.chatMenuHudPosition);
            }
            for (Contact contact : data.contacts) {
                if (contact.name != null && !contact.name.isBlank()) {
                    contacts.put(key(contact.name), contact);
                }
            }
            if (data.history != null) {
                history.addAll(data.history);
            }
            if (data.unreadCounts != null) {
                unreadCounts.putAll(data.unreadCounts);
            }
        } catch (IOException e) {
            BetterchatClient.LOGGER.warn("Não foi possível carregar os contatos do Betterchat", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            SaveData data = new SaveData();
            data.focusedContact = focusedContact;
            data.lastContact = lastContact;
            data.privateMessageCommand = privateMessageCommand;
            data.playSounds = playSounds;
            data.closeAfterSend = closeAfterSend;
            data.keepDraft = keepDraft;
            data.showChatMenuHud = false;
            data.chatMenuHudPosition = chatMenuHudPosition;
            data.contacts = sortedContacts();
            data.history = history.stream()
                    .skip(Math.max(0, history.size() - 250))
                    .toList();
            data.unreadCounts = unreadCounts;
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            BetterchatClient.LOGGER.warn("Não foi possível salvar os contatos do Betterchat", e);
        }
    }

    public Contact add(String name, String alias) {
        Contact contact = new Contact(name, alias == null || alias.isBlank() ? name : alias);
        contacts.put(key(name), contact);
        save();
        return contact;
    }

    public boolean remove(String nameOrAlias) {
        Optional<Contact> contact = find(nameOrAlias);
        if (contact.isEmpty()) {
            return false;
        }
        contacts.remove(key(contact.get().name));
        unreadCounts.remove(key(contact.get().name));
        history.removeIf(record -> record.contactName != null && record.contactName.equalsIgnoreCase(contact.get().name));
        if (contact.get().name.equalsIgnoreCase(focusedContact)) {
            focusedContact = null;
        }
        if (contact.get().name.equalsIgnoreCase(lastContact)) {
            lastContact = null;
        }
        save();
        return true;
    }

    public Optional<Contact> find(String nameOrAlias) {
        Contact byName = contacts.get(key(nameOrAlias));
        if (byName != null) {
            return Optional.of(byName);
        }
        return contacts.values().stream()
                .filter(contact -> contact.alias != null && contact.alias.equalsIgnoreCase(nameOrAlias))
                .findFirst();
    }

    public List<Contact> sortedContacts() {
        return contacts.values().stream()
                .sorted(Comparator.comparing(contact -> contact.alias == null ? contact.name : contact.alias, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<Contact> focusedContact() {
        if (focusedContact == null || focusedContact.isBlank()) {
            return Optional.empty();
        }
        return find(focusedContact);
    }

    public boolean isFocused(Contact contact) {
        return contact != null && focusedContact != null && contact.name.equalsIgnoreCase(focusedContact);
    }

    public void focus(Contact contact) {
        focusedContact = contact.name;
        lastContact = contact.name;
        unreadCounts.remove(key(contact.name));
        save();
    }

    public void markConversation(Contact contact) {
        lastContact = contact.name;
        save();
    }

    public void recordOutgoing(Contact contact, String message) {
        if (contact == null || message == null || message.isBlank()) {
            return;
        }
        history.add(new ChatRecord(contact.name, message.trim(), true));
        trimHistory();
        save();
    }

    public void recordIncoming(Contact contact, String message) {
        if (contact == null || message == null || message.isBlank()) {
            return;
        }
        history.add(new ChatRecord(contact.name, message.trim(), false));
        if (!isFocused(contact)) {
            String key = key(contact.name);
            unreadCounts.put(key, unreadCounts.getOrDefault(key, 0) + 1);
        }
        trimHistory();
        lastContact = contact.name;
        save();
    }

    public int unreadCount(Contact contact) {
        if (contact == null) {
            return 0;
        }
        return unreadCounts.getOrDefault(key(contact.name), 0);
    }

    public List<ChatRecord> historyFor(Contact contact, int limit) {
        if (contact == null) {
            return List.of();
        }
        List<ChatRecord> records = history.stream()
                .filter(record -> record.contactName != null && record.contactName.equalsIgnoreCase(contact.name))
                .toList();
        return records.subList(Math.max(0, records.size() - limit), records.size());
    }

    public Optional<Contact> lastContact() {
        if (lastContact == null || lastContact.isBlank()) {
            return Optional.empty();
        }
        return find(lastContact);
    }

    public List<String> contactSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (Contact contact : sortedContacts()) {
            suggestions.add(contact.name);
            if (contact.alias != null && !contact.alias.isBlank() && !contact.alias.equalsIgnoreCase(contact.name)) {
                suggestions.add(contact.alias);
            }
        }
        return suggestions;
    }

    public String privateMessageCommand() {
        return privateMessageCommand;
    }

    public void setPrivateMessageCommand(String command) {
        privateMessageCommand = sanitizeCommand(command);
        save();
    }

    public boolean playSounds() {
        return playSounds;
    }

    public void setPlaySounds(boolean playSounds) {
        this.playSounds = playSounds;
        save();
    }

    public boolean closeAfterSend() {
        return closeAfterSend;
    }

    public void setCloseAfterSend(boolean closeAfterSend) {
        this.closeAfterSend = closeAfterSend;
        save();
    }

    public boolean keepDraft() {
        return keepDraft;
    }

    public void setKeepDraft(boolean keepDraft) {
        this.keepDraft = keepDraft;
        save();
    }

    public boolean showChatMenuHud() {
        return showChatMenuHud;
    }

    public void setShowChatMenuHud(boolean showChatMenuHud) {
        this.showChatMenuHud = false;
        save();
    }

    public String chatMenuHudPosition() {
        return chatMenuHudPosition;
    }

    public void setChatMenuHudPosition(String position) {
        String normalized = normalizeHudPosition(position);
        if ("left".equals(normalized) || "right".equals(normalized) || "off".equals(normalized)) {
            chatMenuHudPosition = normalized;
        }
        showChatMenuHud = false;
        save();
    }

    private void trimHistory() {
        if (history.size() > 250) {
            history.subList(0, history.size() - 250).clear();
        }
    }

    private static String key(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private static String sanitizeCommand(String command) {
        String value = command == null ? "" : command.trim().replaceFirst("^/+", "");
        return value.isBlank() ? "msg" : value;
    }

    private static String normalizeHudPosition(String position) {
        String value = position == null ? "" : position.toLowerCase(Locale.ROOT);
        return switch (value) {
            case "direita", "right", "dir" -> "right";
            case "esquerda", "left", "esq" -> "left";
            case "desligado", "off", "ocultar", "hide" -> "off";
            default -> value;
        };
    }

    private static class SaveData {
        List<Contact> contacts = new ArrayList<>();
        List<ChatRecord> history = new ArrayList<>();
        Map<String, Integer> unreadCounts = new LinkedHashMap<>();
        String focusedContact;
        String lastContact;
        String privateMessageCommand;
        boolean playSounds = true;
        boolean closeAfterSend;
        boolean keepDraft = true;
        boolean showChatMenuHud = false;
        String chatMenuHudPosition = "off";
    }
}
