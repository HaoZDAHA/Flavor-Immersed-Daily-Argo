package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

// NeoForge 1.21 的 DeferredRegister 移植：真正的注册表交给 Forge 的 DeferredRegister，
// 1.20.1 里不存在的注册表（数据组件 / 数据附件）退化成惰性注册。
public class DeferredRegister<T> {

    protected final net.minecraftforge.registries.DeferredRegister<T> delegate;
    protected final ResourceKey<? extends Registry<T>> registryKey;
    protected final String modid;
    protected final Map<String, DeferredHolder<T, ?>> all = new LinkedHashMap<>();

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        this.registryKey = registryKey;
        this.modid = modid;
        this.delegate = registryExists(registryKey.location())
                ? net.minecraftforge.registries.DeferredRegister.create(registryKey, modid)
                : null;
    }

    private static boolean registryExists(ResourceLocation name) {
        if (BuiltInRegistries.REGISTRY.containsKey(name)) {
            return true;
        }
        return RegistryManager.ACTIVE.getRegistry(name) != null;
    }

    public static <B> DeferredRegister<B> create(ResourceKey<? extends Registry<B>> key, String modid) {
        return new DeferredRegister<>(key, modid);
    }

    public static Blocks createBlocks(String modid) {
        return new Blocks(modid);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        ResourceLocation id = new ResourceLocation(modid, name);
        DeferredHolder<T, I> holder = delegate != null
                ? new DeferredHolder<>(delegate.register(name, supplier), id, registryKey)
                : new DeferredHolder<>(id, registryKey, supplier);
        all.put(name, holder);
        return holder;
    }

    public void register(IEventBus bus) {
        if (delegate != null) {
            delegate.register(bus);
        }
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return registryKey;
    }

    public ResourceLocation getRegistryName() {
        return registryKey.location();
    }

    public String getModId() {
        return modid;
    }

    public static class Blocks extends DeferredRegister<Block> {

        public Blocks(String modid) {
            super(Registries.BLOCK, modid);
        }

        @Override
        public <I extends Block> DeferredBlock<I> register(String name, Supplier<? extends I> supplier) {
            ResourceLocation id = new ResourceLocation(modid, name);
            DeferredBlock<I> holder = delegate != null
                    ? new DeferredBlock<>(delegate.register(name, supplier), id, registryKey)
                    : new DeferredBlock<>(id, registryKey, supplier);
            all.put(name, holder);
            return holder;
        }
    }

    // Forge 的 DeferredRegister.register 返回 RegistryObject，这里只是个便利方法
    protected static <I> RegistryObject<I> registerRaw(net.minecraftforge.registries.DeferredRegister<I> register,
                                                      String name, Supplier<? extends I> supplier) {
        return register.register(name, supplier);
    }
}
