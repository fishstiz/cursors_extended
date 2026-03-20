package io.github.fishstiz.cursors_extended.gui.screen.panel;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.screen.CatalogItem;
import io.github.fishstiz.cursors_extended.gui.widget.*;
import io.github.fishstiz.cursors_extended.gui.MouseEvent;
import io.github.fishstiz.cursors_extended.resource.texture.AnimatedCursorTexture;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;
import static io.github.fishstiz.cursors_extended.util.SettingsUtil.*;

public class CursorOptionsPanel extends AbstractOptionsPanel {
    private static final Component HOTSPOT_GUIDE_TEXT = Component.translatable("cursors_extended.options.hotspot-guide");
    private static final Component ANIMATE_TEXT = Component.translatable("cursors_extended.options.animate");
    private static final Component RESET_ANIMATION_TEXT = Component.translatable("cursors_extended.options.animate-reset");
    private static final Component RESET_DEFAULTS_TEXT = Component.translatable("cursors_extended.options.reset-defaults");
    private static final Tooltip GLOBAL_SCALE_TOOLTIP = createGlobalTooltip(SCALE_TEXT);
    private static final Tooltip GLOBAL_XHOT_TOOLTIP = createGlobalTooltip(XHOT_TEXT);
    private static final Tooltip GLOBAL_YHOT_TOOLTIP = createGlobalTooltip(YHOT_TEXT);
    private static final int CELL_SIZE_STEP = 32;
    private final Runnable refreshCursors;
    private final CatalogItem globalOptions;
    private final Config.CursorSettings settings;
    private final Cursor cursor;
    private GridLayout layout;
    private OptionsList optionsList;
    private SliderWidget scaleSlider;
    private ButtonWidget guiScaleButton;
    private SliderWidget xhotSlider;
    private SliderWidget yhotSlider;
    private ButtonWidget resetToDefaultsButton;
    private CursorHotspotWidget hotspotWidget;
    private CursorPreviewWidget previewWidget;
    private ToggleWidget hotspotGuideToggler;
    private boolean scaling = false;

    public CursorOptionsPanel(
            Runnable refreshCursors,
            CatalogItem globalOptions,
            Cursor cursor
    ) {
        super(Component.translatable("cursors_extended.options.cursor-type", cursor.text()));

        this.refreshCursors = refreshCursors;
        this.globalOptions = globalOptions;
        this.settings = CursorsExtended.CONFIG.getOrCreateSettings(cursor);
        this.cursor = cursor;
    }

    @Override
    protected void initContents() {
        final int row = 0;
        int column = 0;

        this.layout = new GridLayout().spacing(this.getSpacing());
        this.layout.addChild(this.setupFirstColumnWidgets(), row, column);
        this.layout.addChild(this.setupSecondColumnWidgets(), row, ++column);
        this.layout.visitWidgets(this::addRenderableWidget);
    }

