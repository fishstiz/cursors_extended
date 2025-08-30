package io.github.fishstiz.cursors_extended.gui.screen;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.gui.CursorAnimationHelper;
import io.github.fishstiz.cursors_extended.gui.screen.panel.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static io.github.fishstiz.cursors_extended.resource.CursorResourceLoader.reload;

public class ConfigurationScreen extends CatalogBrowserScreen {
    private static final Component GLOBAL_TEXT = Component.translatable("cursors_extended.options.global");
    private static final Component ADAPTIVE_TEXT = Component.translatable("cursors_extended.options.adapt");
    private static final Component COMPAT_TEXT = Component.translatable("cursors_extended.options.compat");
    private static final Component CURSORS_TEXT = Component.translatable("cursors_extended.options.cursor-types");
    private static final Component DEBUG_TEXT = Component.translatable("cursors_extended.options.debug");
    private static final int SPACING = 8;
    private static final int HEADER_HEIGHT = 20;
    private static final int SIDEBAR_WIDTH = 140;
    private static final int MAX_CONTENT_WIDTH = 128 * 2 + SPACING;
    private static final int LIST_CURSOR_SIZE = 16;
    private static final CatalogItem GLOBAL_CATEGORY = new CatalogItem("global", GLOBAL_TEXT);
    private static final CatalogItem CURSORS_CATEGORY = new CatalogItem("cursors", CURSORS_TEXT);
    private final CursorAnimationHelper animationHelper = new CursorAnimationHelper();
    private CatalogItem defaultItem;
    private CompletableFuture<Void> refreshFuture;

    public ConfigurationScreen(Screen previous) {
        super(Component.translatable("cursors_extended.options"), HEADER_HEIGHT, SIDEBAR_WIDTH, MAX_CONTENT_WIDTH, SPACING, previous);
    }

    @Override
    public void onClose() {
        CursorsExtended.CONFIG.save();
        super.onClose();
    }

    @Override
    protected void initItems() {
        this.addGlobalItems();
        this.addAdaptiveItems();
        this.addCursorItems();
        this.addCompatibilityItems();
        this.addDebugItems();
    }

    @Override
    protected void postInit() {
        this.selectItem(this.defaultItem);
    }

    @Override
    protected void refreshItemsAndPanel() {
        if (this.refreshFuture != null && !this.refreshFuture.isDone()) {
            return;
        }

        this.getRefreshButton().active = false;
        this.refreshFuture = CompletableFuture
                .runAsync(() -> reload(Objects.requireNonNull(this.minecraft).getResourceManager()), Util.backgroundExecutor())
                .whenCompleteAsync((result, error) -> {
                    if (error != null) {
                        CursorsExtended.LOGGER.error("[cursors_extended] An error occurred while refreshing cursors. {}", error.getMessage());
                    }

                    this.addCursorItems();
                    super.refreshItemsAndPanel();
                    this.getRefreshButton().active = true;
                }, this.minecraft);
    }

    private void addGlobalItems() {
        this.addCategoryOnly(GLOBAL_CATEGORY, new GlobalOptionsPanel(this::refreshCursors));
    }

    private void addAdaptiveItems() {
        this.addCategoryOnly(new CatalogItem("adaptive", ADAPTIVE_TEXT), new AdaptiveOptionsPanel(ADAPTIVE_TEXT, this.animationHelper, this::refreshCursors));
    }

    private void addCompatibilityItems() {
        this.addCategoryOnly(new CatalogItem("compatibility", COMPAT_TEXT), new CompatibilityOptionsPanel(COMPAT_TEXT));
    }

    private void addCursorItems() {
        for (CatalogItem cursorItem : this.createCursorItems()) {
            if (CursorType.DEFAULT.toString().equals(cursorItem.id())) {
                this.defaultItem = cursorItem;
            }

            this.addOrUpdateItem(CURSORS_CATEGORY, cursorItem, new CursorOptionsPanel(
                    this.animationHelper,
                    this::refreshCursors,
                    GLOBAL_CATEGORY,
                    Objects.requireNonNull(CursorManager.INSTANCE.getCursor(cursorItem.id()))
            ));
        }
    }

    private void addDebugItems() {
        this.addCategoryOnly(new CatalogItem("debug", DEBUG_TEXT), new DebugOptionsPanel(DEBUG_TEXT));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.refreshFuture != null && !this.refreshFuture.isDone()) {
            guiGraphics.requestCursor(CursorTypesExt.BUSY);
        }
    }

    private int renderListCursor(GuiGraphics guiGraphics, Font font, CatalogItem item, LayoutElement bounds, int spacing, int mouseX, int mouseY, float partialTick) {
        Cursor cursor = CursorManager.INSTANCE.getCursor(item.id());

        if (cursor != null && cursor.isLoaded()) {
            int prefixX = bounds.getX() + spacing;
            int prefixY = bounds.getY() + (bounds.getHeight() - LIST_CURSOR_SIZE) / 2;
            this.animationHelper.drawSprite(guiGraphics, cursor, prefixX, prefixY, LIST_CURSOR_SIZE);
        }

        return LIST_CURSOR_SIZE;
    }

    private void refreshCursors() {
        this.addCursorItems();
        this.refreshItems();
    }

    private List<CatalogItem> createCursorItems() {
        return CursorManager.INSTANCE.getCursors()
                .stream()
                .map(cursor -> new CatalogItem(
                        cursor.getName(),
                        cursor.getText().copy().withStyle(getCursorFormat(cursor)),
                        this::renderListCursor
                ))
                .toList();
    }

    private static ChatFormatting getCursorFormat(@NotNull Cursor cursor) {
        if (cursor.isEnabled()) {
            return ChatFormatting.WHITE;
        }
        if (cursor.isLoaded()) {
            return ChatFormatting.GRAY;
        }
        return ChatFormatting.DARK_GRAY;
    }
}
