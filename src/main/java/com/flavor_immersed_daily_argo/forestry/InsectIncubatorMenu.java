package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.all.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// 昆虫培育箱界面
//
// 分成四个相对独立的分区，各自围着标签排布：饲料（1 格，每次转化消耗一份）、
// 父代（2 格）、载体（1 格，不消耗）、子代（6 格，放入要培育的昆虫，进度跑满后就地转化），
// 下方是玩家背包。
public class InsectIncubatorMenu extends AbstractContainerMenu {

// 界面尺寸，与 InsectIncubatorScreen 保持一致
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 166;

// 玩家背包标题的纵向位置
    public static final int INVENTORY_LABEL_Y = 73;

    private static final int SLOT_SIZE = 18;

// 饲料分区：单个槽
    public static final int FEED_SLOT_X = 26;
    public static final int FEED_SLOT_Y = 30;
// 载体分区：单个槽（在饲料下方）
    public static final int CARRIER_SLOT_X = 26;
    public static final int CARRIER_SLOT_Y = 58;
// 父代分区：两个槽竖排
    public static final int PARENT_SLOT_X = 62;
    public static final int PARENT_SLOT_TOP = 30;
// 子代分区：3×2 六个槽
    public static final int OFFSPRING_SLOT_X = 108;
    public static final int OFFSPRING_SLOT_TOP = 26;

// 四个分区的底色框（相对界面左上角），让分区一眼可辨
    public static final int[] FEED_ZONE = {22, 26, 26, 26};
    public static final int[] CARRIER_ZONE = {22, 54, 26, 26};
    public static final int[] PARENT_ZONE = {58, 26, 26, 44};
    public static final int[] OFFSPRING_ZONE = {104, 22, 62, 44};

// 分区标签位置（各自分区的列中心）
    public static final int FEED_LABEL_X = 35;
    public static final int FEED_LABEL_Y = 18;
    public static final int CARRIER_LABEL_X = 35;
    public static final int CARRIER_LABEL_Y = 46;
    public static final int PARENT_LABEL_X = 71;
    public static final int PARENT_LABEL_Y = 18;
    public static final int OFFSPRING_LABEL_X = 135;
    public static final int OFFSPRING_LABEL_Y = 14;

// 进度条（夹在父代与子代之间）
    public static final int PROGRESS_X = 88;
    public static final int PROGRESS_Y = 44;
    public static final int PROGRESS_WIDTH = 16;
    public static final int PROGRESS_HEIGHT = 8;

    private static final int PLAYER_INV_TOP = 84;
    private static final int HOTBAR_Y = 142;

// 玩家离开这个距离界面就关掉
    private static final double MAX_USE_DISTANCE_SQR = 64.0D;

    private final InsectIncubatorBlockEntity incubator;

// 客户端：按方块坐标找回方块实体（由 openMenu 写进附加数据）
    public InsectIncubatorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolve(playerInventory, extraData.readBlockPos()));
    }

    public InsectIncubatorMenu(int containerId, Inventory playerInventory, InsectIncubatorBlockEntity incubator) {
        super(ModMenus.INSECT_INCUBATOR_MENU.get(), containerId);
        this.incubator = incubator;

// 饲料
        this.addSlot(new Slot(incubator.getInventory(), InsectIncubatorBlockEntity.FEED_SLOT,
                FEED_SLOT_X, FEED_SLOT_Y));
// 父代：两个槽竖排
        this.addSlot(new Slot(incubator.getInventory(), InsectIncubatorBlockEntity.PARENT_SLOT_1,
                PARENT_SLOT_X, PARENT_SLOT_TOP));
        this.addSlot(new Slot(incubator.getInventory(), InsectIncubatorBlockEntity.PARENT_SLOT_2,
                PARENT_SLOT_X, PARENT_SLOT_TOP + SLOT_SIZE));
// 载体
        this.addSlot(new Slot(incubator.getInventory(), InsectIncubatorBlockEntity.CARRIER_SLOT,
                CARRIER_SLOT_X, CARRIER_SLOT_Y));
// 子代 3×2：不再是只出不进的产出槽，全程既能放也能取
        for (int index = 0; index < InsectIncubatorBlockEntity.OFFSPRING_SLOTS; index++) {
            int col = index % 3;
            int row = index / 3;
            this.addSlot(new Slot(incubator.getInventory(), InsectIncubatorBlockEntity.OFFSPRING_START + index,
                    OFFSPRING_SLOT_X + col * SLOT_SIZE, OFFSPRING_SLOT_TOP + row * SLOT_SIZE));
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

// 进度同步：0 = 当前进度，1 = 本次转化总时长
        this.addDataSlots(incubator.getData());
    }

// 当前培育进度（0 ~ #getMaxProgress()）
    public int getProgress() {
        return this.incubator.getData().get(0);
    }

// 本次转化总时长；为 0 表示当前没有可匹配的配方
    public int getMaxProgress() {
        return this.incubator.getData().get(1);
    }

    private static InsectIncubatorBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof InsectIncubatorBlockEntity incubator) {
            return incubator;
        }
        throw new IllegalStateException("找不到昆虫培育箱方块实体：" + pos);
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
        if (index < InsectIncubatorBlockEntity.SLOT_COUNT) {
// 从培育箱挪回玩家背包
            if (!this.moveItemStackTo(stack, InsectIncubatorBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, InsectIncubatorBlockEntity.SLOT_COUNT, false)) {
// 四个分区都收东西，按槽位顺序依次尝试
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
        return this.incubator.getLevel() != null
                && this.incubator.getLevel().getBlockEntity(this.incubator.getBlockPos()) == this.incubator
                && player.distanceToSqr(Vec3.atCenterOf(this.incubator.getBlockPos())) <= MAX_USE_DISTANCE_SQR;
    }
}
