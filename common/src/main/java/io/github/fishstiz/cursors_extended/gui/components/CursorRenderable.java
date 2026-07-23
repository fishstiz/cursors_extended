package io.github.fishstiz.cursors_extended.gui.components;

import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.fidgetz.v0.gui.components.WidgetElements;
import io.github.fishstiz.fidgetz.v0.gui.components.WidgetRenderables;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public record CursorRenderable(Cursor cursor) implements RenderableRectangle {
    private static final int CURSOR_SIZE_STEP = 8;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
        if (cursor.getTexture() != null) {
            int adjustedHeight = height - (height % CURSOR_SIZE_STEP);
            int offsetY = top + (height - adjustedHeight) / 2;
            extractCursorState(graphics, cursor, left, offsetY, adjustedHeight);
        }
    }

    public static void extractCursorState(GuiGraphicsExtractor graphics, Cursor cursor, int x, int y, int size) {
        CursorTexture texture = cursor.getTexture();
        if (texture != null) {
            int spriteWidth = texture.spriteWidth();
            int spriteHeight = texture.spriteHeight();

            float scale = (float) size / Math.max(spriteWidth, spriteHeight);
            int drawWidth = Math.round(spriteWidth * scale);
            int drawHeight = Math.round(spriteHeight * scale);

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    texture.texturePath(),
                    x, y,
                    0, texture.spriteVOffset(),
                    drawWidth, drawHeight,
                    spriteWidth, spriteHeight,
                    texture.textureWidth(),
                    texture.textureHeight()
            );
        }
    }

    public static WidgetRenderables widgetRenderables(Cursor cursor) {
        return new WidgetRenderables(new CursorRenderable(cursor));
    }

    public static WidgetElements widgetElements(Cursor cursor, int size) {
        return new WidgetElements(new CursorRenderable(cursor), size, size);
    }
}
