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
import org.lwjgl.sdl.SDLMouse;

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
            getCurrentCursor(window).cursorType().select();
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
        private CursorTexture texture;
        private float drawWidth;
        private float drawHeight;
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
            cursor.cursorType().select();

            CursorTexture texture = cursor.getTexture();
            if (texture == null || cursor.isCustom() || !CursorsExtended.CONFIG.getOrCreateSettings(cursor).enabled()) {
                this.texture = null;
                return;
            }

            this.texture = texture;
            float scale = SettingsUtil.getAutoScale(texture.scale());
            this.xhot = texture.xhot() * scale;
            this.yhot = texture.yhot() * scale;
            this.drawWidth = this.texture.spriteWidth() * scale;
            this.drawHeight = this.texture.spriteHeight() * scale;
        }

        @Override
        public void resetCursor(Window window) {
            SDLMouse.SDL_ShowCursor();
            this.texture = null;
        }

        @Override
        public void render(Window window, Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            if (!minecraft.mouseHandler.isMouseGrabbed()) {
                if (this.texture != null) {
                    int guiScale = minecraft.getWindow().getGuiScale();
                    int scaledWidth = Math.round(this.drawWidth / guiScale);
                    int scaledHeight = Math.round(this.drawHeight / guiScale);
                    int x = mouseX - Math.round(this.xhot / guiScale);
                    int y = mouseY - Math.round(this.yhot / guiScale);

                    SDLMouse.SDL_HideCursor();

                    guiGraphics.nextStratum();
                    guiGraphics.blit(
                            RenderPipelines.GUI_TEXTURED,
                            this.texture.texturePath(),
                            x,
                            y,
                            0,
                            this.texture.spriteVOffset(),
                            scaledWidth,
                            scaledHeight,
                            this.texture.spriteWidth(),
                            this.texture.spriteHeight(),
                            this.texture.textureWidth(),
                            this.texture.textureHeight()
                    );
                } else {
                    resetCursor(window);
                }
            }
        }
    }
}
