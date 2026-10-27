package com.flavor_immersed_daily_argo.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

// 带 tooltip 的方块物品类
//
// 与 TooltipItem 的区别在于它同时是一个可放置的方块物品：
// 右键地面放置方块，无法放置时按原版食物逻辑进食，
// 因此进食动画、音效与时长全部沿用原版 BlockItem 的实现。
public class TooltipBlockItem extends BlockItem {
    private final String tooltipKey;

    public TooltipBlockItem(Block block, Properties properties, String tooltipKey) {
        super(block, properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(tooltipKey));
    }
}
