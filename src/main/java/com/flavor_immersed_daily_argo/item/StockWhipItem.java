package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.herding.AnimalStatsManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// 牧鞭：右键一头正在养的动物，抬手抡出一道横扫，把附近的同类都往前赶。
//
// 被赶的只认「有状态栏的同类」——也就是喂过、养着的那些，野生的不管。
// 挥鞭后鞭子会进一小段冷却，免得一刻不停地甩。
public class StockWhipItem extends TooltipItem {

// 多远的同类会被一起赶
    private static final double HERD_RADIUS = 12.0D;
// 赶去的点在玩家身前多远
    private static final double DRIVE_DISTANCE = 6.0D;
// 驱赶时的寻路速度
    private static final double DRIVE_SPEED = 1.0D;
// 重新赶一次之前到点位的水平抖动，免得全叠在一格上
    private static final double SPREAD = 1.5D;
// 挥一次鞭的冷却（tick）
    private static final int COOLDOWN = 30;

    public StockWhipItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
// 只赶正在养的动物
        if (AnimalStatsManager.getStats(target) == null) {
            return InteractionResult.PASS;
        }
        player.swing(hand, true);
        if (!player.level().isClientSide) {
            crack(player, target);
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

// 抡一道横扫，再把附近同类赶往玩家身前
    private static void crack(Player player, LivingEntity target) {
        Level level = player.level();
        Vec3 look = player.getLookAngle();
        level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level instanceof ServerLevel server) {
            Vec3 sweep = player.position().add(look.scale(2.0D)).add(0.0D, player.getBbHeight() * 0.6D, 0.0D);
            server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    sweep.x, sweep.y, sweep.z, 3, 0.4D, 0.2D, 0.4D, 0.0D);
        }

        double destX = player.getX() + look.x * DRIVE_DISTANCE;
        double destZ = player.getZ() + look.z * DRIVE_DISTANCE;
        AABB box = player.getBoundingBox().inflate(HERD_RADIUS);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (other.getType() != target.getType() || AnimalStatsManager.getStats(other) == null) {
                continue;
            }
            if (!(other instanceof Mob mob)) {
                continue;
            }
            double jitterX = (level.random.nextDouble() * 2.0D - 1.0D) * SPREAD;
            double jitterZ = (level.random.nextDouble() * 2.0D - 1.0D) * SPREAD;
            mob.getNavigation().moveTo(destX + jitterX, player.getY(), destZ + jitterZ, DRIVE_SPEED);
        }
    }
}
