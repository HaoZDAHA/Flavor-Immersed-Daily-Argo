package com.flavor_immersed_daily_argo.entity;


import net.minecraftforge.network.NetworkHooks;
import com.flavor_immersed_daily_argo.all.ModSounds;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.SickleItem;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.List;

public class Harvester extends Mob implements MenuProvider, HasCustomInventoryScreen {

    private static final int INVENTORY_SIZE = 36;
    private static final int HARVEST_INTERVAL = 10;
    private static final int HARVEST_RADIUS = 3;
    private static final int HITS_TO_BREAK = 10;
    private static final int HIT_WINDOW_TICKS = 5;
    private static final float ATTACK_DAMAGE = 8.0F;
    private static final double ATTACK_KNOCKBACK = 1.5D;
    private static final int ATTACK_COOLDOWN = 10;

    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);
    private int harvestCooldown = 0;
    private int hitCount = 0;
    private int lastHitTick = 0;
    private int attackCooldown = 0;

    public Harvester(EntityType<? extends Harvester> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.08D)
                .add(Attributes.MOVEMENT_SPEED, 0.13D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .build();
    }

// 物理防推动

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

// 受击逻辑：无红色效果 + 连击破坏

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) return true;

        if (!(source.getEntity() instanceof Player) && !(source.getDirectEntity() instanceof Player)) {
            return false;
        }

        int currentTick = this.tickCount;
        if (currentTick - lastHitTick > HIT_WINDOW_TICKS) {
            hitCount = 0;
        }
        lastHitTick = currentTick;
        hitCount++;

        if (hitCount >= HITS_TO_BREAK) {
            breakHarvester();
        }

        return true;
    }

    private void breakHarvester() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                spawnAtLocation(stack);
            }
        }
        discard();
    }

// 骑乘

// 直接控制骑乘者Y轴位置，避免1.21.1中getPassengersRidingOffset()废弃导致的高度异常
    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) return;
        double y = this.getY() + (18.0 / 16.0);
        moveFunction.accept(passenger, this.getX(), y, this.getZ());
    }

// 骑乘时按E打开收割机背包
    @Override
    public void openCustomInventoryScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this);
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        List<Entity> passengers = getPassengers();
        if (passengers.isEmpty()) return null;
        Entity entity = passengers.get(0);
        if (entity instanceof Player player) return player;
        return null;
    }

    @Override
    public void travel(Vec3 input) {
        if (isVehicle() && getControllingPassenger() instanceof Player player) {
            float forward = player.zza;

            boolean isMoving = forward != 0;

            if (forward <= 0.0F) {
                forward *= 0.25F;
            }

            this.setSpeed((float) getAttributeValue(Attributes.MOVEMENT_SPEED));

            if (isMoving) {
                this.setYRot(player.getYRot());
            }

            super.travel(new Vec3(0, input.y, forward));
            this.yHeadRot = this.getYRot();
            this.yBodyRot = this.getYRot();
            return;
        }
        float prevYRot = this.getYRot();
        float prevYHeadRot = this.yHeadRot;
        float prevYBodyRot = this.yBodyRot;
        super.travel(Vec3.ZERO);
        this.setYRot(prevYRot);
        this.yRotO = prevYRot;
        this.yHeadRot = prevYHeadRot;
        this.yHeadRotO = prevYHeadRot;
        this.yBodyRot = prevYBodyRot;
        this.yBodyRotO = prevYBodyRot;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.yHeadRot = this.getYRot();
        this.yBodyRot = this.getYRot();
        if (!level().isClientSide && isAlive() && isVehicle()) {
            if (--harvestCooldown <= 0) {
                harvestAndCollect();
                harvestCooldown = HARVEST_INTERVAL;
            }
            LivingEntity rider = getControllingPassenger();
            if (rider instanceof Player player && player.zza > 0) {
                if (--attackCooldown <= 0) {
                    attackForwardEntities(rider);
                    attackCooldown = ATTACK_COOLDOWN;
                }
            }
        }
    }

