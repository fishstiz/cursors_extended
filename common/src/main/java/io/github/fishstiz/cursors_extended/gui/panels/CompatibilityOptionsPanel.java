package io.github.fishstiz.cursors_extended.gui.panels;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.components.OptionsListWidget;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.fidgetz.v0.gui.components.FZText;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

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
    private static final Component REMAP_INFO = appendUnableToApply(
            Component.translatable("cursors_extended.options.compat.remap_cursors.info"),
            CONFIG.isWorkaroundsApplicable()
    );
    private static final Component WORKAROUNDS_TEXT = Component.translatable("cursors_extended.options.compat.workarounds");
    private static final Component WORKAROUNDS_INFO = appendUnableToApply(
            appendRestart(Component.translatable("cursors_extended.options.compat.workarounds.info")),
            CONFIG.isWorkaroundsApplicable()
    );
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

        FZMutableRef<Boolean> remapRef = FZMutableRef.wrap(
                CONFIG::setRemapStandardCursors,
                CONFIG::isRemapStandardCursors
        );
        FZMutableRef<Boolean> workaroundsRef = FZMutableRef.wrap(
                value -> {
                    CONFIG.setWorkarounds(value);
                    remapRef.notifySubscribers();
                },
                CONFIG::isWorkaroundsEnabled
        );

        this.list.rowBuilder()
                .flexWidget(FZText.bind(
                        "WorkaroundsLabel",
                        workaroundsRef.map(value -> FZText.builder(applyWorkaroundsStyle(WORKAROUNDS_TEXT, value))
                                .tooltip(WORKAROUNDS_INFO)
                                .toProps())))
                .keyword(WORKAROUNDS_TEXT)
                .tooltip(WORKAROUNDS_INFO)
                .toggleBuilder()
                .defaultValue(defaults.isWorkaroundsEnabled())
                .state(workaroundsRef)
                .build();

        this.list.rowBuilder()
                .flexWidget(FZText.bind(
                        "RemapLabel",
                        remapRef.map(value -> FZText.builder(applyWorkaroundsStyle(REMAP_TEXT, value))
                                .tooltip(REMAP_INFO)
                                .toProps())))
                .keyword(REMAP_TEXT)
                .tooltip(REMAP_INFO)
                .toggleBuilder()
                .defaultValue(defaults.isRemapStandardCursors())
                .state(remapRef)
                .active(workaroundsRef)
                .build();

        this.list.rowBuilder()
                .label(AGGRESSIVE_TEXT)
                .tooltip(AGGRESSIVE_INFO)
                .toggleBuilder()
                .defaultValue(defaults.isAggressiveCursor())
                .state(CONFIG::setAggressiveCursor, CONFIG::isAggressiveCursor)
                .build();

        FZMutableRef<Boolean> nativeAnimationsRef = FZMutableRef.wrap(
                this::onUpdateNativeAnimatedCursors,
                () -> CONFIG.shouldAnimateCursorsNatively() && !CONFIG.isVirtualMode()
        );
        FZMutableRef<Boolean> virtualModeRef = FZMutableRef.wrap(
                _ -> {
                    CursorsExtended.getInstance().getDisplay().toggleVirtual();
                    CONFIG.setVirtualMode(CursorsExtended.getInstance().getDisplay().isVirtual());
                    nativeAnimationsRef.notifySubscribers();
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
                .state(nativeAnimationsRef)
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

    private static Component applyWorkaroundsStyle(Component component, boolean active) {
        if (!CONFIG.isWorkaroundsApplicable()) {
            return component.copy().withColor(CommonColors.SOFT_RED);
        }

        return component.copy().withStyle(active ? ChatFormatting.WHITE : ChatFormatting.GRAY);
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
