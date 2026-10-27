package com.flavor_immersed_daily_argo.fishing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// 钓鱼手册条目。
//
// 由 datapack 中 data/<命名空间>/fishing_handbook/*.json 驱动，全部字段可选，
// 缺失时回落到默认值：生活水域=both（淡水/海水皆可）、生活水深=无限制、食性=杂食、
// 挣扎拉力=8、警惕性=中等、进食速度=中等、活动时段=全天全气候。
public class FishingHandbookEntry {

// 生活水域
    public enum Habitat {
        FRESH, OCEAN, BOTH;

        static Habitat from(String s) {
            return switch (s.toLowerCase(Locale.ROOT)) {
                case "fresh", "freshwater", "淡水" -> FRESH;
                case "ocean", "sea", "seawater", "海水" -> OCEAN;
                default -> BOTH;
            };
        }
    }

// 食性
    public enum Diet {
        OMNIVORE, HERBIVORE, CARNIVORE, SCAVENGER, LIVE;

        static Diet from(String s) {
            return switch (s.toLowerCase(Locale.ROOT)) {
                case "herbivore", "食草" -> HERBIVORE;
                case "carnivore", "食肉" -> CARNIVORE;
                case "scavenger", "食腐" -> SCAVENGER;
                case "live", "活体" -> LIVE;
                default -> OMNIVORE;
            };
        }

// 图鉴筛选与鱼饵提示共用的文案键
        public String translationKey() {
            return "book.flavor_immersed_daily_argo.diet." + this.name().toLowerCase(Locale.ROOT);
        }
    }

// 活动时段
    public enum Activity {
        ALL, DAY, NIGHT, SUNNY, RAIN;

        static Activity from(String s) {
            return switch (s.toLowerCase(Locale.ROOT)) {
                case "all", "全天" -> ALL;
                case "day", "昼" -> DAY;
                case "night", "夜" -> NIGHT;
                case "sunny", "晴" -> SUNNY;
                case "rain", "雨" -> RAIN;
                default -> null;
            };
        }
    }

// 警惕性
    public enum Alertness {
        LOW, MEDIUM, HIGH;

        static Alertness from(String s) {
            return switch (s.toLowerCase(Locale.ROOT)) {
                case "low", "低" -> LOW;
                case "high", "高" -> HIGH;
                default -> MEDIUM;
            };
        }

// 数值系数：低 0.5 / 中等 1.0 / 高 1.5，用于咬钩率与屏幕抖动
        public double factor() {
            return switch (this) {
                case LOW -> 0.5D;
                case HIGH -> 1.5D;
                default -> 1.0D;
            };
        }
    }

// 进食速度
    public enum FeedingSpeed {
        SLOW, MEDIUM, FAST;

        static FeedingSpeed from(String s) {
            return switch (s.toLowerCase(Locale.ROOT)) {
                case "slow", "慢" -> SLOW;
                case "fast", "快" -> FAST;
                default -> MEDIUM;
            };
        }

// 数值系数：慢 0.5 / 中等 1.0 / 快 1.5，作为玩家反应时间的除数
        public double factor() {
            return switch (this) {
                case SLOW -> 0.5D;
                case FAST -> 1.5D;
                default -> 1.0D;
            };
        }
    }

// 对应战利品物品的注册 id（仅实体形态的条目不填）
    public final ResourceLocation itemId;
// 对应生物实体的注册 id（活体战利品 / 实体图标用，可为空）
    public final ResourceLocation entityId;
// 显示名（缺省回落到物品 / 实体显示名）
    public final String name;
    public final Habitat habitat;
// 特定生物群系（完整 ResourceLocation 字符串，如 "minecraft:warm_ocean"），可为空
    public final List<String> biomes;
// 生活水深；空串表示无限制（界面用本地化文案显示）
    public final String depth;
    public final Diet diet;
    public final int struggleTension;
    public final Alertness alertness;
    public final FeedingSpeed feedingSpeed;
// 活动时段；空列表 = 全时间全气候
    public final List<Activity> activity;
// 可选简介（字面量）
    public final String description;
// 可选简介的本地化键（存在时优先于 description）
    public final String descriptionKey;
// 排序权重：大于 0 时置顶（先按权重升序，再按显示名）；0 表示不置顶
    public final int order;

    private ItemStack icon;
// 缓存解析后的显示名字符串（仅用于排序，运行时展示用 displayNameComponent）
    private String cachedDisplayName;
// 物品 / 实体形态是否真实存在于当前注册表；null 表示尚未解析
    private Boolean itemPresent;
    private Boolean entityPresent;

