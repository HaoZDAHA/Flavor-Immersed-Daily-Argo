package com.flavor_immersed_daily_argo.compat;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

// 1.21 用 ComponentSerialization.STREAM_CODEC 收发文本组件。
// 1.20.1 只有 JSON 形式的 Component.Serializer，这里包一层。
//
// 放在本模组自己的包名下，避免与 Minecraft 模块的 net.minecraft.network.chat 抢包。
public final class ComponentSerialization {

    public static final StreamCodec<ByteBuf, Component> STREAM_CODEC = StreamCodec.of(
            (buf, component) -> ((FriendlyByteBuf) buf).writeUtf(Component.Serializer.toJson(component)),
            buf -> Component.Serializer.fromJson(((FriendlyByteBuf) buf).readUtf()));

    private ComponentSerialization() {
    }
}
