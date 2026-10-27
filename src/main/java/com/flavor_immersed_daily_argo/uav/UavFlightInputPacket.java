package com.flavor_immersed_daily_argo.uav;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：第一人称操作时每 tick 上报一次飞行输入。
//
// 按键打包成一个位掩码（见 Uav.KEY_*），朝向直接抄操作者的视角——机头就跟着人看哪儿转。
// 服务端照着这份输入把无人机推着走；操作者那一端自己也用同样的键算一遍，手感才不滞后。
public record UavFlightInputPacket(int entityId, int keys, float yaw, float pitch) implements CustomPacketPayload {

    public static final Type<UavFlightInputPacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "uav_flight_input"));

    public static final StreamCodec<ByteBuf, UavFlightInputPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId());
                buf.writeInt(packet.keys());
                buf.writeFloat(packet.yaw());
                buf.writeFloat(packet.pitch());
            },
            buf -> new UavFlightInputPacket(buf.readInt(), buf.readInt(), buf.readFloat(), buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
