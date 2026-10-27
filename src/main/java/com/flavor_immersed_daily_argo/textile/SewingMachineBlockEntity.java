package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import javax.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// 缝纫机方块实体
//
// 身上一共三样东西：
//
// #garment：台面上那件正在加工的空白丝绸装备，花纹与「着装」进来的护甲都存在它的组件里；
// 槽位 0（#SLOT_ARMOR）：左侧槽，放要着装进来的真护甲；
// 槽位 1（#SLOT_SILK）：右侧槽，放丝绸，画方块时消耗。
public class SewingMachineBlockEntity extends BlockEntity implements MenuProvider {

// 左侧槽：要着装的真护甲
    public static final int SLOT_ARMOR = 0;
// 右侧槽：丝绸，画方块时消耗
    public static final int SLOT_SILK = 1;
// 槽位总数
    public static final int SLOT_COUNT = 2;

    private static final String GARMENT_TAG = "Garment";
    private static final String INVENTORY_TAG = "Inventory";

// 回退历史最多留这么多步，超了就把最老的一步丢掉
    private static final int MAX_UNDO = 64;

// 一次「回退」要还原的东西：花纹像素 + 3D 体素
    private record Snapshot(List<Integer> design, List<Integer> voxels) {
    }

// 回退点栈：每一笔（从按下到松开）开始时压入一笔之前的状态
    private final Deque<Snapshot> undoStack = new ArrayDeque<>();

// 两个槽位。玩家改动后要写回方块实体，存档才能拿走丝绸
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            SewingMachineBlockEntity.this.setChanged();
        }
    };

// 台面上正在加工的空白丝绸装备，没有就是空的
    private ItemStack garment = ItemStack.EMPTY;

    public SewingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEWING_MACHINE_ENTITY.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public ItemStack getGarment() {
        return this.garment;
    }

    public boolean hasGarment() {
        return !this.garment.isEmpty();
    }

// 放上 / 取下台面上的装备（放下和取出都走这里，顺带同步给开着界面的玩家）
    public void setGarment(ItemStack stack) {
        this.garment = stack;
// 换了件装备，之前的回退历史就不作数了
        this.undoStack.clear();
        this.setChanged();
        this.broadcast();
    }

// 台面上的装备被改动过（花纹、着装数据），存档并同步
    public void markChanged() {
        this.setChanged();
        this.broadcast();
    }

// 当前花纹；没放装备就是一张空画布
    public List<Integer> getDesign() {
        return this.garment.isEmpty() ? SilkDesign.emptyCanvas() : SilkDesign.get(this.garment);
    }

// 画幅编辑（仅服务端调用）

