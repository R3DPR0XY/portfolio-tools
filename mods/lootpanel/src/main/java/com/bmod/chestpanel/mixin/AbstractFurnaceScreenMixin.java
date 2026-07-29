package com.bmod.chestpanel.mixin;

import com.bmod.chestpanel.client.ChestPanelRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin<T extends AbstractFurnaceScreenHandler> extends HandledScreen<T> {
    protected AbstractFurnaceScreenMixin(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"), require = 0)
    private void lootPanel$drawFurnacePanel(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        ChestPanelRenderer.renderFurnaceOverlay(
            context,
            this.handler,
            this.width,
            this.height,
            this.x,
            this.y,
            this.backgroundWidth,
            this.backgroundHeight,
            this.getTitle().getString(),
            mouseX,
            mouseY
        );
    }
}
