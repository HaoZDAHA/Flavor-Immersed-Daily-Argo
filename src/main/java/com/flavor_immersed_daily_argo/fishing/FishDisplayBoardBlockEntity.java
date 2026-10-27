package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 钓鱼展示架方块实体：只记挂着的那条水产（物品栈）。
//
// 存的是完整物品栈，因此称过的重量（WEIGHED_WEIGHT 数据组件）一并留着，取下来还是原来那条。
// 物品栈随方块更新同步给客户端，客户端才能照它查出对应实体、画出挂着的鱼。
public class FishDisplayBoardBlockEntity extends BlockEntity {

    private static final String FISH_TAG = "Fish";

    private ItemStack fish = ItemStack.EMPTY;

    public FishDisplayBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_DISPLAY_BOARD_ENTITY.get(), pos, state);
    }

// 挂着的那条水产；空架返回空栈
    public ItemStack displayed() {
        return this.fish;
    }

    public boolean hasFish() {
        return !this.fish.isEmpty();
    }

// 挂上去（会覆盖旧的），旧的那份由调用方取走处理
    public void place(ItemStack stack) {
        this.fish = stack.copyWithCount(1);
        this.setChanged();
    }

// 取下来并清空，返回取走的那份；空架返回空栈
    public ItemStack take() {
        ItemStack taken = this.fish;
        this.fish = ItemStack.EMPTY;
        this.setChanged();
        return taken;
    }

// 存档与同步

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.fish.isEmpty()) {
            tag.put(FISH_TAG, this.fish.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fish = tag.contains(FISH_TAG)
                ? ItemStack.parse(registries, tag.getCompound(FISH_TAG)).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }

// 挂着的那条随区块下发，客户端才能画出实体模型
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

// 挂上 / 取下都推一次方块更新给客户端
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
