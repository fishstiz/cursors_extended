package io.github.fishstiz.cursors_extended.gui.components;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.util.KeywordSearcher;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexElement;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class OptionsListWidget extends FZAbstractListWidget<OptionsListWidget.Entry> {
    private static final int SPACING = 8;
    private static final int SCROLLBAR_PADDING = 4;
    private static final int MARGIN_Y = 2;
    private static final int ENTRY_BACKGROUND_PADDING = 1;
    private static final int SEARCH_HIGHLIGHT_COLOR = 0x66FFD700; // 40% yellow
    private final ElementSlidingBackground hoveredBackground = new ElementSlidingBackground(0x26FFFFFF); // 15% white
    private final KeywordSearcher<Entry> keywords = new KeywordSearcher<>();
    private List<Entry> matches = Collections.emptyList();
    private @Nullable Entry lastHovered;

    public <T extends LayoutElement> T addEntry(List<Component> keywords, T element, @Nullable Component tooltip) {
        Entry entry = new Entry(element, tooltip);
        addEntry(entry);

        List<String> keywordStrings = keywords.stream().map(Component::getString).filter(string -> !string.isEmpty()).toList();
        if (!keywordStrings.isEmpty()) {
            this.keywords.put(entry, keywordStrings);
        }

        return element;
    }

    public <T extends LayoutElement> T addEntry(Component keyword, T element, @Nullable Component tooltip) {
        return addEntry(List.of(keyword), element, tooltip);
    }

    public <T extends LayoutElement> T addEntry(Component keyword, T element) {
        return addEntry(keyword, element, null);
    }

    public <T extends LayoutElement> T addEntry(T element) {
        addEntry(new Entry(element));
        return element;
    }

    public RowBuilder rowBuilder() {
        return new RowBuilder();
    }

    @Override
    protected int maxContentWidth() {
        return 0;
    }

    @Override
    protected int rowSpacing() {
        return SPACING;
    }

    @Override
    protected void extractBackgroundRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Entry hovered = getHovered();

        if (hovered == null && !isHovered()) {
            lastHovered = null;
            hoveredBackground.reset();
        }

        if (!matches.isEmpty() || isHovered()) {
            int row = rowSpacing();

            graphics.enableScissor(
                    getX() - row,
                    getY() - ENTRY_BACKGROUND_PADDING,
                    getRight() + ENTRY_BACKGROUND_PADDING,
                    getBottom() + ENTRY_BACKGROUND_PADDING
            );

            for (Entry match : matches) {
                ScreenRectangle rect = match.getRectangle();
                graphics.fill(
                        rect.left() - row,
                        rect.top() - ENTRY_BACKGROUND_PADDING,
                        rect.right() + ENTRY_BACKGROUND_PADDING,
                        rect.bottom() + ENTRY_BACKGROUND_PADDING,
                        SEARCH_HIGHLIGHT_COLOR
                );
            }

            if (hovered != null || lastHovered != null) {
                Entry highlighted = Objects.requireNonNullElse(hovered, lastHovered);
                ScreenRectangle rect = highlighted.getRectangle();

                if (ScreenRectangleUtils.containsPoint(
                        rect.left(),
                        rect.top() - row,
                        rect.width(),
                        rect.height() + row * 2,
                        mouseX,
                        mouseY
                )) {
                    int left = rect.left() - rowSpacing();
                    int width = (rect.right() + ENTRY_BACKGROUND_PADDING) - left;

                    hoveredBackground.render(
                            graphics,
                            left,
                            rect.top() - ENTRY_BACKGROUND_PADDING,
                            width,
                            rect.height() + ENTRY_BACKGROUND_PADDING * 2,
                            partialTick
                    );
                }

                this.lastHovered = highlighted;
            }

            graphics.disableScissor();
        }
    }

    @Override
    protected void extractFocusedRenderState(GuiGraphicsExtractor graphics, Entry focused) {
    }

    public void onSearch(String query) {
        this.matches = keywords.query(query)
                .stream()
                .sorted()
                .map(KeywordSearcher.Result::source)
                .toList();

        if (!this.matches.isEmpty()) {
            scrollToEntry(this.matches.getFirst());
        }
    }

    @Override
    protected int scrollbarReserve() {
        return super.scrollbarReserve() + (scrollbarVisible() ? SCROLLBAR_PADDING : 0);
    }

    @Override
    protected int scrollBarX() {
        return super.scrollBarX() + (scrollbarVisible() ? SCROLLBAR_PADDING : 0);
    }

    @Override
    public void repositionEntries() {
        super.repositionEntries();
    }

    class Entry extends FZAbstractListWidget.Entry {
        private final FZFlexLayout layout = FZFlexLayout.horizontal().spacing(8);
        private final List<AbstractWidget> children = new ArrayList<>();
        private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

        Entry(LayoutElement element, @Nullable Component tooltip) {
            super(element.getHeight());
            fidgetz$setHovered(false);

            this.tooltip.set(tooltip == null ? null : Tooltip.create(tooltip));

            if (element instanceof AbstractWidget widget) {
                layout.child(widget, layout.flexChildSettings());
            } else if (element instanceof FZFlexElement flexElement) {
                layout.child(flexElement, layout.flexChildSettings());
            } else {
                layout.child(element, layout.flexChildSettings());
            }

            layout.setPosition(getX(), getY());
            layout.fidgetz$setSize(getWidth(), getHeight());
            layout.arrangeElements();

            element.visitWidgets(children::add);
        }

        Entry(LayoutElement element) {
            this(element, null);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            for (AbstractWidget child : children) {
                child.extractRenderState(graphics, mouseX, mouseY, a);
            }

            boolean hovered = isHovered() && graphics.containsPointInScissor(mouseX, mouseY);
            tooltip.refreshTooltipForNextRenderPass(graphics, mouseX, mouseY, hovered, isFocused(), getRectangle());
        }

        @Override
        protected int getMarginTop() {
            return getIndex() == 0 ? MARGIN_Y : super.getMarginTop();
        }

        @Override
        protected int getMarginBottom() {
            return getIndex() == OptionsListWidget.this.children().size() - 1 ? MARGIN_Y : super.getMarginBottom();
        }

        @Override
        public void setX(int x) {
            layout.setX(x);
            super.setX(x);
        }

        @Override
        public void setY(int y) {
            layout.setY(y);
            super.setY(y);
        }

        @Override
        public void setPosition(int x, int y) {
            layout.setPosition(x, y);
            super.setPosition(x, y);
        }

        @Override
        protected void setWidth(int width) {
            layout.fidgetz$setWidth(width);
            super.setWidth(width);
        }

        @Override
        protected void setHeight(int height) {
            layout.fidgetz$setHeight(height);
            super.setHeight(height);
        }

        @Override
        protected void setBounds(int x, int y, int width, int height) {
            layout.setPosition(x, y);
            layout.fidgetz$setSize(width, height);
            super.setBounds(x, y, width, height);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }
    }

    public final class RowBuilder {
        private static final int TOGGLE_BUTTON_WIDTH = 40;
        private final FZFlexLayout row = FZFlexLayout.horizontal().spacing(rowSpacing());
        private final List<AbstractWidget> labels = new ArrayList<>();
        private final List<Component> keywords = new ArrayList<>();
        private @Nullable Component tooltip;

        private RowBuilder() {
        }

        private static void toggleLabels(boolean toggled, List<AbstractWidget> labels) {
            for (AbstractWidget label : labels) {
                label.setMessage(label.getMessage().copy().withStyle(toggled ? ChatFormatting.WHITE : ChatFormatting.GRAY));
            }
        }

        public RowBuilder widget(AbstractWidget widget) {
            row.child(widget);
            return this;
        }

        public RowBuilder flexWidget(AbstractWidget widget) {
            row.child(widget, row.flexChildSettings());
            return this;
        }

        public RowBuilder keyword(Component keyword) {
            this.keywords.add(keyword);
            return this;
        }

        public RowBuilder tooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public RowBuilder label(Component label, @Nullable Component tooltipInfo) {
            FZText widget = FZText.builder(label).tooltip(tooltipInfo).build();
            labels.add(widget);
            return keyword(label).flexWidget(widget);
        }

        public RowBuilder label(Component label) {
            return label(label, null);
        }

        public RowBuilder toggleButton(
                BooleanSupplier getter,
                BooleanConsumer setter,
                @Nullable Component tooltipInfo
        ) {
            boolean initialValue = getter.getAsBoolean();
            List<AbstractWidget> labels = this.labels;
            toggleLabels(initialValue, labels);

            return widget(FZButton.builder()
                    .width(TOGGLE_BUTTON_WIDTH)
                    .message(CommonComponents.optionStatus(getter.getAsBoolean()))
                    .tooltip(tooltipInfo)
                    .onPress(e -> {
                        setter.accept(!getter.getAsBoolean());
                        boolean newValue = getter.getAsBoolean();
                        e.target().setMessage(CommonComponents.optionStatus(newValue));
                        toggleLabels(newValue, labels);
                    })
                    .build());
        }

        public RowBuilder toggleButton(BooleanSupplier getter, BooleanConsumer setter) {
            return toggleButton(getter, setter, null);
        }

        public Toggle toggleBuilder() {
            return new Toggle();
        }

        public void buildRow() {
            row.arrangeElements();
            addEntry(keywords, row, tooltip);
        }

        public final class Toggle {
            private static final WidgetElements UNDO_SPRITES;
            private final FZMutableRef<State> state = new FZMutableRef<>(new State(false, true));
            private final String key = "RowBuilder.Toggle@%s#%s".formatted(hashCode(), RowBuilder.this.hashCode());
            private Consumer<UnaryOperator<Boolean>> setter;
            private @Nullable Component tooltipInfo = null;
            private @Nullable Boolean defaultValue = null;

            private Toggle() {
                List<AbstractWidget> labels = RowBuilder.this.labels;
                state.subscribe(key + "-self", value -> toggleLabels(value.value && value.active, labels));
            }

            static {
                Identifier undoSprite = CursorsExtended.id("icon/arrow_u_turn_up_left");
                UNDO_SPRITES = WidgetElements.noFocus(
                        Renderables.sprite(undoSprite),
                        Renderables.sprite(undoSprite, CommonColors.GRAY),
                        16,
                        16
                );
            }

            public Toggle defaultValue(boolean defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }

            public Toggle state(FZMutableRef<Boolean> state) {
                this.state.set(prev -> new State(state.value(), prev.active));
                state.subscribe(key, value -> this.state.set(prev -> new State(value, prev.active)));
                this.setter = state::set;
                return this;
            }

            public Toggle state(Consumer<Boolean> setter, Supplier<Boolean> getter) {
                return state(FZMutableRef.wrap(setter, getter));
            }

            public Toggle active(FZRef<Boolean> activeState) {
                this.state.set(prev -> new State(prev.value, activeState.value()));
                activeState.subscribe(key, value -> this.state.set(prev -> new State(prev.value, value)));
                return this;
            }

            public Toggle active(boolean active) {
                this.state.set(prev -> new State(prev.value, active));
                return this;
            }

            public Toggle tooltip(Component tooltipInfo) {
                this.tooltipInfo = tooltipInfo;
                return this;
            }

            public RowBuilder buildToggle() {
                Consumer<UnaryOperator<Boolean>> setter = this.setter;
                Component tooltipInfo = this.tooltipInfo;

                row.child(FZButton.bind(key + "-toggle", state.map(value -> FZButton.builder()
                        .width(TOGGLE_BUTTON_WIDTH)
                        .message(CommonComponents.optionStatus(value.value))
                        .tooltip(tooltipInfo)
                        .active(value.active)
                        .onPress(() -> setter.accept(prev -> !prev))
                        .toProps())));

                if (defaultValue != null) {
                    boolean defaultValue = this.defaultValue;

                    row.child(FZIconButton.bind(key + "-reset", state.map(value -> FZIconButton.builder()
                            .square()
                            .icon(UNDO_SPRITES)
                            .active(value.active && value.value != defaultValue)
                            .onPress(() -> setter.accept(_ -> defaultValue))
                            .toProps())));
                }

                toggleLabels(state.value().value, labels);

                return RowBuilder.this;
            }

            public void build() {
                buildToggle().buildRow();
            }

            private record State(boolean value, boolean active) {
            }
        }
    }
}
