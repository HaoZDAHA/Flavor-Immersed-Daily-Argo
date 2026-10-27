package com.flavor_immersed_daily_argo.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

// 河道淤泥养殖配方。
//
// 一条配方就是「往含水淤泥里放什么苗种，能长出什么、要多久」：
//
// input：可播下的苗种（例如生海参）；
// result：成熟后右键收获的产物，count 决定一次收几份；
// time：从播下到成熟所需的刻数，默认 2400（两分钟）。
//
// 配方写在 data/flavor_immersed_daily_argo/recipe/river_silt_*.json，
// 附属模组照同样格式加文件即可扩展可养殖的水产；JEI / EMI 会自动读取。
public class RiverSiltFarmingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient input;
    private final ItemStack result;
    private final int time;

    public RiverSiltFarmingRecipe(Ingredient input, ItemStack result, int time) {
        this.input = input;
        this.result = result;
        this.time = time;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public ItemStack getResult() {
        return this.result;
    }

// 生长耗时（刻）
    public int getTime() {
        return this.time;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return !input.item().isEmpty() && this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.input);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.RIVER_SILT_FARMING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RIVER_SILT_FARMING_TYPE.get();
    }

    public static final MapCodec<RiverSiltFarmingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(RiverSiltFarmingRecipe::getInput),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(RiverSiltFarmingRecipe::getResult),
            Codec.INT.optionalFieldOf("time", 2400).forGetter(RiverSiltFarmingRecipe::getTime)
    ).apply(inst, RiverSiltFarmingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RiverSiltFarmingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, RiverSiltFarmingRecipe::getInput,
            ItemStack.STREAM_CODEC, RiverSiltFarmingRecipe::getResult,
            ByteBufCodecs.VAR_INT, RiverSiltFarmingRecipe::getTime,
            RiverSiltFarmingRecipe::new);
}
