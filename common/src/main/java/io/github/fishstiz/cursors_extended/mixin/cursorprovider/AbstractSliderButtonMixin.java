package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractSliderButton.class)
public abstract class AbstractSliderButtonMixin extends AbstractWidget implements CursorProvider {
    @Shadow
    private boolean dragging;

    protected AbstractSliderButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (this.isHovered()) {
            if (this.isActive()) {
                return this.dragging ? CursorTypesExt.RESIZE_EW_HOLD : CursorTypes.POINTING_HAND;
            } else {
                return CursorTypes.NOT_ALLOWED;
            }
        }
        return CursorTypes.ARROW;
    }

    @ModifyArg(method = "handleCursor", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;requestCursor(Lcom/mojang/blaze3d/platform/cursor/CursorType;)V"
    ))
    private CursorType setCursor(CursorType cursorType) {
        if (CursorsExtended.CONFIG.isLegacyMode()) {
            if (this.isActive()) {
                return this.dragging ? CursorTypesExt.RESIZE_EW_HOLD : CursorTypes.POINTING_HAND;
            } else {
                return CursorTypes.NOT_ALLOWED;
            }
        }
        return cursorType;
    }
}
