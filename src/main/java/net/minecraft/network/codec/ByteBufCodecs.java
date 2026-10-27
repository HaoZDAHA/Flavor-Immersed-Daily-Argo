package net.minecraft.network.codec;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

// 1.21 的 ByteBufCodecs。1.20.1 只有 FriendlyByteBuf#writeXxx / readXxx，
// 这里给源码里用到的那些编解码器补一份 StreamCodec 包装。
public final class ByteBufCodecs {

    public static final StreamCodec<ByteBuf, Integer> VAR_INT =
            StreamCodec.of((buf, value) -> ((FriendlyByteBuf) buf).writeVarInt(value), buf -> ((FriendlyByteBuf) buf).readVarInt());

    public static final StreamCodec<ByteBuf, Long> VAR_LONG =
            StreamCodec.of((buf, value) -> ((FriendlyByteBuf) buf).writeVarLong(value), buf -> ((FriendlyByteBuf) buf).readVarLong());

    public static final StreamCodec<ByteBuf, Integer> INT =
            StreamCodec.of((buf, value) -> buf.writeInt(value), buf -> buf.readInt());

    public static final StreamCodec<ByteBuf, Long> LONG =
            StreamCodec.of((buf, value) -> buf.writeLong(value), buf -> buf.readLong());

    public static final StreamCodec<ByteBuf, Float> FLOAT =
            StreamCodec.of((buf, value) -> buf.writeFloat(value), buf -> buf.readFloat());

    public static final StreamCodec<ByteBuf, Double> DOUBLE =
            StreamCodec.of((buf, value) -> buf.writeDouble(value), buf -> buf.readDouble());

    public static final StreamCodec<ByteBuf, Boolean> BOOL =
            StreamCodec.of((buf, value) -> buf.writeBoolean(value), buf -> buf.readBoolean());

    public static final StreamCodec<ByteBuf, String> STRING_UTF8 =
            StreamCodec.of((buf, value) -> ((FriendlyByteBuf) buf).writeUtf(value), buf -> ((FriendlyByteBuf) buf).readUtf());

    public static final StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = StreamCodec.of(
            (buf, tag) -> ((FriendlyByteBuf) buf).writeNbt(tag),
            buf -> {
                CompoundTag tag = ((FriendlyByteBuf) buf).readNbt();
                return tag == null ? new CompoundTag() : tag;
            });

    private ByteBufCodecs() {
    }

    // 定长列表：元素编解码器自己写长度
    public static <T> StreamCodec.Collector<T, List<T>> list() {
        return size -> new ArrayList<>(Math.max(size, 0));
    }

    // 自定义集合：由调用方给出容器工厂
    public static <T, C extends Collection<T>> StreamCodec.Collector<T, C> collection(IntFunction<C> factory) {
        return size -> factory.apply(size);
    }

    // 带元素编解码器的集合：写 varint 长度 + 逐个元素
    @SuppressWarnings("unchecked")
    public static <B, T, C extends Collection<T>> StreamCodec<B, C> collection(
            IntFunction<C> factory, StreamCodec<? super B, T> elementCodec) {
        StreamCodec<B, T> element = (StreamCodec<B, T>) elementCodec;
        return StreamCodec.of((buf, values) -> {
            FriendlyByteBuf out = (FriendlyByteBuf) buf;
            out.writeVarInt(values.size());
            for (T value : values) {
                element.encode(buf, value);
            }
        }, buf -> {
            FriendlyByteBuf in = (FriendlyByteBuf) buf;
            int size = in.readVarInt();
            C values = factory.apply(size);
            for (int i = 0; i < size; i++) {
                values.add(element.decode(buf));
            }
            return values;
        });
    }

    // 可选的元素：先写一位「在不在」，在的话再写值
    @SuppressWarnings("unchecked")
    public static <B, T> StreamCodec<B, Optional<T>> optional(StreamCodec<? super B, T> elementCodec) {
        StreamCodec<B, T> element = (StreamCodec<B, T>) elementCodec;
        return StreamCodec.of((buf, value) -> {
            FriendlyByteBuf out = (FriendlyByteBuf) buf;
            out.writeBoolean(value.isPresent());
            value.ifPresent(present -> element.encode(buf, present));
        }, buf -> {
            FriendlyByteBuf in = (FriendlyByteBuf) buf;
            return in.readBoolean() ? Optional.of(element.decode(buf)) : Optional.empty();
        });
    }

    // 把 DFU 的 Codec 塞进网络：值先编码成 NBT 再落进缓冲区
    public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
        return StreamCodec.of((buf, value) -> {
            Tag tag = codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, message -> {
            });
            ((FriendlyByteBuf) buf).writeNbt((CompoundTag) tag);
        }, buf -> {
            CompoundTag tag = ((FriendlyByteBuf) buf).readNbt();
            return codec.parse(NbtOps.INSTANCE, tag == null ? new CompoundTag() : tag)
                    .getOrThrow(false, message -> {
                    });
        });
    }
}
