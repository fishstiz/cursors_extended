package io.github.fishstiz.cursors_extended.gui.screen.panel;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.datafixers.util.Pair;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorRegistry;
import io.github.fishstiz.cursors_extended.gui.MouseEvent;
import io.github.fishstiz.cursors_extended.gui.widget.ButtonWidget;
import io.github.fishstiz.cursors_extended.gui.widget.CursorPreviewWidget;
import io.github.fishstiz.cursors_extended.gui.widget.OptionsListWidget;
import io.github.fishstiz.cursors_extended.gui.widget.SliderWidget;
import io.github.fishstiz.cursors_extended.resource.texture.AnimatedCursorTexture;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class GlobalOptionsPanel extends AbstractOptionsPanel {
    private static final Component TITLE = Component.translatable("cursors_extended.options.global.title");
    private static final Tooltip SCALE_TOOLTIP = createGlobalTooltip(SCALE_TEXT);
    private static final Tooltip XHOT_TOOLTIP = createGlobalTooltip(XHOT_TEXT);
    private static final Tooltip YHOT_TOOLTIP = createGlobalTooltip(YHOT_TEXT);
    private static final Component ANIMATIONS_TEXT = Component.translatable("cursors_extended.options.global.animation");
    private static final Tooltip ANIMATIONS_INFO = Tooltip.create(Component.translatable("cursors_extended.options.global.animation.tooltip"));
    private static final Component RESET_TEXT = Component.translatable("cursors_extended.options.resource_pack.reset");
    private static final Tooltip RESET_INFO = Tooltip.create(Component.translatable("cursors_extended.options.resource_pack.reset.tooltip"));
    private static final int PREVIEW_BUTTON_SIZE = 20;
    private final Runnable refreshCursors;
    private @NotNull Iterator<Cursor> cursors = cursorIterator();
    private @NotNull Cursor currentCursor = getDefaultCursor();
    private OptionsListWidget optionList;
    private GlobalPreviewWidget previewWidget;
    private boolean scaling = false;

    public GlobalOptionsPanel(Runnable refreshCursors) {
        super(TITLE);
        this.refreshCursors = refreshCursors;
    }

    @Override
    protected void initContents() {
        this.recycleCursors();
        final Pair<Integer, Integer> maxHotspot = getMaxHotspots();

        this.previewWidget = new GlobalPreviewWidget(
                this.currentCursor,
                this.getFont(),
                new ButtonWidget(CommonComponents.EMPTY, this::cycleOnPreviewPress).withSize(PREVIEW_BUTTON_SIZE)
        );

        this.optionList = new OptionsListWidget(this.getMinecraft(), this.getFont(), Button.DEFAULT_HEIGHT, this.getSpacing());
        this.optionList.addToggleableSlider(
                new SliderWidget(
                        CONFIG.getGlobal().scale(),
                        SettingsUtil.SCALE_MIN,
                        SettingsUtil.SCALE_MAX,
                        SettingsUtil.SCALE_STEP,
                        this::onChangeScale,
                        this.index(SCALE_TEXT),
                        CommonComponents.EMPTY,
                        SettingsUtil::getAutoText,
                        this::onSliderMouseEvent
                ),
                CONFIG.getGlobal().isScaleActive(),
                applyGlobalOnToggle(CONFIG.getGlobal()::setScaleActive),
                SCALE_TOOLTIP
        );
        this.optionList.addToggleableSlider(
                new SliderWidget(
                        CONFIG.getGlobal().xhot(),
                        SettingsUtil.HOT_MIN,
                        maxHotspot.getFirst(),
                        SettingsUtil.HOT_STEP,
                        this::onChangeXHot,
                        this.index(XHOT_TEXT),
                        HOTSPOT_SUFFIX,
                        null,
                        this::onSliderMouseEvent
                ),
                CONFIG.getGlobal().isXHotActive(),
                applyGlobalOnToggle(CONFIG.getGlobal()::setXHotActive),
                XHOT_TOOLTIP
        );
        this.optionList.addToggleableSlider(
                new SliderWidget(
                        CONFIG.getGlobal().yhot(),
                        SettingsUtil.HOT_MIN,
                        maxHotspot.getSecond(),
                        SettingsUtil.HOT_STEP,
                        this::onChangeYHot,
                        this.index(YHOT_TEXT),
                        HOTSPOT_SUFFIX,
                        null,
                        this::onSliderMouseEvent
                ),
                CONFIG.getGlobal().isYHotActive(),
                applyGlobalOnToggle(CONFIG.getGlobal()::setYHotActive),
                YHOT_TOOLTIP
        );
        this.optionList.addToggle(
                this.isAnimatedAny(),
                this::toggleCursorAnimations,
                this.index(ANIMATIONS_TEXT),
                ANIMATIONS_INFO,
                this.hasAnimationAny()
        );
        this.optionList.addWidget(
                new ButtonWidget(
                        this.index(RESET_TEXT),
                        this::resetCursorSettings
                ).withTooltip(RESET_INFO)
        );

        this.optionList.search(this.getSearch());

        this.addRenderableWidget(this.previewWidget);
        this.addRenderableWidget(this.optionList);
    }

    @Override
    protected void repositionContents(int x, int y) {
        if (this.previewWidget != null && this.optionList != null) {
            this.previewWidget.setWidth(this.getWidth());
            this.previewWidget.setPosition(x, y);

            this.optionList.setSize(this.getWidth(), this.computeMaxHeight(y) - this.previewWidget.getHeight() - this.getSpacing());
            this.optionList.setPosition(x, this.previewWidget.getBottom() + this.getSpacing());
        }
    }

    private void onChangeScale(double scale) {
        CONFIG.getGlobal().setScale((float) scale);
        if (CONFIG.getGlobal().isScaleActive()) {
            setScale(currentCursor, (float) scale);
        }
    }

    private void onChangeXHot(double xhot) {
        CONFIG.getGlobal().setXHot(xhot);
        if (CONFIG.getGlobal().isXHotActive()) {
            setXHot(currentCursor, (int) xhot);
        }
    }

    private void onChangeYHot(double yhot) {
        CONFIG.getGlobal().setYHot(yhot);
        if (CONFIG.getGlobal().isYHotActive()) {
            setYHot(currentCursor, (int) yhot);
        }
    }

    private void onSliderMouseEvent(SliderWidget target, MouseEvent mouseEvent, double scale) {
        if (target.getPrefix().equals(SCALE_TEXT)) {
            this.scaling = (mouseEvent.clicked() || mouseEvent.dragged()) && currentCursor.isTextureEnabled();
        }

        if (mouseEvent.released()) {
            CursorRegistry registry = CursorsExtended.getInstance().getRegistry();
            if (target.getPrefix().equals(SCALE_TEXT) && CONFIG.getGlobal().isScaleActive()) {
                registry.getInternalCursors().forEach(cursor -> setScale(cursor, CONFIG.getGlobal().scale()));
            } else if (target.getPrefix().equals(XHOT_TEXT) && CONFIG.getGlobal().isXHotActive()) {
                registry.getInternalCursors().forEach(cursor -> setXHot(cursor, CONFIG.getGlobal().xhot()));
            } else if (target.getPrefix().equals(YHOT_TEXT) && CONFIG.getGlobal().isYHotActive()) {
                registry.getInternalCursors().forEach(cursor -> setYHot(cursor, CONFIG.getGlobal().yhot()));
            }
        }
    }

    private void cycleOnPreviewPress(Button target) {
        target.setFocused(false);
        this.cycleCurrentCursor();
    }

    private void cycleCurrentCursor() {
        if (!this.cursors.hasNext()) {
            this.cursors = cursorIterator();
        }
        this.currentCursor = this.cursors.hasNext() ? this.cursors.next() : getDefaultCursor();

        if (this.previewWidget != null) {
            this.previewWidget.setCursor(this.currentCursor);
        }
    }

    private void recycleCursors() {
        this.cursors = cursorIterator();
        this.cycleCurrentCursor();
    }

    private void toggleCursorAnimations(boolean animated) {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (cursor.getTexture() instanceof AnimatedCursorTexture animatedCursor) {
                animatedCursor.setAnimated(animated);
                CONFIG.getOrCreateSettings(cursor).setAnimated(animated);
            }
        }
    }

    private void resetCursorSettings() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getCursors()) {
            CursorTexture texture = cursor.getTexture();
            if (texture != null) {
                Config.CursorSettings settings = CONFIG.getOrCreateSettings(cursor);
                settings.mergeAll(texture.metadata().cursor());
                CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getGlobal().apply(settings));
            }
        }

        this.refreshCursors.run();
        this.refreshWidgets();
        this.repositionElements();
    }

    @Override
    protected void searched(@NotNull String search, @Nullable Component matched) {
        if (this.optionList != null) {
            this.optionList.search(search);
        }
    }

    public boolean hasAnimationAny() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (cursor.getTexture() instanceof AnimatedCursorTexture) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnimatedAny() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (cursor.getTexture() instanceof AnimatedCursorTexture animatedCursor && animatedCursor.animated()) {
                return true;
            }
        }
        return false;
    }


    private static Consumer<Boolean> applyGlobalOnToggle(Consumer<Boolean> onToggle) {
        return value -> {
            onToggle.accept(value);
            CursorsExtended.getInstance().getRegistry().getInternalCursors().forEach(cursor ->
                    CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getGlobal().apply(CONFIG.getOrCreateSettings(cursor)))
            );
        };
    }

    private static Tooltip createGlobalTooltip(Component message) {
        return Tooltip.create(Component.translatable("cursors_extended.options.global.tooltip", message));
    }

    private static Iterator<Cursor> cursorIterator() {
        return CursorsExtended.getInstance().getRegistry().getInternalCursors()
                .stream()
                .filter(Cursor::isTextureEnabled)
                .iterator();
    }

    private static @NotNull Cursor getDefaultCursor() {
        return CursorsExtended.getInstance().getRegistry().get(CursorType.DEFAULT);
    }

    private static Pair<Integer, Integer> getMaxHotspots() {
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
        return new Pair<>(currentMaxX != -1 ? currentMaxX : 0, currentMaxY != -1 ? currentMaxY : 0);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.scaling) {
            guiGraphics.requestCursor(CursorTypeUtil.arrowIfDefault(this.currentCursor.cursorType()));
        }
    }

    private static class GlobalPreviewWidget extends CursorPreviewWidget {
        private static final float CELL_DIVISOR = 32;
        private @NotNull Cursor cursor;

        public GlobalPreviewWidget(@NotNull Cursor cursor, @NotNull Font font, @Nullable Button button) {
            super(cursor, font, button);

            this.cursor = cursor;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(guiGraphics);
            this.renderPreviewText(guiGraphics);
            this.renderTestButton(guiGraphics, mouseX, mouseY, partialTick);
            this.renderRuler(guiGraphics, mouseX, mouseY);
            this.renderBorder(guiGraphics);

            if (this.isHovered()) guiGraphics.requestCursor(this.cursors_extended$cursorType(mouseX, mouseY));
        }

        @Override
        protected float getCellWidth() {
            return this.getWidth() / CELL_DIVISOR;
        }

        @Override
        protected float getCellHeight() {
            return this.getCellWidth();
        }

        @Override
        protected boolean isOverflowing() {
            return false;
        }

        public void setCursor(@NotNull Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public @NotNull Cursor getCursor() {
            return this.cursor;
        }
    }
}
