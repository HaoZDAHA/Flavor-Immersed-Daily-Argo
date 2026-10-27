package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.economy.SeatManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// 座垫标记：手持右键任意方块把它标记为顾客可坐的座位，再次右键取消。
public class SeatCushionItem extends TooltipItem {
    public SeatCushionItem(Item.Properties properties, String tooltipKey) { super(properties, tooltipKey); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) { return InteractionResult.SUCCESS; }
        BlockPos pos = context.getClickedPos();
        boolean marked = SeatManager.get((ServerLevel) level).toggle(pos);
        Player player = context.getPlayer();
        if (player != null) {
            player.displayClientMessage(Component.translatable(
                    marked ? "message.flavor_immersed_daily_argo.seat.marked"
                           : "message.flavor_immersed_daily_argo.seat.unmarked"), true);
        }
        return InteractionResult.sidedSuccess(false);
    }
}
