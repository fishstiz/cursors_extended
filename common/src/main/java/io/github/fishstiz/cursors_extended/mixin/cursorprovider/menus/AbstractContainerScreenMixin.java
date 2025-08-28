package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements CursorProvider {
    @Shadow
    @Final
    protected T menu;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (menu.getCarried().isEmpty() &&
            hoveredSlot != null &&
            hoveredSlot.hasItem() &&
            hoveredSlot.isHighlightable()) {
            if (CursorTypeUtil.canShift()) {
                return CursorTypesExt.SHIFT;
            }
            if (CONFIG.isItemSlotEnabled()) {
                return CursorTypes.POINTING_HAND;
            }
        }
        if (CONFIG.isItemGrabbingEnabled() && !menu.getCarried().isEmpty()) {
            return CursorTypesExt.GRABBING;
        }
        return CursorType.DEFAULT;
    }
}
