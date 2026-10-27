package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry.Diet;
import com.flavor_immersed_daily_argo.fishing.SteelyardItem;
import com.flavor_immersed_daily_argo.economy.CreditCardItem;
import com.flavor_immersed_daily_argo.economy.MoneyItem;
import com.flavor_immersed_daily_argo.economy.NavPhoneItem;
import com.flavor_immersed_daily_argo.entity.TakeawayBagItem;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.AnimalFeedItem;
import com.flavor_immersed_daily_argo.item.AquaticFeedItem;
import com.flavor_immersed_daily_argo.item.BaitItem;
import com.flavor_immersed_daily_argo.item.DrinkItem;
import com.flavor_immersed_daily_argo.item.FishingHandbookItem;
import com.flavor_immersed_daily_argo.item.FishingFloatingPlatformItem;
import com.flavor_immersed_daily_argo.item.FishingNetItem;
import com.flavor_immersed_daily_argo.item.FishingRodPartItem;
import com.flavor_immersed_daily_argo.item.FireExtinguisherItem;
import com.flavor_immersed_daily_argo.item.FleaTreatmentItem;
import com.flavor_immersed_daily_argo.item.GuideBookItem;
import com.flavor_immersed_daily_argo.item.HandcartItem;
import com.flavor_immersed_daily_argo.item.HarrowItem;
import com.flavor_immersed_daily_argo.item.HarvesterSpawnEggItem;
import com.flavor_immersed_daily_argo.item.LivestockBrushItem;
import com.flavor_immersed_daily_argo.item.PoultryCatchingNetItem;
import com.flavor_immersed_daily_argo.item.RangerClipboardItem;
import com.flavor_immersed_daily_argo.item.JinkelaItem;
import com.flavor_immersed_daily_argo.item.MarshGasItem;
import com.flavor_immersed_daily_argo.item.HerbicideItem;
import com.flavor_immersed_daily_argo.item.MilkerItem;
import com.flavor_immersed_daily_argo.item.MilkDrinkItem;
import com.flavor_immersed_daily_argo.item.ModFishingRodItem;
import com.flavor_immersed_daily_argo.item.OrganicFertilizerItem;
import com.flavor_immersed_daily_argo.item.PoisonBaitStationItem;
import com.flavor_immersed_daily_argo.item.PoopItem;
import com.flavor_immersed_daily_argo.item.PoultryEggItem;
import com.flavor_immersed_daily_argo.item.ScarecrowItem;
import com.flavor_immersed_daily_argo.item.SeatCushionItem;
import com.flavor_immersed_daily_argo.item.SickleItem;
import com.flavor_immersed_daily_argo.item.StrawHatItem;
import com.flavor_immersed_daily_argo.item.StockWhipItem;
import com.flavor_immersed_daily_argo.item.SyringeItem;
import com.flavor_immersed_daily_argo.item.TooltipBlockItem;
import com.flavor_immersed_daily_argo.item.TooltipItem;
import com.flavor_immersed_daily_argo.item.TreePaintItem;
import com.flavor_immersed_daily_argo.item.UavRangeModuleItem;
import com.flavor_immersed_daily_argo.item.UavControllerItem;
import com.flavor_immersed_daily_argo.item.UavItem;
import com.flavor_immersed_daily_argo.item.RangingRopeItem;
import com.flavor_immersed_daily_argo.item.WateringCanItem;
import com.flavor_immersed_daily_argo.item.WaterLevelGaugeItem;
import com.flavor_immersed_daily_argo.item.WaterQualityDetectorItem;
import com.flavor_immersed_daily_argo.item.WaterTreatmentItem;
import com.flavor_immersed_daily_argo.item.WaterwheelItem;
import com.flavor_immersed_daily_argo.item.WhistleItem;
import com.flavor_immersed_daily_argo.textile.SilkArmorItem;
import com.flavor_immersed_daily_argo.textile.SilkItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

// 物品注册
public final class ModItems {

    private ModItems() {
    }

    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(Registries.ITEM, flavor_immersed_daily_argo.MODID);

// 生鲜海产的通用食用属性：恢复 2 点饥饿值，饱和度系数 0.3
    public static final FoodProperties RAW_SEAFOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.3F)
            .build();

// 生肉的通用食用属性：恢复 3 点饥饿值，饱和度系数 0.3（同原版生牛肉）
    public static final FoodProperties RAW_MEAT = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3F)
            .build();

// 熟肉的通用食用属性：恢复 6 点饥饿值，饱和度系数 0.6（同原版熟鸡肉）
    public static final FoodProperties COOKED_MEAT = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6F)
            .build();

// 烤鱼的通用食用属性：恢复 5 点饥饿值，饱和度系数 0.6（同原版熟鳕鱼）
    public static final FoodProperties COOKED_FISH = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.6F)
            .build();

// 小型鱼各部位的食用属性：聊胜于无，恢复 1 点饥饿值，几乎没有饱和度
    public static final FoodProperties RAW_FISH_PART = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1F)
            .build();

// 奶类的食用属性：清淡解渴，恢复 1 点饥饿值（数值同 flavor_immersed_daily 的牛奶瓶）
    public static final FoodProperties MILK_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.6F)
            .build();

// 野生狗奶：饱食度、饱和度都极高，另带一段抗性提升
    public static final FoodProperties WILD_DOG_MILK_FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0), 1.0F)
            .build();

// 粪便的食用属性：能垫一点肚子，但味道实在一言难尽。
// 必定反胃一阵子，还有一半几率闹得肚子翻江倒海、腿脚生风。
    public static final FoodProperties POOP_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.2F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 160, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0), 0.5F)
            .build();

