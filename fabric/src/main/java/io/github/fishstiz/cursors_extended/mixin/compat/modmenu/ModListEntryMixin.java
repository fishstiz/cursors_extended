package io.github.fishstiz.cursors_extended.mixin.compat.modmenu;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(value = ModListEntry.class, remap = false)
public abstract class ModListEntryMixin extends ObjectSelectionList.Entry<ModListEntry> implements CursorProvider {
    @Shadow
    @Final
    protected static int COMPACT_ICON_SIZE;

    @Shadow
    @Final
    protected static int FULL_ICON_SIZE;

    @Shadow
    @Final
    protected ModListWidget list;

    @Shadow
    @Final
    public Mod mod;

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (ModMenuConfig.QUICK_CONFIGURE.getValue()) {
            int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
            if (this.list.getParent().getModHasConfigScreen(this.mod.getId())
                && mouseX >= this.getX()
                && mouseX - this.getContentX() <= iconSize) {
                return CursorTypes.POINTING_HAND;
            }
        }
        return CursorType.DEFAULT;
    }
}
