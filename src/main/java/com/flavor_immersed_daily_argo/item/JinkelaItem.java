package com.flavor_immersed_daily_argo.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

// 金坷垃：功能与原版骨粉完全一致（可对作物、水生植物等施骨粉，并触发骨粉事件），
// 只是在右键时会以点击位置为中心，对 3×3 范围内的作物各施加 5 次骨粉效果，
// 使用成功后消耗 1 个，并伴随音效与粒子。
public class JinkelaItem extends BoneMealItem {
    private final String tooltipKey;

    public JinkelaItem(Properties properties, String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(tooltipKey));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos center = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        boolean applied = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                for (int i = 0; i < 5; i++) {
// 与原版骨粉相同的施法逻辑（触发骨粉事件、生成绿粒），传入临时骨粉栈以统一控制消耗
                    if (BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, context.getPlayer())) {
                        applied = true;
                    }
                }
            }
        }
        if (applied) {
// 与原版骨粉一致：成功使用后消耗 1 个
            stack.shrink(1);
            if (!level.isClientSide()) {
// 音效：骨粉使用音效，向范围内所有玩家播放
                level.playSound(null, center, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
// 粒子：金色粒子环 + 绿色魔法粒子爆发
                ServerLevel serverLevel = (ServerLevel) level;
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        center.getX() + 0.5, center.getY() + 0.75, center.getZ() + 0.5, 40, 1.6, 0.6, 1.6, 0.05);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }
}
