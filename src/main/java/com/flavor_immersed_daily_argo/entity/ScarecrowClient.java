package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.herding.AnimalRenameScreen;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

// 稻草人的客户端表现：空手直接右键打开取名界面。
//
// 潜行右键是收回，走 Scarecrow#interact 那条服务端逻辑，这里不参与。界面本身复用
// AnimalRenameScreen，确认后由服务端写入名字，稻草人会把名字挂在头顶。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class ScarecrowClient {

// 超过这个距离就不响应（与方块交互距离一致，约 8 格）
    private static final double REACH_SQR = 64.0D;

    private ScarecrowClient() {
    }

    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
// 只认主手：客户端一次点击会把主手、副手依次走一遍，不限定的话界面会开两次
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !event.getEntity().level().isClientSide
                || event.getEntity().isSecondaryUseActive()
                || !event.getItemStack().isEmpty()
                || !(event.getTarget() instanceof Scarecrow)
                || event.getEntity().distanceToSqr(event.getTarget()) > REACH_SQR) {
            return;
        }
        Minecraft.getInstance().setScreen(new AnimalRenameScreen(event.getTarget()));
    }
}
