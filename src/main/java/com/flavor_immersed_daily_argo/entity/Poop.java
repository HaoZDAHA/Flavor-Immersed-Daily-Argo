package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
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

// 堆在地上的粪便实体。
//
// 动物饱食度消耗时拉下的一坨，出生后就在原地一动不动，玩家右键即可收回成 poop 物品。
// 想把它扔出去时会生成另一件投掷物 ThrownPoop，跟这个实体没有关系。
public class Poop extends Entity {

    public Poop(EntityType<? extends Poop> type, Level level) {
        super(type, level);
    }

// 在指定位置放一坨静静躺着的粪便
    public static Poop placed(Level level, double x, double y, double z) {
        Poop poop = new Poop(ModEntities.POOP.get(), level);
        poop.setPos(x, y, z);
        return poop;
    }

// 必须覆写：Entity#isPickable() 默认为 false（原版掉落物就是这样，准星指不到），
// 不改成 true 的话右键事件根本传不到 #interact。
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

// 准星能选中（右键拾取要用），但别让箭矢、投掷物把地上的粪堆当成靶子 ——
// Entity#canBeHitByProjectile() 默认就是 isAlive() && isPickable()，
// 不拦住的话上面那个覆写会顺带把这一条也改成 true。
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = new ItemStack(ModItems.POOP.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.4F);
        this.discard();
        return InteractionResult.SUCCESS;
    }

// 这里没有任何需要同步或存档的状态，下面三个空实现只是满足 Entity 的抽象方法

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
