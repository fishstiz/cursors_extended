package io.github.fishstiz.cursors_extended.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.components.CursorEntryWidget;
import io.github.fishstiz.cursors_extended.gui.panels.*;
import io.github.fishstiz.cursors_extended.gui.panels.AbstractContentPanel;
import io.github.fishstiz.cursors_extended.gui.components.CategoryListWidget;
import io.github.fishstiz.cursors_extended.util.KeywordSearcher;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.layouts.*;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class ConfigScreen extends FZScreen {
    private static final Component CURSORS_TEXT = Component.translatable("cursors_extended.options.cursor-types");
    private static final Component SEARCH_TEXT = Component.translatable("cursors_extended.options.search").withStyle(EditBox.SEARCH_HINT_STYLE);
    private static final Component CLEAR_SEARCH_INFO = Component.translatable("cursors_extended.options.search.clear");
    private static final Identifier CLEAR_SPRITE = CursorsExtended.id("icon/cross");
    private static final int SIDE_PANEL_WIDTH = 150;
    private static final int SPACING = 8;
    private final Screen parent;
    private final Map<String, AbstractContentPanel> contentPanels = new LinkedHashMap<>();
    private final FZMutableRef<AbstractContentPanel> contentRef = new FZMutableRef<>(AbstractContentPanel.empty());
    private final KeywordSearcher<String> keywords = new KeywordSearcher<>();
    private final FZMutableRef<String> searchRef = new FZMutableRef<>("");
    private final CategoryListWidget categoryList = new CategoryListWidget(this::setContent);
    private final FZMutableRef<Boolean> loadingRef = new FZMutableRef<>(false);
    private final FZMutableRef<Cursor> previewCursor = new FZMutableRef<>(CursorsExtended.getInstance()
            .getRegistry()
            .get(CursorType.DEFAULT));
    private FZTextField searchField;
    private FZLayout rootLayout;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("cursors_extended.options"));
        this.parent = parent;
        searchRef.subscribe("Search", this::onSearch);
    }

    @Override
    public void added() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
            if (cursor.isLazy()) {
                CursorsExtended.getInstance().getLoader().loadTexture(cursor);
            }
        }
    }

    private void registerCategory(AbstractContentPanel panel) {
        categoryList.addEntry(panel.getId(), panel.getShorthandTitle());
        contentPanels.put(panel.getId(), panel);
        keywords.put(panel.getId(), panel.buildKeywords());
    }

    private void registerParentCategory(
            String id,
            Component message,
            BiConsumer<CategoryListWidget.ParentConfig, Consumer<AbstractContentPanel>> configurator
    ) {
        List<String> parentKeywords = new ArrayList<>();
        String parentKeyword = message.getString();
        CategoryListWidget.ParentConfig parent = categoryList.addParent(id, message.copy().withStyle(ChatFormatting.BOLD));
        configurator.accept(parent, panel -> {
            contentPanels.put(panel.getId(), panel);
            Set<String> childKeywords = new LinkedHashSet<>(panel.buildKeywords());
            parentKeywords.addAll(childKeywords);
            childKeywords.add(parentKeyword);
            keywords.put(panel.getId(), childKeywords);
        });
        parentKeywords.add(parentKeyword);
        keywords.put(id, parentKeywords);
    }

    private void setContent(String id) {
        AbstractContentPanel content = contentPanels.get(id);

        contentRef.set(prev -> {
            if (prev != content) {
                if (prev != null) {
                    prev.onRemove();
                }
                if (content != null) {
                    content.buildWidgets();
                    content.onSearch(searchRef.value());
                }
                return content;
            }
            return prev;
        });

        categoryList.onSelect(id);
    }

    @Override
    protected void init() {
        registerCategory(new GlobalOptionsPanel(minecraft, this, previewCursor, previewCursor::set, this::setContent));

        AdaptiveOptionsPanel adaptiveOptions = new AdaptiveOptionsPanel(minecraft, this);

        MutableObject<String> initialPanelId = new MutableObject<>(adaptiveOptions.getId());

        registerCategory(adaptiveOptions);

        registerParentCategory("Cursors", CURSORS_TEXT, (parent, panelCollector) -> {
            boolean firstFound = false;
            for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getInternalCursors()) {
                AbstractContentPanel panel = new CursorOptionsPanel(minecraft, this, cursor, () -> {
                    previewCursor.set(cursor);
                    setContent("Global");
                });

                parent.collapse(!CONFIG.hasResourcePack());
                parent.addEntry(panel.getId(), new CursorEntryWidget(cursor, () -> setContent(panel.getId())));
                panelCollector.accept(panel);

                if (!firstFound && cursor.isTextureEnabled()) {
                    previewCursor.set(cursor);
                    initialPanelId.setValue(panel.getId());
                    firstFound = true;
                }
            }
        });

        registerCategory(new CompatibilityOptionsPanel(minecraft, this));

        registerCategory(new DebugOptionsPanel(minecraft, this));

        categoryList.repositionEntries();

        super.init();

        if (initialPanelId.get() != null) {
            setContent(initialPanelId.get());
        }

        if (this.searchField != null) {
            setInitialFocus(this.searchField);
        }
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        FZFlexLayout root = FZFlexLayout.vertical(this).spacing(SPACING);

        FZFlexLayout header = root.child(FZFlexLayout.horizontal(), root.flexChildHorizontalSettings()).spacing(SPACING);
        {
            FZFlexLayout searchBar = header.child(
                    FZFlexLayout.horizontal().spacing(SPACING),
                    header.flexChildHorizontalSettings().maxFlexWidth(SIDE_PANEL_WIDTH)
            );
            {
                this.searchField = searchBar.child(
                        FZTextField.bind("SearchField", searchRef.map(value -> FZTextField.builder()
                                .text(value)
                                .hint(SEARCH_TEXT)
                                .onChange(e -> searchRef.set(e.value()))
                                .toProps())),
                        searchBar.flexChildHorizontalSettings()
                );
                setFocused(this.searchField);

                searchBar.child(FZIconButton.bind("ClearSearchButton", searchRef.map(value -> FZIconButton.builder()
                        .square()
                        .active(!value.isEmpty())
                        .tooltip(CLEAR_SEARCH_INFO)
                        .icon(WidgetElements.noFocus(
                                Renderables.sprite(CLEAR_SPRITE),
                                Renderables.sprite(CLEAR_SPRITE, 0x80A0A0A0),
                                16,
                                16
                        ))
                        .onPress(() -> {
                            searchRef.set("");
                            setFocused(searchField);
                        })
                        .toProps())));
            }

            header.child(
                    FZText.bind("ContentHeader", contentRef.map(value -> FZText
                            .builder(value == null ? CommonComponents.EMPTY : value.getTitle().copy().withStyle(ChatFormatting.BOLD))
                            .toProps())),
                    header.flexChildHorizontalSettings().alignVerticallyMiddle()
            );

            header.child(FZIconButton.bind("RefreshButton", loadingRef.map(value -> FZIconButton.builder()
                    .icon(new WidgetElements(CursorsExtended.id("icon/arrow_clockwise"), 16, 16))
                    .tooltip(Component.translatable("cursors_extended.options.refresh.info"))
                    .square()
                    .onPress(this::refreshCursors)
                    .active(!value)
                    .toProps())));
        }

        FZFlexLayout body = root.child(FZFlexLayout.horizontal(), root.flexChildSettings()).spacing(SPACING);
        {
            body.child(categoryList, body.flexChildSettings().maxFlexWidth(SIDE_PANEL_WIDTH));

            FZFlexLayout contentPanel = body.child(FZFlexLayout.vertical(), body.flexChildSettings()).spacing(SPACING);
            {
                contentPanel.child(
                        WrappedComponent.bind("ContentPanel", contentRef.<AbstractWidget>map(value -> value == null
                                ? AbstractContentPanel.empty()
                                : value
                        )),
                        contentPanel.flexChildSettings()
                );

                FZFlexLayout footer = contentPanel.child(FZFlexLayout.horizontal(), contentPanel.flexChildHorizontalSettings());
                {
                    footer.spacing(SPACING).defaultChildSettings().alignVerticallyMiddle();

                    FZFlexSpacerElement spacer = footer.spacer(footer.flexChildSettings());

                    footer.child(FZButton.builder()
                            .width(128)
                            .message(CommonComponents.GUI_DONE)
                            .onPress(this::onClose)
                            .build());

                    Component footerTitle = getTitle().copy().withStyle(ChatFormatting.GRAY);
                    Component restartRequired = Component.translatable("options.restartRequired").withColor(CommonColors.SOFT_RED);

                    collector.renderableOnly((graphics, _, _, _) -> GuiGraphicsUtils.scrollingText(
                            graphics,
                            getFont(),
                            CONFIG.isRestartRequiredToApply() ? restartRequired : footerTitle,
                            spacer.getX(),
                            spacer.getY(),
                            spacer.getX() + spacer.getWidth(),
                            spacer.getY() + spacer.getHeight(),
                            CommonColors.WHITE
                    ));
                }
            }
        }

        root.visitWidgets(collector::renderableWidget);
        this.rootLayout = FZComposedLayout.contain(this, root).padding(SPACING).center().clamp().arrange().get();
    }

    @Override
    protected void repositionElements() {
        if (this.rootLayout != null) {
            this.rootLayout.fidgetz$setSize(width, height);
            this.rootLayout.arrangeElements();
        } else {
            super.repositionElements();
        }
    }

    @Override
    public void onClose() {
        CONFIG.save();
        minecraft.gui.setScreen(this.parent);
    }

    private void onSearch(String input) {
        LinkedHashSet<String> results = keywords.query(input)
                .stream()
                .sorted()
                .map(KeywordSearcher.Result::source)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!results.isEmpty()) {
            setContent(results.getFirst());
        }

        AbstractContentPanel content = contentRef.value();
        String selected = content == null ? null : content.getId();
        categoryList.onSearch(input, selected, results);

        if (content != null) {
            content.onSearch(input);
        }
    }

    private void refreshCursors() {
        if (loadingRef.value()) return;

        loadingRef.set(true);

        CompletableFuture
                .runAsync(CursorsExtended.getInstance().getLoader()::reload, Util.backgroundExecutor())
                .whenCompleteAsync(
                        (_, error) -> {
                            loadingRef.set(false);

                            if (error != null) {
                                CursorsExtended.LOGGER.error("[cursors_extended] An error occurred while refreshing cursors. ", error);
                            }

                            AbstractContentPanel content = contentRef.value();
                            if (content != null) {
                                content.buildWidgets();
                            }
                        },
                        minecraft
                );
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        if (searchField != null
            && getFocused() != searchField
            && (event.modifiers() & InputConstants.MOD_CONTROL) != 0
            && (event.key() == InputConstants.KEY_K || event.key() == InputConstants.KEY_F)) {
            setFocused(searchField);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        ComponentPath focusPath = getCurrentFocusPath();
        if (focusPath != null) {
            focusPath.applyFocus(false);
        }

        return false;
    }
}
