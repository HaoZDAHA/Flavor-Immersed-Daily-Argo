package com.flavor_immersed_daily_argo.config;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// 模组配置
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

// a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    public static final ModConfigSpec.BooleanValue GIVE_GUIDE_BOOK_ON_FIRST_JOIN = BUILDER
            .comment("Whether to give every player one Guide Book the first time they join. Each player only gets it once.")
            .define("giveGuideBookOnFirstJoin", true);

    public static final ModConfigSpec.BooleanValue GIVE_FISHING_HANDBOOK_ON_FIRST_JOIN = BUILDER
            .comment("Whether to give every player one Fishing Handbook the first time they join. Each player only gets it once.")
            .define("giveFishingHandbookOnFirstJoin", true);

    public static final ModConfigSpec.IntValue MOUSE_CHUNK_LIMIT = BUILDER
            .comment("How many mice may exist in a single chunk at the same time. Reaching this number stops further spawns in that chunk.")
            .defineInRange("mouseChunkLimit", 2, 1, 64);

    public static final ModConfigSpec.IntValue ANIMAL_CHUNK_LIMIT = BUILDER
            .comment("Maximum animals per chunk for natural/world-generation spawns (land and aquatic combined, excluding mice). "
                    + "Default 2. Existing animals count, but are never removed. Eggs, breeding and commands are not blocked; 0 stops natural spawns.")
            .defineInRange("animalChunkLimit", 2, 0, 64);

    public static final ModConfigSpec.BooleanValue MOUSE_NATURAL_SPAWNING = BUILDER
            .comment("Whether mice spawn naturally around mature crops. Off by default; "
                    + "temporarily toggle in game with /mouse spawn <true|false>, or use a mouse spawn egg.")
            .define("mouseNaturalSpawning", false);

    public static final ModConfigSpec.BooleanValue LAND_ANIMAL_NATURAL_SPAWNING = BUILDER
            .comment("Whether this mod's livestock and poultry (ducks, geese, quail, pigeons, turkeys, bullfrogs, "
                    + "bamboo rats) spawn naturally in the overworld. Off by default: they are meant to be bought "
                    + "as spawn eggs from the market. Enabling this makes them appear on their own.")
            .define("landAnimalNaturalSpawning", false);

    public static final ModConfigSpec.BooleanValue FRESHWATER_NATURAL_SPAWNING = BUILDER
            .comment("Whether this mod's freshwater aquatic animals spawn naturally in rivers, lakes, swamps and "
                    + "other non-ocean water. Off by default; they are sold as spawn eggs in the market.")
            .define("freshwaterNaturalSpawning", false);

    public static final ModConfigSpec.BooleanValue SALTWATER_NATURAL_SPAWNING = BUILDER
            .comment("Whether this mod's saltwater aquatic animals spawn naturally in ocean biomes. "
                    + "Off by default; they are sold as spawn eggs in the market.")
            .define("saltwaterNaturalSpawning", false);

    public static final ModConfigSpec.BooleanValue WARN_MISSING_PARENT = BUILDER
            .comment("Whether to remind players on login that fruit trees need the flavor_immersed_daily mod.")
            .define("warnMissingParentMod", true);

    public static final ModConfigSpec.BooleanValue PET_EXCRETION = BUILDER
            .comment("Whether cats and wolves leave droppings like livestock do. Off by default so pets "
                    + "do not spoil the look of a base; livestock and poultry still produce droppings either way.")
            .define("petExcretion", false);

// 畜牧状态数值：整合包可以在这里调快慢，无需改源码
    public static final ModConfigSpec.IntValue ANIMAL_DECAY_AMOUNT = BUILDER
            .comment("How many points satiety, thirst and cleanliness lose on each 30-second decay tick.")
            .defineInRange("animalDecayAmount", 1, 0, 10);

    public static final ModConfigSpec.IntValue ANIMAL_RESTORE_AMOUNT = BUILDER
            .comment("How many points thirst / cleanliness regain every 5 seconds from drinking water, "
                    + "standing in water or rain.")
            .defineInRange("animalRestoreAmount", 1, 0, 10);

