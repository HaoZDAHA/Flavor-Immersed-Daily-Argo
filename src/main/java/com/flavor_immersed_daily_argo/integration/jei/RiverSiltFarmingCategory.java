package com.flavor_immersed_daily_argo.integration.jei;

import com.flavor_immersed_daily_argo.all.ModBlocks;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.recipe.RiverSiltFarmingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

// JEI 河道淤泥养殖分类。
//
// 一页只有「苗种 → 产物」两个槽位，中间画一个箭头；具体要先把淤泥泡在水下、
// 用饲料养成含水状态，则写在淤泥物品的说明页里。
public class RiverSiltFarmingCategory implements IRecipeCategory<RiverSiltFarmingRecipe> {

    public static final RecipeType<RiverSiltFarmingRecipe> TYPE =
            RecipeType.create(flavor_immersed_daily_argo.MODID, "river_silt_farming", RiverSiltFarmingRecipe.class);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 44;

    private static final int LABEL_COLOR = 0xFF404040;
    private static final int ARROW_COLOR = 0xFF808080;

    private final IDrawable background;
    private final IDrawable icon;

    public RiverSiltFarmingCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.RIVER_SILT.asItem()));
    }

    @Override
    public RecipeType<RiverSiltFarmingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + flavor_immersed_daily_argo.MODID + ".river_silt_farming");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RiverSiltFarmingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 13).addIngredients(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 73, 13).addItemStack(recipe.getResult());
    }

    @Override
    public void draw(RiverSiltFarmingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        label(guiGraphics, "input", 2, 2);
        label(guiGraphics, "output", 64, 2);
        guiGraphics.drawString(font, "→", 46, 18, ARROW_COLOR, false);
    }

    private void label(GuiGraphics guiGraphics, String path, int x, int y) {
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.flavor_immersed_daily_argo.river_silt_farming." + path),
                x, y, LABEL_COLOR, false);
    }
}
