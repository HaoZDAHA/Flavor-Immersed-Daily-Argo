package com.flavor_immersed_daily_argo.economy.shop;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Full stack prototypes, including components, are shared by initial menu data and live sync. */
public final class ShopNetwork {
    private ShopNetwork() { }

    public static void writeShops(RegistryFriendlyByteBuf buf, List<Shop> shops) {
        buf.writeVarInt(shops.size());
        for (Shop shop : shops) {
            buf.writeUtf(shop.id());
            buf.writeUtf(shop.title());
            buf.writeUtf(shop.ownerName() == null ? "" : shop.ownerName());
            buf.writeVarLong(shop.balance());
            buf.writeBoolean(shop.playerShop());
            buf.writeVarInt(shop.entries().size());
            for (ShopEntry entry : shop.entries()) {
                ItemStack.STREAM_CODEC.encode(buf, entry.prototype());
                buf.writeVarInt(entry.count());
                buf.writeVarInt(entry.price());
                buf.writeVarInt(entry.stock());
            }
        }
    }

    public static List<Shop> readShops(RegistryFriendlyByteBuf buf) {
        int shopCount = buf.readVarInt();
        if (shopCount < 0 || shopCount > 100_000) throw new IllegalArgumentException("Invalid shop count");
        List<Shop> shops = new ArrayList<>(shopCount);
        for (int i = 0; i < shopCount; i++) {
            String id = buf.readUtf();
            String title = buf.readUtf();
            String owner = buf.readUtf();
            long balance = buf.readVarLong();
            boolean playerShop = buf.readBoolean();
            int entryCount = buf.readVarInt();
            if (entryCount < 0 || entryCount > 100_000) throw new IllegalArgumentException("Invalid entry count");
            List<ShopEntry> entries = new ArrayList<>(entryCount);
            for (int j = 0; j < entryCount; j++) {
                ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
                int count = buf.readVarInt();
                int price = buf.readVarInt();
                int stock = buf.readVarInt();
                entries.add(new ShopEntry(stack, count, price, stock));
            }
            shops.add(new Shop(id, title, owner, List.copyOf(entries), balance, playerShop));
        }
        return shops;
    }
}
