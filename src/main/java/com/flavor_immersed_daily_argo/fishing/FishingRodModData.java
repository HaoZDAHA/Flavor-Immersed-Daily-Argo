package com.flavor_immersed_daily_argo.fishing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public record FishingRodModData(String customName, List<ItemStack> mods) {

    public static final Codec<FishingRodModData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("customName", "").forGetter(FishingRodModData::customName),
// 必须用 OPTIONAL_CODEC：六个槽位里空槽就是 ItemStack.EMPTY，普通 ItemStack.CODEC 会因为
// 「Item must not be minecraft:air」直接让玩家存档保存失败
            ItemStack.OPTIONAL_CODEC.listOf(6, 6).fieldOf("mods").forGetter(FishingRodModData::mods)
    ).apply(instance, FishingRodModData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FishingRodModData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FishingRodModData::customName,
// 同样必须用 OPTIONAL_STREAM_CODEC：空槽位是空物品，普通 STREAM_CODEC 会让
// container_set_slot 同步包编码失败（Empty ItemStack not allowed）并把玩家踢下线
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), FishingRodModData::mods,
            FishingRodModData::new
    );

    public static final FishingRodModData EMPTY = new FishingRodModData("",
            IntStream.range(0, 6).mapToObj(i -> ItemStack.EMPTY).toList());
}
