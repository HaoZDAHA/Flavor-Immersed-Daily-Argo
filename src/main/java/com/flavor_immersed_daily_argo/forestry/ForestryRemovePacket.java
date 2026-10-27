package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：在护林员写字板界面上解除一处网点。
//
// dimension：网点所在维度
// pos：被标记的那格原木（服务端据此换算区块）
public record ForestryRemovePacket(ResourceLocation dimension, BlockPos pos) implements CustomPacketPayload {

    public static final Type<ForestryRemovePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "forestry_remove"));

    public static final StreamCodec<ByteBuf, ForestryRemovePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ForestryRemovePacket::dimension,
            BlockPos.STREAM_CODEC, ForestryRemovePacket::pos,
            ForestryRemovePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
