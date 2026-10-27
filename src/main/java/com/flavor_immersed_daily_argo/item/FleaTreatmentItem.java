package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModMobEffects;
import com.flavor_immersed_daily_argo.herding.AnimalStats;
import com.flavor_immersed_daily_argo.herding.AnimalStatsManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// 体外驱虫药：右键动物，消耗一支，把清洁度顶满并洗掉「蝇虫叮咬」。
//
// 已经干净又没长虫子的话就不浪费药了，这一下放行给别的交互。
public class FleaTreatmentItem extends TooltipItem {

    public FleaTreatmentItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        AnimalStats stats = AnimalStatsManager.getStats(target);
        if (stats == null) {
            return InteractionResult.PASS;
        }
        boolean bitten = target.hasEffect(ModMobEffects.FLY_BITE.get());
// 又干净又没虫，没必要浪费一支
        if (!bitten && stats.cleanliness() >= AnimalStats.MAX) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide) {
            apply(player, target, stack, bitten);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    private static void apply(Player player, LivingEntity target, ItemStack stack, boolean bitten) {
        Level level = player.level();
        if (bitten) {
            target.removeEffect(ModMobEffects.FLY_BITE.get());
        }
        AnimalStats stats = AnimalStatsManager.getStats(target);
        if (stats != null) {
            AnimalStatsManager.setStats(target, stats.withCleanliness(AnimalStats.MAX));
        }

        level.playSound(null, target.blockPosition(),
                SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6D, target.getZ(),
                    8, 0.3D, 0.3D, 0.3D, 0.01D);
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }
}
