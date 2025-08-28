package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementScreenMixin {
    @Shadow
    @Nullable
    private AdvancementTab selectedTab;

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
            Operation<Boolean> original,
            @Local(argsOnly = true) GuiGraphics guiGraphics
    ) {
        if (original.call(tab, offsetX, offsetY, mouseX, mouseY)) {
            if (CursorsExtended.CONFIG.isAdvancementTabsEnabled() && tab != this.selectedTab) {
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
            }
            return true;
        }
        return false;
    }
}
