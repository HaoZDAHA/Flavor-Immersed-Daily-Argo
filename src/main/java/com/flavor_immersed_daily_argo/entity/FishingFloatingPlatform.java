package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// 充气钓鱼浮台。
//
// 玩家在水面上蓄力放下的一块 5×5 浮台，任何生物都能踩在上面。它像木船一样有碰撞体积
// （#canBeCollidedWith()），但本身没有任何动力：不落、不漂、不随水流走。
//
// 坐标钉死：位移只可能经由 #move 发生，所以这里把 #move 直接覆写成空实现，
// 再把 #isPushable() 关掉，无论是重力、水流、活塞还是别的生物推挤都挪不动它。
//
// 被玩家连续攻击若干次后散掉、掉回物品；其它伤害（箭矢、爆炸、仙人掌……）一概不理会。
public class FishingFloatingPlatform extends Entity {

// 连着挨多少下就散掉
    private static final int MAX_HITS = 3;

// 循环播放的起伏动画
    public final AnimationState swimAnimationState = new AnimationState();

// 已经挨了几下
    private int hits;

    public FishingFloatingPlatform(EntityType<? extends FishingFloatingPlatform> type, Level level) {
        super(type, level);
    }

// 准星能选中（要挨打），但别让箭矢、投掷物把它当成靶子
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

// 像木船一样有碰撞体积，生物可以站在上面
    @Override
    public boolean canBeCollidedWith() {
        return !this.isRemoved();
    }

// 推不动：站稳了就不许挪窝
    @Override
    public boolean isPushable() {
        return false;
    }

// 完全静止：不接受任何位移
    @Override
    public void move(MoverType type, Vec3 movement) {
        this.setDeltaMovement(Vec3.ZERO);
    }

// 外力给的初速度直接丢掉，免得服务端每 tick 都发一次运动包
    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getDeltaMovement().equals(Vec3.ZERO)) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

// 挨打：只有玩家动手才作数，连着挨够 #MAX_HITS 下就散掉并掉回物品。
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return false;
        }
        if (!(source.getEntity() instanceof Player)) {
            return false;
        }
        this.hits++;
        if (this.hits >= MAX_HITS) {
            this.spawnAtLocation(new ItemStack(ModItems.FISHING_FLOATING_PLATFORM.get()));
            this.discard();
        }
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.hits = tag.getInt("Hits");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Hits", this.hits);
    }
}