package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// 缝纫机状态同步（服务端 → 客户端）
//
// 机器上那件正在加工的空白丝绸装备不占普通槽位，所以它的内容（花纹像素 + 着装进来的护甲）
// 由这个包整份发给客户端；左右两个槽位走菜单自带的槽位同步，不在这里重复发。
public record SewingMachineSyncPacket(ItemStack garment) implements CustomPacketPayload {

    public static final Type<SewingMachineSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    flavor_immersed_daily_argo.MODID, "sewing_machine_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SewingMachineSyncPacket> STREAM_CODEC =
            StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, SewingMachineSyncPacket::garment,
                    SewingMachineSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
