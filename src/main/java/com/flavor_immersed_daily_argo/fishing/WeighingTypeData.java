package com.flavor_immersed_daily_argo.fishing;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 杆秤称重数据。
//
// 由数据包 data/<命名空间>/weighing_types/*.json 驱动，附属 mod 在自身命名空间下放 JSON
// 即可扩展可称重的物品。一个文件是一个称重分类，例如：
//
// {
// "name": "淡水鱼类",
// "weights": {
// "flavor_immersed_daily_argo:raw_grass_carp": { "mean": 4.0, "sigma": 1.2, "min": 0.8, "max": 12.0 }
// }
// }
//
// 重量按正态分布采样（截断到 [min, max]），单位「斤」。这里只保存分布参数——
// 真正抽到的重量写在物品的重量数据组件上，所以同一件东西反复上秤不会变，没上过秤的则没有重量、
// 照常堆叠。
//
// 与手册 / 配件参数一样只在服务端加载：客户端读不到 data 目录，登录 / 重载时由服务端把原始
// JSON 全量下发，客户端用同一套解析逻辑重建（界面里显示重量用得上）。
public class WeighingTypeData extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "weighing_types";
    private static final WeighingTypeData INSTANCE = new WeighingTypeData();

// 单件物品的重量分布（单位：斤）
    public record Distribution(double mean, double sigma, double min, double max) {

        static Distribution fromJson(JsonObject json) {
            double mean = num(json, "mean", 1.0D);
            double sigma = num(json, "sigma", mean * 0.25D);
            double min = num(json, "min", Math.max(0.1D, mean - sigma * 3.0D));
            double max = num(json, "max", mean + sigma * 3.0D);
            return new Distribution(mean, Math.max(0.0D, sigma), min, Math.max(min, max));
        }

// 抽一个重量，结果截断在 [min, max] 内并保留两位小数
        public double sample(RandomSource random) {
            double value = this.mean + random.nextGaussian() * this.sigma;
            value = Mth.clamp(value, this.min, this.max);
            return Math.round(value * 100.0D) / 100.0D;
        }

        private static double num(JsonObject json, String key, double fallback) {
            return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsDouble() : fallback;
        }
    }

// 一个称重分类：显示名 + 分类下每件物品的重量分布
    public record Category(String name, Map<ResourceLocation, Distribution> weights) {
    }

    private volatile List<Category> categories = List.of();
// 物品 id → 重量分布，查找用
    private volatile Map<ResourceLocation, Distribution> byItem = Map.of();
// 已加载分类的原始 JSON 文本，服务端全量同步给客户端用
    private volatile List<String> rawJson = List.of();

    private WeighingTypeData() {
        super(new Gson(), FOLDER);
    }

    public static WeighingTypeData getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<Category> loaded = new ArrayList<>();
        List<String> raw = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                loaded.add(parse(json));
                raw.add(json.toString());
            } catch (Exception ex) {
                LOGGER.error("[FIDA杆秤] 解析称重分类失败 {}：{}", entry.getKey(), ex.getMessage());
            }
        }
        assign(loaded, raw);
        LOGGER.info("[FIDA杆秤] 已加载 {} 个称重分类", loaded.size());
    }

// 客户端收到服务端同步后重建分类；解析规则与数据包加载完全一致
    public void applyFromJson(List<String> jsonTexts) {
        List<Category> loaded = new ArrayList<>();
        List<String> raw = new ArrayList<>();
        for (String text : jsonTexts) {
            try {
                JsonObject json = JsonParser.parseString(text).getAsJsonObject();
                loaded.add(parse(json));
                raw.add(text);
            } catch (Exception ex) {
                LOGGER.error("[FIDA杆秤] 解析同步称重分类失败：{}", ex.getMessage());
            }
        }
        assign(loaded, raw);
        LOGGER.info("[FIDA杆秤] 已同步 {} 个称重分类", loaded.size());
    }

    private static Category parse(JsonObject json) {
        String name = json.has("name") ? json.get("name").getAsString() : "";
        Map<ResourceLocation, Distribution> weights = new HashMap<>();
        if (json.has("weights") && json.get("weights").isJsonObject()) {
            JsonObject weightsJson = json.getAsJsonObject("weights");
            for (Map.Entry<String, JsonElement> item : weightsJson.entrySet()) {
                ResourceLocation id = ResourceLocation.tryParse(item.getKey());
                if (id != null && item.getValue().isJsonObject()) {
                    weights.put(id, Distribution.fromJson(item.getValue().getAsJsonObject()));
                }
            }
        }
        return new Category(name, Map.copyOf(weights));
    }

    private void assign(List<Category> loaded, List<String> raw) {
        Map<ResourceLocation, Distribution> byItem = new HashMap<>();
        for (Category category : loaded) {
            for (Map.Entry<ResourceLocation, Distribution> entry : category.weights().entrySet()) {
// 丢弃注册表里不存在的物品（对应模组未安装），免得白占一份数据
                if (BuiltInRegistries.ITEM.containsKey(entry.getKey())) {
                    byItem.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }
        this.categories = List.copyOf(loaded);
        this.byItem = Map.copyOf(byItem);
        this.rawJson = List.copyOf(raw);
    }

// 已加载分类的原始 JSON 文本（服务端全量同步给客户端用）
    public List<String> rawJson() {
        return this.rawJson;
    }

// 全部称重分类
    public List<Category> categories() {
        return this.categories;
    }

// 这件东西的重量分布；不在任何分类里返回 null（即「称不了」）
    @Nullable
    public Distribution forItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return this.byItem.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
