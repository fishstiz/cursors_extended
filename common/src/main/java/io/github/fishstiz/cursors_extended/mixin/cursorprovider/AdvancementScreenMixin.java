package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementScreenMixin {
    @Shadow
    @Nullable
    private AdvancementTab selectedTab;

    @Unique
    private CursorType cursors_extended$tabCursorType;

    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;renderWindow(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            shift = At.Shift.AFTER
    ))
    private void requestTabCursorType(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.cursors_extended$tabCursorType != null) {
            guiGraphics.requestCursor(this.cursors_extended$tabCursorType);
            this.cursors_extended$tabCursorType = null;
        }
    }

    @WrapOperation(method = "renderTooltips", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;isMouseOver(IIDD)Z"
    ))
    private boolean setPointerOnHover(
            AdvancementTab tab,
            int offsetX,
            int offsetY,
            double mouseX,
            double mouseY,
            Operation<Boolean> original
    ) {
        boolean hovered = original.call(tab, offsetX, offsetY, mouseX, mouseY);
        if (!CursorsExtended.CONFIG.isAdvancementTabsEnabled()) {
            return hovered;
        }
        if (hovered && tab != this.selectedTab) {
            this.cursors_extended$tabCursorType = CursorTypes.POINTING_HAND;
        }
        return hovered;
    }
}
