package com.flavor_immersed_daily_argo.economy.product;

import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Conservative, server-only discovery. It never rolls loot or spawns animals in the world. */
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class AutomaticProductDiscovery extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Map<String, JsonElement> loot = Map.of();
    private static MinecraftServer server;
    private static List<Item> candidates;

    public AutomaticProductDiscovery() {
        super(new Gson(), "loot_table");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        loot = objects.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                entry -> entry.getKey().toString(), Map.Entry::getValue));
        invalidate();
    }

    public static void invalidate() {
        candidates = null;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        server = event.getServer();
        invalidate();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        server = null;
        loot = Map.of();
        invalidate();
    }

    public static List<EconomicProduct> products() {
        if (!EconomyConfig.AUTO_PRODUCTS_ENABLED.get() || server == null) {
            return List.of();
        }
        if (candidates == null) {
            candidates = discover();
        }
        int min = EconomyConfig.AUTO_PRODUCTS_MIN_PRICE.get();
        int max = EconomyConfig.AUTO_PRODUCTS_MAX_PRICE.get();
        List<EconomicProduct> result = new ArrayList<>();
        for (Item item : candidates) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!blocked(item, id)) {
                double scaled = AutomaticProductRules.price(id.toString(), min, max)
                        * EconomyConfig.AUTO_PRODUCT_PRICE_MULTIPLIER.get();
                if (scaled > 0.0D) {
                    result.add(new EconomicProduct(item, (int) Math.min(1_000_000L, Math.max(1L, Math.round(scaled)))));
                }
            }
        }
        return result;
    }

    private static boolean blocked(Item item, ResourceLocation id) {
        for (String entry : EconomyConfig.AUTO_PRODUCTS_BLACKLIST.get()) {
            String rule = entry.trim();
            if (rule.startsWith("#")) {
                ResourceLocation tag = ResourceLocation.tryParse(rule.substring(1));
                if (tag != null && item.builtInRegistryHolder().is(TagKey.create(Registries.ITEM, tag))) {
                    return true;
                }
            } else if (AutomaticProductRules.idBlocked(id.toString(), rule)) {
                return true;
            }
        }
        return false;
    }

    private static List<Item> discover() {
        Set<Item> found = new LinkedHashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (eligible(item) && item.builtInRegistryHolder().tags().anyMatch(tag ->
                    AutomaticProductRules.isFarmTag(tag.location().getNamespace(), tag.location().getPath()))) {
                found.add(item);
            }
        }
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!AutomaticProductRules.isOtherMod(id.getNamespace())) {
                continue;
            }
            boolean crop = block instanceof CropBlock || block instanceof StemBlock || block instanceof CocoaBlock
                    || block instanceof SweetBerryBushBlock || block instanceof NetherWartBlock
                    || block.builtInRegistryHolder().tags().anyMatch(tag ->
                    (tag.location().getNamespace().equals("c") || tag.location().getNamespace().equals("forge"))
                            && (tag.location().getPath().equals("crops") || tag.location().getPath().startsWith("crops/")));
            if (crop) {
                collectTable(block.getLootTable().location(), found);
            }
        }
        // Only construct unspawned prototypes once per reload, and only from animal mob categories.
        // This is necessary because EntityType has no public entity-class query.
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!AutomaticProductRules.isOtherMod(id.getNamespace()) || !animalCategory(type.getCategory())) {
                continue;
            }
            try {
                Entity prototype = type.create(server.overworld());
                if (prototype instanceof Animal animal) {
                    collectTable(animal.getLootTable().location(), found);
                } else if (prototype instanceof WaterAnimal animal) {
                    collectTable(animal.getLootTable().location(), found);
                }
            } catch (RuntimeException exception) {
                LOGGER.debug("Skipping unsupported animal prototype {}: {}", id, exception.toString());
            }
        }
        List<Item> result = found.stream().filter(AutomaticProductDiscovery::eligible)
                .sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString())).toList();
        LOGGER.info("Discovered {} other-mod farming products for the shipping box", result.size());
        return result;
    }

    private static boolean eligible(Item item) {
        return item != Items.AIR && AutomaticProductRules.isOtherMod(BuiltInRegistries.ITEM.getKey(item).getNamespace());
    }

    private static boolean animalCategory(MobCategory category) {
        return category == MobCategory.CREATURE || category == MobCategory.WATER_CREATURE
                || category == MobCategory.WATER_AMBIENT || category == MobCategory.UNDERGROUND_WATER_CREATURE
                || category == MobCategory.AXOLOTLS;
    }

    private static void collectTable(ResourceLocation id, Set<Item> result) {
        LootProductEntries.Result entries = LootProductEntries.find(loot, id.toString());
        for (String name : entries.items()) {
            ResourceLocation itemId = ResourceLocation.tryParse(name);
            if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
                result.add(BuiltInRegistries.ITEM.get(itemId));
            }
        }
        for (String name : entries.tags()) {
            ResourceLocation tag = ResourceLocation.tryParse(name);
            if (tag != null) {
                BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tag))
                        .ifPresent(set -> set.forEach(holder -> result.add(holder.value())));
            }
        }
    }
}
