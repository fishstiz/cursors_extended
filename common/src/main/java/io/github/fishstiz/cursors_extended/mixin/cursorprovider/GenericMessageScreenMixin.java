package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GenericMessageScreen.class)
public abstract class GenericMessageScreenMixin implements CursorProvider {
    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        return CursorTypes.ARROW;
    }
}
