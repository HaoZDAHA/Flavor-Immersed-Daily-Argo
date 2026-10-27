package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// 收购箱界面：上方 3×9 共 27 个箱格（原版单箱布局），下方是玩家背包，
// 最底下接一条出售栏放「出售」按钮。
public class PurchasingBoxMenu extends AbstractContainerMenu {

// 界面尺寸，与 PurchasingBoxScreen 保持一致：原版 3 行箱子 168 高 + 26 像素出售栏
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 194;

// 玩家背包标题的纵向位置（原版箱子就是这一行，不随出售栏下移）
    public static final int INVENTORY_LABEL_Y = 74;

// 出售栏与出售按钮（相对界面左上角）
    public static final int SELL_BAR_TOP = 167;
    public static final int SELL_BUTTON_Y = 172;
    public static final int SELL_BUTTON_WIDTH = 90;

    private static final int SLOT_SIZE = 18;
    private static final int BOX_LEFT = 8;
    private static final int BOX_TOP = 18;
    private static final int PLAYER_INV_TOP = 84;
    private static final int HOTBAR_Y = 142;

// 玩家离开这个距离界面就关掉
    private static final double MAX_USE_DISTANCE_SQR = 64.0D;

    private final PurchasingBoxBlockEntity box;

// 客户端：按方块坐标找回方块实体（由 openMenu 写进附加数据）
    public PurchasingBoxMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolve(playerInventory, extraData.readBlockPos()));
    }

    public PurchasingBoxMenu(int containerId, Inventory playerInventory, PurchasingBoxBlockEntity box) {
        super(ModMenus.PURCHASING_BOX_MENU.get(), containerId);
        this.box = box;

// 箱格 3×9
        for (int index = 0; index < PurchasingBoxBlockEntity.SLOT_COUNT; index++) {
            int col = index % 9;
            int row = index / 9;
            this.addSlot(new Slot(box.getInventory(), index,
                    BOX_LEFT + col * SLOT_SIZE, BOX_TOP + row * SLOT_SIZE));
        }

// 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * SLOT_SIZE, PLAYER_INV_TOP + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * SLOT_SIZE, HOTBAR_Y));
        }
    }

    private static PurchasingBoxBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof PurchasingBoxBlockEntity box) {
            return box;
        }
        throw new IllegalStateException("找不到收购箱方块实体：" + pos);
    }

// 服务端：把箱内产物按第 day 天的价格卖掉（钱换回纸币放回箱子），并同步槽位给客户端
    public void sell(long day) {
        this.box.sell(day);
        this.broadcastChanges();
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
        if (index < PurchasingBoxBlockEntity.SLOT_COUNT) {
// 从箱子挪回玩家背包
            if (!this.moveItemStackTo(stack, PurchasingBoxBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, PurchasingBoxBlockEntity.SLOT_COUNT, false)) {
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
