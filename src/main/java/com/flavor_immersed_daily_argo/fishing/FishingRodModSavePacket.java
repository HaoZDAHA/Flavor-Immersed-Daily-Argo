package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端 → 服务端：保存改装页面。
//
// 槽位里的配件本身由容器菜单同步，服务端直接读自己那份菜单即可；这里只需要把玩家输入的自定义名称带上去。
// 服务端放进槽位的配件是「真物品」，只有服务端写入钓竿的组件才算真正装好，所以保存必须走服务端。
public record FishingRodModSavePacket(String customName) implements CustomPacketPayload {

    public static final Type<FishingRodModSavePacket> TYPE =
            new Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "fishing_rod_mod_save"));

    public static final StreamCodec<ByteBuf, FishingRodModSavePacket> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(FishingRodModSavePacket::new, FishingRodModSavePacket::customName);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
