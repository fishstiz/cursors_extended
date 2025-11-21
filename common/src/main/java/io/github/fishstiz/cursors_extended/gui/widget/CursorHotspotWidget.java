package io.github.fishstiz.cursors_extended.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.MouseEvent;
import io.github.fishstiz.cursors_extended.util.DrawUtil;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class CursorHotspotWidget extends CursorWidget {
    private static final Identifier BACKGROUND_128 = CursorsExtended.id("textures/gui/background_128.png");
    private static final int BACKGROUND_DISABLED = 0xAF000000; // 70% black
    private static final int RULER_COLOR = 0xFFFF0000; // red
    private static final int OVERRIDE_RULER_COLOR = 0xFF00FF00; // green
    private static final Component OVERFLOW_TEXT = Component.translatable("cursors_extended.options.image_too_large");
    private static final int OVERFLOW_COLOR = 0xFFFFFFFF; // white
    private final SliderWidget xhotSlider;
    private final SliderWidget yhotSlider;
    private final @Nullable MouseEventListener mouseEventListener;
    private final int maxXHot;
    private final int maxYHot;
    private boolean dragging = false;

    public CursorHotspotWidget(
            @NonNull Cursor cursor,
            @NonNull SliderWidget xhotSlider,
            @NonNull SliderWidget yhotSlider,
            @Nullable MouseEventListener mouseEventListener
    ) {
        super(CommonComponents.EMPTY, cursor, BACKGROUND_128);

        this.xhotSlider = xhotSlider;
        this.yhotSlider = yhotSlider;
        this.mouseEventListener = mouseEventListener;
        this.maxXHot = SettingsUtil.getMaxXHot(cursor);
        this.maxYHot = SettingsUtil.getMaxYHot(cursor);
    }

    @Override
    protected void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.active = !this.isOverflowing() && (this.xhotSlider.isActive() || this.yhotSlider.isActive());
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (this.isHovered() && this.isOverflowing()) {
            Font font = Minecraft.getInstance().font;
            int textX = this.getX() + (this.getWidth() / 2 - font.width(OVERFLOW_TEXT) / 2);
            int textY = this.getY() + (this.getHeight() / 2 - font.lineHeight / 2);
            guiGraphics.drawString(font, OVERFLOW_TEXT, textX, textY, OVERFLOW_COLOR);
        }
    }

    @Override
    protected void renderBackground(@NonNull GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);

        if (!this.active) {
            guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), BACKGROUND_DISABLED);
        }
    }

    @Override
    protected void renderCursor(@NonNull GuiGraphics guiGraphics, @NonNull Cursor cursor) {
        DrawUtil.drawCursor(guiGraphics, cursor, this.getX(), this.getY(), this.getWidth());
    }

    @Override
    protected void renderRuler(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.isOverflowing()) return;

        boolean isGlobalX = CONFIG.getGlobal().isXHotActive();
        boolean isGlobalY = CONFIG.getGlobal().isYHotActive();

        int colorX = isGlobalX ? OVERRIDE_RULER_COLOR : RULER_COLOR;
        int colorY = isGlobalY ? OVERRIDE_RULER_COLOR : RULER_COLOR;

        int xhot = this.clampHotspot(isGlobalX ? CONFIG.getGlobal().xhot() : (int) this.xhotSlider.getMappedValue(), this.maxXHot);
        int yhot = this.clampHotspot(isGlobalY ? CONFIG.getGlobal().yhot() : (int) this.yhotSlider.getMappedValue(), this.maxYHot);

        float rulerWidth = this.getCellWidth();
        float rulerHeight = this.getCellHeight();

        float xhotX1 = (getX() + xhot * rulerWidth) - (rulerWidth > 1 || xhot != this.maxXHot ? 0 : 1);
        float xhotX2 = (getX() + xhot * rulerWidth) + (xhot > 0 ? rulerWidth : Math.max(rulerWidth, 2));
        float yhotY1 = (getY() + yhot * rulerHeight) - (rulerHeight > 1 || yhot != this.maxYHot ? 0 : 1);
        float yhotY2 = (getY() + yhot * rulerHeight) + (yhot > 0 ? rulerHeight : Math.max(rulerHeight, 2));


        if ((isGlobalX && !isGlobalY) || (isGlobalX == isGlobalY)) {
            DrawUtil.fill(guiGraphics, this.getX(), yhotY1, this.getRight(), yhotY2, colorY);
            DrawUtil.fill(guiGraphics, xhotX1, this.getY(), xhotX2, this.getBottom(), colorX);
        } else {
            DrawUtil.fill(guiGraphics, xhotX1, this.getY(), xhotX2, this.getBottom(), colorX);
            DrawUtil.fill(guiGraphics, this.getX(), yhotY1, this.getRight(), yhotY2, colorY);
        }
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        this.dragging = true;
        this.setHotspots(MouseEvent.CLICK, mouseButtonEvent.x(), mouseButtonEvent.y());
    }

    @Override
    protected void onDrag(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        if (this.dragging) {
            this.setHotspots(MouseEvent.DRAG, mouseButtonEvent.x(), mouseButtonEvent.y());
        }
    }

    @Override
    public void onRelease(MouseButtonEvent mouseButtonEvent) {
        if (this.dragging) {
            this.dragging = false;
            this.setHotspots(MouseEvent.RELEASE, mouseButtonEvent.x(), mouseButtonEvent.y());
            this.setFocused(false);
        }
    }

    public void setHotspots(MouseEvent mouseEvent, double mouseX, double mouseY) {
        int xhot = this.clampHotspot((int) ((mouseX - this.getX()) / this.getCellWidth()), this.maxXHot);
        int yhot = this.clampHotspot((int) ((mouseY - this.getY()) / this.getCellHeight()), this.maxYHot);

        if (this.xhotSlider.isActive()) {
            this.xhotSlider.applyMappedValue(xhot);
        }
        if (this.yhotSlider.isActive()) {
            this.yhotSlider.applyMappedValue(yhot);
        }
        if (this.mouseEventListener != null) {
            this.mouseEventListener.onMouseEvent(this, mouseEvent, xhot, yhot);
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

        boolean inXHot = !CONFIG.getGlobal().isXHotActive() && this.isInsideXHot(mouseX);
        boolean inYHot = !CONFIG.getGlobal().isYHotActive() && this.isInsideYHot(mouseY);

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

    public interface MouseEventListener {
        void onMouseEvent(@NonNull CursorHotspotWidget target, @NonNull MouseEvent mouseEvent, int xhot, int yhot);
    }
}
