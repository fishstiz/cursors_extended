package io.github.fishstiz.cursors_extended.gui.screen.panel;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.gui.widget.OptionsListWidget;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import io.github.fishstiz.cursors_extended.util.DrawUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class AdaptiveOptionsPanel extends AbstractOptionsPanel {
    private static final Tooltip ADAPTIVE_INFO = Tooltip.create(Component.translatable("cursors_extended.options.adapt.tooltip"));
    private static final Component HOLD_CURSORS = Component.translatable("cursors_extended.options.adapt.hold");
    private static final Tooltip HOLD_CURSORS_INFO = Tooltip.create(Component.translatable("cursors_extended.options.adapt.hold.tooltip"));
    private static final Component ITEM_SLOT = Component.translatable("cursors_extended.options.adapt.item_slot");
    private static final Component ITEM_GRAB = Component.translatable("cursors_extended.options.adapt.item_grab");
    private static final Component SCROLLBAR_POINTER = scrollbarText(CursorTypes.POINTING_HAND);
    private static final Component SCROLLBAR_RESIZE = scrollbarText(CursorTypes.RESIZE_NS);
    private static final Component CREATIVE_TABS = Component.translatable("cursors_extended.options.adapt.creative_tabs");
    private static final Component ENCHANTMENTS = Component.translatable("cursors_extended.options.adapt.enchantments");
    private static final Component STONECUTTER = Component.translatable("cursors_extended.options.adapt.stonecutter");
    private static final Component LOOM = Component.translatable("cursors_extended.options.adapt.loom");
    private static final Component ADVANCEMENTS = Component.translatable("cursors_extended.options.adapt.advancements");
    private static final Component WORLD = Component.translatable("cursors_extended.options.adapt.world");
    private static final Component SERVER = Component.translatable("cursors_extended.options.adapt.server");
    private static final int CURSOR_SIZE_STEP = 8;
    private final Runnable refreshCursors;
    private OptionsListWidget optionsList;

    public AdaptiveOptionsPanel(Component title, Runnable refreshCursors) {
        super(title);
        this.refreshCursors = refreshCursors;
    }

    @Override
    protected void initContents() {
        this.optionsList = new OptionsListWidget(this.getMinecraft(), this.getFont(), Button.DEFAULT_HEIGHT, this.getSpacing());

        boolean adaptive = isAdaptive();
        this.optionsList.addToggle(adaptive, this::toggleAdaptive, ENABLE_TEXT, ADAPTIVE_INFO, true);
        this.addOption(CONFIG.isHeldCursorsEnabled(), CONFIG::setHeldCursorsEnabled, HOLD_CURSORS, HOLD_CURSORS_INFO, null, adaptive);
        this.addOption(CONFIG.isItemSlotEnabled(), CONFIG::setItemSlotEnabled, ITEM_SLOT, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isItemGrabbingEnabled(), CONFIG::setItemGrabbingEnabled, ITEM_GRAB, CursorTypesExt.GRABBING, adaptive);
        this.addOption(CONFIG.isPointerScrollbarEnabled(), CONFIG::setPointerScrollbarEnabled, SCROLLBAR_POINTER, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isResizeScrollbarEnabled(), CONFIG::setResizeScrollbarEnabled, SCROLLBAR_RESIZE, CursorTypes.RESIZE_NS, adaptive);
        this.addOption(CONFIG.isCreativeTabsEnabled(), CONFIG::setCreativeTabsEnabled, CREATIVE_TABS, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isEnchantmentsEnabled(), CONFIG::setEnchantmentsEnabled, ENCHANTMENTS, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isStonecutterRecipesEnabled(), CONFIG::setStonecutterRecipesEnabled, STONECUTTER, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isLoomPatternsEnabled(), CONFIG::setLoomPatternsEnabled, LOOM, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isAdvancementTabsEnabled(), CONFIG::setAdvancementTabsEnabled, ADVANCEMENTS, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isWorldIconEnabled(), CONFIG::setWorldIconEnabled, WORLD, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isServerIconEnabled(), CONFIG::setServerIconEnabled, SERVER, CursorTypes.POINTING_HAND, adaptive);

        this.optionsList.search(this.getSearch());

        this.addRenderableWidget(this.optionsList);
    }

    private void addOption(boolean value, Consumer<Boolean> consumer, Component label, CursorType cursorType, boolean active) {
        this.addOption(value, consumer, label, null, cursorType, active);
    }

    private void addOption(boolean value, Consumer<Boolean> consumer, Component label, Tooltip tooltip, CursorType cursorType, boolean active) {
        this.optionsList.addToggle(value && active, consumer, this.index(label), prefixCursor(cursorType), tooltip, active);
    }

    private OptionsListWidget.Prefix prefixCursor(CursorType cursorType) {
        if (cursorType == null) return null;

        Cursor cursor = CursorsExtended.getInstance().getRegistry().get(cursorType);

        return (guiGraphics, font, x, y, height) -> {
            int adjustedHeight = height - (height % CURSOR_SIZE_STEP);
            int offsetY = y + (height - adjustedHeight) / 2;
            DrawUtil.drawCursor(guiGraphics, cursor, x, offsetY, adjustedHeight);
            return adjustedHeight;
        };
    }

    @Override
    protected void repositionContents(int x, int y) {
        if (this.optionsList != null) {
            this.optionsList.setSize(this.getWidth(), this.computeMaxHeight(y));
            this.optionsList.setPosition(x, y);
        }
    }

    @Override
    protected void searched(@NotNull String search, @Nullable Component matched) {
        if (this.optionsList != null) {
            this.optionsList.search(search);
        }
    }

    private void toggleAdaptive(boolean adaptive) {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (cursor.cursorType() == CursorType.DEFAULT) continue;

            if (adaptive && cursor.getTexture() == null) {
                loadCursor(cursor);
            }

            CONFIG.getOrCreateSettings(cursor).setEnabled(adaptive);
        }

        this.refreshCursors.run();
        this.refreshWidgets();
        this.repositionElements();
    }

    private boolean isAdaptive() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (CursorTypeUtil.nonDefault(cursor.cursorType()) && cursor.isTextureEnabled()) {
                return true;
            }
        }
        return false;
    }

    private static Component scrollbarText(CursorType cursorType) {
        return Component.translatable("cursors_extended.options.adapt.scrollbar", Component.translatable(
                "cursors_extended.options.cursor-type." + cursorType.toString()
        ));
    }
}
