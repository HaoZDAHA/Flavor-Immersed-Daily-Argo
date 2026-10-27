package com.flavor_immersed_daily_argo.integration.jei;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.recipe.InsectNetDrop;
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
import net.minecraft.world.level.block.Blocks;

// JEI「捕虫」分类：捕虫网 + 草 → 对应昆虫，并标出掉落概率。
//
// 每种昆虫各占一条配方，方便在 JEI 里按昆虫反查来源。
// 概率文案直接取 InsectNetDrop#chance()，与实际掉落逻辑同源。
public class InsectNetCategory implements IRecipeCategory<InsectNetDrop> {

    public static final RecipeType<InsectNetDrop> TYPE =
            RecipeType.create(flavor_immersed_daily_argo.MODID, "insect_net", InsectNetDrop.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 50;

    private static final int HINT_COLOR = 0xFF808080;

    private final IDrawable background;
    private final IDrawable icon;

    public InsectNetCategory(IGuiHelper helper) {
// JEI 默认按 256×256 取 UV，底图不是这个尺寸就得显式告诉它实际大小，否则会被拉伸
        this.background = helper.drawableBuilder(GuiTextures.JEI_INSECT_NET, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, HEIGHT)
                .build();
        this.icon = helper.createDrawableItemStack(new ItemStack(ModItems.INSECT_NET.get()));
    }

    @Override
    public RecipeType<InsectNetDrop> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + flavor_immersed_daily_argo.MODID + ".insect_net");
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
    public void setRecipe(IRecipeLayoutBuilder builder, InsectNetDrop recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 24, 16)
                .addItemStack(new ItemStack(ModItems.INSECT_NET.get()));
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 16)
                .addItemStack(new ItemStack(Blocks.SHORT_GRASS));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 16)
                .addItemStack(new ItemStack(recipe.item()));
    }

    @Override
    public void draw(InsectNetDrop recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, "→", 72, 21, HINT_COLOR, false);
        guiGraphics.drawString(font,
                Component.translatable("jei." + flavor_immersed_daily_argo.MODID + ".insect_net.chance",
                        Math.round(recipe.chance() * 100.0F)),
                120, 21, HINT_COLOR, false);
    }
}
