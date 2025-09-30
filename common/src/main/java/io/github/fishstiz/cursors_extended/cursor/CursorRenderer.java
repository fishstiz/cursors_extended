package io.github.fishstiz.cursors_extended.cursor;

import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;

interface CursorRenderer {
    void setCursor(@NotNull Cursor cursor);

    void resetCursor();

    void render(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY);

    class Native implements CursorRenderer {
        Native() {
        }

        @Override
        public void setCursor(@NotNull Cursor cursor) {
            glfwSetCursor(CursorTypeUtil.HANDLE, cursor.getId());
        }

        @Override
        public void resetCursor() {
            glfwSetCursor(CursorTypeUtil.HANDLE, MemoryUtil.NULL);
        }

        @Override
        public void render(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
            // no-op
        }
    }

    class Virtual implements CursorRenderer {
        private ResourceLocation textureLocation;
        private int textureWidth;
        private int textureHeight;
        private int spriteWidth;
        private int spriteHeight;
        private double drawWidth;
        private double drawHeight;
        private int vOffset;
        private double xhot;
        private double yhot;

        Virtual() {
        }

        @Override
        public void setCursor(@NotNull Cursor cursor) {
            this.textureLocation = cursor.getLocation();
            this.textureWidth = cursor.getTextureWidth();
            this.textureHeight = cursor.getTextureHeight();
            this.spriteWidth = cursor.getSpriteWidth();
            this.spriteHeight = cursor.getSpriteHeight();
            this.vOffset = cursor.getSpriteHeight() * cursor.getSpriteIndex();

            double scale = SettingsUtil.getAutoScale(cursor.getScale());
            this.xhot = cursor.getXHot() * scale;
            this.yhot = cursor.getYHot() * scale;
            this.drawWidth = this.spriteWidth * scale;
            this.drawHeight = this.spriteHeight * scale;
        }

        @Override
        public void resetCursor() {
            glfwSetInputMode(CursorTypeUtil.HANDLE, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            this.textureLocation = null;
        }

        @Override
        public void render(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (!minecraft.mouseHandler.isMouseGrabbed() && this.textureLocation != null) {
                int guiScale = minecraft.getWindow().getGuiScale();
                int scaledWidth = (int) Math.round(this.drawWidth / guiScale);
                int scaledHeight = (int) Math.round(this.drawHeight / guiScale);
                int x = mouseX - (int) Math.round(this.xhot / guiScale);
                int y = mouseY - (int) Math.round(this.yhot / guiScale);

                glfwSetInputMode(CursorTypeUtil.HANDLE, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

                guiGraphics.nextStratum();
                guiGraphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.textureLocation,
                        x, y,
                        0, this.vOffset,
                        scaledWidth, scaledHeight,
                        this.spriteWidth, this.spriteHeight,
                        this.textureWidth, this.textureHeight
                );
            }
        }
    }
}
