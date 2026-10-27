package com.flavor_immersed_daily_argo.herding;

import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

// 空手右键动物的两种客户端表现：
//
// 直接右键：在屏幕上浮出这只动物的状态栏（AnimalStatsHud）；
// 潜行右键：打开取名界面（AnimalRenameScreen），确认后交给服务端写入。
//
// 这里不取消事件，原版空手右键本来就没有行为，不需要拦截。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class AnimalStatsClient {

// 超过这个距离就不响应（与方块交互距离一致，约 8 格）
    private static final double REACH_SQR = 64.0D;

    private AnimalStatsClient() {
    }

    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
// 只认主手：客户端一次点击会把主手、副手依次走一遍，不限定的话界面会开两次
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !event.getEntity().level().isClientSide
                || !(event.getTarget() instanceof LivingEntity target)
                || !AnimalStatsManager.isTracked(target)
                || !event.getItemStack().isEmpty()
                || event.getEntity().distanceToSqr(target) > REACH_SQR) {
            return;
        }
        if (event.getEntity().isSecondaryUseActive()) {
            Minecraft.getInstance().setScreen(new AnimalRenameScreen(target));
        } else {
            AnimalStatsHud.show(target);
        }
    }
}
