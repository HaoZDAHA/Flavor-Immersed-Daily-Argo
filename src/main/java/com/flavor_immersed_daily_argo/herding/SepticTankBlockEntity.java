package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.entity.Poop;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

// 化粪发酵桶方块实体
//
// 两件事，都只在服务端做：
//
// 收集——隔 2 秒把周围 8 格内的粪堆Poop吸进来，收进「左半」的粪便槽；
// 发酵——隔 10 秒结算一次，攒着的粪便（或玩家手动放进来的高蛋白聚合物）
// 有概率变成一份有机肥料，放进「右半」的肥料槽。
//
// 槽位分两段：[0, POOP_SLOTS) 是粪便，[POOP_SLOTS, SLOT_COUNT) 是有机肥料。
// 左半的右键只碰前一段，右半只碰后一段。
public class SepticTankBlockEntity extends BlockEntity {

// 左半：粪便的槽位数
    public static final int POOP_SLOTS = 6;
// 右半：有机肥料的槽位数
    public static final int FERTILIZER_SLOTS = 6;
// 槽位总数
    public static final int SLOT_COUNT = POOP_SLOTS + FERTILIZER_SLOTS;

// 肥料段的起始下标
    private static final int FERTILIZER_FROM = POOP_SLOTS;

    private static final String INVENTORY_TAG = "Inventory";

// 收集间隔（2 秒）与半径（8 格）
    private static final int COLLECT_INTERVAL = 40;
    private static final double COLLECT_RADIUS = 8.0D;

// 发酵结算间隔（10 秒）与每次结算成功发酵的概率
    private static final int FERMENT_INTERVAL = 200;
    private static final float FERMENT_CHANCE = 0.35F;

// 两半的槽位。内容变了要写回方块实体，存档才能拿走里面的东西
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            SepticTankBlockEntity.this.setChanged();
        }
    };

    private int collectCooldown;
    private int fermentCooldown;

    public SepticTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEPTIC_TANK_ENTITY.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SepticTankBlockEntity tank) {
        if (--tank.collectCooldown <= 0) {
            tank.collectCooldown = COLLECT_INTERVAL;
            tank.collectNearbyPoop();
        }
        if (--tank.fermentCooldown <= 0) {
            tank.fermentCooldown = FERMENT_INTERVAL;
            tank.ferment();
        }
    }

// 与玩家交互

// 把手里的一摞粪倒进左半；塞不下的仍留在手上
    public void insertPoop(ItemStack hand) {
        if (hand.isEmpty()) {
            return;
        }
        ItemStack remainder = this.insert(0, POOP_SLOTS, hand.copy());
        hand.setCount(remainder.getCount());
        this.inventory.setChanged();
    }

// 空手右键：左半吐一摞粪便，右半吐一摞有机肥料
    public void extractTo(Player player, boolean poopSide) {
        int slot = this.firstNonEmpty(poopSide ? 0 : FERTILIZER_FROM,
                poopSide ? POOP_SLOTS : SLOT_COUNT);
        if (slot < 0) {
            return;
        }
        ItemStack taken = this.inventory.getItem(slot).copy();
        this.inventory.setItem(slot, ItemStack.EMPTY);
        if (!player.getInventory().add(taken)) {
            player.drop(taken, false);
        }
        this.inventory.setChanged();
    }

// 左半粪便槽里的粪便总数，准星提示用
    public int countPoop() {
        return this.count(0, POOP_SLOTS);
    }

// 右半肥料槽里的肥料总数，准星提示用
    public int countFertilizer() {
        return this.count(FERTILIZER_FROM, SLOT_COUNT);
    }

// 收集与发酵

// 把附近的粪堆整堆吸进左半：只有完整塞进去了才让那一坨消失
    private void collectNearbyPoop() {
        if (this.level == null) {
            return;
        }
        AABB box = new AABB(this.worldPosition).inflate(COLLECT_RADIUS);
        for (Poop poop : this.level.getEntitiesOfClass(Poop.class, box)) {
            if (!poop.isAlive() || poop.isRemoved()) {
                continue;
            }
            ItemStack remainder = this.insert(0, POOP_SLOTS, new ItemStack(ModItems.POOP.get()));
            if (remainder.isEmpty()) {
                poop.discard();
            }
        }
    }

// 攒着的粪便 / 高蛋白聚合物有概率发酵成有机肥料。
//
// 一次结算最多转化一坨；肥料那半塞不下时就先不发酵，免得白白吞掉一坨粪。
// 发酵成功时冒几缕白烟并响一声，方便玩家察觉。
    private void ferment() {
        if (this.level == null || this.level.random.nextFloat() >= FERMENT_CHANCE) {
            return;
        }
        int poopSlot = this.firstNonEmpty(0, POOP_SLOTS);
        if (poopSlot < 0) {
            return;
        }
        ItemStack remainder = this.insert(FERTILIZER_FROM, SLOT_COUNT,
                new ItemStack(ModItems.ORGANIC_FERTILIZER.get()));
        if (!remainder.isEmpty()) {
            return;
        }
        this.inventory.getItem(poopSlot).shrink(1);
        this.inventory.setChanged();

        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 1.05D,
                    this.worldPosition.getZ() + 0.5D,
                    3, 0.35D, 0.1D, 0.35D, 0.005D);
        }
        this.level.playSound(null, this.worldPosition, SoundEvents.COMPOSTER_READY,
                SoundSource.BLOCKS, 0.6F, 0.8F);

// 顺手给上方的沼气罐塞一份加压沼气（没放罐子就当这口气白跑了）
        if (this.level.getBlockEntity(this.worldPosition.above()) instanceof MethaneTankBlockEntity tank) {
            tank.addGas(1);
        }
    }

// 槽位工具

// 在下标 [from, to) 里先把同类并进已有堆，再找空槽；返回塞不下的部分
    private ItemStack insert(int from, int to, ItemStack stack) {
        for (int i = from; i < to; i++) {
            ItemStack slot = this.inventory.getItem(i);
            if (!ItemStack.isSameItemSameTags(slot, stack)) {
                continue;
            }
            int space = slot.getMaxStackSize() - slot.getCount();
            if (space <= 0) {
                continue;
            }
            int moved = Math.min(space, stack.getCount());
            slot.grow(moved);
            this.inventory.setChanged();
            stack.shrink(moved);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        for (int i = from; i < to; i++) {
            if (!this.inventory.getItem(i).isEmpty()) {
                continue;
            }
            this.inventory.setItem(i, stack.copy());
            return ItemStack.EMPTY;
        }
        return stack;
    }

// 下标 [from, to) 里第一个非空槽，没有就返回 -1
    private int firstNonEmpty(int from, int to) {
        for (int i = from; i < to; i++) {
            if (!this.inventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

// 下标 [from, to) 里所有槽的物品总数
    private int count(int from, int to) {
        int total = 0;
        for (int i = from; i < to; i++) {
            total += this.inventory.getItem(i).getCount();
        }
        return total;
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INVENTORY_TAG, InventoryNbt.save(this.inventory));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        InventoryNbt.load(this.inventory, tag, INVENTORY_TAG);
    }

// 客户端同步

// 内容随区块一起下发，客户端拿到才能显示数量
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

// 内容一变就推给客户端；方块实体还没接上世界时（读档途中）不发
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
