package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// 钓鱼箱界面：上方 4×9 共 36 个箱格，下方是玩家背包。
public class FishingBoxMenu extends AbstractContainerMenu {

// 界面尺寸，与 FishingBoxScreen 保持一致
    public static final int IMAGE_WIDTH = 208;
// 比标准箱子多一行（4 行箱格）
    public static final int IMAGE_HEIGHT = 184;

// 玩家背包标题的位置
    public static final int INVENTORY_LABEL_X = 24;
    public static final int INVENTORY_LABEL_Y = 90;

    private static final int SLOT_SIZE = 18;
    private static final int BOX_LEFT = 24;
    private static final int BOX_TOP = 18;
    private static final int PLAYER_INV_LEFT = 24;
    private static final int PLAYER_INV_TOP = 102;
    private static final int HOTBAR_Y = 160;

// 玩家离开这个距离界面就关掉
    private static final double MAX_USE_DISTANCE_SQR = 64.0D;

    private final FishingBoxBlockEntity box;

// 客户端：按方块坐标找回方块实体（由 openMenu 写进附加数据）
    public FishingBoxMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolve(playerInventory, extraData.readBlockPos()));
    }

    public FishingBoxMenu(int containerId, Inventory playerInventory, FishingBoxBlockEntity box) {
        super(ModMenus.FISHING_BOX_MENU.get(), containerId);
        this.box = box;

// 箱格 4×9
        for (int index = 0; index < FishingBoxBlockEntity.SLOT_COUNT; index++) {
            int col = index % 9;
            int row = index / 9;
            this.addSlot(new Slot(box.getInventory(), index,
                    BOX_LEFT + col * SLOT_SIZE, BOX_TOP + row * SLOT_SIZE));
        }

// 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_LEFT + col * SLOT_SIZE, PLAYER_INV_TOP + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_LEFT + col * SLOT_SIZE, HOTBAR_Y));
        }
    }

    private static FishingBoxBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof FishingBoxBlockEntity box) {
            return box;
        }
        throw new IllegalStateException("找不到钓鱼箱方块实体：" + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < FishingBoxBlockEntity.SLOT_COUNT) {
// 从箱子挪回玩家背包
            if (!this.moveItemStackTo(stack, FishingBoxBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, FishingBoxBlockEntity.SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.box.getLevel() != null
                && this.box.getLevel().getBlockEntity(this.box.getBlockPos()) == this.box
                && player.distanceToSqr(Vec3.atCenterOf(this.box.getBlockPos())) <= MAX_USE_DISTANCE_SQR;
    }
}
