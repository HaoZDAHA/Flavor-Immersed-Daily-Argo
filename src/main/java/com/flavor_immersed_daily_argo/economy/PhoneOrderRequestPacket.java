package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：在展开的导航手机上右键，请求接单界面。
//
// 订单只活在服务端内存里，客户端看不到，所以这里不带内容，服务端收到后
// DeliveryManager#refresh 洗一批新单，再用 PhoneOrderSyncPacket 发回来。
public record PhoneOrderRequestPacket() implements CustomPacketPayload {

    public static final Type<PhoneOrderRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "phone_order_request"));

    public static final StreamCodec<ByteBuf, PhoneOrderRequestPacket> STREAM_CODEC =
            StreamCodec.unit(new PhoneOrderRequestPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
