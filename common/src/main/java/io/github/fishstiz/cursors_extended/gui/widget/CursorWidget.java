package io.github.fishstiz.cursors_extended.gui.widget;

import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static io.github.fishstiz.cursors_extended.util.SettingsUtil.IMAGE_SIZE_GUI_MAX;

public abstract class CursorWidget extends AbstractWidget implements CursorProvider {
    public static final int DEFAULT_HEIGHT = 32;
    public static final int DEFAULT_WIDTH = 32;
    private static final int BACKGROUND_SIZE = 128;
    private static final int BORDER_COLOR = 0xFF000000; // black
    private static final int FOCUSED_BORDER_COLOR = 0xFFFFFFFF; // white
    private final Identifier background128;
    private final Cursor cursor;
    private boolean renderRuler = true;

    protected CursorWidget(
            int x,
            int y,
            int width,
            int height,
            @NonNull Component message,
            @NonNull Cursor cursor,
            @NonNull Identifier background128
    ) {
        super(x, y, width, height, message);

        this.cursor = cursor;
        this.background128 = background128;
    }

    protected CursorWidget(@NonNull Component message, @NonNull Cursor cursor, @NonNull Identifier background128) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, message, cursor, background128);
    }

    protected void renderCursor(@NonNull GuiGraphicsExtractor guiGraphics, @NonNull Cursor cursor) {
    }

    protected abstract void renderRuler(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

    protected void renderBackground(@NonNull GuiGraphicsExtractor guiGraphics) {
        if (!this.isOverflowing()) {
            DrawUtil.drawCheckerboard(
                    guiGraphics,
                    this.getX(),
                    this.getY(),
                    this.getWidth(),
                    this.getHeight(),
                    this.getCellWidth(),
                    this.getCellHeight(),
                    this.background128,
                    BACKGROUND_SIZE
            );
        }
    }

    protected void renderBorder(@NonNull GuiGraphicsExtractor guiGraphics) {
        int color = this.isFocused() && this.active ? FOCUSED_BORDER_COLOR : BORDER_COLOR;
        DrawUtil.renderOutline(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), color);
    }

    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.cursor.hasTexture()) {
            this.renderBackground(guiGraphics);
            this.renderCursor(guiGraphics, this.cursor);

            if (this.isRenderRuler()) {
                this.renderRuler(guiGraphics, mouseX, mouseY);
            }
        }
        this.renderBorder(guiGraphics);
    }

    public void setRenderRuler(boolean renderRuler) {
        this.renderRuler = renderRuler;
    }

    public boolean isRenderRuler() {
        return renderRuler;
    }

    protected @NonNull Cursor getCursor() {
        return this.cursor;
    }

    protected float getCellWidth() {
        CursorTexture texture = this.getCursor().getTexture();
        if (texture == null) return 0;

        int spriteWidth = texture.spriteWidth();
        int spriteHeight = texture.spriteHeight();
        float scale = (float) this.getWidth() / Math.max(spriteWidth, spriteHeight);
        int drawWidth = Math.round(spriteWidth * scale);
        return (float) drawWidth / spriteWidth;
    }

    protected float getCellHeight() {
        CursorTexture texture = this.getCursor().getTexture();
        if (texture == null) return 0;

        int spriteWidth = texture.spriteWidth();
        int spriteHeight = texture.spriteHeight();
        float scale = (float) this.getHeight() / Math.max(spriteWidth, spriteHeight);
        int drawHeight = Math.round(spriteHeight * scale);
        return (float) drawHeight / spriteHeight;
    }

    protected boolean isOverflowing() {
        CursorTexture texture = this.getCursor().getTexture();
        return texture == null || (texture.spriteWidth() > IMAGE_SIZE_GUI_MAX || texture.spriteHeight() > IMAGE_SIZE_GUI_MAX);
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }
}
