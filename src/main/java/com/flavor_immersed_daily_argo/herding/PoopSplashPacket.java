package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端 → 客户端：某个玩家被抛出的粪便砸中了，让 ta 屏幕上糊一层 shitgui.png。
//
// ticks：覆层停留的刻数
public record PoopSplashPacket(int ticks) implements CustomPacketPayload {

    public static final Type<PoopSplashPacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "poop_splash"));

    public static final StreamCodec<ByteBuf, PoopSplashPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PoopSplashPacket::ticks,
            PoopSplashPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
