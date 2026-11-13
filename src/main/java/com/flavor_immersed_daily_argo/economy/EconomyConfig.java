package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.economy.shop.Shop;
import com.flavor_immersed_daily_argo.economy.shop.ShopEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

// 经济玩法配置：外汇兑换条目。
//
// 配置项 exchange.entries 里每行一条，格式为
// 本模组货币物品,数量,其他模组物品,数量，例如默认的
// flavor_immersed_daily_argo:money_5,64,minecraft:emerald,1。
//
// 也可以用指令增删：/fidaeco add|delete|list。
public final class EconomyConfig {

// 默认：64 张五元纸币（折合 320）换 1 个绿宝石
    private static final String DEFAULT_ENTRY =
            "flavor_immersed_daily_argo:money_5,64,minecraft:emerald,1";

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXCHANGES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FOOD_BLACKLIST;
    public static final ModConfigSpec.BooleanValue ORDER_ALLOW_ALL_FOOD_MODS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ORDER_EXTRA_FOOD_MODS;
    public static final ModConfigSpec.BooleanValue AUTO_PRODUCTS_ENABLED;
    public static final ModConfigSpec.IntValue AUTO_PRODUCTS_MIN_PRICE;
    public static final ModConfigSpec.IntValue AUTO_PRODUCTS_MAX_PRICE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> AUTO_PRODUCTS_BLACKLIST;
// 是否允许玩家开店
    public static final ModConfigSpec.BooleanValue PLAYER_SHOPS_ENABLED;
// 每位玩家最多能开几家店
    public static final ModConfigSpec.IntValue MAX_SHOPS_PER_PLAYER;
// 配置文件商店条目
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXTRA_ENTRIES;
// 配置文件里的经济产物条目（物品或标签,基准收购价）
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PRODUCT_ENTRIES;
// 经济产物没写价格时的默认基准收购价（元 / 每组 64 个）
    public static final ModConfigSpec.IntValue PRODUCT_DEFAULT_PRICE;
// 每日涨跌幅度上限（基准收购价的百分比）
    public static final ModConfigSpec.IntValue PRODUCT_VOLATILITY;
// 按重量收购：畜类条目（实体注册名,标准体重价,超出每公斤加价）
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WEIGHT_ANIMAL_ENTRIES;
// 按重量收购：生鱼条目（物品注册名,每斤价），只对能称重的鱼生效
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WEIGHT_FISH_ENTRIES;
// 生鱼没单独写价时用的每斤价
    public static final ModConfigSpec.DoubleValue WEIGHT_FISH_DEFAULT_PER_JIN;
// 外卖配送费区间（元）：按距离折算后再夹到这两条线之间
    public static final ModConfigSpec.IntValue TAKEOUT_MIN_FEE;
    public static final ModConfigSpec.IntValue TAKEOUT_MAX_FEE;
    public static final ModConfigSpec.IntValue FOOD_ORDER_RAW_MIN;
    public static final ModConfigSpec.IntValue FOOD_ORDER_RAW_MAX;
    public static final ModConfigSpec.IntValue FOOD_ORDER_SIMPLE_MIN;
    public static final ModConfigSpec.IntValue FOOD_ORDER_SIMPLE_MAX;
    public static final ModConfigSpec.IntValue FOOD_ORDER_COMPLEX_MIN;
    public static final ModConfigSpec.IntValue FOOD_ORDER_COMPLEX_MAX;
    public static final ModConfigSpec.DoubleValue FOOD_ORDER_COST_MULTIPLIER;
    public static final ModConfigSpec.IntValue FOOD_ORDER_PROCESSING_FEE;
    public static final ModConfigSpec.DoubleValue FISH_PRICE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue ANIMAL_PRICE_MULTIPLIER;
    public static final ModConfigSpec.IntValue ANIMAL_MIN_BASE_PRICE;
    public static final ModConfigSpec.DoubleValue AUTO_PRODUCT_PRICE_MULTIPLIER;

// 畜类默认收购价：标准体重下的价格 + 超出标准体重每公斤加价
    private static final List<String> DEFAULT_ANIMAL_PRICES = List.of(
            "minecraft:chicken,10,4",
            "flavor_immersed_daily_argo:duck,12,4",
            "flavor_immersed_daily_argo:goose,18,5",
            "flavor_immersed_daily_argo:quail,2,6",
            "flavor_immersed_daily_argo:pigeon,3,6",
            "flavor_immersed_daily_argo:turkey,36,4",
            "minecraft:pig,260,3",
            "minecraft:cow,900,3",
            "minecraft:sheep,140,3.5",
            "minecraft:goat,90,3.5",
            "minecraft:rabbit,8,5",
            "minecraft:horse,800,2.5",
            "minecraft:donkey,260,2.5",
            "minecraft:camel,900,3",
            "minecraft:llama,260,3",
            "flavor_immersed_daily_argo:bamboorat,10,4",
            "flavor_immersed_daily_argo:bullfrog,4,6");

// 生鱼默认每斤价：只列了能称重的那批，没列的用 default_price_per_jin
    private static final List<String> DEFAULT_FISH_PRICES = List.of(
            "flavor_immersed_daily_argo:raw_grass_carp,6.0",
            "flavor_immersed_daily_argo:raw_black_carp,9.5",
            "flavor_immersed_daily_argo:raw_silver_carp,7.0",
            "flavor_immersed_daily_argo:raw_bighead_carp,8.5",
            "flavor_immersed_daily_argo:raw_chinese_carp,9.0",
            "flavor_immersed_daily_argo:raw_crucian_carp,6.5",
            "flavor_immersed_daily_argo:raw_catfish,11.0",
            "flavor_immersed_daily_argo:raw_black_fish,15.0",
            "flavor_immersed_daily_argo:raw_bass,13.0",
            "flavor_immersed_daily_argo:raw_mandarin_fish,12.0",
            "flavor_immersed_daily_argo:raw_loach,10.0",
            "flavor_immersed_daily_argo:raw_goby,9.0",
            "flavor_immersed_daily_argo:raw_pleco,6.0",
            "flavor_immersed_daily_argo:raw_redfin,7.5",
            "flavor_immersed_daily_argo:raw_tench,8.0",
            "flavor_immersed_daily_argo:raw_tilapia,8.0",
            "flavor_immersed_daily_argo:raw_rectum,10.0");

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("经济玩法（银行卡 / 自助取款机）").push("exchange");
        EXCHANGES = builder
                .comment("外汇兑换条目，每条写成「本模组货币物品,数量,其他模组物品,数量」，",
                        "例如 flavor_immersed_daily_argo:money_5,64,minecraft:emerald,1。",
                        "指令 /fidaeco add|delete|list 会直接读写这里。")
                .defineListAllowEmpty("entries", List.of(DEFAULT_ENTRY), () -> DEFAULT_ENTRY,
                        value -> value instanceof String);
        builder.pop();

