package com.flavor_immersed_daily_argo.integration.jei;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// JEI 兼容：只做「按 R 查看订单菜品配方」用的运行时句柄。
// 没装 JEI 时这个类不会被加载，主流程通过 ModList 判断后再调用。
@JeiPlugin
public final class FIDAJEIPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(flavor_immersed_daily_argo.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
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
