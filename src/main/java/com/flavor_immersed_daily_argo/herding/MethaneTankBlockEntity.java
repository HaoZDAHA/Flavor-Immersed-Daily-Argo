package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 沼气罐方块实体
//
// 只记一个数：罐里攒了多少份「加压沼气」。正下方的化粪发酵桶每成功发酵一次就往这里加一份
// （见 SepticTankBlockEntity#ferment），玩家空手右键可以整摞取出来。
//
// 数量要显示在准星提示里，所以这里把 Gas 一起同步给客户端（#getUpdateTag），
// 数量一变就用 #setChanged 发一次方块更新。
public class MethaneTankBlockEntity extends BlockEntity {

// 一罐最多装多少份加压沼气
    public static final int MAX_GAS = 64;

    private static final String GAS_TAG = "Gas";

    private int gas;

    public MethaneTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.METHANE_TANK_ENTITY.get(), pos, state);
    }

// 罐里现存的份数
    public int gas() {
        return this.gas;
    }

// 存进若干份，装满为止
    public void addGas(int amount) {
        this.gas = Math.min(MAX_GAS, this.gas + amount);
        this.setChanged();
    }

// 取走 min(现存, 上限) 份，返回实际取走的数量
    public int take(int limit) {
        int taken = Math.min(this.gas, limit);
        if (taken > 0) {
            this.gas -= taken;
            this.setChanged();
        }
        return taken;
    }

// 存档与同步

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(GAS_TAG, this.gas);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.gas = tag.getInt(GAS_TAG);
    }

// 内容随区块下发，客户端拿到才能显示数量
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

// 数量一变就推给客户端；方块实体还没接上世界时（读档途中）不发
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}