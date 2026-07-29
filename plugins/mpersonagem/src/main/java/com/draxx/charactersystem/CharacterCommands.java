package com.draxx.charactersystem;

import com.draxx.charactersystem.model.CharacterProfile;
import com.draxx.charactersystem.model.PlayerCharacters;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public final class CharacterCommands implements CommandExecutor {
    private final CharacterSystemPlugin plugin;

    public CharacterCommands(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return switch (command.getName().toLowerCase()) {
            case "personagem" -> playerMenu(sender);
            case "id" -> identity(sender, args);
            case "pularano" -> advanceYear(sender);
            case "mpersonagem" -> admin(sender, args);
            default -> false;
        };
    }

    private boolean playerMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "only-player");
            return true;
        }
        if (!player.hasPermission("mpersonagem.use")) {
            plugin.messages().send(player, "no-permission");
            return true;
        }
        plugin.menu().openSelector(player);
        return true;
    }

    private boolean identity(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "only-player");
            return true;
        }
        if (args.length > 0) {
            if (!player.hasPermission("mpersonagem.view")) {
                plugin.messages().send(player, "no-permission");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.messages().send(player, "player-not-found");
                return true;
            }
            plugin.menu().openIdentity(player, target);
            return true;
        }
        plugin.menu().openIdentity(player, player);
        return true;
    }

    private boolean admin(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("ajuda") || args[0].equalsIgnoreCase("help")) {
            plugin.messages().list("help", Map.of()).forEach(sender::sendMessage);
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            if (!sender.hasPermission("mpersonagem.reload")) {
                plugin.messages().send(sender, "no-permission");
                return true;
            }
            plugin.reloadPlugin();
            plugin.messages().send(sender, "reload");
            return true;
        }

        if (sub.equals("ver")) {
            if (!(sender instanceof Player player)) {
                plugin.messages().send(sender, "only-player");
                return true;
            }
            if (!sender.hasPermission("mpersonagem.view")) {
                plugin.messages().send(sender, "no-permission");
                return true;
            }
            Player target = args.length < 2 ? null : Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                plugin.messages().send(sender, "player-not-found");
                return true;
            }
            plugin.menu().openIdentity(player, target);
            return true;
        }

        if (sub.equals("editar")) {
            if (!(sender instanceof Player player)) {
                plugin.messages().send(sender, "only-player");
                return true;
            }
            if (!sender.hasPermission("mpersonagem.edit")) {
                plugin.messages().send(sender, "no-permission");
                return true;
            }
            Player target = args.length < 2 ? null : Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                plugin.messages().send(sender, "player-not-found");
                return true;
            }
            plugin.menu().openIdentity(player, target, true);
            return true;
        }

        if (sub.equals("reset")) {
            if (!sender.hasPermission("mpersonagem.reset")) {
                plugin.messages().send(sender, "no-permission");
                return true;
            }
            OfflinePlayer target = args.length < 2 ? null : Bukkit.getOfflinePlayer(args[1]);
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                plugin.messages().send(sender, "player-not-found");
                return true;
            }
            plugin.resetCharacters(target.getUniqueId());
            if (target.getPlayer() != null) {
                plugin.reloadPlayer(target.getPlayer());
            }
            plugin.messages().send(sender, "reset", Map.of("player", target.getName() == null ? args[1] : target.getName()));
            return true;
        }

        if (sub.equals("setaltura")) {
            return setHeight(sender, args);
        }

        plugin.messages().list("help", Map.of()).forEach(sender::sendMessage);
        return true;
    }

    private boolean setHeight(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mpersonagem.setaltura")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        Player target = args.length < 3 ? null : Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.messages().send(sender, "player-not-found");
            return true;
        }
        Integer height = parseInt(args[2], plugin.heightMin(), plugin.heightMax());
        if (height == null) {
            plugin.messages().send(sender, "prompt.invalid-height", Map.of(
                    "min", Integer.toString(plugin.heightMin()),
                    "max", Integer.toString(plugin.heightMax())
            ));
            return true;
        }
        CharacterProfile character = plugin.activeCharacter(target).orElse(null);
        if (character == null) {
            plugin.messages().send(sender, "not-found");
            return true;
        }
        character.height(height);
        plugin.save(target);
        plugin.reloadPlayer(target);
        plugin.messages().send(sender, "height-set", Map.of("player", target.getName(), "height", Integer.toString(height)));
        return true;
    }

    private boolean advanceYear(CommandSender sender) {
        if (!sender.hasPermission("mpersonagem.pularano")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        int updated = advanceYear();
        plugin.messages().send(sender, "advanced-year", Map.of("amount", Integer.toString(updated)));
        for (Player online : Bukkit.getOnlinePlayers()) {
            plugin.reloadPlayer(online);
        }
        return true;
    }

    private int advanceYear() {
        int updated = 0;
        for (File file : plugin.store().playerFiles()) {
            try {
                UUID playerId = plugin.store().playerIdFromFile(file);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                Player online = offlinePlayer.getPlayer();
                PlayerCharacters playerCharacters = online == null ? plugin.store().load(playerId) : plugin.characters(online);
                for (CharacterProfile character : playerCharacters.characters()) {
                    character.age(character.age() + 1);
                    updated++;
                }
                plugin.store().save(playerId, playerCharacters);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Arquivo de jogador invalido ignorado: " + file.getName());
            }
        }
        return updated;
    }

    private Integer parseInt(String message, int min, int max) {
        try {
            int value = Integer.parseInt(message);
            return value < min || value > max ? null : value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
