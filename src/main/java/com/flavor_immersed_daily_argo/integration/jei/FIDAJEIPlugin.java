package com.flavor_immersed_daily_argo.integration.jei;

import com.flavor_immersed_daily_argo.all.ModBlocks;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookResource;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry;
import com.flavor_immersed_daily_argo.recipe.InsectBreedingRecipe;
import com.flavor_immersed_daily_argo.recipe.InsectNetDrop;
import com.flavor_immersed_daily_argo.recipe.ModRecipes;
import com.flavor_immersed_daily_argo.recipe.RiverSiltFarmingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class FIDAJEIPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "jei_plugin");

// 保存 JEI 运行时，供订单实体按 R 打开配方
    private static IJeiRuntime runtime;
    private static List<FishingHandbookEntry> fishingRecipes = List.of();

    public static void refreshFishingRecipes() {
        List<FishingHandbookEntry> latest = FishingHandbookResource.load();
        if (runtime == null || fishingRecipes == latest) {
            return;
        }
        runtime.getRecipeManager().hideRecipes(FishingHandbookJeiCategory.TYPE, fishingRecipes);
        runtime.getRecipeManager().addRecipes(FishingHandbookJeiCategory.TYPE, latest);
        fishingRecipes = latest;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        FIDAJEIPlugin.runtime = runtime;
        refreshFishingRecipes();
    }

    @Override
    public void onRuntimeUnavailable() {
        FIDAJEIPlugin.runtime = null;
    }

// 在 JEI 里打开这道菜作为产出的配方页；JEI 还没就绪时返回 false
    public static boolean showRecipes(ItemStack stack) {
        if (runtime == null || stack.isEmpty()) {
            return false;
        }
        IFocus<ItemStack> focus = runtime.getJeiHelpers().getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack);
        runtime.getRecipesGui().show(focus);
        return true;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new InsectBreedingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new InsectNetCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new RiverSiltFarmingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FishingHandbookJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                List.of(new ItemStack(ModItems.HARVESTER_SPAWN_EGG.get())),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.flavor_immersed_daily_argo.harvester_spawn_egg.description"));

// 蚕茧缫丝：父模组的木盆页面只有「输入 → 输出」，看不出必须先装纯净水，这里补一页说明
        registration.addIngredientInfo(
                List.of(new ItemStack(ModItems.SILKWORM_COCOON.get()),
                        new ItemStack(ModItems.RAW_SILKWORM_CHRYSALIS.get())),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.flavor_immersed_daily_argo.basin_silk.info.1"),
                Component.translatable("jei.flavor_immersed_daily_argo.basin_silk.info.2"),
                Component.translatable("jei.flavor_immersed_daily_argo.basin_silk.info.3"));

// 昆虫培育：直接读取培育箱的配方表
        List<InsectBreedingRecipe> breedingRecipes = new ArrayList<>();
        var level = Minecraft.getInstance().level;
        if (level != null) {
            for (var holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.INSECT_BREEDING_TYPE.get())) {
                breedingRecipes.add(holder.value());
            }
        }
        registration.addRecipes(InsectBreedingCategory.TYPE, breedingRecipes);

// 捕虫：捕虫网破坏草 → 四种昆虫
        registration.addRecipes(InsectNetCategory.TYPE, InsectNetDrop.drops());

// 河道淤泥：先泡在水下用饲料养成含水状态，才能放苗种；配方本身是「苗种 → 产物」
        registration.addIngredientInfo(
                List.of(new ItemStack(ModBlocks.RIVER_SILT.asItem())),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.flavor_immersed_daily_argo.river_silt.info.1"),
                Component.translatable("jei.flavor_immersed_daily_argo.river_silt.info.2"),
                Component.translatable("jei.flavor_immersed_daily_argo.river_silt.info.3"));
        List<RiverSiltFarmingRecipe> siltRecipes = new ArrayList<>();
        if (level != null) {
            for (var holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.RIVER_SILT_FARMING_TYPE.get())) {
                siltRecipes.add(holder.value());
            }
        }
        registration.addRecipes(RiverSiltFarmingCategory.TYPE, siltRecipes);

// 南山钓竿钓获：同步晚于注册时，由运行时接口补充。
        fishingRecipes = FishingHandbookResource.load();
        registration.addRecipes(FishingHandbookJeiCategory.TYPE, fishingRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INSECT_INCUBATOR.asItem()),
                InsectBreedingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.INSECT_NET.get()),
                InsectNetCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RIVER_SILT.asItem()),
                RiverSiltFarmingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.MOD_FISHING_ROD.get()),
                FishingHandbookJeiCategory.TYPE);
    }
}
