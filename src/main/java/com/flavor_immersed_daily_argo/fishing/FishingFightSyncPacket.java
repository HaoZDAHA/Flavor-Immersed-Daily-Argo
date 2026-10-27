package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端 → 客户端：同步东篱钓竿「遛鱼阶段」的状态。
//
// 阶段开始时会带上浮漂入水的锚点坐标 —— 鱼的一切发力（拽玩家、拽相机）都是朝这个点，
// 客户端据此自行推算方向，无需再逐帧同步方向。
// active 为 false 表示阶段结束。
//
// 同时携带由钓竿 / 鱼参数算出的两个表现系数：params 里的拖拽力度与屏幕抖动幅度。
// 刻意不携带进度值：阶段进度是服务端权威数据，且设计上不做任何 HUD 提示。
public record FishingFightSyncPacket(int hookId, double anchorX, double anchorY, double anchorZ, boolean active,
                                     FightParams params)
        implements CustomPacketPayload {

    public static final Type<FishingFightSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "fishing_fight_sync"));

    public static final StreamCodec<ByteBuf, FishingFightSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, FishingFightSyncPacket::hookId,
                    ByteBufCodecs.DOUBLE, FishingFightSyncPacket::anchorX,
                    ByteBufCodecs.DOUBLE, FishingFightSyncPacket::anchorY,
                    ByteBufCodecs.DOUBLE, FishingFightSyncPacket::anchorZ,
                    ByteBufCodecs.BOOL, FishingFightSyncPacket::active,
                    FightParams.STREAM_CODEC, FishingFightSyncPacket::params,
                    FishingFightSyncPacket::new);

// 遛鱼阶段的表现系数：拖拽力度 = 鱼最大拉力 / 钓竿最大拉力；抖动幅度 = 鱼警惕性 × 钓组显眼度
    public record FightParams(double tensionFactor, double shakeAmount) {

        public static final StreamCodec<ByteBuf, FightParams> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.DOUBLE, FightParams::tensionFactor,
                ByteBufCodecs.DOUBLE, FightParams::shakeAmount,
                FightParams::new);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
