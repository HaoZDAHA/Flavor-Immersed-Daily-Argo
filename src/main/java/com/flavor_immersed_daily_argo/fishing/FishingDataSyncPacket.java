package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

// 服务端 → 客户端：全量同步数据包驱动的钓鱼内容（手册条目 + 配件参数 + 称重分类）。
//
// 这些数据都由 data/<命名空间>/*.json 驱动，客户端资源管理器读不到 data 目录，
// 因此远端客户端拿不到服务端（含附属数据包）的内容。登录 / 数据包重载时服务端把已加载条目的
// 原始 JSON 全量下发，客户端用与数据包加载完全一致的解析逻辑重建，保证多端内容一致。
public record FishingDataSyncPacket(List<String> handbook, List<String> parts, List<String> weighing) implements CustomPacketPayload {

    public static final Type<FishingDataSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "fishing_data_sync"));

    public static final StreamCodec<ByteBuf, FishingDataSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.<ByteBuf, String, List<String>>collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    FishingDataSyncPacket::handbook,
                    ByteBufCodecs.<ByteBuf, String, List<String>>collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    FishingDataSyncPacket::parts,
                    ByteBufCodecs.<ByteBuf, String, List<String>>collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    FishingDataSyncPacket::weighing,
                    FishingDataSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
