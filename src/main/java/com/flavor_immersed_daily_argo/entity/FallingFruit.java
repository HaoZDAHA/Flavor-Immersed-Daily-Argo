package com.flavor_immersed_daily_argo.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// 坠落的果子：悬挂的果子掉下来时生成的实体，照父模组 flavor_immersed_daily 的 FallingFruitEntity 简化而来。
//
// 带着挂在树上时的方块模型从上空落下，落地后停在原地（渲染时带一个随机的翻滚角度），
// 之后可以用手带走；落地超过 5 分钟自动消失。
public class FallingFruit extends Entity {

    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE =
            SynchedEntityData.defineId(FallingFruit.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<ItemStack> DATA_DROP_ITEM =
            SynchedEntityData.defineId(FallingFruit.class, EntityDataSerializers.ITEM_STACK);
// 是否已落地（落地逻辑在服务端 tick 中置位，同步给客户端供渲染层附加旋转翻滚）
    private static final EntityDataAccessor<Boolean> DATA_SETTLED =
            SynchedEntityData.defineId(FallingFruit.class, EntityDataSerializers.BOOLEAN);

// 落地后停留多久自动消失（5 分钟）
    private static final int LIFETIME_TICKS = 6000;

    private int age;

    public FallingFruit(EntityType<?> type, Level level) {
        super(type, level);
    }

// 渲染用的方块状态：直接沿用挂在树上时的悬挂果实方块
    public void setBlockState(BlockState state) {
        this.entityData.set(DATA_BLOCK_STATE, state);
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

// 被捡起来时给玩家的物品
    public void setDropItem(ItemStack stack) {
        this.entityData.set(DATA_DROP_ITEM, stack);
    }

    public ItemStack getDropItem() {
        return this.entityData.get(DATA_DROP_ITEM);
    }

// 是否已落地（渲染层据此附加随机旋转翻滚）
    public boolean isSettled() {
        return this.entityData.get(DATA_SETTLED);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_BLOCK_STATE, Blocks.AIR.defaultBlockState());
        this.entityData.define(DATA_DROP_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_SETTLED, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (!this.entityData.get(DATA_SETTLED)) {
// 下落期间保持完整挂果方块的实体渲染，触地后停留在地面上
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.98, 0.98));
            this.move(MoverType.SELF, this.getDeltaMovement());

            if (this.onGround()) {
                this.entityData.set(DATA_SETTLED, true);
                this.setDeltaMovement(0.0, 0.0, 0.0);
// 落地后随机给一个水平偏移，让果子散落在落点周围，而不是挤在正下方
                this.setPos(this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getY(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.4);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.3F, 1.0F);
            }
        }

        if (++this.age > LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        ItemStack drop = this.getDropItem().copy();
        if (!drop.isEmpty()) {
            ItemStack remaining = drop.copy();
            player.getInventory().add(remaining);
            if (!remaining.isEmpty()) {
                player.drop(remaining, false);
            }
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BlockState")) {
            this.setBlockState(NbtUtils.readBlockState(
                    this.level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState")));
        }
        if (tag.contains("DropItem")) {
            java.util.Optional.of(ItemStack.of(tag.getCompound("DropItem")))
                    .ifPresent(stack -> this.entityData.set(DATA_DROP_ITEM, stack));
        }
        this.age = tag.getInt("Age");
        this.entityData.set(DATA_SETTLED, tag.getBoolean("Settled"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(this.getBlockState()));
        if (!this.getDropItem().isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            this.getDropItem().save(itemTag);
            tag.put("DropItem", itemTag);
        }
        tag.putInt("Age", this.age);
        tag.putBoolean("Settled", this.entityData.get(DATA_SETTLED));
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}
