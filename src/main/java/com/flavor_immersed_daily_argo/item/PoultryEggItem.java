package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.entity.ThrownPoultryEgg;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// 禽蛋（鸭蛋 / 鹅蛋 / 鸽子蛋 / 鹌鹑蛋 / 火鸡蛋）。
//
// 机制与原版鸡蛋完全相同：右键掷出一枚 ThrownPoultryEgg，落地后有小概率孵出幼崽；
// 唯一的区别是把「鸡」换成蛋所对应的禽类——孵出什么由蛋的注册名推导
// （见 herding.CoopEggs），所以这五个蛋共用一个物品类。
public class PoultryEggItem extends Item {

    public PoultryEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ThrownPoultryEgg egg = new ThrownPoultryEgg(level, player);
            egg.setItem(stack);
            egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(egg);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
