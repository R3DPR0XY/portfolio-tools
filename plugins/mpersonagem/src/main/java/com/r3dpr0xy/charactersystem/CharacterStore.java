package com.r3dpr0xy.charactersystem;

import com.r3dpr0xy.charactersystem.model.CharacterProfile;
import com.r3dpr0xy.charactersystem.model.PlayerCharacters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class CharacterStore {
    private final CharacterSystemPlugin plugin;
    private final File dataFolder;

    public CharacterStore(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "players");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Nao foi possivel criar a pasta de dados dos jogadores.");
        }
    }

    public PlayerCharacters load(UUID playerId) {
        File file = file(playerId);
        PlayerCharacters playerCharacters = new PlayerCharacters();
        if (!file.exists()) {
            return playerCharacters;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String active = yaml.getString("active");
        if (active != null && !active.isBlank()) {
            try {
                playerCharacters.activeCharacterId(UUID.fromString(active));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Personagem ativo invalido em " + file.getName());
            }
        }

        ConfigurationSection section = yaml.getConfigurationSection("characters");
        if (section == null) {
            return playerCharacters;
        }

        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                String path = "characters." + key + ".";
                String name = yaml.getString(path + "name", "Sem Nome");
                String surname = yaml.getString(path + "surname", "");
                String talent = yaml.getString(path + "talent", "Sobrevivente");
                int age = yaml.getInt(path + "age", 18);
                int height = yaml.getInt(path + "height", plugin.heightDefault());
                String gender = yaml.getString(path + "gender", "");
                String description = yaml.getString(path + "description", "");
                long createdAt = yaml.getLong(path + "created-at", System.currentTimeMillis());
                playerCharacters.characters().add(new CharacterProfile(id, name, surname, talent, age, height, gender, description, createdAt));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Personagem invalido ignorado em " + file.getName() + ": " + key);
            }
        }
        return playerCharacters;
    }

    public void save(UUID playerId, PlayerCharacters playerCharacters) {
        File file = file(playerId);
        YamlConfiguration yaml = new YamlConfiguration();
        if (playerCharacters.activeCharacterId() != null) {
            yaml.set("active", playerCharacters.activeCharacterId().toString());
        }
        for (CharacterProfile character : playerCharacters.characters()) {
            String path = "characters." + character.id() + ".";
            yaml.set(path + "name", character.name());
            yaml.set(path + "surname", character.surname());
            yaml.set(path + "talent", character.talent());
            yaml.set(path + "age", character.age());
            yaml.set(path + "height", character.height());
            yaml.set(path + "gender", character.gender());
            yaml.set(path + "description", character.description());
            yaml.set(path + "created-at", character.createdAt());
        }

        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Nao foi possivel salvar personagens de " + playerId, ex);
        }
    }

    public List<File> playerFiles() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        return files == null ? List.of() : List.of(files);
    }

    public UUID playerIdFromFile(File file) {
        String name = file.getName().replace(".yml", "");
        return UUID.fromString(name);
    }

    public void delete(UUID playerId) {
        File file = file(playerId);
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("Nao foi possivel apagar o arquivo " + file.getName());
        }
    }

    private File file(UUID playerId) {
        return new File(dataFolder, playerId + ".yml");
    }
}
