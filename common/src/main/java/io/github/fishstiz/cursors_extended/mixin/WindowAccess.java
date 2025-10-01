package io.github.fishstiz.cursors_extended.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Window.class)
public interface WindowAccess {
    @Accessor("currentCursor")
    CursorType cursors_extended$getCurrentCursor();
}
