package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus.access;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookPage.class)
public interface RecipeBookResultsAccessor {
    @Accessor("hoveredButton")
    RecipeButton getHoveredResultButton();

    @Accessor("overlay")
    OverlayRecipeComponent getAlternatesWidget();
}
