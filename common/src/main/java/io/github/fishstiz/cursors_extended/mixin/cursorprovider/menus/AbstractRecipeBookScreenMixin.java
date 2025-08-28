package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus.access.RecipeAlternativesWidgetAccessor;
import io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus.access.RecipeBookResultsAccessor;
import io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus.access.RecipeBookWidgetAccessor;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin extends AbstractContainerScreenMixin<RecipeBookMenu> {
    @Shadow
    @Final
    private RecipeBookComponent<?> recipeBookComponent;

    protected AbstractRecipeBookScreenMixin(Component title) {
        super(title);
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (this.recipeBookComponent.isVisible()) {
            RecipeBookWidgetAccessor recipeBook = (RecipeBookWidgetAccessor) this.recipeBookComponent;
            RecipeBookResultsAccessor recipesArea = (RecipeBookResultsAccessor) recipeBook.getRecipesArea();
            RecipeAlternativesWidgetAccessor alternatesWidget = (RecipeAlternativesWidgetAccessor) recipesArea.getAlternatesWidget();

            if (((OverlayRecipeComponent) alternatesWidget).isVisible()) {
                return cursors_extended$getAlternatesWidgetCursor(alternatesWidget);
            }

            boolean isResultHovered = recipesArea.getHoveredResultButton() != null;
            if (isResultHovered && CursorTypeUtil.canShift()) {
                return CursorTypesExt.SHIFT;
            } else if (this.cursors_extended$isButtonHovered(recipeBook, recipesArea) || isResultHovered) {
                return CursorTypes.POINTING_HAND;
            } else if (recipeBook.getSearchField().isHovered()) {
                return CursorTypes.IBEAM;
            }

            CursorType tabCursorType = this.cursors_extended$getTabCursor(recipeBook);
            if (tabCursorType != CursorType.DEFAULT) {
                return tabCursorType;
            }
        }

        return super.cursors_extended$cursorType(mouseX, mouseY);
    }

    @Unique
    private CursorType cursors_extended$getAlternatesWidgetCursor(RecipeAlternativesWidgetAccessor alternatesWidget) {
        for (AbstractWidget alternativeButton : alternatesWidget.getAlternativeButtons()) {
            if (alternativeButton.isActive() && alternativeButton.isHovered()) {
                return CursorTypeUtil.canShift() ? CursorTypesExt.SHIFT : CursorTypes.POINTING_HAND;
            }
        }
        return CursorTypes.ARROW;
    }

    @Unique
    private boolean cursors_extended$isButtonHovered(RecipeBookWidgetAccessor recipeBook, RecipeBookResultsAccessor recipesArea) {
        StateSwitchingButton prevPageButton = recipesArea.getPrevPageButton();
        StateSwitchingButton nextPageButton = recipesArea.getNextPageButton();
        StateSwitchingButton toggleCraftableButton = recipeBook.getToggleCraftableButton();

        return (toggleCraftableButton.visible && toggleCraftableButton.isHovered()) ||
               (prevPageButton.visible && prevPageButton.isHovered()) ||
               (nextPageButton.visible && nextPageButton.isHovered());
    }

    @Unique
    private CursorType cursors_extended$getTabCursor(RecipeBookWidgetAccessor recipeBook) {
        for (RecipeBookTabButton tabButton : recipeBook.getTabButtons()) {
            if (tabButton.isHovered() && tabButton.isActive() && tabButton != recipeBook.getCurrentTab()) {
                return CursorTypes.POINTING_HAND;
            }
        }
        return CursorType.DEFAULT;
    }
}
