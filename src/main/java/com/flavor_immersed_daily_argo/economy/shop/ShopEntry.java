package com.flavor_immersed_daily_argo.economy.shop;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** One purchase lot. The prototype preserves all serializable item data/components. */
public record ShopEntry(ItemStack prototype, int count, int price, int stock) {
    public ShopEntry {
        prototype = prototype == null || prototype.isEmpty() ? ItemStack.EMPTY : prototype.copyWithCount(1);
    }

    // Existing system/config listings and legacy saves still create plain-item entries.
    public ShopEntry(Item item, int count, int price, int stock) {
        this(new ItemStack(item), count, price, stock);
    }

    @Override
    public ItemStack prototype() {
        return this.prototype.copy();
    }

    public Item item() {
        return this.prototype.getItem();
    }

    public ItemStack createStack(int quantity) {
        return this.prototype.copyWithCount(quantity);
    }

    public boolean matches(ItemStack goods) {
        return !goods.isEmpty() && ItemStack.isSameItemSameComponents(this.prototype, goods);
    }

    public ShopEntry withStock(int value) {
        return new ShopEntry(this.prototype, this.count, this.price, value);
    }

    public ShopEntry withPrice(int value) {
        return new ShopEntry(this.prototype, this.count, value, this.stock);
    }

    public boolean perStack() {
        return this.count > 1 && this.count == this.prototype.getMaxStackSize();
    }

    public String priceTranslationKey() {
        return "gui.flavor_immersed_daily_argo.market."
                + (this.count == 1 ? "price_each" : this.perStack() ? "price_stack" : "price_batch");
    }

    public boolean infinite() {
        return this.stock < 0;
    }
}
