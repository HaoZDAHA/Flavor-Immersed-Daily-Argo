package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// 小票打印机界面：8×5 共 40 个槽位，只展示今日菜单。
//
// 这 40 个槽全部只读——不能放入、不能取出，下面接玩家背包。展示内容由服务端灌好后随菜单同步过来。
public class ReceiptPrinterMenu extends AbstractContainerMenu {

// 界面尺寸，与 ReceiptPrinterScreen 保持一致
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 226;

// 玩家背包标题的纵向位置
    public static final int INVENTORY_LABEL_Y = 134;

// 菜单网格：8 列 5 行
    public static final int GRID_COLUMNS = 8;
    public static final int GRID_ROWS = ReceiptPrinterBlockEntity.SLOT_COUNT / GRID_COLUMNS;
    public static final int GRID_X = 16;
    public static final int GRID_Y = 20;
    private static final int SLOT_SIZE = 18;

// 菜单底板的边框（相对界面左上角），把 40 个展示槽圈起来
    public static final int[] GRID_ZONE = {
            GRID_X - 5, GRID_Y - 5,
            GRID_COLUMNS * SLOT_SIZE + 8, GRID_ROWS * SLOT_SIZE + 8};

    private static final int PLAYER_INV_TOP = 146;
    private static final int HOTBAR_Y = 204;

// 玩家离开这个距离界面就关掉
    private static final double MAX_USE_DISTANCE_SQR = 64.0D;

// 界面按钮：拨动出单开关
    public static final int BUTTON_TOGGLE = 0;

    private final ReceiptPrinterBlockEntity printer;

// 客户端：按方块坐标找回方块实体（由 openMenu 写进附加数据）
    public ReceiptPrinterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolve(playerInventory, extraData.readBlockPos()));
    }

    public ReceiptPrinterMenu(int containerId, Inventory playerInventory, ReceiptPrinterBlockEntity printer) {
        super(ModMenus.RECEIPT_PRINTER_MENU.get(), containerId);
        this.printer = printer;

// 今日菜单：8 列 5 行，全部只读
        for (int index = 0; index < ReceiptPrinterBlockEntity.SLOT_COUNT; index++) {
            int col = index % GRID_COLUMNS;
            int row = index / GRID_COLUMNS;
            this.addSlot(new ReadOnlySlot(printer.getInventory(), index,
                    GRID_X + col * SLOT_SIZE, GRID_Y + row * SLOT_SIZE));
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

// 出单开关读数（0 = 关闭，1 = 打开），界面里那个开关按它显示
        this.addDataSlots(printer.getData());
    }

    private static ReceiptPrinterBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof ReceiptPrinterBlockEntity printer) {
            return printer;
        }
        throw new IllegalStateException("找不到小票打印机方块实体：" + pos);
    }

// 出单开关当前是不是打开的
    public boolean isEnabled() {
        return this.printer.getData().get(0) != 0;
    }

// 界面按钮：拨动出单开关，服务端写入后立刻同步回界面
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_TOGGLE) {
            return false;
        }
        this.printer.setEnabled(!this.printer.isEnabled());
        this.broadcastChanges();
        return true;
    }

// 展示槽什么都动不了
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.printer.getLevel() != null
                && this.printer.getLevel().getBlockEntity(this.printer.getBlockPos()) == this.printer
                && player.distanceToSqr(Vec3.atCenterOf(this.printer.getBlockPos())) <= MAX_USE_DISTANCE_SQR;
    }

// 只展示的槽位：既放不进去也拿不出来
    private static final class ReadOnlySlot extends Slot {

        private ReadOnlySlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean isActive() {
            return true;
        }
    }
}
