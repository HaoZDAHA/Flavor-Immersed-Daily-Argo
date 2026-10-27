package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// 杆秤界面：正中一个秤盘，放上去的东西立刻被称一次，下方是玩家背包。
//
// 秤盘上是临时容器，关界面时剩的东西原样还回玩家背包（塞不下就掉在脚下），不会吞物品。
public class SteelyardMenu extends AbstractContainerMenu {

// 界面尺寸，与 SteelyardScreen 保持一致
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 166;
// 玩家背包标题的纵向位置
    public static final int INVENTORY_LABEL_Y = 73;

// 秤盘槽位（界面坐标）
    public static final int PAN_X = 80;
    public static final int PAN_Y = 32;

    private final SimpleContainer pan = new SimpleContainer(1);

    public SteelyardMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }

    public SteelyardMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.STEELYARD_MENU.get(), containerId);
        Player owner = playerInventory.player;

// 秤盘：东西一放上去就称（客户端只接收结果，不再重复抽重量）
        this.addSlot(new Slot(this.pan, 0, PAN_X, PAN_Y) {
            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                if (!owner.level().isClientSide) {
                    SteelyardItem.weigh(this.getItem(), owner);
                    this.setChanged();
                }
            }
        });

// 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

// 秤盘上那件东西：客户端界面据此显示重量
    public ItemStack panItem() {
        return this.pan.getItem(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

// 关界面时把秤盘上的东西还回玩家背包，避免物品被吞
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide) {
            return;
        }
        ItemStack stack = this.pan.getItem(0);
        if (stack.isEmpty()) {
            return;
        }
        this.pan.setItem(0, ItemStack.EMPTY);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
