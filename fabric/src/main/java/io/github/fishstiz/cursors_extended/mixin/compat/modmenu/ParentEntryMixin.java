package io.github.fishstiz.cursors_extended.mixin.compat.modmenu;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.terraformersmc.modmenu.gui.widget.entries.ParentEntry;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(value = ParentEntry.class, remap = false)
public class ParentEntryMixin implements CursorProvider {
    @Shadow
    protected boolean hoveringIcon;

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        return this.hoveringIcon ? CursorTypes.POINTING_HAND : CursorType.DEFAULT;
    }
}
