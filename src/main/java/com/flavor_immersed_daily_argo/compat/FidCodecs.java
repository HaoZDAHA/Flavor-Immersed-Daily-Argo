package com.flavor_immersed_daily_argo.compat;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

// 1.21 里挂在 ItemStack 上的那几个编解码器在 1.20.1 没有，
// 这里用物品 NBT 表示，语义与原来一致（空堆叠按普通 / 可选两种方式处理）。
public final class FidCodecs {

    public static final Codec<ItemStack> STACK_CODEC =
            CompoundTag.CODEC.xmap(ItemStack::of, stack -> stack.save(new CompoundTag()));

    public static final Codec<ItemStack> OPTIONAL_STACK_CODEC = STACK_CODEC;

    public static final StreamCodec<ByteBuf, ItemStack> STACK_STREAM_CODEC = StreamCodec.of(
            (buf, stack) -> ((FriendlyByteBuf) buf).writeNbt(stack.save(new CompoundTag())),
            buf -> {
                CompoundTag tag = ((FriendlyByteBuf) buf).readNbt();
                return tag == null ? ItemStack.EMPTY : ItemStack.of(tag);
            });

    public static final StreamCodec<ByteBuf, ItemStack> OPTIONAL_STACK_STREAM_CODEC = STACK_STREAM_CODEC;

    private FidCodecs() {
    }

    // 1.21 的 ItemStack#copyWithCount：改数量但不丢组件（1.20.1 是改 NBT）
    public static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}
