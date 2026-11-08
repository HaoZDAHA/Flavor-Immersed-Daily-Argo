package com.flavor_immersed_daily_argo.config;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

// 模组配置
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

// a list of strings that are treated as resource locations for items
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    public static final ForgeConfigSpec.BooleanValue GIVE_GUIDE_BOOK_ON_FIRST_JOIN = BUILDER
            .comment("Whether to give every player one Guide Book the first time they join. Each player only gets it once.")
            .define("giveGuideBookOnFirstJoin", true);

    public static final ForgeConfigSpec.BooleanValue GIVE_FISHING_HANDBOOK_ON_FIRST_JOIN = BUILDER
            .comment("Whether to give every player one Fishing Handbook the first time they join. Each player only gets it once.")
            .define("giveFishingHandbookOnFirstJoin", true);

    public static final ForgeConfigSpec.IntValue MOUSE_CHUNK_LIMIT = BUILDER
            .comment("How many mice may exist in a single chunk at the same time. Reaching this number stops further spawns in that chunk.")
            .defineInRange("mouseChunkLimit", 2, 1, 64);

    public static final ForgeConfigSpec.BooleanValue MOUSE_NATURAL_SPAWNING = BUILDER
            .comment("Whether mice spawn naturally around mature crops. Off by default; "
                    + "temporarily toggle in game with /mouse spawn <true|false>, or use a mouse spawn egg.")
            .define("mouseNaturalSpawning", false);

    public static final ForgeConfigSpec.BooleanValue LAND_ANIMAL_NATURAL_SPAWNING = BUILDER
            .comment("Whether this mod's livestock and poultry (ducks, geese, quail, pigeons, turkeys, bullfrogs, "
                    + "bamboo rats) spawn naturally in the overworld. Off by default: they are meant to be bought "
                    + "as spawn eggs from the market. Enabling this makes them appear on their own.")
            .define("landAnimalNaturalSpawning", false);

    public static final ForgeConfigSpec.BooleanValue FRESHWATER_NATURAL_SPAWNING = BUILDER
            .comment("Whether this mod's freshwater aquatic animals spawn naturally in rivers, lakes, swamps and "
                    + "other non-ocean water. Off by default; they are sold as spawn eggs in the market.")
            .define("freshwaterNaturalSpawning", false);

    public static final ForgeConfigSpec.BooleanValue SALTWATER_NATURAL_SPAWNING = BUILDER
            .comment("Whether this mod's saltwater aquatic animals spawn naturally in ocean biomes. "
                    + "Off by default; they are sold as spawn eggs in the market.")
            .define("saltwaterNaturalSpawning", false);

    public static final ForgeConfigSpec.BooleanValue WARN_MISSING_PARENT = BUILDER
            .comment("Whether to remind players on login that fruit trees need the flavor_immersed_daily mod.")
            .define("warnMissingParentMod", true);

    public static final ForgeConfigSpec.BooleanValue PET_EXCRETION = BUILDER
            .comment("Whether cats and wolves leave droppings like livestock do. Off by default so pets "
                    + "do not spoil the look of a base; livestock and poultry still produce droppings either way.")
            .define("petExcretion", false);

    public static final ForgeConfigSpec.IntValue ANIMAL_DECAY_AMOUNT = BUILDER.defineInRange("animalDecayAmount", 1, 0, 10);
    public static final ForgeConfigSpec.IntValue ANIMAL_RESTORE_AMOUNT = BUILDER.defineInRange("animalRestoreAmount", 1, 0, 10);
    public static final ForgeConfigSpec.IntValue ANIMAL_FEED_SATIETY = BUILDER.defineInRange("animalFeedSatiety", 3, 0, 10);
    public static final ForgeConfigSpec.IntValue ANIMAL_FEED_MOOD = BUILDER.defineInRange("animalFeedMood", 1, 0, 10);
    public static final ForgeConfigSpec.DoubleValue ANIMAL_WEIGHT_STEP_RATIO = BUILDER.defineInRange("animalWeightStepRatio", 0.02D, 0.0D, 1.0D);
    public static final ForgeConfigSpec.DoubleValue ANIMAL_POOP_CHANCE = BUILDER.defineInRange("animalPoopChance", 0.25D, 0.0D, 1.0D);
    public static final ForgeConfigSpec.IntValue ANIMAL_FLY_BITE_CLEAN_LOSS = BUILDER.defineInRange("animalFlyBiteCleanLoss", 5, 0, 10);
    public static final ForgeConfigSpec.IntValue TROUGH_NEED_THRESHOLD = BUILDER.defineInRange("troughNeedThreshold", 5, 0, 9);
    public static final ForgeConfigSpec.IntValue TROUGH_RESTORE_AMOUNT = BUILDER.defineInRange("troughRestoreAmount", 5, 1, 10);
    public static final ForgeConfigSpec.IntValue TROUGH_FEED_INTERVAL = BUILDER.defineInRange("troughFeedInterval", 20, 1, 1200);
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_FEED_ANIMALS = BUILDER
            .comment("Additional Animal-subclass entity IDs; these accept grass feed unless explicitly mapped. Does not support arbitrary mobs. Missing mods are ignored.")
            .defineListAllowEmpty("extraFeedAnimals", List.of(), value -> value instanceof String id && ResourceLocation.tryParse(id) != null);
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ANIMAL_FEED_OVERRIDES = BUILDER
            .comment("Server authoritative Animal feed overrides: entity_id=item_id,item_id. An empty right side disables feeding. Overrides replace built-in foods. Each feeding consumes one item; do not map container items. Reload worlds after changing mappings to rebuild native tempt goals.")
            .defineListAllowEmpty("animalFeedOverrides", List.of(), Config::validateFeedOverride);

    private static boolean validateFeedOverride(Object value) {
        if (!(value instanceof String entry)) return false;
        String[] parts = entry.split("=", -1);
        if (parts.length != 2 || ResourceLocation.tryParse(parts[0].trim()) == null) return false;
        if (parts[1].isBlank()) return true;
        for (String item : parts[1].split(",", -1)) {
            if (ResourceLocation.tryParse(item.trim()) == null) return false;
        }
        return true;
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemName)) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(itemName);
        return id != null && BuiltInRegistries.ITEM.containsKey(id);
    }
}
