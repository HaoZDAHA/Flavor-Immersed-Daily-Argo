package com.flavor_immersed_daily_argo.economy;

import net.minecraft.core.component.DataComponents;
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
// 默认只扫描 namespace 以 flavor_immersed_daily 开头的食物；额外模组白名单
// 或全模组开关可扩大范围。黑名单最后生效，再按游戏天数确定性地抽 40 种。
// 同一天里所有打印机看到的是同一批，换天才会重新洗牌。
public final class DeliveryFoodPool {

// 每天展示的食物种数
    public static final int DAILY_COUNT = 40;

    private DeliveryFoodPool() {
    }

// 全部候选食物（已剔除黑名单），每次重新扫描，随数据包 / 模组变化而更新
    public static List<Item> candidates() {
        List<? extends String> blacklist = EconomyConfig.FOOD_BLACKLIST.get();
        List<? extends String> extraMods = EconomyConfig.ORDER_EXTRA_FOOD_MODS.get();
        boolean allowAll = EconomyConfig.ORDER_ALLOW_ALL_FOOD_MODS.get();
        List<Item> result = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || new ItemStack(item).get(DataComponents.FOOD) == null) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !allowsNamespace(id.getNamespace(), allowAll, extraMods)
                    || blacklist.contains(id.toString())) {
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

    public static boolean allowsNamespace(String namespace, boolean allowAll, List<? extends String> extraMods) {
        return allowAll || namespace.startsWith("flavor_immersed_daily") || extraMods.contains(namespace);
    }

    private static List<Item> candidateCopy() {
        return new ArrayList<>(candidates());
    }
}
