package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

// 服务端 → 客户端：同步该玩家「已钓获」的图鉴条目。
//
// 携带物品 id 字符串的全量列表，客户端直接覆盖本地缓存（条目数量很小，无需增量协议）。
public record FishingCaughtSyncPacket(List<String> caught) implements CustomPacketPayload {

    public static final Type<FishingCaughtSyncPacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "fishing_caught_sync"));

    public static final StreamCodec<ByteBuf, FishingCaughtSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.<ByteBuf, String, List<String>>collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    FishingCaughtSyncPacket::caught,
                    FishingCaughtSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}