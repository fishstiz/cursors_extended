package io.github.fishstiz.cursors_extended.gui.components;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.CursorState;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import io.github.fishstiz.fidgetz.v0.utils.FunctionUtils;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class CursorHotspotWidget extends CursorWidget {
    private static final Identifier BACKGROUND_128 = CursorsExtended.id("textures/gui/background_128.png");
    private static final int BACKGROUND_DISABLED = 0xAF000000; // 70% black
    private static final int RULER_COLOR = 0xFFFF0000; // red
    private static final int OVERRIDE_RULER_COLOR = 0xFF00FF00; // green
    private static final Component OVERFLOW_TEXT = Component.translatable("cursors_extended.options.image_too_large");
    private static final int OVERFLOW_COLOR = 0xFFFFFFFF; // white
    private final FZRef<CursorState.Hotspots> state;
    private final Consumer<CursorState.Hotspots> onChange;
    private final int maxXHot;
    private final int maxYHot;
    private Runnable onRelease = FunctionUtils.nop();
    private boolean globalMode;
    private boolean dragging = false;

    public CursorHotspotWidget(Cursor cursor, FZRef<CursorState.Hotspots> state, Consumer<CursorState.Hotspots> onChange) {
        super(0, 0, 128, 128, CommonComponents.ELLIPSIS, cursor, BACKGROUND_128);
        this.state = state;
        this.onChange = onChange;
        this.maxXHot = SettingsUtil.getMaxXHot(cursor);
        this.maxYHot = SettingsUtil.getMaxYHot(cursor);
    }

    public void setOnRelease(Runnable onRelease) {
        this.onRelease = onRelease;
    }

    public void setGlobalMode(boolean globalMode) {
        this.globalMode = globalMode;
    }

    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.active = !this.isOverflowing() && (globalMode == (CONFIG.getGlobal().isXHotActive() || CONFIG.getGlobal().isYHotActive()));
        super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTick);

        if (this.isHovered() && this.isOverflowing()) {
            Font font = Minecraft.getInstance().font;
            int textX = this.getX() + (this.getWidth() / 2 - font.width(OVERFLOW_TEXT) / 2);
            int textY = this.getY() + (this.getHeight() / 2 - font.lineHeight / 2);
            guiGraphics.text(font, OVERFLOW_TEXT, textX, textY, OVERFLOW_COLOR);
        }
    }

    @Override
    protected void renderBackground(@NonNull GuiGraphicsExtractor guiGraphics) {
        super.renderBackground(guiGraphics);

        if (!this.active) {
            guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), BACKGROUND_DISABLED);
        }
    }

    @Override
    protected void renderCursor(@NonNull GuiGraphicsExtractor guiGraphics, @NonNull Cursor cursor) {
        CursorRenderable.extractCursorState(guiGraphics, cursor, this.getX(), this.getY(), this.getWidth());
    }

    @Override
    protected void renderRuler(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (this.isOverflowing()) return;

        boolean isGlobalX = CONFIG.getGlobal().isXHotActive();
        boolean isGlobalY = CONFIG.getGlobal().isYHotActive();

        int colorX = isGlobalX ? OVERRIDE_RULER_COLOR : RULER_COLOR;
        int colorY = isGlobalY ? OVERRIDE_RULER_COLOR : RULER_COLOR;

        int xhot = this.clampHotspot(isGlobalX ? CONFIG.getGlobal().xhot() : state.value().x(), this.maxXHot);
        int yhot = this.clampHotspot(isGlobalY ? CONFIG.getGlobal().yhot() : state.value().y(), this.maxYHot);

        float rulerWidth = this.getCellWidth();
        float rulerHeight = this.getCellHeight();

        float xhotX1 = (getX() + xhot * rulerWidth) - (rulerWidth > 1 || xhot != this.maxXHot ? 0 : 1);
        float xhotX2 = (getX() + xhot * rulerWidth) + (xhot > 0 ? rulerWidth : Math.max(rulerWidth, 2));
        float yhotY1 = (getY() + yhot * rulerHeight) - (rulerHeight > 1 || yhot != this.maxYHot ? 0 : 1);
        float yhotY2 = (getY() + yhot * rulerHeight) + (yhot > 0 ? rulerHeight : Math.max(rulerHeight, 2));


        if ((isGlobalX && !isGlobalY) || (isGlobalX == isGlobalY)) {
            GuiGraphicsUtils.fillFloat(guiGraphics, this.getX(), yhotY1, this.getRight(), yhotY2, colorY);
            GuiGraphicsUtils.fillFloat(guiGraphics, xhotX1, this.getY(), xhotX2, this.getBottom(), colorX);
        } else {
            GuiGraphicsUtils.fillFloat(guiGraphics, xhotX1, this.getY(), xhotX2, this.getBottom(), colorX);
            GuiGraphicsUtils.fillFloat(guiGraphics, this.getX(), yhotY1, this.getRight(), yhotY2, colorY);
        }
    }

    private int clampHotspot(int hotspot, int max) {
        return SettingsUtil.clamp(hotspot, SettingsUtil.HOT_MIN, max);
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (!this.active) {
            return this.isHovered() ? CursorTypes.NOT_ALLOWED : CursorType.DEFAULT;
        }
        if (!this.dragging) {
            return this.isHovered() ? CursorTypes.CROSSHAIR : CursorType.DEFAULT;
        }

        boolean inXHot = (globalMode || !CONFIG.getGlobal().isXHotActive()) && this.isInsideXHot(mouseX);
        boolean inYHot = (globalMode || !CONFIG.getGlobal().isYHotActive()) && this.isInsideYHot(mouseY);

        if (inXHot && inYHot) {
            return CursorTypes.RESIZE_ALL;
        }
        if (inXHot) {
            return CursorTypes.RESIZE_EW;
        }
        if (inYHot) {
            return CursorTypes.RESIZE_NS;
        }
        return CursorTypes.NOT_ALLOWED;
    }

    private boolean isInsideXHot(double mouseX) {
        double rawX = (mouseX - this.getX()) / this.getCellWidth();
        return rawX >= 0 && rawX <= this.maxXHot;
    }

    private boolean isInsideYHot(double mouseY) {
        double rawY = (mouseY - this.getY()) / this.getCellHeight();
        return rawY >= 0 && rawY <= this.maxYHot;
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        this.dragging = true;
        this.setHotspots(mouseButtonEvent.x(), mouseButtonEvent.y());
    }

    @Override
    protected void onDrag(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        if (this.dragging) {
            this.setHotspots(mouseButtonEvent.x(), mouseButtonEvent.y());
        }
    }

    @Override
    public void onRelease(MouseButtonEvent mouseButtonEvent) {
        if (this.dragging) {
            this.dragging = false;
            this.setHotspots(mouseButtonEvent.x(), mouseButtonEvent.y());
            this.setFocused(false);
            onRelease.run();
        }
    }

    public void setHotspots(double mouseX, double mouseY) {
        int xhot = this.clampHotspot((int) ((mouseX - this.getX()) / this.getCellWidth()), this.maxXHot);
        int yhot = this.clampHotspot((int) ((mouseY - this.getY()) / this.getCellHeight()), this.maxYHot);

        onChange.accept(new CursorState.Hotspots(xhot, yhot));
    }
}
