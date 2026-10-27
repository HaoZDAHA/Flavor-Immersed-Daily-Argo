package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.economy.DeliveryOrder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

// 「打包好的外卖」：外卖袋被捡起后变成的物品，身上带着整条订单
// （起点 / 终点 xz、截止刻、配送费），拿它到终点的配送点右键即可交付。
public class TakeawayBagItem extends Item {

    public TakeawayBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        DeliveryOrder order = stack.get(ModDataComponents.DELIVERY_ORDER.get());
        if (order == null) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.takeaway_bag_hint")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.takeaway_bag_origin",
                order.originX(), order.originZ()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.takeaway_bag_dest",
                order.destX(), order.destZ()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.takeaway_bag_reward",
                order.money()).withStyle(ChatFormatting.GOLD));
        Level level = context.level();
        if (level != null) {
            tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.takeaway_bag_time",
                    formatTime(order.remaining(level.getGameTime()))).withStyle(ChatFormatting.GRAY));
        }
    }

    private static String formatTime(long ticks) {
        long seconds = Math.max(0L, ticks / 20L);
        return String.format("%d:%02d", seconds / 60L, seconds % 60L);
    }
}
