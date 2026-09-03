package io.github.fishstiz.cursors_extended.gui.panels;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.components.OptionsListWidget;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class CompatibilityOptionsPanel extends AbstractContentPanel {
    private static final Component AGGRESSIVE_TEXT = Component.translatable("cursors_extended.options.compat.aggressive_cursor");
    private static final Component AGGRESSIVE_INFO = Component.translatable("cursors_extended.options.compat.aggressive_cursor.info");
    private static final Component VIRTUAL_TEXT = Component.translatable("cursors_extended.options.compat.virtual_mode");
    private static final Component VIRTUAL_INFO = Component.translatable("cursors_extended.options.compat.virtual_mode.info");
    private static final Component NATIVE_ANIMATED_CURSORS_TEXT = Component.translatable("cursors_extended.options.compat.native_animated_cursors");
    private static final Component NATIVE_ANIMATED_CURSORS_INFO = Component.translatable("cursors_extended.options.compat.native_animated_cursors.info");
    private static final Component REMAP_TEXT = Component.translatable("cursors_extended.options.compat.remap_cursors");
    private static final Component REMAP_INFO = Component.translatable("cursors_extended.options.compat.remap_cursors.info");
    private static final Component WORKAROUNDS_TEXT = Component.translatable("cursors_extended.options.compat.workarounds");
    private static final Component WORKAROUNDS_INFO = Component.translatable("cursors_extended.options.compat.workarounds.info");
    private static final Component LEGACY_MODE_TEXT = Component.translatable("cursors_extended.options.compat.legacy_mode");
    private static final Component LEGACY_MODE_INFO = Component.translatable("cursors_extended.options.compat.legacy_mode.info");
    private OptionsListWidget list;

    public CompatibilityOptionsPanel(Minecraft minecraft, Screen screen) {
        super("Compatibility", minecraft, screen, Component.translatable("cursors_extended.options.compat"));
    }

    @Override
    protected void buildKeywords(Consumer<Component> builder) {
        builder.accept(AGGRESSIVE_TEXT);
        builder.accept(VIRTUAL_TEXT);
        builder.accept(NATIVE_ANIMATED_CURSORS_TEXT);
        builder.accept(REMAP_TEXT);
        builder.accept(WORKAROUNDS_TEXT);
        builder.accept(LEGACY_MODE_TEXT);
    }

    @Override
    protected void buildWidgets(GuiComponentCollector collector, FZFlexLayout layout) {
        this.list = layout.child(new OptionsListWidget(), layout.flexChildSettings());

        Config defaults = Config.defaults();

        FZMutableRef<Boolean> workaroundsRef = FZMutableRef.wrap(CONFIG::setWorkarounds, CONFIG::isWorkaroundsEnabled);

        this.list.rowBuilder()
                .label(WORKAROUNDS_TEXT)
                .tooltip(WORKAROUNDS_INFO)
                .toggleBuilder()
                .defaultValue(defaults.isWorkaroundsEnabled())
                .state(workaroundsRef)
                .build();

        this.list.rowBuilder()
                .label(REMAP_TEXT)
                .tooltip(REMAP_INFO)
                .toggleBuilder()
                .defaultValue(defaults.isRemapStandardCursors())
                .state(CONFIG::setRemapStandardCursors, CONFIG::isRemapStandardCursors)
                .active(workaroundsRef)
                .build();

        this.list.rowBuilder()
                .label(AGGRESSIVE_TEXT)
                .tooltip(AGGRESSIVE_INFO)
                .toggleBuilder()
                .defaultValue(defaults.isAggressiveCursor())
                .state(CONFIG::setAggressiveCursor, CONFIG::isAggressiveCursor)
                .build();

        FZMutableRef<Boolean> virtualModeRef = FZMutableRef.wrap(
                _ -> {
                    CursorsExtended.getInstance().getDisplay().toggleVirtual();
                    CONFIG.setVirtualMode(CursorsExtended.getInstance().getDisplay().isVirtual());
                },
                CursorsExtended.getInstance().getDisplay()::isVirtual
        );

        this.list.rowBuilder()
                .label(VIRTUAL_TEXT)
                .tooltip(VIRTUAL_INFO)
                .toggleBuilder()
                .defaultValue(defaults.isVirtualMode())
                .state(virtualModeRef)
                .build();

        this.list.rowBuilder()
                .label(NATIVE_ANIMATED_CURSORS_TEXT)
                .tooltip(NATIVE_ANIMATED_CURSORS_INFO)
                .toggleBuilder()
                .defaultValue(defaults.shouldAnimateCursorsNatively())
                .state(this::onUpdateNativeAnimatedCursors, () -> CONFIG.shouldAnimateCursorsNatively() && virtualModeRef.value())
                .active(virtualModeRef.map(value -> !value))
                .build();

        this.list.rowBuilder()
                .label(LEGACY_MODE_TEXT)
                .tooltip(LEGACY_MODE_INFO)
                .toggleBuilder()
                .defaultValue(defaults.isLegacyMode())
                .state(CONFIG::setLegacyMode, CONFIG::isLegacyMode)
                .build();
    }

    @Override
    public void onSearch(String search) {
        if (this.list != null) {
            this.list.onSearch(search);
        }
    }

    private void onUpdateNativeAnimatedCursors(boolean nativeAnimatedCursors) {
        CONFIG.setNativeAnimatedCursors(nativeAnimatedCursors);
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getCursors()) {
            CursorTexture cursorTexture = cursor.getTexture();
            if (cursorTexture != null && cursorTexture.metadata().animation() != null) {
                CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getOrCreateSettings(cursor));
            }
        }
    }
}
