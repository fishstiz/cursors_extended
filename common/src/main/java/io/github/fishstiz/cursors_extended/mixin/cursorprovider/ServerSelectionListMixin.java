package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus.access.OnlineServerEntryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin extends ObjectSelectionList<ServerSelectionList.Entry> implements CursorProvider {
    @Unique
    private static final int cursors_extended$ICON_WIDTH = 32;

    @Unique
    private static final int cursors_extended$MOVE_ICON_SIZE = 16;

    protected ServerSelectionListMixin(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (CursorsExtended.CONFIG.isServerIconEnabled()
            && mouseX < this.getRowLeft() + cursors_extended$ICON_WIDTH
            && this.getEntryAtPosition(mouseX, mouseY) instanceof ServerSelectionList.OnlineServerEntry serverEntry) {
            OnlineServerEntryAccessor accessor = (OnlineServerEntryAccessor) serverEntry;
            int i = this.children().indexOf(serverEntry);
            double relativeX = mouseX - this.getRowLeft();
            double relativeY = mouseY - this.getRowTop(i);

            if (relativeX < cursors_extended$ICON_WIDTH && relativeX > cursors_extended$MOVE_ICON_SIZE && accessor.invokeCanJoin()) {
                return CursorTypes.POINTING_HAND;
            }
            if (relativeX < cursors_extended$MOVE_ICON_SIZE && relativeY < cursors_extended$MOVE_ICON_SIZE && i > 0) {
                return CursorTypes.POINTING_HAND;
            }
            if (relativeX < cursors_extended$MOVE_ICON_SIZE && relativeY > cursors_extended$MOVE_ICON_SIZE && i < accessor.getScreen().getServers().size() - 1) {
                return CursorTypes.POINTING_HAND;
            }
        }
        return CursorType.DEFAULT;
    }
}
