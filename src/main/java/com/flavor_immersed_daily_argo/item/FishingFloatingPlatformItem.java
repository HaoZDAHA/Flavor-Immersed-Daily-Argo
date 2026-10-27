package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.entity.FishingFloatingPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

// 充气钓鱼浮台：对着水面长按右键蓄力，拉满松手就在水上撑开一块 FishingFloatingPlatform。
//
// 蓄力走的是原版拉弓那一套（#getUseDuration / #getUseAnimation /
// #releaseUsing）：把总使用时长设得远大于蓄力时长，松手时 releaseUsing 收到
// 的剩余时长一旦不足 #CHARGE_TICKS，就说明已经拉满了。
//
// 为什么不能把「总时长」直接设成蓄力时长：原版把「剩余使用时长归零」当成自然使用完毕，
// 会走 finishUsingItem 而不是 releaseUsing；普通物品的 finishUsingItem
// 什么都不做，于是永远放不出浮台。
public class FishingFloatingPlatformItem extends TooltipItem {

// 拉满所需的蓄力时长（tick）
    private static final int CHARGE_TICKS = 20;
// 一次右键的总时长：留足余量，保证松手时一定回到 releaseUsing（同原版弓）
    private static final int USE_DURATION = 72000;
// 水面不准时的提示 key
    private static final String NO_WATER_KEY = "message.flavor_immersed_daily_argo.fishing_floating_platform.no_water";

    public FishingFloatingPlatformItem(Item.Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

// 兜底：右键空气也能起手蓄力
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

// 兜底：对着方块右键（方块自身没吃下这一下时）同样起手蓄力
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

// 蓄力（拉弓姿势）

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

// 用拉弓的姿势来表达蓄力
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

// 松手：拉满则在水面上撑开浮台，并消耗一个物品
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (USE_DURATION - timeLeft < CHARGE_TICKS) {
            return; // 没拉满就不放
        }
        if (level.isClientSide) {
            return;
        }
        BlockPos surface = findWaterSurface(level, player);
        if (surface == null) {
            player.displayClientMessage(Component.translatable(NO_WATER_KEY), true);
            return;
        }
        colorPlace(level, surface);
        FishingFloatingPlatform platform = ModEntities.FISHING_FLOATING_PLATFORM.get().create(level);
        if (platform == null) {
            return;
        }
        platform.moveTo(surface.getX() + 0.5D, surface.getY() + 1.0D, surface.getZ() + 0.5D,
                player.getYRot(), 0.0F);
        level.addFreshEntity(platform);
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
    }

// 工具方法

// 准星打在哪儿的水面上，返回最顶那一格水方块；没打到水就返回 null
    private static BlockPos findWaterSurface(Level level, Player player) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            return null;
        }
        BlockPos top = pos;
        while (level.getFluidState(top.above()).is(FluidTags.WATER)) {
            top = top.above();
        }
        return top;
    }

// 放下浮台的水花与音效
    private static void colorPlace(Level level, BlockPos surface) {
        level.playSound(null, surface.getX() + 0.5D, surface.getY() + 0.5D, surface.getZ() + 0.5D,
                SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}