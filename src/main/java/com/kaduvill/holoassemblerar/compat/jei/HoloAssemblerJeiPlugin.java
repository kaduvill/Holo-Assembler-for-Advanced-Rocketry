package com.kaduvill.holoassemblerar.compat.jei;

import com.kaduvill.holoassemblerar.compat.jei.projectorupgrade.HoloProjectorUpgradeCategory;
import com.kaduvill.holoassemblerar.compat.jei.projectorupgrade.HoloProjectorUpgradeRecipeHandler;
import com.kaduvill.holoassemblerar.compat.jei.projectorupgrade.HoloProjectorUpgradeRecipeMaker;
import com.kaduvill.holoassemblerar.compat.jei.projectorupgrade.HoloProjectorUpgradeWrapper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.JEIPlugin;

import java.util.List;

@JEIPlugin
public final class HoloAssemblerJeiPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        List<HoloProjectorUpgradeWrapper> recipes =
                HoloProjectorUpgradeRecipeMaker.getRecipes(registry.getJeiHelpers());

        if (recipes.isEmpty()) {
            return;
        }

        registry.addRecipeCategories(
                new HoloProjectorUpgradeCategory(guiHelper, recipes.get(0))
        );
    }

    @Override
    public void register(IModRegistry registry) {
        List<HoloProjectorUpgradeWrapper> recipes =
                HoloProjectorUpgradeRecipeMaker.getRecipes(registry.getJeiHelpers());

        if (recipes.isEmpty()) {
            return;
        }

        registry.addRecipeHandlers(new HoloProjectorUpgradeRecipeHandler());

        registry.addRecipes(
                recipes,
                HoloProjectorUpgradeCategory.UID
        );

        // Optional: makes the projector show as a catalyst for this category.
        registry.addRecipeCatalyst(
                recipes.get(0).getProjector(),
                HoloProjectorUpgradeCategory.UID
        );

        // Optional: assembler also opens the upgrade page as a related page.
        registry.addRecipeCatalyst(
                recipes.get(0).getAssembler(),
                HoloProjectorUpgradeCategory.UID
        );
    }
}