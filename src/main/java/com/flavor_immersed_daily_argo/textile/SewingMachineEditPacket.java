package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.compat.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 画幅编辑（客户端 → 服务端）
//
// 四种操作：单点涂色、单点擦除、放一个 3D 体素、拆一个 3D 体素。放体素要吃掉右侧槽位里的
// 一份丝绸，所以这笔账必须在服务端算。
//
// 花纹是画在盔甲模型的 cube 上的，所以一笔要同时带上「哪个 cube」。面上编辑再带上「哪个面」，
// 剩下的 x/y 只是这个面上的像素坐标；体素编辑时 (x1,y1,z1) 直接是 cube 里的体素坐标 (i,j,k)，
// 与面无关。
//
// mode：见 #MODE_PAINT / #MODE_ERASE / #MODE_VOXEL / #MODE_VOXEL_REMOVE
// cube：目标 cube，见 SilkDesign#CUBE_HEAD 等
// face：目标面，见 SilkDesign#FACE_DOWN 等（体素模式忽略）
// x1：面上编辑是像素列；体素模式是体素 i
// y1：面上编辑是像素行；体素模式是体素 j
// z1：仅体素模式用，体素 k
// x2：面上编辑的终点像素列（体素模式忽略）
// y2：面上编辑的终点像素行（体素模式忽略）
// z2：仅历史上用的终点，现在固定 0
// color：ARGB 颜色
// beginStroke：是否是这一笔（从按下到松开）的第一包：服务端据此记一个回退点
public record SewingMachineEditPacket(int mode, int cube, int face,
                                      int x1, int y1, int z1, int x2, int y2, int z2,
                                      int color, boolean beginStroke) implements CustomPacketPayload {

// 画笔：单点涂色
    public static final int MODE_PAINT = 0;
// 橡皮：单点擦除（color 忽略）
    public static final int MODE_ERASE = 1;
// 方块：在 (x1,y1,z1) 这个体素格上放一个小方块，扣 1 丝绸
    public static final int MODE_VOXEL = 2;
// 方块：把 (x1,y1,z1) 这个格子上的小方块拆掉
    public static final int MODE_VOXEL_REMOVE = 3;

    public static final Type<SewingMachineEditPacket> TYPE =
            new Type<>(new ResourceLocation(
                    flavor_immersed_daily_argo.MODID, "sewing_machine_edit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SewingMachineEditPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeVarInt(packet.mode());
                        buf.writeVarInt(packet.cube());
                        buf.writeVarInt(packet.face());
                        buf.writeVarInt(packet.x1());
                        buf.writeVarInt(packet.y1());
                        buf.writeVarInt(packet.z1());
                        buf.writeVarInt(packet.x2());
                        buf.writeVarInt(packet.y2());
                        buf.writeVarInt(packet.z2());
                        buf.writeVarInt(packet.color());
                        buf.writeBoolean(packet.beginStroke());
                    },
                    buf -> new SewingMachineEditPacket(
                            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
