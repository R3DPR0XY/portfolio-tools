package com.r3dpr0xy.charactersystem;

import com.r3dpr0xy.charactersystem.model.CharacterProfile;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class AttributeService {
    private final CharacterSystemPlugin plugin;

    public AttributeService(CharacterSystemPlugin plugin) {
        this.plugin = plugin;
    }

    public void apply(Player player, CharacterProfile character) {
        int height = character.height();
        int reference = plugin.getConfig().getInt("height.reference", 170);
        int delta = height - reference;

        applyAttribute(player, "health", delta, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
        applyAttribute(player, "speed", delta, "MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED");
        applyAttribute(player, "entity-interaction-range", delta, "ENTITY_INTERACTION_RANGE", "PLAYER_ENTITY_INTERACTION_RANGE");
        if (plugin.getConfig().getBoolean("attributes.scale.enabled", true)) {
            applyAttribute(player, "scale", delta, "SCALE", "GENERIC_SCALE");
        }

        AttributeInstance maxHealth = findAttribute(player, "MAX_HEALTH", "GENERIC_MAX_HEALTH").orElse(null);
        if (maxHealth != null && player.getHealth() > maxHealth.getBaseValue()) {
            player.setHealth(maxHealth.getBaseValue());
        }
    }

    public void clear(Player player) {
        reset(player, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
        reset(player, "MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED");
        reset(player, "ENTITY_INTERACTION_RANGE", "PLAYER_ENTITY_INTERACTION_RANGE");
        reset(player, "SCALE", "GENERIC_SCALE");
    }

    public double calculatedValue(String configKey, int height) {
        int reference = plugin.getConfig().getInt("height.reference", 170);
        int delta = height - reference;
        String path = "attributes." + configKey + ".";
        double base = plugin.getConfig().getDouble(path + "base");
        double perCentimeter = plugin.getConfig().getDouble(path + "per-centimeter");
        double min = plugin.getConfig().getDouble(path + "min");
        double max = plugin.getConfig().getDouble(path + "max");
        return clamp(base + (delta * perCentimeter), min, max);
    }

    private void applyAttribute(Player player, String configKey, int delta, String... names) {
        Optional<AttributeInstance> attribute = findAttribute(player, names);
        if (attribute.isEmpty()) {
            return;
        }
        String path = "attributes." + configKey + ".";
        double base = plugin.getConfig().getDouble(path + "base");
        double perCentimeter = plugin.getConfig().getDouble(path + "per-centimeter");
        double min = plugin.getConfig().getDouble(path + "min");
        double max = plugin.getConfig().getDouble(path + "max");
        attribute.get().setBaseValue(clamp(base + (delta * perCentimeter), min, max));
    }

    private Optional<AttributeInstance> findAttribute(Player player, String... names) {
        for (String name : names) {
            Attribute attribute = attribute(name);
            if (attribute == null) {
                continue;
            }
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                return Optional.of(instance);
            }
        }
        return Optional.empty();
    }

    private void reset(Player player, String... names) {
        findAttribute(player, names).ifPresent(attribute -> attribute.setBaseValue(attribute.getDefaultValue()));
    }

    private Attribute attribute(String name) {
        try {
            return Attribute.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
