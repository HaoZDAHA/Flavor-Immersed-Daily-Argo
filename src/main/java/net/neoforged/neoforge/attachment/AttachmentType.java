package net.neoforged.neoforge.attachment;

import com.flavor_immersed_daily_argo.compat.FidAttachmentSync;
import com.flavor_immersed_daily_argo.compat.NameAware;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

// NeoForge 1.21 的数据附件。1.20.1 没有这套系统，这里落到
// Entity#getPersistentData() 上：每个附件一个固定 NBT 键。
// 声明了 sync(...) 的附件由 FidAttachmentSync 定期推给附近客户端。
public class AttachmentType<T> implements NameAware {

    // 需要往客户端同步的附件（构造时自动登记）
    public static final List<AttachmentType<?>> SYNCED = new ArrayList<>();

    private final Supplier<? extends T> defaultValue;
    private final Codec<T> codec;
    private final Predicate<T> shouldSerialize;
    private final StreamCodec<?, T> syncCodec;
    private String name = "unknown";

    private AttachmentType(Supplier<? extends T> defaultValue, Codec<T> codec, Predicate<T> shouldSerialize,
                           StreamCodec<?, T> syncCodec) {
        this.defaultValue = defaultValue;
        this.codec = codec;
        this.shouldSerialize = shouldSerialize;
        this.syncCodec = syncCodec;
    }

    public static <T> Builder<T> builder(Supplier<? extends T> defaultValue) {
        return new Builder<>(defaultValue);
    }

    @Override
    public void fidSetName(String name) {
        this.name = name;
        if (this.syncCodec != null && !SYNCED.contains(this)) {
            SYNCED.add(this);
        }
    }

    // 附件名（等于注册名）
    public String key() {
        return this.name;
    }

    // 存档里用的 NBT 键
    public String dataKey() {
        return "fid_attachment_" + this.name;
    }

    public boolean isSynced() {
        return this.syncCodec != null;
    }

    public Codec<T> codec() {
        return this.codec;
    }

    @SuppressWarnings("unchecked")
    public T get(Entity entity) {
        T value = getOrNull(entity);
        return value != null ? value : (T) this.defaultValue.get();
    }

    @SuppressWarnings("unchecked")
    public T getOrNull(Entity entity) {
        if (entity == null) {
            return null;
        }
        Tag tag = rawTag(entity);
        if (tag == null) {
            return null;
        }
        return this.codec.parse(NbtOps.INSTANCE, tag).result().orElse(null);
    }

    public boolean has(Entity entity) {
        return rawTag(entity) != null;
    }

    public void set(Entity entity, T value) {
        if (entity == null) {
            return;
        }
        if (entity.level().isClientSide) {
            FidAttachmentSync.clientSet(entity.getId(), dataKey(), value == null ? null : encode(value));
            return;
        }
        if (value == null || (this.shouldSerialize != null && !this.shouldSerialize.test(value))) {
            remove(entity);
            return;
        }
        Tag tag = encode(value);
        if (tag == null) {
            remove(entity);
            return;
        }
        entity.getPersistentData().put(dataKey(), tag);
    }

    public void remove(Entity entity) {
        if (entity == null) {
            return;
        }
        entity.getPersistentData().remove(dataKey());
    }

    @SuppressWarnings("unchecked")
    private Tag rawTag(Entity entity) {
        if (entity.level().isClientSide) {
            return FidAttachmentSync.clientTag(entity.getId(), dataKey());
        }
        return entity.getPersistentData().get(dataKey());
    }

    private Tag encode(T value) {
        return this.codec.encodeStart(NbtOps.INSTANCE, value).result().orElse(null);
    }

    public static final class Builder<T> {

        private final Supplier<? extends T> defaultValue;
        private Codec<T> codec;
        private Predicate<T> shouldSerialize;
        private StreamCodec<?, T> syncCodec;

        private Builder(Supplier<? extends T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Builder<T> serialize(Codec<T> codec) {
            return serialize(codec, value -> true);
        }

        // 只有判定通过的值才写进存档
        public Builder<T> serialize(Codec<T> codec, Predicate<T> shouldSerialize) {
            this.codec = codec;
            this.shouldSerialize = shouldSerialize;
            return this;
        }

        public Builder<T> sync(StreamCodec<?, T> syncCodec) {
            this.syncCodec = syncCodec;
            return this;
        }

        public AttachmentType<T> build() {
            if (this.codec == null) {
                throw new IllegalStateException("附件必须声明 serialize(...)");
            }
            return new AttachmentType<>(this.defaultValue, this.codec, this.shouldSerialize, this.syncCodec);
        }
    }
}
