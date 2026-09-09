package io.github.fishstiz.cursors_extended.gui.panels;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.gui.components.OptionsListWidget;
import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.util.function.Consumer;

public class DebugOptionsPanel extends AbstractContentPanel {
    private static final String ISSUES_LINK = "https://github.com/fishstiz/cursors_extended/issues";
    private static final String WIKI_LINK = "https://fishstiz.github.io/cursors_extended-wiki/resource-pack/getting-started";
    private static final Component INSPECT_TEXT = Component.translatable("cursors_extended.options.debug.inspect");
    private static final Component REPORT_ISSUES_TEXT = Component.translatable("cursors_extended.options.debug.report_issues");
    private static final Component OPEN_WIKI_TEXT = Component.translatable("cursors_extended.options.debug.open_wiki");
    private OptionsListWidget list;

    public DebugOptionsPanel(Minecraft minecraft, Screen screen) {
        super("Debug", minecraft, screen, Component.translatable("cursors_extended.options.debug"));
    }

    @Override
    protected void buildKeywords(Consumer<Component> builder) {
        builder.accept(INSPECT_TEXT);
        builder.accept(REPORT_ISSUES_TEXT);
        builder.accept(OPEN_WIKI_TEXT);
    }

    @Override
    protected void buildWidgets(GuiComponentCollector collector, FZFlexLayout layout) {
        this.list = new OptionsListWidget();

        this.list.rowBuilder()
                .label(INSPECT_TEXT)
                .toggleButton(
                        CursorsExtended.getInstance().getDisplay()::isDebugging,
                        _ -> CursorsExtended.getInstance().getDisplay().toggleDebugger()
                )
                .buildRow();

        this.list.addEntry(REPORT_ISSUES_TEXT, FZButton.builder()
                .message(REPORT_ISSUES_TEXT)
                .leftAlignedMessage()
                .onPress(e -> ConfirmLinkScreen.confirmLink(screen, URI.create(ISSUES_LINK), true).onPress(e.target()))
                .build());

        this.list.addEntry(OPEN_WIKI_TEXT, FZButton.builder()
                .message(OPEN_WIKI_TEXT)
                .leftAlignedMessage()
                .onPress(e -> ConfirmLinkScreen.confirmLink(screen, URI.create(WIKI_LINK), true).onPress(e.target()))
                .build());

        layout.child(this.list, layout.flexChildSettings());
    }

    @Override
    public void onSearch(String query) {
        if (this.list != null) {
            this.list.onSearch(query);
        }
    }
}
