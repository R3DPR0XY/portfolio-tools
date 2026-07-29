package com.bmod.chestpanel.mixin;

import com.bmod.chestpanel.client.ChestPanelRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    @Inject(method = "render", at = @At("TAIL"))
    private void lootPanel$renderChestPanel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HandledScreen<T> screen = (HandledScreen<T>) (Object) this;
        ChestPanelRenderer.render(
            context,
            screen.getScreenHandler(),
            screen.width,
            screen.height,
            x,
            y,
            backgroundWidth,
            backgroundHeight,
            screen.getTitle().getString(),
            mouseX,
            mouseY
        );
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void lootPanel$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<T> screen = (HandledScreen<T>) (Object) this;
        if (ChestPanelRenderer.onKeyPressed(screen.getScreenHandler(), keyCode)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void lootPanel$mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<T> screen = (HandledScreen<T>) (Object) this;
        if (ChestPanelRenderer.onMouseScrolled(screen.getScreenHandler(), mouseX, mouseY, verticalAmount)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void lootPanel$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<T> screen = (HandledScreen<T>) (Object) this;
        if (ChestPanelRenderer.onMouseClicked(screen.getScreenHandler(), mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
}
