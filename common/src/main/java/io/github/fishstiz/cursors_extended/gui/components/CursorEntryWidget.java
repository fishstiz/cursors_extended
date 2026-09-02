package io.github.fishstiz.cursors_extended.gui.components;

import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.fidgetz.v0.gui.components.FZButtonBase;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;

public class CursorEntryWidget extends FZButtonBase {
    private static final int ICON_SIZE = 16;
    private final Cursor cursor;
    private final CursorRenderable icon;
    private final Component disabledMessage;
    private final Component unloadedMessage;
    private final Runnable pressHandler;

    public CursorEntryWidget(Cursor cursor, Runnable pressHandler) {
        this.cursor = cursor;
        this.icon = new CursorRenderable(cursor);
        this.message = cursor.text();
        this.disabledMessage = AbstractWidget.WithInactiveMessage.defaultInactiveMessage(cursor.text());
        this.unloadedMessage = ComponentUtils.mergeStyles(cursor.text(), Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY));
        this.pressHandler = pressHandler;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        super.onPress(input);
        this.pressHandler.run();
    }

    @Override
    public Component getMessage() {
        if (cursor.isTextureEnabled()) {
            return super.getMessage();
        }
        if (cursor.hasTexture()) {
            return disabledMessage;
        }
        return unloadedMessage;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = getX() + DEFAULT_SPACING;
        int right = getRight() - DEFAULT_SPACING;
        int top = getY();
        int height = getHeight();
        int bottom = top + height;

        int iconTop = (top + height / 2 - ICON_SIZE / 2);

        icon.extractRenderState(graphics, left, iconTop, ICON_SIZE, ICON_SIZE, mouseX, mouseY, partialTick);

        left += ICON_SIZE + DEFAULT_SPACING / 2;

        ActiveTextCollector textRenderer = graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE);
        GuiGraphicsUtils.scrollingText(textRenderer, getMessage(), left, top, right, bottom);
    }
}
