package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.mixin.WindowAccess;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class CursorListener {
    public static final CursorListener INSTANCE = new CursorListener();
    private CursorType deferredCursorType = null;
    private CursorType lastCursorType;

    private CursorListener() {
    }

    private void setCurrentCursor(CursorType cursorType) {
        this.lastCursorType = cursorType;
        CursorManager.INSTANCE.setCurrentCursor(cursorType);
    }

    public void afterTick(Minecraft minecraft) {
        if (minecraft.screen == null && this.deferredCursorType == null) {
            this.setCurrentCursor(CursorTypeUtil.firstNonDefault(arrowOrDefault(minecraft), consumeTickCursors()));
        } else if (this.deferredCursorType == null && nonScreenCursorVisible(minecraft)) {
            this.setCurrentCursor(this.deferredCursorType);
        }
    }

    public void afterScreenRender(Minecraft minecraft, Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (nonScreenCursorVisible(minecraft)) {
            CursorProviderInspector.INSTANCE.getInspector().render(minecraft, screen, guiGraphics, mouseX, mouseY);
            this.deferredCursorType = resolve(minecraft, screen, mouseX, mouseY);
        }
    }

    public void afterRenderTooltip(Minecraft minecraft, Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        CursorProviderInspector.INSTANCE.getInspector().render(minecraft, screen, guiGraphics, mouseX, mouseY);
        this.setCurrentCursor(resolve(minecraft, screen, mouseX, mouseY));
    }

    private CursorType resolve(Minecraft minecraft, Screen screen, int mouseX, int mouseY) {
        CursorType tickCursor = CursorTickController.INSTANCE.consumeTickCursor();
        CursorType fallbackTickCursor = CursorTickController.INSTANCE.consumeFallbackTickCursor();
        if (!((WindowAccess) (Object) minecraft.getWindow()).cursors_extended$isAdaptive()) {
            return CursorTypes.ARROW;
        }
        if (CursorTypeUtil.nonDefault(tickCursor)) {
            return tickCursor;
        }
        if (this.isGrabHeld()) {
            return CursorTypesExt.GRABBING_HOLD;
        }
        CursorType inspected = CursorProviderInspector.INSTANCE.inspect(screen, mouseX, mouseY);
        if (CursorTypeUtil.nonDefault(inspected)) {
            return inspected;
        }
        if (CursorTypeUtil.nonDefault(fallbackTickCursor)) {
            return fallbackTickCursor;
        }
        return CursorType.DEFAULT;
    }

    private boolean isGrabHeld() {
        return this.lastCursorType == CursorTypesExt.GRABBING_HOLD &&
               CursorManager.INSTANCE.isEnabled(CursorTypesExt.GRABBING_HOLD) &&
               CursorTypeUtil.nameEquals(CursorManager.INSTANCE.getAppliedCursor().getType(), CursorTypesExt.GRABBING_HOLD) &&
               CursorTypeUtil.isLeftClickHeld();
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
