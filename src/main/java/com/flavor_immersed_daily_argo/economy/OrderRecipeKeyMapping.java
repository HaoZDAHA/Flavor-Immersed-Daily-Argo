package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.integration.emi.FIDEmiRecipeLookup;
import com.flavor_immersed_daily_argo.integration.jei.FIDAJEIPlugin;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

// 瞄准顾客 / 外卖小哥时按 R，用 JEI 或 EMI 打开这道菜的配方。
//
// 键位本身在 flavor_immersed_daily_argoClient#registerKeyMappings（MOD 总线）注册，
// 这里只处理按下后的动作。JEI 优先，其次是 EMI；两者都没装就在动作栏提示。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class OrderRecipeKeyMapping {

    public static final KeyMapping SHOW_ORDER_RECIPE = new KeyMapping(
            "key.flavor_immersed_daily_argo.show_order_recipe",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.flavor_immersed_daily_argo.economy"
    );

    private static final String KEY_NO_VIEWER = "message.flavor_immersed_daily_argo.no_recipe_viewer";

    private OrderRecipeKeyMapping() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (SHOW_ORDER_RECIPE.consumeClick()) {
            ItemStack order = aimedOrder(minecraft);
            if (order.isEmpty()) {
                continue;
            }
            if (ModList.get().isLoaded("jei") && FIDAJEIPlugin.showRecipes(order)) {
                continue;
            }
            if (ModList.get().isLoaded("emi")) {
                FIDEmiRecipeLookup.showRecipes(order);
                continue;
            }
            minecraft.player.displayClientMessage(Component.translatable(KEY_NO_VIEWER), true);
        }
    }

// 准星指着的订单实体要交的食物；没指着订单实体时返回空栈
    private static ItemStack aimedOrder(Minecraft minecraft) {
        if (minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof FoodOrder order) {
            return order.getRequiredFood();
        }
        return ItemStack.EMPTY;
    }
}
