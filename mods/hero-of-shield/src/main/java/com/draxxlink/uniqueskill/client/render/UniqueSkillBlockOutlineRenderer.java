package com.draxxlink.uniqueskill.client.render;

import com.draxxlink.uniqueskill.config.UniqueSkillConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public final class UniqueSkillBlockOutlineRenderer {
    private UniqueSkillBlockOutlineRenderer() {
    }

    public static void register() {
        WorldRenderEvents.BLOCK_OUTLINE.register(UniqueSkillBlockOutlineRenderer::renderOutline);
    }

    private static boolean renderOutline(WorldRenderContext context, WorldRenderContext.BlockOutlineContext blockOutlineContext) {
        UniqueSkillConfig config = UniqueSkillConfig.getInstance();

        if (!config.blockOutlineEnabled || context.matrixStack() == null || context.consumers() == null) {
            return true;
        }

        if (context.translucentBlockOutline()) {
            return false;
        }

        VoxelShape outlineShape = blockOutlineContext.blockState().getOutlineShape(context.world(), blockOutlineContext.blockPos());
        if (outlineShape.isEmpty()) {
            return false;
        }

        float[] color = resolveColor(config);
        VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayer.getLines());

        for (Box shapeBox : outlineShape.getBoundingBoxes()) {
            Box worldBox = shapeBox.offset(blockOutlineContext.blockPos())
                .offset(-blockOutlineContext.cameraX(), -blockOutlineContext.cameraY(), -blockOutlineContext.cameraZ())
                .expand(0.002D);
            VertexRendering.drawBox(context.matrixStack(), vertexConsumer, worldBox, color[0], color[1], color[2], config.blockOutlineAlpha);
        }

        return false;
    }

    private static float[] resolveColor(UniqueSkillConfig config) {
        return switch (config.blockOutlineColor) {
            case "GOLD" -> new float[] {0.95F, 0.79F, 0.26F};
            case "MAGENTA" -> new float[] {0.92F, 0.34F, 0.95F};
            case "WHITE" -> new float[] {0.97F, 0.97F, 0.97F};
            default -> new float[] {0.30F, 0.96F, 0.95F};
        };
    }
}
