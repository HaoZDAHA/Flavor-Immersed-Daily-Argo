package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：在护林员写字板界面上解除一处网点。
//
// dimension：网点所在维度
// pos：被标记的那格原木（服务端据此换算区块）
public record ForestryRemovePacket(ResourceLocation dimension, BlockPos pos) implements CustomPacketPayload {

    public static final Type<ForestryRemovePacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "forestry_remove"));

    public static final StreamCodec<ByteBuf, ForestryRemovePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                FriendlyByteBuf out = (FriendlyByteBuf) buf;
                out.writeResourceLocation(packet.dimension());
                out.writeBlockPos(packet.pos());
            },
            buf -> {
                FriendlyByteBuf in = (FriendlyByteBuf) buf;
                return new ForestryRemovePacket(in.readResourceLocation(), in.readBlockPos());
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