    private @NonNull LayoutElement setupFirstColumnWidgets() {
        this.optionsList = new OptionsList(this.getMinecraft(), Button.DEFAULT_HEIGHT, this.getSpacing());

        this.optionsList.addOption(new ToggleWidget(
                this.cursor.isTextureEnabled(),
                ENABLE_TEXT,
                this::onToggleEnable
        ));

        if (this.cursor.getTexture() != null) {
            this.scaleSlider = this.optionsList.addOption(
                    new SliderWidget(
                            sanitizeScale(this.settings.scale()),
                            SCALE_MIN,
                            SCALE_MAX,
                            SCALE_STEP,
                            this::onChangeScale,
                            SCALE_TEXT,
                            CommonComponents.EMPTY,
                            AbstractOptionsPanel::getAutoText,
                            this::onScaleMouseEvent
                    ),
                    this.bindGlobalInfo(GLOBAL_SCALE_TOOLTIP, CursorsExtended.CONFIG.getGlobal().isScaleActive())
            );
            this.guiScaleButton = this.optionsList.addOption(new ButtonWidget(GUI_SCALE_TEXT, this::setGuiScale));
            this.xhotSlider = this.optionsList.addOption(
                    new SliderWidget(
                            sanitizeXHot(this.settings.xhot(), this.cursor),
                            HOT_MIN,
                            getMaxXHot(this.cursor),
                            HOT_STEP,
                            this::onChangeXHot,
                            XHOT_TEXT,
                            HOTSPOT_SUFFIX
                    ),
                    this.bindGlobalInfo(GLOBAL_XHOT_TOOLTIP, CursorsExtended.CONFIG.getGlobal().isXHotActive())
            );
            this.yhotSlider = this.optionsList.addOption(
                    new SliderWidget(
                            sanitizeYHot(this.settings.yhot(), this.cursor),
                            HOT_MIN,
                            getMaxYHot(this.cursor),
                            HOT_STEP,
                            this::onChangeYHot,
                            YHOT_TEXT,
                            HOTSPOT_SUFFIX
                    ),
                    this.bindGlobalInfo(GLOBAL_YHOT_TOOLTIP, CursorsExtended.CONFIG.getGlobal().isYHotActive())
            );
            this.hotspotGuideToggler = this.optionsList.addOption(new ToggleWidget(
                    CursorsExtended.CONFIG.isShowHotspotGuide(),
                    HOTSPOT_GUIDE_TEXT,
                    this::onToggleGuide
            ));

            if (this.cursor.getTexture() instanceof AnimatedCursorTexture animatedCursor) {
                this.optionsList.addOption(new ToggleWidget(animatedCursor.animated(), ANIMATE_TEXT, this::onToggleAnimate));
                this.optionsList.addOption(new ButtonWidget(RESET_ANIMATION_TEXT, this::restartAnimation));
            }

            this.resetToDefaultsButton = this.optionsList.addOption(new ButtonWidget(RESET_DEFAULTS_TEXT, this::resetToDefaults));

            this.refreshGuiScaleButton(this.settings.scale());
            this.refreshDefaultsButton();
        }

        return this.optionsList;
    }

    private @NonNull LayoutElement setupSecondColumnWidgets() {
        final int column = 0;
        int row = 0;

        GridLayout cursorWidgetsLayout = new GridLayout().spacing(this.getSpacing());

        if (this.cursor.getTexture() != null) {
            this.hotspotWidget = cursorWidgetsLayout.addChild(
                    new CursorHotspotWidget(
                            this.cursor,
                            Objects.requireNonNull(this.xhotSlider),
                            Objects.requireNonNull(this.yhotSlider),
                            this::onHotspotWidgetMouseEvent
                    ),
                    row,
                    column
            );
            this.hotspotWidget.setRenderRuler(CursorsExtended.CONFIG.isShowHotspotGuide());
            this.previewWidget = cursorWidgetsLayout.addChild(
                    new CursorPreviewWidget(this.cursor, this.getFont()),
                    ++row,
                    column
            );
            this.previewWidget.setRenderRuler(CursorsExtended.CONFIG.isShowHotspotGuide());
        }

        return cursorWidgetsLayout;
    }

    @Override
    protected void repositionContents(int x, int y) {
        if (this.layout != null) {
            int cellSize = clampCell(this.getWidth() / 2 - this.getSpacing() / 2);
            this.layout.visitWidgets(widget -> widget.setWidth(cellSize));

            if (this.optionsList != null) {
                this.optionsList.setHeight(this.computeMaxHeight(y));
            }
            if (this.hotspotWidget != null) {
                this.hotspotWidget.setHeight(cellSize);
            }

            this.layout.setPosition(x, y);
            this.layout.arrangeElements();

            if (this.previewWidget != null) {
                this.previewWidget.setHeight(Math.min(cellSize, this.getBottom() - this.previewWidget.getY()));
            }
        }
    }

    private <T extends AbstractWidget> Function<T, AbstractWidget> bindGlobalInfo(Tooltip tooltip, boolean global) {
        return widget -> {
            if (widget != null && global) {
                widget.active = false;
                return new InactiveInfoWidget(widget, tooltip, this::toGlobalOptionsPanel);
            }
            return null;
        };
    }

    private void toGlobalOptionsPanel() {
        this.changeItem(this.globalOptions);
    }

    private void onToggleEnable(ToggleWidget target, boolean enabled) {
        if (!this.cursor.hasTexture() && loadCursor(this.cursor)) {
            this.settings.setEnabled(true);
            this.refreshCursors.run();
            return;
        }

        if (this.cursor.hasTexture()) {
            this.settings.setEnabled(enabled);
            this.refreshCursors.run();
        } else {
            target.setValue(false);
        }
    }

