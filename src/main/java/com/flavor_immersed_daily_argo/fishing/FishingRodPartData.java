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
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 钓竿配件参数。
//
// 由数据包 data/<命名空间>/fishing_rod_parts/*.json 驱动，每个文件描述一种可作配件的
// 物品及其系数，例如：
//
// {
// "item": "flavor_immersed_daily_argo:xxx",
// "durability": 384,   // 耐久（竿体 / 鱼线）
// "hardness": 1.0,     // 硬度系数（竿体）
// "tension": 20,       // 拉力（竿体 / 鱼线）
// "stretch": 1.0       // 延展系数（鱼线）
// }
//
// 六个改装槽位依次为：鱼线、竿体、线轮、鱼饵、浮漂、铅坠，槽位决定读取哪些系数。
// 未在数据包中定义的物品按全 0 处理，即不影响任何计算结果。
//
// 只注册在服务端（钓鱼玩法）：客户端资源管理器读不到 data 目录，登录 / 重载时由服务端把
// 已加载配件的原始 JSON 全量下发，客户端用同一套解析逻辑重建，界面 / tooltip 与玩法数据一致。
public class FishingRodPartData extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "fishing_rod_parts";
    private static final FishingRodPartData INSTANCE = new FishingRodPartData();

    private volatile Map<ResourceLocation, PartStats> parts = Map.of();
// 已加载配件的原始 JSON 文本，服务端全量同步给客户端用
    private volatile List<String> rawJson = List.of();

// 配件系数；未涉及的字段为 0，表示该配件不影响对应计算
    public record PartStats(double durability, double hardness, double tension, double stretch,
                            double thickness, double drag, double reelSpeed, double weight,
                            double antiCurrent, double size, double setDepth) {

// 未定义 / 空槽位：所有系数为 0
        public static final PartStats NONE = new PartStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        static PartStats fromJson(JsonObject json) {
            return new PartStats(
                    num(json, "durability"),
                    num(json, "hardness"),
                    num(json, "tension"),
                    num(json, "stretch"),
                    num(json, "thickness"),
                    num(json, "drag"),
                    num(json, "reel_speed"),
                    num(json, "weight"),
                    num(json, "anti_current"),
                    num(json, "size"),
                    num(json, "set_depth"));
        }

        private static double num(JsonObject json, String key) {
            return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsDouble() : 0.0D;
        }
    }

    private FishingRodPartData() {
        super(new Gson(), FOLDER);
    }

    public static FishingRodPartData getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, PartStats> loaded = new HashMap<>();
        List<String> raw = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                loaded.put(itemIdOf(json), PartStats.fromJson(json));
                raw.add(json.toString());
            } catch (Exception ex) {
                LOGGER.error("[FIDA配件] 解析配件失败 {}：{}", entry.getKey(), ex.getMessage());
            }
        }
        assign(loaded, raw);
        LOGGER.info("[FIDA配件] 已加载 {} 个配件定义", loaded.size());
    }

// 客户端收到服务端同步后重建配件表；解析规则与数据包加载完全一致
    public void applyFromJson(List<String> jsonTexts) {
        Map<ResourceLocation, PartStats> loaded = new HashMap<>();
        List<String> raw = new ArrayList<>();
        for (String text : jsonTexts) {
            try {
                JsonObject json = JsonParser.parseString(text).getAsJsonObject();
                loaded.put(itemIdOf(json), PartStats.fromJson(json));
                raw.add(text);
            } catch (Exception ex) {
                LOGGER.error("[FIDA配件] 解析同步配件失败：{}", ex.getMessage());
            }
        }
        assign(loaded, raw);
        LOGGER.info("[FIDA配件] 已同步 {} 个配件定义", loaded.size());
    }

// 读取配件对应的物品 id；字段缺失或非法时抛出异常，由加载器跳过该文件
    private static ResourceLocation itemIdOf(JsonObject json) {
        ResourceLocation itemId = ResourceLocation.tryParse(json.get("item").getAsString());
        if (itemId == null) {
            throw new IllegalArgumentException("item 字段非法");
        }
        return itemId;
    }

// 替换当前配件表；raw 与 loaded 一一对应，供同步回传给客户端
    private void assign(Map<ResourceLocation, PartStats> loaded, List<String> raw) {
        this.parts = Map.copyOf(loaded);
        this.rawJson = List.copyOf(raw);
    }

// 已加载配件的原始 JSON 文本（服务端全量同步给客户端用）
    public List<String> rawJson() {
        return this.rawJson;
    }

// 查询物品对应的配件系数；未定义或空槽位返回全 0
    public PartStats statsOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return PartStats.NONE;
        }
        return this.parts.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), PartStats.NONE);
    }

// 查询指定槽位的配件系数；槽位越界或为空返回全 0
    public PartStats statsOf(List<ItemStack> mods, int slot) {
        return slot >= 0 && slot < mods.size() ? statsOf(mods.get(slot)) : PartStats.NONE;
    }
}
