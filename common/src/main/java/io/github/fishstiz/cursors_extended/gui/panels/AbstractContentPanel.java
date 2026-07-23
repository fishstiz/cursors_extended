package io.github.fishstiz.cursors_extended.gui.panels;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableElement;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractContentPanel extends AbstractContainerWidget {
    protected static final Component GLOBAL_SETTINGS_TEXT = Component.translatable("cursors_extended.options.global.title");
    protected static final Component ENABLE_TEXT = Component.translatable("cursors_extended.options.enabled");
    protected static final Component SCALE_TEXT = Component.translatable("cursors_extended.options.scale");
    protected static final Component GUI_SCALE_TEXT = Component.translatable("cursors_extended.options.scale.gui");
    protected static final Component XHOT_TEXT = Component.translatable("cursors_extended.options.xhot");
    protected static final Component YHOT_TEXT = Component.translatable("cursors_extended.options.yhot");
    protected static final Component HOTSPOT_SUFFIX = Component.translatable("cursors_extended.options.hotspot-suffix");
    protected static final Component HOTSPOT_GUIDE_TEXT = Component.translatable("cursors_extended.options.hotspot-guide");
    protected static final int DEFAULT_SPACING = 8;
    protected final Minecraft minecraft;
    protected final Screen screen;
    private final Component shorthandTitle;
    private final String id;
    private final FZFlexLayout layout = FZFlexLayout.horizontal(this).spacing(DEFAULT_SPACING);
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<Renderable> renderables = new ArrayList<>();

    protected AbstractContentPanel(String id, Minecraft minecraft, Screen screen, Component title, Component shorthandTitle) {
        super(0, 0, 0, 0, title.copy().withStyle(ChatFormatting.BOLD));
        this.minecraft = minecraft;
        this.screen = screen;
        this.shorthandTitle = shorthandTitle.copy().withStyle(ChatFormatting.BOLD);
        this.id = id;
    }

    protected AbstractContentPanel(String id, Minecraft minecraft, Screen screen, Component title) {
        this(id, minecraft, screen, title, title);
    }

    public static AbstractContentPanel empty() {
        Minecraft minecraft = Minecraft.getInstance();
        return new AbstractContentPanel.Empty(minecraft, minecraft.gui.screen());
    }

    protected abstract void buildKeywords(Consumer<Component> builder);

    public final Set<String> buildKeywords() {
        Set<String> keywords = new LinkedHashSet<>();
        Consumer<Component> collector = keyword -> keywords.add(keyword.getString());
        collector.accept(shorthandTitle);
        collector.accept(getTitle());
        buildKeywords(collector);
        return keywords;
    }

    protected abstract void buildWidgets(GuiComponentCollector collector, FZFlexLayout layout);

    public final void buildWidgets() {
        layout.removeChildren();
        children.clear();
        renderables.clear();

        GuiComponentCollector collector = new GuiComponentCollector();
        buildWidgets(collector, layout);

        layout.arrangeElements();
        layout.setPosition(getX(), getY());
        layout.fidgetz$setSize(getWidth(), getHeight());

        layout.visitWidgets(collector::renderableWidget);
        collector.flushTo(this::addWidget, renderables::add);
    }

    private <T extends GuiEventListener & NarratableEntry> void addWidget(T widget) {
        if (widget instanceof FZHoverableElement hoverable) {
            hoverable.fidgetz$setHovered(false);
        }
        children.add(widget);
    }

    public void onSearch(String search) {
    }

    public void onRemove() {
    }

    public String getId() {
        return id;
    }

    public Component getTitle() {
        return getMessage();
    }

    public Component getShorthandTitle() {
        return shorthandTitle;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        for (Renderable renderable : renderables) {
            renderable.extractRenderState(graphics, mouseX, mouseY, a);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        return getChildAt(mx, my).filter(child -> child.mouseScrolled(mx, my, scrollX, scrollY)).isPresent();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        layout.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        layout.setY(y);
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        layout.setPosition(x, y);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        layout.fidgetz$setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        layout.fidgetz$setHeight(height);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        layout.fidgetz$setSize(width, height);
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (getFocused() != focused) {
            super.setFocused(focused);
        }
    }

    @Override
    protected int contentHeight() {
        return getHeight();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    private static final class Empty extends AbstractContentPanel {
        private Empty(Minecraft minecraft, Screen screen) {
            super("Empty", minecraft, screen, CommonComponents.EMPTY);
        }

        @Override
        protected void buildKeywords(Consumer<Component> builder) {
        }

        @Override
        protected void buildWidgets(GuiComponentCollector collector, FZFlexLayout layout) {
        }
    }

    protected static boolean loadCursor(@NonNull Cursor deferredCursor) {
        CursorTexture texture = deferredCursor.getTexture();
        if (texture != null) {
            throw new IllegalStateException("Cursor is already loaded");
        }
        if (CursorsExtended.getInstance().getLoader().loadTexture(deferredCursor)) {
            return true;
        }
        Minecraft.getInstance().gui.toastManager().addToast(new SystemToast(
                SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                Component.translatable("resourcePack.load_fail"),
                Component.translatable("cursors_extended.options.global.deferred_loading.fail", deferredCursor.text())
        ));
        return false;
    }

    protected static @Nullable Component getAutoText(double scale) {
        return SettingsUtil.isAutoScale((float) scale) ? Component.translatable("options.guiScale.auto") : null;
    }

    protected static void setScale(Cursor cursor, float scale) {
        CursorTexture texture = cursor.getTexture();
        if (texture != null && texture.scale() != scale) {
            CursorsExtended.getInstance().getLoader().updateTexture(cursor, scale, texture.xhot(), texture.yhot());
        }
    }

    protected static void setXHot(Cursor cursor, int xhot) {
        CursorTexture texture = cursor.getTexture();
        if (texture != null && texture.xhot() == xhot) {
            CursorsExtended.getInstance().getLoader().updateTexture(cursor, texture.scale(), xhot, texture.yhot());
        }
    }

    protected static void setYHot(Cursor cursor, int yhot) {
        CursorTexture texture = cursor.getTexture();
        if (texture != null && texture.yhot() != yhot) {
            CursorsExtended.getInstance().getLoader().updateTexture(cursor, texture.scale(), texture.xhot(), yhot);
        }
    }

    protected static void setHotspots(Cursor cursor, int xhot, int yhot) {
        CursorTexture texture = cursor.getTexture();
        if (texture != null && (texture.xhot() != xhot || texture.yhot() != yhot)) {
            CursorsExtended.getInstance().getLoader().updateTexture(cursor, texture.scale(), xhot, yhot);
        }
    }
}
