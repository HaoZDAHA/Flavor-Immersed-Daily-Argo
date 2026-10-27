package com.flavor_immersed_daily_argo.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.lang.reflect.Method;
import java.util.List;

// EMI 工作方块配方页的通用展示。
//
// fidworkblock 的工作方块共用一个配方类型，但它的 EMI 插件只做了砧板，
// 所以本模组在这里补上石磨、榨汁机、蒸笼、炒锅四类。为避免编译期依赖，
// 配方数据全部按 Recipe 的公开接口 + 反射读取。
//
// 排布：左侧横排所有材料，中间箭头，右侧产出；有时长时在产出下方标注。
final class FIDWorkblockEmiRecipe extends BasicEmiRecipe {

    private static final int SLOT_Y = 10;
    private static final int HINT_COLOR = 0xFF808080;

    private final List<Ingredient> ingredients;
    private final ItemStack result;
    private final int cookingTime;

    FIDWorkblockEmiRecipe(EmiRecipeCategory category, ResourceLocation id,
                          Recipe<?> recipe, ItemStack result) {
        super(category, id, width(recipe.getIngredients().size()), 40);
        this.ingredients = recipe.getIngredients();
        this.result = result;
        this.cookingTime = cookingTimeOf(recipe);

        for (Ingredient ingredient : ingredients) {
            this.inputs.add(EmiIngredient.of(ingredient));
        }
        this.outputs.add(EmiStack.of(result));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < ingredients.size(); i++) {
            widgets.addSlot(EmiIngredient.of(ingredients.get(i)), 8 + i * 18, SLOT_Y);
        }

        int arrowX = 8 + ingredients.size() * 18;
        widgets.addText(Component.literal("→"), arrowX, SLOT_Y + 5, HINT_COLOR, false);
        widgets.addSlot(EmiStack.of(result), arrowX + 16, SLOT_Y).recipeContext(this);

        if (cookingTime > 0) {
            widgets.addText(Component.literal(String.format("%.1fs", cookingTime / 20.0F)),
                    arrowX + 16, SLOT_Y + 21, HINT_COLOR, false);
        }
    }

    private static int width(int slots) {
        return 8 + Math.max(1, slots) * 18 + 16 + 18 + 8;
    }

// fidworkblock 的 CookingRecipe.getCookingTime()，反射读取以免编译期依赖
    static int cookingTimeOf(Recipe<?> recipe) {
        try {
            Method method = recipe.getClass().getMethod("getCookingTime");
            Object value = method.invoke(recipe);
            return value instanceof Integer time ? time : 0;
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

// fidworkblock 的 CookingRecipe.getAppliance()，返回机器名字符串
    static String applianceOf(Recipe<?> recipe) {
        try {
            Method method = recipe.getClass().getMethod("getAppliance");
            Object value = method.invoke(recipe);
            return value instanceof String appliance ? appliance : "";
        } catch (ReflectiveOperationException ignored) {
            return "";
        }
    }
}