// 7×7 收割 + 收集

    private void harvestAndCollect() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        BlockPos center = blockPosition();

        for (int x = -HARVEST_RADIUS; x <= HARVEST_RADIUS; x++) {
            for (int z = -HARVEST_RADIUS; z <= HARVEST_RADIUS; z++) {
                for (int y = -1; y <= 1; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level().getBlockState(pos);

                    if (SickleItem.isMatureCrop(state)) {
                        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos,
                                level().getBlockEntity(pos), this, ItemStack.EMPTY);

// 收获后原地补种回幼苗（与本模组镰刀同一套判据，兼容非 CropBlock 的模组作物）
                        level().setBlock(pos, SickleItem.replantedState(state), 3);

                        Item seedItem = state.getBlock().asItem();
                        boolean consumed = false;
                        for (ItemStack drop : drops) {
                            if (!consumed && !drop.isEmpty() && drop.is(seedItem)) {
                                drop.shrink(1);
                                consumed = true;
                            }
                            if (!drop.isEmpty()) {
                                addToInventoryOrDrop(drop);
                            }
                        }
                    }
                }
            }
        }

        AABB collectBox = new AABB(
                center.getX() - HARVEST_RADIUS, center.getY() - 1, center.getZ() - HARVEST_RADIUS,
                center.getX() + HARVEST_RADIUS + 1, center.getY() + 2, center.getZ() + HARVEST_RADIUS + 1
        );
        for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, collectBox)) {
            if (item.isAlive()) {
                ItemStack remainder = addToInventory(item.getItem());
                if (remainder.isEmpty()) {
                    item.discard();
                } else {
                    item.setItem(remainder);
                }
            }
        }
    }

    private void addToInventoryOrDrop(ItemStack stack) {
        ItemStack remainder = addToInventory(stack);
        if (!remainder.isEmpty()) {
            spawnAtLocation(remainder);
        }
    }

    private ItemStack addToInventory(ItemStack stack) {
        for (int i = 0; i < 36; i++) {
            ItemStack slot = inventory.getItem(i);
            if (ItemStack.isSameItemSameTags(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int toAdd = Math.min(space, stack.getCount());
                slot.grow(toAdd);
                inventory.setItem(i, slot);
                stack.shrink(toAdd);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        for (int i = 0; i < 36; i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot.isEmpty()) {
                inventory.setItem(i, stack.copy());
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, this);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        } else if (!isVehicle()) {
            if (!level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", InventoryNbt.save(this.inventory));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        InventoryNbt.load(this.inventory, tag, "Inventory");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.flavor_immersed_daily_argo.harvester");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChestMenu(MenuType.GENERIC_9x4, containerId, playerInventory, inventory, 4);
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean causedByPlayer) {
    }

    private void attackForwardEntities(LivingEntity rider) {
        Vec3 forward = Vec3.directionFromRotation(0, this.getYRot());
        Vec3 center = this.position().add(forward.scale(2.5));
        AABB attackBox = new AABB(
                center.x - 1.5, this.getY() - 0.5, center.z - 1.5,
                center.x + 1.5, this.getY() + 2.5, center.z + 1.5
        );

        boolean hitAny = false;
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, attackBox)) {
            if (target == this || target == rider) continue;

            Vec3 toTarget = target.position().subtract(this.position());
            if (toTarget.dot(forward) > 0) {
                Vec3 knockbackVec = forward.scale(ATTACK_KNOCKBACK).add(0, 0.4, 0);
                target.setDeltaMovement(target.getDeltaMovement().add(knockbackVec));
                target.hurtMarked = true;
                target.hurt(this.damageSources().mobAttack(this), ATTACK_DAMAGE);
                hitAny = true;
            }
        }
// 砍中至少一个生物才播放击打音效，空挥不出声
        if (hitAny) {
            this.playSound(ModSounds.HARVESTER_ATTACK.get(), 1.0F, 1.0F);
        }
    }
}
