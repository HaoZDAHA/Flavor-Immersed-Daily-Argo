package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：在自助取款机的外汇兑换页面点某一条兑换。
//
// 账户是写在物品组件上的，必须由服务端落笔；这里只带上条目下标，服务端再核对
// 「打开界面的玩家是不是卡主人、余额够不够」后执行。
public record AtmExchangePacket(int index) implements CustomPacketPayload {

    public static final Type<AtmExchangePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "atm_exchange"));

    public static final StreamCodec<ByteBuf, AtmExchangePacket> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(AtmExchangePacket::new, AtmExchangePacket::index);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
