package com.flavor_immersed_daily_argo.uav;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.all.ModMenus;
import com.flavor_immersed_daily_argo.entity.Uav;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

// 无人机物品栏界面：模组槽 1 个 + 使用槽 1 个 + 背包槽 4 个，下面接玩家背包。
//
// 无人机是实体，所以客户端没法只靠 containerId 找回它：openMenu 时把实体 id 写进附加数据，
// 客户端按 id 从当前世界取回同一架。取不到（比如刚好被卸载）就在本地拿个占位容器顶上，
// 槽里的内容本来就会由服务端同步下来，界面照常能用。
public class UavMenu extends AbstractContainerMenu {

// 界面尺寸，与 UavScreen 保持一致
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 188;

// 玩家背包标题的纵向位置
    public static final int INVENTORY_LABEL_Y = 94;

// 三个标签的位置，UavScreen 照着画
    public static final int MODULE_LABEL_X = 53;
    public static final int USE_LABEL_X = 125;
    public static final int TOP_LABEL_Y = 24;
    public static final int BACKPACK_LABEL_X = 88;
    public static final int BACKPACK_LABEL_Y = 60;

    private static final int SLOT_SIZE = 18;
    private static final int MODULE_X = 44;
    private static final int USE_X = 116;
    private static final int TOP_Y = 36;
    private static final int BACKPACK_LEFT = 52;
    private static final int BACKPACK_Y = 72;
    private static final int PLAYER_INV_TOP = 106;
    private static final int HOTBAR_Y = 164;

// 客户端找回来的那架无人机；服务端一定有，客户端可能因为实体没加载而拿不到
    @Nullable
    private final Uav uav;

// 客户端构造：附加数据里写的是无人机的实体 id
    public UavMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolve(playerInventory, extraData.readVarInt()));
    }

    public UavMenu(int containerId, Inventory playerInventory, @Nullable Uav uav) {
        super(ModMenus.UAV_MENU.get(), containerId);
        this.uav = uav;
        Container container = uav != null ? uav.getInventory() : new SimpleContainer(Uav.SLOT_COUNT);

// 模组槽：只收五种无人机模块之一
        this.addSlot(new Slot(container, Uav.MODULE_SLOT, MODULE_X, TOP_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isModule(stack);
            }
        });

// 使用槽
        this.addSlot(new Slot(container, Uav.USE_SLOT, USE_X, TOP_Y));

// 背包槽 4 个
        for (int col = 0; col < Uav.BACKPACK_SLOTS; col++) {
            this.addSlot(new Slot(container, 2 + col, BACKPACK_LEFT + col * SLOT_SIZE, BACKPACK_Y));
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

// 这东西是不是无人机模块
    public static boolean isModule(ItemStack stack) {
        for (DeferredHolder<Item, Item> module : ModItems.UAV_MODULES) {
            if (stack.is(module.get())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Uav resolve(Inventory playerInventory, int entityId) {
        return playerInventory.player.level().getEntity(entityId) instanceof Uav uav ? uav : null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();

        if (index < Uav.SLOT_COUNT) {
// 无人机槽 → 玩家背包
            if (!this.moveItemStackTo(stack, Uav.SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (isModule(stack)) {
// 模块优先塞模组槽，塞不下再退到背包槽（使用槽留给要用的东西）
            if (!this.moveItemStackTo(stack, Uav.MODULE_SLOT, Uav.MODULE_SLOT + 1, false)
                    && !this.moveItemStackTo(stack, Uav.USE_SLOT + 1, Uav.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, Uav.USE_SLOT, Uav.SLOT_COUNT, false)) {
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
// 无人机是遥控操作的，不卡距离；它还在就没问题。客户端没取到实体时也放行（内容照样由服务端同步）
        return this.uav == null || !this.uav.isRemoved();
    }
}