    private void onChangeScale(double scale) {
        setScale(cursor, (float) scale);
        this.settings.setScale((float) scale);
        this.refreshGuiScaleButton(scale);
        this.refreshDefaultsButton();
    }

    private void onChangeXHot(double xhot) {
        setXHot(cursor, (int) xhot);
        this.settings.setXHot(this.cursor, (int) xhot);
        this.refreshDefaultsButton();
    }

    private void onChangeYHot(double yhot) {
        setYHot(cursor, (int) yhot);
        this.settings.setYHot(this.cursor, (int) yhot);
        this.refreshDefaultsButton();
    }

    private void onToggleAnimate(boolean animated) {
        if (!(this.cursor.getTexture() instanceof AnimatedCursorTexture animatedCursor)) {
            throw new IllegalStateException("Cursor is not an animated cursor");
        }
        animatedCursor.setAnimated(animated);
        this.settings.setAnimated(animated);
        this.refreshDefaultsButton();
    }

    private void onToggleGuide(boolean shown) {
        CursorsExtended.CONFIG.setShowHotspotGuide(shown);
        this.hotspotWidget.setRenderRuler(shown);
        this.previewWidget.setRenderRuler(shown);
    }

    private void restartAnimation() {
        if (this.cursor.getTexture() instanceof AnimatedCursorTexture animatedCursor) {
            animatedCursor.restartAnimation();
        }
    }

    private void setGuiScale(Button target) {
        target.setFocused(false);
        target.active = false;
        if (this.scaleSlider != null) {
            this.scaleSlider.applyMappedValue(SCALE_AUTO_PREFERRED);
            this.setFocused(this.scaleSlider);
        }
    }

    private void refreshGuiScaleButton(double scale) {
        if (this.guiScaleButton != null) {
            this.guiScaleButton.active = !isAutoScale(scale) && !CursorsExtended.CONFIG.getGlobal().isScaleActive();
        }
    }

    private void refreshDefaultsButton() {
        if (this.resetToDefaultsButton != null && this.cursor.getTexture() != null) {
            this.resetToDefaultsButton.active = !equalSettings(this.cursor.getTexture().metadata().cursor(), this.settings, true);
        }
    }

    private void resetToDefaults() {
        CursorTexture texture = cursor.getTexture();
        if (texture == null) {
            return;
        }

        Config.CursorSettings defaults = getDefaults();
        this.settings.mergeAll(defaults);
        CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getGlobal().apply(defaults));

