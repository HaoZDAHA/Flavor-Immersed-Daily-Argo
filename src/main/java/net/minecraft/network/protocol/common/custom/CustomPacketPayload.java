package net.minecraft.network.protocol.common.custom;

import net.minecraft.resources.ResourceLocation;

// 1.21 的自定义数据包基类。1.20.1 走 Forge 的 SimpleChannel，
// 这里只保留源码里用到的两个东西：payload 接口与它的类型标记。
public interface CustomPacketPayload {

    Type<? extends CustomPacketPayload> type();

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }
}
