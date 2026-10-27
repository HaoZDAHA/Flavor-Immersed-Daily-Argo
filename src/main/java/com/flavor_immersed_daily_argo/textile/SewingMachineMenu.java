package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.all.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// 缝纫机界面
//
// 布局：中间是一大块可缩放 / 可拖动的画幅（对应空白丝绸装备的 16×16 花纹），
// 画幅下方是调色板与「保存 / 取出」；左边是护甲槽和四个工具（着装 / 拆卸 / 画笔 / 橡皮 / 方块），
// 右边是丝绸槽。画幅像素与着装数据都存在装备自己的数据组件里。
public class SewingMachineMenu extends AbstractContainerMenu {

// 界面尺寸
// 绘制页的界面尺寸（画布 + 色板 + 工具，比较宽）
    public static final int IMAGE_WIDTH = 320;
    public static final int IMAGE_HEIGHT = 236;

// 装备页的界面尺寸：没有画布了，收紧成一个标准物品栏大小
    public static final int ARMOR_IMAGE_WIDTH = 176;
    public static final int ARMOR_IMAGE_HEIGHT = 152;

// 界面分两页：装备页只管放东西，绘制页只管画画
    public static final int PAGE_ARMOR = 0;
    public static final int PAGE_PAINT = 1;

// 顶部两个页签
    public static final int TAB_Y = 6;
    public static final int TAB_W = 64;
    public static final int TAB_H = 14;
    public static final int TAB_ARMOR_X = 6;
    public static final int TAB_PAINT_X = 74;

// 装备页：槽位（只在装备页显示）
    public static final int ARMOR_SLOT_X = 14;
    public static final int ARMOR_SLOT_Y = 32;
    public static final int SILK_SLOT_X = 52;
    public static final int SILK_SLOT_Y = 32;

    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_TOP = 66;
    public static final int HOTBAR_Y = 124;
    public static final int SLOT_SIZE = 18;
    public static final int INVENTORY_LABEL_Y = 54;

// 机器占用的槽位总数（护甲 + 丝绸）
    public static final int MACHINE_SLOTS = SewingMachineBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_END = MACHINE_SLOTS + 27;
    private static final int HOTBAR_START = MACHINE_SLOTS + 27;

// 装备页：按钮
    public static final int BTN_LEFT_X = 84;
    public static final int BTN_LEFT_W = 80;
    public static final int BTN_LEFT_H = 16;
    public static final int BTN_DRESS_Y = 30;
    public static final int BTN_UNDRESS_Y = 48;

// 绘制页：画布
    public static final int CANVAS_X = 12;
    public static final int CANVAS_Y = 24;
    public static final int CANVAS_SIZE = 160;

// 绘制页：调色板（HSV 色板 + 明度条 + 当前色）
    public static final int PALETTE_X = 180;
    public static final int PALETTE_Y = 24;
    public static final int PALETTE_W = 112;
    public static final int PALETTE_H = 92;
    public static final int VALUE_X = 296;
    public static final int VALUE_Y = 24;
    public static final int VALUE_W = 14;
    public static final int VALUE_H = 92;
    public static final int SWATCH_X = 180;
    public static final int SWATCH_Y = 120;
    public static final int SWATCH_SIZE = 16;

// 绘制页：工具 / 保存 / 取出
    public static final int TOOL_X1 = 180;
    public static final int TOOL_X2 = 242;
    public static final int TOOL_W = 58;
    public static final int TOOL_H = 16;
    public static final int TOOL_ROW1_Y = 140;
    public static final int TOOL_ROW2_Y = 158;
    public static final int TOOL_ROW3_Y = 176;
    public static final int TOOL_WIDE_W = 120;
    public static final int BTN_SAVE_X = 180;
    public static final int BTN_SAVE_Y = 196;
    public static final int BTN_TAKE_X = 242;
    public static final int BTN_TAKE_Y = 196;
    public static final int BTN_BOTTOM_W = 58;
    public static final int BTN_BOTTOM_H = 16;

// 绘制页：回退（摆在画布正下方那块空位上）
    public static final int BTN_UNDO_X = 12;
    public static final int BTN_UNDO_Y = 196;
    public static final int BTN_UNDO_W = 120;
    public static final int BTN_UNDO_H = 16;

// 服务端按钮编号
    public static final int BUTTON_DRESS = 0;
    public static final int BUTTON_UNDRESS = 1;
    public static final int BUTTON_SAVE = 2;
    public static final int BUTTON_TAKE = 3;
    public static final int BUTTON_UNDO = 4;

// 玩家离开这个距离界面就关掉
    private static final double MAX_USE_DISTANCE_SQR = 64.0D;

