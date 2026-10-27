package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.entity.ThrownMarshGas;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// 加压沼气。
//
// 平时是燃料（燃烧时长见数据映射 data/neoforge/data_maps/item/furnace_fuels.json）；
// 右键则扔出去（ThrownMarshGas），落地爆开：不炸方块，但会伤到附近的生物。
public class MarshGasItem extends TooltipItem {

    public MarshGasItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ThrownMarshGas gas = new ThrownMarshGas(level, player);
            gas.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, 1.0F);
            level.addFreshEntity(gas);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.6F);
// consume 会自己判断创造模式，不用手工 shrink
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}