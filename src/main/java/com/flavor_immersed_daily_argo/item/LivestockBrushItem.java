package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.herding.AnimalStatsManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

// 刷毛梳子：右键家畜 / 家禽 / 猫狗，做出和原版刷子一样的刷毛动作，
// 慢慢把动物的心情值和清洁度刷上去。
//
// 「慢慢」是真的慢：同一只动物每 #BRUSH_INTERVAL 刻才长一点，
// 刷太快只是空挥。上一条刷的时间记在动物的持久化数据里，读档也不会忘。
public class LivestockBrushItem extends TooltipItem {

// 同一只动物两次见效之间至少隔多久（20 秒）
    private static final int BRUSH_INTERVAL = 400;
// 上次刷到这只动物是什么时候（存动物自身的持久化数据）
    private static final String LAST_BRUSH_TAG = "FlavorImmersedLastBrush";

// 一次刷毛动作的时长：跟原版刷子一样是十来刻
    private static final int BRUSH_ANIMATION_TICKS = 20;

    public LivestockBrushItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
// 只刷有状态栏的动物（也就是被喂过、正在养的）
        if (AnimalStatsManager.getStats(target) == null) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide) {
            brush(player, target);
        }
// 端起刷子的动作，跟原版刷子同一套
        player.startUsingItem(hand);
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

// 刷毛的姿势
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return BRUSH_ANIMATION_TICKS;
    }

    private static void brush(Player player, LivingEntity target) {
        Level level = player.level();
        level.playSound(null, target.blockPosition(),
                SoundEvents.BRUSH_GENERIC, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6D, target.getZ(),
                    4, 0.3D, 0.3D, 0.3D, 0.0D);
        }

        CompoundTag data = target.getPersistentData();
        long now = level.getGameTime();
        if (now - data.getLong(LAST_BRUSH_TAG) < BRUSH_INTERVAL) {
            return;
        }
        data.putLong(LAST_BRUSH_TAG, now);
        AnimalStatsManager.onBrushed(target);
    }
}
