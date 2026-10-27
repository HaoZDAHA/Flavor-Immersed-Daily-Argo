package com.flavor_immersed_daily_argo.recipe;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 本模组的自定义配方类型与序列化器注册表。
//
// 目前只有昆虫培育箱的 insect_breeding：配方 JSON 放在
// data/flavor_immersed_daily_argo/recipe/ 下。
public final class ModRecipes {

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, flavor_immersed_daily_argo.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, flavor_immersed_daily_argo.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<InsectBreedingRecipe>> INSECT_BREEDING_TYPE =
            RECIPE_TYPES.register("insect_breeding", () -> new RecipeType<>() {
            });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InsectBreedingRecipe>> INSECT_BREEDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("insect_breeding",
                    () -> serializer(InsectBreedingRecipe.CODEC, InsectBreedingRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeType<?>, RecipeType<RiverSiltFarmingRecipe>> RIVER_SILT_FARMING_TYPE =
            RECIPE_TYPES.register("river_silt_farming", () -> new RecipeType<>() {
            });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RiverSiltFarmingRecipe>> RIVER_SILT_FARMING_SERIALIZER =
            RECIPE_SERIALIZERS.register("river_silt_farming",
                    () -> serializer(RiverSiltFarmingRecipe.CODEC, RiverSiltFarmingRecipe.STREAM_CODEC));

    private ModRecipes() {
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    private static <T extends Recipe<?>> RecipeSerializer<T> serializer(
            MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return new RecipeSerializer<>() {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodec;
            }
        };
    }
}
