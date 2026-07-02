package com.kaduvill.holoassemblerar.compat.jei.projectorupgrade;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HoloProjectorUpgradeWrapper implements IRecipeWrapper {

    private final ItemStack projector;
    private final ItemStack assembler;

    public HoloProjectorUpgradeWrapper(ItemStack projector, ItemStack assembler) {
        this.projector = projector.copy();
        this.assembler = assembler.copy();
    }

    public ItemStack getProjector() {
        return projector.copy();
    }

    public ItemStack getAssembler() {
        return assembler.copy();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(Collections.singletonList(projector));
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);

        List<ItemStack> outputs = new ArrayList<>();

        // Visible output. Category renders this in the output slot.
        outputs.add(assembler);

        // Hidden/discoverability output:
        // lets R / left-click on Holo-Projector find this JEI page.
        outputs.add(projector);

        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }
}