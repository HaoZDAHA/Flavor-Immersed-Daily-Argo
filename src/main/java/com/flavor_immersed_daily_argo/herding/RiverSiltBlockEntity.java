package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.recipe.ModRecipes;
import com.flavor_immersed_daily_argo.recipe.RiverSiltFarmingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.flavor_immersed_daily_argo.compat.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

// 河道淤泥方块实体
//
// 干燥的淤泥什么都不记；用（水产）饲料点过之后变成含水状态，玩家再拿着苗种右键，
// 这里就记下「苗种物品 + 产物 + 生长耗时 + 当前进度」，并由方块实体渲染器把苗种画在淤泥表面。
//
// 生长计时走方块刻（每 tick 加一），成熟后不再推进，等玩家右键收获。成熟状态与进度都会
// 随方块实体同步给客户端，渲染器据此决定画一份苗种还是三份产物。
public class RiverSiltBlockEntity extends BlockEntity {

    private static final String SEED_TAG = "Seed";
    private static final String RESULT_TAG = "Result";
    private static final String GROWTH_TAG = "Growth";
    private static final String GROW_TIME_TAG = "GrowTime";

// 客户端同步间隔：每秒推一次进度，让渲染缩放跟着长
    private static final int SYNC_INTERVAL = 20;

// 种下的苗种，只留 1 个用于渲染
    private ItemStack seed = ItemStack.EMPTY;

// 成熟后收获的产物（数量即一次收获的份数）
    private ItemStack result = ItemStack.EMPTY;

    private int growth;
    private int growTime;

    public RiverSiltBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIVER_SILT_ENTITY.get(), pos, state);
    }

    public ItemStack getSeed() {
        return this.seed;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public boolean hasCrop() {
        return !this.seed.isEmpty();
    }

    public boolean isMature() {
        return this.hasCrop() && this.growth >= this.growTime;
    }

    public int getGrowTime() {
        return this.growTime;
    }

// 饲料催熟：把生长进度推进一段，到了总耗时就算成熟；返回催熟后是否已经成熟
    public boolean fertilize(int amount) {
        if (!this.hasCrop() || this.isMature()) {
            return false;
        }
        this.growth = Math.min(this.growTime, this.growth + Math.max(1, amount));
        this.setChanged();
        return this.isMature();
    }

// 生长进度 0~1，渲染器用它把苗种从幼苗放到正常大小
    public float growthProgress() {
        if (this.growTime <= 0) {
            return 0.0F;
        }
        return Math.min(1.0F, (float) this.growth / (float) this.growTime);
    }

// 按手上的物品找养殖配方；找不到就说明这东西不能养
    public Optional<RiverSiltFarmingRecipe> findRecipe(ItemStack stack) {
        if (this.level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return this.level.getRecipeManager()
                .getRecipeFor(ModRecipes.RIVER_SILT_FARMING_TYPE.get(), new SingleRecipeInput(stack), this.level);
    }

// 播下苗种：苗种只留 1 个用于渲染，产物与耗时都来自配方 JSON
    public void plant(ItemStack seed, ItemStack result, int growTime) {
        this.seed = seed.copyWithCount(1);
        this.result = result.copy();
        this.growth = 0;
        this.growTime = Math.max(1, growTime);
        this.setChanged();
    }

// 收获：把产物塞进背包，塞不下就掉在脚下，然后清空这份养殖记录
    public void harvest(Player player) {
        ItemStack drop = this.result.isEmpty() ? this.seed.copy() : this.result.copy();
        if (!drop.isEmpty() && !player.getInventory().add(drop)) {
            player.drop(drop, false);
        }
        this.seed = ItemStack.EMPTY;
        this.result = ItemStack.EMPTY;
        this.growth = 0;
        this.growTime = 0;
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RiverSiltBlockEntity silt) {
        if (!silt.hasCrop() || silt.isMature()) {
            return;
        }
        silt.growth++;
        if (silt.growth >= silt.growTime) {
            silt.growth = silt.growTime;
            silt.setChanged();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                        6, 0.3D, 0.1D, 0.3D, 0.0D);
            }
        } else if (silt.growth % SYNC_INTERVAL == 0) {
            silt.setChanged();
        }
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.seed.isEmpty()) {
            tag.put(SEED_TAG, this.seed.save(new CompoundTag()));
        }
        if (!this.result.isEmpty()) {
            tag.put(RESULT_TAG, this.result.save(new CompoundTag()));
        }
        tag.putInt(GROWTH_TAG, this.growth);
        tag.putInt(GROW_TIME_TAG, this.growTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.seed = tag.contains(SEED_TAG)
                ? ItemStack.of(tag.getCompound(SEED_TAG))
                : ItemStack.EMPTY;
        this.result = tag.contains(RESULT_TAG)
                ? ItemStack.of(tag.getCompound(RESULT_TAG))
                : ItemStack.EMPTY;
        this.growth = tag.getInt(GROWTH_TAG);
        this.growTime = tag.getInt(GROW_TIME_TAG);
    }

// 客户端同步

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

// 进度 / 苗种一变就推给客户端；方块实体还没接上世界时（读档途中）不发
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
