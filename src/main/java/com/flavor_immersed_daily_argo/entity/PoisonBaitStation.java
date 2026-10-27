package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// 毒饵站。
//
// 用毒饵站物品右键稻草人放下，就立在稻草人脚下（稻草人留着不动）。它罩住的这一片地里
// 不会再刷出老鼠 —— 判定由 MouseSpawning 负责：刷怪点落在以它为中心
// #RADIUS 格的范围内就取消。它本身不会损坏。
//
// 和 Scarecrow 一样是最朴素的 Entity：不移动也不参与碰撞，只当个坐标锚点用。
// 潜行右键收回成物品。
public class PoisonBaitStation extends Entity {

// 阻止老鼠生成的作用半径（格）
    public static final double RADIUS = 16.0D;

    public PoisonBaitStation(EntityType<? extends PoisonBaitStation> type, Level level) {
        super(type, level);
// 毒饵站本身是铁打的：不参与任何伤害结算，摆下就一直立着
        this.setInvulnerable(true);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

// 潜行右键收回成物品，直接右键不做事
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResult.SUCCESS;
        }
        if (!this.level().isClientSide) {
            ItemStack stack = new ItemStack(ModItems.POISON_BAIT_STATION.get());
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.4F);
            this.discard();
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

// 没有任何需要同步或存档的状态，下面三个空实现只是满足 Entity 的抽象方法

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
