package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// 灭火器：右键喷出灭火粒子、扑灭准星前方一片范围内的火，每次消耗 1 点耐久。
//
// 扑灭林业火灾时，对应网点的火灾事件随之消解并加分（见
// ForestryNetworkManager#extinguish）。
public class FireExtinguisherItem extends TooltipItem {

    public FireExtinguisherItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        spray(level, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        spray(context.getLevel(), player, context.getItemInHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    private static void spray(Level level, Player player, ItemStack stack) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ForestryNetworkManager.extinguish(serverPlayer);
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }
}
