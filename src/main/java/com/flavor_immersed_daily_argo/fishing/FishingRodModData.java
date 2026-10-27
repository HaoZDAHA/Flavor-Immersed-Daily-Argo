package com.flavor_immersed_daily_argo.fishing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.flavor_immersed_daily_argo.compat.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public record FishingRodModData(String customName, List<ItemStack> mods) {

    public static final Codec<FishingRodModData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("customName", "").forGetter(FishingRodModData::customName),
// 空槽就是 ItemStack.EMPTY，1.20.1 的 ItemStack.CODEC 允许 air，存档不会因此保存失败
            ItemStack.CODEC.listOf().fieldOf("mods").forGetter(FishingRodModData::mods)
    ).apply(instance, FishingRodModData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FishingRodModData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeUtf(data.customName());
                buf.writeVarInt(data.mods().size());
                for (ItemStack mod : data.mods()) {
                    buf.writeItem(mod);
                }
            },
            buf -> {
                String name = buf.readUtf();
                int size = buf.readVarInt();
                List<ItemStack> mods = new ArrayList<>(Math.max(size, 0));
                for (int i = 0; i < size; i++) {
                    mods.add(buf.readItem());
                }
                return new FishingRodModData(name, List.copyOf(mods));
            });

    public static final FishingRodModData EMPTY = new FishingRodModData("",
            IntStream.range(0, 6).mapToObj(i -> ItemStack.EMPTY).toList());
}
