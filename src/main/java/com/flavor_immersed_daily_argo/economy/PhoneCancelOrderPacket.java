package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：在接单界面点下某一单的「取消」。
//
// 服务端据此把这一单整个作废：清掉可能已在场的两端实体、收回身上那条「打包好的外卖」，
// 再把订单从在途列表里摘掉，随后回发一包 PhoneOrderSyncPacket 让界面就地刷新。
public record PhoneCancelOrderPacket(long orderId) implements CustomPacketPayload {

    public static final Type<PhoneCancelOrderPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "phone_cancel_order"));

    public static final StreamCodec<ByteBuf, PhoneCancelOrderPacket> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(PhoneCancelOrderPacket::new, PhoneCancelOrderPacket::orderId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
