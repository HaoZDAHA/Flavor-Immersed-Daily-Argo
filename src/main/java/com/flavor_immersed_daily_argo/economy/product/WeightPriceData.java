package com.flavor_immersed_daily_argo.economy.product;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import com.flavor_immersed_daily_argo.fishing.WeighingTypeData;
import com.flavor_immersed_daily_argo.herding.AnimalStatsManager;
import com.flavor_immersed_daily_argo.item.PoultryCatchingNetItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 按重量收购的两套价目：畜类（标准体重价 + 超出部分每公斤加价）与生鱼（每斤价）。
//
// 价目来自 EconomyConfig 的 weight_price 段。生鱼这一边只认能在
// weighing_types 数据包里称重的物品——称不了的东西没有斤数，也就没法按斤算钱。
// 只在服务端使用，商业电脑要展示的那份由 WeightPriceNetwork 下发。
//
// 配置里写的是基准价，实际每天按「天 + 物种 / 物品」算一个确定性浮动，幅度不超过
// #DAILY_SWING_PERCENT%；并且价格不会正好落在整元上，末尾总会带点零头。同一天里
// 商业电脑行情页显示的价格与收购箱结算的价格完全一致。
public final class WeightPriceData {

// 每日浮动幅度上限（基准价的百分比）
    public static final int DAILY_SWING_PERCENT = 20;

// 一种畜类的收购价：标准体重下的价格 + 超出标准体重每公斤加价
    public record AnimalPrice(EntityType<?> type, double basePrice, double extraPerKg) {
    }

// 一种生鱼的收购价：每斤多少元
    public record FishPrice(Item item, double perJin) {
    }

    private WeightPriceData() {
    }

// 配置里的畜类基准价（不含每日浮动），顺序与配置文件一致
    public static List<AnimalPrice> baseAnimals() {
        List<AnimalPrice> result = new ArrayList<>();
        for (String raw : EconomyConfig.WEIGHT_ANIMAL_ENTRIES.get()) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String[] parts = raw.split(",");
            if (parts.length < 3) {
                continue;
            }
            EntityType<?> type = EntityType.byString(parts[0].trim()).orElse(null);
            if (type == null) {
                continue;
            }
            try {
                result.add(new AnimalPrice(type,
                        Math.max(0.0D, Double.parseDouble(parts[1].trim())),
                        Math.max(0.0D, Double.parseDouble(parts[2].trim()))));
            } catch (NumberFormatException ignored) {
// 价格写坏就跳过这一条
            }
        }
        return List.copyOf(result);
    }

// 第 day 天的畜类收购价（含当日浮动），按标准价从高到低排
    public static List<AnimalPrice> animals(long day) {
        List<AnimalPrice> result = new ArrayList<>();
        for (AnimalPrice price : baseAnimals()) {
            String key = entityId(price.type());
            result.add(new AnimalPrice(price.type(),
                    swing(price.basePrice(), day, key + "|base"),
                    swing(price.extraPerKg(), day, key + "|kg")));
        }
        result.sort(Comparator.comparingDouble(AnimalPrice::basePrice).reversed()
                .thenComparing(price -> entityId(price.type())));
        return List.copyOf(result);
    }

// 配置里的生鱼基准价：先按「能称重」列全，再用配置逐样改价，按物品注册名排序
    public static List<FishPrice> baseFish() {
        Map<Item, Double> prices = new LinkedHashMap<>();
        double defaultPrice = EconomyConfig.WEIGHT_FISH_DEFAULT_PER_JIN.get();
        for (WeighingTypeData.Category category : WeighingTypeData.getInstance().categories()) {
            for (ResourceLocation id : category.weights().keySet()) {
                if (BuiltInRegistries.ITEM.containsKey(id)) {
                    prices.putIfAbsent(BuiltInRegistries.ITEM.get(id), defaultPrice);
                }
            }
        }
        for (String raw : EconomyConfig.WEIGHT_FISH_ENTRIES.get()) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String[] parts = raw.split(",");
            if (parts.length < 2) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
// 只改能称重的那批；配置里写了称不了的东西也没用
            if (!prices.containsKey(item)) {
                continue;
            }
            try {
                prices.put(item, Math.max(0.0D, Double.parseDouble(parts[1].trim())));
            } catch (NumberFormatException ignored) {
// 价格写坏就保留默认价
            }
        }
        List<FishPrice> result = new ArrayList<>(prices.size());
        prices.forEach((item, price) -> result.add(new FishPrice(item, price)));
        result.sort(Comparator.comparing(price -> BuiltInRegistries.ITEM.getKey(price.item()).toString()));
        return List.copyOf(result);
    }

