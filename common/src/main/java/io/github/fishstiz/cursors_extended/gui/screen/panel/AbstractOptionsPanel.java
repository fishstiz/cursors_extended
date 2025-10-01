package io.github.fishstiz.cursors_extended.gui.screen.panel;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.screen.CatalogBrowserScreen;
import io.github.fishstiz.cursors_extended.resource.CursorTexture;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractOptionsPanel extends CatalogBrowserScreen.ContentPanel {
    protected static final Component ENABLE_TEXT = Component.translatable("cursors_extended.options.enabled");
    protected static final Component SCALE_TEXT = Component.translatable("cursors_extended.options.scale");
    protected static final Component GUI_SCALE_TEXT = Component.translatable("cursors_extended.options.scale.gui");
    protected static final Component XHOT_TEXT = Component.translatable("cursors_extended.options.xhot");
    protected static final Component YHOT_TEXT = Component.translatable("cursors_extended.options.yhot");
    protected static final Component HOTSPOT_SUFFIX = Component.translatable("cursors_extended.options.hotspot-suffix");
    private final Component title;
    private StringWidget titleWidget;

    protected AbstractOptionsPanel(Component title) {
        this.title = withBold(title);
    }

    protected abstract void initContents();

    protected abstract void repositionContents(int x, int y);

    @Override
    protected final void init() {
        this.titleWidget = new StringWidget(this.title, this.getFont());
        this.addRenderableIndexedWidget(this.titleWidget);
        this.initContents();
    }

    @Override
    protected final void repositionElements() {
        this.titleWidget.setSize(this.getHeaderWidth(), this.getHeaderHeight());
        this.titleWidget.setMaxWidth(this.getHeaderWidth(), StringWidget.TextOverflow.CLAMPED);
        this.titleWidget.setPosition(this.getX(), this.getY());

        this.repositionContents(this.getX(), this.getY() + this.titleWidget.getHeight() + this.getSpacing());
    }

    protected int computeMaxHeight(int top) {
        return Math.max(0, this.getHeight() - (top - this.getY()));
    }

    protected boolean loadCursor(@NotNull Cursor deferredCursor) {
        CursorTexture texture = deferredCursor.getTexture();
        if (texture != null) {
            throw new IllegalStateException("Cursor is already loaded");
        }
        if (CursorsExtended.getInstance().getLoader().loadTexture(deferredCursor)) {
            return true;
        }
        this.getMinecraft().getToastManager().addToast(SystemToast.multiline(
                this.getMinecraft(),
                SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                Component.translatable("resourcePack.load_fail"),
                Component.translatable("cursors_extended.options.global.deferred_loading.fail", deferredCursor.text())
        ));
        return false;
    }

    protected void refreshWidgets() {
        this.clearWidgets();
        this.init();
    }

    @Override
    protected void added() {
        this.refreshWidgets();
    }

    private static MutableComponent withBold(Component text) {
        return text.copy().withStyle(style -> style.withBold(true));
    }
}
