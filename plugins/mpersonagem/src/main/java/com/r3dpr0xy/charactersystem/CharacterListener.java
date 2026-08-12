package com.r3dpr0xy.charactersystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CharacterListener implements Listener {
    private final CharacterSystemPlugin plugin;

    public CharacterListener(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.characters(event.getPlayer());
        plugin.reloadPlayer(event.getPlayer());
        if (plugin.getConfig().getBoolean("settings.ask-on-first-join", true)
                && plugin.characters(event.getPlayer()).characters().isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.menu().openSelector(event.getPlayer()), 20L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.unload(event.getPlayer());
    }
}
