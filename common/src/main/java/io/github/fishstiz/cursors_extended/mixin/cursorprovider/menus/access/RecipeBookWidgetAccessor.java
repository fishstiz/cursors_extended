package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus.access;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookWidgetAccessor {
    @Accessor("searchBox")
    EditBox getSearchField();

    @Accessor("filterButton")
    StateSwitchingButton getToggleCraftableButton();

    @Accessor("tabButtons")
    List<RecipeBookTabButton> getTabButtons();

    @Accessor("selectedTab")
    RecipeBookTabButton getCurrentTab();

    @Accessor("recipeBookPage")
    RecipeBookPage getRecipesArea();
}