        this.refreshCursors.run();
    }

    private Config.CursorSettings getDefaults() {
        Config.CursorSettings defaultSettings = new Config.CursorSettings();

        if (cursor.getTexture() != null) {
            defaultSettings.mergeAll(cursor.getTexture().metadata().cursor());
        }

        if (CONFIG.getGlobal().isScaleActive()) {
            defaultSettings.setScale(settings.scale());
        }
        if (CONFIG.getGlobal().isXHotActive()) {
            defaultSettings.setXHot(cursor, settings.xhot());
        }
        if (CONFIG.getGlobal().isYHotActive()) {
            defaultSettings.setYHot(cursor, settings.yhot());
        }

        return defaultSettings;
    }

    private void onScaleMouseEvent(SliderWidget target, MouseEvent mouseEvent, double mappedValue) {
        this.scaling = (mouseEvent.clicked() || mouseEvent.dragged()) && this.cursor.isTextureEnabled();
    }

    private void onHotspotWidgetMouseEvent(CursorHotspotWidget target, MouseEvent mouseEvent, int xhot, int yhot) {
        if (mouseEvent.clicked() || mouseEvent.dragged()) {
            target.setRenderRuler(true);
            CursorsExtended.CONFIG.setShowHotspotGuide(true);
            if (this.hotspotGuideToggler != null) {
                this.hotspotGuideToggler.setValue(true);
            }
        }
    }

    private static int clampCell(int cell) {
        return Math.round(cell / (float) CELL_SIZE_STEP) * CELL_SIZE_STEP;
    }

    private static Tooltip createGlobalTooltip(Component option) {
        return Tooltip.create(Component.translatable("cursors_extended.options.global.inactive.tooltip", option));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        if (this.scaling) {
            guiGraphics.requestCursor(CursorTypeUtil.arrowIfDefault(this.cursor.cursorType()));
        } else if (this.hotspotWidget != null) {
            CursorType cursorType = this.hotspotWidget.cursors_extended$cursorType(mouseX, mouseY);
            if (cursorType != CursorType.DEFAULT) {
                guiGraphics.requestCursor(cursorType);
            }
        }
    }

    private static class OptionsList extends AbstractListWidget<OptionsList.Entry> implements Layout {
        private static final int BACKGROUND_PADDING_Y = 2;
        private final ElementSlidingBackground hoveredBackground = new ElementSlidingBackground(0x26FFFFFF); // 15% white

        private OptionsList(Minecraft minecraft, int itemHeight, int spacing) {
            super(minecraft, 0, 0, 0, itemHeight, spacing);
        }

        private <T extends AbstractWidget> @NonNull T addOption(@NonNull T optionWidget, @Nullable Function<T, AbstractWidget> decoration) {
            this.addEntry(new Entry(optionWidget, decoration != null ? decoration.apply(optionWidget) : null));
            return optionWidget;
        }

        private <T extends AbstractWidget> @NonNull T addOption(@NonNull T optionWidget) {
            return this.addOption(optionWidget, null);
        }

        @Override
        protected void extractListBackground(@NonNull GuiGraphicsExtractor guiGraphics) {
            // remove background
        }

        @Override
        protected void extractListSeparators(@NonNull GuiGraphicsExtractor guiGraphics) {
            // remove separators
        }

        @Override
        public void visitChildren(@NonNull Consumer<LayoutElement> visitor) {
            this.children().forEach(visitor);
        }

        @Override
        public void arrangeElements() {
            Layout.super.arrangeElements();
            this.clampScrollAmount();
        }

        protected void renderSlidingBackground(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            int paddingX = this.rowGap;

            int minX = this.getX() - paddingX;
            int minY = this.getY() - BACKGROUND_PADDING_Y;
            int maxX = this.getRight() + SCROLLBAR_WIDTH;
            int maxY = this.getBottom() + BACKGROUND_PADDING_Y;
            guiGraphics.enableScissor(minX, minY, maxX, maxY);

            Entry entry = this.getEntryAtPosition(mouseX, mouseY);
            if (entry == null) {
                this.hoveredBackground.reset();
            } else {
                int x = entry.getX() - paddingX;
                int y = entry.getY() - BACKGROUND_PADDING_Y;
                int width = entry.getWidth() + paddingX + (this.scrollable() ? SCROLLBAR_WIDTH : BACKGROUND_PADDING_Y);
                int height = entry.getHeight() + BACKGROUND_PADDING_Y * 2;
                this.hoveredBackground.render(guiGraphics, x, y, width, height, partialTick);
            }

            guiGraphics.disableScissor();
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderSlidingBackground(guiGraphics, mouseX, mouseY, partialTick);
            super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }

        private class Entry extends AbstractListWidget<Entry>.Entry implements Layout {
            private final List<AbstractWidget> children = new ArrayList<>();
            private final AbstractWidget optionWidget;
            private final AbstractWidget decoration;

            private Entry(@NonNull AbstractWidget optionWidget, @Nullable AbstractWidget decoration) {
                this.optionWidget = optionWidget;
                this.decoration = decoration;

                if (this.decoration != null) {
                    this.children.add(this.decoration);
                }
                this.children.add(this.optionWidget);
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                this.optionWidget.setPosition(this.getX(), this.getY());
                this.optionWidget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

                if (this.decoration != null) {
                    this.decoration.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
                }
            }

            @Override
            public @NonNull List<AbstractWidget> children() {
                return this.children;
            }

            @Override
            public @NonNull List<? extends NarratableEntry> narratables() {
                return this.children;
            }

            @Override
            public void visitChildren(@NonNull Consumer<LayoutElement> visitor) {
                visitor.accept(this.optionWidget);
            }

            @Override
            public void visitWidgets(@NonNull Consumer<AbstractWidget> visitor) {
                visitor.accept(this.optionWidget);
            }

            @Override
            public void arrangeElements() {
                this.optionWidget.setWidth(this.getWidth());
            }
        }
    }
}
