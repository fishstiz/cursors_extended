package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.CrafterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

@Mixin(CrafterScreen.class)
public abstract class CrafterScreenMixin extends AbstractContainerScreenMixin<CrafterMenu> {
    protected CrafterScreenMixin(Component title) {
        super(title);
    }

    @ModifyArg(method = "extractSlot", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;requestCursor(Lcom/mojang/blaze3d/platform/cursor/CursorType;)V"
    ))
    private CursorType onRequestCursor(
            CursorType cursorType,
            @Local(argsOnly = true, ordinal = 0) int mouseX,
            @Local(argsOnly = true, ordinal = 1) int mouseY
    ) {
        if (CONFIG.isLegacyMode()) {
            CursorType provided = cursors_extended$cursorType(mouseX, mouseY);
            if (CursorTypeUtil.nonDefault(provided)) {
                return provided;
            }
        }
        return cursorType;
    }
}
