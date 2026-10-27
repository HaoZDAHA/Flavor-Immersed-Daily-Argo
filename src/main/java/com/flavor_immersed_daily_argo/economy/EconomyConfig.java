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