        builder.comment("外卖订单（小票打印机 / 外卖小哥）").push("takeout");
        FOOD_BLACKLIST = builder
                .comment("小票打印机每日菜单的食物黑名单：写在这里的物品不会出现在打印机里。",
                        "每行一条物品注册名，例如 minecraft:rotten_flesh。")
                .defineListAllowEmpty("food_blacklist", List.of(), () -> "",
                        value -> value instanceof String);
        ORDER_ALLOW_ALL_FOOD_MODS = builder
                .comment("允许订单从全部模组选择食物，黑名单仍生效。默认关闭。",
                        "Allow foods from all namespaces in orders; the food blacklist still applies.")
                .define("allow_all_food_mods", false);
        ORDER_EXTRA_FOOD_MODS = builder
                .comment("额外允许进入订单的模组命名空间（精确匹配）。默认仅允许 flavor_immersed_daily 前缀。",
                        "Additional exact mod ids allowed in food orders. The flavor_immersed_daily prefix is always allowed.")
                .defineListAllowEmpty("extra_food_mods", List.<String>of(), () -> "",
                        value -> value instanceof String s && s.matches("[a-z0-9_.-]+"));
        TAKEOUT_MIN_FEE = builder
                .comment("外卖配送费的最低值（元）。按距离算出来的配送费低于这个数时取这个数。")
                .defineInRange("min_delivery_fee", 5, 0, 1_000_000);
        TAKEOUT_MAX_FEE = builder
                .comment("外卖配送费的最高值（元）。按距离算出来的配送费高于这个数时取这个数。")
                .defineInRange("max_delivery_fee", 60, 0, 1_000_000);
        builder.pop();

        builder.comment("做菜订单收益 / Cooking-order rewards per serving").push("food_orders");
        FOOD_ORDER_RAW_MIN = builder.comment("生食每份奖励下限 / Minimum raw-food reward per serving")
                .defineInRange("raw_min", 2, 0, 1_000_000);
        FOOD_ORDER_RAW_MAX = builder.comment("生食每份奖励上限 / Maximum raw-food reward per serving")
                .defineInRange("raw_max", 6, 0, 1_000_000);
        FOOD_ORDER_SIMPLE_MIN = builder.comment("简单料理每份奖励下限 / Minimum simple-dish reward")
                .defineInRange("simple_min", 12, 0, 1_000_000);
        FOOD_ORDER_SIMPLE_MAX = builder.comment("简单料理每份奖励上限 / Maximum simple-dish reward")
                .defineInRange("simple_max", 24, 0, 1_000_000);
        FOOD_ORDER_COMPLEX_MIN = builder.comment("复杂料理每份奖励下限 / Minimum complex-dish reward")
                .defineInRange("complex_min", 30, 0, 1_000_000);
        FOOD_ORDER_COMPLEX_MAX = builder.comment("复杂料理常规每份奖励上限；原料成本保护可超过 / Normal complex-dish reward cap; cost protection may exceed it")
                .defineInRange("complex_max", 80, 0, 1_000_000);
        FOOD_ORDER_COST_MULTIPLIER = builder.comment("可识别配方的每份原料收购价值保护倍率 / Ingredient opportunity-cost margin per serving")
                .defineInRange("ingredient_margin", 1.35D, 1.0D, 10.0D);
        FOOD_ORDER_PROCESSING_FEE = builder.comment("料理每份基础加工费 / Processing fee per cooked serving")
                .defineInRange("processing_fee", 4, 0, 1000);
        builder.pop();

