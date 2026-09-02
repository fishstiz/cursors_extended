package io.github.fishstiz.cursors_extended.gui.panels;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.CursorState;
import io.github.fishstiz.cursors_extended.gui.components.*;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.utils.NavigationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.util.Objects;
import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class CursorOptionsPanel extends AbstractContentPanel {
    private static final Component ANIMATE_TEXT = Component.translatable("cursors_extended.options.animate");
    private static final Component RESET_ANIMATION_TEXT = Component.translatable("cursors_extended.options.animate-reset");
    private static final Component RESET_DEFAULTS_TEXT = Component.translatable("cursors_extended.options.reset-defaults");
    private static final Tooltip GLOBAL_SCALE_TOOLTIP = createGlobalTooltip(SCALE_TEXT);
    private static final Tooltip GLOBAL_XHOT_TOOLTIP = createGlobalTooltip(XHOT_TEXT);
    private static final Tooltip GLOBAL_YHOT_TOOLTIP = createGlobalTooltip(YHOT_TEXT);
    private final FZMutableRef<Boolean> hotspotGuide = new FZMutableRef<>(CONFIG.isShowHotspotGuide());
    private final Cursor cursor;
    private final Runnable globalRedirect;
    private final FZMutableRef<CursorState> state;
    private String lastFocusedId;
    private OptionsListWidget list;
    private CursorHotspotWidget hotspotWidget;
    private boolean scaling = false;

    public CursorOptionsPanel(Minecraft minecraft, Screen screen, Cursor cursor, Runnable globalRedirect) {
        Component title = Component.translatable("cursors_extended.options.cursor-type", cursor.text());
        Component shorthandTitle = cursor.text();
        super(cursor.text().toString(), minecraft, screen, title, shorthandTitle);
        this.state = new FZMutableRef<>(new CursorState(cursor));
        this.cursor = cursor;
        this.globalRedirect = globalRedirect;
    }

    private static Tooltip createGlobalTooltip(Component option) {
        return Tooltip.create(Component.translatable("cursors_extended.options.global.inactive.tooltip", option));
    }

    @Override
    protected void buildKeywords(Consumer<Component> builder) {
    }

    @Override
    protected void buildWidgets(GuiComponentCollector collector, FZFlexLayout layout) {
        state.set(new CursorState(cursor));

        OptionsListWidget list = layout.child(new OptionsListWidget(), layout.flexChildSettings());
        {
            FZButton enabledButton = list.addEntry(FZButton.bind("EnabledButton", state
                    .map(CursorState::enabled)
                    .map(value -> FZButton.builder()
                            .id("EnabledButton")
                            .message(CommonComponents.optionNameValue(ENABLE_TEXT, CommonComponents.optionStatus(value && cursor.hasTexture())))
                            .onPress(e -> {
                                this.lastFocusedId = e.target().fidgetz$componentId();

                                if (!cursor.hasTexture() && loadCursor(cursor)) {

                                    CONFIG.getOrCreateSettings(cursor).setEnabled(true);

                                    state.set(prev -> prev.enabled(true));
                                    buildWidgets();
                                    return;
                                }
                                if (this.cursor.hasTexture()) {
                                    boolean newValue = !value;
                                    CONFIG.getOrCreateSettings(cursor).setEnabled(newValue);
                                    state.set(prev -> prev.enabled(newValue));
                                }
                            })
                            .focusOnInteraction(value)
                            .toProps())));

            refocusListElement(list, enabledButton);

            if (!cursor.hasTexture()) {
                return;
            }

            FZSlider scaleSlider = list.addEntry(FZSlider.bind("ScaleSlider", state.map(CursorState::scale).map(value -> FZSlider.builder()
                    .label(SCALE_TEXT)
                    .min(SettingsUtil.SCALE_MIN)
                    .max(SettingsUtil.SCALE_MAX)
                    .step(SettingsUtil.SCALE_STEP)
                    .value(SettingsUtil.sanitizeScale(value))
                    .onChange(e -> state.set(prev -> prev.scale((float) e.value())))
                    .onFormat(e -> {
                        Component format = getAutoText(e.target().getValue());
                        if (format != null) e.format(format);
                    })
                    .onClick(_ -> this.scaling = true)
                    .onDrag(_ -> this.scaling = true)
                    .onRelease(_ -> this.scaling = false)
                    .active(!CONFIG.getGlobal().isScaleActive())
                    .toProps())));

            if (CONFIG.getGlobal().isScaleActive()) {
                collector.renderableWidget(new InactiveInfoWidget(scaleSlider, GLOBAL_SCALE_TOOLTIP, globalRedirect));
            }

            list.addEntry(FZButton.bind("AutoScaleButton", state.map(CursorState::scale).map(value -> FZButton.builder()
                    .message(GUI_SCALE_TEXT)
                    .onPress(() -> state.set(prev -> prev.scale(SettingsUtil.SCALE_AUTO_PREFERRED)))
                    .active(!SettingsUtil.isAutoScale(value) && !CONFIG.getGlobal().isScaleActive())
                    .toProps())));

            state.subscribe("ScaleSettings", CursorState::scale, this::onChangeScale);

            FZSlider xhotSlider = list.addEntry(FZSlider.bind("XHotSlider", state
                    .map(CursorState::hotspots)
                    .map(hotspots -> FZSlider.builder()
                            .min(SettingsUtil.HOT_MIN)
                            .max(SettingsUtil.getMaxXHot(cursor))
                            .step(SettingsUtil.HOT_STEP)
                            .value(SettingsUtil.sanitizeXHot(hotspots.x(), cursor))
                            .onChange(e -> onChangeHotspots(hotspots.x(((int) e.value()))))
                            .onFormat(e -> e.format(pixelValue(XHOT_TEXT, (int) e.target().getValue())))
                            .active(!CONFIG.getGlobal().isXHotActive())
                            .toProps())));

            if (CONFIG.getGlobal().isXHotActive()) {
                collector.renderableWidget(new InactiveInfoWidget(xhotSlider, GLOBAL_XHOT_TOOLTIP, globalRedirect));
            }

            FZSlider yhotSlider = list.addEntry(FZSlider.bind("YHotSlider", state
                    .map(CursorState::hotspots)
                    .map(hotspots -> FZSlider.builder()
                            .min(SettingsUtil.HOT_MIN)
                            .max(SettingsUtil.getMaxYHot(cursor))
                            .step(SettingsUtil.HOT_STEP)
                            .value(SettingsUtil.sanitizeYHot(hotspots.y(), cursor))
                            .onChange(e -> onChangeHotspots(hotspots.y(((int) e.value()))))
                            .onFormat(e -> e.format(pixelValue(YHOT_TEXT, (int) e.target().getValue())))
                            .active(!CONFIG.getGlobal().isYHotActive())
                            .toProps())));

            if (CONFIG.getGlobal().isXHotActive()) {
                collector.renderableWidget(new InactiveInfoWidget(yhotSlider, GLOBAL_YHOT_TOOLTIP, globalRedirect));
            }

            list.addEntry(FZButton.bind("HotspotGuideToggle", hotspotGuide.map(value -> FZButton.builder()
                    .message(CommonComponents.optionNameValue(HOTSPOT_GUIDE_TEXT, CommonComponents.optionStatus(value)))
                    .onPress(() -> hotspotGuide.set(prev -> !prev))
                    .toProps())));

            hotspotGuide.subscribe("HotspotGuideSettings", CONFIG::setShowHotspotGuide);

            CursorTexture cursorTexture = cursor.getTexture();

            if (cursorTexture != null && cursorTexture.metadata().animation() != null) {
                FZButton animateButton = list.addEntry(FZButton.bind("AnimateButton", state
                        .map(CursorState::animationEnabled)
                        .map(value -> FZButton.builder()
                                .id("AnimateButton")
                                .message(CommonComponents.optionNameValue(ANIMATE_TEXT, CommonComponents.optionStatus(value)))
                                .focusOnInteraction(false)
                                .onPress(e -> {
                                    this.lastFocusedId = e.target().fidgetz$componentId();
                                    state.set(prev -> prev.animated(!prev.animationEnabled()));
                                })
                                .toProps())));

                refocusListElement(list, animateButton);

                state.subscribe("AnimatedSettings", CursorState::animated, this::onChangeAnimated);

                list.addEntry(FZButton.builder()
                        .message(RESET_ANIMATION_TEXT)
                        .onPress(() -> {
                            if (cursorTexture instanceof CursorTexture.Animated animated) {
                                animated.restartAnimation();
                            }
                        })
                        .build());
            }

            if (cursorTexture != null) {
                FZButton resetButton = list.addEntry(FZButton.bind("ResetDefaultButton", state.map(value -> FZButton.builder()
                        .id("ResetDefaultButton")
                        .message(RESET_DEFAULTS_TEXT)
                        .onPress(e -> {
                            this.lastFocusedId = e.target().fidgetz$componentId();

                            Config.CursorSettings defaults = getDefaults();
                            Config.CursorSettings settings = CONFIG.getOrCreateSettings(cursor);

                            settings.mergeAll(defaults);
                            state.set(new CursorState(settings));

                            CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getGlobal().apply(defaults));


                            buildWidgets();
                        })
                        .active(!SettingsUtil.equalSettings(cursorTexture.metadata().cursor(), value, true))
                        .focusOnInteraction(false)
                        .toProps())));

                refocusListElement(list, resetButton);
            }

            list.addEntry(FZIcon.builder(Renderables.fill(CommonColors.DARK_GRAY)).height(1).build());

            list.addEntry(FZButton.builder()
                    .message(GLOBAL_SETTINGS_TEXT.copy().append(CommonComponents.ELLIPSIS))
                    .onPress(globalRedirect)
                    .build());
        }

        FZFlexLayout cursorWidgets = layout.child(FZFlexLayout.vertical(), layout.flexChildVerticalSettings());
        {
            cursorWidgets.spacing(DEFAULT_SPACING);

            CursorHotspotWidget hotspotWidget = new CursorHotspotWidget(
                    cursor,
                    state.map(CursorState::hotspots),
                    this::onChangeHotspots
            );
            hotspotWidget.active = !CONFIG.getGlobal().isYHotActive() && !CONFIG.getGlobal().isXHotActive();
            hotspotWidget.setRenderRuler(hotspotGuide.value());
            cursorWidgets.child(hotspotWidget);

            CursorPreviewWidget previewWidget = new CursorPreviewWidget(cursor, minecraft.font);
            previewWidget.setRenderRuler(hotspotGuide.value());
            cursorWidgets.child(previewWidget, cursorWidgets.flexChildSettings());

            hotspotGuide.subscribe("CursorWidgets", value -> {
                hotspotWidget.setRenderRuler(value);
                previewWidget.setRenderRuler(value);
            });

            this.hotspotWidget = hotspotWidget;
        }

        if (this.list != null) {
            this.list.onRemove();
            list.repositionEntries();
            list.setScrollAmount(this.list.scrollAmount());
        }

        this.list = list;
        this.lastFocusedId = null;
    }

    private Config.CursorSettings getDefaults() {
        Config.CursorSettings defaultSettings = new Config.CursorSettings();

        if (cursor.getTexture() != null) {
            defaultSettings.mergeAll(cursor.getTexture().metadata().cursor());
        }

        if (CONFIG.getGlobal().isScaleActive()) {
            defaultSettings.setScale(state.value().scale());
        }
        if (CONFIG.getGlobal().isXHotActive()) {
            defaultSettings.setXHot(cursor, state.value().xhot());
        }
        if (CONFIG.getGlobal().isYHotActive()) {
            defaultSettings.setYHot(cursor, state.value().yhot());
        }

        return defaultSettings;
    }

    private void onChangeScale(float scale) {
        CONFIG.getOrCreateSettings(cursor).setScale(scale);
        setScale(cursor, scale);
    }

    private void onChangeHotspots(CursorState.Hotspots hotspots) {
        hotspotGuide.set(true);
        int xhot = hotspots.x();
        int yhot = hotspots.y();

        if (!CONFIG.getGlobal().isXHotActive()) {
            CONFIG.getOrCreateSettings(cursor).setXHot(cursor, xhot);
        } else {
            xhot = CONFIG.getOrCreateSettings(cursor).xhot();
        }

        if (!CONFIG.getGlobal().isYHotActive()) {
            CONFIG.getOrCreateSettings(cursor).setYHot(cursor, yhot);
        } else {
            yhot = CONFIG.getOrCreateSettings(cursor).yhot();
        }

        final int xhotspot = xhot;
        final int yhotspot = yhot;
        setHotspots(cursor, xhotspot, yhotspot);
        state.set(prev -> prev.hotspots(xhotspot, yhotspot));
    }

    private void onChangeAnimated(Boolean animated) {
        CONFIG.getOrCreateSettings(cursor).setAnimated(animated);
        CursorsExtended.getInstance().getLoader().updateTexture(cursor, animated == null || animated);
        buildWidgets();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

        if (this.scaling) {
            graphics.requestCursor(CursorTypeUtil.arrowIfDefault(cursor.cursorType()));
        } else if (this.hotspotWidget != null) {
            CursorType cursorType = this.hotspotWidget.cursors_extended$cursorType(mouseX, mouseY);
            if (cursorType != CursorType.DEFAULT) {
                graphics.requestCursor(cursorType);
            }
        }
    }

    private <T extends FZComponent & GuiEventListener> void refocusListElement(OptionsListWidget newList, T component) {
        if (!newList.isFocused() &&
            this.lastFocusedId != null &&
            Objects.equals(component.fidgetz$componentId(), this.lastFocusedId)) {
            ComponentPath path = NavigationUtils.findPath(newList, component);
            if (path != null) {
                setFocused(newList);
                path.applyFocus(true);
            }
        }
    }
}
