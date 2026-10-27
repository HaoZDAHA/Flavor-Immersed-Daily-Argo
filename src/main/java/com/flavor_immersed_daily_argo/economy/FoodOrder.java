package com.flavor_immersed_daily_argo.economy;

import net.minecraft.world.item.ItemStack;

// 「身上带着一张单子」的实体：需要交付的食物与报酬。
// 外卖小哥与顾客都实现它，准星提示因此能共用同一套绘制。
public interface FoodOrder {
    ItemStack getRequiredFood();
    long getReward();
}