        builder.comment("经济平衡系数，不修改已有基础报价或配送费 / Balance multipliers; delivery fees unchanged").push("balance");
        FISH_PRICE_MULTIPLIER = builder.comment("生鱼每斤价倍率 / Fish price multiplier")
                .defineInRange("fish_price_multiplier", 1.5D, 0.0D, 100.0D);
        ANIMAL_PRICE_MULTIPLIER = builder.comment("活畜标准价及超重价倍率 / Livestock base and overweight price multiplier")
                .defineInRange("animal_price_multiplier", 2.0D, 0.0D, 100.0D);
        ANIMAL_MIN_BASE_PRICE = builder.comment("有正收购价的小型活畜最低标准价 / Minimum standard price for positively priced livestock")
                .defineInRange("animal_min_base_price", 24, 0, 1_000_000);
        AUTO_PRODUCT_PRICE_MULTIPLIER = builder.comment("其他模组自动产物的基准价格倍率，原5~20变为40~160 / Automatic-product base multiplier, before daily fluctuation")
                .defineInRange("auto_product_price_multiplier", 8.0D, 0.0D, 100.0D);
        builder.pop();

        builder.comment("商业电脑（市场店铺）").push("shop");
        PLAYER_SHOPS_ENABLED = builder
                .comment("是否允许玩家在商业电脑的「开店」页创建店铺。")
                .define("player_shops_enabled", true);
        MAX_SHOPS_PER_PLAYER = builder
                .comment("每位玩家最多能开几家店。")
                .defineInRange("max_shops_per_player", 3, 0, 64);
        EXTRA_ENTRIES = builder
                .comment("配置文件商店的条目，每条写成「物品注册名,数量,价格」，",
                        "例如 minecraft:apple,4,6。所有有效行并入一个「配置文件商店」，坏行忽略。")
                .defineListAllowEmpty("extra_entries", List.of(), () -> "",
                        value -> value instanceof String);
        builder.pop();

        builder.comment("经济产物（新闻页的今日涨跌）").push("products");
        PRODUCT_ENTRIES = builder
                .comment("配置文件里的经济产物，每条写成「物品或标签，基准收购价(元/每组64个)」，",
                        "例如 minecraft:apple,8 或 #fid:fruit,8（标签以 # 开头）。",
                        "会与数据包里的 data/<命名空间>/economic_product/*.json 合并，同物品以数据包为准。")
                .defineListAllowEmpty("entries", List.of(), () -> "",
                        value -> value instanceof String);
        PRODUCT_DEFAULT_PRICE = builder
                .comment("经济产物没写价格时用的默认基准收购价（元 / 每组 64 个）。")
                .defineInRange("default_price", 8, 1, 1_000_000);
        PRODUCT_VOLATILITY = builder
                .comment("每日涨跌幅度上限，按基准收购价的百分比算；例如 25 表示最多涨跌四分之一。")
                .defineInRange("volatility_percent", 25, 0, 100);
        builder.pop();

        builder.comment("其他模组农牧产物自动识别 / Other-mod farming products").push("auto_products");
        AUTO_PRODUCTS_ENABLED = builder
                .comment("自动识别其他模组的农作物与动物掉落物并添加到出货箱和行情。",
                        "Automatically buy other-mod crops and animal drops identified by tags and loot tables.")
                .define("enabled", true);
        AUTO_PRODUCTS_MIN_PRICE = builder
                .comment("自动识别产物的最低基准价（元/一组，按物品堆叠上限）。",
                        "Minimum automatic base price per stack; daily market volatility still applies.")
                .defineInRange("min_price", 5, 1, 1_000_000);
        AUTO_PRODUCTS_MAX_PRICE = builder
                .comment("自动识别产物的最高基准价。写反时交换上下限；同物品基准价保持确定性。",
                        "Maximum automatic base price. Reversed bounds are normalized; prices are deterministic.")
                .defineInRange("max_price", 20, 1, 1_000_000);
        AUTO_PRODUCTS_BLACKLIST = builder
                .comment("自动识别黑名单：物品id、模组:* 或 #物品标签；不覆盖手工配置/数据包的显式价格。",
                        "Blacklist item ids, namespace:* or #item_tag. Explicit product prices retain priority.")
                .defineListAllowEmpty("blacklist", List.<String>of(), () -> "", value -> value instanceof String);
        builder.pop();

