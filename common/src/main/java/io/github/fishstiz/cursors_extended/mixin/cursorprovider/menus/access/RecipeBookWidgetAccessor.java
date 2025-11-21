package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus.access;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookWidgetAccessor {
    @Accessor("recipeBookPage")
    RecipeBookPage getRecipesArea();
}
