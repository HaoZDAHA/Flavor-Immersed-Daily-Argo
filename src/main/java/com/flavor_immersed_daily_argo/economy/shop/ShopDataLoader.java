package com.flavor_immersed_daily_argo.economy.shop;

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

// 系统店铺数据。
//
// 从数据包的 data/<命名空间>/shop/*.json 加载，附属 mod 在自身命名空间下放 JSON 即可扩展内容。
// 只在服务端注册（走 AddReloadListenerEvent，客户端读不到 data 目录），加载结果由服务端
// 随登录包 / 同步包全量下发，保证多端一致。
//
// 条目支持两种写法：{"item": "命名空间:物品", "count": n, "price": p} 直接写物品；
// {"tag": "命名空间:标签", "count": n, "price": p} 会在加载期把标签内每个物品各展开成一条
// （stock = -1 无限）。_ 前缀文件跳过；坏条目 / 未知物品忽略。
//
// 标签展开的时机：物品标签是在资源重载「跑完监听器之后」才绑定的，所以监听器里读标签会得到空集。
// 因此这里只把 JSON 解析成「原始条目」（物品名 / 标签名 + 数量 + 价格 + 是否标签）存起来，
// 真正展开交给 #rebuild()；重载结束时先展开一次，物品标签绑定后的
// TagsUpdatedEvent 再调一次，把标签条目补全。
public class ShopDataLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "shop";

// 已加载的系统店铺（仅服务端资源重载后写入；客户端不注册该监听器，避免空数据覆盖客户端）
    public static volatile List<Shop> SHOPS = List.of();

// 数据包里读到的原始条目（标签未展开），资源重载时更新
    private static volatile List<RawShop> RAW = List.of();

    public ShopDataLoader() {
        super(new Gson(), FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<RawShop> parsed = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            ResourceLocation id = file.getKey();
// 「_」开头的文件按约定跳过（方便放注释 / 草稿）
            if (id.getPath().startsWith("_")) {
                continue;
            }
            try {
                JsonObject json = file.getValue().getAsJsonObject();
                String title = json.has("title") ? json.get("title").getAsString() : id.toString();
                List<RawEntry> entries = new ArrayList<>();
                JsonArray array = json.getAsJsonArray("entries");
                if (array != null) {
                    for (JsonElement element : array) {
                        RawEntry entry = parseEntry(element.getAsJsonObject());
                        if (entry != null) {
                            entries.add(entry);
                        }
                    }
                }
                parsed.add(new RawShop(id.toString(), title, List.copyOf(entries)));
            } catch (Exception ex) {
                LOGGER.error("[FIDA店铺] 解析店铺失败 {}：{}", id, ex.getMessage());
            }
        }
        RAW = List.copyOf(parsed);
        rebuild();
    }

// 把原始条目展开成店铺条目并发布到 #SHOPS。
//
// 资源重载时物品标签还没绑定，这一步只能展开写死的物品条目；标签绑定后的
// TagsUpdatedEvent 会再调一次，把标签条目补全。
    public static void rebuild() {
        List<Shop> loaded = new ArrayList<>();
        int entryCount = 0;
        for (RawShop raw : RAW) {
            List<ShopEntry> entries = new ArrayList<>();
            for (RawEntry entry : raw.entries()) {
                entries.addAll(entry.expand());
            }
            entryCount += entries.size();
            loaded.add(new Shop(raw.id(), raw.title(), "", List.copyOf(entries), 0L, false));
        }
        SHOPS = List.copyOf(loaded);
        LOGGER.info("[FIDA店铺] 已加载 {} 家系统店铺（共 {} 条条目）", loaded.size(), entryCount);
    }

// 解析一条原始条目；item 与 tag 二选一，都不写就返回 null
    private static RawEntry parseEntry(JsonObject obj) {
        int count = obj.has("count") ? Math.max(1, obj.get("count").getAsInt()) : 1;
        int price = obj.has("price") ? Math.max(0, obj.get("price").getAsInt()) : 0;
        if (obj.has("item")) {
            return new RawEntry(obj.get("item").getAsString(), false, count, price);
        }
        if (obj.has("tag")) {
            return new RawEntry(obj.get("tag").getAsString(), true, count, price);
        }
        return null;
    }

    private static Item itemOf(String key) {
        ResourceLocation rl = ResourceLocation.tryParse(key);
        Item item = rl == null ? Items.AIR : BuiltInRegistries.ITEM.get(rl);
        return item == Items.AIR ? null : item;
    }

// 一家系统店铺的原始定义（条目尚未展开）
    private record RawShop(String id, String title, List<RawEntry> entries) {
    }

// 一条原始条目：物品名 / 标签名 + 数量 + 价格 + 是否标签
    private record RawEntry(String key, boolean tag, int count, int price) {

// 物品条目直接一条；标签条目展开成标签内每个物品各一条（stock = -1 无限）
        List<ShopEntry> expand() {
            if (!this.tag) {
                Item item = itemOf(this.key);
                return item == null ? List.of() : List.of(new ShopEntry(item, this.count, this.price, -1));
            }
            ResourceLocation rl = ResourceLocation.tryParse(this.key);
            if (rl == null) {
                return List.of();
            }
            List<ShopEntry> out = new ArrayList<>();
            BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, rl))
                    .ifPresent(set -> set.forEach(holder ->
                            out.add(new ShopEntry(holder.value(), this.count, this.price, -1))));
            return out;
        }
    }
}
