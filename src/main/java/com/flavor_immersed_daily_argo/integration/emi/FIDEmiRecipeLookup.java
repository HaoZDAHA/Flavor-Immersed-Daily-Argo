package com.flavor_immersed_daily_argo.integration.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;

// 订单实体按 R 时打开 EMI 的配方页。
//
// 单独成类：只有确认装了 EMI 才会走到这里，避免未安装时加载 EMI 的类。
public final class FIDEmiRecipeLookup {

    private FIDEmiRecipeLookup() {
    }

    public static void showRecipes(ItemStack stack) {
        if (!stack.isEmpty()) {
            EmiApi.displayRecipes(EmiStack.of(stack));
        }
    }
}
