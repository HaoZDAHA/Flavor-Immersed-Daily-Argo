package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// 鸡窝界面
//
// 上方是唯一的孵化槽（只收蛋，放进去会慢慢孵出对应的禽类幼崽），
// 中间 3×3 是收集槽（只存蛋，永远不孵化），下方是玩家背包。
public class ChickenCoopMenu extends AbstractContainerMenu {

// 界面尺寸，与 ChickenCoopScreen 保持一致
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 200;

// 玩家背包标题的纵向位置
    public static final int INVENTORY_LABEL_Y = 106;

    private static final int SLOT_SIZE = 18;
// 三列槽位在 176 宽界面里居中排布
    private static final int GRID_LEFT = 61;
    private static final int HATCH_Y = 20;
    private static final int COLLECT_TOP = 46;
    private static final int PLAYER_INV_TOP = 118;
    private static final int HOTBAR_Y = 176;

// 玩家离开这个距离界面就关掉
    private static final double MAX_USE_DISTANCE_SQR = 64.0D;

    private final ChickenCoopBlockEntity coop;

// 客户端：按方块坐标找回方块实体（由 openMenu 写进附加数据）
    public ChickenCoopMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolve(playerInventory, extraData.readBlockPos()));
    }

    public ChickenCoopMenu(int containerId, Inventory playerInventory, ChickenCoopBlockEntity coop) {
        super(ModMenus.CHICKEN_COOP_MENU.get(), containerId);
        this.coop = coop;

// 孵化槽：只接受鸡窝认得的蛋
        this.addSlot(new Slot(coop.getInventory(), ChickenCoopBlockEntity.HATCH_SLOT,
                GRID_LEFT + SLOT_SIZE, HATCH_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CoopEggs.entityFor(stack) != null;
            }
        });

// 收集槽 3×3
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = 1 + row * 3 + col;
                this.addSlot(new Slot(coop.getInventory(), index,
                        GRID_LEFT + col * SLOT_SIZE, COLLECT_TOP + row * SLOT_SIZE));
            }
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

    private static ChickenCoopBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof ChickenCoopBlockEntity coop) {
            return coop;
        }
        throw new IllegalStateException("找不到鸡窝方块实体：" + pos);
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
        if (index < ChickenCoopBlockEntity.SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, ChickenCoopBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, ChickenCoopBlockEntity.HATCH_SLOT,
                ChickenCoopBlockEntity.HATCH_SLOT + 1, false)
                && !this.moveItemStackTo(stack, 1, ChickenCoopBlockEntity.SLOT_COUNT, false)) {
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
        return this.coop.getLevel() != null
                && this.coop.getLevel().getBlockEntity(this.coop.getBlockPos()) == this.coop
                && player.distanceToSqr(Vec3.atCenterOf(this.coop.getBlockPos())) <= MAX_USE_DISTANCE_SQR;
    }
}
