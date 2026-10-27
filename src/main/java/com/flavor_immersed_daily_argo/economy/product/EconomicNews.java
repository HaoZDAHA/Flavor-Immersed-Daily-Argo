package com.flavor_immersed_daily_argo.economy.product;

import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 一条经济产物新闻：某样产物今天的收购价与涨跌。
//
// price 是今日收购价（基准价 + 涨跌），delta 是今日涨跌——
// 单位是每组（#PRICE_UNIT 个）多少元，正数涨、负数跌。
public record EconomicNews(Item item, int price, int delta) {

// 报价单位：原版一整叠 64 个算「一组」，价格与涨跌都按每组算
    public static final int PRICE_UNIT = 64;

// 第 day 天的新闻：给每样经济产物按当天种子算一个确定性的涨跌，再按涨跌从高到低排序。
//
// 同一天里所有人看到的一样，换天才重新洗牌。涨跌幅度上限由
// EconomyConfig#PRODUCT_VOLATILITY（基准价的百分比）决定。
    public static List<EconomicNews> today(long day) {
        List<EconomicProduct> products = EconomicProductData.all();
// 能称重的生鱼按斤收购，不再进「每组」的普通产物行情
        Set<Item> byWeight = WeightPriceData.weightSoldFish();
        List<EconomicNews> news = new ArrayList<>(products.size());
        for (EconomicProduct product : products) {
            if (byWeight.contains(product.item())) {
                continue;
            }
            int delta = swing(day, product);
            news.add(new EconomicNews(product.item(), Math.max(0, product.price() + delta), delta));
        }
// 从长得最高排到跌得最狠；同幅度按物品注册名兜底，保证顺序稳定
        news.sort(Comparator.comparingInt(EconomicNews::delta).reversed()
                .thenComparing(EconomicNews::itemId));
        return news;
    }

// 第 day 天每样经济产物「每组」的今日收购价，供收购箱结算用。
//
// 与 #today(long) 同源，只是摊成「物品 → 价格」的查表。
    public static Map<Item, Integer> todayPrices(long day) {
        Map<Item, Integer> prices = new HashMap<>();
        for (EconomicNews news : today(day)) {
            prices.put(news.item(), news.price());
        }
        return prices;
    }

// 今天的涨跌：基准价 ± 波动上限内的一个确定性随机值
    private static int swing(long day, EconomicProduct product) {
        int span = Math.max(1, product.price() * EconomyConfig.PRODUCT_VOLATILITY.get() / 100);
        RandomSource random = RandomSource.create(seed(day, product.item()));
        return random.nextInt(span * 2 + 1) - span;
    }

    private static long seed(long day, Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return day * 341873128712L + 132897987541L + (id == null ? 0 : id.toString().hashCode());
    }

// 物品注册名，用作排序兜底键
    private static String itemId(EconomicNews news) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(news.item());
        return id == null ? "" : id.toString();
    }
}
