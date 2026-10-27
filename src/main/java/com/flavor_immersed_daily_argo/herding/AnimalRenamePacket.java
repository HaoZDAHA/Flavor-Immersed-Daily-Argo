package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端改完名之后交给服务端写入实体。
//
// entityId：目标实体
// name：新名字，空串表示恢复成默认名字
public record AnimalRenamePacket(int entityId, String name) implements CustomPacketPayload {

    public static final Type<AnimalRenamePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "animal_rename"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnimalRenamePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, AnimalRenamePacket::entityId,
                    ByteBufCodecs.STRING_UTF8, AnimalRenamePacket::name,
                    AnimalRenamePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
