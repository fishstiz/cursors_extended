package io.github.fishstiz.cursors_extended.gui.widget;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class OptionsListWidget extends AbstractListWidget<OptionsListWidget.AbstractEntry> {
    private static final int BACKGROUND_PADDING_Y = 2;
    private static final int SEARCH_HIGHLIGHT_COLOR = 0x66FFD700; // 40% yellow
    private final ElementSlidingBackground hoveredBackground = new ElementSlidingBackground(0x26FFFFFF); // 15% white
    private final Font font;
    private @NonNull String search = "";

    public OptionsListWidget(Minecraft minecraft, Font font, int itemHeight, int spacing) {
        super(minecraft, 0, 0, 0, itemHeight, spacing);

        this.font = font;
    }

    public OptionsListWidget(Minecraft minecraft, Font font, int spacing) {
        this(minecraft, font, Button.DEFAULT_HEIGHT, spacing);
    }

    public void addWidget(AbstractWidget widget) {
        this.addEntry(new WidgetEntry(widget));
    }

    public void addToggle(
            boolean value,
            Boolean defaultValue,
            @NonNull Consumer<Boolean> onToggle,
            @NonNull Component label,
            @Nullable Tooltip tooltip,
            BooleanSupplier active
    ) {
        this.addEntry(new ToggleEntry(value, defaultValue, onToggle, label, null, tooltip, active));
    }

    public void addToggle(
            boolean value,
            Boolean defaultValue,
            @NonNull Consumer<Boolean> onToggle,
            @NonNull Component label,
            @Nullable Tooltip tooltip,
            boolean active
    ) {
        this.addEntry(new ToggleEntry(value, defaultValue, onToggle, label, null, tooltip, () -> active));
    }

    public void addToggle(
            boolean value,
            @NonNull Consumer<Boolean> onToggle,
            @NonNull Component label,
            @Nullable Tooltip tooltip,
            boolean active
    ) {
        this.addToggle(value, null, onToggle, label, tooltip, active);
    }

    public void addToggle(
            boolean value,
            @NonNull Consumer<Boolean> onToggle,
            @NonNull Component label,
            @Nullable Prefix prefix,
            @Nullable Tooltip tooltip,
            boolean active
    ) {
        this.addEntry(new ToggleEntry(value, onToggle, label, prefix, tooltip, active));
    }

    public void addToggleableSlider(
            @NonNull SliderWidget slider,
            boolean value,
            @NonNull Consumer<Boolean> onToggle,
            @Nullable Tooltip tooltip
    ) {
        this.addEntry(new ToggleableSliderEntry(slider, value, onToggle, tooltip, true));
    }

    public void search(@NonNull String search) {
        this.search = search.toLowerCase();

        if (!this.search.isEmpty()) {
            AbstractEntry bestMatch = null;
            for (AbstractEntry entry : this.children()) {
                String label = entry.indexedLabel;
                if (label.startsWith(this.search)) {
                    bestMatch = entry;
                    break;
                }
                if (bestMatch == null && label.contains(this.search)) {
                    bestMatch = entry;
                }
            }
            if (bestMatch != null) {
                this.scrollToEntry(bestMatch);
            }
        }
    }

    private int computeBackgroundX(AbstractEntry entry) {
        return entry.getX() - this.rowGap;
    }

    private int computeBackgroundY(AbstractEntry entry) {
        return entry.getY() - BACKGROUND_PADDING_Y;
    }

    private int computeBackgroundWidth(AbstractEntry entry) {
        return entry.getWidth() + this.rowGap + (this.scrollbarVisible() ? SCROLLBAR_WIDTH : BACKGROUND_PADDING_Y);
    }

    private int computeBackgroundHeight(AbstractEntry entry) {
        return entry.getHeight() + BACKGROUND_PADDING_Y * 2;
    }

    protected void renderSearchBackground(@NonNull GuiGraphics guiGraphics) {
        if (!this.search.isEmpty()) {
            for (AbstractEntry entry : this.children()) {
                if (entry.indexedLabel.contains(this.search)) {
                    int bgX = this.computeBackgroundX(entry);
                    int bgY = this.computeBackgroundY(entry);
                    int bgWidth = this.computeBackgroundWidth(entry);
                    int bgHeight = this.computeBackgroundHeight(entry);

                    guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, SEARCH_HIGHLIGHT_COLOR);
                }
            }
        }
    }

    protected void renderEntryBackground(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int minX = this.getX() - this.rowGap;
        int minY = this.getY() - BACKGROUND_PADDING_Y;
        int maxX = this.getRight() + SCROLLBAR_WIDTH;
        int maxY = this.getBottom() + BACKGROUND_PADDING_Y;
        guiGraphics.enableScissor(minX, minY, maxX, maxY);

        this.renderSearchBackground(guiGraphics);

        AbstractEntry entry = this.getEntryAtPosition(mouseX, mouseY);
        if (entry == null) {
            this.hoveredBackground.reset();
        } else {
            int x = this.computeBackgroundX(entry);
            int y = this.computeBackgroundY(entry);
            int width = this.computeBackgroundWidth(entry);
            int height = this.computeBackgroundHeight(entry);
            this.hoveredBackground.render(guiGraphics, x, y, width, height, partialTick);
        }

        guiGraphics.disableScissor();
    }

    @Override
    public void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderEntryBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderListBackground(@NonNull GuiGraphics guiGraphics) {
        // remove background
    }

    @Override
    protected void renderListSeparators(@NonNull GuiGraphics guiGraphics) {
        // remove separators
    }

    protected abstract class AbstractEntry extends AbstractListWidget<AbstractEntry>.Entry {
        protected final Component label;
        private final String indexedLabel;
        private final List<AbstractWidget> children = new ArrayList<>();

        protected AbstractEntry(Component label) {
            this.label = label;
            this.indexedLabel = label.getString().toLowerCase();
        }

        @Override
        public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            for (AbstractWidget child : this.children) {
                child.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        protected <T extends AbstractWidget> T addChild(T child) {
            this.children.add(child);
            return child;
        }

        @Override
        public @NonNull List<AbstractWidget> children() {
            return this.children;
        }

        @Override
        public @NonNull List<AbstractWidget> narratables() {
            return this.children;
        }
    }

    private class WidgetEntry extends AbstractEntry {
        private final AbstractWidget widget;

        private WidgetEntry(@NonNull AbstractWidget widget) {
            super(widget.getMessage());

            this.widget = this.addChild(widget);
        }

        @Override
        public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            this.widget.setSize(width, this.getHeight());
            this.widget.setPosition(this.getX(), this.getY());
            super.renderContent(guiGraphics, mouseX, mouseY, hovered, partialTick);
        }
    }

    private class ToggleEntry extends AbstractEntry {
        protected static final Identifier UNDO_ICON = CursorsExtended.id("textures/gui/sprites/icon/arrow_u_turn_up_left.png");
        protected static final int BUTTON_WIDTH = 40;
        private static final int LABEL_COLOR = 0xFFFFFFFF; // white
        private static final int DISABLED_COLOR = 0xFFAAAAAA; // gray
        private final ButtonWidget button;
        private final ButtonWidget resetButton;
        private final Boolean defaultValue;
        private final Consumer<Boolean> onToggle;
        private final @Nullable Prefix prefix;
        private final BooleanSupplier active;
        protected boolean value;

        private ToggleEntry(
                boolean value,
                Boolean defaultValue,
                @NonNull Consumer<Boolean> onToggle,
                @NonNull Component label,
                @Nullable Prefix prefix,
                @Nullable Tooltip tooltip,
                BooleanSupplier active
        ) {
            super(label);

            this.value = value;
            this.onToggle = onToggle;
            this.active = active;
            this.button = new ButtonWidget(
                    this.getRight() - BUTTON_WIDTH,
                    this.getY(),
                    BUTTON_WIDTH,
                    Button.DEFAULT_HEIGHT,
                    value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
                    this::onPress
            );
            this.button.setTooltip(tooltip);
            this.button.active = active.getAsBoolean();

            this.prefix = prefix;
            this.defaultValue = defaultValue;

            if (this.defaultValue != null) {
                this.resetButton = new ButtonWidget(Component.empty(), btn -> {
                    this.value = this.defaultValue;
                    this.updateMessage();
                    this.onToggle.accept(this.defaultValue);
                }).withSize(Button.DEFAULT_HEIGHT).spriteOnly(UNDO_ICON);
                this.resetButton.active = this.button.active && this.value != this.defaultValue;
                this.addChild(this.resetButton);
            } else {
                this.resetButton = null;
            }

            this.addChild(this.button);
        }

        private ToggleEntry(
                boolean value,
                @NonNull Consumer<Boolean> onToggle,
                @NonNull Component label,
                @Nullable Prefix prefix,
                @Nullable Tooltip tooltip,
                boolean active
        ) {
            this(value, null, onToggle, label, prefix, tooltip, () -> active);
        }

        protected void updateMessage() {
            this.button.setMessage(this.value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
        }

        protected void onPress(Button button) {
            this.value = !this.value;
            this.updateMessage();
            this.onToggle.accept(this.value);
        }

        protected void renderLabel(@NonNull GuiGraphics guiGraphics) {
            int marginX = 0;

            if (this.prefix != null) {
                marginX = this.prefix.render(guiGraphics, OptionsListWidget.this.font, this.getX(), this.getY(), this.getHeight());
                if (marginX > 0) marginX += OptionsListWidget.this.rowGap;
            }

            int startX = this.getX() + marginX;
            int startY = this.getY();
            int endX = this.button.getX() - OptionsListWidget.this.rowGap;
            int endY = this.getBottom();
            int color = this.active.getAsBoolean() && this.value ? LABEL_COLOR : DISABLED_COLOR;
            DrawUtil.drawScrollableTextLeftAlign(guiGraphics, OptionsListWidget.this.font, this.label, startX, startY, endX, endY, color);
        }

        @Override
        public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            this.button.active = this.active.getAsBoolean();
            int right = this.getRight();

            if (this.resetButton != null) {
                this.resetButton.active = this.button.active && this.value != this.defaultValue;
                this.resetButton.setPosition(right - this.resetButton.getWidth(), this.getY());
                right -= this.resetButton.getWidth() + OptionsListWidget.this.rowGap;
            }

            this.button.setPosition(right - this.button.getWidth(), this.getY());
            this.renderLabel(guiGraphics);
            super.renderContent(guiGraphics, mouseX, mouseY, hovered, partialTick);
        }
    }

    private class ToggleableSliderEntry extends ToggleEntry {
        private final SliderWidget slider;

        private ToggleableSliderEntry(
                @NonNull SliderWidget slider,
                boolean value,
                @NonNull Consumer<Boolean> onToggle,
                @Nullable Tooltip tooltip,
                boolean active
        ) {
            super(value, onToggle, slider.getMessage(), null, tooltip, active);

            this.slider = slider;
            this.slider.active = value;
            this.addChild(this.slider);
        }

        @Override
        protected void onPress(Button button) {
            super.onPress(button);
            this.slider.active = this.value;
        }

        @Override
        protected void renderLabel(@NonNull GuiGraphics guiGraphics) {
            // remove label
        }

        @Override
        public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            this.slider.setWidth(width - BUTTON_WIDTH - OptionsListWidget.this.rowGap);
            this.slider.setPosition(this.getX(), this.getY());
            this.slider.render(guiGraphics, mouseX, mouseY, partialTick);
            super.renderContent(guiGraphics, mouseX, mouseY, hovered, partialTick);
        }
    }

    @FunctionalInterface
    public interface Prefix {
        /**
         * @return width of prefix
         */
        int render(@NonNull GuiGraphics guiGraphics, Font font, int x, int y, int height);
    }
}