package com.daich.betterchat;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BetterchatClient implements ClientModInitializer {
    public static final String MOD_ID = "betterchat";
    public static final Logger LOGGER = LoggerFactory.getLogger("Betterchat");

    private static final ContactBook CONTACTS = new ContactBook();
    private static final Pattern[] PRIVATE_MESSAGE_PATTERNS = {
            Pattern.compile("^From\\s+([A-Za-z0-9_]{3,16})[:>]?\\s+(.+)$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^([A-Za-z0-9_]{3,16})\\s+whispers(?:\\s+to\\s+you)?[:>]?\\s+(.+)$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^([A-Za-z0-9_]{3,16})\\s+->\\s+(?:You|Voce|Você|me|mim)[:>]?\\s+(.+)$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^\\[?([A-Za-z0-9_]{3,16})\\s*->\\s*(?:You|Voce|Você|me|mim)\\]?[: ]+(.+)$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^([A-Za-z0-9_]{3,16})\\s+(?:sussurra|cochicha)(?:\\s+para\\s+voce|\\s+para\\s+você)?[:>]?\\s+(.+)$", Pattern.CASE_INSENSITIVE)
    };
    private static KeyBinding openContactsKey;

    @Override
    public void onInitializeClient() {
        CONTACTS.load();
        CONTACTS.setShowChatMenuHud(false);
        openContactsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterchat.open_contacts",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.betterchat"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openContactsKey.wasPressed()) {
                openContactsScreen();
            }
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> capturePrivateMessage(message.getString()));
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> capturePrivateMessage(message.getString()));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(commandTree("betterchat"));
            dispatcher.register(commandTree("bc"));
            dispatcher.register(commandTree("pc"));
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> commandTree(String command) {
        return literal(command)
                .then(literal("add").then(addContactArgument()))
                .then(literal("adicionar").then(addContactArgument()))
                .then(literal("remove").then(contactArgument("contato").executes(context -> removeContact(context.getSource(), StringArgumentType.getString(context, "contato")))))
                .then(literal("remover").then(contactArgument("contato").executes(context -> removeContact(context.getSource(), StringArgumentType.getString(context, "contato")))))
                .then(literal("list").executes(context -> listContacts(context.getSource())))
                .then(literal("listar").executes(context -> listContacts(context.getSource())))
                .then(literal("focus").then(contactArgument("contato").executes(context -> focusContact(context.getSource(), StringArgumentType.getString(context, "contato")))))
                .then(literal("focar").then(contactArgument("contato").executes(context -> focusContact(context.getSource(), StringArgumentType.getString(context, "contato")))))
                .then(literal("msg").then(contactArgument("contato")
                        .then(argument("mensagem", StringArgumentType.greedyString())
                                .executes(context -> sendToContact(context.getSource(), StringArgumentType.getString(context, "contato"), StringArgumentType.getString(context, "mensagem"))))))
                .then(literal("mensagem").then(contactArgument("contato")
                        .then(argument("mensagem", StringArgumentType.greedyString())
                                .executes(context -> sendToContact(context.getSource(), StringArgumentType.getString(context, "contato"), StringArgumentType.getString(context, "mensagem"))))))
                .then(literal("say").then(argument("mensagem", StringArgumentType.greedyString())
                        .executes(context -> sendToFocused(context.getSource(), StringArgumentType.getString(context, "mensagem")))))
                .then(literal("falar").then(argument("mensagem", StringArgumentType.greedyString())
                        .executes(context -> sendToFocused(context.getSource(), StringArgumentType.getString(context, "mensagem")))))
                .then(literal("reply").then(argument("mensagem", StringArgumentType.greedyString())
                        .executes(context -> reply(context.getSource(), StringArgumentType.getString(context, "mensagem")))))
                .then(literal("responder").then(argument("mensagem", StringArgumentType.greedyString())
                        .executes(context -> reply(context.getSource(), StringArgumentType.getString(context, "mensagem")))))
                .then(literal("ui").executes(context -> openUi(context.getSource())))
                .then(literal("menu").executes(context -> openUi(context.getSource())))
                .then(literal("hud").executes(context -> showHudStatus(context.getSource())))
                .then(literal("command").executes(context -> showCommand(context.getSource())).then(commandArgument()))
                .then(literal("comando").executes(context -> showCommand(context.getSource())).then(commandArgument()));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource, String> addContactArgument() {
        return argument("jogador", StringArgumentType.word())
                .executes(context -> addContact(context.getSource(), StringArgumentType.getString(context, "jogador"), null))
                .then(argument("apelido", StringArgumentType.greedyString())
                        .executes(context -> addContact(context.getSource(), StringArgumentType.getString(context, "jogador"), StringArgumentType.getString(context, "apelido"))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource, String> contactArgument(String name) {
        return argument(name, StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(CONTACTS.contactSuggestions(), builder));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource, String> commandArgument() {
        return argument("comando", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"msg", "tell", "w", "whisper"}, builder))
                .executes(context -> setCommand(context.getSource(), StringArgumentType.getString(context, "comando")));
    }

    public static ContactBook contacts() {
        return CONTACTS;
    }

    public static void openContactsScreen() {
        MinecraftClient.getInstance().setScreen(new BetterchatScreen());
        playUiOpenSound();
    }

    public static boolean sendPrivateMessage(Contact contact, String message) {
        if (contact == null || message == null || message.isBlank()) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            return false;
        }
        CONTACTS.markConversation(contact);
        CONTACTS.recordOutgoing(contact, message);
        client.player.networkHandler.sendChatCommand(CONTACTS.privateMessageCommand() + " " + contact.name + " " + message.trim());
        return true;
    }

    public static void playUiOpenSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (CONTACTS.playSounds() && client.getSoundManager() != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_IN, 1.0F));
        }
    }

    private static void capturePrivateMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return;
        }
        String cleaned = rawMessage.replaceAll("§.", "").trim();
        for (Pattern pattern : PRIVATE_MESSAGE_PATTERNS) {
            Matcher matcher = pattern.matcher(cleaned);
            if (matcher.find()) {
                CONTACTS.find(matcher.group(1)).ifPresent(contact -> CONTACTS.recordIncoming(contact, matcher.group(2)));
                return;
            }
        }
    }

    private static int addContact(FabricClientCommandSource source, String player, String alias) {
        Contact contact = CONTACTS.add(player, alias);
        source.sendFeedback(feedback("Contato salvo: " + contact.label()));
        return 1;
    }

    private static int removeContact(FabricClientCommandSource source, String nameOrAlias) {
        if (CONTACTS.remove(nameOrAlias)) {
            source.sendFeedback(feedback("Contato removido: " + nameOrAlias));
            return 1;
        }
        source.sendError(feedback("Contato não encontrado: " + nameOrAlias));
        return 0;
    }

    private static int listContacts(FabricClientCommandSource source) {
        if (CONTACTS.sortedContacts().isEmpty()) {
            source.sendFeedback(feedback("Nenhum contato salvo. Use /bc adicionar <jogador> [apelido]."));
            return 1;
        }
        String contacts = CONTACTS.sortedContacts().stream()
                .map(Contact::label)
                .collect(Collectors.joining(", "));
        source.sendFeedback(feedback("Contatos: " + contacts));
        return 1;
    }

    private static int showCommand(FabricClientCommandSource source) {
        source.sendFeedback(feedback("Comando privado atual: /" + CONTACTS.privateMessageCommand()));
        return 1;
    }

    private static int setCommand(FabricClientCommandSource source, String command) {
        CONTACTS.setPrivateMessageCommand(command);
        source.sendFeedback(feedback("Comando privado definido para /" + CONTACTS.privateMessageCommand()));
        return 1;
    }

    private static int openUi(FabricClientCommandSource source) {
        openContactsScreen();
        return 1;
    }

    private static int showHudStatus(FabricClientCommandSource source) {
        source.sendFeedback(feedback("HUD removido. Use a tecla P ou /bc menu para abrir o Betterchat."));
        return 1;
    }

    private static int focusContact(FabricClientCommandSource source, String nameOrAlias) {
        Optional<Contact> contact = CONTACTS.find(nameOrAlias);
        if (contact.isEmpty()) {
            source.sendError(feedback("Contato não encontrado: " + nameOrAlias));
            return 0;
        }
        CONTACTS.focus(contact.get());
        source.sendFeedback(feedback("Conversa ativa: " + contact.get().label() + ". Use /bc falar <mensagem>."));
        return 1;
    }

    private static int sendToContact(FabricClientCommandSource source, String nameOrAlias, String message) {
        Optional<Contact> contact = CONTACTS.find(nameOrAlias);
        if (contact.isEmpty()) {
            source.sendError(feedback("Contato não encontrado: " + nameOrAlias));
            return 0;
        }
        return sendWithFeedback(source, contact.get(), message);
    }

    private static int sendToFocused(FabricClientCommandSource source, String message) {
        Optional<Contact> contact = CONTACTS.focusedContact();
        if (contact.isEmpty()) {
            source.sendError(feedback("Nenhuma conversa ativa. Use /bc focar <contato>."));
            return 0;
        }
        return sendWithFeedback(source, contact.get(), message);
    }

    private static int reply(FabricClientCommandSource source, String message) {
        Optional<Contact> contact = CONTACTS.lastContact();
        if (contact.isEmpty()) {
            source.sendError(feedback("Nenhuma conversa recente. Use /bc mensagem <contato> <mensagem>."));
            return 0;
        }
        return sendWithFeedback(source, contact.get(), message);
    }

    private static int sendWithFeedback(FabricClientCommandSource source, Contact contact, String message) {
        if (!sendPrivateMessage(contact, message)) {
            source.sendError(feedback("Não foi possível enviar agora."));
            return 0;
        }
        source.sendFeedback(feedback("Para " + contact.label() + ": " + message));
        return 1;
    }

    private static Text feedback(String message) {
        return Text.literal("[Betterchat] " + message);
    }
}
