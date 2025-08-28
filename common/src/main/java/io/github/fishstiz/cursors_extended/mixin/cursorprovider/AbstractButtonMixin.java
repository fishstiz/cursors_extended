package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin extends AbstractWidget implements CursorProvider {
    protected AbstractButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (this.isHovered()) {
            if (this.isActive()) {
                return CursorTypes.POINTING_HAND;
            } else {
                return CursorTypes.NOT_ALLOWED;
            }
        }
        return CursorTypes.ARROW;
    }
}
