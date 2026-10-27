package com.flavor_immersed_daily_argo.economy.shop;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

// 店铺列表的编解码：Shop / ShopEntry 里带着 Item 实例，不能直接序列化，
// 统一按物品注册名（ResourceLocation）编解码。
public final class ShopNetwork {

    private ShopNetwork() {
    }

    public static void writeShops(FriendlyByteBuf buf, List<Shop> shops) {
        buf.writeVarInt(shops.size());
        for (Shop shop : shops) {
            buf.writeUtf(shop.id());
            buf.writeUtf(shop.title());
            buf.writeUtf(shop.ownerName() == null ? "" : shop.ownerName());
            buf.writeVarLong(shop.balance());
            buf.writeBoolean(shop.playerShop());
            buf.writeVarInt(shop.entries().size());
            for (ShopEntry entry : shop.entries()) {
                buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.item()));
                buf.writeVarInt(entry.count());
                buf.writeVarInt(entry.price());
                buf.writeVarInt(entry.stock());
            }
        }
    }

    public static List<Shop> readShops(FriendlyByteBuf buf) {
        int shopCount = buf.readVarInt();
        List<Shop> shops = new ArrayList<>(shopCount);
        for (int i = 0; i < shopCount; i++) {
            String id = buf.readUtf();
            String title = buf.readUtf();
            String owner = buf.readUtf();
            long balance = buf.readVarLong();
            boolean playerShop = buf.readBoolean();
            int entryCount = buf.readVarInt();
            List<ShopEntry> entries = new ArrayList<>(entryCount);
            for (int j = 0; j < entryCount; j++) {
                ResourceLocation rl = buf.readResourceLocation();
                Item item = BuiltInRegistries.ITEM.get(rl);
                int count = buf.readVarInt();
                int price = buf.readVarInt();
                int stock = buf.readVarInt();
                entries.add(new ShopEntry(item, count, price, stock));
            }
            shops.add(new Shop(id, title, owner, List.copyOf(entries), balance, playerShop));
        }
        return shops;
    }
}