// 第 day 天的生鱼收购价（含当日浮动），按每斤价从高到低排
    public static List<FishPrice> fish(long day) {
        List<FishPrice> result = new ArrayList<>();
        for (FishPrice price : baseFish()) {
            result.add(new FishPrice(price.item(), swing(price.perJin(), day, itemId(price.item()))));
        }
        result.sort(Comparator.comparingDouble(FishPrice::perJin).reversed()
                .thenComparing(price -> itemId(price.item())));
        return List.copyOf(result);
    }

// 第 day 天这种生鱼的每斤价；不是按斤收购的生鱼返回 0
    public static double perJin(Item item, long day) {
        for (FishPrice price : fish(day)) {
            if (price.item() == item) {
                return price.perJin();
            }
        }
        return 0.0D;
    }

// 全部「按斤收购」的生鱼物品；和每日浮动无关，按基准价判断
    public static Set<Item> weightSoldFish() {
        Set<Item> result = new HashSet<>();
        for (FishPrice price : baseFish()) {
            if (price.perJin() > 0.0D) {
                result.add(price.item());
            }
        }
        return result;
    }

// 一件生鱼第 day 天按重量算的钱。
//
// 上过秤就按秤上的斤数算，没上秤的用该物品重量分布的均值兜底（能直接丢进收购箱卖），
// 结果向上取整到元。
    public static int fishPrice(ItemStack stack, long day) {
        double pricePerJin = perJin(stack.getItem(), day);
        if (pricePerJin <= 0.0D) {
            return 0;
        }
        Double weighed = ModDataComponents.WEIGHED_WEIGHT.get().get(stack);
        double jin = weighed == null ? meanJin(stack) : weighed;
        return (int) Math.ceil(jin * pricePerJin);
    }

    private static double meanJin(ItemStack stack) {
        WeighingTypeData.Distribution distribution = WeighingTypeData.getInstance().forItem(stack);
        return distribution == null ? 0.0D : distribution.mean();
    }

// 第 day 天这种畜类的收购价；没有收购价返回 null
    @Nullable
    public static AnimalPrice animalPrice(EntityType<?> type, long day) {
        for (AnimalPrice price : animals(day)) {
            if (price.type() == type) {
                return price;
            }
        }
        return null;
    }

// 一袋动物第 day 天的收购价：标准体重价 + 超出标准体重部分 × 每公斤加价，向上取整到元。
//
// 空袋、认不出物种、或该物种没有收购价时返回 0（卖不掉，原样留在袋子里）。
    public static int sackPrice(ItemStack sack, long day) {
        CompoundTag data = PoultryCatchingNetItem.getStored(sack);
        if (data == null) {
            return 0;
        }
        EntityType<?> type = EntityType.byString(data.getString("id")).orElse(null);
        if (type == null) {
            return 0;
        }
        AnimalPrice price = animalPrice(type, day);
        if (price == null) {
            return 0;
        }
        Float standard = AnimalStatsManager.weightStandard(type);
        float baseWeight = standard == null ? 0.0F : standard;
        float weight = ModDataComponents.CAPTURED_WEIGHT.get().getOrDefault(sack, (double) baseWeight).floatValue();
        double extra = Math.max(0.0D, weight - baseWeight) * price.extraPerKg();
        return (int) Math.ceil(price.basePrice() + extra);
    }

// 某一天的价格：以 base 为基准上下浮动最多 #DAILY_SWING_PERCENT%，四舍五入到分；
// 正好落在整元上就再补一点零头，免得出现「10.00」这种规整价格。
    private static double swing(double base, long day, String key) {
        if (base <= 0.0D) {
            return 0.0D;
        }
        RandomSource random = RandomSource.create(seed(day, key));
        double span = base * DAILY_SWING_PERCENT / 100.0D;
        long cents = Math.round((base + (random.nextDouble() * 2.0D - 1.0D) * span) * 100.0D);
        if (cents % 100L == 0L) {
            cents += 1L + random.nextInt(99);
        }
        return cents / 100.0D;
    }

    private static long seed(long day, String key) {
        return day * 341873128712L + 132897987541L + key.hashCode();
    }

    private static String entityId(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return id == null ? "" : id.toString();
    }

    private static String itemId(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id == null ? "" : id.toString();
    }
}
