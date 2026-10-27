package com.flavor_immersed_daily_argo.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

// 容器存读档的公共写法：内部统一是「Slot(byte) + 物品」的 NBT 列表。
//
// 方块实体与实体用的本来就是同一套格式，原先十来个类各抄一遍，改一处要改十处；
// 收在这里以后，存档格式只有这一个出处。
public final class InventoryNbt {

    private InventoryNbt() {
    }

// 把容器内容写成 NBT 列表，空格子不写入
    public static ListTag save(Container container, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag slotTag = new CompoundTag();
            slotTag.putByte("Slot", (byte) slot);
            list.add(stack.save(registries, slotTag));
        }
        return list;
    }

// 先清空容器，再从 tag[key] 读回。
//
// 越界槽位直接跳过；单个物品解析失败只丢那一格（原版会在日志里报
// "Tried to load invalid item"），不会让整个方块实体读档失败。
    public static void load(Container container, CompoundTag tag, String key, HolderLookup.Provider registries) {
        container.clearContent();
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return;
        }
// 列表元素是「一格一个 CompoundTag」，这里必须按 TAG_COMPOUND 取：
// 传 TAG_LIST 时原版会把类型不符的列表当成空表返回，容器内容会被清空
// （化粪桶 / 无人机等所有用这套格式的容器都会读不出东西）
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slotTag = list.getCompound(i);
            int slot = slotTag.getByte("Slot") & 0xFF;
            if (slot < container.getContainerSize()) {
                ItemStack.parse(registries, slotTag).ifPresent(stack -> container.setItem(slot, stack));
            }
        }
    }
}
