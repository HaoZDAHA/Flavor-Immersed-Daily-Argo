package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：点了收购箱界面里的「出售」。
//
// 不带参数：服务端只认玩家当前开着的收购箱界面（PurchasingBoxMenu），
// 谁开的就卖谁那只箱子。
public record PurchasingBoxSellPacket() implements CustomPacketPayload {

    public static final Type<PurchasingBoxSellPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "purchasing_box_sell"));

    public static final StreamCodec<FriendlyByteBuf, PurchasingBoxSellPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
            }, buf -> new PurchasingBoxSellPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
