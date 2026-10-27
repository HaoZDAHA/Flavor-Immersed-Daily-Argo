package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：上报本 tick 玩家是否做出了正确操作
// （视角反向转动 + 往鱼拽动的反方向后退，两者都满足才算正确）。
//
// 服务端只把它累加进「当前发力窗口内的正确 tick 数」，阶段进度与成败仍由服务端裁定。
public record FishingFightInputPacket(int hookId, boolean correct) implements CustomPacketPayload {

    public static final Type<FishingFightInputPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "fishing_fight_input"));

    public static final StreamCodec<ByteBuf, FishingFightInputPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, FishingFightInputPacket::hookId,
                    ByteBufCodecs.BOOL, FishingFightInputPacket::correct,
                    FishingFightInputPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
