package com.draxx.charactersystem;

import com.draxx.charactersystem.hooks.CharacterPlaceholders;
import com.draxx.charactersystem.menu.CharacterMenu;
import com.draxx.charactersystem.model.CharacterProfile;
import com.draxx.charactersystem.model.PlayerCharacters;
import com.draxx.charactersystem.prompt.PromptManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CharacterSystemPlugin extends JavaPlugin {
    private CharacterStore store;
    private AttributeService attributeService;
    private NameService nameService;
    private MessageService messageService;
    private CharacterMenu menu;
    private PromptManager promptManager;
    private final Map<UUID, PlayerCharacters> cache = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messageService = new MessageService(this);
        store = new CharacterStore(this);
        attributeService = new AttributeService(this);
        nameService = new NameService(this);
        promptManager = new PromptManager(this);
        menu = new CharacterMenu(this);

        CharacterCommands commands = new CharacterCommands(this);
        command("personagem").setExecutor(commands);
        command("mpersonagem").setExecutor(commands);
        command("id").setExecutor(commands);
        command("pularano").setExecutor(commands);

        Bukkit.getPluginManager().registerEvents(new CharacterListener(this), this);
        Bukkit.getPluginManager().registerEvents(menu, this);
        Bukkit.getPluginManager().registerEvents(promptManager, this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CharacterPlaceholders(this).register();
            getLogger().info("PlaceholderAPI encontrado. Placeholders registrados.");
        }
    }

    @Override
    public void onDisable() {
        for (Map.Entry<UUID, PlayerCharacters> entry : cache.entrySet()) {
            store.save(entry.getKey(), entry.getValue());
        }
    }

    public PlayerCharacters characters(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), store::load);
    }

    public Optional<CharacterProfile> activeCharacter(Player player) {
        return characters(player).activeCharacter();
    }

    public void save(Player player) {
        PlayerCharacters playerCharacters = characters(player);
        store.save(player.getUniqueId(), playerCharacters);
    }

    public void reloadPlayer(Player player) {
        activeCharacter(player).ifPresentOrElse(character -> {
            nameService.apply(player, character);
            attributeService.apply(player, character);
        }, () -> {
            nameService.clear(player);
            attributeService.clear(player);
        });
    }

    public void reloadPlugin() {
        reloadConfig();
        messageService.reload();
        for (Player player : Bukkit.getOnlinePlayers()) {
            reloadPlayer(player);
        }
    }

    public void unload(Player player) {
        save(player);
        cache.remove(player.getUniqueId());
    }

    public void resetCharacters(UUID playerId) {
        cache.remove(playerId);
        store.delete(playerId);
    }

    public int heightMin() {
        return getConfig().getInt("height.min", 120);
    }

    public int heightMax() {
        return getConfig().getInt("height.max", 230);
    }

    public int heightDefault() {
        return getConfig().getInt("height.default", 170);
    }

    public CharacterStore store() {
        return store;
    }

    public AttributeService attributeService() {
        return attributeService;
    }

    public CharacterMenu menu() {
        return menu;
    }

    public PromptManager promptManager() {
        return promptManager;
    }

    public MessageService messages() {
        return messageService;
    }

    private PluginCommand command(String name) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            throw new IllegalStateException("Comando ausente no plugin.yml: " + name);
        }
        return command;
    }
}
