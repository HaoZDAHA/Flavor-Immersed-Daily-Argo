package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// 除草剂：应付林业网点的「杂草丛生」突发情况。
//
// 右键清除以点击处（或玩家脚下）为中心一片范围内的杂草；若命中区块上的网点正闹杂草，
// 则一并消解掉并加分。
public class HerbicideItem extends TooltipItem {

    public HerbicideItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            spray(serverPlayer, context.getClickedPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            spray(serverPlayer, serverPlayer.blockPosition());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void spray(ServerPlayer player, net.minecraft.core.BlockPos center) {
        int cleared = ForestryNetworkManager.clearWeeds(player, center);
        player.displayClientMessage(Component.translatable(cleared > 0
                ? "message.flavor_immersed_daily_argo.forestry.herbicide.cleared"
                : "message.flavor_immersed_daily_argo.forestry.herbicide.none", cleared), false);
    }
}
