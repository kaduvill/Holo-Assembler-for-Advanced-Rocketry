package com.kaduvill.holoassemblerar.compat.jei.projectorupgrade;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public final class HoloProjectorUpgradeRecipeHandler implements IRecipeHandler<HoloProjectorUpgradeWrapper> {

    @Override
    public Class<HoloProjectorUpgradeWrapper> getRecipeClass() {
        return HoloProjectorUpgradeWrapper.class;
    }

    @Override
    public String getRecipeCategoryUid(HoloProjectorUpgradeWrapper recipe) {
        return HoloProjectorUpgradeCategory.UID;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(HoloProjectorUpgradeWrapper recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(HoloProjectorUpgradeWrapper recipe) {
        return recipe != null;
    }
}