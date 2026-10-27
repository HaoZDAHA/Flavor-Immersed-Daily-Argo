package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：在护林员写字板界面里给一处网点改名。
//
// dimension：网点所在维度
// pos：被标记的那格原木
// name：新名字，空串表示恢复成默认名字
public record ForestryRenamePacket(ResourceLocation dimension, BlockPos pos, String name)
        implements CustomPacketPayload {

    public static final Type<ForestryRenamePacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "forestry_rename"));

    public static final StreamCodec<ByteBuf, ForestryRenamePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                FriendlyByteBuf out = (FriendlyByteBuf) buf;
                out.writeResourceLocation(packet.dimension());
                out.writeBlockPos(packet.pos());
                out.writeUtf(packet.name());
            },
            buf -> {
                FriendlyByteBuf in = (FriendlyByteBuf) buf;
                return new ForestryRenamePacket(in.readResourceLocation(), in.readBlockPos(), in.readUtf());
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
