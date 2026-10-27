package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenRodModPacket() implements CustomPacketPayload {

    public static final Type<OpenRodModPacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "open_rod_mod"));

    public static final StreamCodec<ByteBuf, OpenRodModPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenRodModPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
