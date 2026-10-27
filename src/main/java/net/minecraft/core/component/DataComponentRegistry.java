package net.minecraft.core.component;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

// 数据组件在 1.20.1 里并不是注册表；这里伪造一个注册表键，
// 让 ModDataComponents 的 DeferredRegister 走「惰性注册」分支。
public final class DataComponentRegistry {

    public static final ResourceKey<Registry<DataComponentType<?>>> KEY =
            ResourceKey.createRegistryKey(new ResourceLocation("flavor_immersed_daily_argo", "data_component_type"));

    private DataComponentRegistry() {
    }
}
