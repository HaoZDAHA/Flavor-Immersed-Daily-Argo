package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：在自助取款机界面里改银行卡的持卡主人。
//
// 主人是写在物品组件上的，必须由服务端落笔；这里只把玩家输入的那串 ID / 名字带上去，
// 服务端再核对「当前打开界面的人是不是现任主人」后写回。
public record AtmSetOwnerPacket(String owner) implements CustomPacketPayload {

    public static final Type<AtmSetOwnerPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "atm_set_owner"));

    public static final StreamCodec<ByteBuf, AtmSetOwnerPacket> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(AtmSetOwnerPacket::new, AtmSetOwnerPacket::owner);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
