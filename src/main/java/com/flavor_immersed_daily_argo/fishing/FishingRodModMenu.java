package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.all.ModMenus;
import com.flavor_immersed_daily_argo.item.ModFishingRodItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FishingRodModMenu extends AbstractContainerMenu {

    public static final int SLOT_COUNT = 6;

    private final SimpleContainer modSlots;
    private final ItemStack rodStack;
    private final int rodSlot;
    private final Player owner;
// 自定义名称，随配件一起写入钓竿
    private String customName = "";
// 装载初始配件期间不写回，避免把空槽位覆盖到钓竿上
    private boolean commitEnabled;

    public FishingRodModMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, extraData.readVarInt());
    }

    public FishingRodModMenu(int containerId, Inventory playerInv, int rodSlot) {
        super(ModMenus.ROD_MOD_MENU.get(), containerId);
        this.rodSlot = rodSlot;
        this.rodStack = playerInv.getItem(rodSlot);
        this.owner = playerInv.player;
// 槽位内容一有变化就立刻写回钓竿：不存在「未保存」的中间状态，配件因此不会被吞掉
        this.modSlots = new SimpleContainer(SLOT_COUNT) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (commitEnabled && !owner.level().isClientSide()) {
                    commitToRod();
                }
            }
        };

// 加载已有改装数据
        FishingRodModData data = getData(rodStack);
        this.customName = data.customName();
        for (int i = 0; i < SLOT_COUNT; i++) {
            modSlots.setItem(i, (data != null && i < data.mods().size()) ? data.mods().get(i).copy() : ItemStack.EMPTY);
        }
        this.commitEnabled = true;

// 鱼线 (0)
        addSlot(new Slot(modSlots, 0, 105, 45) {
            @Override public boolean mayPlace(ItemStack s) { return true; }
        });
// 竿体 (1)
        addSlot(new Slot(modSlots, 1, 160, 45) {
            @Override public boolean mayPlace(ItemStack s) { return true; }
        });
// 线轮 (2)
        addSlot(new Slot(modSlots, 2, 215, 45) {
            @Override public boolean mayPlace(ItemStack s) { return true; }
        });
// 鱼饵 (3)
        addSlot(new Slot(modSlots, 3, 105, 105) {
            @Override public boolean mayPlace(ItemStack s) { return true; }
        });
// 浮漂 (4)
        addSlot(new Slot(modSlots, 4, 160, 105) {
            @Override public boolean mayPlace(ItemStack s) { return true; }
        });
// 铅坠 (5)
        addSlot(new Slot(modSlots, 5, 215, 105) {
            @Override public boolean mayPlace(ItemStack s) { return true; }
        });

// 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 69 + col * 18, 172 + row * 18));
            }
        }
// 玩家快捷栏
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 69 + col * 18, 230));
        }
    }

// 服务端保存：写入自定义名称并把当前槽位内容写回钓竿
    public void saveAll(String customName) {
        this.customName = customName;
        commitToRod();
    }

// 把六个槽位的内容与名称写入钓竿的组件
    private void commitToRod() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.add(modSlots.getItem(i).copy());
        }
        ModDataComponents.ROD_MOD_DATA.get().set(rodStack, new FishingRodModData(customName, items));
    }

    public String getCustomName() {
        return customName;
    }

// 当前六个槽位的实时内容：客户端界面据此同步展示钓竿的贴图（装了竿体就换竿体贴图）
    public List<ItemStack> currentMods() {
        List<ItemStack> items = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.add(modSlots.getItem(i));
        }
        return items;
    }

    public static FishingRodModData getData(ItemStack stack) {
        return ModDataComponents.ROD_MOD_DATA.get().getOrDefault(stack, FishingRodModData.EMPTY);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            if (index < SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, 0, SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            slot.setChanged();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(rodSlot).getItem() instanceof ModFishingRodItem;
    }
}