// 刷怪蛋

    public static final DeferredHolder<Item, SpawnEggItem> DUCK_SPAWN_EGG =
            REGISTRY.register("duck_spawn_egg", () -> new SpawnEggItem(ModEntities.DUCK.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> GOOSE_SPAWN_EGG =
            REGISTRY.register("goose_spawn_egg", () -> new SpawnEggItem(ModEntities.GOOSE.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> QUAIL_SPAWN_EGG =
            REGISTRY.register("quail_spawn_egg", () -> new SpawnEggItem(ModEntities.QUAIL.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> PIGEON_SPAWN_EGG =
            REGISTRY.register("pigeon_spawn_egg", () -> new SpawnEggItem(ModEntities.PIGEON.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> TURKEY_SPAWN_EGG =
            REGISTRY.register("turkey_spawn_egg", () -> new SpawnEggItem(ModEntities.TURKEY.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> BULLFROG_SPAWN_EGG =
            REGISTRY.register("bullfrog_spawn_egg", () -> new SpawnEggItem(ModEntities.BULLFROG.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> BAMBOORAT_SPAWN_EGG =
            REGISTRY.register("bamboorat_spawn_egg", () -> new SpawnEggItem(ModEntities.BAMBOORAT.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> MOUSE_SPAWN_EGG =
            REGISTRY.register("mouse_spawn_egg", () -> new SpawnEggItem(ModEntities.MOUSE.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> GRASS_CARP_SPAWN_EGG =
            REGISTRY.register("grass_carp_spawn_egg", () -> new SpawnEggItem(ModEntities.GRASS_CARP.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> TENCH_SPAWN_EGG =
            REGISTRY.register("tench_spawn_egg", () -> new SpawnEggItem(ModEntities.TENCH.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> MANDARIN_FISH_SPAWN_EGG =
            REGISTRY.register("mandarin_fish_spawn_egg", () -> new SpawnEggItem(ModEntities.MANDARIN_FISH.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> LOACH_SPAWN_EGG =
            REGISTRY.register("loach_spawn_egg", () -> new SpawnEggItem(ModEntities.LOACH.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> PLECO_SPAWN_EGG =
            REGISTRY.register("pleco_spawn_egg", () -> new SpawnEggItem(ModEntities.PLECO.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> GOBY_SPAWN_EGG =
            REGISTRY.register("goby_spawn_egg", () -> new SpawnEggItem(ModEntities.GOBY.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> SILVER_CARP_SPAWN_EGG =
            REGISTRY.register("silver_carp_spawn_egg", () -> new SpawnEggItem(ModEntities.SILVER_CARP.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> TILAPIA_SPAWN_EGG =
            REGISTRY.register("tilapia_spawn_egg", () -> new SpawnEggItem(ModEntities.TILAPIA.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> REDFIN_SPAWN_EGG =
            REGISTRY.register("redfin_spawn_egg", () -> new SpawnEggItem(ModEntities.REDFIN.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> BLACK_CARP_SPAWN_EGG =
            REGISTRY.register("black_carp_spawn_egg", () -> new SpawnEggItem(ModEntities.BLACK_CARP.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> RECTUM_SPAWN_EGG =
            REGISTRY.register("rectum_spawn_egg", () -> new SpawnEggItem(ModEntities.RECTUM.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> BASS_SPAWN_EGG =
            REGISTRY.register("bass_spawn_egg", () -> new SpawnEggItem(ModEntities.BASS.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> CHINESE_CARP_SPAWN_EGG =
            REGISTRY.register("chinese_carp_spawn_egg", () -> new SpawnEggItem(ModEntities.CHINESE_CARP.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> CRUCIAN_CARP_SPAWN_EGG =
            REGISTRY.register("crucian_carp_spawn_egg", () -> new SpawnEggItem(ModEntities.CRUCIAN_CARP.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> CATFISH_SPAWN_EGG =
            REGISTRY.register("catfish_spawn_egg", () -> new SpawnEggItem(ModEntities.CATFISH.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> BIGHEAD_CARP_SPAWN_EGG =
            REGISTRY.register("bighead_carp_spawn_egg", () -> new SpawnEggItem(ModEntities.BIGHEAD_CARP.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> BLACK_FISH_SPAWN_EGG =
            REGISTRY.register("black_fish_spawn_egg", () -> new SpawnEggItem(ModEntities.BLACK_FISH.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> KING_CRAB_SPAWN_EGG =
            REGISTRY.register("king_crab_spawn_egg", () -> new SpawnEggItem(ModEntities.KING_CRAB.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> SWORDFISH_SPAWN_EGG =
            REGISTRY.register("swordfish_spawn_egg", () -> new SpawnEggItem(ModEntities.SWORDFISH.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> HAIRTAIL_SPAWN_EGG =
            REGISTRY.register("hairtail_spawn_egg", () -> new SpawnEggItem(ModEntities.HAIRTAIL.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> TUNA_SPAWN_EGG =
            REGISTRY.register("tuna_spawn_egg", () -> new SpawnEggItem(ModEntities.TUNA.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> BROWN_CRAB_SPAWN_EGG =
            REGISTRY.register("brown_crab_spawn_egg", () -> new SpawnEggItem(ModEntities.BROWN_CRAB.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> ANGLERFISH_SPAWN_EGG =
            REGISTRY.register("anglerfish_spawn_egg", () -> new SpawnEggItem(ModEntities.ANGLERFISH.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> CHINESE_MACKEREL_SPAWN_EGG =
            REGISTRY.register("chinese_mackerel_spawn_egg", () -> new SpawnEggItem(ModEntities.CHINESE_MACKEREL.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> EEL_SPAWN_EGG =
            REGISTRY.register("eel_spawn_egg", () -> new SpawnEggItem(ModEntities.EEL.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> LOBSTER_SPAWN_EGG =
            REGISTRY.register("lobster_spawn_egg", () -> new SpawnEggItem(ModEntities.LOBSTER.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> TURTLE_SPAWN_EGG =
            REGISTRY.register("turtle_spawn_egg", () -> new SpawnEggItem(ModEntities.TURTLE.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> HAIRY_CRAB_SPAWN_EGG =
            REGISTRY.register("hairy_crab_spawn_egg", () -> new SpawnEggItem(ModEntities.HAIRY_CRAB.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> PRAWN_SPAWN_EGG =
            REGISTRY.register("prawn_spawn_egg", () -> new SpawnEggItem(ModEntities.PRAWN.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> SWIMMING_CRAB_SPAWN_EGG =
            REGISTRY.register("swimming_crab_spawn_egg", () -> new SpawnEggItem(ModEntities.SWIMMING_CRAB.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> MUD_CRAB_SPAWN_EGG =
            REGISTRY.register("mud_crab_spawn_egg", () -> new SpawnEggItem(ModEntities.MUD_CRAB.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> SARDINE_SPAWN_EGG =
            REGISTRY.register("sardine_spawn_egg", () -> new SpawnEggItem(ModEntities.SARDINE.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> GROUPER_SPAWN_EGG =
            REGISTRY.register("grouper_spawn_egg", () -> new SpawnEggItem(ModEntities.GROUPER.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> PACIFIC_SAURY_SPAWN_EGG =
            REGISTRY.register("pacific_saury_spawn_egg", () -> new SpawnEggItem(ModEntities.PACIFIC_SAURY.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> SILVERY_POMFRET_SPAWN_EGG =
            REGISTRY.register("silvery_pomfret_spawn_egg", () -> new SpawnEggItem(ModEntities.SILVERY_POMFRET.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, SpawnEggItem> JAPANESE_HALFBEAK_SPAWN_EGG =
            REGISTRY.register("japanese_halfbeak_spawn_egg", () -> new SpawnEggItem(ModEntities.JAPANESE_HALFBEAK.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredHolder<Item, HarvesterSpawnEggItem> HARVESTER_SPAWN_EGG =
            REGISTRY.register("harvester_spawn_egg", () -> new HarvesterSpawnEggItem(new Item.Properties()));

// 生鲜海产

// 生草鱼
    public static final DeferredHolder<Item, Item> RAW_GRASS_CARP = tooltipFood("raw_grass_carp", RAW_SEAFOOD);

// 生丁桂鱼
    public static final DeferredHolder<Item, Item> RAW_TENCH = tooltipFood("raw_tench", RAW_SEAFOOD);

// 生鳜鱼
    public static final DeferredHolder<Item, Item> RAW_MANDARIN_FISH = tooltipFood("raw_mandarin_fish", RAW_SEAFOOD);

// 生黑鱼
    public static final DeferredHolder<Item, Item> RAW_BLACK_FISH = tooltipFood("raw_black_fish", RAW_SEAFOOD);

// 生鮰鱼
    public static final DeferredHolder<Item, Item> RAW_RECTUM = tooltipFood("raw_rectum", RAW_SEAFOOD);

// 生鲫鱼
    public static final DeferredHolder<Item, Item> RAW_CRUCIAN_CARP = tooltipFood("raw_crucian_carp", RAW_SEAFOOD);

// 生中华鲤鱼
    public static final DeferredHolder<Item, Item> RAW_CHINESE_CARP = tooltipFood("raw_chinese_carp", RAW_SEAFOOD);

// 生白鲢
    public static final DeferredHolder<Item, Item> RAW_SILVER_CARP = tooltipFood("raw_silver_carp", RAW_SEAFOOD);

// 生加州鲈
    public static final DeferredHolder<Item, Item> RAW_BASS = tooltipFood("raw_bass", RAW_SEAFOOD);

// 生非洲鲫鱼
    public static final DeferredHolder<Item, Item> RAW_TILAPIA = tooltipFood("raw_tilapia", RAW_SEAFOOD);

// 生泥鳅
    public static final DeferredHolder<Item, Item> RAW_LOACH = tooltipFood("raw_loach", RAW_SEAFOOD);

// 生鲶鱼
    public static final DeferredHolder<Item, Item> RAW_CATFISH = tooltipFood("raw_catfish", RAW_SEAFOOD);

// 生虾虎鱼
    public static final DeferredHolder<Item, Item> RAW_GOBY = tooltipFood("raw_goby", RAW_SEAFOOD);

// 生翘嘴红鲌
    public static final DeferredHolder<Item, Item> RAW_REDFIN = tooltipFood("raw_redfin", RAW_SEAFOOD);

// 生螺蛳青
    public static final DeferredHolder<Item, Item> RAW_BLACK_CARP = tooltipFood("raw_black_carp", RAW_SEAFOOD);

// 生多辐翼甲鲶
    public static final DeferredHolder<Item, Item> RAW_PLECO = tooltipFood("raw_pleco", RAW_SEAFOOD);

// 生花鲢
    public static final DeferredHolder<Item, Item> RAW_BIGHEAD_CARP = tooltipFood("raw_bighead_carp", RAW_SEAFOOD);

// 生大闸蟹
    public static final DeferredHolder<Item, Item> RAW_HAIRY_CRAB = tooltipFood("raw_hairy_crab", RAW_SEAFOOD);

// 生对虾
    public static final DeferredHolder<Item, Item> RAW_PRAWN = tooltipFood("raw_prawn", RAW_SEAFOOD);

// 生花蛤
    public static final DeferredHolder<Item, Item> RAW_CLAM = tooltipFood("raw_clam", RAW_SEAFOOD);

// 生中华鳖
    public static final DeferredHolder<Item, Item> RAW_TURTLE = tooltipFood("raw_turtle", RAW_SEAFOOD);

// 生鲅鱼（立体模型：可食用，也可放置成方块）
    public static final DeferredHolder<Item, Item> RAW_CHINESE_MACKEREL =
            REGISTRY.register("raw_chinese_mackerel", () -> new TooltipBlockItem(
                    ModBlocks.RAW_CHINESE_MACKEREL.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_chinese_mackerel"
            ));

// 生青蟹
    public static final DeferredHolder<Item, Item> RAW_MUD_CRAB = tooltipFood("raw_mud_crab", RAW_SEAFOOD);

// 生梭子蟹
    public static final DeferredHolder<Item, Item> RAW_SWIMMING_CRAB = tooltipFood("raw_swimming_crab", RAW_SEAFOOD);

// 生田螺
    public static final DeferredHolder<Item, Item> RAW_RIVER_SNAIL = tooltipFood("raw_river_snail", RAW_SEAFOOD);

// 生小龙虾
    public static final DeferredHolder<Item, Item> RAW_CRAYFISH = tooltipFood("raw_crayfish", RAW_SEAFOOD);

// 生银鲳
    public static final DeferredHolder<Item, Item> RAW_SILVERY_POMFRET = tooltipFood("raw_silvery_pomfret", RAW_SEAFOOD);

// 生带鱼（立体模型：可食用，也可放置成方块）
    public static final DeferredHolder<Item, Item> RAW_HAIRTAIL =
            REGISTRY.register("raw_hairtail", () -> new TooltipBlockItem(
                    ModBlocks.RAW_HAIRTAIL.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_hairtail"
            ));

// 生黄鳍金枪鱼（立体模型：可食用，也可放置成方块）
    public static final DeferredHolder<Item, Item> RAW_TUNA =
            REGISTRY.register("raw_tuna", () -> new TooltipBlockItem(
                    ModBlocks.RAW_TUNA.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_tuna"
            ));

// 生鳗鱼（立体模型：可食用，也可放置成方块）
    public static final DeferredHolder<Item, Item> RAW_EEL =
            REGISTRY.register("raw_eel", () -> new TooltipBlockItem(
                    ModBlocks.RAW_EEL.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_eel"
            ));

// 生鮟鱇鱼（立体模型：可食用，也可放置成方块）
    public static final DeferredHolder<Item, Item> RAW_ANGLERFISH =
            REGISTRY.register("raw_anglerfish", () -> new TooltipBlockItem(
                    ModBlocks.RAW_ANGLERFISH.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_anglerfish"
            ));

// 生阿拉斯加帝王蟹（立体模型：可食用，也可放置成方块）
    public static final DeferredHolder<Item, Item> RAW_KING_CRAB =
            REGISTRY.register("raw_king_crab", () -> new TooltipBlockItem(
                    ModBlocks.RAW_KING_CRAB.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_king_crab"
            ));

// 生美洲螯龙虾（立体模型：可食用，也可放置成方块）
    public static final DeferredHolder<Item, Item> RAW_LOBSTER =
            REGISTRY.register("raw_lobster", () -> new TooltipBlockItem(
                    ModBlocks.RAW_LOBSTER.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_lobster"
            ));

// 生面包蟹（立体模型：可食用，也可放置成方块）
    public static final DeferredHolder<Item, Item> RAW_BROWN_CRAB =
            REGISTRY.register("raw_brown_crab", () -> new TooltipBlockItem(
                    ModBlocks.RAW_BROWN_CRAB.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_brown_crab"
            ));

// 生剑鱼（立体模型：可食用，也可放置成方块；模型超出单方块范围，走方块实体渲染）
    public static final DeferredHolder<Item, Item> RAW_SWORDFISH =
            REGISTRY.register("raw_swordfish", () -> new TooltipBlockItem(
                    ModBlocks.RAW_SWORDFISH.get(),
                    new Item.Properties().food(RAW_SEAFOOD),
                    "tooltip.flavor_immersed_daily_argo.raw_swordfish"
            ));

// 生日本秋刀鱼
    public static final DeferredHolder<Item, Item> RAW_PACIFIC_SAURY = tooltipFood("raw_pacific_saury", RAW_SEAFOOD);

// 生沙丁鱼
    public static final DeferredHolder<Item, Item> RAW_SARDINE = tooltipFood("raw_sardine", RAW_SEAFOOD);

// 生老虎斑
    public static final DeferredHolder<Item, Item> RAW_GROUPER = tooltipFood("raw_grouper", RAW_SEAFOOD);

// 生马步鱼
    public static final DeferredHolder<Item, Item> RAW_JAPANESE_HALFBEAK = tooltipFood("raw_japanese_halfbeak", RAW_SEAFOOD);

// 生竹蛏
    public static final DeferredHolder<Item, Item> RAW_RAZOR_CLAM = tooltipFood("raw_razor_clam", RAW_SEAFOOD);

// 生仿刺参
    public static final DeferredHolder<Item, Item> RAW_SEA_CUCUMBER = tooltipFood("raw_sea_cucumber", RAW_SEAFOOD);

// 生单环刺螠
    public static final DeferredHolder<Item, Item> RAW_SPOON_WORM = tooltipFood("raw_spoon_worm", RAW_SEAFOOD);

// 生马粪海胆
    public static final DeferredHolder<Item, Item> RAW_SEA_URCHIN = tooltipFood("raw_sea_urchin", RAW_SEAFOOD);

// 生东风螺
    public static final DeferredHolder<Item, Item> RAW_SEA_SNAIL = tooltipFood("raw_sea_snail", RAW_SEAFOOD);

// 生沙海蜇
    public static final DeferredHolder<Item, Item> RAW_JELLYFISH = tooltipFood("raw_jellyfish", RAW_SEAFOOD);

// 生毛虾
    public static final DeferredHolder<Item, Item> RAW_AKIAMI_SHRIMP = tooltipFood("raw_akiami_shrimp", RAW_SEAFOOD);

// 生口虾蛄
    public static final DeferredHolder<Item, Item> RAW_MANTIS_SHRIMP = tooltipFood("raw_mantis_shrimp", RAW_SEAFOOD);

// 生威海扇贝
    public static final DeferredHolder<Item, Item> RAW_SCALLOP = tooltipFood("raw_scallop", RAW_SEAFOOD);

// 生乳山生蚝
    public static final DeferredHolder<Item, Item> RAW_OYSTER = tooltipFood("raw_oyster", RAW_SEAFOOD);

// 生土虾
    public static final DeferredHolder<Item, Item> RAW_MUD_SHRIMP = tooltipFood("raw_mud_shrimp", RAW_SEAFOOD);

// 生皱纹盘鲍
    public static final DeferredHolder<Item, Item> RAW_ABALONE = tooltipFood("raw_abalone", RAW_SEAFOOD);

// 桶装水产
// 与原版鱼桶同逻辑：手持空桶右键水里的鱼把它捞起，装鱼的桶右键水面把鱼放回去（发射器同理）

// 草鱼桶
    public static final DeferredHolder<Item, Item> GRASS_CARP_BUCKET =
            REGISTRY.register("grass_carp_bucket", () -> fishBucket(ModEntities.GRASS_CARP));

// 丁桂鱼桶
    public static final DeferredHolder<Item, Item> TENCH_BUCKET =
            REGISTRY.register("tench_bucket", () -> fishBucket(ModEntities.TENCH));

// 鳜鱼桶
    public static final DeferredHolder<Item, Item> MANDARIN_FISH_BUCKET =
            REGISTRY.register("mandarin_fish_bucket", () -> fishBucket(ModEntities.MANDARIN_FISH));

// 黑鱼桶
    public static final DeferredHolder<Item, Item> BLACK_FISH_BUCKET =
            REGISTRY.register("black_fish_bucket", () -> fishBucket(ModEntities.BLACK_FISH));

// 鮰鱼桶
    public static final DeferredHolder<Item, Item> RECTUM_BUCKET =
            REGISTRY.register("rectum_bucket", () -> fishBucket(ModEntities.RECTUM));

// 鲫鱼桶
    public static final DeferredHolder<Item, Item> CRUCIAN_CARP_BUCKET =
            REGISTRY.register("crucian_carp_bucket", () -> fishBucket(ModEntities.CRUCIAN_CARP));

// 鲤鱼桶
    public static final DeferredHolder<Item, Item> CHINESE_CARP_BUCKET =
            REGISTRY.register("chinese_carp_bucket", () -> fishBucket(ModEntities.CHINESE_CARP));

// 鲢鱼桶
    public static final DeferredHolder<Item, Item> SILVER_CARP_BUCKET =
            REGISTRY.register("silver_carp_bucket", () -> fishBucket(ModEntities.SILVER_CARP));

// 鲈鱼桶
    public static final DeferredHolder<Item, Item> BASS_BUCKET =
            REGISTRY.register("bass_bucket", () -> fishBucket(ModEntities.BASS));

// 罗非鱼桶
    public static final DeferredHolder<Item, Item> TILAPIA_BUCKET =
            REGISTRY.register("tilapia_bucket", () -> fishBucket(ModEntities.TILAPIA));

// 泥鳅桶
    public static final DeferredHolder<Item, Item> LOACH_BUCKET =
            REGISTRY.register("loach_bucket", () -> fishBucket(ModEntities.LOACH));

// 鲶鱼桶
    public static final DeferredHolder<Item, Item> CATFISH_BUCKET =
            REGISTRY.register("catfish_bucket", () -> fishBucket(ModEntities.CATFISH));

// 虾虎鱼桶
    public static final DeferredHolder<Item, Item> GOBY_BUCKET =
            REGISTRY.register("goby_bucket", () -> fishBucket(ModEntities.GOBY));

// 翘嘴桶
    public static final DeferredHolder<Item, Item> REDFIN_BUCKET =
            REGISTRY.register("redfin_bucket", () -> fishBucket(ModEntities.REDFIN));

// 青鱼桶
    public static final DeferredHolder<Item, Item> BLACK_CARP_BUCKET =
            REGISTRY.register("black_carp_bucket", () -> fishBucket(ModEntities.BLACK_CARP));

// 清道夫桶
    public static final DeferredHolder<Item, Item> PLECO_BUCKET =
            REGISTRY.register("pleco_bucket", () -> fishBucket(ModEntities.PLECO));

// 鳙鱼桶
    public static final DeferredHolder<Item, Item> BIGHEAD_CARP_BUCKET =
            REGISTRY.register("bighead_carp_bucket", () -> fishBucket(ModEntities.BIGHEAD_CARP));

// 大闸蟹桶
    public static final DeferredHolder<Item, Item> HAIRY_CRAB_BUCKET =
            REGISTRY.register("hairy_crab_bucket", () -> fishBucket(ModEntities.HAIRY_CRAB));

// 对虾桶
    public static final DeferredHolder<Item, Item> PRAWN_BUCKET =
            REGISTRY.register("prawn_bucket", () -> fishBucket(ModEntities.PRAWN));

// 甲鱼桶
    public static final DeferredHolder<Item, Item> TURTLE_BUCKET =
            REGISTRY.register("turtle_bucket", () -> fishBucket(ModEntities.TURTLE));

// 青蟹桶
    public static final DeferredHolder<Item, Item> MUD_CRAB_BUCKET =
            REGISTRY.register("mud_crab_bucket", () -> fishBucket(ModEntities.MUD_CRAB));

// 梭子蟹桶
    public static final DeferredHolder<Item, Item> SWIMMING_CRAB_BUCKET =
            REGISTRY.register("swimming_crab_bucket", () -> fishBucket(ModEntities.SWIMMING_CRAB));

// 鲳鱼桶
    public static final DeferredHolder<Item, Item> SILVERY_POMFRET_BUCKET =
            REGISTRY.register("silvery_pomfret_bucket", () -> fishBucket(ModEntities.SILVERY_POMFRET));

// 秋刀鱼桶
    public static final DeferredHolder<Item, Item> PACIFIC_SAURY_BUCKET =
            REGISTRY.register("pacific_saury_bucket", () -> fishBucket(ModEntities.PACIFIC_SAURY));

// 沙丁鱼桶
    public static final DeferredHolder<Item, Item> SARDINE_BUCKET =
            REGISTRY.register("sardine_bucket", () -> fishBucket(ModEntities.SARDINE));

// 石斑鱼桶
    public static final DeferredHolder<Item, Item> GROUPER_BUCKET =
            REGISTRY.register("grouper_bucket", () -> fishBucket(ModEntities.GROUPER));

// 马步鱼桶
    public static final DeferredHolder<Item, Item> JAPANESE_HALFBEAK_BUCKET =
            REGISTRY.register("japanese_halfbeak_bucket", () -> fishBucket(ModEntities.JAPANESE_HALFBEAK));

// 桶装鱼：内容是水，倒出来时按实体类型生成生物，与原版鳕鱼桶一致
    private static MobBucketItem fishBucket(Supplier<? extends EntityType<?>> type) {
        return new MobBucketItem(type.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH,
                new Item.Properties().stacksTo(1));
    }

// 大型鱼的生肉与部位（食用数值同生肉）

// 生鮟鱇鱼肝
    public static final DeferredHolder<Item, Item> RAW_ANGLERFISH_LIVER = food("raw_anglerfish_liver", RAW_MEAT);

// 生鲅鱼肉
    public static final DeferredHolder<Item, Item> RAW_MACKEREL_MEAT = food("raw_mackerel_meat", RAW_MEAT);

// 生带鱼段
    public static final DeferredHolder<Item, Item> RAW_HAIRTAIL_SEGMENT = food("raw_hairtail_segment", RAW_MEAT);

// 生剑鱼肉
    public static final DeferredHolder<Item, Item> RAW_SWORDFISH_MEAT = food("raw_swordfish_meat", RAW_MEAT);

// 生金枪鱼肉
    public static final DeferredHolder<Item, Item> RAW_TUNA_MEAT = food("raw_tuna_meat", RAW_MEAT);

// 生鳗鱼段
    public static final DeferredHolder<Item, Item> RAW_EEL_SEGMENT = food("raw_eel_segment", RAW_MEAT);


// 鱿鱼（砧板屠宰产物）

// 生鱿鱼：钓鱿鱼得到的整只鱿鱼，放上砧板用刀剁开（立体模型手持）
    public static final DeferredHolder<Item, Item> RAW_SQUID = tooltipFood("raw_squid", RAW_SEAFOOD);

// 生鱿鱼肉（← 砧板剁开生鱿鱼）
    public static final DeferredHolder<Item, Item> RAW_SQUID_MEAT = tooltipFood("raw_squid_meat", RAW_SEAFOOD);

// 生鱿鱼须（← 砧板剁开生鱿鱼）
    public static final DeferredHolder<Item, Item> RAW_SQUID_TENTACLE = tooltipFood("raw_squid_tentacle", RAW_SEAFOOD);

// 熟鱿鱼肉（← 生鱿鱼肉，熔炉 / 烟熏炉 / 营火）
    public static final DeferredHolder<Item, Item> COOKED_SQUID_MEAT = food("cooked_squid_meat", COOKED_MEAT);

// 熟鱿鱼须（← 生鱿鱼须，熔炉 / 烟熏炉 / 营火）
    public static final DeferredHolder<Item, Item> COOKED_SQUID_TENTACLE = food("cooked_squid_tentacle", COOKED_MEAT);

// 奶类
// 三种都用取奶器取对应动物获得，喝完还一只玻璃瓶

// 新鲜羊奶（← 取奶器取成年山羊）：同原版牛奶，喝下清除效果
    public static final DeferredHolder<Item, Item> FRESH_GOAT_MILK =
            REGISTRY.register("fresh_goat_milk", () -> new MilkDrinkItem(
                    new Item.Properties().food(MILK_FOOD), true));

// 新鲜骆驼奶（← 取奶器取成年骆驼）：同原版牛奶，喝下清除效果
    public static final DeferredHolder<Item, Item> CAMEL_MILK =
            REGISTRY.register("camel_milk", () -> new MilkDrinkItem(
                    new Item.Properties().food(MILK_FOOD), true));

// 野生狗奶（← 取奶器取野生狼，取完狼会被激怒）：紫色品质，抗性提升 + 大量饱食度 / 饱和度
    public static final DeferredHolder<Item, Item> WILD_DOG_MILK =
            REGISTRY.register("wild_dog_milk", () -> new MilkDrinkItem(
                    new Item.Properties().food(WILD_DOG_MILK_FOOD).rarity(Rarity.EPIC), false));

// 畜禽饲养

// 五种禽类的蛋：机制同原版鸡蛋——右键掷出后按蛋的注册名孵出对应禽类（见 item/PoultryEggItem、herding/CoopEggs），
// 同时会被鸡窝认作可孵化 / 可收集的蛋

// 鸭蛋
    public static final DeferredHolder<Item, Item> DUCK_EGG =
            REGISTRY.register("duck_egg", () -> new PoultryEggItem(new Item.Properties().stacksTo(16)));

// 鹅蛋
    public static final DeferredHolder<Item, Item> GOOSE_EGG =
            REGISTRY.register("goose_egg", () -> new PoultryEggItem(new Item.Properties().stacksTo(16)));

// 鹌鹑蛋
    public static final DeferredHolder<Item, Item> QUAIL_EGG =
            REGISTRY.register("quail_egg", () -> new PoultryEggItem(new Item.Properties().stacksTo(16)));

// 鸽子蛋
    public static final DeferredHolder<Item, Item> PIGEON_EGG =
            REGISTRY.register("pigeon_egg", () -> new PoultryEggItem(new Item.Properties().stacksTo(16)));

// 火鸡蛋
    public static final DeferredHolder<Item, Item> TURKEY_EGG =
            REGISTRY.register("turkey_egg", () -> new PoultryEggItem(new Item.Properties().stacksTo(16)));

// 小型鱼屠宰产物
// 除鱼骨、鱼鳞外均为可生食的部位，食用属性统一走 RAW_FISH_PART（数值极小）

// 鱼头
    public static final DeferredHolder<Item, Item> RAW_FISH_HEAD = food("raw_fish_head", RAW_FISH_PART);

// 鱼泡
    public static final DeferredHolder<Item, Item> RAW_FISH_SWIM_BLADDER = food("raw_fish_swim_bladder", RAW_FISH_PART);

// 鱼皮
    public static final DeferredHolder<Item, Item> RAW_FISH_SKIN = food("raw_fish_skin", RAW_FISH_PART);

// 鱼籽
    public static final DeferredHolder<Item, Item> RAW_FISH_ROE = food("raw_fish_roe", RAW_FISH_PART);

// 鱼肝
    public static final DeferredHolder<Item, Item> RAW_FISH_LIVER = food("raw_fish_liver", RAW_FISH_PART);

// 鱼骨（非食物）
    public static final DeferredHolder<Item, Item> FISH_BONE = item("fish_bone");

// 鱼鳞（非食物）
    public static final DeferredHolder<Item, Item> FISH_SCALE = item("fish_scale");

// 鸡窝（方块物品，手持为 16×16 平面贴图）
    public static final DeferredHolder<Item, Item> CHICKEN_COOP =
            REGISTRY.register("chicken_coop", () -> new BlockItem(ModBlocks.CHICKEN_COOP.get(), new Item.Properties()));

// 牧场食槽（方块物品，手持为 16×16 平面贴图）
    public static final DeferredHolder<Item, Item> FEEDING_TROUGH =
            REGISTRY.register("feeding_trough", () -> new TooltipBlockItem(
                    ModBlocks.FEEDING_TROUGH.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.feeding_trough"
            ));

// 河道淤泥（方块物品）：铺在水底，先用饲料养成含水状态，再放苗种养殖水产
    public static final DeferredHolder<Item, Item> RIVER_SILT =
            REGISTRY.register("river_silt", () -> new TooltipBlockItem(
                    ModBlocks.RIVER_SILT.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.river_silt"
            ));

// 晒盐场（方块物品）：放在水边让顶面过一遍水，之后靠日晒一级级结出盐；敲掉时按阶段掉盐
    public static final DeferredHolder<Item, Item> SALT_PAN =
            REGISTRY.register("salt_pan", () -> new TooltipBlockItem(
                    ModBlocks.SALT_PAN.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.salt_pan"
            ));

// 草谷饲料：繁殖猪、牛、绵羊、山羊、马、驴、兔子、骆驼、羊驼、竹鼠、牛蛙
    public static final DeferredHolder<Item, Item> FEED_GRASS =
            REGISTRY.register("feed_grass", () -> new AnimalFeedItem(new Item.Properties(), AnimalFeedItem.GRASS_TARGETS));

// 稻谷饲料：繁殖鸡、鸭、鹅、鹌鹑、鸽子、火鸡
    public static final DeferredHolder<Item, Item> FEED_SEED =
            REGISTRY.register("feed_seed", () -> new AnimalFeedItem(new Item.Properties(), AnimalFeedItem.SEED_TARGETS));

// 水产饲料：右键给周围 30 格内的水产生物附加 5 分钟产卵 buff
    public static final DeferredHolder<Item, Item> FEED_AQUATIC =
            REGISTRY.register("feed_aquatic", () -> new AquaticFeedItem(new Item.Properties(), AquaticFeedItem.AQUATIC_TARGETS));

// 鲜肉饲料：繁殖猫、狗
    public static final DeferredHolder<Item, Item> FEED_MEAT =
            REGISTRY.register("feed_meat", () -> new AnimalFeedItem(new Item.Properties(), AnimalFeedItem.MEAT_TARGETS));

// 编制麻袋：装禽类（不分大小）与其他动物的幼崽，装袋后换成 full 贴图
    public static final DeferredHolder<Item, Item> POULTRY_CATCHING_NET =
            REGISTRY.register("poultry_catching_net", () -> new PoultryCatchingNetItem(new Item.Properties().stacksTo(1)));

// 挤奶器：右键成年牛挤出 2~5 瓶 flavor_immersed_daily:milkbottle，每次消耗牛 1 点心情值
    public static final DeferredHolder<Item, Item> MILKER =
            REGISTRY.register("milker", () -> new MilkerItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.milker"
            ));

// 粪便：动物饱食度消耗时拉下的产物。右键可食用（垫肚子但反胃），潜行右键则扔出去砸人
    public static final DeferredHolder<Item, Item> POOP =
            REGISTRY.register("poop", () -> new PoopItem(
                    new Item.Properties().food(POOP_FOOD),
                    "tooltip.flavor_immersed_daily_argo.poop"
            ));

// 化粪发酵桶（两格宽的方块，方块物品手持为 16×16 平面贴图）：自动收粪便，攒着慢慢发酵成有机肥料
    public static final DeferredHolder<Item, Item> SEPTIC_TANK =
            REGISTRY.register("septic_tank", () -> new TooltipBlockItem(
                    ModBlocks.SEPTIC_TANK.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.septic_tank"
            ));

// 沼气罐（方块物品）：架在化粪发酵桶上方，化粪桶每发酵成功一次就存进一份加压沼气
    public static final DeferredHolder<Item, Item> METHANE_TANK =
            REGISTRY.register("methane_tank", () -> new TooltipBlockItem(
                    ModBlocks.METHANE_TANK.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.methane_tank"
            ));

// 宠物窝（方块物品）：驯服后的猫狗会寻路过来安家，过世后次日在此复活；空手右键宠物可切换跟随/蹲下/游荡
    public static final DeferredHolder<Item, Item> PET_NEST =
            REGISTRY.register("pet_nest", () -> new TooltipBlockItem(
                    ModBlocks.PET_NEST.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.pet_nest"
            ));

// 加压沼气：化粪桶发酵的副产物。可作燃料（烧 8 个物品），右键扔出去会爆开（不炸方块、只伤生物）
    public static final DeferredHolder<Item, Item> MARSH_GAS =
            REGISTRY.register("marsh_gas", () -> new MarshGasItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.marsh_gas"
            ));

// 针管：攻击家畜抽一份遗传液（变「筒装生物遗传液」），再攻击状态正好的家畜让它怀上
    public static final DeferredHolder<Item, Item> SYRINGE =
            REGISTRY.register("syringe", () -> new SyringeItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.syringe"
            ));

// 刷毛梳子：右键动物做出刷毛动作，慢慢地给心情值和清洁度加分
    public static final DeferredHolder<Item, Item> LIVESTOCK_BRUSH =
            REGISTRY.register("livestock_brush", () -> new LivestockBrushItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.livestock_brush"
            ));

// 牧鞭：右键动物抡一道横扫，把附近的同类都往前赶
    public static final DeferredHolder<Item, Item> STOCK_WHIP =
            REGISTRY.register("stock_whip", () -> new StockWhipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.stock_whip"
            ));

// 哨子：吹响后附近游荡的宠物转为跟随，坐骑循声走向玩家
    public static final DeferredHolder<Item, Item> WHISTLE =
            REGISTRY.register("whistle", () -> new WhistleItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.whistle"
            ));

// 体外驱虫药：右键动物消耗一支，清洁度顶满并洗掉「蝇虫叮咬」
    public static final DeferredHolder<Item, Item> FLEA_TREATMENT =
            REGISTRY.register("flea_treatment", () -> new FleaTreatmentItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.flea_treatment"
            ));

// 工具与杂项

// 东篱钓竿（无耐久限制）
    public static final DeferredHolder<Item, ModFishingRodItem> MOD_FISHING_ROD =
            REGISTRY.register("fishing_rod", () -> new ModFishingRodItem(new Item.Properties()));

// 抄网
    public static final DeferredHolder<Item, FishingNetItem> FISHING_NET =
            REGISTRY.register("fishing_net", () -> new FishingNetItem(new Item.Properties().durability(128)));

// 鱼护（方块物品）：可含水的网笼，抄网抄到的活鱼养在里面，空手右键放一条出来
    public static final DeferredHolder<Item, Item> FISH_PROTECTION =
            REGISTRY.register("fish_protection", () -> new TooltipBlockItem(
                    ModBlocks.FISH_PROTECTION.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.fish_protection"
            ));

// 钓鱼箱（方块物品）：36 格容器，没拿抄网时钓到的东西都会收进来
    public static final DeferredHolder<Item, Item> FISHING_BOX =
            REGISTRY.register("fishing_box", () -> new TooltipBlockItem(
                    ModBlocks.FISHING_BOX.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.fishing_box"
            ));

// 竿架（方块物品）：把南山钓竿架上去替人守竿，咬钩后提醒最近的玩家
    public static final DeferredHolder<Item, Item> ROD_POD =
            REGISTRY.register("rod_pod", () -> new TooltipBlockItem(
                    ModBlocks.ROD_POD.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.rod_pod"
            ));

// 钓鱼展示架（方块物品）：手持钓上来的水产右键挂上去，渲染那条鱼的活体实体模型
    public static final DeferredHolder<Item, Item> FISH_DISPLAY_BOARD =
            REGISTRY.register("fish_display_board", () -> new TooltipBlockItem(
                    ModBlocks.FISH_DISPLAY_BOARD.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.fish_display_board"
            ));

// 鱼缸（方块物品）：没有底的玻璃缸，可含水，相邻的会自动连成一片
    public static final DeferredHolder<Item, Item> FISH_BOWL =
            REGISTRY.register("fish_bowl", () -> new TooltipBlockItem(
                    ModBlocks.FISH_BOWL.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.fish_bowl"
            ));

// 杆秤：右键打开称重界面，放上去的东西按数据包里的分布抽一个重量
    public static final DeferredHolder<Item, Item> STEELYARD =
            REGISTRY.register("steelyard", () -> new SteelyardItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.steelyard"
            ));

// 镰刀：右键收割周围 3×3 内的成熟作物并补种，支持时运
    public static final DeferredHolder<Item, Item> SICKLE =
            REGISTRY.register("sickle", () -> new SickleItem(
                    new Item.Properties().durability(1024),
                    "tooltip.flavor_immersed_daily_argo.sickle"
            ));

// 洒水壶：耐久即水量（最高 128、最低 1、永不损毁），右键水源打水，长按右键浇水催熟
    public static final DeferredHolder<Item, Item> WATERING_CAN =
            REGISTRY.register("watering_can", () -> new WateringCanItem(
                    new Item.Properties().durability(128),
                    "tooltip.flavor_immersed_daily_argo.watering_can"
            ));

// 耙子：右键一次把周围 3×3 的土地耕成耕地，整次只消耗 1 点耐久
    public static final DeferredHolder<Item, Item> HARROW =
            REGISTRY.register("harrow", () -> new HarrowItem(
                    new Item.Properties().durability(512),
                    "tooltip.flavor_immersed_daily_argo.harrow"
            ));

// 草帽：戴在头上（占头部槽位），收获农作物时有概率让时运 +1
    public static final DeferredHolder<Item, Item> STRAW_HAT =
            REGISTRY.register("straw_hat", () -> new StrawHatItem(
                    new Item.Properties().stacksTo(1),
                    "tooltip.flavor_immersed_daily_argo.straw_hat"
            ));

// 肥沃耕地（方块物品）：继承原版耕地全部行为，随机刻时把上方作物催一个生长阶段
    public static final DeferredHolder<Item, Item> FERTILE_FARMLAND =
            REGISTRY.register("fertile_farmland", () -> new BlockItem(
                    ModBlocks.FERTILE_FARMLAND.get(),
                    new Item.Properties()
            ));

// 灌溉水车（方块物品）：立在水源上方就是一座三格高的水源，可灌溉、可打水
    public static final DeferredHolder<Item, Item> WATERWHEEL =
            REGISTRY.register("waterwheel", () -> new WaterwheelItem(
                    ModBlocks.WATERWHEEL.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.waterwheel"
            ));

// 水管（方块物品）：放下后自动与相邻水管拼接，竖直、拐弯、三通、四通都能自己认
    public static final DeferredHolder<Item, Item> WATER_PIPE =
            REGISTRY.register("water_pipe", () -> new TooltipBlockItem(
                    ModBlocks.WATER_PIPE.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.water_pipe"
            ));

// 水泵（方块物品）：埋进水里，与它相连的水管就会通水
    public static final DeferredHolder<Item, Item> WATER_PUMP =
            REGISTRY.register("water_pump", () -> new TooltipBlockItem(
                    ModBlocks.WATER_PUMP.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.water_pump"
            ));

// 加料阀（方块物品）：串在管路中间，投一颗金坷垃进去，整张网喷出去的水就带上一点随机催熟
    public static final DeferredHolder<Item, Item> DOSING_TUBE =
            REGISTRY.register("dosing_tube", () -> new TooltipBlockItem(
                    ModBlocks.DOSING_TUBE.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dosing_tube"
            ));

// 旋转水管（方块物品）：管网末端的旋转洒水器，通水后转着圈喷水浇地
    public static final DeferredHolder<Item, Item> ROTATING_WATER_PIPE =
            REGISTRY.register("rotating_water_pipe", () -> new TooltipBlockItem(
                    ModBlocks.ROTATING_WATER_PIPE.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.rotating_water_pipe"
            ));

// 扇形水管（方块物品）：管网末端的扇形洒水器，通水后朝正面水平扇形喷水浇地
    public static final DeferredHolder<Item, Item> SECTOR_WATER_PIPE =
            REGISTRY.register("sector_water_pipe", () -> new TooltipBlockItem(
                    ModBlocks.SECTOR_WATER_PIPE.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.sector_water_pipe"
            ));

// 捕捞渔网（方块物品）：放进水里当渔网，会随机刷出淡水/海水水产活体
    public static final DeferredHolder<Item, Item> FISH_NET =
            REGISTRY.register("fish_net", () -> new TooltipBlockItem(
                    ModBlocks.FISH_NET.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.fish_net"
            ));

// 稻草人：对着方块右键插进田里，护住周围 8 格内的耕地不被踩坏
    public static final DeferredHolder<Item, Item> SCARECROW =
            REGISTRY.register("scarecrow", () -> new ScarecrowItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.scarecrow"
            ));

// 毒饵站：对着稻草人右键放下，罩住周围 16 格不让老鼠生成；本身不会损坏
    public static final DeferredHolder<Item, Item> POISON_BAIT_STATION =
            REGISTRY.register("poison_bait_station", () -> new PoisonBaitStationItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.poison_bait_station"
            ));

// 拉力木车：对着方块右键放下，右键木车就能挂在玩家身后跟着走，潜行松开
    public static final DeferredHolder<Item, Item> HANDCART =
            REGISTRY.register("handcart", () -> new HandcartItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.handcart"
            ));

// 充气钓鱼浮台：对着水面长按右键蓄力，拉满松手就在水上撑开一块 5×5 的浮台
    public static final DeferredHolder<Item, Item> FISHING_FLOATING_PLATFORM =
            REGISTRY.register("fishing_floating_platform", () -> new FishingFloatingPlatformItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.fishing_floating_platform"
            ));

// 多功能无人机：对着方块右键放下，刚放下没有动作
    public static final DeferredHolder<Item, Item> UAV =
            REGISTRY.register("uav", () -> new UavItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.uav"
            ));

// 无人机遥控器：右键无人机绑定，绑定后随便右键都能打开操作界面
    public static final DeferredHolder<Item, Item> UAV_CONTROLLER =
            REGISTRY.register("uav_controller", () -> new UavControllerItem(new Item.Properties()));

// 无人机模块：放进无人机「模组槽」里的五选一

// 无人机看护模块：右键两个方块记录工作范围，放进模组槽就按记录的两点起飞、常驻干活
    public static final DeferredHolder<Item, Item> UAV_MODULE_CARE =
            REGISTRY.register("uav_module_care", () -> new UavRangeModuleItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.uav_module_care"
            ));

// 无人机战斗模块
    public static final DeferredHolder<Item, Item> UAV_MODULE_COMBAT = tooltipItem("uav_module_combat");

// 无人机收获模块：同样右键两个方块记录工作范围，放进模组槽就常驻收割、把收获物送进范围内的容器
    public static final DeferredHolder<Item, Item> UAV_MODULE_HARVEST =
            REGISTRY.register("uav_module_harvest", () -> new UavRangeModuleItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.uav_module_harvest"
            ));

// 无人机运输模块
    public static final DeferredHolder<Item, Item> UAV_MODULE_TRANSPORT = tooltipItem("uav_module_transport");

// 无人机高亮标记模块
    public static final DeferredHolder<Item, Item> UAV_MODULE_HIGHLIGHT = tooltipItem("uav_module_highlight");

// 无人机模块：无人机的「模组槽」只收这里面的东西，五选一
    public static final List<DeferredHolder<Item, Item>> UAV_MODULES = List.of(
            UAV_MODULE_CARE, UAV_MODULE_COMBAT, UAV_MODULE_HARVEST, UAV_MODULE_TRANSPORT, UAV_MODULE_HIGHLIGHT);

// 纸箱：运输模式下无人机「使用槽」里装的东西，无人机把它连货一起投到指定坐标
    public static final DeferredHolder<Item, Item> CARTON = item("carton");

// 钓鱼手册：查看南山钓竿可钓到的战利品图鉴
    public static final DeferredHolder<Item, FishingHandbookItem> FISHING_HANDBOOK =
            REGISTRY.register("fishing_handbook", () -> new FishingHandbookItem(new Item.Properties()));

// 南山-教程卷轴：右键打开帕秋莉手册（未安装帕秋莉时给出提示）
    public static final DeferredHolder<Item, GuideBookItem> GUIDE_BOOK =
            REGISTRY.register("guide_book", () -> new GuideBookItem());

// 南山开发者指导手册：仅创造模式可获取，讲配置、数据包 JSON 与各项机制的实现原理
    public static final DeferredHolder<Item, GuideBookItem> DEVELOPER_GUIDE_BOOK =
            REGISTRY.register("developer_guide_book",
                    () -> new GuideBookItem(GuideBookItem.DEVELOPER_BOOK_ID));

// 生鸭子
// 生鸭子的各部位，食用属性统一走 RAW_MEAT；头两件是整鸭（放血后的鸭子 → 白条鸭）

// 放血后的鸭子
    public static final DeferredHolder<Item, Item> BLED_DUCK = food("bled_duck", RAW_MEAT);

// 生白条鸭
    public static final DeferredHolder<Item, Item> RAW_DRESSED_DUCK = food("raw_dressed_duck", RAW_MEAT);

// 生鸭大腿
    public static final DeferredHolder<Item, Item> RAW_DUCK_THIGH = food("raw_duck_thigh", RAW_MEAT);

// 生鸭屁股
    public static final DeferredHolder<Item, Item> RAW_DUCK_RUMP = food("raw_duck_rump", RAW_MEAT);

// 生鸭心
    public static final DeferredHolder<Item, Item> RAW_DUCK_HEART = food("raw_duck_heart", RAW_MEAT);

// 生鸭掌
    public static final DeferredHolder<Item, Item> RAW_DUCK_WEB = food("raw_duck_web", RAW_MEAT);

// 生鸭翅
    public static final DeferredHolder<Item, Item> RAW_DUCK_WING = food("raw_duck_wing", RAW_MEAT);

// 生鸭肉
    public static final DeferredHolder<Item, Item> RAW_DUCK_MEAT = food("raw_duck_meat", RAW_MEAT);

// 生鸭肝
    public static final DeferredHolder<Item, Item> RAW_DUCK_LIVER = food("raw_duck_liver", RAW_MEAT);

// 生鸭肠
    public static final DeferredHolder<Item, Item> RAW_DUCK_INTESTINE = food("raw_duck_intestine", RAW_MEAT);

// 生鸭胗
    public static final DeferredHolder<Item, Item> RAW_DUCK_GIZZARD = food("raw_duck_gizzard", RAW_MEAT);

// 生鸭脖
    public static final DeferredHolder<Item, Item> RAW_DUCK_NECK = food("raw_duck_neck", RAW_MEAT);

// 生鸭腿
    public static final DeferredHolder<Item, Item> RAW_DUCK_LEG = food("raw_duck_leg", RAW_MEAT);

// 生鸭舌
    public static final DeferredHolder<Item, Item> RAW_DUCK_TONGUE = food("raw_duck_tongue", RAW_MEAT);

// 生鸭血
    public static final DeferredHolder<Item, Item> RAW_DUCK_BLOOD = food("raw_duck_blood", RAW_MEAT);

// 鸭头
    public static final DeferredHolder<Item, Item> RAW_DUCK_HEAD = food("raw_duck_head", RAW_MEAT);

// 生鹅
// 生鹅的各部位，食用属性统一走 RAW_MEAT；头两件是整鹅（放血后的鹅 → 白条鹅）

// 放血后的鹅
    public static final DeferredHolder<Item, Item> BLED_GOOSE = food("bled_goose", RAW_MEAT);

// 生白条鹅
    public static final DeferredHolder<Item, Item> RAW_DRESSED_GOOSE = food("raw_dressed_goose", RAW_MEAT);

// 生鹅肉
    public static final DeferredHolder<Item, Item> RAW_GOOSE_MEAT = food("raw_goose_meat", RAW_MEAT);

// 生鹅肝
    public static final DeferredHolder<Item, Item> RAW_GOOSE_LIVER = food("raw_goose_liver", RAW_MEAT);

// 生鹅肠
    public static final DeferredHolder<Item, Item> RAW_GOOSE_INTESTINE = food("raw_goose_intestine", RAW_MEAT);

// 生鹅腿
    public static final DeferredHolder<Item, Item> RAW_GOOSE_LEG = food("raw_goose_leg", RAW_MEAT);

// 熟食：烤鸭 / 烤鹅
// 生鸭子与生鹅的各部位在熔炉 / 烟熏炉 / 营火上烤熟，食用属性统一走 COOKED_MEAT（同原版熟鸡肉）

// 烤鸭头
    public static final DeferredHolder<Item, Item> ROAST_DUCK_HEAD = food("roast_duck_head", COOKED_MEAT);

// 烤鸭胗
    public static final DeferredHolder<Item, Item> ROAST_DUCK_GIZZARD = food("roast_duck_gizzard", COOKED_MEAT);

// 烤鸭心
    public static final DeferredHolder<Item, Item> ROAST_DUCK_HEART = food("roast_duck_heart", COOKED_MEAT);

// 烤鸭掌
    public static final DeferredHolder<Item, Item> ROAST_DUCK_WEB = food("roast_duck_web", COOKED_MEAT);

// 烤鸭血糕
    public static final DeferredHolder<Item, Item> ROAST_DUCK_BLOOD_CAKE = food("roast_duck_blood_cake", COOKED_MEAT);

// 烤鸭腿
    public static final DeferredHolder<Item, Item> ROAST_DUCK_LEG = food("roast_duck_leg", COOKED_MEAT);

// 烤鸭屁股
    public static final DeferredHolder<Item, Item> ROAST_DUCK_RUMP = food("roast_duck_rump", COOKED_MEAT);

// 烤鸭舌
    public static final DeferredHolder<Item, Item> ROAST_DUCK_TONGUE = food("roast_duck_tongue", COOKED_MEAT);

// 烤鸭肉
    public static final DeferredHolder<Item, Item> ROAST_DUCK_MEAT = food("roast_duck_meat", COOKED_MEAT);

// 烤鸭大腿
    public static final DeferredHolder<Item, Item> ROAST_DUCK_THIGH = food("roast_duck_thigh", COOKED_MEAT);

// 烤鸭肝
    public static final DeferredHolder<Item, Item> ROAST_DUCK_LIVER = food("roast_duck_liver", COOKED_MEAT);

// 烤鸭翅
    public static final DeferredHolder<Item, Item> ROAST_DUCK_WING = food("roast_duck_wing", COOKED_MEAT);

// 烤鸭肠
    public static final DeferredHolder<Item, Item> ROAST_DUCK_INTESTINE = food("roast_duck_intestine", COOKED_MEAT);

// 烤鸭脖
    public static final DeferredHolder<Item, Item> ROAST_DUCK_NECK = food("roast_duck_neck", COOKED_MEAT);

// 烤鹅肠
    public static final DeferredHolder<Item, Item> ROAST_GOOSE_INTESTINE = food("roast_goose_intestine", COOKED_MEAT);

// 烤鹅肝
    public static final DeferredHolder<Item, Item> ROAST_GOOSE_LIVER = food("roast_goose_liver", COOKED_MEAT);

// 烤鹅肉
    public static final DeferredHolder<Item, Item> ROAST_GOOSE_MEAT = food("roast_goose_meat", COOKED_MEAT);

// 烤鹅腿
    public static final DeferredHolder<Item, Item> ROAST_GOOSE_LEG = food("roast_goose_leg", COOKED_MEAT);

// 熟食：动物内脏 / 原版动物 / 水产
// 屠宰动物获得的生内脏、原版动物的生肉、以及各种水产，烤熟后同样走 COOKED_MEAT（同原版熟鸡肉）

// 烤动物肠子（← 动物肠子）
    public static final DeferredHolder<Item, Item> ROAST_ANIMAL_INTESTINE = food("roast_animal_intestine", COOKED_MEAT);

// 烤动物肺脏（← 动物肺脏）
    public static final DeferredHolder<Item, Item> ROAST_ANIMAL_LUNG = food("roast_animal_lung", COOKED_MEAT);

// 烤动物肝脏（← 动物肝脏）
    public static final DeferredHolder<Item, Item> ROAST_ANIMAL_LIVER = food("roast_animal_liver", COOKED_MEAT);

// 烤动物胃部（← 动物胃部）
    public static final DeferredHolder<Item, Item> ROAST_ANIMAL_STOMACH = food("roast_animal_stomach", COOKED_MEAT);

// 烤动物心脏（← 动物心脏）
    public static final DeferredHolder<Item, Item> ROAST_ANIMAL_HEART = food("roast_animal_heart", COOKED_MEAT);

// 烤动物血糕（← 动物血液）
    public static final DeferredHolder<Item, Item> ROAST_ANIMAL_BLOOD_CAKE = food("roast_animal_blood_cake", COOKED_MEAT);

// 烤兔头（← 生兔头）
    public static final DeferredHolder<Item, Item> ROAST_RABBIT_HEAD = food("roast_rabbit_head", COOKED_MEAT);

// 烤狗肉（← 生狗肉）
    public static final DeferredHolder<Item, Item> ROAST_DOG_MEAT = food("roast_dog_meat", COOKED_MEAT);

// 烤羊驼肉（← 生羊驼肉）
    public static final DeferredHolder<Item, Item> ROAST_LLAMA_MEAT = food("roast_llama_meat", COOKED_MEAT);

// 烤驴肉（← 生驴肉）
    public static final DeferredHolder<Item, Item> ROAST_DONKEY_MEAT = food("roast_donkey_meat", COOKED_MEAT);

// 烤驼峰（← 生驼峰）
    public static final DeferredHolder<Item, Item> ROAST_CAMEL_HUMP = food("roast_camel_hump", COOKED_MEAT);

// 烤骆驼肉（← 生骆驼肉）
    public static final DeferredHolder<Item, Item> ROAST_CAMEL_MEAT = food("roast_camel_meat", COOKED_MEAT);

// 烤梭子蟹（← 生三疣梭子蟹）
    public static final DeferredHolder<Item, Item> ROAST_SWIMMING_CRAB = food("roast_swimming_crab", COOKED_MEAT);

// 烤海肠（← 生单环刺螠）
    public static final DeferredHolder<Item, Item> ROAST_SPOON_WORM = food("roast_spoon_worm", COOKED_MEAT);

// 烤鲳鱼（← 生银鲳）
    public static final DeferredHolder<Item, Item> ROAST_SILVERY_POMFRET = food("roast_silvery_pomfret", COOKED_MEAT);

// 烤海胆（← 生马粪海胆）
    public static final DeferredHolder<Item, Item> ROAST_SEA_URCHIN = food("roast_sea_urchin", COOKED_MEAT);

// 烤海螺（← 生东风螺）
    public static final DeferredHolder<Item, Item> ROAST_SEA_SNAIL = food("roast_sea_snail", COOKED_MEAT);

// 烤海参（← 生仿刺参）
    public static final DeferredHolder<Item, Item> ROAST_SEA_CUCUMBER = food("roast_sea_cucumber", COOKED_MEAT);

// 烤扇贝（← 生威海扇贝）
    public static final DeferredHolder<Item, Item> ROAST_SCALLOP = food("roast_scallop", COOKED_MEAT);

// 烤沙丁鱼（← 生沙丁鱼）
    public static final DeferredHolder<Item, Item> ROAST_SARDINE = food("roast_sardine", COOKED_MEAT);

// 烤田螺（← 生中华圆田螺）
    public static final DeferredHolder<Item, Item> ROAST_RIVER_SNAIL = food("roast_river_snail", COOKED_MEAT);

// 烤蛏子（← 生竹蛏）
    public static final DeferredHolder<Item, Item> ROAST_RAZOR_CLAM = food("roast_razor_clam", COOKED_MEAT);

// 虾仁（← 生中国明对虾，烤熟后去壳）
    public static final DeferredHolder<Item, Item> SHRIMP_MEAT = food("shrimp_meat", COOKED_MEAT);

// 烤秋刀鱼（← 生日本秋刀鱼）
    public static final DeferredHolder<Item, Item> ROAST_PACIFIC_SAURY = food("roast_pacific_saury", COOKED_MEAT);

// 烤青蟹（← 生锯缘青蟹）
    public static final DeferredHolder<Item, Item> ROAST_MUD_CRAB = food("roast_mud_crab", COOKED_MEAT);

// 烤皮皮虾（← 生口虾蛄）
    public static final DeferredHolder<Item, Item> ROAST_MANTIS_SHRIMP = food("roast_mantis_shrimp", COOKED_MEAT);

// 烤海蜇皮（← 生沙海蜇）
    public static final DeferredHolder<Item, Item> ROAST_JELLYFISH = food("roast_jellyfish", COOKED_MEAT);

// 烤马步鱼（← 生马步鱼）
    public static final DeferredHolder<Item, Item> ROAST_JAPANESE_HALFBEAK = food("roast_japanese_halfbeak", COOKED_MEAT);

// 烤大闸蟹（← 生中华绒螯蟹）
    public static final DeferredHolder<Item, Item> ROAST_HAIRY_CRAB = food("roast_hairy_crab", COOKED_MEAT);

// 烤土虾（← 生土虾）
    public static final DeferredHolder<Item, Item> ROAST_MUD_SHRIMP = food("roast_mud_shrimp", COOKED_MEAT);

// 烤石斑鱼（← 生老虎斑）
    public static final DeferredHolder<Item, Item> ROAST_GROUPER = food("roast_grouper", COOKED_MEAT);

// 烤生蚝（← 生乳山生蚝）
    public static final DeferredHolder<Item, Item> ROAST_OYSTER = food("roast_oyster", COOKED_MEAT);

// 烤小龙虾（← 生克氏原螯虾）
    public static final DeferredHolder<Item, Item> ROAST_CRAYFISH = food("roast_crayfish", COOKED_MEAT);

// 烤花蛤（← 生菲律宾花蛤）
    public static final DeferredHolder<Item, Item> ROAST_CLAM = food("roast_clam", COOKED_MEAT);

// 虾皮（← 生毛虾，烤熟后晒干的虾皮）
    public static final DeferredHolder<Item, Item> DRIED_SHRIMP = food("dried_shrimp", COOKED_MEAT);

// 烤鲍鱼（← 生皱纹盘鲍）
    public static final DeferredHolder<Item, Item> ROAST_ABALONE = food("roast_abalone", COOKED_MEAT);

// 烤鱼（整鱼，熔炉 / 烟熏炉 / 营火）

// 烤草鱼（← 生草鲩）
    public static final DeferredHolder<Item, Item> ROAST_GRASS_CARP = food("roast_grass_carp", COOKED_FISH);

// 烤丁桂鱼（← 生丁鱥）
    public static final DeferredHolder<Item, Item> ROAST_TENCH = food("roast_tench", COOKED_FISH);

// 烤鳜鱼（← 生鳜鱼）
    public static final DeferredHolder<Item, Item> ROAST_MANDARIN_FISH = food("roast_mandarin_fish", COOKED_FISH);

// 烤黑鱼（← 生乌鳢）
    public static final DeferredHolder<Item, Item> ROAST_BLACK_FISH = food("roast_black_fish", COOKED_FISH);

// 烤鮰鱼（← 生长江鮰鱼）
    public static final DeferredHolder<Item, Item> ROAST_RECTUM = food("roast_rectum", COOKED_FISH);

// 烤鲫鱼（← 生河鲫鱼）
    public static final DeferredHolder<Item, Item> ROAST_CRUCIAN_CARP = food("roast_crucian_carp", COOKED_FISH);

// 烤鲤鱼（← 生中华鲤鱼）
    public static final DeferredHolder<Item, Item> ROAST_CHINESE_CARP = food("roast_chinese_carp", COOKED_FISH);

// 烤鲢鱼（← 生白鲢）
    public static final DeferredHolder<Item, Item> ROAST_SILVER_CARP = food("roast_silver_carp", COOKED_FISH);

// 烤鲈鱼（← 生加州鲈）
    public static final DeferredHolder<Item, Item> ROAST_BASS = food("roast_bass", COOKED_FISH);

// 烤罗非鱼（← 生非洲鲫鱼）
    public static final DeferredHolder<Item, Item> ROAST_TILAPIA = food("roast_tilapia", COOKED_FISH);

// 烤泥鳅（← 生泥鳅）
    public static final DeferredHolder<Item, Item> ROAST_LOACH = food("roast_loach", COOKED_FISH);

// 烤鲶鱼（← 生八须鲶）
    public static final DeferredHolder<Item, Item> ROAST_CATFISH = food("roast_catfish", COOKED_FISH);

// 烤爬地虎（← 生虾虎鱼）
    public static final DeferredHolder<Item, Item> ROAST_GOBY = food("roast_goby", COOKED_FISH);

// 烤翘嘴（← 生翘嘴红鲌）
    public static final DeferredHolder<Item, Item> ROAST_REDFIN = food("roast_redfin", COOKED_FISH);

// 烤青鱼（← 生螺蛳青）
    public static final DeferredHolder<Item, Item> ROAST_BLACK_CARP = food("roast_black_carp", COOKED_FISH);

// 烤清道夫（← 生多辐翼甲鲶，贴图为三帧动画）
    public static final DeferredHolder<Item, Item> ROAST_PLECO = food("roast_pleco", COOKED_FISH);

// 烤鳙鱼（← 生花鲢）
    public static final DeferredHolder<Item, Item> ROAST_BIGHEAD_CARP = food("roast_bighead_carp", COOKED_FISH);

// 生禽类（鹌鹑 / 鸽子 / 火鸡）
// 流程比鹅鸭简单：死禽放血 → 放血后禽 → 木盆拔毛 → 白条禽 + 羽毛，白条即最终产物

// 放血后的鹌鹑
    public static final DeferredHolder<Item, Item> BLED_QUAIL = food("bled_quail", RAW_MEAT);

// 生白条鹌鹑
    public static final DeferredHolder<Item, Item> RAW_DRESSED_QUAIL = food("raw_dressed_quail", RAW_MEAT);

// 放血后的鸽子
    public static final DeferredHolder<Item, Item> BLED_PIGEON = food("bled_pigeon", RAW_MEAT);

// 生白条鸽子
    public static final DeferredHolder<Item, Item> RAW_DRESSED_PIGEON = food("raw_dressed_pigeon", RAW_MEAT);

// 放血后的火鸡
    public static final DeferredHolder<Item, Item> BLED_TURKEY = food("bled_turkey", RAW_MEAT);

// 生白条火鸡
    public static final DeferredHolder<Item, Item> RAW_DRESSED_TURKEY = food("raw_dressed_turkey", RAW_MEAT);

// 生肉与动物内脏
// 屠宰动物获得的生肉与内脏，食用属性统一走 RAW_MEAT

// 生兔头
    public static final DeferredHolder<Item, Item> RAW_RABBIT_HEAD = food("raw_rabbit_head", RAW_MEAT);

// 生狗肉
    public static final DeferredHolder<Item, Item> RAW_DOG_MEAT = food("raw_dog_meat", RAW_MEAT);

// 生羊驼肉
    public static final DeferredHolder<Item, Item> RAW_LLAMA_MEAT = food("raw_llama_meat", RAW_MEAT);

// 生驴肉
    public static final DeferredHolder<Item, Item> RAW_DONKEY_MEAT = food("raw_donkey_meat", RAW_MEAT);

// 生驼峰
    public static final DeferredHolder<Item, Item> RAW_CAMEL_HUMP = food("raw_camel_hump", RAW_MEAT);

// 生骆驼肉
    public static final DeferredHolder<Item, Item> RAW_CAMEL_MEAT = food("raw_camel_meat", RAW_MEAT);

// 动物心脏
    public static final DeferredHolder<Item, Item> RAW_ANIMAL_HEART = food("raw_animal_heart", RAW_MEAT);

// 动物肝脏
    public static final DeferredHolder<Item, Item> RAW_ANIMAL_LIVER = food("raw_animal_liver", RAW_MEAT);

// 动物肠子
    public static final DeferredHolder<Item, Item> RAW_ANIMAL_INTESTINE = food("raw_animal_intestine", RAW_MEAT);

// 动物肺脏
    public static final DeferredHolder<Item, Item> RAW_ANIMAL_LUNG = food("raw_animal_lung", RAW_MEAT);

// 动物胃部
    public static final DeferredHolder<Item, Item> RAW_ANIMAL_STOMACH = food("raw_animal_stomach", RAW_MEAT);

// 动物血液
    public static final DeferredHolder<Item, Item> RAW_ANIMAL_BLOOD = food("raw_animal_blood", RAW_MEAT);

// 死动物
// 用 flavor_immersed_daily 的斩骨刀攻击对应动物即可获得，挂到大挂钩上按阶段屠宰（数据包 butchering 定义）

// 死鸭子
    public static final DeferredHolder<Item, Item> DEAD_DUCK =
            REGISTRY.register("dead_duck", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死鹅
    public static final DeferredHolder<Item, Item> DEAD_GOOSE =
            REGISTRY.register("dead_goose", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死鹌鹑
    public static final DeferredHolder<Item, Item> DEAD_QUAIL =
            REGISTRY.register("dead_quail", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死鸽子
    public static final DeferredHolder<Item, Item> DEAD_PIGEON =
            REGISTRY.register("dead_pigeon", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死火鸡
    public static final DeferredHolder<Item, Item> DEAD_TURKEY =
            REGISTRY.register("dead_turkey", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死狗
    public static final DeferredHolder<Item, Item> DEAD_DOG =
            REGISTRY.register("dead_dog", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死驴
    public static final DeferredHolder<Item, Item> DEAD_DONKEY =
            REGISTRY.register("dead_donkey", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死骆驼
    public static final DeferredHolder<Item, Item> DEAD_CAMEL =
            REGISTRY.register("dead_camel", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死兔子
    public static final DeferredHolder<Item, Item> DEAD_RABBIT =
            REGISTRY.register("dead_rabbit", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 死羊驼
    public static final DeferredHolder<Item, Item> DEAD_LLAMA =
            REGISTRY.register("dead_llama", () -> new TooltipItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.dead_animal"
            ));

// 南山钓竿配件：杆体
// 数值（耐久 / 拉力 / 硬度系数）同步写在 data/flavor_immersed_daily_argo/fishing_rod_parts/ 下

// 竹节竿
    public static final DeferredHolder<Item, Item> ROD_BAMBOO =
            REGISTRY.register("rod_bamboo", () -> new FishingRodPartItem(new Item.Properties().durability(80)));

// 玻纤竿
    public static final DeferredHolder<Item, Item> ROD_FIBERGLASS =
            REGISTRY.register("rod_fiberglass", () -> new FishingRodPartItem(new Item.Properties().durability(160)));

// 碳素竿
    public static final DeferredHolder<Item, Item> ROD_CARBON =
            REGISTRY.register("rod_carbon", () -> new FishingRodPartItem(new Item.Properties().durability(220)));

// 树脂竿
    public static final DeferredHolder<Item, Item> ROD_RESIN =
            REGISTRY.register("rod_resin", () -> new FishingRodPartItem(new Item.Properties().durability(300)));

// 硬木竿
    public static final DeferredHolder<Item, Item> ROD_HARDWOOD =
            REGISTRY.register("rod_hardwood", () -> new FishingRodPartItem(new Item.Properties().durability(400)));

// 复合材料竿
    public static final DeferredHolder<Item, Item> ROD_COMPOSITE =
            REGISTRY.register("rod_composite", () -> new FishingRodPartItem(new Item.Properties().durability(500)));

// 南山钓竿配件：线轮
// 数值（收线速度 / 泄力系数）同步写在 data/flavor_immersed_daily_argo/fishing_rod_parts/ 下

// 手拨轮
    public static final DeferredHolder<Item, Item> REEL_HAND =
            REGISTRY.register("reel_hand", () -> new FishingRodPartItem(new Item.Properties()));

// 纺车轮
    public static final DeferredHolder<Item, Item> REEL_SPINNING =
            REGISTRY.register("reel_spinning", () -> new FishingRodPartItem(new Item.Properties()));

// 鼓式轮
    public static final DeferredHolder<Item, Item> REEL_DRUM =
            REGISTRY.register("reel_drum", () -> new FishingRodPartItem(new Item.Properties()));

// 水滴轮
    public static final DeferredHolder<Item, Item> REEL_BAITCAST =
            REGISTRY.register("reel_baitcast", () -> new FishingRodPartItem(new Item.Properties()));

// 泄力轮
    public static final DeferredHolder<Item, Item> REEL_DRAG =
            REGISTRY.register("reel_drag", () -> new FishingRodPartItem(new Item.Properties()));

// 慢摇轮
    public static final DeferredHolder<Item, Item> REEL_JIGGING =
            REGISTRY.register("reel_jigging", () -> new FishingRodPartItem(new Item.Properties()));

// 南山钓竿配件：鱼线
// 数值（耐久 / 拉力 / 延展系数 / 粗细系数）同步写在 data/flavor_immersed_daily_argo/fishing_rod_parts/ 下

// 棉线
    public static final DeferredHolder<Item, Item> LINE_COTTON =
            REGISTRY.register("line_cotton", () -> new FishingRodPartItem(new Item.Properties().durability(40)));

// 尼龙线
    public static final DeferredHolder<Item, Item> LINE_NYLON =
            REGISTRY.register("line_nylon", () -> new FishingRodPartItem(new Item.Properties().durability(120)));

// 碳氟线
    public static final DeferredHolder<Item, Item> LINE_FLUOROCARBON =
            REGISTRY.register("line_fluorocarbon", () -> new FishingRodPartItem(new Item.Properties().durability(90)));

// 编织线
    public static final DeferredHolder<Item, Item> LINE_BRAID =
            REGISTRY.register("line_braid", () -> new FishingRodPartItem(new Item.Properties().durability(240)));

// 钢丝线
    public static final DeferredHolder<Item, Item> LINE_STEEL =
            REGISTRY.register("line_steel", () -> new FishingRodPartItem(new Item.Properties().durability(350)));

// 荧光线
    public static final DeferredHolder<Item, Item> LINE_GLOW =
            REGISTRY.register("line_glow", () -> new FishingRodPartItem(new Item.Properties().durability(100)));

// 南山钓竿配件：浮漂
// 数值（设定深度 / 抗流系数 / 大小系数）同步写在 data/flavor_immersed_daily_argo/fishing_rod_parts/ 下

// 芦苇漂
    public static final DeferredHolder<Item, Item> FLOAT_REED =
            REGISTRY.register("float_reed", () -> new FishingRodPartItem(new Item.Properties()));

// 巴尔杉漂
    public static final DeferredHolder<Item, Item> FLOAT_BALSA =
            REGISTRY.register("float_balsa", () -> new FishingRodPartItem(new Item.Properties()));

// 纳米漂
    public static final DeferredHolder<Item, Item> FLOAT_NANO =
            REGISTRY.register("float_nano", () -> new FishingRodPartItem(new Item.Properties()));

// 孔雀羽漂
    public static final DeferredHolder<Item, Item> FLOAT_PEACOCK =
            REGISTRY.register("float_peacock", () -> new FishingRodPartItem(new Item.Properties()));

// 夜光漂
    public static final DeferredHolder<Item, Item> FLOAT_GLOW =
            REGISTRY.register("float_glow", () -> new FishingRodPartItem(new Item.Properties()));

// 阿波漂
    public static final DeferredHolder<Item, Item> FLOAT_ISO =
            REGISTRY.register("float_iso", () -> new FishingRodPartItem(new Item.Properties()));

// 南山钓竿配件：铅坠
// 数值（重量系数）同步写在 data/flavor_immersed_daily_argo/fishing_rod_parts/ 下

// 咬铅
    public static final DeferredHolder<Item, Item> SINKER_SPLIT =
            REGISTRY.register("sinker_split", () -> new FishingRodPartItem(new Item.Properties()));

// 通心坠
    public static final DeferredHolder<Item, Item> SINKER_BULLET =
            REGISTRY.register("sinker_bullet", () -> new FishingRodPartItem(new Item.Properties()));

// 水滴坠
    public static final DeferredHolder<Item, Item> SINKER_DROP =
            REGISTRY.register("sinker_drop", () -> new FishingRodPartItem(new Item.Properties()));

// 铅皮
    public static final DeferredHolder<Item, Item> SINKER_SHEET =
            REGISTRY.register("sinker_sheet", () -> new FishingRodPartItem(new Item.Properties()));

// 底坠
    public static final DeferredHolder<Item, Item> SINKER_BOTTOM =
            REGISTRY.register("sinker_bottom", () -> new FishingRodPartItem(new Item.Properties()));

// 南山钓竿配件：鱼饵
// 每款鱼饵只对一种食性的鱼有效，杂食鱼不受限制（不装饵或装任意饵都可能咬钩）；
// 可堆叠，每次成功钓到对应食性的鱼消耗一个

// 草饵（食草）
    public static final DeferredHolder<Item, BaitItem> BAIT_GRASS =
            REGISTRY.register("bait_grass", () -> new BaitItem(Diet.HERBIVORE, new Item.Properties()));

// 肉饵（食肉）
    public static final DeferredHolder<Item, BaitItem> BAIT_MEAT =
            REGISTRY.register("bait_meat", () -> new BaitItem(Diet.CARNIVORE, new Item.Properties()));

// 腐饵（食腐）
    public static final DeferredHolder<Item, BaitItem> BAIT_ROT =
            REGISTRY.register("bait_rot", () -> new BaitItem(Diet.SCAVENGER, new Item.Properties()));

// 蚯蚓（活体）
    public static final DeferredHolder<Item, BaitItem> BAIT_WORM =
            REGISTRY.register("earthworm", () -> new BaitItem(Diet.LIVE, new Item.Properties()));

// 竿体配件顺序：即钓竿模型 body 谓词的取值顺序（1 开始），模型与贴图由这里推导生成
    public static final List<DeferredHolder<Item, Item>> ROD_BODIES = List.of(
            ROD_BAMBOO, ROD_FIBERGLASS, ROD_CARBON, ROD_RESIN, ROD_HARDWOOD, ROD_COMPOSITE);

// 线轮配件顺序：渲染时线轮缩略图叠在竿体模型之上（下标 + 1 即
// com.flavor_immersed_daily_argo.fishing.FishingRodModelOverrides#partVariant 的返回值）
    public static final List<DeferredHolder<Item, Item>> ROD_REELS = List.of(
            REEL_HAND, REEL_SPINNING, REEL_DRUM, REEL_BAITCAST, REEL_DRAG, REEL_JIGGING);

// 鱼线配件顺序：渲染时鱼线覆盖叠在线轮之上；抛竿时整层不渲染，没装鱼线时浮漂 / 铅坠也不渲染
    public static final List<DeferredHolder<Item, Item>> ROD_LINES = List.of(
            LINE_COTTON, LINE_NYLON, LINE_FLUOROCARBON, LINE_BRAID, LINE_STEEL, LINE_GLOW);

// 浮漂配件顺序：渲染时浮漂叠在鱼线之上（所以没装鱼线或抛竿时不渲染）；
// 投出的鱼钩实体也按这里的贴图渲染，取代原版钓鱼浮漂
    public static final List<DeferredHolder<Item, Item>> ROD_FLOATS = List.of(
            FLOAT_REED, FLOAT_BALSA, FLOAT_NANO, FLOAT_PEACOCK, FLOAT_GLOW, FLOAT_ISO);

// 铅坠配件顺序：即 com.flavor_immersed_daily_argo.fishing.FishingRodStats#SLOT_SINKER 槽位
// 读取顺序，铅坠渲染同样叠在鱼线之上，没装鱼线或抛竿时都不渲染
    public static final List<DeferredHolder<Item, Item>> ROD_SINKERS = List.of(
            SINKER_SPLIT, SINKER_BULLET, SINKER_DROP, SINKER_SHEET, SINKER_BOTTOM);

// 鱼饵：每款对应一种食性，顺序无所谓，仅供创造模式栏与遍历使用
    public static final List<DeferredHolder<Item, BaitItem>> BAITS = List.of(
            BAIT_GRASS, BAIT_MEAT, BAIT_ROT, BAIT_WORM);

// 金坷垃
    public static final DeferredHolder<Item, JinkelaItem> JINKELA =
            REGISTRY.register("jinkela", () -> new JinkelaItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.jinkela"
            ));

// 有机肥料：化粪发酵桶攒够粪便后发酵出来的产物，右键效果与骨粉完全一致
    public static final DeferredHolder<Item, OrganicFertilizerItem> ORGANIC_FERTILIZER =
            REGISTRY.register("organic_fertilizer", () -> new OrganicFertilizerItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.organic_fertilizer"
            ));

// 林业生态

// 护林员写字板：右键一株树的原木标记 / 潜行右键解除林业网点，右键空气打开网点界面
    public static final DeferredHolder<Item, Item> RANGER_CLIPBOARD =
            REGISTRY.register("ranger_clipboard", () -> new RangerClipboardItem(
                    new Item.Properties().stacksTo(1),
                    "tooltip.flavor_immersed_daily_argo.ranger_clipboard"
            ));

// 树漆：手持右键网点的树，消除蚜虫侵蚀
    public static final DeferredHolder<Item, Item> TREE_PAINT =
            REGISTRY.register("tree_paint", () -> new TreePaintItem(
                    new Item.Properties().durability(64),
                    "tooltip.flavor_immersed_daily_argo.tree_paint"
            ));

// 灭火器：右键喷出灭火粒子并扑灭周围范围的火，消耗耐久
    public static final DeferredHolder<Item, Item> FIRE_EXTINGUISHER =
            REGISTRY.register("fire_extinguisher", () -> new FireExtinguisherItem(
                    new Item.Properties().durability(256),
                    "tooltip.flavor_immersed_daily_argo.fire_extinguisher"
            ));

// 气象仪器：放置后平日风车盘悠悠转，区块闹非常规天气时转得飞快
    public static final DeferredHolder<Item, Item> WEATHER_INSTRUMENT =
            REGISTRY.register("weather_instrument", () -> new TooltipBlockItem(
                    ModBlocks.WEATHER_INSTRUMENT.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.weather_instrument"
            ));

// 除草剂：右键清除周围一片杂草，并处理网点的「杂草丛生」突发情况
    public static final DeferredHolder<Item, Item> HERBICIDE =
            REGISTRY.register("herbicide", () -> new HerbicideItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.herbicide"
            ));

// 测距绳：手持时显示当前所在区块的边界，便于确定网点范围
    public static final DeferredHolder<Item, Item> RANGING_ROPE =
            REGISTRY.register("ranging_rope", () -> new RangingRopeItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.ranging_rope"
            ));

// 水位尺：右键测量所在网点的水位，洪水 / 干涸时读数异常
    public static final DeferredHolder<Item, Item> WATER_LEVEL_GAUGE =
            REGISTRY.register("water_level_gauge", () -> new WaterLevelGaugeItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.water_level_gauge"
            ));

// 水质检测器：右键测量五项水质指标并把确切灾难名上报到写字板
    public static final DeferredHolder<Item, Item> WATER_QUALITY_DETECTOR =
            REGISTRY.register("water_quality_detector", () -> new WaterQualityDetectorItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.water_quality_detector"
            ));

// 昆虫培育箱：空手右键打开界面，4 个输入槽 + 6 个产出槽
    public static final DeferredHolder<Item, Item> INSECT_INCUBATOR =
            REGISTRY.register("insect_incubator", () -> new TooltipBlockItem(
                    ModBlocks.INSECT_INCUBATOR.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.insect_incubator"
            ));

// 纸板蜂巢
    public static final DeferredHolder<Item, Item> CARDBOARD_HONEYCOMB = tooltipItem("cardboard_honeycomb");

// 轻薄木板
    public static final DeferredHolder<Item, Item> INSECT_BOARD = tooltipItem("insect_board");

// 捕虫网
    public static final DeferredHolder<Item, Item> INSECT_NET = tooltipItem("insect_net");

// 蜜蜂卵：昆虫培育的起点
    public static final DeferredHolder<Item, Item> BEE_ROE = item("bee_roe");

// 蝉卵
    public static final DeferredHolder<Item, Item> CICADA_ROE = item("cicada_roe");

// 蚂蚱卵
    public static final DeferredHolder<Item, Item> LOCUST_ROE = item("locust_roe");

// 蚕卵
    public static final DeferredHolder<Item, Item> SILKWORM_ROE = item("silkworm_roe");

// 蜜蜂：初级农产品，昆虫系列的成虫
    public static final DeferredHolder<Item, Item> BEE = item("bee");

// 知了猴：初级农产品，昆虫系列的成虫
    public static final DeferredHolder<Item, Item> CICADA = item("cicada");

// 蝉蜕：知了猴羽化后留下的外壳，初级农产品
    public static final DeferredHolder<Item, Item> CICADA_SLOUGH = item("cicada_slough");

// 蚂蚱：初级农产品，昆虫系列的成虫
    public static final DeferredHolder<Item, Item> LOCUST = item("locust");

// 蚕：初级农产品，昆虫系列的成虫
    public static final DeferredHolder<Item, Item> SILKWORM = item("silkworm");

// 高蛋白聚合物
    public static final DeferredHolder<Item, Item> HIGH_PROTEIN_SUBSTANCE = tooltipItem("high_protein_substance");

// 活性炭颗粒：右键水体投放（消耗品），吸附杂质降低浊度，并平衡电导率
    public static final DeferredHolder<Item, Item> ACTIVATED_CARBON_PARTICLES =
            REGISTRY.register("activated_carbon_particles", () -> new WaterTreatmentItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.activated_carbon_particles",
                    true,
                    WaterTreatmentItem.Effect.TURBIDITY_DOWN,
                    WaterTreatmentItem.Effect.CONDUCTIVITY_BALANCE
            ));

// 蝉蜕酸：右键水体投放（消耗品），中和碱性、降低 pH
    public static final DeferredHolder<Item, Item> PROTOCATECHUIC_ACID =
            REGISTRY.register("protocatechuic_acid", () -> new WaterTreatmentItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.protocatechuic_acid",
                    true,
                    WaterTreatmentItem.Effect.PH_DOWN
            ));

// 生物碱：右键水体投放（消耗品），中和酸性、升高 pH
    public static final DeferredHolder<Item, Item> OXYPHYLLINE =
            REGISTRY.register("oxyphylline", () -> new WaterTreatmentItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.oxyphylline",
                    true,
                    WaterTreatmentItem.Effect.PH_UP
            ));

// 水体供氧机：右键水体投放（非消耗品），把溶解氧平衡到适宜水平
    public static final DeferredHolder<Item, Item> WATER_OXYGEN_MACHINE =
            REGISTRY.register("water_oxygen_machine", () -> new WaterTreatmentItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.water_oxygen_machine",
                    false,
                    WaterTreatmentItem.Effect.OXYGEN_BALANCE
            ));

// 水体菌种培养皿：右键水体投放（消耗品），微生物分解有机污染、降低有机污染指数
    public static final DeferredHolder<Item, Item> MICROBE_POWDER =
            REGISTRY.register("microbe_powder", () -> new WaterTreatmentItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.microbe_powder",
                    true,
                    WaterTreatmentItem.Effect.ORGANIC_DOWN
            ));

// 农作物（果树）

// 果实的通用食用属性：恢复 3 点饥饿值，饱和度系数 0.3，饱了也能吃
    public static final FoodProperties FRUIT_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3F)
            .alwaysEdible()
            .build();

// 荠菜的食用属性：野菜清淡，只垫一点点饥饿值，饱了也能嚼两口
    public static final FoodProperties SHEPHERDS_PURSE_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.2F)
            .alwaysEdible()
            .build();

// 果酱的食用属性：恢复 3 点饥饿值，饱和度系数 0.8
    public static final FoodProperties FRUIT_JAM_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.8F)
            .build();

// 果味甜品（冰淇淋 / 棒冰 / 果汁）的食用属性：恢复 5 点饥饿值，饱和度系数 0.8
    public static final FoodProperties FRUIT_DESSERT_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.8F)
            .build();

// 板栗面团的食用属性：恢复 2 点饥饿值，饱和度系数 0.6
    public static final FoodProperties CHESTNUT_DOUGH_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.6F)
            .build();

// 烤栗子的食用属性：恢复 3 点饥饿值，饱和度系数 0.6
    public static final FoodProperties ROASTED_CHESTNUT_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.6F)
            .build();

// 糖炒栗子的食用属性：恢复 5 点饥饿值，饱和度系数 0.8
    public static final FoodProperties SUGAR_ROASTED_CHESTNUT_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.8F)
            .build();

// 板栗糕的食用属性：恢复 6 点饥饿值，饱和度系数 0.8
    public static final FoodProperties CHESTNUT_CAKE_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.8F)
            .build();

// 卤鸭小件的食用属性（鸭头 / 鸭肠 / 鸭脖）：恢复 6 点饥饿值
    public static final FoodProperties BRAISED_DUCK_PART_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(1.0F)
            .build();

// 卤鸭零碎的食用属性（鸭掌 / 鸭舌）：没多少肉，恢复 5 点饥饿值
    public static final FoodProperties BRAISED_DUCK_TIDBIT_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(1.0F)
            .build();

// 卤鸭胗的食用属性：紧实耐嚼，恢复 7 点饥饿值
    public static final FoodProperties BRAISED_DUCK_GIZZARD_FOOD = new FoodProperties.Builder()
            .nutrition(7)
            .saturationModifier(1.0F)
            .build();

// 卤鸭腿的食用属性：一整条腿，恢复 8 点饥饿值
    public static final FoodProperties BRAISED_DUCK_LEG_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(1.0F)
            .build();

// 流油咸鸭蛋的食用属性：腌得冒油，恢复 5 点饥饿值
    public static final FoodProperties SALTED_DUCK_EGG_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.6F)
            .build();

// 煮鸭蛋的食用属性：白水煮的，恢复 4 点饥饿值
    public static final FoodProperties BOILED_DUCK_EGG_FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.5F)
            .build();

// 鸭血粉丝汤的食用属性：一碗顶一顿，恢复 8 点饥饿值
    public static final FoodProperties DUCK_BLOOD_SOUP_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(0.8F)
            .build();

// 三道整鸭菜（北京烤鸭 / 卤鸭拼盘 / 手撕烤鸭）直接吃的食用属性：一顿管饱
    public static final FoodProperties WHOLE_DUCK_DISH_FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(0.8F)
            .build();

// 铁锅炖大鹅直接吃的食用属性：连锅端走也管饱
    public static final FoodProperties POT_STEW_GOOSE_FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(0.8F)
            .build();

// 法式鹅肝直接吃的食用属性：一小盘肥肝配吐司，油润顶饱
    public static final FoodProperties FOIE_GRAS_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(0.8F)
            .build();

// 广式烧鹅直接吃的食用属性：整只烧鹅，皮脆肉厚
    public static final FoodProperties ROASTED_GOOSE_FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(0.8F)
            .build();

// 沛县狗肉直接吃的食用属性：一大盘卤好的狗肉
    public static final FoodProperties PEIXIAN_DOG_MEAT_FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(0.8F)
            .build();

// 清蒸大闸蟹直接吃的食用属性：整只大闸蟹，恢复 9 点饥饿值
    public static final FoodProperties STEAMED_HAIRY_CRAB_FOOD = new FoodProperties.Builder()
            .nutrition(9)
            .saturationModifier(0.8F)
            .build();

// 白灼虾直接吃的食用属性：一盘水煮虾，恢复 7 点饥饿值
    public static final FoodProperties SCALDED_PRAWNS_FOOD = new FoodProperties.Builder()
            .nutrition(7)
            .saturationModifier(0.8F)
            .build();

// 油焖大虾直接吃的食用属性：一盘油焖虾，恢复 8 点饥饿值
    public static final FoodProperties BRAISED_PRAWNS_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(0.8F)
            .build();

// 鸽子汤的食用属性：一碗热汤，恢复 8 点饥饿值
    public static final FoodProperties PIGEON_SOUP_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(0.8F)
            .build();

// 酱肉（酱驴肉 / 卤骆驼肉）的食用属性：切盘凉肉，恢复 6 点饥饿值
    public static final FoodProperties BRAISED_MEAT_DISH_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.7F)
            .build();

// 烤鹌鹑的食用属性：小小一只，恢复 5 点饥饿值
    public static final FoodProperties ROAST_QUAIL_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.7F)
            .build();

// 烤鸽子的食用属性：比鹌鹑肥一些，恢复 6 点饥饿值
    public static final FoodProperties ROAST_PIGEON_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.7F)
            .build();

// 烤火鸡的食用属性：整只大鸟，恢复 8 点饥饿值
    public static final FoodProperties ROAST_TURKEY_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(0.8F)
            .build();

// 卤鹌鹑蛋的食用属性：一小碟卤蛋，恢复 3 点饥饿值
    public static final FoodProperties BRAISED_QUAIL_EGG_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.6F)
            .build();

// 驴肉火烧的食用属性：酱驴肉（6）+ 葱油烙饼（9），恢复 15 点饥饿值
    public static final FoodProperties DONKEY_MEAT_BURGER_FOOD = new FoodProperties.Builder()
            .nutrition(15)
            .saturationModifier(0.8F)
            .build();

// 麻辣兔头的食用属性：主要是啃味，恢复 4 点饥饿值
    public static final FoodProperties SPICY_RABBIT_HEAD_FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.6F)
            .build();

// 煮鹌鹑蛋的食用属性：一口一个，恢复 2 点饥饿值
    public static final FoodProperties BOILED_QUAIL_EGG_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.5F)
            .build();

// 煮鸽子蛋的食用属性：恢复 3 点饥饿值
    public static final FoodProperties BOILED_PIGEON_EGG_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.5F)
            .build();

// 煮大禽蛋（鹅蛋 / 火鸡蛋）的食用属性：恢复 5 点饥饿值
    public static final FoodProperties BOILED_BIG_EGG_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.5F)
            .build();

// 柚子：清韵木果树的果实
    public static final DeferredHolder<Item, Item> POMELO = food("pomelo", FRUIT_FOOD);

// 板栗：核毅木果树的果实
    public static final DeferredHolder<Item, Item> CHESTNUT = food("chestnut", FRUIT_FOOD);

// 番石榴：虬藤木果树的果实
    public static final DeferredHolder<Item, Item> GUAVA = food("guava", FRUIT_FOOD);

// 荠菜：大田作物 ShepherdsPurseCropBlock 的产物，可以吃，也可以再搓成种子
    public static final DeferredHolder<Item, Item> SHEPHERDS_PURSE = food("shepherds_purse", SHEPHERDS_PURSE_FOOD);

// 荠菜种子：种在耕地上长出荠菜；方块作物本身没有 BlockItem，靠它放置
    public static final DeferredHolder<Item, Item> SHEPHERDS_PURSE_SEEDS =
            REGISTRY.register("shepherds_purse_seeds", () -> new BlockItem(ModBlocks.SHEPHERDS_PURSE.get(), new Item.Properties()));

// 柚子苗（方块物品）：种在地上长成柚子树
    public static final DeferredHolder<Item, Item> POMELO_SAPLING =
            REGISTRY.register("pomelo_sapling", () -> new BlockItem(ModBlocks.POMELO_SAPLING.get(), new Item.Properties()));

// 板栗苗（方块物品）：种在地上长成板栗树
    public static final DeferredHolder<Item, Item> CHESTNUT_SAPLING =
            REGISTRY.register("chestnut_sapling", () -> new BlockItem(ModBlocks.CHESTNUT_SAPLING.get(), new Item.Properties()));

// 番石榴苗（方块物品）：种在地上长成番石榴树
    public static final DeferredHolder<Item, Item> GUAVA_SAPLING =
            REGISTRY.register("guava_sapling", () -> new BlockItem(ModBlocks.GUAVA_SAPLING.get(), new Item.Properties()));

// 柚子树叶（方块物品）：随机刻结果，挂果后下方长出悬挂的柚子
    public static final DeferredHolder<Item, Item> POMELO_FRUITING_LEAVES =
            REGISTRY.register("pomelo_fruiting_leaves", () -> new BlockItem(ModBlocks.POMELO_FRUITING_LEAVES.get(), new Item.Properties()));

// 板栗树叶（方块物品）：随机刻结果，挂果后下方长出悬挂的板栗
    public static final DeferredHolder<Item, Item> CHESTNUT_FRUITING_LEAVES =
            REGISTRY.register("chestnut_fruiting_leaves", () -> new BlockItem(ModBlocks.CHESTNUT_FRUITING_LEAVES.get(), new Item.Properties()));

// 番石榴树叶（方块物品）：随机刻结果，挂果后下方长出悬挂的番石榴
    public static final DeferredHolder<Item, Item> GUAVA_FRUITING_LEAVES =
            REGISTRY.register("guava_fruiting_leaves", () -> new BlockItem(ModBlocks.GUAVA_FRUITING_LEAVES.get(), new Item.Properties()));

// 悬挂的柚子（方块物品）：挂在柚子树叶下，右键可摘下
    public static final DeferredHolder<Item, Item> HANGING_POMELO =
            REGISTRY.register("hanging_pomelo", () -> new BlockItem(ModBlocks.HANGING_POMELO.get(), new Item.Properties()));

// 悬挂的板栗（方块物品）：挂在板栗树叶下，右键可摘下
    public static final DeferredHolder<Item, Item> HANGING_CHESTNUT =
            REGISTRY.register("hanging_chestnut", () -> new BlockItem(ModBlocks.HANGING_CHESTNUT.get(), new Item.Properties()));

// 悬挂的番石榴（方块物品）：挂在番石榴树叶下，右键可摘下
    public static final DeferredHolder<Item, Item> HANGING_GUAVA =
            REGISTRY.register("hanging_guava", () -> new BlockItem(ModBlocks.HANGING_GUAVA.get(), new Item.Properties()));

// 果食（果树果实的加工品）

// 柚子果酱：柚子熬成的酱
    public static final DeferredHolder<Item, Item> POMELO_JAM = food("pomelo_jam", FRUIT_JAM_FOOD);

// 番石榴果酱：番石榴熬成的酱
    public static final DeferredHolder<Item, Item> GUAVA_JAM = food("guava_jam", FRUIT_JAM_FOOD);

// 柚子冰淇淋
    public static final DeferredHolder<Item, Item> POMELO_ICECREAM = food("pomelo_icecream", FRUIT_DESSERT_FOOD);

// 番石榴冰淇淋
    public static final DeferredHolder<Item, Item> GUAVA_ICECREAM = food("guava_icecream", FRUIT_DESSERT_FOOD);

// 柚子棒冰
    public static final DeferredHolder<Item, Item> POMELO_POPSICLE = food("pomelo_popsicle", FRUIT_DESSERT_FOOD);

// 番石榴棒冰
    public static final DeferredHolder<Item, Item> GUAVA_POPSICLE = food("guava_popsicle", FRUIT_DESSERT_FOOD);

// 柚子果汁：饮用动画
    public static final DeferredHolder<Item, Item> POMELO_JUICE =
            REGISTRY.register("pomelo_juice", () -> new DrinkItem(new Item.Properties().food(FRUIT_DESSERT_FOOD)));

// 番石榴果汁：饮用动画
    public static final DeferredHolder<Item, Item> GUAVA_JUICE =
            REGISTRY.register("guava_juice", () -> new DrinkItem(new Item.Properties().food(FRUIT_DESSERT_FOOD)));

// 板栗粉：板栗磨成的粉（合成材料，不可食用）
    public static final DeferredHolder<Item, Item> CHESTNUT_FLOUR = item("chestnut_flour");

// 板栗面团：板栗粉揉成的面团
    public static final DeferredHolder<Item, Item> CHESTNUT_DOUGH = food("chestnut_dough", CHESTNUT_DOUGH_FOOD);

// 烤栗子：烤熟的板栗
    public static final DeferredHolder<Item, Item> ROASTED_CHESTNUT = food("roasted_chestnut", ROASTED_CHESTNUT_FOOD);

// 糖炒栗子：裹了糖的炒板栗
    public static final DeferredHolder<Item, Item> SUGAR_ROASTED_CHESTNUT = food("sugar_roasted_chestnut", SUGAR_ROASTED_CHESTNUT_FOOD);

// 板栗糕：板栗面团蒸出的糕点
    public static final DeferredHolder<Item, Item> CHESTNUT_CAKE = food("chestnut_cake", CHESTNUT_CAKE_FOOD);

// 食物佳肴：鸭菜（卤味 / 鸭蛋 / 汤 / 整鸭菜）

// 卤鸭头
    public static final DeferredHolder<Item, Item> BRAISED_DUCK_HEAD = food("braised_duck_head", BRAISED_DUCK_PART_FOOD);

// 卤鸭掌
    public static final DeferredHolder<Item, Item> BRAISED_DUCK_WEB = food("braised_duck_web", BRAISED_DUCK_TIDBIT_FOOD);

// 卤鸭肠
    public static final DeferredHolder<Item, Item> BRAISED_DUCK_INTESTINE = food("braised_duck_intestine", BRAISED_DUCK_PART_FOOD);

// 卤鸭胗
    public static final DeferredHolder<Item, Item> BRAISED_DUCK_GIZZARD = food("braised_duck_gizzard", BRAISED_DUCK_GIZZARD_FOOD);

// 卤鸭脖
    public static final DeferredHolder<Item, Item> BRAISED_DUCK_NECK = food("braised_duck_neck", BRAISED_DUCK_PART_FOOD);

// 卤鸭腿
    public static final DeferredHolder<Item, Item> BRAISED_DUCK_LEG = food("braised_duck_leg", BRAISED_DUCK_LEG_FOOD);

// 卤鸭舌
    public static final DeferredHolder<Item, Item> BRAISED_DUCK_TONGUE = food("braised_duck_tongue", BRAISED_DUCK_TIDBIT_FOOD);

// 流油咸鸭蛋
    public static final DeferredHolder<Item, Item> OOZING_SALTED_DUCK_EGG = food("oozing_salted_duck_egg", SALTED_DUCK_EGG_FOOD);

// 煮鸭蛋
    public static final DeferredHolder<Item, Item> BOILED_DUCK_EGG = food("boiled_duck_egg", BOILED_DUCK_EGG_FOOD);

// 鸭血粉丝汤
    public static final DeferredHolder<Item, Item> DUCK_BLOOD_VERMICELLI_SOUP = food("duck_blood_vermicelli_soup", DUCK_BLOOD_SOUP_FOOD);

// 北京烤鸭（方块物品）：直接吃恢复大量饱食度，右键地面可摆成 3 阶段的菜
    public static final DeferredHolder<Item, Item> PEKING_ROAST_DUCK =
            REGISTRY.register("peking_roast_duck", () -> new BlockItem(ModBlocks.PEKING_ROAST_DUCK.get(),
                    new Item.Properties().food(WHOLE_DUCK_DISH_FOOD)));

// 卤鸭拼盘（方块物品）：直接吃恢复大量饱食度，右键地面可摆成 3 阶段的菜
    public static final DeferredHolder<Item, Item> BRAISED_DUCK_PLATTER =
            REGISTRY.register("braised_duck_platter", () -> new BlockItem(ModBlocks.BRAISED_DUCK_PLATTER.get(),
                    new Item.Properties().food(WHOLE_DUCK_DISH_FOOD)));

// 手撕烤鸭（方块物品）：直接吃恢复大量饱食度，右键地面可摆成 4 阶段的菜
    public static final DeferredHolder<Item, Item> ROAST_DUCK =
            REGISTRY.register("roast_duck", () -> new BlockItem(ModBlocks.ROAST_DUCK.get(),
                    new Item.Properties().food(WHOLE_DUCK_DISH_FOOD)));

// 铁锅炖大鹅（方块物品）：直接吃恢复大量饱食度，右键地面可摆成一锅，12 个模型 = 12 个阶段
    public static final DeferredHolder<Item, Item> POT_STEW_GOOSE =
            REGISTRY.register("pot_stew_goose", () -> new BlockItem(ModBlocks.POT_STEW_GOOSE.get(),
                    new Item.Properties().food(POT_STEW_GOOSE_FOOD)));

// 全驴宴（方块物品）：一桌宴席只能摆在地上吃、不能直接啃；吃满 20 口整桌消失，破坏也不掉落
    public static final DeferredHolder<Item, Item> DONKEY_FEAST =
            REGISTRY.register("donkey_feast", () -> new BlockItem(ModBlocks.DONKEY_FEAST.get(), new Item.Properties()));

// 法式鹅肝（方块物品）：直接吃恢复大量饱食度，右键地面可摆成一小盘
    public static final DeferredHolder<Item, Item> FOIE_GRAS =
            REGISTRY.register("foie_gras", () -> new BlockItem(ModBlocks.FOIE_GRAS.get(),
                    new Item.Properties().food(FOIE_GRAS_FOOD)));

// 广式烧鹅（方块物品）：直接吃恢复大量饱食度，右键地面可摆成 6 阶段的烧鹅
    public static final DeferredHolder<Item, Item> ROASTED_GOOSE =
            REGISTRY.register("roasted_goose", () -> new BlockItem(ModBlocks.ROASTED_GOOSE.get(),
                    new Item.Properties().food(ROASTED_GOOSE_FOOD)));

// 沛县狗肉（方块物品）：直接吃恢复大量饱食度，右键地面可摆成 4 阶段的狗肉
    public static final DeferredHolder<Item, Item> PEIXIAN_DOG_MEAT =
            REGISTRY.register("peixian_dog_meat", () -> new BlockItem(ModBlocks.PEIXIAN_DOG_MEAT.get(),
                    new Item.Properties().food(PEIXIAN_DOG_MEAT_FOOD)));

// 清蒸大闸蟹（方块物品）：直接吃恢复大量饱食度，右键地面可摆成 2 阶段的大闸蟹
    public static final DeferredHolder<Item, Item> STEAMED_HAIRY_CRAB =
            REGISTRY.register("steamed_hairy_crab", () -> new BlockItem(ModBlocks.STEAMED_HAIRY_CRAB.get(),
                    new Item.Properties().food(STEAMED_HAIRY_CRAB_FOOD)));

// 白灼虾（方块物品）：直接吃恢复大量饱食度，右键地面可摆成 4 阶段的白灼虾
    public static final DeferredHolder<Item, Item> SCALDED_PRAWNS =
            REGISTRY.register("scalded_prawns", () -> new BlockItem(ModBlocks.SCALDED_PRAWNS.get(),
                    new Item.Properties().food(SCALDED_PRAWNS_FOOD)));

// 油焖大虾（方块物品）：直接吃恢复大量饱食度，右键地面可摆成 4 阶段的油焖大虾
    public static final DeferredHolder<Item, Item> BRAISED_PRAWNS =
            REGISTRY.register("braised_prawns", () -> new BlockItem(ModBlocks.BRAISED_PRAWNS.get(),
                    new Item.Properties().food(BRAISED_PRAWNS_FOOD)));

// 鸽子汤
    public static final DeferredHolder<Item, Item> PIGEON_SOUP = food("pigeon_soup", PIGEON_SOUP_FOOD);

// 酱驴肉
    public static final DeferredHolder<Item, Item> BRAISED_DONKEY_MEAT = food("braised_donkey_meat", BRAISED_MEAT_DISH_FOOD);

// 烤鹌鹑
    public static final DeferredHolder<Item, Item> ROAST_QUAIL = food("roast_quail", ROAST_QUAIL_FOOD);

// 烤鸽子
    public static final DeferredHolder<Item, Item> ROAST_PIGEON = food("roast_pigeon", ROAST_PIGEON_FOOD);

// 烤火鸡
    public static final DeferredHolder<Item, Item> ROAST_TURKEY = food("roast_turkey", ROAST_TURKEY_FOOD);

// 卤鹌鹑蛋
    public static final DeferredHolder<Item, Item> BRAISED_QUAIL_EGG = food("braised_quail_egg", BRAISED_QUAIL_EGG_FOOD);

// 卤骆驼肉
    public static final DeferredHolder<Item, Item> BRAISED_CAMEL_MEAT = food("braised_camel_meat", BRAISED_MEAT_DISH_FOOD);

// 驴肉火烧
    public static final DeferredHolder<Item, Item> DONKEY_MEAT_BURGER = food("donkey_meat_burger", DONKEY_MEAT_BURGER_FOOD);

// 麻辣兔头
    public static final DeferredHolder<Item, Item> SPICY_RABBIT_HEAD = food("spicy_rabbit_head", SPICY_RABBIT_HEAD_FOOD);

// 煮鹌鹑蛋
    public static final DeferredHolder<Item, Item> BOILED_QUAIL_EGG = food("boiled_quail_egg", BOILED_QUAIL_EGG_FOOD);

// 煮鹅蛋
    public static final DeferredHolder<Item, Item> BOILED_GOOSE_EGG = food("boiled_goose_egg", BOILED_BIG_EGG_FOOD);

// 煮鸽子蛋
    public static final DeferredHolder<Item, Item> BOILED_PIGEON_EGG = food("boiled_pigeon_egg", BOILED_PIGEON_EGG_FOOD);

// 煮火鸡蛋
    public static final DeferredHolder<Item, Item> BOILED_TURKEY_EGG = food("boiled_turkey_egg", BOILED_BIG_EGG_FOOD);

// 食物佳肴：调味料与水产熟食

// 调味料（不可直接食用）

// 蚝油
    public static final DeferredHolder<Item, Item> OYSTER_SAUCE = item("oyster_sauce");

// 蒜蓉
    public static final DeferredHolder<Item, Item> GARLIC_MINCE = item("garlic_mince");

// 虾酱
    public static final DeferredHolder<Item, Item> SHRIMP_PASTE = item("shrimp_paste");

// 蟹黄
    public static final DeferredHolder<Item, Item> CRAB_ROE = item("crab_roe");

// 鱼肝油
    public static final DeferredHolder<Item, Item> FISH_LIVER_OIL = item("fish_liver_oil");

// 深海鱼肝油
    public static final DeferredHolder<Item, Item> DEEP_SEA_FISH_LIVER_OIL = item("deep_sea_fish_liver_oil");

// 腌剑鱼肉：腌制的鱼，食用数值暂同熟肉
    public static final DeferredHolder<Item, Item> PICKLED_SWORDFISH = food("pickled_swordfish", COOKED_MEAT);

// 可摆放的菜品（方块物品）：直接吃数值暂同熟肉，右键地面摆成一盘

// 八卦汤
    public static final DeferredHolder<Item, Item> TURTLE_PIGEON_SOUP =
            REGISTRY.register("turtle_pigeon_soup", () -> new BlockItem(ModBlocks.TURTLE_PIGEON_SOUP.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 鲅鱼煎饼套餐
    public static final DeferredHolder<Item, Item> SPICY_MACKEREL_CRUMBLE =
            REGISTRY.register("spicy_mackerel_crumble", () -> new BlockItem(ModBlocks.SPICY_MACKEREL_CRUMBLE.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 爆炒花蛤
    public static final DeferredHolder<Item, Item> STIR_FRIED_CLAMS =
            REGISTRY.register("stir_fried_clams", () -> new BlockItem(ModBlocks.STIR_FRIED_CLAMS.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 避风塘炒虾
    public static final DeferredHolder<Item, Item> TYPHOON_SHELTER_PRAWN =
            REGISTRY.register("typhoon_shelter_prawn", () -> new BlockItem(ModBlocks.TYPHOON_SHELTER_PRAWN.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 春秋剑鱼
    public static final DeferredHolder<Item, Item> STINKY_SWORDFISH =
            REGISTRY.register("stinky_swordfish", () -> new BlockItem(ModBlocks.STINKY_SWORDFISH.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 花蛤韭菜疙瘩汤
    public static final DeferredHolder<Item, Item> CLAM_CHIVE_DOUGH_DROP_SOUP =
            REGISTRY.register("clam_chive_dough_drop_soup", () -> new BlockItem(ModBlocks.CLAM_CHIVE_DOUGH_DROP_SOUP.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 麻辣小龙虾
    public static final DeferredHolder<Item, Item> SPICY_CRAYFISH =
            REGISTRY.register("spicy_crayfish", () -> new BlockItem(ModBlocks.SPICY_CRAYFISH.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 水产熟食（食用数值暂同熟肉）

// 鲅鱼卷煎饼
    public static final DeferredHolder<Item, Item> MACKEREL_PANCAKE_ROLL = food("mackerel_pancake_roll", COOKED_MEAT);

// 蛏子炒蛋
    public static final DeferredHolder<Item, Item> RAZOR_CLAM_SCRAMBLED_EGGS = food("razor_clam_scrambled_eggs", COOKED_MEAT);

// 葱烧海参
    public static final DeferredHolder<Item, Item> SCALLION_SEA_CUCUMBER = food("scallion_sea_cucumber", COOKED_MEAT);

// 海肠捞饭
    public static final DeferredHolder<Item, Item> SPOON_WORM_RICE = food("spoon_worm_rice", COOKED_MEAT);

// 海胆丝瓜汤
    public static final DeferredHolder<Item, Item> SEA_URCHIN_LUFFA_SOUP = food("sea_urchin_luffa_soup", COOKED_MEAT);

// 海胆蒸蛋
    public static final DeferredHolder<Item, Item> SEA_URCHIN_STEAMED_EGG = food("sea_urchin_steamed_egg", COOKED_MEAT);

// 红烧鲍鱼
    public static final DeferredHolder<Item, Item> BRAISED_ABALONE = food("braised_abalone", COOKED_MEAT);

// 红烧带鱼
    public static final DeferredHolder<Item, Item> BRAISED_HAIRTAIL = food("braised_hairtail", COOKED_MEAT);

// 红烧鱼籽
    public static final DeferredHolder<Item, Item> BRAISED_FISH_ROE = food("braised_fish_roe", COOKED_MEAT);

// 椒盐皮皮虾
    public static final DeferredHolder<Item, Item> SALT_PEPPER_MANTIS_SHRIMP = food("salt_pepper_mantis_shrimp", COOKED_MEAT);

// 金枪鱼寿司
    public static final DeferredHolder<Item, Item> TUNA_SUSHI = food("tuna_sushi", COOKED_MEAT);

// 烤鲅鱼肉
    public static final DeferredHolder<Item, Item> ROAST_MACKEREL = food("roast_mackerel", COOKED_MEAT);

// 烤蚕蛹
    public static final DeferredHolder<Item, Item> ROAST_SILKWORM_CHRYSALIS = food("roast_silkworm_chrysalis", COOKED_MEAT);

// 烤带鱼段
    public static final DeferredHolder<Item, Item> ROAST_HAIRTAIL_SEGMENT = food("roast_hairtail_segment", COOKED_MEAT);

// 烤剑鱼肉
    public static final DeferredHolder<Item, Item> ROAST_SWORDFISH = food("roast_swordfish", COOKED_MEAT);

// 烤金枪鱼肉
    public static final DeferredHolder<Item, Item> ROAST_TUNA = food("roast_tuna", COOKED_MEAT);

// 烤蚂蚱
    public static final DeferredHolder<Item, Item> ROAST_LOCUST = food("roast_locust", COOKED_MEAT);

// 烤鳗鱼段
    public static final DeferredHolder<Item, Item> ROAST_EEL_SEGMENT = food("roast_eel_segment", COOKED_MEAT);

// 烤知了猴
    public static final DeferredHolder<Item, Item> ROAST_CICADA = food("roast_cicada", COOKED_MEAT);

// 辣椒炒鲅鱼渣
    public static final DeferredHolder<Item, Item> CHILI_FRIED_MACKEREL_CRUMBLE = food("chili_fried_mackerel_crumble", COOKED_MEAT);

// 老醋蛰头
    public static final DeferredHolder<Item, Item> VINEGAR_JELLYFISH_HEAD = food("vinegar_jellyfish_head", COOKED_MEAT);

// 凉拌鱼皮
    public static final DeferredHolder<Item, Item> COLD_FISH_SKIN = food("cold_fish_skin", COOKED_MEAT);

// 麻辣田螺
    public static final DeferredHolder<Item, Item> SPICY_RIVER_SNAIL = food("spicy_river_snail", COOKED_MEAT);

// 鳗鱼饭
    public static final DeferredHolder<Item, Item> EEL_RICE = food("eel_rice", COOKED_MEAT);

// 清蒸老虎斑
    public static final DeferredHolder<Item, Item> STEAMED_GROUPER = food("steamed_grouper", COOKED_MEAT);

// 清蒸青蟹
    public static final DeferredHolder<Item, Item> STEAMED_MUD_CRAB = food("steamed_mud_crab", COOKED_MEAT);

// 清蒸梭子蟹
    public static final DeferredHolder<Item, Item> STEAMED_SWIMMING_CRAB = food("steamed_swimming_crab", COOKED_MEAT);

// 蒸龙虾（方块物品）：右键地面摆上桌，每次右键取一份熟龙虾肉，最后一份取 4 个蟹黄
    public static final DeferredHolder<Item, Item> STEAMED_LOBSTER =
            REGISTRY.register("steamed_lobster", () -> new BlockItem(ModBlocks.STEAMED_LOBSTER.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 蒸帝王蟹（方块物品）：同上，取熟帝王蟹腿棒
    public static final DeferredHolder<Item, Item> STEAMED_KING_CRAB =
            REGISTRY.register("steamed_king_crab", () -> new BlockItem(ModBlocks.STEAMED_KING_CRAB.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 蒸面包蟹（方块物品）：同上，取熟面包蟹肉
    public static final DeferredHolder<Item, Item> STEAMED_BROWN_CRAB =
            REGISTRY.register("steamed_brown_crab", () -> new BlockItem(ModBlocks.STEAMED_BROWN_CRAB.get(),
                    new Item.Properties().food(COOKED_MEAT)));

// 熟帝王蟹腿棒
    public static final DeferredHolder<Item, Item> COOKED_KING_CRAB_LEG = food("cooked_king_crab_leg", COOKED_MEAT);

// 熟龙虾肉
    public static final DeferredHolder<Item, Item> COOKED_LOBSTER_MEAT = food("cooked_lobster_meat", COOKED_MEAT);

// 熟面包蟹肉
    public static final DeferredHolder<Item, Item> COOKED_BROWN_CRAB_MEAT = food("cooked_brown_crab_meat", COOKED_MEAT);

// 水煮大虾
    public static final DeferredHolder<Item, Item> BOILED_PRAWN = food("boiled_prawn", COOKED_MEAT);

// 蒜蓉粉丝烤扇贝
    public static final DeferredHolder<Item, Item> GARLIC_VERMICELLI_SCALLOP = food("garlic_vermicelli_scallop", COOKED_MEAT);

// 蒜蓉粉丝烤生蚝
    public static final DeferredHolder<Item, Item> GARLIC_VERMICELLI_OYSTER = food("garlic_vermicelli_oyster", COOKED_MEAT);

// 油泡螺片
    public static final DeferredHolder<Item, Item> OIL_SOAKED_SNAIL_SLICES = food("oil_soaked_snail_slices", COOKED_MEAT);

// 鱼泡豆腐汤
    public static final DeferredHolder<Item, Item> FISH_MAW_TOFU_SOUP = food("fish_maw_tofu_soup", COOKED_MEAT);

// 炸蚕蛹
    public static final DeferredHolder<Item, Item> FRIED_SILKWORM_CHRYSALIS = food("fried_silkworm_chrysalis", COOKED_MEAT);

// 炸带鱼段
    public static final DeferredHolder<Item, Item> FRIED_HAIRTAIL_SEGMENT = food("fried_hairtail_segment", COOKED_MEAT);

// 炸蚂蚱
    public static final DeferredHolder<Item, Item> FRIED_LOCUST = food("fried_locust", COOKED_MEAT);

// 炸知了猴
    public static final DeferredHolder<Item, Item> FRIED_CICADA = food("fried_cicada", COOKED_MEAT);

// 纺织系列

// 蚕茧：养蚕产出的茧，缝纫机纺成丝绸的原料
    public static final DeferredHolder<Item, Item> SILKWORM_COCOON = item("silkworm_cocoon");

// 生蚕蛹：蚕茧缫丝时的副产物，食用数值同生肉
    public static final DeferredHolder<Item, Item> RAW_SILKWORM_CHRYSALIS = food("raw_silkworm_chrysalis", RAW_MEAT);

// 丝绸：右键极短蓄力后消耗一个，随机修复身上一件已损坏护甲的耐久
    public static final DeferredHolder<Item, Item> SILK =
            REGISTRY.register("silk", () -> new SilkItem(new Item.Properties()));

// 缝纫机（方块物品）：放置后空手右键打开界面，界面与切石机一致
    public static final DeferredHolder<Item, Item> SEWING_MACHINE =
            REGISTRY.register("sewing_machine", () -> new TooltipBlockItem(
                    ModBlocks.SEWING_MACHINE.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.sewing_machine"
            ));

// 空白丝绸帽子：缝纫机里作画的画布护甲，本身不提供防御
    public static final DeferredHolder<Item, Item> BLANK_SILK_HAT =
            REGISTRY.register("blank_silk_hat", () -> new SilkArmorItem(
                    ModArmorMaterials.SILK, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(ModArmorMaterials.SILK_DURABILITY))));

// 空白丝绸上衣
    public static final DeferredHolder<Item, Item> BLANK_SILK_TUNIC =
            REGISTRY.register("blank_silk_tunic", () -> new SilkArmorItem(
                    ModArmorMaterials.SILK, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(ModArmorMaterials.SILK_DURABILITY))));

// 空白丝绸下装
    public static final DeferredHolder<Item, Item> BLANK_SILK_LEGGINGS =
            REGISTRY.register("blank_silk_leggings", () -> new SilkArmorItem(
                    ModArmorMaterials.SILK, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(ModArmorMaterials.SILK_DURABILITY))));

// 空白丝绸鞋子
    public static final DeferredHolder<Item, Item> BLANK_SILK_BOOTS =
            REGISTRY.register("blank_silk_boots", () -> new SilkArmorItem(
                    ModArmorMaterials.SILK, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(ModArmorMaterials.SILK_DURABILITY))));

// 经济与生活

// 一种纸币：手里这张的「金融价值」直接作为注册时的默认组件写进去，
// tooltip 与存入银行卡时的折算都读它，不再各处硬编码数字。
    private static Item.Properties money(int value) {
        return new Item.Properties().component(ModDataComponents.MONEY_VALUE.get(), value);
    }

// 自助取款机（方块物品）：放置后拿银行卡右键打开银行卡界面
    public static final DeferredHolder<Item, Item> ATM =
            REGISTRY.register("atm", () -> new TooltipBlockItem(
                    ModBlocks.ATM.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.atm"
            ));

// 小票打印机（方块物品）：放置后空手右键查看今日菜单（40 种只展示的食物）
    public static final DeferredHolder<Item, Item> RECEIPT_PRINTER =
            REGISTRY.register("receipt_printer", () -> new TooltipBlockItem(
                    ModBlocks.RECEIPT_PRINTER.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.receipt_printer"
            ));

// 座垫标记：手持右键方块标记 / 取消座位，暂无自有贴图，用原版占位模型
    public static final DeferredHolder<Item, Item> SEAT_CUSHION =
            REGISTRY.register("seat_cushion", () -> new SeatCushionItem(
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.seat_cushion"
            ));

// 餐饮营业执照（方块物品）：挂在墙上，空手右键开业 / 打烊，开业后招来顾客堂食
    public static final DeferredHolder<Item, Item> CATERING_BUSINESS_LICENSE =
            REGISTRY.register("catering_business_license", () -> new TooltipBlockItem(
                    ModBlocks.CATERING_BUSINESS_LICENSE.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.catering_business_license"
            ));

// 商业电脑（方块物品）：放在地上空手 / 持物右键打开市场界面，可逛店、下单、自己开店
    public static final DeferredHolder<Item, Item> SHOPPING_COMPUTER =
            REGISTRY.register("shopping_computer", () -> new TooltipBlockItem(
                    ModBlocks.SHOPPING_COMPUTER.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.shopping_computer"
            ));

// 收购箱（方块物品）：27 格容器，把经济产物放进去空手右键打开界面点「出售」变现成纸币
    public static final DeferredHolder<Item, Item> PURCHASING_BOX =
            REGISTRY.register("purchasing_box", () -> new TooltipBlockItem(
                    ModBlocks.PURCHASING_BOX.get(),
                    new Item.Properties(),
                    "tooltip.flavor_immersed_daily_argo.purchasing_box"
            ));

// 银行卡：可绑定一位玩家，卡内 9 格只放六种纸币，卡面账户与金融价值随卡保存
    public static final DeferredHolder<Item, Item> CREDIT_CARD =
            REGISTRY.register("credit_card", () -> new CreditCardItem(new Item.Properties().stacksTo(1)));

// 一元纸币
    public static final DeferredHolder<Item, Item> MONEY_1 =
            REGISTRY.register("money_1", () -> new MoneyItem(money(1)));

// 五元纸币
    public static final DeferredHolder<Item, Item> MONEY_5 =
            REGISTRY.register("money_5", () -> new MoneyItem(money(5)));

// 十元纸币
    public static final DeferredHolder<Item, Item> MONEY_10 =
            REGISTRY.register("money_10", () -> new MoneyItem(money(10)));

// 二十元纸币
    public static final DeferredHolder<Item, Item> MONEY_20 =
            REGISTRY.register("money_20", () -> new MoneyItem(money(20)));

// 五十元纸币
    public static final DeferredHolder<Item, Item> MONEY_50 =
            REGISTRY.register("money_50", () -> new MoneyItem(money(50)));

// 一百元纸币
    public static final DeferredHolder<Item, Item> MONEY_100 =
            REGISTRY.register("money_100", () -> new MoneyItem(money(100)));

// 导航手机：潜行右键在展开 / 收起之间切换，展开后直接右键打开外卖接单界面
    public static final DeferredHolder<Item, Item> NAV_PHONE =
            REGISTRY.register("phone", () -> new NavPhoneItem(new Item.Properties().stacksTo(1)));

// 打包好的外卖：外卖袋捡起后得到，身上带着订单，送到目的地交付
    public static final DeferredHolder<Item, Item> TAKEAWAY_BAG =
            REGISTRY.register("takeaway_bag", () -> new TakeawayBagItem(new Item.Properties().stacksTo(1)));

// 注册辅助

// 普通物品，无 tooltip。
    private static DeferredHolder<Item, Item> item(String name) {
        return REGISTRY.register(name, () -> new Item(new Item.Properties()));
    }

// 可直接食用的普通物品。
    private static DeferredHolder<Item, Item> food(String name, FoodProperties food) {
        return REGISTRY.register(name, () -> new Item(new Item.Properties().food(food)));
    }

// 带 tooltip 的物品，tooltip 键按 tooltip.<modid>.<name> 约定生成。
    private static DeferredHolder<Item, Item> tooltipItem(String name) {
        return REGISTRY.register(name, () -> new TooltipItem(new Item.Properties(), tooltipKey(name)));
    }

// 带 tooltip 的可食用物品。
    private static DeferredHolder<Item, Item> tooltipFood(String name, FoodProperties food) {
        return REGISTRY.register(name, () -> new TooltipItem(new Item.Properties().food(food), tooltipKey(name)));
    }

    private static String tooltipKey(String name) {
        return "tooltip." + flavor_immersed_daily_argo.MODID + "." + name;
    }

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
