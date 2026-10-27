package com.flavor_immersed_daily_argo.uav;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端 → 客户端：无人机开了一枪，让听得见的人按这杆枪自己的声音资源播一声。
//
// TaCZ 的枪声是「按开枪的那个实体播」的：客户端收到包后拿实体 id 去世界里找人，找得到才播。
// 无人机借的那位替身玩家只活在服务端，客户端上根本不存在，那一份声音自然落空；所以这里换成
// 无人机的实体 id 重新发一份，客户端自己去找这杆枪的声音资源来放。
//
// entityId：谁在开枪（无人机），客户端拿它定位声源
// gunId：枪械 id，用来取它的声音资源
// gunDisplayId：枪械展示 id，同上
// soundName：声音槽位名（shoot_3p 等）
// volume：音量
// pitch：音调
// distance：能听见的距离（TaCZ 的 sound_distance）
public record UavGunSoundPacket(int entityId, ResourceLocation gunId, ResourceLocation gunDisplayId,
                                String soundName, float volume, float pitch, int distance)
        implements CustomPacketPayload {

    public static final Type<UavGunSoundPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "uav_gun_sound"));

    public static final StreamCodec<FriendlyByteBuf, UavGunSoundPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.entityId());
                buf.writeResourceLocation(packet.gunId());
                buf.writeResourceLocation(packet.gunDisplayId());
                buf.writeUtf(packet.soundName());
                buf.writeFloat(packet.volume());
                buf.writeFloat(packet.pitch());
                buf.writeVarInt(packet.distance());
            },
            buf -> new UavGunSoundPacket(
                    buf.readVarInt(), buf.readResourceLocation(), buf.readResourceLocation(),
                    buf.readUtf(), buf.readFloat(), buf.readFloat(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