    public FishingHandbookEntry(ResourceLocation itemId, ResourceLocation entityId, String name, Habitat habitat,
                                List<String> biomes, String depth, Diet diet, int struggleTension,
                                Alertness alertness, FeedingSpeed feedingSpeed, List<Activity> activity,
                                String description, String descriptionKey, int order) {
        this.itemId = itemId;
        this.entityId = entityId;
        this.name = name;
        this.habitat = habitat;
        this.biomes = biomes;
        this.depth = depth;
        this.diet = diet;
        this.struggleTension = struggleTension;
        this.alertness = alertness;
        this.feedingSpeed = feedingSpeed;
        this.activity = activity;
        this.description = description;
        this.descriptionKey = descriptionKey;
        this.order = order;
    }

// 从 JSON 解析；item 字段非法时抛出异常，由加载器跳过该文件
    public static FishingHandbookEntry fromJson(JsonObject json) {
        ResourceLocation itemId = json.has("item") ? ResourceLocation.tryParse(json.get("item").getAsString()) : null;
        ResourceLocation entityId = json.has("entity") ? ResourceLocation.tryParse(json.get("entity").getAsString()) : null;
        if (itemId == null && entityId == null) {
            throw new IllegalArgumentException("缺少可用的 item / entity 字段");
        }

// 不写 name 时留空，运行时回退到物品的本地化显示名（跟随当前语言）
        String name = str(json, "name", null);

        Habitat habitat = Habitat.from(str(json, "habitat", "both"));
        Diet diet = Diet.from(str(json, "diet", "omnivore"));
        int tension = json.has("struggle_tension") ? json.get("struggle_tension").getAsInt() : 8;
        Alertness alertness = Alertness.from(str(json, "alertness", "medium"));
        FeedingSpeed speed = FeedingSpeed.from(str(json, "feeding_speed", "medium"));
        String depth = str(json, "depth", "");
        String description = str(json, "description", null);
        String descriptionKey = str(json, "description_key", null);
        int order = json.has("order") ? json.get("order").getAsInt() : 0;

        List<String> biomes = new ArrayList<>();
        if (json.has("biomes") && json.get("biomes").isJsonArray()) {
            for (var el : json.getAsJsonArray("biomes")) {
                if (el.isJsonPrimitive()) {
                    biomes.add(el.getAsString());
                }
            }
        }

        List<Activity> activity = new ArrayList<>();
        if (json.has("activity") && json.get("activity").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("activity");
            for (var el : arr) {
                Activity a = Activity.from(el.getAsString());
                if (a != null) {
                    activity.add(a);
                }
            }
        }

        return new FishingHandbookEntry(itemId, entityId, name, habitat, biomes, depth, diet, tension, alertness,
                speed, activity, description, descriptionKey, order);
    }

    private static String str(JsonObject json, String key, String def) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : def;
    }

// 物品形态在当前注册表里真实存在。
//
// 刻意不判断 JSON 里有没有写 item：各模组版本增删物品是常态，
// 只要那个版本里还有这件物品，条目就该照常可钓。
    public boolean hasItem() {
        if (this.itemPresent == null) {
            this.itemPresent = this.itemId != null
                    && BuiltInRegistries.ITEM.getOptional(this.itemId).isPresent();
        }
        return this.itemPresent;
    }

// 生物实体形态在当前注册表里真实存在（版本里没有该实体时按「无实体」处理）
    public boolean hasEntity() {
        if (this.entityPresent == null) {
            this.entityPresent = this.entityId != null
                    && BuiltInRegistries.ENTITY_TYPE.getOptional(this.entityId).isPresent();
        }
        return this.entityPresent;
    }

// 是否用生物实体作为图标（该版本里没有物品形态、但实体存在）
    public boolean usesEntityIcon() {
        return !hasItem() && hasEntity();
    }

// 图鉴「钓获记录」的记账键：优先实际存在的物品 id，其次实际存在的实体 id
    public ResourceLocation caughtKey() {
        if (hasItem()) {
            return this.itemId;
        }
        if (hasEntity()) {
            return this.entityId;
        }
        return this.itemId != null ? this.itemId : this.entityId;
    }

// 条目图标：直接渲染对应物品（无物品形态时返回空，由界面另行渲染实体）
    public ItemStack icon() {
        if (this.icon == null) {
            this.icon = hasItem()
                    ? BuiltInRegistries.ITEM.getOptional(this.itemId)
                            .map(Item::getDefaultInstance)
                            .orElse(ItemStack.EMPTY)
                    : ItemStack.EMPTY;
        }
        return this.icon;
    }

    public String displayName() {
        if (this.name != null && !this.name.isEmpty()) {
            return this.name;
        }
        if (this.cachedDisplayName == null) {
            this.cachedDisplayName = displayNameComponent().getString();
        }
        return this.cachedDisplayName;
    }

// 显示名组件：显式 name 优先，其次物品显示名，再次实体显示名（均随当前语言）
    public Component displayNameComponent() {
        if (this.name != null && !this.name.isEmpty()) {
            return Component.literal(this.name);
        }
        if (this.itemId != null) {
            Item item = BuiltInRegistries.ITEM.getOptional(this.itemId).orElse(null);
            if (item != null) {
                return item.getDefaultInstance().getHoverName();
            }
        }
        if (this.entityId != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(this.entityId).orElse(null);
            if (type != null) {
                return type.getDescription();
            }
        }
        return Component.literal(caughtKey().toString());
    }

