package com.flavor_immersed_daily_argo.economy.shop;

import com.flavor_immersed_daily_argo.economy.product.EconomicNews;
import com.flavor_immersed_daily_argo.economy.product.EconomicProductNetwork;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

// 服务端 → 客户端：全量下发店铺列表（含玩家店铺余额）与「今日经济产物涨跌」，客户端收到后写入
// ShopClientData。
public record ShopSyncPacket(List<Shop> shops, List<EconomicNews> news) implements CustomPacketPayload {

    public static final Type<ShopSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "shop_sync"));

    public static final StreamCodec<FriendlyByteBuf, ShopSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ShopNetwork.writeShops(buf, packet.shops());
                EconomicProductNetwork.writeNews(buf, packet.news());
            },
            buf -> new ShopSyncPacket(ShopNetwork.readShops(buf), EconomicProductNetwork.readNews(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
