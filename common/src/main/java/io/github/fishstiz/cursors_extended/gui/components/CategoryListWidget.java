package io.github.fishstiz.cursors_extended.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.FZAbstractListWidget;
import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.WidgetElements;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.gui.text.TextComponentUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CategoryListWidget extends FZAbstractListWidget<CategoryListWidget.Entry> {
    private static final int ITEM_HEIGHT = 24;
    private static final double SCROLL_RATE = ITEM_HEIGHT / 2d;
    private static final WidgetElements DEFAULT_ICON = createTextIcon(TextComponentUtils.BLACK_SQUARE);
    private static final WidgetElements COLLAPSED = createTextIcon(TextComponentUtils.BLACK_RIGHT_POINTING_TRIANGLE);
    private static final WidgetElements EXPANDED = createTextIcon(TextComponentUtils.BLACK_DOWN_POINTING_TRIANGLE);
    private final ElementSlidingBackground hoveredBackground = new ElementSlidingBackground(0x26FFFFFF); // 15% white
    private final ElementSlidingBackground selectedBackground = new ElementSlidingBackground(0x33FFFFFF); // 20% white
    private final ElementSlidingBackground focusedBackground = new ElementSlidingBackground(0xFFFFFFFF, true); // white
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private final Consumer<String> selector;
    private @Nullable Entry selected;

    public CategoryListWidget(Consumer<String> selector) {
        this.selector = selector;
    }

    @Override
    protected int maxContentWidth() {
        return 0;
    }

    public void addEntry(String id, AbstractWidget widget) {
        Entry entry = Entry.simple(id, widget);
        entries.put(id, entry);
        addEntry(entry);
    }

    public void addEntry(String id, Component message, WidgetElements icon) {
        Entry entry = Entry.simple(id, FZButton.builder()
                .height(ITEM_HEIGHT)
                .sprites(null)
                .leftIcon(icon)
                .message(message)
                .leftAlignedMessage()
                .onPress(() -> selector.accept(id))
                .build());

        entries.put(id, entry);
        addEntry(entry);
    }

    public void addEntry(String id, Component message) {
        addEntry(id, message, DEFAULT_ICON);
    }

    public ParentConfig addParent(String id, Component message) {
        FZMutableRef<Boolean> collapsedRef = new FZMutableRef<>(false);

        Entry entry = Entry.simple(id, FZButton.bind(id, collapsedRef.map(value -> FZButton.builder()
                .height(ITEM_HEIGHT)
                .sprites(null)
                .leftIcon(value ? COLLAPSED : EXPANDED)
                .message(message)
                .leftAlignedMessage()
                .onPress(() -> collapsedRef.set(prev -> !prev))
                .toProps())));

        entries.put(id, entry);
        addEntry(entry);

        collapsedRef.subscribe("CategoryList", this::onToggle);

        return new ParentConfig(entry, collapsedRef);
    }

    private void onToggle() {
        ComponentPath focusPath = getCurrentFocusPath();
        Entry focused = getFocused();
        Entry hovered = getHovered();

        clearEntries();

        for (Entry entry : entries.values()) {
            if (entry.hidden == null || !entry.hidden.value()) {
                entry.visible = true;
                addEntry(entry);
                if (focused == entry) {
                    setFocused(entry);
                    if (focusPath != null) {
                        focusPath.applyFocus(true);
                    }
                }
                if (hovered == entry) {
                    setHovered(entry);
                }
            } else {
                entry.visible = false;
            }
        }

        repositionEntries();
    }

    public void onSelect(@Nullable String selected) {
        if (selected == null || !entries.containsKey(selected)) {
            this.selected = null;
            return;
        }

        Entry entry = entries.get(selected);
        if (entry != null) {
            this.selected = entry;
            setFocused(entry);
            if (entry.hidden != null) {
                entry.hidden.set(false);
                entry.visible = true;
            }
            scrollToEntry(entry);
        }
    }

    public void onSearch(String query, @Nullable String selectedId, Set<String> resultIds) {
        Entry focused = getFocused();
        Entry hovered = getHovered();

        if (!query.isEmpty()) {
            for (String id : resultIds) {
                Entry entry = entries.get(id);
                if (entry != null && entry.hidden != null) {
                    entry.hidden.set(false);
                }
            }
        }

        clearEntries();

        Predicate<Entry> canAdd = query.isEmpty()
                ? entry -> entry.hidden == null || !entry.hidden.value()
                : entry -> resultIds.contains(entry.id);

        for (Entry entry : entries.values()) {
            if (canAdd.test(entry)) {
                entry.visible = true;
                addEntry(entry);
                if (Objects.equals(entry.id, selectedId)) {
                    this.selected = entry;
                }
                if (focused == entry) {
                    setFocused(entry);
                }
                if (hovered == entry) {
                    setHovered(entry);
                }
            } else {
                entry.visible = false;
            }
        }

        repositionEntries();

        if (this.selected != null) {
            scrollToEntry(selected);
        }
    }

    @Override
    public double scrollRate() {
        return SCROLL_RATE;
    }

    @Override
    public void repositionEntries() {
        super.repositionEntries();
    }

    @Override
    protected void extractEntriesRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredBackground.render(graphics, getHovered(), partialTick);
        this.selectedBackground.render(graphics, selected != null && selected.visible ? selected : null, partialTick);
        this.focusedBackground.render(graphics, getFocused(), partialTick);

        super.extractEntriesRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractFocusedRenderState(GuiGraphicsExtractor graphics, Entry focused) {
    }

    protected static WidgetElements createTextIcon(Component component) {
        return new WidgetElements(Renderables.text(component), 9, 9).marginBottom(1);
    }

    public class ParentConfig {
        private final Entry parentEntry;
        private final FZMutableRef<Boolean> collapsedRef;

        private ParentConfig(Entry parentEntry, FZMutableRef<Boolean> collapsedRef) {
            this.parentEntry = parentEntry;
            this.collapsedRef = collapsedRef;
        }

        public String getId() {
            return parentEntry.id;
        }

        public void collapse(boolean collapsed) {
            collapsedRef.set(collapsed);
        }

        public void collapse() {
            collapse(true);
        }

        public void addEntry(String id, AbstractWidget widget) {
            widget.setHeight(ITEM_HEIGHT);

            Entry entry = Entry.child(id, collapsedRef, widget);
            entries.put(id, entry);
            if (!collapsedRef.value()) {
                CategoryListWidget.this.addEntry(entry);
            }
        }

        public void addEntry(String id, Component message, WidgetElements icon) {
            addEntry(id, FZButton.builder()
                    .height(ITEM_HEIGHT)
                    .sprites(null)
                    .leftIcon(icon)
                    .message(message)
                    .leftAlignedMessage()
                    .onPress(() -> selector.accept(id))
                    .build());
        }

        public void addEntry(String id, Component message) {
            addEntry(id, message, DEFAULT_ICON);
        }
    }

    static class Entry extends FZAbstractListWidget.Entry {
        private final String id;
        private final List<GuiEventListener> children;
        private final AbstractWidget widget;
        private final @Nullable FZMutableRef<Boolean> hidden;
        private boolean visible = true;

        Entry(String id, AbstractWidget widget, @Nullable FZMutableRef<Boolean> hidden) {
            super(widget.getHeight());
            fidgetz$setHovered(false);
            this.id = id;
            this.children = List.of(widget);
            this.widget = widget;
            this.hidden = hidden;
        }

        static Entry simple(String id, AbstractWidget widget) {
            return new Entry(id, widget, null);
        }

        static Entry child(String id, FZMutableRef<Boolean> hidden, AbstractWidget widget) {
            return new Entry(id, widget, Objects.requireNonNull(hidden, "hidden cannot be null"));
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            widget.extractRenderState(graphics, mouseX, mouseY, a);
        }

        @Override
        public void setX(int x) {
            widget.setX(x);
            super.setX(x);
        }

        @Override
        public void setY(int y) {
            widget.setY(y);
            super.setY(y);
        }

        @Override
        public void setPosition(int x, int y) {
            widget.setPosition(x, y);
            super.setPosition(x, y);
        }

        @Override
        protected void setWidth(int width) {
            widget.setWidth(width);
            super.setWidth(width);
        }

        @Override
        protected void setHeight(int height) {
            widget.setHeight(height);
            super.setHeight(height);
        }

        @Override
        protected void setBounds(int x, int y, int width, int height) {
            widget.setRectangle(width, height, x, y);
            super.setBounds(x, y, width, height);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }
    }
}
