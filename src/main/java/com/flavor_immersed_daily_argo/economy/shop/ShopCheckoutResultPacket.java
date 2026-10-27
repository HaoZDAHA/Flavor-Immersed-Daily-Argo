package com.flavor_immersed_daily_argo.economy.shop;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.compat.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import com.flavor_immersed_daily_argo.compat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端 → 客户端：结账（或开店动作）的回执。
//
// 容器界面打开时原版不绘制 HUD，displayClientMessage(..., true) 的 actionBar 提示根本看不见，
// 会拖到关闭界面才一闪而过；所以把消息原样发回客户端，由 MarketScreen 画在窗口里。
// success 为真时客户端顺手清空购物车。
//
// success：是否成功
// message：要显示的文本
public record ShopCheckoutResultPacket(boolean success, Component message) implements CustomPacketPayload {

    public static final Type<ShopCheckoutResultPacket> TYPE = new Type<>(
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "shop_checkout_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopCheckoutResultPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ShopCheckoutResultPacket::success,
                    ComponentSerialization.STREAM_CODEC, ShopCheckoutResultPacket::message,
                    ShopCheckoutResultPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
