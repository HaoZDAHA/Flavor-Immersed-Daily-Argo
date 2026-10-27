package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.neoforged.neoforge.attachment.AttachmentType;

// NeoForge 自己的注册表键。1.20.1 里流体类型是 Forge 的注册表，
// 数据附件则根本没有注册表 —— 这里给一个占位键，注册退化成惰性。
public final class NeoForgeRegistries {

    public static final class Keys {

        public static final ResourceKey<Registry<net.minecraftforge.fluids.FluidType>> FLUID_TYPES =
                ForgeRegistries.Keys.FLUID_TYPES;

        public static final ResourceKey<Registry<AttachmentType<?>>> ATTACHMENT_TYPES =
                ResourceKey.createRegistryKey(new ResourceLocation("flavor_immersed_daily_argo", "attachment_types"));

        private Keys() {
        }
    }

    private NeoForgeRegistries() {
    }
}
