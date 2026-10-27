package com.flavor_immersed_daily_argo.integration.emi;

import com.flavor_immersed_daily_argo.recipe.RiverSiltFarmingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

// EMI 河道淤泥养殖配方页。
//
// 排布与 JEI 侧一致：左边苗种、右边产物，中间一个箭头。标签复用 JEI 分类的文案。
final class FIDRiverSiltEmiRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 44;

    private static final int LABEL_COLOR = 0xFF404040;
    private static final int ARROW_COLOR = 0xFF808080;

    private final RiverSiltFarmingRecipe recipe;

    FIDRiverSiltEmiRecipe(RecipeHolder<RiverSiltFarmingRecipe> holder) {
        super(FIDEmiPlugin.RIVER_SILT_FARMING, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        this.inputs.add(EmiIngredient.of(recipe.getInput()));
        this.outputs.add(EmiStack.of(recipe.getResult()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(recipe.getInput()), 9, 13);
        widgets.addSlot(EmiStack.of(recipe.getResult()), 73, 13).recipeContext(this);
        label(widgets, "input", 2, 2);
        label(widgets, "output", 64, 2);
        widgets.addText(Component.literal("→"), 46, 18, ARROW_COLOR, false);
    }

    private void label(WidgetHolder widgets, String path, int x, int y) {
        widgets.addText(Component.translatable("jei.flavor_immersed_daily_argo.river_silt_farming." + path),
                x, y, LABEL_COLOR, false);
    }
}
