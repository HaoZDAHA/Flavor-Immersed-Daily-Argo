package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.AnimalFeedItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

// 状态栏的服务端规则入口：推进数值、后代继承、以及「用原版食物喂食」也算喂过。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AnimalStatsEvents {

// 每 tick 推进一次：衰减、靠近水喝水、泡水淋雨洗澡、身边长大的判定
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.level().isClientSide) {
            return;
        }
        if (AnimalStatsManager.isTracked(living)) {
            AnimalStatsManager.serverTick(living);
        }
    }

// 拥有状态栏的父母生下的幼崽自动继承
    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (event.getChild() != null) {
            AnimalStatsManager.inheritToChild(event.getChild(), event.getParentA(), event.getParentB());
        }
    }

// 用动物原本认的食物喂食同样算「被玩家喂食」。
//
// 本模组自己的四种饲料走 AnimalFeedItem 内部那条路径，这里跳过以免重复计数。
    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Animal animal) || !AnimalStatsManager.isTracked(animal)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() instanceof AnimalFeedItem) {
            return;
        }
        if (animal.isBaby() || animal.isInLove() || !animal.isFood(stack)) {
            return;
        }
        if (!animal.level().isClientSide) {
            AnimalStatsManager.onFed(animal);
        }
    }
}
