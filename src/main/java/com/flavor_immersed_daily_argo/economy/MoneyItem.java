package com.flavor_immersed_daily_argo.economy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

// 纸币
//
// 六种纸币（一元 / 五元 / 十元 / 二十元 / 五十元 / 一百元）共用这一个类，
// 面额差别只体现在构造时给的值上（1 / 5 / 10 / 20 / 50 / 100）。
// 这个数值就是它通过自助取款机存入银行卡账户时折算的钱数，tooltip 里直接显示出来。
public class MoneyItem extends Item {

// 面额
    private final int value;

    public MoneyItem(int value, Properties properties) {
        super(properties);
        this.value = value;
    }

// 这张纸币的金融价值（面额）
    public static int valueOf(ItemStack stack) {
        return stack.getItem() instanceof MoneyItem money ? money.value : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.money_value", valueOf(stack)));
    }
}
