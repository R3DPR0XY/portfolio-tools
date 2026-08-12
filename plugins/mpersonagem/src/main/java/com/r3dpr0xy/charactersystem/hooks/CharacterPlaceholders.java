package com.r3dpr0xy.charactersystem.hooks;

import com.r3dpr0xy.charactersystem.CharacterSystemPlugin;
import com.r3dpr0xy.charactersystem.model.CharacterProfile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CharacterPlaceholders extends PlaceholderExpansion {
    private final CharacterSystemPlugin plugin;

    public CharacterPlaceholders(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mpersonagem";
    }

    @Override
    public @NotNull String getAuthor() {
        return "R3DPR0XY";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (!(offlinePlayer instanceof Player player)) {
            return "";
        }
        CharacterProfile character = plugin.activeCharacter(player).orElse(null);
        String empty = plugin.getConfig().getString("settings.placeholder-no-character", "Sem personagem");
        if (character == null) {
            if (params.equalsIgnoreCase("tem_personagem") || params.equalsIgnoreCase("has_character")) {
                return "nao";
            }
            return empty;
        }
        return switch (params.toLowerCase()) {
            case "nome", "name" -> character.name();
            case "sobrenome", "surname" -> character.surname();
            case "nome_completo", "full_name" -> character.fullName();
            case "talento", "talent" -> character.talent();
            case "idade", "age" -> Integer.toString(character.age());
            case "altura", "height" -> Integer.toString(character.height());
            case "altura_formatada", "height_formatted" -> character.height() + " cm";
            case "altura_metros", "height_meters" -> String.format("%.2f", character.height() / 100.0);
            case "genero", "gender" -> character.gender();
            case "descricao", "description" -> character.description();
            case "tem_personagem", "has_character" -> "sim";
            default -> null;
        };
    }
}
