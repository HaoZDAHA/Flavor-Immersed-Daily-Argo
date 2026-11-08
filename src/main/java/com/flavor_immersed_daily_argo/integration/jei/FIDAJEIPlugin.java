package com.flavor_immersed_daily_argo.integration.jei;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookResource;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// JEI 兼容：订单菜品配方跳转与南山钓竿钓获分类。
// 没装 JEI 时这个类不会被加载，主流程通过 ModList 判断后再调用。
@JeiPlugin
public final class FIDAJEIPlugin implements IModPlugin {

    private static IJeiRuntime runtime;
    private static List<FishingHandbookEntry> fishingRecipes = List.of();

    public static boolean refreshFishingRecipes() {
        if (runtime == null) {
            return false;
        }
        List<FishingHandbookEntry> latest = FishingHandbookResource.load();
        if (fishingRecipes != latest) {
            runtime.getRecipeManager().hideRecipes(FishingHandbookJeiCategory.TYPE, fishingRecipes);
            runtime.getRecipeManager().addRecipes(FishingHandbookJeiCategory.TYPE, latest);
            fishingRecipes = latest;
        }
        return true;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FishingHandbookJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        fishingRecipes = FishingHandbookResource.load();
        registration.addRecipes(FishingHandbookJeiCategory.TYPE, fishingRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.MOD_FISHING_ROD.get()), FishingHandbookJeiCategory.TYPE);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(flavor_immersed_daily_argo.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        refreshFishingRecipes();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        fishingRecipes = List.of();
    }

// 在 JEI 里打开该物品作为产物的配方页；JEI 尚未就绪时返回 false
    public static boolean showRecipes(ItemStack stack) {
        IJeiRuntime current = runtime;
        if (current == null || stack.isEmpty()) {
            return false;
        }
        IFocus<ItemStack> focus = current.getJeiHelpers().getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack);
        current.getRecipesGui().show(focus);
        return true;
    }
}
