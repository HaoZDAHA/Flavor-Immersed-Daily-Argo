package com.flavor_immersed_daily_argo.economy.shop;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

// 客户端 → 服务端：提交购物车结账。
//
// 钱写在银行卡组件上、库存写在存档数据上，都必须由服务端落笔；这里把购物车与玩家选中的
// 银行卡所在背包格一起带上去，服务端再核对「那张卡 / 卡面账户够 / 有限库存足量」后执行。
//
// cart：购物车内容
// cardSlot：玩家选中的银行卡所在的背包槽位（0–35 主背包 / 40 副手）
public record ShopCheckoutPacket(List<CartLine> cart, int cardSlot) implements CustomPacketPayload {

    public static final Type<ShopCheckoutPacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "shop_checkout"));

    public static final StreamCodec<FriendlyByteBuf, ShopCheckoutPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.cart().size());
                for (CartLine line : packet.cart()) {
                    buf.writeUtf(line.shopId());
                    buf.writeVarInt(line.entryIndex());
                    buf.writeVarInt(line.qty());
                }
                buf.writeVarInt(packet.cardSlot());
            },
            buf -> {
                int size = buf.readVarInt();
                List<CartLine> cart = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    cart.add(new CartLine(buf.readUtf(), buf.readVarInt(), buf.readVarInt()));
                }
                return new ShopCheckoutPacket(List.copyOf(cart), buf.readVarInt());
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
