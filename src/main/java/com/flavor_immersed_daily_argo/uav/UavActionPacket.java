package com.flavor_immersed_daily_argo.uav;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：遥控器界面上的一次操作（解绑 / 开关电源 / 开始或结束操作 / 附身战斗）。
//
// 坐标只在 #MARK_BLOCK 时有意义，其余动作一律填 0。一次操作就一个包，省得为每种按钮各写一个类。
public record UavActionPacket(int entityId, byte action,
                              double x1, double y1, double z1,
                              double x2, double y2, double z2) implements CustomPacketPayload {

// 解绑无人机
    public static final byte UNBIND = 0;
// 开 / 关电源
    public static final byte TOGGLE_POWER = 1;
// 开始第一人称操作
    public static final byte OPERATE = 4;
// 结束第一人称操作
    public static final byte STOP_OPERATE = 5;
// 打开无人机的物品栏界面
    public static final byte OPEN_INVENTORY = 6;
// 附身时左键：发射弹射物
    public static final byte COMBAT_FIRE = 7;
// 附身时右键：食用使用槽物品，不行就发射弹射物
    public static final byte COMBAT_USE = 8;
// 附身时右键方块：高亮标记这格方块（坐标塞在 x1 / y1 / z1 里）
    public static final byte MARK_BLOCK = 9;

    public static final Type<UavActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "uav_action"));

    public static final StreamCodec<ByteBuf, UavActionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId());
                buf.writeByte(packet.action());
                buf.writeDouble(packet.x1());
                buf.writeDouble(packet.y1());
                buf.writeDouble(packet.z1());
                buf.writeDouble(packet.x2());
                buf.writeDouble(packet.y2());
                buf.writeDouble(packet.z2());
            },
            buf -> new UavActionPacket(
                    buf.readInt(), buf.readByte(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
