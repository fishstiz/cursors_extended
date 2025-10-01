package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.mixin.WindowAccess;
import io.github.fishstiz.cursors_extended.resource.CursorTexture;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

import static org.lwjgl.glfw.GLFW.*;

public sealed interface CursorRenderer {
    CursorRegistry registry();

    void setCursor(Window window);

    void resetCursor(Window window);

    void render(Window window, Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY);

    default Cursor getCurrentCursor(Window window) {
        return registry().get(((WindowAccess) (Object) window).cursors_extended$getCurrentCursor());
    }

    record Native(CursorRegistry registry) implements CursorRenderer {
        @Override
        public void setCursor(Window window) {
            getCurrentCursor(window).cursorType().select(window);
        }

        @Override
        public void resetCursor(Window window) {
            window.selectCursor(CursorType.DEFAULT);
        }

        @Override
        public void render(Window window, Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
            // no-op
        }
    }

    final class Virtual implements CursorRenderer {
        private final CursorRegistry registry;
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

        public Virtual(CursorRegistry registry) {
            this.registry = registry;
        }

        @Override
        public CursorRegistry registry() {
            return registry;
        }

        @Override
        public void setCursor(Window window) {
            Cursor cursor = getCurrentCursor(window);
            cursor.cursorType().select(window);

            CursorTexture texture = cursor.getTexture();
            if (texture == null || !cursor.isEnabled()) {
                this.textureLocation = null;
                return;
            }

            this.textureLocation = texture.texturePath();
            this.textureWidth = texture.textureWidth();
            this.textureHeight = texture.textureHeight();
            this.spriteWidth = texture.spriteWidth();
            this.spriteHeight = texture.spriteHeight();
            this.vOffset = texture.spriteHeight() * texture.spriteVOffset();

            double scale = SettingsUtil.getAutoScale(texture.scale());
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
        public void render(Window window, Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (!minecraft.mouseHandler.isMouseGrabbed()) {
                if (this.textureLocation != null) {
                    int guiScale = minecraft.getWindow().getGuiScale();
                    int scaledWidth = (int) Math.round(this.drawWidth / guiScale);
                    int scaledHeight = (int) Math.round(this.drawHeight / guiScale);
                    int x = mouseX - (int) Math.round(this.xhot / guiScale);
                    int y = mouseY - (int) Math.round(this.yhot / guiScale);

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