    private final SewingMachineBlockEntity machine;

// 客户端：从同步包里拿到的那台面上装备的一份拷贝
    private ItemStack syncedGarment = ItemStack.EMPTY;

// 当前页（只影响客户端：绘制页要把所有槽位藏起来）
    private int page = PAGE_ARMOR;

    public SewingMachineMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolve(playerInventory, extraData.readBlockPos()));
    }

    public SewingMachineMenu(int containerId, Inventory playerInventory, SewingMachineBlockEntity machine) {
        super(ModMenus.SEWING_MACHINE_MENU.get(), containerId);
        this.machine = machine;
        SimpleContainer container = machine.getInventory();

// 左侧：要着装进来的真护甲
        this.addSlot(new PageSlot(container, SewingMachineBlockEntity.SLOT_ARMOR, ARMOR_SLOT_X, ARMOR_SLOT_Y,
                stack -> stack.getItem() instanceof ArmorItem));
// 右侧：丝绸，画方块时消耗
        this.addSlot(new PageSlot(container, SewingMachineBlockEntity.SLOT_SILK, SILK_SLOT_X, SILK_SLOT_Y,
                stack -> stack.is(com.flavor_immersed_daily_argo.all.ModItems.SILK.get())));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new PageSlot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * SLOT_SIZE, PLAYER_INV_TOP + row * SLOT_SIZE, stack -> true));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new PageSlot(playerInventory, col, PLAYER_INV_X + col * SLOT_SIZE, HOTBAR_Y, stack -> true));
        }
    }

// 只在装备页启用的槽位。绘制页把它整个变成不活跃，原版就会跳过绘制、悬停与命中测试，
// 于是「绘制页看不到也点不到任何槽位」不需要挪动坐标（Slot.x/y 是 final）。
    private class PageSlot extends Slot {

        private final Predicate<ItemStack> filter;

        PageSlot(Container container, int index, int x, int y, Predicate<ItemStack> filter) {
            super(container, index, x, y);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.filter.test(stack);
        }

        @Override
        public boolean isActive() {
            return page == PAGE_ARMOR;
        }
    }

// 页面

    public int page() {
        return this.page;
    }

// 切换页面；绘制页没有槽位（槽位自身按页判定 isActive），这里只记下当前页
    public void setPage(int page) {
        this.page = page;
    }

    private static SewingMachineBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof SewingMachineBlockEntity machine) {
            return machine;
        }
        throw new IllegalStateException("找不到缝纫机方块实体：" + pos);
    }

    public SewingMachineBlockEntity blockEntity() {
        return this.machine;
    }

// 台面装备（客户端渲染用）

// 客户端收到同步包后落到这里
    public void applySync(ItemStack garment) {
        this.syncedGarment = garment;
    }

// 台面上那件装备；客户端用同步下来的拷贝
    public ItemStack displayGarment() {
        if (this.machine.getLevel() != null && this.machine.getLevel().isClientSide) {
            return this.syncedGarment;
        }
        return this.machine.getGarment();
    }

    public boolean hasGarment() {
        return !displayGarment().isEmpty();
    }

// 当前花纹
    public List<Integer> displayDesign() {
        ItemStack garment = displayGarment();
        return garment.isEmpty() ? SilkDesign.emptyCanvas() : SilkDesign.get(garment);
    }

// 当前已经堆出来的 3D 体素
    public List<Integer> displayVoxels() {
        ItemStack garment = displayGarment();
        return garment.isEmpty() ? SilkDesign.emptyVoxels() : SilkDesign.getVoxels(garment);
    }

// 台面装备对应的槽位，决定要在哪些 cube 上作画；不是护甲就返回 null
    public EquipmentSlot garmentSlot() {
        ItemStack garment = displayGarment();
        return garment.getItem() instanceof ArmorItem armor ? armor.getEquipmentSlot() : null;
    }

