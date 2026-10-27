package com.flavor_immersed_daily_argo.economy.product;

import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 经济产物数据包。
//
// 从 data/<命名空间>/economic_product/*.json 加载，格式：
//
// {
// "entries": [
// { "item": "minecraft:apple", "price": 8 },
// { "tag": "fid:fruit", "price": 8 }
// ]
// }
//
// item 与 tag 二选一，price 是基准收购价（元 / 每组 64 个），不写就用配置里的默认价。
// tag 会在加载期展开成逐物品条目。_ 前缀文件跳过；坏条目 / 未知物品忽略。
//
// 标签展开的时机：物品标签是在资源重载「跑完监听器之后」才绑定的，所以监听器里读标签会得到空集。
// 因此这里只把 JSON 解析成「原始条目」（物品名 / 标签名 + 价格 + 是否标签）存起来，真正展开交给
// #rebuild()；重载结束时先展开一次，物品标签绑定后的 TagsUpdatedEvent 再调一次，
// 把标签条目补全。
//
// 与店铺一样只在服务端注册（客户端读不到 data 目录），结果由服务端算成「今日涨跌」后随界面下发。
public class EconomicProductLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "economic_product";

// 数据包里读到的原始条目（标签未展开），资源重载时更新
    private static volatile List<RawEntry> RAW = List.of();

    public EconomicProductLoader() {
        super(new Gson(), FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<RawEntry> parsed = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            ResourceLocation id = file.getKey();
            if (id.getPath().startsWith("_")) {
                continue;
            }
            try {
                JsonArray array = file.getValue().getAsJsonObject().getAsJsonArray("entries");
                if (array == null) {
                    continue;
                }
                for (JsonElement element : array) {
                    RawEntry entry = parseEntry(element.getAsJsonObject());
                    if (entry != null) {
                        parsed.add(entry);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("[FIDA经济产物] 解析失败 {}：{}", id, ex.getMessage());
            }
        }
        RAW = List.copyOf(parsed);
        rebuild();
    }

// 把原始条目展开成经济产物并发布到 EconomicProductData。
//
// 资源重载时物品标签还没绑定，这一步只能展开写死的物品条目；标签绑定后的
// TagsUpdatedEvent 会再调一次，把标签条目补全。
    public static void rebuild() {
        List<EconomicProduct> loaded = new ArrayList<>();
        for (RawEntry entry : RAW) {
            loaded.addAll(entry.expand());
        }
        EconomicProductData.setLoaded(loaded);
        LOGGER.info("[FIDA经济产物] 已加载 {} 种经济产物", loaded.size());
    }

// 解析一条原始条目；item 与 tag 二选一，都不写就返回 null
    private static RawEntry parseEntry(JsonObject obj) {
        int price = obj.has("price")
                ? Math.max(1, obj.get("price").getAsInt())
                : EconomyConfig.PRODUCT_DEFAULT_PRICE.get();
        if (obj.has("item")) {
            return new RawEntry(obj.get("item").getAsString(), false, price);
        }
        if (obj.has("tag")) {
            return new RawEntry(obj.get("tag").getAsString(), true, price);
        }
        return null;
    }

// 一条原始条目：物品名 / 标签名 + 基准收购价 + 是否标签
    private record RawEntry(String key, boolean tag, int price) {

// 物品条目直接一条；标签条目展开成标签内每个物品各一条
        List<EconomicProduct> expand() {
            if (!this.tag) {
                ResourceLocation rl = ResourceLocation.tryParse(this.key);
                Item item = rl == null ? Items.AIR : BuiltInRegistries.ITEM.get(rl);
                return item == Items.AIR ? List.of() : List.of(new EconomicProduct(item, this.price));
            }
            ResourceLocation rl = ResourceLocation.tryParse(this.key);
            if (rl == null) {
                return List.of();
            }
            List<EconomicProduct> out = new ArrayList<>();
            BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, rl))
                    .ifPresent(set -> set.forEach(holder ->
                            out.add(new EconomicProduct(holder.value(), this.price))));
            return out;
        }
    }
}
