package com.flavor_immersed_daily_argo.economy.product;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

// 经济产物新闻的编解码：EconomicNews 里带着 Item 实例，统一按物品注册名编解码。
public final class EconomicProductNetwork {

    private EconomicProductNetwork() {
    }

    public static void writeNews(FriendlyByteBuf buf, List<EconomicNews> news) {
        buf.writeVarInt(news.size());
        for (EconomicNews line : news) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(line.item()));
            buf.writeVarInt(line.price());
            buf.writeVarInt(line.delta());
        }
    }

    public static List<EconomicNews> readNews(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<EconomicNews> news = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation rl = buf.readResourceLocation();
            Item item = BuiltInRegistries.ITEM.get(rl);
            int price = buf.readVarInt();
            int delta = buf.readVarInt();
            news.add(new EconomicNews(item, price, delta));
        }
        return news;
    }
}
