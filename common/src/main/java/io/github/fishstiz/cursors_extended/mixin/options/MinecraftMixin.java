package io.github.fishstiz.cursors_extended.mixin.options;

import com.mojang.blaze3d.platform.Window;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private Window window;

    @Unique
    private int cursors_extended$previousGuiScale;

    @Inject(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;initBackendSystem()Lnet/minecraft/util/TimeSource$NanoTimeSource;",
            shift = At.Shift.AFTER,
            unsafe = true
    ))
    private void onInitRenderSystem(GameConfig gameConfig, CallbackInfo ci) {
        CursorsExtended.getInstance().getRegistry().onInitRenderSystem();
    }

    @Inject(method = "resizeGui", at = @At("TAIL"))
    private void reloadCursorsOnResize(CallbackInfo ci) {
        int guiScale = this.window.getGuiScale();

        if (this.cursors_extended$previousGuiScale != guiScale) {
            this.cursors_extended$previousGuiScale = guiScale;

            CursorsExtended.getInstance().getRegistry().getCursors().forEach(cursor -> {
                if (cursor.isCustom() || !CursorsExtended.CONFIG.getOrCreateSettings(cursor).enabled()) return;

                CursorTexture texture = cursor.getTexture();
                if (texture != null && SettingsUtil.isAutoScale(cursor.getTexture().scale())) {
                    CursorsExtended.getInstance().getLoader().updateTexture(cursor, texture.scale(), texture.xhot(), texture.yhot());
                }
            });
        }
    }
}
