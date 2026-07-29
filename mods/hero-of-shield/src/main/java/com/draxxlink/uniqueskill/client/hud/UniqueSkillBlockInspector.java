package com.draxxlink.uniqueskill.client.hud;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class UniqueSkillBlockInspector {
    private UniqueSkillBlockInspector() {
    }

    public static BlockInspectorData inspect(MinecraftClient client) {
        if (client.world == null || client.player == null || !(client.crosshairTarget instanceof BlockHitResult blockHitResult)) {
            return null;
        }

        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = client.world.getBlockState(blockPos);
        if (blockState.isAir()) {
            return null;
        }

        Block block = blockState.getBlock();
        Item iconItem = block.asItem();
        ItemStack iconStack = iconItem == Items.AIR ? new ItemStack(Items.STONE) : new ItemStack(iconItem);
        List<StyledLine> lines = new ArrayList<>();
        String blockId = Registries.BLOCK.getId(block).toString();

        lines.add(new StyledLine(Text.translatable("hud.unique_skill.block.position", blockPos.getX(), blockPos.getY(), blockPos.getZ()), 0xFFE77356));
        lines.add(new StyledLine(Text.literal(blockId), 0xFFE2B454));
        lines.add(new StyledLine(Text.translatable("hud.unique_skill.block.separator"), 0xFF8A6B45));
        lines.add(new StyledLine(Text.translatable("hud.unique_skill.block.blast", formatDecimal(block.getBlastResistance())), 0xFFF0D28B));
        lines.add(new StyledLine(Text.translatable("hud.unique_skill.block.hardness", formatDecimal(blockState.getHardness(client.world, blockPos))), 0xFFD8B46F));
        lines.add(new StyledLine(Text.translatable("hud.unique_skill.block.light", Integer.toString(blockState.getLuminance())), 0xFFFF9C5B));
        lines.add(new StyledLine(Text.translatable("hud.unique_skill.block.separator"), 0xFF8A6B45));
        lines.add(new StyledLine(Text.translatable("hud.unique_skill.block.face", humanizeDirection(blockHitResult.getSide())), 0xFFF3D48A));

        int propertyCount = 0;
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getEntries().entrySet()) {
            if (propertyCount >= 4) {
                break;
            }

            String propertyName = humanizeProperty(entry.getKey().getName());
            String propertyValue = humanizeProperty(entry.getValue().toString());
            lines.add(new StyledLine(
                Text.translatable("hud.unique_skill.block.property", propertyName, propertyValue),
                propertyColor(entry.getValue())
            ));
            propertyCount++;
        }

        if (propertyCount == 0) {
            lines.add(new StyledLine(Text.translatable("hud.unique_skill.block.no_properties"), 0xFFD7C8AD));
        }

        return new BlockInspectorData(iconStack, block.getName(), lines);
    }

    private static String formatDecimal(float value) {
        return value == Math.rint(value) ? Integer.toString((int) value) : String.format(Locale.ROOT, "%.1f", value);
    }

    private static String humanizeProperty(String value) {
        String normalized = value.replace('_', ' ').trim();
        if (normalized.isEmpty()) {
            return value;
        }

        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static String humanizeDirection(Direction direction) {
        return switch (direction) {
            case DOWN -> Text.translatable("hud.unique_skill.direction.down").getString();
            case UP -> Text.translatable("hud.unique_skill.direction.up").getString();
            case NORTH -> Text.translatable("hud.unique_skill.direction.north").getString();
            case SOUTH -> Text.translatable("hud.unique_skill.direction.south").getString();
            case WEST -> Text.translatable("hud.unique_skill.direction.west").getString();
            case EAST -> Text.translatable("hud.unique_skill.direction.east").getString();
        };
    }

    private static int propertyColor(Comparable<?> value) {
        String text = value.toString().toLowerCase(Locale.ROOT);
        if ("true".equals(text)) {
            return 0xFFE1C16D;
        }
        if ("false".equals(text)) {
            return 0xFFD7604D;
        }
        return 0xFFF3DEC0;
    }

    public record BlockInspectorData(ItemStack iconStack, Text title, List<StyledLine> lines) {
    }

    public record StyledLine(Text text, int color) {
    }
}
