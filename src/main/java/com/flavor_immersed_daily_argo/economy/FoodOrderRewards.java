package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.economy.product.EconomicNews;
import com.flavor_immersed_daily_argo.economy.product.WeightPriceData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.lang.reflect.Method;
import java.util.Map;

/** Server adapter shared by counter customers and receipt-printer riders, never phone delivery. */
public final class FoodOrderRewards {
    private FoodOrderRewards() { }

    public static long reward(ServerLevel level, ItemStack order) {
        if (order.isEmpty()) return 0L;
        FoodProperties food = order.get(DataComponents.FOOD);
        if (food == null) return 0L;
        FoodOrderPricing.Limits limits = limits();
        boolean raw = isRaw(order);
        long day = level.getDayTime() / 24000L;
        Map<Item, Integer> prices = EconomicNews.todayPrices(day);
        FoodOrderPricing.Profile cheapest = null;
        int cheapestReward = Integer.MAX_VALUE;
        // Read actual server recipes so data-pack output counts and cooking times are honored.
        // Choose the least expensive valid route rather than rewarding an optional costly recipe.
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            Recipe<?> recipe = holder.value();
            ItemStack result;
            try {
                result = recipe.getResultItem(level.registryAccess());
            } catch (RuntimeException ignored) {
                continue; // Some dynamic recipes expose no static result.
            }
            if (result.isEmpty() || !result.is(order.getItem())) continue;
            int kinds = 0;
            double materialUnits = 0;
            double materialCost = 0;
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.isEmpty()) continue;
                ItemStack[] alternatives = ingredient.getItems();
                if (alternatives.length == 0) continue;
                kinds++;
                double cheapestCost = Double.POSITIVE_INFINITY;
                int cheapestCount = 1;
                for (ItemStack input : alternatives) {
                    if (input.isEmpty()) continue;
                    int count = Math.max(1, input.getCount());
                    double cost = opportunityCost(input, day, prices) * count;
                    if (cost < cheapestCost) {
                        cheapestCost = cost;
                        cheapestCount = count;
                    }
                }
                if (Double.isFinite(cheapestCost)) {
                    materialUnits += cheapestCount;
                    materialCost += cheapestCost;
                }
            }
            int portions = Math.max(1, result.getCount());
            FoodOrderPricing.Profile profile = new FoodOrderPricing.Profile(food.nutrition(), food.saturation(),
                    materialUnits / portions, kinds, cookingTicks(recipe) / (20.0D * portions),
                    true, raw, materialCost / portions);
            int value = FoodOrderPricing.unitPrice(profile, limits);
            if (value < cheapestReward) {
                cheapestReward = value;
                cheapest = profile;
            }
        }
        if (cheapest == null) {
            // Unknown external processing formats get a conservative nutrition-based quote;
            // raw crops/meat still cannot masquerade as high-end cooking.
            cheapest = new FoodOrderPricing.Profile(food.nutrition(), food.saturation(), 0, 0, 0,
                    false, raw, 0);
        }
        return FoodOrderPricing.total(cheapest, order.getCount(), limits);
    }

    private static FoodOrderPricing.Limits limits() {
        return new FoodOrderPricing.Limits(
                EconomyConfig.FOOD_ORDER_RAW_MIN.get(), EconomyConfig.FOOD_ORDER_RAW_MAX.get(),
                EconomyConfig.FOOD_ORDER_SIMPLE_MIN.get(), EconomyConfig.FOOD_ORDER_SIMPLE_MAX.get(),
                EconomyConfig.FOOD_ORDER_COMPLEX_MIN.get(), EconomyConfig.FOOD_ORDER_COMPLEX_MAX.get(),
                EconomyConfig.FOOD_ORDER_COST_MULTIPLIER.get(), EconomyConfig.FOOD_ORDER_PROCESSING_FEE.get());
    }

    private static double opportunityCost(ItemStack stack, long day, Map<Item, Integer> prices) {
        int fish = WeightPriceData.fishPrice(stack, day);
        if (fish > 0) return fish;
        Integer group = prices.get(stack.getItem());
        if (group != null) return Math.max(0, group) / (double) Math.max(1, stack.getMaxStackSize());
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food == null ? 2.0D : Math.max(1.0D, food.nutrition() * 0.75D);
    }

    private static int cookingTicks(Recipe<?> recipe) {
        if (recipe instanceof AbstractCookingRecipe cooking) return Math.max(0, cooking.getCookingTime());
        // Existing fidworkblock recipes expose this public method without a compile-time dependency.
        try {
            Method method = recipe.getClass().getMethod("getCookingTime");
            Object value = method.invoke(recipe);
            return value instanceof Number number ? Math.max(0, number.intValue()) : 0;
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    private static boolean isRaw(ItemStack food) {
        String path = BuiltInRegistries.ITEM.getKey(food.getItem()).getPath();
        if (path.startsWith("raw") || path.equals("pomelo") || path.equals("chestnut")
                || path.equals("guava") || path.equals("shepherds_purse")) return true;
        return food.getTags().anyMatch(tag -> {
            String namespace = tag.location().getNamespace();
            if (!namespace.equals("c") && !namespace.equals("forge")) return false;
            String key = tag.location().getPath();
            return key.equals("crops") || key.startsWith("crops/") || key.equals("seeds")
                    || key.startsWith("seeds/") || key.equals("fruits") || key.startsWith("fruits/")
                    || key.equals("vegetables") || key.startsWith("vegetables/")
                    || key.equals("foods/raw_meat") || key.equals("foods/raw_meats")
                    || key.equals("foods/raw_fish") || key.equals("foods/raw_fishes");
        });
    }
}
