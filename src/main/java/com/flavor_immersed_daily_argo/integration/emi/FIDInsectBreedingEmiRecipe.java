package com.flavor_immersed_daily_argo.integration.emi;

import com.flavor_immersed_daily_argo.recipe.InsectBreedingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

// EMI 昆虫培育配方页。
//
// 排布与 JEI 侧保持一致：左边竖排饲料 / 载体 / 父代，右边是子代输入 → 产出。
// 标签直接复用 JEI 分类用的那套 jei.flavor_immersed_daily_argo.insect_breeding.* 文案，
// 免得同一句话在两边各写一遍。
final class FIDInsectBreedingEmiRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 70;

    private static final int LABEL_COLOR = 0xFF404040;
    private static final int HINT_COLOR = 0xFF808080;

    private final InsectBreedingRecipe recipe;

    FIDInsectBreedingEmiRecipe(RecipeHolder<InsectBreedingRecipe> holder) {
        super(FIDEmiPlugin.INSECT_BREEDING, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        this.inputs.add(EmiIngredient.of(recipe.getFeed()));
        this.inputs.add(EmiIngredient.of(recipe.getCarrier()));
        recipe.getParents().ifPresent(parents -> this.inputs.add(EmiIngredient.of(parents)));
        recipe.getOffspringInput().ifPresent(input -> this.inputs.add(EmiIngredient.of(input)));
        this.outputs.add(EmiStack.of(recipe.getOffspringOutput()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
// 饲料 / 载体
        widgets.addSlot(EmiIngredient.of(recipe.getFeed()), 52, 9);
        widgets.addSlot(EmiIngredient.of(recipe.getCarrier()), 52, 31);
// 父代：两个槽
        recipe.getParents().ifPresent(parents -> {
            widgets.addSlot(EmiIngredient.of(parents), 52, 53);
            widgets.addSlot(EmiIngredient.of(parents), 70, 53);
        });
// 子代输入 → 产出
        recipe.getOffspringInput().ifPresent(input -> widgets.addSlot(EmiIngredient.of(input), 100, 31));
        widgets.addSlot(EmiStack.of(recipe.getOffspringOutput()), 132, 31).recipeContext(this);

// 角色标签
        label(widgets, "feed", 2, 13);
        label(widgets, "carrier", 2, 35);
        label(widgets, "parents", 2, 57);
        label(widgets, "offspring", 100, 13);
        label(widgets, "output", 132, 53);
        widgets.addText(Component.literal("→"), 120, 36, HINT_COLOR, false);

// 父代槽为空时给出「留空」提示
        if (recipe.getParents().isEmpty()) {
            label(widgets, "parents_empty", 52, 57);
        }
    }

    private void label(WidgetHolder widgets, String path, int x, int y) {
        widgets.addText(Component.translatable("jei.flavor_immersed_daily_argo.insect_breeding." + path),
                x, y, LABEL_COLOR, false);
    }
}
