package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

// NeoForge 1.21 的 DeferredBlock：方块条目，额外实现 ItemLike（asItem 取方块对应的物品）。
public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemLike {

    public DeferredBlock(RegistryObject<T> registryObject, ResourceLocation id,
                         ResourceKey<? extends Registry<Block>> registryKey) {
        super(registryObject, id, registryKey);
    }

    public DeferredBlock(ResourceLocation id, ResourceKey<? extends Registry<Block>> registryKey,
                         Supplier<? extends T> lazy) {
        super(id, registryKey, lazy);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
