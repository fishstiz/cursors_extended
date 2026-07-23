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
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class GlobalOptionsPanel extends AbstractContentPanel {
    private static final WidgetRenderables PREVIEW_CURSOR_SPRITES = new WidgetRenderables(
            Renderables.sprite(Identifier.fromNamespaceAndPath("fidgetz", "widget/popovermenu_entry")),
            Renderables.sprite(Identifier.fromNamespaceAndPath("fidgetz", "widget/popovermenu_entry")),
            Renderables.sprite(Identifier.fromNamespaceAndPath("fidgetz", "widget/popovermenu_entry_highlighted"))
    );
    private static final Component SCALE_INFO = createGlobalInfo(SCALE_TEXT);
    private static final Component XHOT_INFO = createGlobalInfo(XHOT_TEXT);
    private static final Component YHOT_INFO = createGlobalInfo(YHOT_TEXT);
    private static final Component ANIMATIONS_TEXT = Component.translatable("cursors_extended.options.global.animation");
    private static final Component ANIMATIONS_INFO = Component.translatable("cursors_extended.options.global.animation.tooltip");
    private static final Component RESET_TEXT = Component.translatable("cursors_extended.options.resource_pack.reset");
    private static final Component RESET_INFO = Component.translatable("cursors_extended.options.resource_pack.reset.tooltip");
    private final FZMutableRef<GlobalState> state = new FZMutableRef<>(new GlobalState());
    private final Map<String, FZMutableRef<CursorState>> cursorStates;
    private final FZMutableRef<Cursor> previewCursor = new FZMutableRef<>(CursorsExtended.getInstance().getRegistry().get(CursorType.DEFAULT));
    private final FZMutableRef<Boolean> hotspotGuide = new FZMutableRef<>(CONFIG.isShowHotspotGuide());
    private OptionsListWidget list;
    private CursorHotspotWidget hotspotWidget;
    private boolean scaling = false;

    public GlobalOptionsPanel(Minecraft minecraft, Screen screen, Map<String, FZMutableRef<CursorState>> cursorStates) {
        Component title = Component.translatable("cursors_extended.options.global.title");
        Component shorthandTitle = Component.translatable("cursors_extended.options.global");
        super("Global", minecraft, screen, title, shorthandTitle);
        this.cursorStates = cursorStates;
    }

    private static Component createGlobalInfo(Component message) {
        return Component.translatable("cursors_extended.options.global.tooltip", message);
    }

    @Override
    protected void buildKeywords(Consumer<Component> builder) {
        builder.accept(SCALE_TEXT);
        builder.accept(XHOT_TEXT);
        builder.accept(YHOT_TEXT);
        builder.accept(ANIMATIONS_TEXT);
        builder.accept(RESET_TEXT);
    }

    @Override
    protected void buildWidgets(GuiComponentCollector collector, FZFlexLayout layout) {
        FZFlexLayout center = layout.child(FZFlexLayout.vertical(), layout.flexChildSettings()).spacing(DEFAULT_SPACING);

        center.child(
                FZDropdown.bind("PreviewCursorDropdown", previewCursor.map(value -> FZDropdown.builder(this)
                        .message(CommonComponents.optionNameValue(Component.translatable("cursors_extended.options.preview"), value.text()))
                        .leftIcon(CursorRenderable.widgetElements(value, 16).marginLeft(4).marginRight(-12))
                        .height(24)
                        .entryDivider(null)
                        .entries(CursorsExtended.getInstance().getRegistry().getInternalCursors()
                                .stream()
                                .filter(Cursor::isTextureEnabled)
                                .map(cursor -> FZPopoverMenuItem.fromWidget(FZButton.builder()
                                        .sprites(PREVIEW_CURSOR_SPRITES)
                                        .leftIcon(CursorRenderable.widgetElements(cursor, 16))
                                        .message(cursor.text())
                                        .leftAlignedMessage()
                                        .active(value != cursor)
                                        .onPress(() -> previewCursor.set(cursor))
                                        .build()))
                                .collect(Collectors.toUnmodifiableList()))
                        .active(CONFIG.hasResourcePack())
                        .toProps())),
                center.flexChildHorizontalSettings()
        );

        this.list = center.child(new OptionsListWidget(), center.flexChildSettings());
        {
            list.rowBuilder()
                    .keyword(SCALE_TEXT)
                    .tooltip(SCALE_INFO)
                    .flexWidget(FZSlider.bind("ScaleSlider", state.map(GlobalState::scale).map(setting -> FZSlider.builder()
                            .label(SCALE_TEXT)
                            .min(SettingsUtil.SCALE_MIN)
                            .max(SettingsUtil.SCALE_MAX)
                            .step(SettingsUtil.SCALE_STEP)
                            .value(SettingsUtil.sanitizeScale(setting.value))
                            .onChange(e -> state.set(prev -> prev.scale((float) e.value())))
                            .onFormat(e -> {
                                Component format = getAutoText(e.target().getValue());
                                if (format != null) e.format(format);
                            })
                            .onDrag(_ -> {
                                Cursor cursor = previewCursor.value();
                                if (cursor.isTextureEnabled()) {
                                    cursor.cursorType().select();
                                }
                                this.scaling = true;
                            })
                            .onRelease(_ -> onReleaseScale())
                            .active(setting.active)
                            .toProps())))
                    .toggleBuilder()
                    .state(active -> state.set(prev -> prev.scaleActive(active)), () -> state.value().scale.active)
                    .build();

            list.addEntry(FZButton.bind("AutoScaleButton", state.map(GlobalState::scale).map(setting -> FZButton.builder()
                    .message(GUI_SCALE_TEXT)
                    .onPress(() -> {
                        state.set(prev -> prev.scale(SettingsUtil.SCALE_AUTO_PREFERRED));
                        onReleaseScale();
                    })
                    .active(!SettingsUtil.isAutoScale(setting.value) && CONFIG.getGlobal().isScaleActive())
                    .toProps())));

            state.subscribe("ScaleValue", GlobalState::scaleValue, this::onChangeScale);
            state.subscribe("ScaleActive", GlobalState::scaleActive, applyGlobalOnToggle(CONFIG.getGlobal()::setScaleActive));

            IntIntPair maxHotspots = getMaxHotspots();

            list.rowBuilder()
                    .keyword(XHOT_TEXT)
                    .tooltip(XHOT_INFO)
                    .flexWidget(FZSlider.bind("XHotSlider", state.map(GlobalState::xhot).map(setting -> FZSlider.builder()
                            .label(XHOT_TEXT)
                            .min(SettingsUtil.HOT_MIN)
                            .max(maxHotspots.firstInt())
                            .step(SettingsUtil.HOT_STEP)
                            .value(setting.value)
                            .onChange(e -> state.set(prev -> prev.xhot((int) e.value())))
                            .onFormat(e -> e.format(FZSlider.defaultValueFormat(e.target().getValue(), 0).copy().append(HOTSPOT_SUFFIX)))
                            .onRelease(_ -> onReleaseXHot())
                            .active(setting.active)
                            .toProps())))
                    .toggleBuilder()
                    .state(active -> state.set(prev -> prev.xhotActive(active)), () -> state.value().xhot.active)
                    .build();

            state.subscribe("XHotValue", GlobalState::xhotValue, this::onChangeXHot);
            state.subscribe("XHotActive", GlobalState::xhotActive, applyGlobalOnToggle(CONFIG.getGlobal()::setXHotActive));

            list.rowBuilder()
                    .keyword(YHOT_TEXT)
                    .tooltip(YHOT_INFO)
                    .flexWidget(FZSlider.bind("YHotSlider", state.map(GlobalState::yhot).map(setting -> FZSlider.builder()
                            .label(YHOT_TEXT)
                            .min(SettingsUtil.HOT_MIN)
                            .max(maxHotspots.secondInt())
                            .step(SettingsUtil.HOT_STEP)
                            .value(setting.value)
                            .onChange(e -> state.set(prev -> prev.yhot((int) e.value())))
                            .onFormat(e -> e.format(FZSlider.defaultValueFormat(e.target().getValue(), 0).copy().append(HOTSPOT_SUFFIX)))
                            .onRelease(_ -> onReleaseYHot())
                            .active(setting.active)
                            .toProps())))
                    .toggleBuilder()
                    .state(active -> state.set(prev -> prev.yhotActive(active)), () -> state.value().xhot.active)
                    .build();

            state.subscribe("YHotValue", GlobalState::yhotValue, this::onChangeYHot);
            state.subscribe("YHotActive", GlobalState::yhotActive, applyGlobalOnToggle(CONFIG.getGlobal()::setYHotActive));

            list.addEntry(FZButton.bind("HotspotGuideToggle", hotspotGuide.map(value -> FZButton.builder()
                    .message(CommonComponents.optionNameValue(HOTSPOT_GUIDE_TEXT, CommonComponents.optionStatus(value)))
                    .onPress(() -> hotspotGuide.set(prev -> !prev))
                    .toProps())));

            hotspotGuide.subscribe("HotspotGuideSettings", CONFIG::setShowHotspotGuide);

            list.rowBuilder()
                    .label(ANIMATIONS_TEXT)
                    .tooltip(ANIMATIONS_INFO)
                    .toggleBuilder()
                    .state(this::toggleCursorAnimations, GlobalOptionsPanel::isAnimatedAny)
                    .active(hasAnimationAny())
                    .build();

            list.rowBuilder()
                    .keyword(RESET_TEXT)
                    .tooltip(RESET_INFO)
                    .flexWidget(FZButton.builder()
                            .message(RESET_TEXT)
                            .leftAlignedMessage()
                            .onPress(this::resetCursorSettings)
                            .build())
                    .buildRow();
        }

        FZFlexLayout cursorWidgets = layout.child(FZFlexLayout.vertical(), layout.flexChildVerticalSettings());
        {
            cursorWidgets.spacing(DEFAULT_SPACING);

            CursorHotspotWidget hotspotWidget = new CursorHotspotWidget(
                    previewCursor.value(),
                    previewCursor.map(cursor -> new CursorState(cursor).hotspots()),
                    this::onChangeHotspots
            );
            hotspotWidget.setGlobalMode(true);
            hotspotWidget.setRenderRuler(hotspotGuide.value());
            hotspotWidget.setOnRelease(this::onReleaseHotspots);

            cursorWidgets.child(hotspotWidget);

            CursorPreviewWidget previewWidget = new CursorPreviewWidget(previewCursor.value(), minecraft.font);
            previewWidget.setRenderRuler(CONFIG.isShowHotspotGuide());
            cursorWidgets.child(previewWidget, cursorWidgets.flexChildSettings());

            previewCursor.subscribe("CursorPreviewWidget", value -> {
                hotspotWidget.setCursor(value);
                previewWidget.setCursor(value);
            });

            hotspotGuide.subscribe("CursorWidgets", value -> {
                hotspotWidget.setRenderRuler(value);
                previewWidget.setRenderRuler(value);
            });

            this.hotspotWidget = hotspotWidget;
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

        if (this.scaling && previewCursor.value() != null) {
            graphics.requestCursor(CursorTypeUtil.arrowIfDefault(previewCursor.value().cursorType()));
        } else if (this.hotspotWidget != null) {
            CursorType cursorType = this.hotspotWidget.cursors_extended$cursorType(mouseX, mouseY);
            if (cursorType != CursorType.DEFAULT) {
                graphics.requestCursor(cursorType);
            }
        }
    }

    @Override
    public void onRemove() {
        this.scaling = false;
    }

    @Override
    public void onSearch(String search) {
        if (this.list != null) {
            this.list.onSearch(search);
        }
    }

    private void onChangeScale(float scale) {
        CONFIG.getGlobal().setScale(scale);
        if (CONFIG.getGlobal().isScaleActive()) {
            setScale(previewCursor.value(), scale);
        }
    }

    private void onReleaseScale() {
        this.scaling = false;
        if (CONFIG.getGlobal().isScaleActive()) {
            CursorsExtended.getInstance().getRegistry().getInternalCursors().forEach(
                    cursor -> setScale(cursor, CONFIG.getGlobal().scale())
            );
        }
    }

    private void onChangeXHot(int xhot) {
        hotspotGuide.set(true);

        CONFIG.getGlobal().setXHot(xhot);
        if (CONFIG.getGlobal().isXHotActive()) {
            setXHot(previewCursor.value(), xhot);
        }
    }

    private void onReleaseXHot() {
        if (CONFIG.getGlobal().isXHotActive()) {
            CursorsExtended.getInstance().getRegistry().getInternalCursors().forEach(
                    cursor -> setXHot(cursor, CONFIG.getGlobal().xhot())
            );
        }
    }

    private void onChangeYHot(int yhot) {
        hotspotGuide.set(true);

        CONFIG.getGlobal().setYHot(yhot);
        if (CONFIG.getGlobal().isYHotActive()) {
            setYHot(previewCursor.value(), yhot);
        }
    }

    private void onReleaseYHot() {
        if (CONFIG.getGlobal().isYHotActive()) {
            CursorsExtended.getInstance().getRegistry().getInternalCursors().forEach(
                    cursor -> setYHot(cursor, CONFIG.getGlobal().yhot())
            );
        }
    }

    private void onChangeHotspots(CursorState.Hotspots hotspots) {
        state.set(prev -> prev.hotspots(hotspots.x(), hotspots.y()));

        hotspotGuide.set(true);

        if (CONFIG.getGlobal().isXHotActive() && CONFIG.getGlobal().isYHotActive()) {
            CONFIG.getGlobal().setXHot(hotspots.x());
            CONFIG.getGlobal().setYHot(hotspots.y());
            setHotspots(previewCursor.value(), hotspots.x(), hotspots.y());
        } else if (CONFIG.getGlobal().isXHotActive()) {
            onChangeXHot(hotspots.x());
        } else if (CONFIG.getGlobal().isYHotActive()) {
            onChangeYHot(hotspots.y());
        }
    }

    private void onReleaseHotspots() {
        Set<Cursor> cursors = CursorsExtended.getInstance().getRegistry().getInternalCursors();
        if (CONFIG.getGlobal().isXHotActive() && CONFIG.getGlobal().isYHotActive()) {
            cursors.forEach(cursor -> setHotspots(cursor, CONFIG.getGlobal().xhot(), CONFIG.getGlobal().yhot()));
        } else if (CONFIG.getGlobal().isXHotActive()) {
            onReleaseXHot();
        } else if (CONFIG.getGlobal().isYHotActive()) {
            onReleaseYHot();
        }
    }

    private void toggleCursorAnimations(boolean animated) {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            CursorTexture texture = cursor.getTexture();
            if (texture != null && texture.metadata().animation() != null) {
                CursorsExtended.getInstance().getLoader().updateTexture(cursor, animated);
                CONFIG.getOrCreateSettings(cursor).setAnimated(animated);
                FZMutableRef<CursorState> cursorState = cursorStates.get(cursor.name());
                if (cursorState != null) {
                    cursorState.set(prev -> prev.animated(animated));
                }
            }
        }
    }

    private void resetCursorSettings() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getCursors()) {
            CursorTexture texture = cursor.getTexture();
            if (texture != null) {
                Config.CursorSettings settings = CONFIG.getOrCreateSettings(cursor);
                settings.mergeAll(texture.metadata().cursor());
                FZMutableRef<CursorState> cursorState = cursorStates.get(cursor.name());
                if (cursorState != null) {
                    cursorState.set(new CursorState(settings));
                }

                CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getGlobal().apply(settings));
            }
        }
    }

    public static boolean hasAnimationAny() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            CursorTexture texture = cursor.getTexture();
            if (texture != null && texture.metadata().animation() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAnimatedAny() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (cursor.getTexture() instanceof CursorTexture.Animated) {
                return true;
            }
        }
        return false;
    }

    private static IntIntPair getMaxHotspots() {
        int currentMaxX = -1;
        int currentMaxY = -1;

        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (cursor.hasTexture()) {
                int maxXHot = SettingsUtil.getMaxXHot(cursor);
                if (maxXHot > currentMaxX) {
                    currentMaxX = maxXHot;
                }

                int maxYHot = SettingsUtil.getMaxYHot(cursor);
                if (maxYHot > currentMaxY) {
                    currentMaxY = maxYHot;
                }
            }
        }
        return IntIntPair.of(currentMaxX != -1 ? currentMaxX : 0, currentMaxY != -1 ? currentMaxY : 0);
    }

    private static Consumer<Boolean> applyGlobalOnToggle(Consumer<Boolean> onToggle) {
        return value -> {
            onToggle.accept(value);
            CursorsExtended.getInstance().getRegistry().getInternalCursors().forEach(cursor ->
                    CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getGlobal().apply(CONFIG.getOrCreateSettings(cursor)))
            );
        };
    }

    private record GlobalState(FloatSetting scale, IntSetting xhot, IntSetting yhot) {
        private record FloatSetting(float value, boolean active) {
        }

        private record IntSetting(int value, boolean active) {
        }

        private GlobalState() {
            Config.GlobalSettings global = CONFIG.getGlobal();
            FloatSetting scale = new FloatSetting(global.scale(), global.isScaleActive());
            IntSetting xhot = new IntSetting(global.xhot(), global.isXHotActive());
            IntSetting yhot = new IntSetting(global.yhot(), global.isYHotActive());
            this(scale, xhot, yhot);
        }

        private float scaleValue() {
            return scale.value;
        }

        private int xhotValue() {
            return xhot.value;
        }

        private int yhotValue() {
            return yhot.value;
        }

        private boolean scaleActive() {
            return scale.active;
        }

        private boolean xhotActive() {
            return xhot.active;
        }

        private boolean yhotActive() {
            return yhot.active;
        }

        private GlobalState scale(float scale) {
            return new GlobalState(new FloatSetting(scale, this.scale.active), xhot, yhot);
        }

        private GlobalState scaleActive(boolean active) {
            return new GlobalState(new FloatSetting(scale.value, active), xhot, yhot);
        }

        private GlobalState xhot(int xhot) {
            return new GlobalState(scale, new IntSetting(xhot, this.xhot.active), yhot);
        }

        private GlobalState xhotActive(boolean active) {
            return new GlobalState(scale, new IntSetting(xhot.value, active), yhot);
        }

        private GlobalState yhot(int yhot) {
            return new GlobalState(scale, xhot, new IntSetting(yhot, this.yhot.active));
        }

        private GlobalState yhotActive(boolean active) {
            return new GlobalState(scale, xhot, new IntSetting(yhot.value, active));
        }

        private GlobalState hotspots(int xhot, int yhot) {
            return new GlobalState(scale, new IntSetting(xhot, this.xhot.active), new IntSetting(yhot, this.yhot.active));
        }
    }
}
