package com.kaduvill.holoassemblerar.compat.jei.projectorupgrade;

import com.kaduvill.holoassemblerar.HoloAssemblerAR;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HoloProjectorUpgradeCategory implements IRecipeCategory<HoloProjectorUpgradeWrapper> {

    public static final String UID = HoloAssemblerAR.MOD_ID + ".holo_projector_upgrade";

    private static final String LANG_TITLE = "jei.holoassemblerar.projector_upgrade.title";
    private static final String LANG_NOTE = "jei.holoassemblerar.projector_upgrade.note";

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotFrame;

    private static final int BG_W = 136;
    private static final int BG_H = 48;

    private static final int INPUT_X = 18;
    private static final int OUTPUT_X = 100;
    private static final int SLOT_Y = 8;

    public HoloProjectorUpgradeCategory(IGuiHelper guiHelper, HoloProjectorUpgradeWrapper preview) {
        this.background = guiHelper.createBlankDrawable(BG_W, BG_H);
        this.icon = guiHelper.createDrawableIngredient(preview.getAssembler());
        this.slotFrame = guiHelper.getSlotDrawable();
    }

    @Nonnull
    @Override
    public String getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format(LANG_TITLE);
    }

    @Nonnull
    @Override
    public String getModName() {
        return "Holo-Assembler AR";
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout layout,
                          @Nonnull HoloProjectorUpgradeWrapper wrapper,
                          @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup items = layout.getItemStacks();

        items.init(0, true, INPUT_X, SLOT_Y);
        items.init(1, false, OUTPUT_X, SLOT_Y);

        items.set(0, wrapper.getProjector());
        items.set(1, wrapper.getAssembler());
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        slotFrame.draw(minecraft, INPUT_X, SLOT_Y);
        slotFrame.draw(minecraft, OUTPUT_X, SLOT_Y);

        minecraft.fontRenderer.drawString("->", 63, 13, 0x555555);
        minecraft.fontRenderer.drawString(I18n.format(LANG_NOTE), 16, 34, 0x555555);
    }
}