package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.economy.DeliveryManager;
import com.flavor_immersed_daily_argo.economy.DeliveryOrder;
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

// 外卖袋：出发点路边放着的那只袋子，身上带着整条订单。
//
// 玩家走过来右键就把它收进背包，变成「打包好的外卖」（TakeawayBagItem），
// 订单信息随之装进物品的数据组件里。
public class TakeawayBag extends Entity {

    private DeliveryOrder order;

    public TakeawayBag(EntityType<? extends TakeawayBag> type, Level level) {
        super(type, level);
    }

    public void setOrder(DeliveryOrder order) {
        this.order = order;
    }

    public DeliveryOrder getOrder() {
        return this.order;
    }

// 必须覆写：Entity#isPickable() 默认 false，不改的话准星选不中，右键传不进来。
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

// 别把袋子当成投掷物的靶子
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

// 订单过期（比如服务器中途重启、调度台不再认识它）就自己消失，免得留一只收不了的袋子
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.order != null
                && this.level().getGameTime() > this.order.deadline()) {
            this.discard();
        }
    }

// 右键捡起：变成一件「打包好的外卖」，订单原样存进去
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (this.order == null) {
            this.discard();
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = new ItemStack(ModItems.TAKEAWAY_BAG.get());
        ModDataComponents.DELIVERY_ORDER.get().set(stack, this.order);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        DeliveryManager.markPickedUp(this.order.id());
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 1.2F);
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
