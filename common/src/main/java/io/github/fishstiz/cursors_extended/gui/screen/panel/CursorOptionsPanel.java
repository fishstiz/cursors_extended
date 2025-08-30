package io.github.fishstiz.cursors_extended.gui.screen.panel;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.resource.CursorResourceLoader;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.AnimatedCursor;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.CursorAnimationHelper;
import io.github.fishstiz.cursors_extended.gui.screen.CatalogItem;
import io.github.fishstiz.cursors_extended.gui.widget.*;
import io.github.fishstiz.cursors_extended.gui.MouseEvent;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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
    private final CursorAnimationHelper animationHelper;
    private final Runnable refreshCursors;
    private final CatalogItem globalOptions;
    private final @NotNull Config.CursorSettings settings;
    private final @NotNull Cursor cursor;
    private final @NotNull CursorType holdCursorType;
    private GridLayout layout;
    private OptionsList optionsList;
    private ToggleWidget enableToggler;
    private SliderWidget scaleSlider;
    private ButtonWidget guiScaleButton;
    private SliderWidget xhotSlider;
    private SliderWidget yhotSlider;
    private ButtonWidget resetToDefaultsButton;
    private CursorHotspotWidget hotspotWidget;
    private CursorPreviewWidget previewWidget;
    private ToggleWidget hotspotGuideToggler;

    public CursorOptionsPanel(
            CursorAnimationHelper animationHelper,
            Runnable refreshCursors,
            CatalogItem globalOptions,
            Cursor cursor
    ) {
        super(Component.translatable("cursors_extended.options.cursor-type", cursor.getText()));

        this.animationHelper = animationHelper;
        this.refreshCursors = refreshCursors;
        this.globalOptions = globalOptions;
        this.settings = CursorsExtended.CONFIG.getOrCreateSettings(cursor);
        this.cursor = cursor;
        this.holdCursorType = CursorTypesExt.asHold(cursor.getType());
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

    private @NotNull LayoutElement setupFirstColumnWidgets() {
        this.optionsList = new OptionsList(this.getMinecraft(), Button.DEFAULT_HEIGHT, this.getSpacing());

        this.enableToggler = this.optionsList.addOption(new ToggleWidget(
                this.cursor.isLoaded() && this.cursor.isEnabled(),
                ENABLE_TEXT,
                this::onToggleEnable
        ));

        if (this.cursor.isLoaded()) {
            this.scaleSlider = this.optionsList.addOption(
                    new SliderWidget(
                            sanitizeScale(this.settings.getScale()),
                            SCALE_MIN,
                            SCALE_MAX,
                            SCALE_STEP,
                            this::onChangeScale,
                            SCALE_TEXT,
                            CommonComponents.EMPTY,
                            SettingsUtil::getAutoText,
                            this::onScaleMouseEvent
                    ),
                    this.bindGlobalInfo(GLOBAL_SCALE_TOOLTIP, CursorsExtended.CONFIG.getGlobal().isScaleActive())
            );
            this.guiScaleButton = this.optionsList.addOption(new ButtonWidget(GUI_SCALE_TEXT, this::setGuiScale));
            this.xhotSlider = this.optionsList.addOption(
                    new SliderWidget(
                            sanitizeHotspot(this.settings.getXHot(), this.cursor),
                            HOT_MIN,
                            getMaxHotspot(this.cursor),
                            HOT_STEP,
                            this::onChangeXHot,
                            XHOT_TEXT,
                            HOTSPOT_SUFFIX
                    ),
                    this.bindGlobalInfo(GLOBAL_XHOT_TOOLTIP, CursorsExtended.CONFIG.getGlobal().isXHotActive())
            );
            this.yhotSlider = this.optionsList.addOption(
                    new SliderWidget(
                            sanitizeHotspot(this.settings.getYHot(), this.cursor),
                            HOT_MIN,
                            getMaxHotspot(this.cursor),
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

            if (this.cursor instanceof AnimatedCursor animatedCursor) {
                this.optionsList.addOption(new ToggleWidget(
                        animatedCursor.isAnimated(),
                        ANIMATE_TEXT,
                        this::onToggleAnimate
                ));
                this.optionsList.addOption(new ButtonWidget(RESET_ANIMATION_TEXT, this::restartAnimation));
            }

            this.resetToDefaultsButton = this.optionsList.addOption(new ButtonWidget(RESET_DEFAULTS_TEXT, this::resetToDefaults));

            this.refreshGuiScaleButton(this.settings.getScale());
            this.refreshDefaultsButton();
        }

        return this.optionsList;
    }

    private @NotNull LayoutElement setupSecondColumnWidgets() {
        final int column = 0;
        int row = 0;

        GridLayout cursorWidgetsLayout = new GridLayout().spacing(this.getSpacing());

        if (this.cursor.isLoaded()) {
            this.hotspotWidget = cursorWidgetsLayout.addChild(
                    new CursorHotspotWidget(
                            this.cursor,
                            this.animationHelper,
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
        if (!this.cursor.isLoaded() && this.loadCursor(this.cursor)) {
            Cursor loaded = Objects.requireNonNull(CursorManager.INSTANCE.getCursor(this.cursor.getType()));
            loaded.enable(true);
            this.settings.setEnabled(true);
            this.refreshCursors.run();
            return;
        }

        if (this.cursor.isLoaded()) {
            this.cursor.enable(enabled);
            this.settings.setEnabled(enabled);
            this.refreshCursors.run();
        } else {
            target.setValue(false);
        }
    }

    private void onChangeScale(double scale) {
        this.cursor.setScale(scale);
        this.settings.setScale(scale);
        this.refreshGuiScaleButton(scale);
        this.refreshDefaultsButton();
    }

    private void onChangeXHot(double xhot) {
        this.cursor.setXHot(xhot);
        this.settings.setXHot(this.cursor, (int) xhot);
        this.refreshDefaultsButton();
    }

    private void onChangeYHot(double yhot) {
        this.cursor.setYHot(yhot);
        this.settings.setYHot(this.cursor, (int) yhot);
        this.refreshDefaultsButton();
    }

    private void onToggleAnimate(boolean animated) {
        if (!(this.cursor instanceof AnimatedCursor animatedCursor)) {
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
        if (this.cursor instanceof AnimatedCursor animatedCursor) {
            this.animationHelper.reset(animatedCursor);
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
        if (this.resetToDefaultsButton != null) {
            this.resetToDefaultsButton.active = !CursorResourceLoader.isResourceSetting(this.cursor, this.settings);
        }
    }

    private void resetToDefaults() {
        if (CursorResourceLoader.retoreActiveResourceSettings(Objects.requireNonNull(this.cursor))) {
            this.refreshCursors.run();
            if (this.enableToggler != null) {
                this.setFocused(this.enableToggler);
            }
        }
    }

    private void onScaleMouseEvent(SliderWidget target, MouseEvent mouseEvent, double mappedValue) {
        if ((mouseEvent.clicked() || mouseEvent.dragged()) && CursorManager.INSTANCE.isEnabled(this.holdCursorType)) {
            this.getMinecraft().getWindow().selectCursor(this.holdCursorType);
        }
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

    private static class OptionsList extends AbstractListWidget<OptionsList.Entry> implements Layout {
        private static final int BACKGROUND_PADDING_Y = 2;
        private final ElementSlidingBackground hoveredBackground = new ElementSlidingBackground(0x26FFFFFF); // 15% white

        private OptionsList(Minecraft minecraft, int itemHeight, int spacing) {
            super(minecraft, 0, 0, 0, itemHeight, spacing);
        }

        private <T extends AbstractWidget> @NotNull T addOption(@NotNull T optionWidget, @Nullable Function<T, AbstractWidget> decoration) {
            this.addEntry(new Entry(optionWidget, decoration != null ? decoration.apply(optionWidget) : null));
            return optionWidget;
        }

        private <T extends AbstractWidget> @NotNull T addOption(@NotNull T optionWidget) {
            return this.addOption(optionWidget, null);
        }

        @Override
        protected void renderListBackground(@NotNull GuiGraphics guiGraphics) {
            // remove background
        }

        @Override
        protected void renderListSeparators(@NotNull GuiGraphics guiGraphics) {
            // remove separators
        }

        @Override
        public void visitChildren(@NotNull Consumer<LayoutElement> visitor) {
            this.children().forEach(visitor);
        }

        @Override
        public void arrangeElements() {
            Layout.super.arrangeElements();
            this.clampScrollAmount();
        }

        protected void renderSlidingBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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
                int width = entry.getWidth() + paddingX + (this.scrollbarVisible() ? SCROLLBAR_WIDTH : BACKGROUND_PADDING_Y);
                int height = entry.getHeight() + BACKGROUND_PADDING_Y * 2;
                this.hoveredBackground.render(guiGraphics, x, y, width, height, partialTick);
            }

            guiGraphics.disableScissor();
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderSlidingBackground(guiGraphics, mouseX, mouseY, partialTick);
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }

        private class Entry extends AbstractListWidget<Entry>.Entry implements Layout {
            private final List<AbstractWidget> children = new ArrayList<>();
            private final AbstractWidget optionWidget;
            private final AbstractWidget decoration;

            private Entry(@NotNull AbstractWidget optionWidget, @Nullable AbstractWidget decoration) {
                this.optionWidget = optionWidget;
                this.decoration = decoration;

                if (this.decoration != null) {
                    this.children.add(this.decoration);
                }
                this.children.add(this.optionWidget);
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                this.optionWidget.setPosition(this.getX(), this.getY());
                this.optionWidget.render(guiGraphics, mouseX, mouseY, partialTick);

                if (this.decoration != null) {
                    this.decoration.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            }

            @Override
            public @NotNull List<AbstractWidget> children() {
                return this.children;
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return this.children;
            }

            @Override
            public void setX(int x) {
                this.optionWidget.setX(x);
            }

            @Override
            public void setY(int y) {
                this.optionWidget.setY(y);
            }

            @Override
            public void visitChildren(@NotNull Consumer<LayoutElement> visitor) {
                visitor.accept(this.optionWidget);
            }

            @Override
            public void visitWidgets(@NotNull Consumer<AbstractWidget> visitor) {
                visitor.accept(this.optionWidget);
            }

            @Override
            public void arrangeElements() {
                this.optionWidget.setWidth(this.getWidth());
            }
        }
    }
}
