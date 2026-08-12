package com.r3dpr0xy.charactersystem.prompt;

import com.r3dpr0xy.charactersystem.CharacterSystemPlugin;
import com.r3dpr0xy.charactersystem.model.CharacterProfile;
import com.r3dpr0xy.charactersystem.model.PlayerCharacters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PromptManager implements Listener {
    private static final String SKIP = "pular";

    private final CharacterSystemPlugin plugin;
    private final Map<UUID, PromptSession> sessions = new HashMap<>();

    public PromptManager(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
    }

    public void startCreate(Player player) {
        sessions.put(player.getUniqueId(), new PromptSession(player.getUniqueId(), null, PromptType.NAME));
        prompt(player, PromptType.NAME, false);
    }

    public void startEdit(Player player, UUID ownerId, UUID characterId, PromptType type) {
        sessions.put(player.getUniqueId(), new PromptSession(ownerId, characterId, type));
        prompt(player, type, true);
    }

    public void startDraftEdit(Player player, PromptType type) {
        sessions.put(player.getUniqueId(), new PromptSession(player.getUniqueId(), null, type));
        prompt(player, type, true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        PromptSession session = sessions.get(event.getPlayer().getUniqueId());
        if (session == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage().trim();
        Bukkit.getScheduler().runTask(plugin, () -> handle(event.getPlayer(), session, message));
    }

    private void handle(Player player, PromptSession session, String message) {
        if (message.equalsIgnoreCase("cancelar")) {
            sessions.remove(player.getUniqueId());
            plugin.messages().send(player, "cancelled");
            plugin.menu().openSelector(player);
            return;
        }

        if (session.type() == PromptType.DRAFT_NAME || session.type() == PromptType.DRAFT_SURNAME) {
            handleDraft(player, session, message);
        } else if (session.characterId() == null) {
            handleCreate(player, session, message);
        } else {
            handleEdit(player, session, message);
        }
    }

    private void handleDraft(Player player, PromptSession session, String message) {
        if (session.type() == PromptType.DRAFT_NAME) {
            String name = validateText(message, "validation.name");
            if (name == null) {
                invalidName(player, "validation.name");
                return;
            }
            sessions.remove(player.getUniqueId());
            plugin.menu().setDraftName(player, name);
            return;
        }

        if (message.equalsIgnoreCase(SKIP)) {
            sessions.remove(player.getUniqueId());
            plugin.menu().setDraftSurname(player, "");
            return;
        }

        String surname = validateText(message, "validation.surname");
        if (surname == null) {
            invalidName(player, "validation.surname");
            return;
        }
        sessions.remove(player.getUniqueId());
        plugin.menu().setDraftSurname(player, surname);
    }

    private void handleCreate(Player player, PromptSession session, String message) {
        switch (session.type()) {
            case NAME -> {
                String name = validateText(message, "validation.name");
                if (name == null) {
                    invalidName(player, "validation.name");
                    return;
                }
                session.name(name);
                session.type(PromptType.SURNAME);
                prompt(player, PromptType.SURNAME, false);
            }
            case SURNAME -> {
                if (!message.equalsIgnoreCase(SKIP)) {
                    String surname = validateText(message, "validation.surname");
                    if (surname == null) {
                        invalidName(player, "validation.surname");
                        return;
                    }
                    session.surname(surname);
                }
                session.type(PromptType.AGE);
                prompt(player, PromptType.AGE, false);
            }
            case AGE -> {
                Integer age = parseInt(message, plugin.getConfig().getInt("validation.age.min", 0),
                        plugin.getConfig().getInt("validation.age.max", 150));
                if (age == null) {
                    plugin.messages().send(player, "prompt.invalid-age", Map.of(
                            "min", Integer.toString(plugin.getConfig().getInt("validation.age.min", 0)),
                            "max", Integer.toString(plugin.getConfig().getInt("validation.age.max", 150))
                    ));
                    return;
                }
                session.age(age);
                session.type(PromptType.HEIGHT);
                prompt(player, PromptType.HEIGHT, false);
            }
            case HEIGHT -> {
                Integer height = parseInt(message, plugin.heightMin(), plugin.heightMax());
                if (height == null) {
                    invalidHeight(player);
                    return;
                }
                session.height(height);
                session.type(PromptType.GENDER);
                prompt(player, PromptType.GENDER, false);
            }
            case GENDER -> {
                if (!message.equalsIgnoreCase(SKIP)) {
                    session.gender(limit(message, 24));
                }
                session.type(PromptType.DESCRIPTION);
                prompt(player, PromptType.DESCRIPTION, false);
            }
            case DESCRIPTION -> {
                String description = message.equalsIgnoreCase(SKIP) ? "" : limit(message,
                        plugin.getConfig().getInt("validation.description.max-length", 80));
                createCharacter(player, session.name(), session.surname(), session.age(), session.height(), session.gender(), description);
                sessions.remove(player.getUniqueId());
                plugin.menu().openSelector(player);
            }
            case DRAFT_NAME, DRAFT_SURNAME -> {
            }
        }
    }

    private void handleEdit(Player player, PromptSession session, String message) {
        Player owner = Bukkit.getPlayer(session.ownerId());
        if (owner == null) {
            sessions.remove(player.getUniqueId());
            plugin.messages().send(player, "player-not-found");
            return;
        }

        CharacterProfile character = plugin.characters(owner).character(session.characterId()).orElse(null);
        if (character == null) {
            sessions.remove(player.getUniqueId());
            plugin.messages().send(player, "not-found");
            plugin.menu().openSelector(player);
            return;
        }

        switch (session.type()) {
            case NAME -> {
                String name = validateText(message, "validation.name");
                if (name == null) {
                    invalidName(player, "validation.name");
                    return;
                }
                character.name(name);
            }
            case SURNAME -> {
                if (message.equalsIgnoreCase(SKIP)) {
                    character.surname("");
                } else {
                    String surname = validateText(message, "validation.surname");
                    if (surname == null) {
                        invalidName(player, "validation.surname");
                        return;
                    }
                    character.surname(surname);
                }
            }
            case AGE -> {
                Integer age = parseInt(message, plugin.getConfig().getInt("validation.age.min", 0),
                        plugin.getConfig().getInt("validation.age.max", 150));
                if (age == null) {
                    plugin.messages().send(player, "prompt.invalid-age", Map.of(
                            "min", Integer.toString(plugin.getConfig().getInt("validation.age.min", 0)),
                            "max", Integer.toString(plugin.getConfig().getInt("validation.age.max", 150))
                    ));
                    return;
                }
                character.age(age);
            }
            case HEIGHT -> {
                Integer height = parseInt(message, plugin.heightMin(), plugin.heightMax());
                if (height == null) {
                    invalidHeight(player);
                    return;
                }
                character.height(height);
            }
            case GENDER -> character.gender(message.equalsIgnoreCase(SKIP) ? "" : limit(message, 24));
            case DESCRIPTION -> character.description(message.equalsIgnoreCase(SKIP) ? "" : limit(message,
                    plugin.getConfig().getInt("validation.description.max-length", 80)));
            case DRAFT_NAME, DRAFT_SURNAME -> {
            }
        }

        sessions.remove(player.getUniqueId());
        plugin.save(owner);
        plugin.reloadPlayer(owner);
        plugin.messages().send(player, "updated");
        plugin.menu().openEditor(player, owner.getUniqueId(), character.id());
    }

    private void createCharacter(Player player, String name, String surname, int age, int height, String gender, String description) {
        PlayerCharacters characters = plugin.characters(player);
        int max = plugin.getConfig().getInt("settings.max-characters-per-player", 3);
        if (characters.characters().size() >= max) {
            plugin.messages().send(player, "limit-reached");
            return;
        }
        CharacterProfile character = new CharacterProfile(UUID.randomUUID(), name, surname, age, height, gender, description, System.currentTimeMillis());
        characters.characters().add(character);
        characters.activeCharacterId(character.id());
        plugin.save(player);
        plugin.reloadPlayer(player);
        plugin.messages().send(player, "created");
    }

    private void prompt(Player player, PromptType type, boolean edit) {
        switch (type) {
            case NAME -> plugin.messages().send(player, "prompt.name");
            case SURNAME -> plugin.messages().send(player, "prompt.surname");
            case AGE -> plugin.messages().send(player, "prompt.age");
            case HEIGHT -> plugin.messages().send(player, "prompt.height", Map.of(
                    "min", Integer.toString(plugin.heightMin()),
                    "max", Integer.toString(plugin.heightMax())
            ));
            case GENDER -> plugin.messages().send(player, "prompt.gender");
            case DESCRIPTION -> plugin.messages().send(player, "prompt.description");
            case DRAFT_NAME -> plugin.messages().send(player, "prompt.name");
            case DRAFT_SURNAME -> plugin.messages().send(player, "prompt.surname");
        }
        if (type == PromptType.SURNAME || type == PromptType.GENDER || type == PromptType.DESCRIPTION || type == PromptType.DRAFT_SURNAME) {
            plugin.messages().send(player, "prompt.skip-line");
        }
        plugin.messages().send(player, "prompt.cancel-line");
    }

    private String validateText(String message, String path) {
        int min = plugin.getConfig().getInt(path + ".min-length", 0);
        int max = plugin.getConfig().getInt(path + ".max-length", 16);
        String regex = plugin.getConfig().getString(path + ".regex", ".+");
        if (message.length() < min || message.length() > max || !message.matches(regex)) {
            return null;
        }
        return message;
    }

    private void invalidName(Player player, String path) {
        plugin.messages().send(player, "prompt.invalid-name", Map.of(
                "min", Integer.toString(plugin.getConfig().getInt(path + ".min-length", 0)),
                "max", Integer.toString(plugin.getConfig().getInt(path + ".max-length", 16))
        ));
    }

    private void invalidHeight(Player player) {
        plugin.messages().send(player, "prompt.invalid-height", Map.of(
                "min", Integer.toString(plugin.heightMin()),
                "max", Integer.toString(plugin.heightMax())
        ));
    }

    private Integer parseInt(String message, int min, int max) {
        try {
            int value = Integer.parseInt(message);
            return value < min || value > max ? null : value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String limit(String message, int max) {
        return message.length() <= max ? message : message.substring(0, max);
    }
}