// 客户端落笔后先本地改一份，画面立刻跟上，服务端的同步随后到。
//
// 体素模式（SewingMachineEditPacket#MODE_VOXEL / SewingMachineEditPacket#MODE_VOXEL_REMOVE）
// 时 (x1,y1,z1) 是 cube 里的体素坐标 (i,j,k)，face 忽略；其余模式 x1/y1/x2/y2 是 face 上的像素坐标。
    public void applyLocalEdit(int mode, int cube, int face,
                               int x1, int y1, int z1, int x2, int y2, int z2, int color) {
        ItemStack garment = displayGarment();
        if (garment.isEmpty()) {
            return;
        }
        if (mode == SewingMachineEditPacket.MODE_VOXEL || mode == SewingMachineEditPacket.MODE_VOXEL_REMOVE) {
            List<Integer> voxels = new ArrayList<>(SilkDesign.getVoxels(garment));
            boolean changed = mode == SewingMachineEditPacket.MODE_VOXEL
                    ? SilkDesign.addVoxel(voxels, cube, x1, y1, z1, color)
                    : SilkDesign.removeVoxel(voxels, cube, x1, y1, z1);
            if (changed) {
                SilkDesign.setVoxels(garment, voxels);
            }
            return;
        }
        List<Integer> design = new ArrayList<>(SilkDesign.get(garment));
        int width = SilkDesign.faceWidth(cube, face);
        int height = SilkDesign.faceHeight(cube, face);
        int minX = Mth.clamp(Math.min(x1, x2), 0, width - 1);
        int maxX = Mth.clamp(Math.max(x1, x2), 0, width - 1);
        int minY = Mth.clamp(Math.min(y1, y2), 0, height - 1);
        int maxY = Mth.clamp(Math.max(y1, y2), 0, height - 1);
        int value = mode == SewingMachineEditPacket.MODE_ERASE ? SilkDesign.TRANSPARENT : color;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                design.set(SilkDesign.index(cube, face, x, y), value);
            }
        }
        SilkDesign.set(garment, design);
    }

// 右侧槽里是否还有丝绸（方块工具要用）
    public boolean hasSilk() {
        return this.machine.getInventory().getItem(SewingMachineBlockEntity.SLOT_SILK)
                .is(com.flavor_immersed_daily_argo.all.ModItems.SILK.get());
    }

// 服务端按钮

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.machine.getLevel() == null || this.machine.getLevel().isClientSide) {
            return false;
        }
        return switch (id) {
            case BUTTON_DRESS -> dress();
            case BUTTON_UNDRESS -> undress();
            case BUTTON_SAVE -> {
                this.machine.markChanged();
                yield true;
            }
            case BUTTON_TAKE -> takeOut(player);
            case BUTTON_UNDO -> this.machine.undo();
            default -> false;
        };
    }

// 着装：把左侧护甲的附魔、属性、耐久搬到台面装备上，护甲原件消失
    private boolean dress() {
        SimpleContainer container = this.machine.getInventory();
        ItemStack armor = container.getItem(SewingMachineBlockEntity.SLOT_ARMOR);
        ItemStack garment = this.machine.getGarment();
        if (armor.isEmpty() || garment.isEmpty() || SilkDressing.isDressed(garment)) {
            return false;
        }
        SilkDressing.dress(garment, armor);
        container.setItem(SewingMachineBlockEntity.SLOT_ARMOR, ItemStack.EMPTY);
        this.machine.markChanged();
        return true;
    }

// 拆卸：把护甲按剩余耐久还回来，并清掉丝绸装备上的那些数据
    private boolean undress() {
        SimpleContainer container = this.machine.getInventory();
        ItemStack garment = this.machine.getGarment();
        if (garment.isEmpty() || !SilkDressing.isDressed(garment)) {
            return false;
        }
        if (!container.getItem(SewingMachineBlockEntity.SLOT_ARMOR).isEmpty()) {
            return false;
        }
        ItemStack restored = SilkDressing.undress(garment);
        container.setItem(SewingMachineBlockEntity.SLOT_ARMOR, restored);
        this.machine.markChanged();
        return true;
    }

// 取出：把台面装备收进背包
    private boolean takeOut(Player player) {
        ItemStack garment = this.machine.getGarment();
        if (garment.isEmpty()) {
            return false;
        }
        ItemStack taken = garment.copy();
        this.machine.setGarment(ItemStack.EMPTY);
        if (!player.getInventory().add(taken)) {
            player.drop(taken, false);
        }
        return true;
    }

// 交互

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!this.moveItemStackTo(stack, MACHINE_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, MACHINE_SLOTS, false)) {
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
        return this.machine.getLevel() != null
                && this.machine.getLevel().getBlockEntity(this.machine.getBlockPos()) == this.machine
                && player.distanceToSqr(Vec3.atCenterOf(this.machine.getBlockPos())) <= MAX_USE_DISTANCE_SQR;
    }
}
