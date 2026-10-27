package com.flavor_immersed_daily_argo.economy.product;

import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 经济产物的合并视图：数据包（data/<命名空间>/economic_product/*.json）与配置文件两路并集。
//
// 同一物品两边都写了的话，以数据包那份为准（先放入者胜）。只在服务端使用——客户端要看的是
// 服务端算好的「今日涨跌」，随商业电脑界面 / 同步包下发。
public final class EconomicProductData {

// 数据包加载出来的经济产物（仅服务端资源重载后写入）
    private static volatile List<EconomicProduct> loaded = List.of();

    private EconomicProductData() {
    }

    static void setLoaded(List<EconomicProduct> products) {
        loaded = List.copyOf(products);
    }

// 数据包 + 配置文件合并后的全部经济产物（按物品去重）
    public static List<EconomicProduct> all() {
        Map<Item, EconomicProduct> merged = new LinkedHashMap<>();
        for (EconomicProduct product : loaded) {
            merged.putIfAbsent(product.item(), product);
        }
        for (EconomicProduct product : fromConfig()) {
            merged.putIfAbsent(product.item(), product);
        }
        return List.copyOf(merged.values());
    }

// 配置文件那一路：products.entries 每行 物品或标签,基准收购价。
// 标签以 # 开头（如 #fid:fruit），没写价格的用 products.default_price。
    private static List<EconomicProduct> fromConfig() {
        List<EconomicProduct> result = new ArrayList<>();
        for (String raw : EconomyConfig.PRODUCT_ENTRIES.get()) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String[] parts = raw.split(",");
            String key = parts[0].trim();
            if (key.isEmpty()) {
                continue;
            }
            int price = EconomyConfig.PRODUCT_DEFAULT_PRICE.get();
            if (parts.length >= 2) {
                try {
                    price = Math.max(1, Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {
// 价格写坏就用默认价
                }
            }
            if (key.startsWith("#")) {
                ResourceLocation rl = ResourceLocation.tryParse(key.substring(1));
                if (rl == null) {
                    continue;
                }
                int unit = price;
                BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, rl))
                        .ifPresent(set -> set.forEach(holder -> result.add(new EconomicProduct(holder.value(), unit))));
            } else {
                ResourceLocation rl = ResourceLocation.tryParse(key);
                Item item = rl == null ? Items.AIR : BuiltInRegistries.ITEM.get(rl);
                if (item != Items.AIR) {
                    result.add(new EconomicProduct(item, price));
                }
            }
        }
        return result;
    }
}
