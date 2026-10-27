package com.flavor_immersed_daily_argo.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// 一条外卖订单。
//
// 只记 xz —— 起点与终点的 y 都是「该 xz 上地表最高处」，取货 / 交付时现算即可，不用存。
//
// id：订单编号，用来把「打包好的外卖」与目的地的配送点对上
// originX：出发地 x
// originZ：出发地 z
// destX：目的地 x
// destZ：目的地 z
// deadline：截止的游戏刻（超时作废）
// money：配送费（元）
public record DeliveryOrder(long id, int originX, int originZ, int destX, int destZ, long deadline, long money) {

    public static final Codec<DeliveryOrder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("id").forGetter(DeliveryOrder::id),
            Codec.INT.fieldOf("ox").forGetter(DeliveryOrder::originX),
            Codec.INT.fieldOf("oz").forGetter(DeliveryOrder::originZ),
            Codec.INT.fieldOf("dx").forGetter(DeliveryOrder::destX),
            Codec.INT.fieldOf("dz").forGetter(DeliveryOrder::destZ),
            Codec.LONG.fieldOf("deadline").forGetter(DeliveryOrder::deadline),
            Codec.LONG.fieldOf("money").forGetter(DeliveryOrder::money)
    ).apply(instance, DeliveryOrder::new));

    public static final StreamCodec<ByteBuf, DeliveryOrder> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

// 起点到终点的水平距离（格）
    public double distance() {
        double dx = (double) this.destX - this.originX;
        double dz = (double) this.destZ - this.originZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

// 剩余时间（游戏刻）
    public long remaining(long gameTime) {
        return Math.max(0L, this.deadline - gameTime);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("id", this.id);
        tag.putInt("ox", this.originX);
        tag.putInt("oz", this.originZ);
        tag.putInt("dx", this.destX);
        tag.putInt("dz", this.destZ);
        tag.putLong("deadline", this.deadline);
        tag.putLong("money", this.money);
        return tag;
    }

    public static DeliveryOrder fromTag(CompoundTag tag) {
        return new DeliveryOrder(
                tag.getLong("id"),
                tag.getInt("ox"), tag.getInt("oz"),
                tag.getInt("dx"), tag.getInt("dz"),
                tag.getLong("deadline"), tag.getLong("money"));
    }
}
