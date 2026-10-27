package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：在接单界面点下某一单的「接单」。
//
// 服务端据此把该单标记为已接，并在出发地生成外卖袋、在目的地生成配送点，
// 随后回发一包 PhoneOrderSyncPacket 让界面就地刷新。
public record PhoneAcceptOrderPacket(long orderId) implements CustomPacketPayload {

    public static final Type<PhoneAcceptOrderPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "phone_accept_order"));

    public static final StreamCodec<ByteBuf, PhoneAcceptOrderPacket> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(PhoneAcceptOrderPacket::new, PhoneAcceptOrderPacket::orderId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
