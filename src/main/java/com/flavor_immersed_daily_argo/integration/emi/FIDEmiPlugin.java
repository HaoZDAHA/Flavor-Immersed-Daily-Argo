package com.flavor_immersed_daily_argo.integration.emi;

import com.flavor_immersed_daily_argo.all.ModBlocks;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.recipe.InsectBreedingRecipe;
import com.flavor_immersed_daily_argo.recipe.InsectNetDrop;
import com.flavor_immersed_daily_argo.recipe.ModRecipes;
import com.flavor_immersed_daily_argo.recipe.RiverSiltFarmingRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 本模组的 EMI 集成入口。
//
// 目前只挂昆虫培育箱的配方页；机器本身作为工作站注册，方便在 EMI 里按机器过滤。
// 分类标题的 lang 键由 EMI 按 emi.category.<命名空间>.<路径> 自动拼出。
@EmiEntrypoint
public final class FIDEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory INSECT_BREEDING =
            new EmiRecipeCategory(id("insect_breeding"), EmiStack.of(ModBlocks.INSECT_INCUBATOR.asItem()));

    public static final EmiRecipeCategory INSECT_NET =
            new EmiRecipeCategory(id("insect_net"), EmiStack.of(ModItems.INSECT_NET.get()));

    public static final EmiRecipeCategory RIVER_SILT_FARMING =
            new EmiRecipeCategory(id("river_silt_farming"), EmiStack.of(ModBlocks.RIVER_SILT.asItem()));

// fidworkblock 的命名空间与它那套共用配方类型
    private static final String WORKBLOCK_MODID = "fidworkblock";

// 需要补进 EMI 的机器：fidworkblock 自带的 EMI 插件只做了砧板的生物屠宰，其余全部由本模组登记
    private static final String[] COOKING_APPLIANCES =
            {"mortar", "squeezingmachine", "steambox", "wok", "pot", "jar", "pressurecooker",
                    "fryingpan", "choppingboard"};


    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, path);
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(INSECT_BREEDING);
        registry.addWorkstation(INSECT_BREEDING, EmiStack.of(ModBlocks.INSECT_INCUBATOR.asItem()));

        for (RecipeHolder<InsectBreedingRecipe> holder : registry.getRecipeManager()
                .getAllRecipesFor(ModRecipes.INSECT_BREEDING_TYPE.get())) {
            registry.addRecipe(new FIDInsectBreedingEmiRecipe(holder));
        }

// 捕虫：捕虫网破坏草 → 四种昆虫
        registry.addCategory(INSECT_NET);
        registry.addWorkstation(INSECT_NET, EmiStack.of(ModItems.INSECT_NET.get()));
        for (InsectNetDrop drop : InsectNetDrop.drops()) {
            registry.addRecipe(new FIDInsectNetEmiRecipe(drop));
        }

// 河道淤泥养殖：配方是「苗种 → 产物」，淤泥方块本身作为工作站
        registry.addCategory(RIVER_SILT_FARMING);
        registry.addWorkstation(RIVER_SILT_FARMING, EmiStack.of(ModBlocks.RIVER_SILT.asItem()));
        for (RecipeHolder<RiverSiltFarmingRecipe> holder : registry.getRecipeManager()
                .getAllRecipesFor(ModRecipes.RIVER_SILT_FARMING_TYPE.get())) {
            registry.addRecipe(new FIDRiverSiltEmiRecipe(holder));
        }
        registry.addRecipe(new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(ModBlocks.RIVER_SILT.asItem())),
                List.of(Component.translatable("jei.flavor_immersed_daily_argo.river_silt.info.1"),
                        Component.translatable("jei.flavor_immersed_daily_argo.river_silt.info.2"),
                        Component.translatable("jei.flavor_immersed_daily_argo.river_silt.info.3")),
                id("info/river_silt")));

        registerWorkblockCooking(registry);

// 蚕茧缫丝：父模组的木盆页面只有「输入 → 输出」，看不出必须先装纯净水，这里补一页说明
        registry.addRecipe(new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(ModItems.SILKWORM_COCOON.get()),
                        EmiStack.of(ModItems.RAW_SILKWORM_CHRYSALIS.get())),
                List.of(Component.translatable("jei.flavor_immersed_daily_argo.basin_silk.info.1"),
                        Component.translatable("jei.flavor_immersed_daily_argo.basin_silk.info.2"),
                        Component.translatable("jei.flavor_immersed_daily_argo.basin_silk.info.3")),
                id("info/basin_silk")));
    }

// 把 fidworkblock 的工作方块配方补进 EMI。
//
// fidworkblock 自带的 EMI 插件只注册了砧板，石磨、榨汁机、蒸笼、炒锅的配方
// 在 EMI 里查不到，所以这里按配方类型取出全部配方，再按 appliance 分组登记。
    private void registerWorkblockCooking(EmiRegistry registry) {
        RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(
                ResourceLocation.fromNamespaceAndPath(WORKBLOCK_MODID, "cooking"));
        if (type == null) {
            return; // 没装 fidworkblock，安静跳过
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        HolderLookup.Provider registries = minecraft.level.registryAccess();

        Map<String, EmiRecipeCategory> categories = new LinkedHashMap<>();
        for (String appliance : COOKING_APPLIANCES) {
            Item machine = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(WORKBLOCK_MODID, appliance));
            if (machine == Items.AIR) {
                continue;
            }
            EmiRecipeCategory category = new EmiRecipeCategory(id("cooking_" + appliance), EmiStack.of(machine));
            categories.put(appliance, category);
            registry.addCategory(category);
            registry.addWorkstation(category, EmiStack.of(machine));
        }
        if (categories.isEmpty()) {
            return;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        Collection<RecipeHolder<?>> recipes = (Collection) registry.getRecipeManager()
                .getAllRecipesFor((RecipeType) type);
        for (RecipeHolder<?> holder : recipes) {
            Recipe<?> recipe = holder.value();
            EmiRecipeCategory category = categories.get(FIDWorkblockEmiRecipe.applianceOf(recipe));
            if (category == null) {
                continue;
            }
            ItemStack result = recipe.getResultItem(registries);
            if (result.isEmpty()) {
                continue;
            }
            registry.addRecipe(new FIDWorkblockEmiRecipe(category, holder.id(), recipe, result));
        }
    }
}
