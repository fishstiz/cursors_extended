package io.github.fishstiz.cursors_extended.mixin.compat.modmenu;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {"com.terraformersmc.modmenu.gui.widget.DescriptionListWidget$MojangCreditsEntry", "com.terraformersmc.modmenu.gui.widget.DescriptionListWidget$LinkEntry"}, remap = false)
public abstract class ModsScreenDescriptionEntryMixin implements CursorProvider {
    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        return CursorTypes.POINTING_HAND;
    }
}
