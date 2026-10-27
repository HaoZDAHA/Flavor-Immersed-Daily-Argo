package com.flavor_immersed_daily_argo.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

// 小票打印机的「每日菜单」。
//
// 候选池是当前游戏里所有模组（含原版）注册的、带食物组件的物品，剔除
// EconomyConfig#FOOD_BLACKLIST 里写下的物品后，按游戏天数确定性地抽 40 种。
// 同一天里所有打印机看到的是同一批，换天才会重新洗牌。
public final class DeliveryFoodPool {

// 每天展示的食物种数
    public static final int DAILY_COUNT = 40;

    private DeliveryFoodPool() {
    }

// 全部候选食物（已剔除黑名单），每次重新扫描，随数据包 / 模组变化而更新
    public static List<Item> candidates() {
        List<? extends String> blacklist = EconomyConfig.FOOD_BLACKLIST.get();
        List<Item> result = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || item.getFoodProperties() == null) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && blacklist.contains(id.toString())) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

// 第 day 天的每日菜单：把候选池用当天种子洗一遍再取前 #DAILY_COUNT 种。
//
// 候选不足 40 种时就整池返回，不做重复填充。
    public static List<Item> dailyFoods(long day) {
        List<Item> pool = candidateCopy();
        if (pool.isEmpty()) {
            return pool;
        }
// 打乱：同一天同一批，换天才变
        RandomSource random = RandomSource.create(day * 341873128712L + 132897987541L);
        for (int i = pool.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Item tmp = pool.get(i);
            pool.set(i, pool.get(j));
            pool.set(j, tmp);
        }
        return pool.subList(0, Math.min(DAILY_COUNT, pool.size()));
    }

    private static List<Item> candidateCopy() {
        return new ArrayList<>(candidates());
    }
}
