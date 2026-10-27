package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

// 服务端 → 客户端：全量同步该玩家的林业网点表。
//
// 每个网点编码成一行文本，客户端直接覆盖本地缓存供写字板界面显示（网点数量很小，无需增量协议）。
public record ForestrySyncPacket(List<String> points) implements CustomPacketPayload {

    public static final Type<ForestrySyncPacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "forestry_sync"));

    public static final StreamCodec<ByteBuf, ForestrySyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.<ByteBuf, String, List<String>>collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    ForestrySyncPacket::points,
                    ForestrySyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
