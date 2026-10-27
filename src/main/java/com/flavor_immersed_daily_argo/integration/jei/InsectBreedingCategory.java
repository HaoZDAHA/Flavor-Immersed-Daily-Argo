package com.flavor_immersed_daily_argo.integration.jei;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import com.flavor_immersed_daily_argo.all.ModBlocks;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.recipe.InsectBreedingRecipe;
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

// JEI 昆虫培育分类。
//
// 左边竖排三种原料（饲料 / 载体 / 父代），右边是子代槽里的转化：子代输入 → 产出。
// 父代槽为空时画一句「留空」提示，与配方实际要求对应。标签放在槽位左侧，
// 这样中英文文案都能放得下。
public class InsectBreedingCategory implements IRecipeCategory<InsectBreedingRecipe> {

    public static final RecipeType<InsectBreedingRecipe> TYPE =
            RecipeType.create(flavor_immersed_daily_argo.MODID, "insect_breeding", InsectBreedingRecipe.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 70;

    private static final int LABEL_COLOR = 0xFF404040;
    private static final int HINT_COLOR = 0xFF808080;

    private final IDrawable background;
    private final IDrawable icon;

    public InsectBreedingCategory(IGuiHelper helper) {
// JEI 默认按 256×256 取 UV，底图不是这个尺寸就得显式告诉它实际大小，否则会被拉伸
        this.background = helper.drawableBuilder(GuiTextures.JEI_INSECT_BREEDING, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, HEIGHT)
                .build();
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.INSECT_INCUBATOR.asItem()));
    }

    @Override
    public RecipeType<InsectBreedingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + flavor_immersed_daily_argo.MODID + ".insect_breeding");
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
    public void setRecipe(IRecipeLayoutBuilder builder, InsectBreedingRecipe recipe, IFocusGroup focuses) {
// 饲料（每次转化消耗一份）
        builder.addSlot(RecipeIngredientRole.INPUT, 52, 9).addIngredients(recipe.getFeed());
// 载体（不消耗）
        builder.addSlot(RecipeIngredientRole.INPUT, 52, 31).addIngredients(recipe.getCarrier());
// 父代：两个槽都放同一种东西
        recipe.getParents().ifPresent(parents -> {
            builder.addSlot(RecipeIngredientRole.INPUT, 52, 53).addIngredients(parents);
            builder.addSlot(RecipeIngredientRole.INPUT, 70, 53).addIngredients(parents);
        });
// 子代输入：就地转化，产物替换掉这些格子
        recipe.getOffspringInput().ifPresent(input ->
                builder.addSlot(RecipeIngredientRole.INPUT, 100, 31).addIngredients(input));
// 产出
        builder.addSlot(RecipeIngredientRole.OUTPUT, 132, 31).addItemStack(recipe.getOffspringOutput());
    }

    @Override
    public void draw(InsectBreedingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        label(guiGraphics, "feed", 2, 13);
        label(guiGraphics, "carrier", 2, 35);
        label(guiGraphics, "parents", 2, 57);
        label(guiGraphics, "offspring", 100, 13);
        label(guiGraphics, "output", 132, 53);

// 转化箭头：子代输入 → 产出
        guiGraphics.drawString(font, "→", 120, 36, HINT_COLOR, false);

// 父代槽为空时给出「留空」提示
        if (recipe.getParents().isEmpty()) {
            guiGraphics.drawString(font,
                    Component.translatable("jei.flavor_immersed_daily_argo.insect_breeding.parents_empty"),
                    52, 57, HINT_COLOR, false);
        }
    }

    private void label(GuiGraphics guiGraphics, String path, int x, int y) {
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.flavor_immersed_daily_argo.insect_breeding." + path),
                x, y, LABEL_COLOR, false);
    }
}
