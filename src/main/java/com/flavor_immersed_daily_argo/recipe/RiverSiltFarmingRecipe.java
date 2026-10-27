package com.flavor_immersed_daily_argo.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import com.flavor_immersed_daily_argo.compat.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

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

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack result;
    private final int time;

    public RiverSiltFarmingRecipe(ResourceLocation id, Ingredient input, ItemStack result, int time) {
        this.id = id;
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
    public ItemStack assemble(SingleRecipeInput input, RegistryAccess registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
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

// 1.20.1 同步配方到客户端时用的是这个 ID，几条配方各用各的文件名，不能写死
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public static final RecipeSerializer<RiverSiltFarmingRecipe> SERIALIZER = new RecipeSerializer<>() {

        @Override
        public RiverSiltFarmingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(json.get("input"));
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            int time = GsonHelper.getAsInt(json, "time", 2400);
            return new RiverSiltFarmingRecipe(id, input, result, time);
        }

        @Override
        public RiverSiltFarmingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int time = buffer.readVarInt();
            return new RiverSiltFarmingRecipe(id, input, result, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RiverSiltFarmingRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.time);
        }
    };
}
