package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static org.lwjgl.glfw.GLFW.*;

public sealed interface CursorRenderer {
    CursorRegistry registry();

    void applyCursor(Window window);

    void resetCursor(Window window);

    void render(Window window, Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

    default Cursor getCurrentCursor(Window window) {
        return registry().get(window.currentCursor);
    }

    record Native(CursorRegistry registry) implements CursorRenderer {
        @Override
        public void applyCursor(Window window) {
            getCurrentCursor(window).cursorType().select(window);
        }

        @Override
        public void resetCursor(Window window) {
            window.selectCursor(CursorType.DEFAULT);
        }

        @Override
        public void render(Window window, Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            // no-op
        }
    }

    final class Virtual implements CursorRenderer {
        private final CursorRegistry registry;
        private Identifier textureLocation;
        private int textureWidth;
        private int textureHeight;
        private int spriteWidth;
        private int spriteHeight;
        private float drawWidth;
        private float drawHeight;
        private int vOffset;
        private float xhot;
        private float yhot;

        public Virtual(CursorRegistry registry) {
            this.registry = registry;
        }

        @Override
        public CursorRegistry registry() {
            return registry;
        }

        @Override
        public void applyCursor(Window window) {
            Cursor cursor = getCurrentCursor(window);
            cursor.cursorType().select(window);

            CursorTexture texture = cursor.getTexture();
            if (texture == null || cursor.isCustom() || !CursorsExtended.CONFIG.getOrCreateSettings(cursor).enabled()) {
                this.textureLocation = null;
                return;
            }

            this.textureLocation = texture.texturePath();
            this.textureWidth = texture.textureWidth();
            this.textureHeight = texture.textureHeight();
            this.spriteWidth = texture.spriteWidth();
            this.spriteHeight = texture.spriteHeight();
            this.vOffset = texture.spriteVOffset();

            float scale = SettingsUtil.getAutoScale(texture.scale());
            this.xhot = texture.xhot() * scale;
            this.yhot = texture.yhot() * scale;
            this.drawWidth = this.spriteWidth * scale;
            this.drawHeight = this.spriteHeight * scale;
        }

        @Override
        public void resetCursor(Window window) {
            glfwSetInputMode(window.handle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            this.textureLocation = null;
        }

        @Override
        public void render(Window window, Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            if (!minecraft.mouseHandler.isMouseGrabbed()) {
                if (this.textureLocation != null) {
                    int guiScale = minecraft.getWindow().getGuiScale();
                    int scaledWidth = Math.round(this.drawWidth / guiScale);
                    int scaledHeight = Math.round(this.drawHeight / guiScale);
                    int x = mouseX - Math.round(this.xhot / guiScale);
                    int y = mouseY - Math.round(this.yhot / guiScale);

                    glfwSetInputMode(window.handle(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

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
                } else {
                    glfwSetInputMode(window.handle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                }
            }
        }
    }
}
