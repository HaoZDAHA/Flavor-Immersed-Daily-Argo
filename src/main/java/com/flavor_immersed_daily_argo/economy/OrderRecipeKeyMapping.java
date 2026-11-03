package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.integration.emi.FIDEmiRecipeLookup;
import com.flavor_immersed_daily_argo.integration.jei.FIDAJEIPlugin;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

// 瞄准顾客 / 外卖小哥时按 R，用 JEI 或 EMI 打开这道菜的配方。
//
// 键位本身在 flavor_immersed_daily_argoClient#registerKeyMappings（MOD 总线）注册，
// 这里只处理按下后的动作。JEI 优先，其次是 EMI；两者都没装就在动作栏提示。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class OrderRecipeKeyMapping {

    public static final KeyMapping SHOW_ORDER_RECIPE = new KeyMapping(
            "key.flavor_immersed_daily_argo.show_order_recipe",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.flavor_immersed_daily_argo.economy"
    );

    private static final String KEY_NO_VIEWER = "message.flavor_immersed_daily_argo.no_recipe_viewer";
// 手瞄准星够不到前后柜台边的顾客时，再按这个距离做一次实体射线检测
    private static final double AIM_DISTANCE = 5.0;

    private OrderRecipeKeyMapping() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (SHOW_ORDER_RECIPE.consumeClick()) {
            FoodOrder aimed = aimedOrder(minecraft);
            if (aimed == null) {
                continue;
            }
            ItemStack order = aimed.getRequiredFood();
            if (order.isEmpty()) {
                continue;
            }
            if (ModList.get().isLoaded("jei") && FIDAJEIPlugin.showRecipes(order)) {
                continue;
            }
            if (ModList.get().isLoaded("emi") && FIDEmiRecipeLookup.showRecipes(order)) {
                continue;
            }
            minecraft.player.displayClientMessage(Component.translatable(KEY_NO_VIEWER), true);
        }
    }

// 准星指着的订单实体；先看原版命中结果与准星实体，都落空时再自己补一次射线检测
    public static FoodOrder aimedOrder(Minecraft minecraft) {
        if (minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof FoodOrder order) {
            return order;
        }
        if (minecraft.crosshairPickEntity instanceof FoodOrder order) {
            return order;
        }
        Entity camera = minecraft.getCameraEntity();
        if (camera == null) {
            return null;
        }
        Vec3 eye = camera.getEyePosition(1.0F);
        Vec3 end = eye.add(camera.getViewVector(1.0F).scale(AIM_DISTANCE));
        AABB box = camera.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        EntityHitResult fallback = ProjectileUtil.getEntityHitResult(camera, eye, end, box,
                entity -> entity instanceof FoodOrder && entity != camera,
                AIM_DISTANCE * AIM_DISTANCE);
        return fallback != null && fallback.getEntity() instanceof FoodOrder order ? order : null;
    }
}
