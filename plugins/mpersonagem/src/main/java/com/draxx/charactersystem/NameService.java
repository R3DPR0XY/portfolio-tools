package com.draxx.charactersystem;

import com.draxx.charactersystem.model.CharacterProfile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class NameService {
    private final CharacterSystemPlugin plugin;

    public NameService(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
    }

    public void apply(Player player, CharacterProfile character) {
        String formatted = plugin.getConfig().getString("settings.display-name-format", "%nome%")
                .replace("%nome%", character.name())
                .replace("%sobrenome%", character.surname() == null ? "" : character.surname())
                .replace("%nome_completo%", character.fullName())
                .replace("%idade%", Integer.toString(character.age()))
                .replace("%altura%", Integer.toString(character.height()));
        formatted = ChatColor.translateAlternateColorCodes('&', formatted);
        player.setDisplayName(formatted);
        player.setCustomName(formatted);
        player.setCustomNameVisible(false);
        if (plugin.getConfig().getBoolean("settings.update-tab-list-name", true)) {
            player.setPlayerListName(formatted);
        }
    }

    public void clear(Player player) {
        player.setDisplayName(player.getName());
        player.setCustomName(null);
        if (plugin.getConfig().getBoolean("settings.update-tab-list-name", true)) {
            player.setPlayerListName(player.getName());
        }
    }
}
