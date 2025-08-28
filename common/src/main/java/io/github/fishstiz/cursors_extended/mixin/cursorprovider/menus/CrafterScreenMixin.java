package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CrafterSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CrafterScreen.class)
public abstract class CrafterScreenMixin extends AbstractContainerScreenMixin<CrafterMenu> {
    @Shadow
    @Final
    private Player player;

    protected CrafterScreenMixin(Component title) {
        super(title);
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (this.hoveredSlot instanceof CrafterSlot crafterSlot
            && this.menu.getCarried().isEmpty()
            && !crafterSlot.hasItem()
            && !player.isSpectator()) {
            return CursorTypes.POINTING_HAND;
        }
        return super.cursors_extended$cursorType(mouseX, mouseY);
    }
}
