package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.economy.DeliveryManager;
import com.flavor_immersed_daily_argo.economy.DeliveryOrder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// 配送点：目的地地表上站着的一个不动的收件人（客户端渲染成村民的样子）。
//
// 玩家手里拿着「打包好的外卖」（TakeawayBagItem）右键它即可交付：
// 校验订单编号对得上、还没超时，就把配送费按面额掉在脚下，订单结束。
public class DeliveryPoint extends Entity {

    private DeliveryOrder order;

    public DeliveryPoint(EntityType<? extends DeliveryPoint> type, Level level) {
        super(type, level);
    }

    public void setOrder(DeliveryOrder order) {
        this.order = order;
    }

    public DeliveryOrder getOrder() {
        return this.order;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

// 站桩不动：不落、不漂，但订单过期就自己消失
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.order != null
                && this.level().getGameTime() > this.order.deadline()) {
            this.discard();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.order == null) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof TakeawayBagItem)) {
            return InteractionResult.PASS;
        }
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        DeliveryOrder carried = ModDataComponents.DELIVERY_ORDER.get().get(held);
        if (carried == null || carried.id() != this.order.id()) {
            return InteractionResult.PASS;
        }
        if (this.level().getGameTime() > this.order.deadline()) {
            player.displayClientMessage(
                    Component.translatable("message.flavor_immersed_daily_argo.delivery.expired"), true);
            this.discard();
            return InteractionResult.SUCCESS;
        }

        ServerLevel level = (ServerLevel) this.level();
        DeliveryManager.spawnMoney(level, this.position(), this.order.money());
        DeliveryManager.complete(level, this.order.id());
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                this.getX(), this.getY() + 1.5D, this.getZ(), 8, 0.4D, 0.4D, 0.4D, 0.0D);
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.delivery.done", this.order.money()), true);
        this.discard();
        return InteractionResult.SUCCESS;
    }

// 存读

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Order", CompoundTag.TAG_COMPOUND)) {
            this.order = DeliveryOrder.fromTag(tag.getCompound("Order"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.order != null) {
            tag.put("Order", this.order.toTag());
        }
    }
}
