package com.flavor_immersed_daily_argo.economy.compat;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端 → 客户端：联动开没开，以及共享账户当前余额。
//
// 余额只在 SDM Economy 的服务端数据里，客户端算不出来，得由服务端推。
// shared = false 时余额没意义，客户端按原版走。
//
// 1.20.1 的余额是 double，照原样传，显示时向下取整。
public record SharedBalancePacket(boolean shared, double balance) implements CustomPacketPayload {

// 用构造器而不是 fromNamespaceAndPath：后者只有 Forge 47.4+ 才有，
// 而本模组声明的最低支持版本是 47.3.0，在 47.3.0 上会直接 NoSuchMethodError。
    public static final Type<SharedBalancePacket> TYPE = new Type<>(
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "shared_balance"));

    public static final StreamCodec<FriendlyByteBuf, SharedBalancePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.shared());
                buf.writeDouble(packet.balance());
            },
            buf -> new SharedBalancePacket(buf.readBoolean(), buf.readDouble()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
