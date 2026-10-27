package com.flavor_immersed_daily_argo.compat;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

// 1.20.1 没有「带注册表的缓冲区」这一层，配方同步直接按普通缓冲区写。
// 源码里把它当类型用，这里给个同名子类让签名对得上。
//
// 放在本模组自己的包名下：net.minecraft.* 是 Minecraft 模块的包，
// 模组再往同一个包里塞类会和 minecraft 模块抢包，ModLauncher 直接拒绝启动。
public class RegistryFriendlyByteBuf extends FriendlyByteBuf {

    public RegistryFriendlyByteBuf(ByteBuf buffer) {
        super(buffer);
    }
}
