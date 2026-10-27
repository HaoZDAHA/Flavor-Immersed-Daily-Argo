package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.entity.PoisonBaitStation;
import com.flavor_immersed_daily_argo.entity.Scarecrow;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// 毒饵站（手持物品）：对着稻草人右键，就在稻草人脚下立起一座 PoisonBaitStation
// （稻草人留着不动，两者叠在同一格），手上的毒饵站消耗一个。
//
// 目标是实体而不是方块，所以走 PlayerInteractEvent.EntityInteract：在稻草人自己的右键逻辑之前
// 接管并取消事件。潜行右键仍然留给稻草人自己（那是「把稻草人拔回来」），这里不抢。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public class PoisonBaitStationItem extends TooltipItem {

    public PoisonBaitStationItem(Item.Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
// 客户端一次点击会把主手、副手各走一遍，只认主手免得放两座
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getTarget() instanceof Scarecrow scarecrow)
                || !(event.getItemStack().getItem() instanceof PoisonBaitStationItem)
                || event.getEntity().isSecondaryUseActive()) {
            return;
        }
        Level level = event.getLevel();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));

        if (level instanceof ServerLevel serverLevel) {
            PoisonBaitStation station = new PoisonBaitStation(ModEntities.POISON_BAIT_STATION.get(), serverLevel);
// 落在稻草人脚下：两者共用同一格坐标，稻草人保留
            station.moveTo(scarecrow.getX(), scarecrow.getY(), scarecrow.getZ(), scarecrow.getYRot(), 0.0F);
            serverLevel.addFreshEntity(station);
            serverLevel.playSound(null, station.getX(), station.getY(), station.getZ(),
                    SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
            event.getItemStack().shrink(1);
        }
    }
}
