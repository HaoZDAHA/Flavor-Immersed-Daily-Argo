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
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.Optional;

// 昆虫培育箱配方。
//
// 一头连着「饲料 + 父代 + 载体」，另一头是「子代」：子代槽里放的东西在进度跑满后
// 就地转成产物。JSON 里四个字段各管一摊：
//
// feed：饲料槽要放什么（每次转化消耗一份）；
// carrier：载体槽要放什么（不消耗）；
// parents：写了就要求两个父代槽都放它（如蚕茧繁育），不写就要求父代槽全空；
// offspring_input：写了就要求子代槽里的东西全是它、且至少一格（如蚕卵→蚕），
// 产物会逐个替换掉这些格；不写就要求子代槽全空，产物把六格铺满（如蚕茧→蚕卵）；
// offspring_output：转化出的产物；
// time：转化耗时（刻），表现上和作物生长一样——进度跑满才出结果。
public class InsectBreedingRecipe implements Recipe<InsectBreedingInput> {

    private final ResourceLocation id;
    private final Ingredient feed;
    private final Ingredient carrier;
    private final Optional<Ingredient> parents;
    private final Optional<Ingredient> offspringInput;
    private final ItemStack offspringOutput;
    private final int time;

    public InsectBreedingRecipe(ResourceLocation id, Ingredient feed, Ingredient carrier, Optional<Ingredient> parents,
                                Optional<Ingredient> offspringInput, ItemStack offspringOutput, int time) {
        this.id = id;
        this.feed = feed;
        this.carrier = carrier;
        this.parents = parents;
        this.offspringInput = offspringInput;
        this.offspringOutput = offspringOutput;
        this.time = time;
    }

    public Ingredient getFeed() {
        return this.feed;
    }

    public Ingredient getCarrier() {
        return this.carrier;
    }

    public Optional<Ingredient> getParents() {
        return this.parents;
    }

    public Optional<Ingredient> getOffspringInput() {
        return this.offspringInput;
    }

// 子代槽是否需要先放着东西（true = 就地转化，false = 由空槽铺满产物）
    public boolean hasOffspringInput() {
        return this.offspringInput.isPresent();
    }

    public ItemStack getOffspringOutput() {
        return this.offspringOutput;
    }

// 转化耗时（刻）
    public int getTime() {
        return this.time;
    }

    @Override
    public boolean matches(InsectBreedingInput input, Level level) {
        if (input.feed().isEmpty() || !this.feed.test(input.feed())) {
            return false;
        }
        if (input.carrier().isEmpty() || !this.carrier.test(input.carrier())) {
            return false;
        }
// 父代：写了就要求两格都匹配，没写就要求两格都空着
        if (this.parents.isPresent()) {
            Ingredient parent = this.parents.get();
            if (input.parent1().isEmpty() || !parent.test(input.parent1())) {
                return false;
            }
            if (input.parent2().isEmpty() || !parent.test(input.parent2())) {
                return false;
            }
        } else if (!input.parent1().isEmpty() || !input.parent2().isEmpty()) {
            return false;
        }
// 子代：写了输入就要求非空的格子全部匹配（且至少一格），没写就要求全空
        boolean any = false;
        for (ItemStack stack : input.offspring()) {
            if (stack.isEmpty()) {
                continue;
            }
            any = true;
            if (this.offspringInput.isEmpty() || !this.offspringInput.get().test(stack)) {
                return false;
            }
        }
        return this.offspringInput.isEmpty() ? !any : any;
    }

    @Override
    public ItemStack assemble(InsectBreedingInput input, RegistryAccess registries) {
        return this.offspringOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return this.offspringOutput;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.feed);
        this.parents.ifPresent(list::add);
        list.add(this.carrier);
        this.offspringInput.ifPresent(list::add);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.INSECT_BREEDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.INSECT_BREEDING_TYPE.get();
    }

// 1.20.1 同步配方到客户端时用的是这个 ID，各条配方各用各的文件名，不能写死
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public static final RecipeSerializer<InsectBreedingRecipe> SERIALIZER = new RecipeSerializer<>() {

        @Override
        public InsectBreedingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient feed = Ingredient.fromJson(json.get("feed"));
            Ingredient carrier = Ingredient.fromJson(json.get("carrier"));
            Optional<Ingredient> parents = json.has("parents")
                    ? Optional.of(Ingredient.fromJson(json.get("parents")))
                    : Optional.empty();
            Optional<Ingredient> offspringInput = json.has("offspring_input")
                    ? Optional.of(Ingredient.fromJson(json.get("offspring_input")))
                    : Optional.empty();
            ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "offspring_output"), true);
            int time = GsonHelper.getAsInt(json, "time", 200);
            return new InsectBreedingRecipe(id, feed, carrier, parents, offspringInput, output, time);
        }

        @Override
        public InsectBreedingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient feed = Ingredient.fromNetwork(buffer);
            Ingredient carrier = Ingredient.fromNetwork(buffer);
            Optional<Ingredient> parents = buffer.readBoolean()
                    ? Optional.of(Ingredient.fromNetwork(buffer)) : Optional.empty();
            Optional<Ingredient> offspringInput = buffer.readBoolean()
                    ? Optional.of(Ingredient.fromNetwork(buffer)) : Optional.empty();
            ItemStack output = buffer.readItem();
            int time = buffer.readVarInt();
            return new InsectBreedingRecipe(id, feed, carrier, parents, offspringInput, output, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, InsectBreedingRecipe recipe) {
            recipe.feed.toNetwork(buffer);
            recipe.carrier.toNetwork(buffer);
            buffer.writeBoolean(recipe.parents.isPresent());
            recipe.parents.ifPresent(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeBoolean(recipe.offspringInput.isPresent());
            recipe.offspringInput.ifPresent(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeItem(recipe.offspringOutput);
            buffer.writeVarInt(recipe.time);
        }
    };
}
