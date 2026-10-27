package com.flavor_immersed_daily_argo.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// 下发给接单界面的一条订单：订单本身 + 接单状态 + 是否已取货。
//
// 只有「已接单」的订单才会在出发地生成外卖袋、在目的地生成配送点。HUD 指南针据此决定
// 该指向出发地（还没取货）还是目的地（已经取到货）。
public record PhoneOrderView(DeliveryOrder order, boolean accepted, boolean pickedUp) {

    public static final Codec<PhoneOrderView> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DeliveryOrder.CODEC.fieldOf("order").forGetter(PhoneOrderView::order),
            Codec.BOOL.fieldOf("accepted").forGetter(PhoneOrderView::accepted),
            Codec.BOOL.fieldOf("picked_up").forGetter(PhoneOrderView::pickedUp)
    ).apply(instance, PhoneOrderView::new));

    public static final StreamCodec<ByteBuf, PhoneOrderView> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
