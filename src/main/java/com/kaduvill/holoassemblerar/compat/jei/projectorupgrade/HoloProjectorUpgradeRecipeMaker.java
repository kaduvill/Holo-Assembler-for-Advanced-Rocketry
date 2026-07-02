package com.kaduvill.holoassemblerar.compat.jei.projectorupgrade;

import com.kaduvill.holoassemblerar.HoloAssemblerAR;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

public final class HoloProjectorUpgradeRecipeMaker {

    private HoloProjectorUpgradeRecipeMaker() {
    }

    public static List<HoloProjectorUpgradeWrapper> getRecipes(IJeiHelpers helpers) {
        Item projectorItem = Item.getByNameOrId("libvulpes:holoProjector");

        if (projectorItem == null || HoloAssemblerAR.HOLO_ASSEMBLER == null) {
            return Collections.emptyList();
        }

        ItemStack projector = new ItemStack(projectorItem);
        ItemStack assembler = new ItemStack(HoloAssemblerAR.HOLO_ASSEMBLER);

        if (projector.isEmpty() || assembler.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new HoloProjectorUpgradeWrapper(projector, assembler));
    }
}