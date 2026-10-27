package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// 钓鱼箱方块实体：36 格容器，收岸边钓获用。
//
// 玩家没拿抄网时钓上来的东西会被塞进附近的箱子里（见 ModFishingHook#giveCatch），
// 空手右键打开界面取用。
public class FishingBoxBlockEntity extends BlockEntity implements MenuProvider {

// 箱格数量：4 行 × 9 列
    public static final int SLOT_COUNT = 36;

    private static final String INVENTORY_TAG = "Inventory";

// 箱格内容；改动后写回方块实体，存档才能拿走东西
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            FishingBoxBlockEntity.this.setChanged();
        }
    };

    public FishingBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISHING_BOX_ENTITY.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

// 把一件钓获塞进箱子；没有空位返回 false（调用方改走掉落）
    public boolean add(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (this.inventory.getItem(i).isEmpty()) {
                this.inventory.setItem(i, stack.copy());
                return true;
            }
        }
        return false;
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(INVENTORY_TAG, InventoryNbt.save(this.inventory, registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        InventoryNbt.load(this.inventory, tag, INVENTORY_TAG, registries);
    }

// 界面

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.flavor_immersed_daily_argo.fishing_box");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FishingBoxMenu(containerId, playerInventory, this);
    }
}
