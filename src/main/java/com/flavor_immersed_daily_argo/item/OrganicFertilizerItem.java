package com.flavor_immersed_daily_argo.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

// 有机肥料：化粪发酵桶里发酵出来的产物。
//
// 作用与使用方式完全等同于原版骨粉——直接沿用 BoneMealItem 的右键逻辑
// （催熟作物、蔓延草方块、水中长出海草等），只是图标与提示不同。
public class OrganicFertilizerItem extends BoneMealItem {

    private final String tooltipKey;

    public OrganicFertilizerItem(Properties properties, String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(tooltipKey));
    }
}
