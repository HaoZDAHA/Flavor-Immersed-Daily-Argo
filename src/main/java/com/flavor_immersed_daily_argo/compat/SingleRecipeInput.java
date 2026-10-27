package com.flavor_immersed_daily_argo.compat;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// 1.21 的 SingleRecipeInput 在 1.20.1 不存在，这里补一个「只装一件东西」的容器，
// 让 Recipe<C extends Container> 的签名能对上。
//
// 放在本模组自己的包名下，避免与 Minecraft 模块的 net.minecraft.world.item.crafting 抢包。
public record SingleRecipeInput(ItemStack item) implements Container {

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.item.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? this.item : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
