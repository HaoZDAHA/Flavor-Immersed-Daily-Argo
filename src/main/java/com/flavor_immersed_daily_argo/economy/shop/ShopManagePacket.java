package com.flavor_immersed_daily_argo.economy.shop;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：开店页的所有动作。
//
// 建店 / 删店 / 上架 / 下架 / 改价 / 补货 / 提现都由服务端分派到 ShopManager；
// 上架与补货直接用手上拿着的整叠物品，提现则用 index 携带玩家选中的银行卡槽位。
//
// action：动作
// shopId：目标店铺
// text：建店时的店名
// index：条目下标（改价 / 下架 / 补货），提现时改作银行卡槽位
// price：价格
public record ShopManagePacket(Action action, String shopId, String text, int index, int price) implements CustomPacketPayload {

// 开店页能做的七件事
    public enum Action {
        CREATE,
        DELETE,
        ADD_ENTRY,
        REMOVE_ENTRY,
        SET_PRICE,
        RESTOCK,
        WITHDRAW
    }

    public static final Type<ShopManagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "shop_manage"));

    public static final StreamCodec<FriendlyByteBuf, ShopManagePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.action().ordinal());
                buf.writeUtf(packet.shopId() == null ? "" : packet.shopId());
                buf.writeUtf(packet.text() == null ? "" : packet.text());
                buf.writeVarInt(packet.index());
                buf.writeVarInt(packet.price());
            },
            buf -> {
                Action action = Action.values()[buf.readVarInt()];
                String shopId = buf.readUtf();
                String text = buf.readUtf();
                int index = buf.readVarInt();
                int price = buf.readVarInt();
                return new ShopManagePacket(action, shopId, text, index, price);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
