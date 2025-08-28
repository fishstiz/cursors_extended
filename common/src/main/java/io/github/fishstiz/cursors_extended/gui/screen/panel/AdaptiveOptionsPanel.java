package io.github.fishstiz.cursors_extended.gui.screen.panel;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.gui.CursorAnimationHelper;
import io.github.fishstiz.cursors_extended.gui.widget.OptionsListWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class AdaptiveOptionsPanel extends AbstractOptionsPanel {
    private static final Tooltip ADAPTIVE_INFO = Tooltip.create(Component.translatable("cursors_extended.options.adapt.tooltip"));
    private static final Component ITEM_SLOT = Component.translatable("cursors_extended.options.adapt.item_slot");
    private static final Component ITEM_GRAB = Component.translatable("cursors_extended.options.adapt.item_grab");
    private static final Component CREATIVE_TABS = Component.translatable("cursors_extended.options.adapt.creative_tabs");
    private static final Component ENCHANTMENTS = Component.translatable("cursors_extended.options.adapt.enchantments");
    private static final Component STONECUTTER = Component.translatable("cursors_extended.options.adapt.stonecutter");
    private static final Component LOOM = Component.translatable("cursors_extended.options.adapt.loom");
    private static final Component ADVANCEMENTS = Component.translatable("cursors_extended.options.adapt.advancements");
    private static final Component WORLD = Component.translatable("cursors_extended.options.adapt.world");
    private static final Component SERVER = Component.translatable("cursors_extended.options.adapt.server");
    private static final Component INACTIVE_WIDGETS = Component.translatable("cursors_extended.options.adapt.inactive_widgets");
    private static final int CURSOR_SIZE_STEP = 8;
    private final CursorAnimationHelper animationHelper;
    private final Runnable refreshCursors;
    private OptionsListWidget optionsList;

    public AdaptiveOptionsPanel(Component title, CursorAnimationHelper animationHelper, Runnable refreshCursors) {
        super(title);

        this.animationHelper = animationHelper;
        this.refreshCursors = refreshCursors;
    }

    @Override
    protected void initContents() {
        this.optionsList = new OptionsListWidget(this.getMinecraft(), this.getFont(), Button.DEFAULT_HEIGHT, this.getSpacing());

        boolean adaptive = CursorManager.INSTANCE.isAdaptive();
        this.optionsList.addToggle(adaptive, this::toggleAdaptive, ENABLE_TEXT, ADAPTIVE_INFO, true);
        this.addOption(CONFIG.isItemSlotEnabled(), CONFIG::setItemSlotEnabled, ITEM_SLOT, CursorTypes.POINTING_HAND, adaptive);
        this.addOption(CONFIG.isItemGrabbingEnabled(), CONFIG::setItemGrabbingEnabled, ITEM_GRAB, CursorTypesExt.GRABBING, adaptive);
        this.addOption(CONFIG.isInactiveWidgetsEnabled(), CONFIG::setInactiveWidgetsEnabled, INACTIVE_WIDGETS, CursorTypes.NOT_ALLOWED, adaptive);
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
        this.optionsList.addToggle(value && active, consumer, this.index(label), prefixCursor(cursorType), null, active);
    }

    private OptionsListWidget.Prefix prefixCursor(CursorType cursorType) {
        Cursor cursor = Objects.requireNonNull(CursorManager.INSTANCE.getCursor(cursorType));

        return (guiGraphics, font, x, y, height) -> {
            int adjustedHeight = height - (height % CURSOR_SIZE_STEP);
            int offsetY = y + (height - adjustedHeight) / 2;
            this.animationHelper.drawSprite(guiGraphics, cursor, x, offsetY, adjustedHeight);
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
        for (Cursor cursor : CursorManager.INSTANCE.getCursors()) {
            if (cursor.getType() == CursorType.DEFAULT) continue;

            Cursor loadedCursor = cursor;
            if (adaptive && !cursor.isLoaded() && this.loadCursor(cursor)) {
                loadedCursor = Objects.requireNonNull(CursorManager.INSTANCE.getCursor(cursor.getType()));
            }

            loadedCursor.enable(adaptive);
            CONFIG.getOrCreateSettings(loadedCursor).setEnabled(adaptive);
        }

        this.refreshCursors.run();
        this.refreshWidgets();
        this.repositionElements();
    }
}
