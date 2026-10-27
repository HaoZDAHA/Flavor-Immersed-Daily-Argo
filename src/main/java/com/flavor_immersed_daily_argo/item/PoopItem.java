package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.entity.ThrownPoop;
import com.flavor_immersed_daily_argo.herding.SepticTankBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// 粪便物品。
//
// 平时是食物：右键就能吃（属性见 ModItems.POOP_FOOD）。
//
// 潜行右键则改为投掷：扔出一枚以粪便贴图飞出去的弹射物（ThrownPoop），砸到玩家就给 ta 糊一屏。
public class PoopItem extends TooltipItem {

    public PoopItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide) {
                ThrownPoop poop = new ThrownPoop(level, player);
                poop.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, 1.0F);
                level.addFreshEntity(poop);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.6F);
// consume 会自己判断创造模式，不用手工 shrink
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

// 潜行右键方块时原版会跳过方块的 useItemOn，直接走到这里。
//
// 所以化粪桶要在这里再接一次：否则玩家怕把粪吃掉而潜行右键，反而一坨都倒不进去。
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (SepticTankBlock.insertPoop(level, pos, player.getItemInHand(context.getHand()))) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(context);
    }
}
