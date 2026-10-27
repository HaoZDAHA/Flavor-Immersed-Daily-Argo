package net.minecraft.core.component;

import com.flavor_immersed_daily_argo.compat.NameAware;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

// 1.20.1 没有数据组件系统。这里用物品 NBT 模拟：每个组件一个固定的 NBT 键，
// 值用组件自己的 Codec 编码成 NBT。物品堆叠的 NBT 会照常同步给客户端、写进存档，
// 所以行为上等价于 1.21 的「持久化 + 网络同步」组件。
public final class DataComponentType<T> implements NameAware {

    private final Codec<T> codec;
    private String name = "unknown";

    private DataComponentType(Codec<T> codec) {
        this.codec = codec;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    public void fidSetName(String name) {
        this.name = name;
    }

    private String key() {
        return "fid_component_" + name;
    }

    public boolean has(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(key());
    }

    public T get(ItemStack stack) {
        if (!has(stack)) {
            return null;
        }
        Tag tag = stack.getTag().get(key());
        return codec.parse(NbtOps.INSTANCE, tag).result().orElse(null);
    }

    public T getOrDefault(ItemStack stack, T fallback) {
        T value = get(stack);
        return value == null ? fallback : value;
    }

    public void set(ItemStack stack, T value) {
        if (value == null) {
            remove(stack);
            return;
        }
        Tag tag = codec.encodeStart(NbtOps.INSTANCE, value).result().orElse(null);
        if (tag == null) {
            remove(stack);
            return;
        }
        stack.getOrCreateTag().put(key(), tag);
    }

    public void remove(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(key());
        }
    }

    public static final class Builder<T> {

        private Codec<T> codec;

        public Builder<T> persistent(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        // 1.20.1 物品 NBT 本来就随物品同步，不需要单独的网络编解码器
        public Builder<T> networkSynchronized(Object streamCodec) {
            return this;
        }

        public DataComponentType<T> build() {
            return new DataComponentType<>(codec);
        }
    }
}
