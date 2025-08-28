package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.mixin.WindowAccess;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class CursorResolver {
    public static final CursorResolver INSTANCE = new CursorResolver();
    private CursorType deferredCursorType = null;
    private CursorType lastCursorType;

    private CursorResolver() {
    }

    private void setCurrentCursor(CursorType cursorType) {
        this.lastCursorType = cursorType;
        CursorManager.INSTANCE.setCurrentCursor(cursorType);
    }

    public void afterTick(Minecraft minecraft) {
        if (minecraft.screen == null && this.deferredCursorType == null) {
            this.setCurrentCursor(CursorTypeUtil.firstNonDefault(arrowOrDefault(minecraft), consumeTickCursors()));
        } else if (this.deferredCursorType == null && nonScreenCursorVisible(minecraft)) {
            this.setCurrentCursor(CursorTypeUtil.firstNonDefault(arrowOrDefault(minecraft), this.deferredCursorType));
        }
    }

    public void afterRenderScreen(Minecraft minecraft, Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (nonScreenCursorVisible(minecraft)) {
            CursorProviderInspector.INSTANCE.getInspector().render(minecraft, screen, guiGraphics, mouseX, mouseY);
            this.deferredCursorType = resolve(minecraft, screen, mouseX, mouseY);
        }
    }

    public void afterRenderTooltip(Minecraft minecraft, Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        CursorProviderInspector.INSTANCE.getInspector().render(minecraft, screen, guiGraphics, mouseX, mouseY);
    }

    public void beforeApplyCursor(Minecraft minecraft, Screen screen, int mouseX, int mouseY) {
        this.setCurrentCursor(resolve(minecraft, screen, mouseX, mouseY));
    }

    private CursorType resolve(Minecraft minecraft, Screen screen, int mouseX, int mouseY) {
        if (!((WindowAccess) (Object) minecraft.getWindow()).cursors_extended$isAdaptive()) {
            return CursorTypes.ARROW;
        }

        CursorType tickCursor = CursorTickController.INSTANCE.consumeTickCursor();
        CursorType fallbackTickCursor = CursorTickController.INSTANCE.consumeFallbackTickCursor();

        if (CursorTypeUtil.isHeld(this.lastCursorType)) {
            return this.lastCursorType;
        }
        if (CursorTypeUtil.nonDefault(tickCursor)) {
            return tickCursor;
        }
        if (CursorsExtended.CONFIG.isLegacyMode()) {
            CursorType inspected = CursorProviderInspector.INSTANCE.inspect(screen, mouseX, mouseY);
            if (CursorTypeUtil.nonDefault(inspected)) {
                return inspected;
            }
        }
        if (CursorTypeUtil.nonDefault(fallbackTickCursor)) {
            return fallbackTickCursor;
        }
        return CursorType.DEFAULT;
    }

    private static CursorType consumeTickCursors() {
        return CursorTypeUtil.firstNonDefault(
                CursorTickController.INSTANCE.consumeTickCursor(),
                CursorTickController.INSTANCE.consumeFallbackTickCursor()
        );
    }

    private static CursorType arrowOrDefault(Minecraft minecraft) {
        return !((WindowAccess) (Object) minecraft.getWindow()).cursors_extended$isAdaptive()
                ? CursorTypes.ARROW
                : CursorType.DEFAULT;
    }

    private static boolean nonScreenCursorVisible(Minecraft minecraft) {
        return minecraft.screen == null && !minecraft.mouseHandler.isMouseGrabbed();
    }
}
