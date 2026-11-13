package com.flavor_immersed_daily_argo.herding;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// 空手直接右键查看动物状态栏。潜行右键不再打开命名界面。
// 不取消事件，保留原版/其他模组交互；命名牌仍走原版物品交互。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
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
        if (!event.getEntity().isSecondaryUseActive()) {
            AnimalStatsHud.show(target);
        }
    }
}