// 进食频率：同一只动物两次被玩家喂食之间的最短间隔；0 表示不限
    public static final ModConfigSpec.IntValue ANIMAL_FEED_COOLDOWN_TICKS = BUILDER
            .comment("Minimum ticks between two hand-feedings of the same animal (20 ticks = 1 second). "
                    + "0 removes the limit. Trough feeding is paced by troughEatIntervalTicks instead.")
            .defineInRange("animalFeedCooldownTicks", 20, 0, 72000);

    public static final ModConfigSpec.IntValue ANIMAL_FEED_SATIETY = BUILDER
            .comment("Satiety gained each time a player hand-feeds an animal.")
            .defineInRange("animalFeedSatiety", 3, 0, 10);

    public static final ModConfigSpec.IntValue ANIMAL_FEED_MOOD = BUILDER
            .comment("Mood gained each time a player hand-feeds an animal.")
            .defineInRange("animalFeedMood", 1, 0, 10);

    public static final ModConfigSpec.DoubleValue ANIMAL_WEIGHT_STEP_RATIO = BUILDER
            .comment("How much of the standard weight is gained or lost per 30-second tick when the animal "
                    + "is in good or bad condition (0.02 = 2%).")
            .defineInRange("animalWeightStepRatio", 0.02D, 0.0D, 1.0D);

    public static final ModConfigSpec.IntValue ANIMAL_POOP_INTERVAL_TICKS = BUILDER
            .comment("Ticks between excretion attempts per player-fed animal (20 ticks = 1 second, default 600 = 30 seconds). "
                    + "Independent of hunger decay; only animals directly fed by a player may excrete. Trough feeding does not qualify.")
            .defineInRange("animalPoopIntervalTicks", 600, 1, 72000);

    public static final ModConfigSpec.DoubleValue ANIMAL_POOP_CHANCE = BUILDER
            .comment("Chance to leave a dropping on each animalPoopIntervalTicks attempt while fed and not starving (0.25 = 25%; 0 disables excretion).")
            .defineInRange("animalPoopChance", 0.25D, 0.0D, 1.0D);

    public static final ModConfigSpec.IntValue ANIMAL_FLY_BITE_CLEAN_LOSS = BUILDER
            .comment("Extra cleanliness lost per decay tick while the animal has fly bite.")
            .defineInRange("animalFlyBiteCleanLoss", 5, 0, 10);

// 食槽：低于阈值才来吃，吃到满才离开
    public static final ModConfigSpec.IntValue TROUGH_NEED_THRESHOLD = BUILDER
            .comment("An animal comes to the feeding trough only when its satiety (or thirst for water) "
                    + "is at or below this value.")
            .defineInRange("troughNeedThreshold", 5, 0, 9);

    public static final ModConfigSpec.IntValue TROUGH_RESTORE_AMOUNT = BUILDER
            .comment("Satiety / thirst restored by one portion from the feeding trough.")
            .defineInRange("troughRestoreAmount", 5, 1, 10);

    public static final ModConfigSpec.IntValue TROUGH_EAT_INTERVAL = BUILDER
            .comment("Ticks between two portions while an animal keeps eating at the trough.")
            .defineInRange("troughEatIntervalTicks", 20, 1, 72000);

// 额外可饲养动物：按饲料种类分别配置白名单
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXTRA_FEED_ANIMALS = BUILDER
            .comment("Extra animal entity ids (e.g. \"othermod:animal\") that can be kept like livestock "
                    + "and eat Grass Feed. They also get the animal status panel.")
            .defineListAllowEmpty("extraFeedAnimals", List.<String>of(), () -> "", Config::validateEntityName);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXTRA_SEED_FEED_ANIMALS = BUILDER
            .comment("Extra animal entity ids that eat Seed Feed. They also get the animal status panel.")
            .defineListAllowEmpty("extraSeedFeedAnimals", List.<String>of(), () -> "", Config::validateEntityName);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXTRA_MEAT_FEED_ANIMALS = BUILDER
            .comment("Extra animal entity ids that eat Meat Feed. They also get the animal status panel.")
            .defineListAllowEmpty("extraMeatFeedAnimals", List.<String>of(), () -> "", Config::validateEntityName);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    private static boolean validateEntityName(final Object obj) {
        return obj instanceof String entityName
                && BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(entityName));
    }
}
