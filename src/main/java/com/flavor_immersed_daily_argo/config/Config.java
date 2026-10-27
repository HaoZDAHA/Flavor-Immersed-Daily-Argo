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

    public static final ModConfigSpec.IntValue MOUSE_CHUNK_LIMIT = BUILDER
            .comment("How many mice may exist in a single chunk at the same time. Reaching this number stops further spawns in that chunk.")
            .defineInRange("mouseChunkLimit", 2, 1, 64);

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

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
