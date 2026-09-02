package io.github.fishstiz.cursors_extended.gui.panels;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.gui.components.OptionsListWidget;
import io.github.fishstiz.cursors_extended.gui.components.CursorRenderable;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import io.github.fishstiz.fidgetz.v0.gui.components.FZIcon;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.components.WidgetRenderables;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class AdaptiveOptionsPanel extends AbstractContentPanel {
    private static final Component ADAPTIVE_INFO = Component.translatable("cursors_extended.options.adapt.tooltip");
    private static final Component HOLD_CURSORS = Component.translatable("cursors_extended.options.adapt.hold");
    private static final Component HOLD_CURSORS_INFO = Component.translatable("cursors_extended.options.adapt.hold.tooltip");
    private static final Component ITEM_SLOT = Component.translatable("cursors_extended.options.adapt.item_slot");
    private static final Component ITEM_GRAB = Component.translatable("cursors_extended.options.adapt.item_grab");
    private static final Component SCROLLBAR_POINTER = scrollbarText(CursorTypes.POINTING_HAND);
    private static final Component SCROLLBAR_RESIZE = scrollbarText(CursorTypes.RESIZE_NS);
    private OptionsListWidget list;

    private static Component scrollbarText(CursorType cursorType) {
        return Component.translatable("cursors_extended.options.adapt.scrollbar", Component.translatable(
                "cursors_extended.options.cursor-type." + cursorType.toString()
        ));
    }

    public AdaptiveOptionsPanel(Minecraft minecraft, Screen screen) {
        super("Adaptive", minecraft, screen, Component.translatable("cursors_extended.options.adapt"));
    }

    @Override
    protected void buildKeywords(Consumer<Component> builder) {
        builder.accept(HOLD_CURSORS);
        builder.accept(ITEM_SLOT);
        builder.accept(ITEM_GRAB);
        builder.accept(SCROLLBAR_POINTER);
        builder.accept(SCROLLBAR_RESIZE);
    }

    @Override
    protected void buildWidgets(GuiComponentCollector collector, FZFlexLayout layout) {
        this.list = layout.child(new OptionsListWidget(), layout.flexChildSettings());

        Config defaults = Config.defaults();

        this.list.rowBuilder()
                .label(ENABLE_TEXT)
                .tooltip(ADAPTIVE_INFO)
                .toggleBuilder()
                .state(this::toggleAdaptive, this::isAdaptiveTexturesEnabled)
                .active(CONFIG.hasResourcePack())
                .defaultValue(true)
                .build();

        this.list.rowBuilder()
                .label(HOLD_CURSORS)
                .tooltip(HOLD_CURSORS_INFO)
                .toggleBuilder()
                .state(CONFIG::setHeldCursorsEnabled, CONFIG::isHeldCursorsEnabled)
                .defaultValue(defaults.isHeldCursorsEnabled())
                .build();

        this.list.rowBuilder()
                .widget(createCursorWidget(CursorTypes.POINTING_HAND))
                .label(ITEM_SLOT)
                .toggleBuilder()
                .state(CONFIG::setItemSlotEnabled, CONFIG::isItemSlotEnabled)
                .defaultValue(defaults.isItemSlotEnabled())
                .build();

        this.list.rowBuilder()
                .widget(createCursorWidget(CursorTypesExt.GRABBING))
                .label(ITEM_GRAB)
                .toggleBuilder()
                .state(CONFIG::setItemGrabbingEnabled, CONFIG::isItemGrabbingEnabled)
                .defaultValue(defaults.isItemGrabbingEnabled())
                .build();

        this.list.rowBuilder()
                .widget(createCursorWidget(CursorTypes.POINTING_HAND))
                .label(SCROLLBAR_POINTER)
                .toggleBuilder()
                .state(CONFIG::setPointerScrollbarEnabled, CONFIG::isPointerScrollbarEnabled)
                .defaultValue(defaults.isPointerScrollbarEnabled())
                .build();

        this.list.rowBuilder()
                .widget(createCursorWidget(CursorTypes.RESIZE_NS))
                .label(SCROLLBAR_RESIZE)
                .toggleBuilder()
                .state(CONFIG::setResizeScrollbarEnabled, CONFIG::isResizeScrollbarEnabled)
                .defaultValue(defaults.isResizeScrollbarEnabled())
                .build();
    }

    private FZIcon createCursorWidget(CursorType cursorType) {
        Cursor cursor = CursorsExtended.getInstance().getRegistry().get(cursorType);
        WidgetRenderables sprites = cursor.hasTexture()
                ? CursorRenderable.widgetRenderables(cursor)
                : new WidgetRenderables(Renderables.text("?"));
        return FZIcon.builder(sprites).size(20, 20).visible(cursor.hasTexture()).build();
    }

    private void toggleAdaptive(boolean adaptive) {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (cursor.cursorType() == CursorType.DEFAULT) continue;

            if (adaptive && cursor.getTexture() == null && CONFIG.hasResourcePack()) {
                loadCursor(cursor);
            }

            Config.CursorSettings settings =  CONFIG.getOrCreateSettings(cursor);
            settings.setEnabled(adaptive);
        }
    }

    private boolean isAdaptiveTexturesEnabled() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (CursorTypeUtil.nonDefault(cursor.cursorType()) && cursor.isTextureEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSearch(String search) {
        if (this.list != null) {
            this.list.onSearch(search);
        }
    }
}
