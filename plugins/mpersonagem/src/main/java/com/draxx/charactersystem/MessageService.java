package com.draxx.charactersystem;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class MessageService {
    private final CharacterSystemPlugin plugin;
    private YamlConfiguration messages;

    public MessageService(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> replacements) {
        String text = text(key, replacements);
        if (!text.isBlank()) {
            sender.sendMessage(text);
        }
    }

    public String text(String key) {
        return text(key, Map.of());
    }

    public String text(String key, Map<String, String> replacements) {
        String raw = messages.getString(key, "&cMensagem ausente: " + key);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return color(raw);
    }

    public List<String> list(String key, Map<String, String> replacements) {
        return messages.getStringList(key).stream()
                .map(line -> replace(line, replacements))
                .map(this::color)
                .toList();
    }

    private String replace(String text, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return text;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
