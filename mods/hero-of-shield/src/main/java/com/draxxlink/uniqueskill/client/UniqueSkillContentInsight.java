package com.draxxlink.uniqueskill.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public final class UniqueSkillContentInsight {
    private static final long RESCAN_INTERVAL_TICKS = 40L;

    private static long nextRescanTick;
    private static String topNamespace = "";
    private static int topNamespaceItemCount;
    private static final Set<String> observedCustomIds = new HashSet<>();
    private static final Map<String, Integer> namespaceCounts = new HashMap<>();

    private UniqueSkillContentInsight() {
    }

    public static void tick(MinecraftClient client, long worldTick) {
        if (client.player == null || worldTick < nextRescanTick) {
            return;
        }

        nextRescanTick = worldTick + RESCAN_INTERVAL_TICKS;
        observeScreenHandler(client.player.currentScreenHandler);
    }

    public static void reset() {
        nextRescanTick = 0L;
        topNamespace = "";
        topNamespaceItemCount = 0;
        observedCustomIds.clear();
        namespaceCounts.clear();
    }

    public static boolean hasCustomProfile() {
        return !topNamespace.isBlank() && !observedCustomIds.isEmpty();
    }

    public static Text serverProfileText() {
        if (!hasCustomProfile()) {
            return Text.translatable("hud.unique_skill.server_profile.none");
        }

        return Text.translatable("hud.unique_skill.server_profile.line", topNamespace, observedCustomIds.size());
    }

    public static Text stackSourceText(ItemStack stack) {
        observeStack(stack);
        Identifier id = stackId(stack);
        if (id == null) {
            return Text.translatable("hud.unique_skill.source.unknown");
        }

        return Text.translatable("hud.unique_skill.source.line", id.getNamespace());
    }

    public static Text stackIdText(ItemStack stack) {
        observeStack(stack);
        Identifier id = stackId(stack);
        return id == null
            ? Text.translatable("hud.unique_skill.source.id", "--")
            : Text.translatable("hud.unique_skill.source.id", id.toString());
    }

    public static boolean isCustomStack(ItemStack stack) {
        Identifier id = stackId(stack);
        return id != null && !"minecraft".equals(id.getNamespace());
    }

    public static void observeStack(ItemStack stack) {
        Identifier id = stackId(stack);
        if (id == null || "minecraft".equals(id.getNamespace())) {
            return;
        }

        if (observedCustomIds.add(id.toString())) {
            namespaceCounts.merge(id.getNamespace(), 1, Integer::sum);
            recomputeTopNamespace();
        }
    }

    private static Identifier stackId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        return Registries.ITEM.getId(stack.getItem());
    }

    public static String stackIdString(ItemStack stack) {
        Identifier id = stackId(stack);
        return id == null ? null : id.toString();
    }

    private static void observeScreenHandler(ScreenHandler handler) {
        if (handler == null) {
            return;
        }

        handler.slots.forEach(slot -> observeStack(slot.getStack()));
    }

    private static void recomputeTopNamespace() {
        Map.Entry<String, Integer> topEntry = namespaceCounts.entrySet()
            .stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .orElse(null);

        if (topEntry == null) {
            topNamespace = "";
            topNamespaceItemCount = 0;
            return;
        }

        topNamespace = topEntry.getKey();
        topNamespaceItemCount = topEntry.getValue();
    }
}
