package io.github.fishstiz.cursors_extended.gui.screen.panel;

import io.github.fishstiz.cursors_extended.cursor.CursorProviderInspector;
import io.github.fishstiz.cursors_extended.gui.widget.ButtonWidget;
import io.github.fishstiz.cursors_extended.gui.widget.OptionsListWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugOptionsPanel extends AbstractOptionsPanel {
    private static final String ISSUES_LINK = "https://github.com/fishstiz/cursors_extended/issues";
    private static final String WIKI_LINK = "https://fishstiz.github.io/cursors_extended-wiki/resource-pack/getting-started";
    private static final Component INSPECT_TEXT = Component.translatable("cursors_extended.options.debug.inspect");
    private static final Component REPORT_ISSUES_TEXT = Component.translatable("cursors_extended.options.debug.report_issues");
    private static final Component OPEN_WIKI_TEXT = Component.translatable("cursors_extended.options.debug.open_wiki");
    private OptionsListWidget optionsList;

    public DebugOptionsPanel(Component title) {
        super(title);
    }

    @Override
    protected void initContents() {
        this.optionsList = new OptionsListWidget(this.getMinecraft(), this.getFont(), this.getSpacing());

        this.optionsList.addToggle(
                CursorProviderInspector.INSTANCE.getInspector().isInspecting(),
                v -> CursorProviderInspector.INSTANCE.toggleInspector(),
                this.index(INSPECT_TEXT),
                null,
                true
        );

        ButtonWidget wikiButton = new ButtonWidget(
                this.index(OPEN_WIKI_TEXT),
                ConfirmLinkScreen.confirmLink(this.getScreen(), WIKI_LINK, true)
        );
        wikiButton.active = false;
        this.optionsList.addWidget(wikiButton);
        this.optionsList.addWidget(new ButtonWidget(
                this.index(REPORT_ISSUES_TEXT),
                ConfirmLinkScreen.confirmLink(this.getScreen(), ISSUES_LINK, true)
        ));

        this.optionsList.search(this.getSearch());

        this.addRenderableWidget(this.optionsList);
    }

    @Override
    protected void repositionContents(int x, int y) {
        if (this.optionsList != null) {
            this.optionsList.setSize(this.getWidth(), this.computeMaxHeight(y));
            this.optionsList.setPosition(x, y);
        }
    }

    @Override
    protected void searched(@NotNull String search, @Nullable Component matched) {
        if (this.optionsList != null) {
            this.optionsList.search(search);
        }
    }
}
