package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.Supplier;

// NeoForge 1.21 的 DeferredHolder 在 1.20.1 Forge 里没有对应物，这里包一层 RegistryObject。
// 只有「真正的注册表」才走 RegistryObject；盔甲材质 / 数据组件 / 数据附件这类
// 1.20.1 没有注册表的对象走惰性 supplier。
public class DeferredHolder<R, T> implements Supplier<T> {

    private final ResourceLocation id;
    private final RegistryObject<T> registryObject;
    private final Supplier<? extends T> lazy;
    private final ResourceKey<? extends Registry<R>> registryKey;
    private T cached;

    public DeferredHolder(RegistryObject<T> registryObject, ResourceLocation id,
                          ResourceKey<? extends Registry<R>> registryKey) {
        this.registryObject = registryObject;
        this.id = id;
        this.registryKey = registryKey;
        this.lazy = null;
    }

    public DeferredHolder(ResourceLocation id, ResourceKey<? extends Registry<R>> registryKey,
                          Supplier<? extends T> lazy) {
        this.registryObject = null;
        this.id = id;
        this.registryKey = registryKey;
        this.lazy = lazy;
    }

    @Override
    public T get() {
        if (registryObject != null) {
            return registryObject.get();
        }
        if (cached == null) {
            T value = lazy.get();
            if (value instanceof com.flavor_immersed_daily_argo.compat.NameAware aware) {
                aware.fidSetName(id.getPath());
            }
            cached = value;
        }
        return cached;
    }

    public ResourceLocation getId() {
        return id;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResourceKey<T> getKey() {
        if (registryObject != null) {
            return registryObject.getKey();
        }
        return ResourceKey.create((ResourceKey) registryKey, id);
    }

    public boolean isPresent() {
        return registryObject != null ? registryObject.isPresent() : true;
    }

    public Optional<T> asOptional() {
        return isPresent() ? Optional.ofNullable(get()) : Optional.empty();
    }
}
