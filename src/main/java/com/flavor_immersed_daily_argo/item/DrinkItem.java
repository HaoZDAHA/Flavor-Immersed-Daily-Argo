package com.flavor_immersed_daily_argo.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

// 饮用类食物（果汁等）：食用时走原版「喝」的动作与音效（同药水 / 蜂蜜瓶），
// 而不是普通食物的咀嚼动画。
public class DrinkItem extends Item {

// 一口喝完的时间：与原版药水一致（32 刻）
    private static final int DRINK_DURATION = 32;

    public DrinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return DRINK_DURATION;
    }
}