        builder.comment("按重量收购（畜类 / 生鱼）").push("weight_price");
        WEIGHT_ANIMAL_ENTRIES = builder
                .comment("畜类的收购价，每条写成「实体注册名,标准体重价,超出标准体重每公斤加价」。",
                        "标准体重见各动物的体重标准；体重低于标准时按标准体重价收，超出部分按每公斤加价另算。",
                        "装进编制麻袋的动物才能出售，价格向上取整到元。")
                .defineListAllowEmpty("animal_entries", DEFAULT_ANIMAL_PRICES, () -> "minecraft:pig,260,3",
                        value -> value instanceof String);
        WEIGHT_FISH_ENTRIES = builder
                .comment("生鱼每斤的收购价，每条写成「物品注册名,每斤价」，可以写小数。",
                        "只对能在 weighing_types 数据包里称重的物品生效；没写价的用下面的默认每斤价。")
                .defineListAllowEmpty("fish_entries", DEFAULT_FISH_PRICES, () -> "flavor_immersed_daily_argo:raw_grass_carp,6.0",
                        value -> value instanceof String);
        WEIGHT_FISH_DEFAULT_PER_JIN = builder
                .comment("生鱼没在 fish_entries 里单独写价时用的每斤价。")
                .defineInRange("fish_default_price_per_jin", 6.0D, 0.0D, 1_000_000.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private EconomyConfig() {
    }

// 配置文件商店的标题语言键
    public static final String CONFIG_SHOP_TITLE_KEY = "gui.flavor_immersed_daily_argo.shop.config_shop";

// 由 shop.extra_entries 驱动的系统店铺（「适应 config」那一路）。
//
// 每行 物品注册名,数量,价格，全部并入同一家店；一条有效条目都没有就返回 null。
    public static Shop configShop() {
        List<ShopEntry> entries = new ArrayList<>();
        for (String raw : EXTRA_ENTRIES.get()) {
            if (raw == null) {
                continue;
            }
            String[] parts = raw.split(",");
            if (parts.length < 3) {
                continue;
            }
            ResourceLocation rl = ResourceLocation.tryParse(parts[0].trim());
            Item item = rl == null ? Items.AIR : BuiltInRegistries.ITEM.get(rl);
            if (item == Items.AIR) {
                continue;
            }
            int count;
            int price;
            try {
                count = Math.max(1, Integer.parseInt(parts[1].trim()));
                price = Math.max(0, Integer.parseInt(parts[2].trim()));
            } catch (NumberFormatException ex) {
                continue;
            }
            entries.add(new ShopEntry(item, count, price, -1));
        }
        if (entries.isEmpty()) {
            return null;
        }
        return new Shop("config:shop", CONFIG_SHOP_TITLE_KEY, "", List.copyOf(entries), 0L, false);
    }

// 当前生效的兑换条目（写坏的行会被跳过）
    public static List<ExchangeEntry> entries() {
        List<ExchangeEntry> result = new ArrayList<>();
        for (String raw : EXCHANGES.get()) {
            ExchangeEntry entry = ExchangeEntry.parse(raw);
            if (entry != null) {
                result.add(entry);
            }
        }
        return result;
    }

// 新增一条；已存在就不重复加
    public static boolean add(ExchangeEntry entry) {
        List<String> list = new ArrayList<>(EXCHANGES.get());
        String line = entry.serialize();
        if (list.contains(line)) {
            return false;
        }
        list.add(line);
        save(list);
        return true;
    }

// 删除一条
    public static boolean remove(ExchangeEntry entry) {
        List<String> list = new ArrayList<>(EXCHANGES.get());
        if (!list.remove(entry.serialize())) {
            return false;
        }
        save(list);
        return true;
    }

    private static void save(List<String> list) {
        EXCHANGES.set(list);
        SPEC.save();
    }

// 把兑换条目写进「打开界面」的登录包，供客户端界面读取
    public static void writeEntries(FriendlyByteBuf buf) {
        List<ExchangeEntry> list = entries();
        buf.writeVarInt(list.size());
        for (ExchangeEntry entry : list) {
            entry.write(buf);
        }
    }

    public static List<ExchangeEntry> readEntries(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ExchangeEntry> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(ExchangeEntry.read(buf));
        }
        return list;
    }
}
