package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
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
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "forestry_rename"));

    public static final StreamCodec<ByteBuf, ForestryRenamePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ForestryRenamePacket::dimension,
            BlockPos.STREAM_CODEC, ForestryRenamePacket::pos,
            ByteBufCodecs.STRING_UTF8, ForestryRenamePacket::name,
            ForestryRenamePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
