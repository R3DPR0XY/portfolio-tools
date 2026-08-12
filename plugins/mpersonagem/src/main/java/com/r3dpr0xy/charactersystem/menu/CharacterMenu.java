package com.r3dpr0xy.charactersystem.menu;

import com.r3dpr0xy.charactersystem.CharacterSystemPlugin;
import com.r3dpr0xy.charactersystem.model.CharacterProfile;
import com.r3dpr0xy.charactersystem.model.PlayerCharacters;
import com.r3dpr0xy.charactersystem.prompt.PromptType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CharacterMenu implements Listener {
    private static final String ZOMBIE_GUI_GLYPH = "\uE001";

    private final CharacterSystemPlugin plugin;
    private final NamespacedKey actionKey;
    private final NamespacedKey characterKey;
    private final NamespacedKey valueKey;
    private final Map<UUID, CreationDraft> drafts = new HashMap<>();

    public CharacterMenu(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "action");
        this.characterKey = new NamespacedKey(plugin, "character");
        this.valueKey = new NamespacedKey(plugin, "value");
    }

    public void openSelector(Player player) {
        Inventory inventory = createTexturedInventory(new CharacterHolder(MenuView.SELECTOR, player.getUniqueId()), 27,
                color(plugin.getConfig().getString("menu.title", "Personagens")));
        PlayerCharacters characters = plugin.characters(player);

        int slot = 10;
        for (CharacterProfile character : characters.characters()) {
            inventory.setItem(slot, characterItem(character, characters.activeCharacterId() != null
                    && characters.activeCharacterId().equals(character.id())));
            slot++;
            if (slot == 17) {
                break;
            }
        }

        int max = plugin.getConfig().getInt("settings.max-characters-per-player", 3);
        if (characters.characters().size() < max) {
            inventory.setItem(22, item(Material.EMERALD, "&aCriar personagem",
                    List.of("&7Inicia a sequencia de criacao.", "&7Limite: &f" + characters.characters().size() + "/" + max),
                    MenuActionType.CREATE, null, plugin.getConfig().getInt("menu.create-custom-model-data", 1002)));
        } else {
            inventory.setItem(22, item(Material.BARRIER, "&cLimite atingido",
                    List.of("&7Voce ja possui o maximo de personagens."), null, null, null));
        }

        player.openInventory(inventory);
    }

    public void openIdentity(Player viewer, Player target) {
        openIdentity(viewer, target, false);
    }

    public void openIdentity(Player viewer, Player target, boolean forceEdit) {
        Inventory inventory = Bukkit.createInventory(new CharacterHolder(MenuView.IDENTITY, target.getUniqueId()), 27,
                color(plugin.getConfig().getString("menu.id-title", "Identidade")));
        CharacterProfile character = plugin.activeCharacter(target).orElse(null);
        if (character == null) {
            inventory.setItem(13, item(Material.BARRIER, "&cSem personagem ativo",
                    List.of("&7Use /personagem para criar ou escolher."), null, null, null));
        } else {
            inventory.setItem(10, plain(Material.NAME_TAG, "&fNome", List.of("&7" + character.fullName())));
            inventory.setItem(12, plain(Material.CLOCK, "&fIdade", List.of("&7" + character.age() + " anos")));
            inventory.setItem(14, plain(Material.ARMOR_STAND, "&fAltura", List.of("&7" + character.height() + " cm")));
            inventory.setItem(16, plain(Material.BOOK, "&fRoleplay", List.of(
                    "&7Talento: &f" + character.talent(),
                    "&7Genero: &f" + empty(character.gender()),
                    "&7Descricao: &f" + empty(character.description())
            )));
            if (viewer.getUniqueId().equals(target.getUniqueId()) || forceEdit) {
                inventory.setItem(21, item(Material.WRITABLE_BOOK, "&eEditar personagem",
                        List.of("&7Altere os dados do personagem."), MenuActionType.EDIT, character.id(),
                        plugin.getConfig().getInt("menu.edit-custom-model-data", 1003)));
                if (forceEdit) {
                    inventory.setItem(23, item(Material.COMPARATOR, "&cMenu admin",
                            List.of("&7Ajustes rapidos por clique."), MenuActionType.ADMIN_EDIT, character.id(), null));
                }
            }
        }
        viewer.openInventory(inventory);
    }

    public void openTalentMenu(Player player) {
        CreationDraft draft = drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault()));
        Inventory inventory = createTexturedInventory(new CharacterHolder(MenuView.CREATE_TALENT, player.getUniqueId()), 27,
                color("&8Criacao: Talentos"));
        ConfigurationSection talents = plugin.getConfig().getConfigurationSection("talents");
        int slot = 10;
        if (talents != null) {
            for (String talent : talents.getKeys(false)) {
                Material material = material(plugin.getConfig().getString("talents." + talent + ".material", "BOOK"));
                List<String> lore = new ArrayList<>(plugin.getConfig().getStringList("talents." + talent + ".description"));
                lore.add(draft.talent().equalsIgnoreCase(talent) ? "&aSelecionado" : "&eClique para escolher");
                inventory.setItem(slot++, valueItem(material, "&f" + talent, lore, MenuActionType.TALENT, talent));
            }
        }
        inventory.setItem(22, item(Material.ARROW, "&aProximo", List.of("&7Ir para altura e atributos."),
                MenuActionType.CREATION_NEXT_HEIGHT, null, null));
        player.openInventory(inventory);
    }

    public void openCreationHeight(Player player) {
        CreationDraft draft = drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault()));
        Inventory inventory = createTexturedInventory(new CharacterHolder(MenuView.CREATE_HEIGHT, player.getUniqueId()), 27,
                color("&8Criacao: Altura"));
        int height = draft.height();
        inventory.setItem(11, item(Material.RED_STAINED_GLASS_PANE, "&c-5 cm", List.of(), MenuActionType.HEIGHT_DOWN, null, null));
        inventory.setItem(13, plain(Material.ARMOR_STAND, "&fAltura: &e" + height + " cm", List.of(
                "&7Vida: &f" + format(plugin.attributeService().calculatedValue("health", height)),
                "&7Velocidade: &f" + format(plugin.attributeService().calculatedValue("speed", height)),
                "&7Alcance: &f" + format(plugin.attributeService().calculatedValue("entity-interaction-range", height))
        )));
        inventory.setItem(15, item(Material.LIME_STAINED_GLASS_PANE, "&a+5 cm", List.of(), MenuActionType.HEIGHT_UP, null, null));
        inventory.setItem(22, item(Material.ARROW, "&aProximo", List.of("&7Ir para nome e idade."),
                MenuActionType.CREATION_NEXT_IDENTITY, null, null));
        player.openInventory(inventory);
    }

    public void openCreationIdentity(Player player) {
        CreationDraft draft = drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault()));
        Inventory inventory = createTexturedInventory(new CharacterHolder(MenuView.CREATE_IDENTITY, player.getUniqueId()), 27,
                color("&8Criacao: Nome e Idade"));
        inventory.setItem(10, item(Material.NAME_TAG, "&eNome", List.of("&7Atual: &f" + empty(draft.name())),
                MenuActionType.DRAFT_NAME, null, null));
        inventory.setItem(12, item(Material.PAPER, "&eSobrenome", List.of("&7Atual: &f" + empty(draft.surname())),
                MenuActionType.DRAFT_SURNAME, null, null));
        inventory.setItem(14, item(Material.RED_STAINED_GLASS_PANE, "&c-1 idade", List.of("&7Atual: &f" + draft.age()),
                MenuActionType.DRAFT_AGE_DOWN, null, null));
        inventory.setItem(15, plain(Material.CLOCK, "&fIdade: &e" + draft.age(), List.of()));
        inventory.setItem(16, item(Material.LIME_STAINED_GLASS_PANE, "&a+1 idade", List.of("&7Atual: &f" + draft.age()),
                MenuActionType.DRAFT_AGE_UP, null, null));
        inventory.setItem(22, item(Material.ARROW, "&aProximo", List.of("&7Ver confirmacao final."),
                MenuActionType.CREATION_CONFIRM, null, null));
        player.openInventory(inventory);
    }

    public void openCreationConfirm(Player player) {
        CreationDraft draft = drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault()));
        Inventory inventory = createTexturedInventory(new CharacterHolder(MenuView.CREATE_CONFIRM, player.getUniqueId()), 27,
                color("&8Confirmar personagem"));
        inventory.setItem(11, item(Material.RED_WOOL, "&cCancelar", List.of("&7Cancela a criacao."),
                MenuActionType.CREATION_CANCEL, null, null));
        inventory.setItem(13, plain(Material.PLAYER_HEAD, "&f" + empty(draft.name()), List.of(
                "&7Sobrenome: &f" + empty(draft.surname()),
                "&7Talento: &f" + draft.talent(),
                "&7Idade: &f" + draft.age(),
                "&7Altura: &f" + draft.height() + " cm"
        )));
        inventory.setItem(15, item(Material.LIME_WOOL, "&aCriar personagem", List.of("&7Salva e seleciona este personagem."),
                MenuActionType.CREATION_CONFIRM, null, null));
        player.openInventory(inventory);
    }

    public void openEditor(Player player, UUID characterId) {
        openEditor(player, player.getUniqueId(), characterId);
    }

    public void openEditor(Player viewer, UUID ownerId, UUID characterId) {
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner == null) {
            plugin.messages().send(viewer, "player-not-found");
            return;
        }
        CharacterProfile character = plugin.characters(owner).character(characterId).orElse(null);
        if (character == null) {
            plugin.messages().send(viewer, "not-found");
            openSelector(viewer);
            return;
        }

        Inventory inventory = Bukkit.createInventory(new CharacterHolder(MenuView.EDITOR, ownerId), 36,
                color("&8Editar " + character.name()));
        inventory.setItem(10, item(Material.NAME_TAG, "&eAlterar nome", List.of("&7Atual: &f" + character.name()),
                MenuActionType.SET_NAME, character.id(), null));
        inventory.setItem(11, item(Material.PAPER, "&eAlterar sobrenome", List.of("&7Atual: &f" + empty(character.surname())),
                MenuActionType.SET_SURNAME, character.id(), null));
        inventory.setItem(12, item(Material.CLOCK, "&eAlterar idade", List.of("&7Atual: &f" + character.age() + " anos"),
                MenuActionType.SET_AGE, character.id(), null));
        inventory.setItem(13, item(Material.ARMOR_STAND, "&eAlterar altura", List.of("&7Atual: &f" + character.height() + " cm"),
                MenuActionType.SET_HEIGHT, character.id(), null));
        inventory.setItem(14, item(Material.OAK_SIGN, "&eAlterar genero", List.of("&7Atual: &f" + empty(character.gender())),
                MenuActionType.SET_GENDER, character.id(), null));
        inventory.setItem(15, item(Material.WRITABLE_BOOK, "&eAlterar descricao", List.of("&7Atual: &f" + empty(character.description())),
                MenuActionType.SET_DESCRIPTION, character.id(), null));
        inventory.setItem(16, item(Material.TNT, "&cApagar personagem", List.of("&7Abre uma confirmacao antes de apagar."),
                MenuActionType.DELETE, character.id(), plugin.getConfig().getInt("menu.delete-custom-model-data", 1004)));
        inventory.setItem(31, item(Material.ARROW, "&7Voltar", List.of(), MenuActionType.BACK, null, null));
        viewer.openInventory(inventory);
    }

    public void openDeleteConfirm(Player viewer, UUID ownerId, UUID characterId) {
        Inventory inventory = Bukkit.createInventory(new CharacterHolder(MenuView.DELETE_CONFIRM, ownerId), 27,
                color("&8Confirmar exclusao"));
        inventory.setItem(11, item(Material.LIME_WOOL, "&aCancelar", List.of("&7Volta para o editor."),
                MenuActionType.CANCEL_DELETE, characterId, null));
        inventory.setItem(15, item(Material.RED_WOOL, "&cApagar", List.of("&7Essa acao remove o personagem."),
                MenuActionType.CONFIRM_DELETE, characterId, null));
        viewer.openInventory(inventory);
    }

    public void openAdminEditor(Player viewer, UUID ownerId, UUID characterId) {
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner == null) {
            plugin.messages().send(viewer, "player-not-found");
            return;
        }
        CharacterProfile character = plugin.characters(owner).character(characterId).orElse(null);
        if (character == null) {
            plugin.messages().send(viewer, "not-found");
            return;
        }
        Inventory inventory = Bukkit.createInventory(new CharacterHolder(MenuView.ADMIN_EDITOR, ownerId), 36,
                color("&8Admin: " + character.name()));
        inventory.setItem(10, item(Material.RED_STAINED_GLASS_PANE, "&c-1 idade", List.of("&7Atual: &f" + character.age()),
                MenuActionType.ADMIN_AGE_DOWN, character.id(), null));
        inventory.setItem(11, plain(Material.CLOCK, "&fIdade: &e" + character.age(), List.of()));
        inventory.setItem(12, item(Material.LIME_STAINED_GLASS_PANE, "&a+1 idade", List.of("&7Atual: &f" + character.age()),
                MenuActionType.ADMIN_AGE_UP, character.id(), null));
        inventory.setItem(14, item(Material.RED_STAINED_GLASS_PANE, "&c-5 altura", List.of("&7Atual: &f" + character.height() + " cm"),
                MenuActionType.ADMIN_HEIGHT_DOWN, character.id(), null));
        inventory.setItem(15, plain(Material.ARMOR_STAND, "&fAltura: &e" + character.height() + " cm", List.of(
                "&7Vida: &f" + format(plugin.attributeService().calculatedValue("health", character.height())),
                "&7Velocidade: &f" + format(plugin.attributeService().calculatedValue("speed", character.height())),
                "&7Alcance: &f" + format(plugin.attributeService().calculatedValue("entity-interaction-range", character.height()))
        )));
        inventory.setItem(16, item(Material.LIME_STAINED_GLASS_PANE, "&a+5 altura", List.of("&7Atual: &f" + character.height() + " cm"),
                MenuActionType.ADMIN_HEIGHT_UP, character.id(), null));
        inventory.setItem(22, item(Material.NETHER_STAR, "&eTrocar talento", List.of("&7Atual: &f" + character.talent()),
                MenuActionType.ADMIN_TALENT_NEXT, character.id(), null));
        inventory.setItem(31, item(Material.ARROW, "&7Voltar", List.of(), MenuActionType.BACK, null, null));
        viewer.openInventory(inventory);
    }

    public void setDraftName(Player player, String name) {
        drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault())).name(name);
        openCreationIdentity(player);
    }

    public void setDraftSurname(Player player, String surname) {
        drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault())).surname(surname);
        openCreationIdentity(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CharacterHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir() || !clicked.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        String actionName = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
        if (actionName == null) {
            return;
        }
        MenuActionType action = MenuActionType.valueOf(actionName);
        String characterRaw = meta.getPersistentDataContainer().get(characterKey, PersistentDataType.STRING);
        UUID characterId = characterRaw == null ? null : UUID.fromString(characterRaw);
        String value = meta.getPersistentDataContainer().get(valueKey, PersistentDataType.STRING);
        handle(player, holder.view(), holder.owner(), new MenuAction(action, characterId, value == null ? "" : value));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CharacterHolder) {
            event.setCancelled(true);
        }
    }

    private void handle(Player player, MenuView view, UUID ownerId, MenuAction action) {
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner == null) {
            plugin.messages().send(player, "player-not-found");
            return;
        }
        PlayerCharacters playerCharacters = plugin.characters(owner);
        switch (action.type()) {
            case SELECT -> {
                playerCharacters.activeCharacterId(action.characterId());
                plugin.save(owner);
                plugin.reloadPlayer(owner);
                player.closeInventory();
                plugin.messages().send(player, "selected");
            }
            case CREATE -> {
                drafts.put(player.getUniqueId(), new CreationDraft(plugin.heightDefault()));
                openTalentMenu(player);
            }
            case TALENT -> {
                drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault())).talent(action.value());
                openTalentMenu(player);
            }
            case HEIGHT_DOWN -> {
                CreationDraft draft = drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault()));
                draft.height(clamp(draft.height() - 5, plugin.heightMin(), plugin.heightMax()));
                openCreationHeight(player);
            }
            case HEIGHT_UP -> {
                CreationDraft draft = drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault()));
                draft.height(clamp(draft.height() + 5, plugin.heightMin(), plugin.heightMax()));
                openCreationHeight(player);
            }
            case CREATION_NEXT_HEIGHT -> openCreationHeight(player);
            case CREATION_NEXT_IDENTITY -> openCreationIdentity(player);
            case DRAFT_NAME -> {
                player.closeInventory();
                plugin.promptManager().startDraftEdit(player, PromptType.DRAFT_NAME);
            }
            case DRAFT_SURNAME -> {
                player.closeInventory();
                plugin.promptManager().startDraftEdit(player, PromptType.DRAFT_SURNAME);
            }
            case DRAFT_AGE_DOWN -> {
                CreationDraft draft = drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault()));
                int min = plugin.getConfig().getInt("validation.age.min", 0);
                draft.age(Math.max(min, draft.age() - 1));
                openCreationIdentity(player);
            }
            case DRAFT_AGE_UP -> {
                CreationDraft draft = drafts.computeIfAbsent(player.getUniqueId(), id -> new CreationDraft(plugin.heightDefault()));
                int max = plugin.getConfig().getInt("validation.age.max", 150);
                draft.age(Math.min(max, draft.age() + 1));
                openCreationIdentity(player);
            }
            case CREATION_CONFIRM -> {
                if (view == MenuView.CREATE_IDENTITY) {
                    openCreationConfirm(player);
                } else {
                    createDraftCharacter(player);
                }
            }
            case CREATION_CANCEL -> {
                drafts.remove(player.getUniqueId());
                openSelector(player);
            }
            case EDIT -> openEditor(player, ownerId, action.characterId());
            case ADMIN_EDIT -> openAdminEditor(player, ownerId, action.characterId());
            case ADMIN_AGE_DOWN -> {
                adjustAge(owner, action.characterId(), -1);
                openAdminEditor(player, ownerId, action.characterId());
            }
            case ADMIN_AGE_UP -> {
                adjustAge(owner, action.characterId(), 1);
                openAdminEditor(player, ownerId, action.characterId());
            }
            case ADMIN_HEIGHT_DOWN -> {
                adjustHeight(owner, action.characterId(), -5);
                openAdminEditor(player, ownerId, action.characterId());
            }
            case ADMIN_HEIGHT_UP -> {
                adjustHeight(owner, action.characterId(), 5);
                openAdminEditor(player, ownerId, action.characterId());
            }
            case ADMIN_TALENT_NEXT -> {
                nextTalent(owner, action.characterId());
                openAdminEditor(player, ownerId, action.characterId());
            }
            case DELETE -> {
                openDeleteConfirm(player, ownerId, action.characterId());
            }
            case CONFIRM_DELETE -> {
                CharacterProfile active = playerCharacters.activeCharacter().orElse(null);
                playerCharacters.characters().removeIf(character -> character.id().equals(action.characterId()));
                if (active != null && active.id().equals(action.characterId())) {
                    playerCharacters.activeCharacterId(null);
                }
                plugin.save(owner);
                plugin.reloadPlayer(owner);
                plugin.messages().send(player, "deleted");
                if (player.getUniqueId().equals(ownerId)) {
                    openSelector(player);
                } else {
                    player.closeInventory();
                }
            }
            case CANCEL_DELETE -> {
                plugin.messages().send(player, "delete-cancelled");
                openEditor(player, ownerId, action.characterId());
            }
            case SET_NAME -> {
                player.closeInventory();
                plugin.promptManager().startEdit(player, ownerId, action.characterId(), PromptType.NAME);
            }
            case SET_SURNAME -> {
                player.closeInventory();
                plugin.promptManager().startEdit(player, ownerId, action.characterId(), PromptType.SURNAME);
            }
            case SET_AGE -> {
                player.closeInventory();
                plugin.promptManager().startEdit(player, ownerId, action.characterId(), PromptType.AGE);
            }
            case SET_HEIGHT -> {
                player.closeInventory();
                plugin.promptManager().startEdit(player, ownerId, action.characterId(), PromptType.HEIGHT);
            }
            case SET_GENDER -> {
                player.closeInventory();
                plugin.promptManager().startEdit(player, ownerId, action.characterId(), PromptType.GENDER);
            }
            case SET_DESCRIPTION -> {
                player.closeInventory();
                plugin.promptManager().startEdit(player, ownerId, action.characterId(), PromptType.DESCRIPTION);
            }
            case BACK -> {
                if (view == MenuView.ADMIN_EDITOR) {
                    openIdentity(player, owner, true);
                } else if (player.getUniqueId().equals(ownerId)) {
                    openSelector(player);
                } else {
                    player.closeInventory();
                }
            }
        }
    }

    private void createDraftCharacter(Player player) {
        CreationDraft draft = drafts.get(player.getUniqueId());
        if (draft == null || draft.name().isBlank()) {
            openCreationIdentity(player);
            return;
        }
        PlayerCharacters characters = plugin.characters(player);
        int max = plugin.getConfig().getInt("settings.max-characters-per-player", 3);
        if (characters.characters().size() >= max) {
            plugin.messages().send(player, "limit-reached");
            openSelector(player);
            return;
        }
        CharacterProfile character = new CharacterProfile(UUID.randomUUID(), draft.name(), draft.surname(), draft.talent(),
                draft.age(), draft.height(), draft.gender(), draft.description(), System.currentTimeMillis());
        characters.characters().add(character);
        characters.activeCharacterId(character.id());
        drafts.remove(player.getUniqueId());
        plugin.save(player);
        plugin.reloadPlayer(player);
        plugin.messages().send(player, "created");
        openSelector(player);
    }

    private void adjustAge(Player owner, UUID characterId, int amount) {
        CharacterProfile character = plugin.characters(owner).character(characterId).orElse(null);
        if (character == null) {
            return;
        }
        int min = plugin.getConfig().getInt("validation.age.min", 0);
        int max = plugin.getConfig().getInt("validation.age.max", 150);
        character.age(clamp(character.age() + amount, min, max));
        plugin.save(owner);
        plugin.reloadPlayer(owner);
    }

    private void adjustHeight(Player owner, UUID characterId, int amount) {
        CharacterProfile character = plugin.characters(owner).character(characterId).orElse(null);
        if (character == null) {
            return;
        }
        character.height(clamp(character.height() + amount, plugin.heightMin(), plugin.heightMax()));
        plugin.save(owner);
        plugin.reloadPlayer(owner);
    }

    private void nextTalent(Player owner, UUID characterId) {
        CharacterProfile character = plugin.characters(owner).character(characterId).orElse(null);
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("talents");
        if (character == null || section == null || section.getKeys(false).isEmpty()) {
            return;
        }
        List<String> talents = new ArrayList<>(section.getKeys(false));
        int index = talents.indexOf(character.talent());
        character.talent(talents.get((index + 1) % talents.size()));
        plugin.save(owner);
        plugin.reloadPlayer(owner);
    }

    private ItemStack characterItem(CharacterProfile character, boolean active) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Nome: &f" + character.name());
        if (character.surname() != null && !character.surname().isBlank()) {
            lore.add("&7Sobrenome: &f" + character.surname());
        }
        lore.add("&7Idade: &f" + character.age() + " anos");
        lore.add("&7Talento: &f" + character.talent());
        lore.add("&7Altura: &f" + character.height() + " cm");
        lore.add("&7Vida: &f" + format(plugin.attributeService().calculatedValue("health", character.height())));
        lore.add("&7Velocidade: &f" + format(plugin.attributeService().calculatedValue("speed", character.height())));
        lore.add("&7Alcance: &f" + format(plugin.attributeService().calculatedValue("entity-interaction-range", character.height())));
        lore.add(active ? "&aSelecionado" : "&eClique para selecionar");
        lore.add("&7Botao direito nao e necessario: use /id para editar o ativo.");
        return item(Material.PLAYER_HEAD, (active ? "&a" : "&f") + character.fullName(), lore,
                MenuActionType.SELECT, character.id(), plugin.getConfig().getInt("menu.character-custom-model-data", 1001));
    }

    private ItemStack plain(Material material, String name, List<String> lore) {
        return item(material, name, lore, null, null, null);
    }

    private ItemStack item(Material material, String name, List<String> lore, MenuActionType action, UUID characterId, Integer customModelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        meta.setLore(lore.stream().map(this::color).toList());
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }
        if (action != null) {
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action.name());
        }
        if (characterId != null) {
            meta.getPersistentDataContainer().set(characterKey, PersistentDataType.STRING, characterId.toString());
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack valueItem(Material material, String name, List<String> lore, MenuActionType action, String value) {
        ItemStack item = item(material, name, lore, action, null, null);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(valueKey, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    private Inventory createTexturedInventory(InventoryHolder holder, int size, String fallbackTitle) {
        if (!plugin.getConfig().getBoolean("resource-pack.enabled", true)) {
            return Bukkit.createInventory(holder, size, fallbackTitle);
        }
        Component title = Component.text(ZOMBIE_GUI_GLYPH).font(Key.key("mpersonagem:gui"));
        return Bukkit.createInventory(holder, size, title);
    }

    private Material material(String name) {
        Material material = Material.matchMaterial(name == null ? "BOOK" : name);
        return material == null ? Material.BOOK : material;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

    private String empty(String value) {
        return value == null || value.isBlank() ? "Nao informado" : value;
    }

    private enum MenuView {
        SELECTOR,
        IDENTITY,
        CREATE_TALENT,
        CREATE_HEIGHT,
        CREATE_IDENTITY,
        CREATE_CONFIRM,
        EDITOR,
        DELETE_CONFIRM,
        ADMIN_EDITOR
    }

    private record CharacterHolder(MenuView view, UUID owner) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
