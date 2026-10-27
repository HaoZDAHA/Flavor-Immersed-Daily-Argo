package com.flavor_immersed_daily_argo.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.EffectCures;

// 瓶装奶：走「喝」的动作，喝完把空玻璃瓶还给玩家。
//
// curesEffects 为 true 时（新鲜羊奶 / 新鲜骆驼奶）与原版牛奶同效：
// 喝下后洗掉身上所有可被牛奶解除的效果；野生狗奶只提供自己的效果，不做清除。
public class MilkDrinkItem extends DrinkItem {

// 是否像原版牛奶一样清除效果
    private final boolean curesEffects;

    public MilkDrinkItem(Properties properties, boolean curesEffects) {
        super(properties);
        this.curesEffects = curesEffects;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
// 先按普通食物吃下：加饥饿值 / 饱食度、结算食物自带效果，并消耗掉一瓶
        super.finishUsingItem(stack, level, entity);
        if (curesEffects && !level.isClientSide) {
            entity.removeEffectsCuredBy(EffectCures.MILK);
        }
// 喝完还一只空玻璃瓶（同原版蜂蜜瓶），创造模式不返还
        if (entity instanceof Player player && !player.hasInfiniteMaterials()) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        }
        return stack;
    }
}
