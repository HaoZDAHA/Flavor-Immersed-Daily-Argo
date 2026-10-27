package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

// 被标记的座位方块被破坏时，顺手把标记一起清掉。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class SeatCushionEvents {
    private SeatCushionEvents() {}

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && SeatManager.get(level).isSeat(event.getPos())) {
            SeatManager.get(level).unmark(event.getPos());
        }
    }
}
