package com.daich.betterchat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BetterchatScreen extends Screen {
    private static final int PANEL = 0xB30A0D12;
    private static final int PANEL_SOFT = 0x77141A24;
    private static final int PANEL_STRONG = 0xD006090D;
    private static final int ACCENT = 0xE83DA5FF;
    private static final int TEXT_MAIN = 0xFFFFFFFF;
    private static final int TEXT_MUTED = 0xFFB7C2D0;
    private static final int TEXT_DIM = 0xFF7F8B99;

    private final ContactBook contacts = BetterchatClient.contacts();
    private final List<ButtonWidget> contactButtons = new ArrayList<>();
    private TextFieldWidget playerField;
    private TextFieldWidget aliasField;
    private TextFieldWidget messageField;
    private TextFieldWidget commandField;
    private Contact selectedContact;
    private int scroll;
    private String draft = "";

    public BetterchatScreen() {
        super(Text.literal("Betterchat"));
    }

    @Override
    protected void init() {
        Optional<Contact> focused = contacts.focusedContact();
        selectedContact = focused.orElseGet(() -> contacts.sortedContacts().stream().findFirst().orElse(null));

        Layout layout = layout();
        int leftX = layout.x + 16;
        int contentX = layout.x + layout.leftW + 22;
        int contentW = layout.w - layout.leftW - 38;

        playerField = new TextFieldWidget(textRenderer, leftX, layout.y + layout.h - 80, 78, 20, Text.literal("Jogador"));
        playerField.setPlaceholder(Text.literal("jogador"));
        aliasField = new TextFieldWidget(textRenderer, leftX + 84, layout.y + layout.h - 80, 88, 20, Text.literal("Apelido"));
        aliasField.setPlaceholder(Text.literal("apelido"));
        commandField = new TextFieldWidget(textRenderer, contentX, layout.y + 56, 80, 20, Text.literal("Comando"));
        commandField.setText(contacts.privateMessageCommand());
        commandField.setPlaceholder(Text.literal("msg"));
        messageField = new TextFieldWidget(textRenderer, contentX, layout.y + layout.h - 44, Math.max(80, contentW - 86), 20, Text.literal("Mensagem"));
        messageField.setPlaceholder(Text.literal("mensagem privada"));
        messageField.setText(contacts.keepDraft() ? draft : "");

        addDrawableChild(playerField);
        addDrawableChild(aliasField);
        addDrawableChild(commandField);
        addDrawableChild(messageField);

        addDrawableChild(ButtonWidget.builder(Text.literal("+ Novo"), button -> addContact())
                .dimensions(leftX, layout.y + layout.h - 54, 62, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Remover"), button -> removeSelected())
                .dimensions(leftX + 68, layout.y + layout.h - 54, 78, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cmd"), button -> saveCommand())
                .dimensions(contentX + 86, layout.y + 56, 48, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(toggleText("Som", contacts.playSounds()), button -> toggleSounds(button))
                .dimensions(contentX + 144, layout.y + 56, 66, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(toggleText("Fechar", contacts.closeAfterSend()), button -> toggleClose(button))
                .dimensions(contentX + 216, layout.y + 56, 82, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(toggleText("Rascunho", contacts.keepDraft()), button -> toggleDraft(button))
                .dimensions(contentX + 304, layout.y + 56, 92, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Enviar"), button -> sendMessage())
                .dimensions(contentX + contentW - 78, layout.y + layout.h - 44, 78, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> closeWithSound())
                .dimensions(layout.x + layout.w - 34, layout.y + 12, 20, 20)
                .build());

        rebuildContactButtons();
        setInitialFocus(messageField);
    }

    private void rebuildContactButtons() {
        for (ButtonWidget button : contactButtons) {
            remove(button);
        }
        contactButtons.clear();

        Layout layout = layout();
        List<Contact> list = contacts.sortedContacts();
        int visible = Math.max(1, (layout.h - 148) / 24);
        int maxScroll = Math.max(0, list.size() - visible);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        for (int i = 0; i < Math.min(visible, list.size() - scroll); i++) {
            Contact contact = list.get(i + scroll);
            int unread = contacts.unreadCount(contact);
            String prefix = contacts.isFocused(contact) ? "> " : unread > 0 ? "! " : "";
            String suffix = unread > 0 ? " (" + unread + ")" : "";
            ButtonWidget button = ButtonWidget.builder(Text.literal(prefix + contact.label() + suffix), ignored -> {
                        playClick(1.12F);
                        selectedContact = contact;
                        contacts.focus(contact);
                        messageField.setFocused(true);
                        rebuildContactButtons();
                    })
                    .dimensions(layout.x + 16, layout.y + 74 + i * 24, layout.leftW - 32, 20)
                    .build();
            contactButtons.add(addDrawableChild(button));
        }
    }

    private void addContact() {
        String player = playerField.getText().trim();
        if (player.isEmpty()) {
            playClick(0.6F);
            return;
        }
        selectedContact = contacts.add(player, aliasField.getText().trim());
        contacts.focus(selectedContact);
        playerField.setText("");
        aliasField.setText("");
        playClick(1.25F);
        rebuildContactButtons();
    }

    private void removeSelected() {
        if (selectedContact == null) {
            playClick(0.6F);
            return;
        }
        contacts.remove(selectedContact.name);
        selectedContact = contacts.sortedContacts().stream().findFirst().orElse(null);
        if (selectedContact != null) {
            contacts.focus(selectedContact);
        }
        playClick(0.8F);
        rebuildContactButtons();
    }

    private void saveCommand() {
        contacts.setPrivateMessageCommand(commandField.getText());
        commandField.setText(contacts.privateMessageCommand());
        playClick(1.3F);
    }

    private void toggleSounds(ButtonWidget button) {
        contacts.setPlaySounds(!contacts.playSounds());
        button.setMessage(toggleText("Som", contacts.playSounds()));
        playClick(1.0F);
    }

    private void toggleClose(ButtonWidget button) {
        contacts.setCloseAfterSend(!contacts.closeAfterSend());
        button.setMessage(toggleText("Fechar", contacts.closeAfterSend()));
        playClick(1.0F);
    }

    private void toggleDraft(ButtonWidget button) {
        contacts.setKeepDraft(!contacts.keepDraft());
        button.setMessage(toggleText("Rascunho", contacts.keepDraft()));
        playClick(1.0F);
    }

    private void sendMessage() {
        if (selectedContact == null) {
            playClick(0.6F);
            return;
        }
        String message = messageField.getText().trim();
        if (BetterchatClient.sendPrivateMessage(selectedContact, message)) {
            draft = "";
            messageField.setText("");
            contacts.focus(selectedContact);
            playSend();
            rebuildContactButtons();
            if (contacts.closeAfterSend()) {
                close();
            }
        } else {
            playClick(0.6F);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (messageField.isFocused()) {
                sendMessage();
                return true;
            }
            if (commandField.isFocused()) {
                saveCommand();
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            cycleContact((modifiers & GLFW.GLFW_MOD_SHIFT) == 0 ? 1 : -1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean handled = super.charTyped(chr, modifiers);
        draft = messageField.getText();
        return handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int oldScroll = scroll;
        scroll -= (int) Math.signum(verticalAmount);
        rebuildContactButtons();
        if (oldScroll != scroll) {
            playClick(0.9F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x44000000);
        Layout layout = layout();
        int contentX = layout.x + layout.leftW + 22;
        int contentW = layout.w - layout.leftW - 38;

        fillPanel(context, layout.x, layout.y, layout.w, layout.h);
        drawSidePanel(context, layout);
        drawHeader(context, layout, contentX);
        drawOptions(context, layout, contentX);
        drawConversation(context, layout, contentX, contentW);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSidePanel(DrawContext context, Layout layout) {
        context.fill(layout.x + 10, layout.y + 48, layout.x + layout.leftW - 10, layout.y + layout.h - 92, PANEL_SOFT);
        context.fill(layout.x + layout.leftW, layout.y + 16, layout.x + layout.leftW + 1, layout.y + layout.h - 16, 0x55FFFFFF);
        context.drawText(textRenderer, Text.literal("Contatos"), layout.x + 16, layout.y + 52, TEXT_MUTED, false);
        if (contacts.sortedContacts().isEmpty()) {
            context.drawText(textRenderer, Text.literal("Sem contatos ainda"), layout.x + 22, layout.y + 82, TEXT_DIM, false);
            context.drawText(textRenderer, Text.literal("Adicione abaixo."), layout.x + 22, layout.y + 94, TEXT_DIM, false);
        }
        context.drawText(textRenderer, Text.literal("Novo contato"), layout.x + 16, layout.y + layout.h - 96, TEXT_MUTED, false);
    }

    private void drawHeader(DrawContext context, Layout layout, int contentX) {
        context.drawText(textRenderer, Text.literal("Betterchat"), layout.x + 16, layout.y + 16, TEXT_MAIN, true);
        context.drawText(textRenderer, Text.literal("chat privado rapido"), layout.x + 16, layout.y + 30, TEXT_MUTED, false);
        context.drawText(textRenderer, Text.literal("Painel"), contentX, layout.y + 16, TEXT_MAIN, true);
        context.drawText(textRenderer, Text.literal("P abre | Enter envia | roda do mouse navega"), contentX, layout.y + 31, TEXT_MUTED, false);
    }

    private void drawOptions(DrawContext context, Layout layout, int contentX) {
        context.drawText(textRenderer, Text.literal("Comando base"), contentX, layout.y + 44, TEXT_MUTED, false);
        String commandPreview = "/" + contacts.privateMessageCommand() + " " + (selectedContact == null ? "<jogador>" : selectedContact.name) + " <mensagem>";
        context.drawText(textRenderer, Text.literal(commandPreview), contentX, layout.y + 82, 0xFF8EE3FF, false);
    }

    private void drawConversation(DrawContext context, Layout layout, int contentX, int contentW) {
        int boxY = layout.y + 104;
        int boxH = layout.h - 164;
        context.fill(contentX, boxY, layout.x + layout.w - 16, boxY + boxH, 0x4419232F);
        context.fill(contentX, boxY, layout.x + layout.w - 16, boxY + 1, ACCENT);

        if (selectedContact == null) {
            drawEmptyState(context, contentX, boxY, contentW);
            return;
        }

        context.drawText(textRenderer, Text.literal(selectedContact.label()), contentX + 12, boxY + 12, TEXT_MAIN, true);
        context.drawText(textRenderer, Text.literal("Destino: " + selectedContact.name), contentX + 12, boxY + 27, TEXT_MUTED, false);

        List<ChatRecord> records = contacts.historyFor(selectedContact, 7);
        if (records.isEmpty()) {
            context.drawText(textRenderer, Text.literal("Nenhuma mensagem local ainda."), contentX + 12, boxY + 54, TEXT_DIM, false);
        } else {
            int y = boxY + 52;
            for (ChatRecord record : records) {
                context.drawText(textRenderer, Text.literal(trim(record.displayLine(), contentW - 34)), contentX + 12, y, 0xFFE8F5FF, false);
                y += 14;
            }
        }
    }

    private void drawEmptyState(DrawContext context, int contentX, int boxY, int contentW) {
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Nenhum contato selecionado"), contentX + contentW / 2, boxY + 54, TEXT_MAIN);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Adicione um jogador ou selecione na lista."), contentX + contentW / 2, boxY + 74, TEXT_MUTED);
    }

    private void cycleContact(int direction) {
        List<Contact> list = contacts.sortedContacts();
        if (list.isEmpty()) {
            return;
        }
        int index = selectedContact == null ? -1 : list.indexOf(selectedContact);
        int next = Math.floorMod(index + direction, list.size());
        selectedContact = list.get(next);
        contacts.focus(selectedContact);
        playClick(1.15F);
        rebuildContactButtons();
    }

    private void closeWithSound() {
        playClose();
        close();
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + ": " + (enabled ? "SIM" : "NAO"));
    }

    private String trim(String value, int maxWidth) {
        String result = value;
        while (textRenderer.getWidth(result) > maxWidth && result.length() > 4) {
            result = result.substring(0, result.length() - 4) + "...";
        }
        return result;
    }

    private void fillPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x + 2, y, x + width - 2, y + height, PANEL);
        context.fill(x, y + 2, x + width, y + height - 2, PANEL);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_STRONG);
        context.fill(x + 1, y + 1, x + width - 1, y + 3, ACCENT);
        context.fill(x + 1, y + height - 3, x + width - 1, y + height - 1, 0x554FE6A6);
    }

    private void playClick(float pitch) {
        if (contacts.playSounds() && client != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, pitch));
        }
    }

    private void playSend() {
        if (contacts.playSounds() && client != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_IN, 1.15F));
        }
    }

    private void playClose() {
        if (contacts.playSounds() && client != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_OUT, 1.0F));
        }
    }

    private Layout layout() {
        int margin = 18;
        int maxW = Math.max(300, width - margin * 2);
        int maxH = Math.max(240, height - margin * 2);
        int w = Math.min(maxW, 720);
        int h = Math.min(maxH, 390);
        return new Layout((width - w) / 2, (height - h) / 2, w, h, Math.min(210, w / 3));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private record Layout(int x, int y, int w, int h, int leftW) {
    }
}

