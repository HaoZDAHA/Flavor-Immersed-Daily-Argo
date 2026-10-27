package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

// 服务端 → 客户端：把当前的订单列表（含接单 / 取货状态）发给客户端。
//
// open 区分两种下发：打开手机、点「接单」之后回发的一包带 true，客户端据此弹出 /
// 刷新接单界面；每秒给「手持导航手机」的玩家静默同步的一包带 false，只更新客户端缓存，
// 供 HUD 指南针读取，不弹界面。
public record PhoneOrderSyncPacket(List<PhoneOrderView> orders, boolean open) implements CustomPacketPayload {

    public static final Type<PhoneOrderSyncPacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "phone_order_sync"));

    public static final StreamCodec<ByteBuf, PhoneOrderSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    PhoneOrderView.STREAM_CODEC.apply(ByteBufCodecs.list()), PhoneOrderSyncPacket::orders,
                    ByteBufCodecs.BOOL, PhoneOrderSyncPacket::open,
                    PhoneOrderSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