// 落一笔画。
//
// 面上编辑（画笔 / 橡皮）时 x1/y1/x2/y2 是「cube 的 face 这张面」上的像素坐标，
// z 忽略；体素编辑（SewingMachineEditPacket#MODE_VOXEL 等）时 (x1,y1,z1) 是 cube 里的体素
// 坐标 (i,j,k)，face 忽略。
//
// beginStroke：是否是这一笔的第一包；为真时先把「这一笔之前」的状态压进回退栈
// 返回：是否真的改到了东西（用来决定要不要广播）
    public boolean applyEdit(int mode, int cube, int face,
                             int x1, int y1, int z1, int x2, int y2, int z2, int color, boolean beginStroke) {
        if (this.garment.isEmpty()) {
            return false;
        }
        if (beginStroke) {
            this.pushUndo();
        }

// 体素建模：在某个格子上放 / 拆一个小方块
        if (mode == SewingMachineEditPacket.MODE_VOXEL || mode == SewingMachineEditPacket.MODE_VOXEL_REMOVE) {
            List<Integer> voxels = new ArrayList<>(SilkDesign.getVoxels(this.garment));
            if (mode == SewingMachineEditPacket.MODE_VOXEL) {
// 只有「新长出来一个方块」才吃丝绸，给已有方块改色不算
                boolean brandNew = SilkDesign.voxelAt(voxels, cube, x1, y1, z1) < 0;
                if (brandNew && !this.consumeSilk()) {
                    return false;
                }
                if (!SilkDesign.addVoxel(voxels, cube, x1, y1, z1, color)) {
                    return false;
                }
            } else if (!SilkDesign.removeVoxel(voxels, cube, x1, y1, z1)) {
                return false;
            }
            SilkDesign.setVoxels(this.garment, voxels);
            this.markChanged();
            return true;
        }

        List<Integer> design = new ArrayList<>(SilkDesign.get(this.garment));
        int width = SilkDesign.faceWidth(cube, face);
        int height = SilkDesign.faceHeight(cube, face);
        int minX = Mth.clamp(Math.min(x1, x2), 0, width - 1);
        int maxX = Mth.clamp(Math.max(x1, x2), 0, width - 1);
        int minY = Mth.clamp(Math.min(y1, y2), 0, height - 1);
        int maxY = Mth.clamp(Math.max(y1, y2), 0, height - 1);
        int value = mode == SewingMachineEditPacket.MODE_ERASE ? SilkDesign.TRANSPARENT : color;
        boolean changed = false;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (paint(design, cube, face, x, y, value)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            SilkDesign.set(this.garment, design);
            this.markChanged();
        }
        return changed;
    }

    private static boolean paint(List<Integer> design, int cube, int face, int x, int y, int value) {
        int index = SilkDesign.index(cube, face, x, y);
        if (design.get(index) == value) {
            return false;
        }
        design.set(index, value);
        return true;
    }

    private boolean consumeSilk() {
        ItemStack silk = this.inventory.getItem(SLOT_SILK);
        if (silk.isEmpty() || !silk.is(ModItems.SILK.get())) {
            return false;
        }
        silk.shrink(1);
        this.inventory.setChanged();
        return true;
    }

// 回退

// 把当前这身花纹 + 体素原样记一份，作为「上一步」
    private void pushUndo() {
        this.undoStack.push(new Snapshot(
                new ArrayList<>(SilkDesign.get(this.garment)),
                new ArrayList<>(SilkDesign.getVoxels(this.garment))));
        while (this.undoStack.size() > MAX_UNDO) {
            this.undoStack.removeLast();
        }
    }

// 回退上一步（画笔 / 橡皮 / 放方块 / 拆方块各算一步）。
//
// 只把花纹与体素还原成那一步之前的样子，不退丝绸：已经花掉的丝绸就当花了。
//
// 返回：是否真的回退了（栈空或回退后没有变化就是 false）
    public boolean undo() {
        if (this.garment.isEmpty()) {
            return false;
        }
        while (!this.undoStack.isEmpty()) {
            Snapshot snapshot = this.undoStack.pop();
            if (snapshot.design().equals(SilkDesign.get(this.garment))
                    && snapshot.voxels().equals(SilkDesign.getVoxels(this.garment))) {
// 这一步什么都没改（比如反复涂同一色），跳过它继续往前找
                continue;
            }
            SilkDesign.set(this.garment, snapshot.design());
            SilkDesign.setVoxels(this.garment, snapshot.voxels());
            this.markChanged();
            return true;
        }
        return false;
    }

// 把台面上装备的最新样子发给所有正开着这台机器界面的玩家
    private void broadcast() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        SewingMachineSyncPacket packet = new SewingMachineSyncPacket(this.garment.copy());
        for (ServerPlayer player : serverLevel.players()) {
            if (player.containerMenu instanceof SewingMachineMenu menu && menu.blockEntity() == this) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.garment.isEmpty()) {
            tag.put(GARMENT_TAG, this.garment.save(new CompoundTag()));
        }
        tag.put(INVENTORY_TAG, InventoryNbt.save(this.inventory));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.garment = ItemStack.EMPTY;
        if (tag.contains(GARMENT_TAG, CompoundTag.TAG_COMPOUND)) {
            java.util.Optional.of(ItemStack.of(tag.getCompound(GARMENT_TAG))).ifPresent(stack -> this.garment = stack);
        }
        InventoryNbt.load(this.inventory, tag, INVENTORY_TAG);
    }

// 界面

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.flavor_immersed_daily_argo.sewing_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SewingMachineMenu(containerId, playerInventory, this);
    }
}
