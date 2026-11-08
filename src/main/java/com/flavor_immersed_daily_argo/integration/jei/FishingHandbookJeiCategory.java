package com.flavor_immersed_daily_argo.integration.jei;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookResource;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// 「南山钓竿钓获」JEI 分类：一条鱼一页，左边放战利品，右边列出图鉴里的全部参数。
public class FishingHandbookJeiCategory implements IRecipeCategory<FishingHandbookEntry> {

    public static final RecipeType<FishingHandbookEntry> TYPE = RecipeType.create(
            flavor_immersed_daily_argo.MODID, "fishing_handbook", FishingHandbookEntry.class);

    private static final int WIDTH = 190;
    private static final int HEIGHT = 96;

    private final IDrawable icon;
    private final IDrawable background;

    public FishingHandbookJeiCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(new ItemStack(ModItems.MOD_FISHING_ROD.get()));
        this.background = helper.createBlankDrawable(WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<FishingHandbookEntry> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.flavor_immersed_daily_argo.fishing.category");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FishingHandbookEntry entry, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 4, 4).addItemStack(entry.icon());
    }

    @Override
    public void draw(FishingHandbookEntry entry, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        List<Component> lines = FishingHandbookResource.lines(entry);
        int y = 26;
        for (Component line : lines) {
            graphics.drawString(font, line, 4, y, 0xFF404040, false);
            y += 10;
        }
    }
}