// 简介组件：本地化键优先，其次字面量；都没有则返回 null
    public Component descriptionComponent() {
        if (this.descriptionKey != null && !this.descriptionKey.isEmpty()) {
            return Component.translatable(this.descriptionKey);
        }
        if (this.description != null && !this.description.isEmpty()) {
            return Component.literal(this.description);
        }
        return null;
    }

// 玩法侧：与钓竿参数对接

// 鱼的最大拉力（即手册中的「挣扎拉力」）
    public double fishTension() {
        return this.struggleTension;
    }

// 警惕性数值系数
    public double alertnessFactor() {
        return this.alertness.factor();
    }

// 进食速度数值系数
    public double feedingFactor() {
        return this.feedingSpeed.factor();
    }

// 生活水深是否无限制
    public boolean isDepthUnlimited() {
        return this.depth == null || this.depth.isBlank();
    }

// 生活水深下界；无限制返回 0
    public double depthMin() {
        if (isDepthUnlimited()) {
            return 0.0D;
        }
        String[] parts = this.depth.split("-");
        return parseDouble(parts[0], 0.0D);
    }

// 生活水深上界；无限制返回 Double#MAX_VALUE
    public double depthMax() {
        if (isDepthUnlimited()) {
            return Double.MAX_VALUE;
        }
        String[] parts = this.depth.split("-");
        if (parts.length > 1) {
            return parseDouble(parts[1], depthMin());
        }
        return parseDouble(parts[0], Double.MAX_VALUE);
    }

// 鱼的生活水深是否落在钓竿深度区间内（区间相交即算命中）
    public boolean depthMatches(double rodMin, double rodMax) {
        if (isDepthUnlimited()) {
            return true;
        }
        return rodMin <= depthMax() && depthMin() <= rodMax;
    }

// 战利品在当前游戏里真实存在（对应模组已安装且该版本仍有此物）。
// 物品与实体都不存在时该条目不可钓获，由加载器隐藏。
    public boolean available() {
        return hasItem() || hasEntity();
    }

// 当前生物群系是否符合条目的限定；列表为空表示不限群系
    public boolean biomesMatch(ResourceLocation biome) {
        return this.biomes.isEmpty() || (biome != null && this.biomes.contains(biome.toString()));
    }

// 生活习性是否与当前水域相符；both 表示淡海水皆可
    public boolean habitatMatches(boolean isOcean) {
        return switch (this.habitat) {
            case FRESH -> !isOcean;
            case OCEAN -> isOcean;
            case BOTH -> true;
        };
    }

// 活动时段是否与当前昼夜 / 天气相符；空列表或含 ALL 表示全天全气候
    public boolean activityMatches(boolean isDay, boolean isRaining) {
        if (this.activity.isEmpty() || this.activity.contains(Activity.ALL)) {
            return true;
        }
        for (Activity a : this.activity) {
            switch (a) {
                case DAY -> {
                    if (isDay) {
                        return true;
                    }
                }
                case NIGHT -> {
                    if (!isDay) {
                        return true;
                    }
                }
                case SUNNY -> {
                    if (!isRaining) {
                        return true;
                    }
                }
                case RAIN -> {
                    if (isRaining) {
                        return true;
                    }
                }
                default -> {
                }
            }
        }
        return false;
    }

    private static double parseDouble(String raw, double def) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            return def;
        }
    }

// 是否满足全部筛选条件；空集合表示该维度不限
    public boolean matches(Set<Habitat> habitats, Set<Diet> diets, Set<Activity> activities,
                           Set<Alertness> alertnesses, Set<FeedingSpeed> speeds) {
        if (!habitats.isEmpty() && !matchesHabitat(habitats)) {
            return false;
        }
        if (!diets.isEmpty() && !diets.contains(diet)) {
            return false;
        }
        if (!activities.isEmpty() && !matchesActivity(activities)) {
            return false;
        }
        if (!alertnesses.isEmpty() && !alertnesses.contains(alertness)) {
            return false;
        }
        return speeds.isEmpty() || speeds.contains(feedingSpeed);
    }

// 水域筛选：both（淡海水皆可）同时满足淡水与海水
    private boolean matchesHabitat(Set<Habitat> selected) {
        return selected.contains(habitat)
                || (habitat == Habitat.BOTH && (selected.contains(Habitat.FRESH) || selected.contains(Habitat.OCEAN)));
    }

// 活动时段筛选：空列表或含 ALL（全天）满足任意选中的时段
    private boolean matchesActivity(Set<Activity> selected) {
        if (activity.isEmpty() || activity.contains(Activity.ALL)) {
            return true;
        }
        for (Activity a : activity) {
            if (selected.contains(a)) {
                return true;
            }
        }
        return false;
    }
}
