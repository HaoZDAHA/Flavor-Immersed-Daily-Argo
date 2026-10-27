package com.flavor_immersed_daily_argo.fishing;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 钓鱼手册条目数据。
//
// 从数据包的 data/<命名空间>/fishing_handbook/*.json 加载，附属 mod 在自身命名空间下放 JSON 即可扩展内容。
// 在服务端 AddReloadListenerEvent 中注册：条目的生活习性需要参与服务端玩法计算；界面虽是客户端展示，
// 客户端读不到 data 目录，因此登录 / 重载时由服务端把原始 JSON 全量下发，客户端用同一套解析逻辑重建。
public class FishingHandbookData extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "fishing_handbook";
    private static final FishingHandbookData INSTANCE = new FishingHandbookData();

    private List<FishingHandbookEntry> entries = List.of();
// 已加载条目的原始 JSON 文本，服务端全量同步给客户端用
    private List<String> rawJson = List.of();

    private FishingHandbookData() {
        super(new Gson(), FOLDER);
    }

    public static FishingHandbookData getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<FishingHandbookEntry> loaded = new ArrayList<>();
        List<String> raw = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                loaded.add(FishingHandbookEntry.fromJson(json));
                raw.add(json.toString());
            } catch (Exception ex) {
                LOGGER.error("[FIDA手册] 解析条目失败 {}：{}", entry.getKey(), ex.getMessage());
            }
        }
        assign(loaded, raw);
        LOGGER.info("[FIDA手册] 已加载 {} 个钓获条目", loaded.size());
    }

// 客户端收到服务端同步后重建条目；解析规则与数据包加载完全一致
    public void applyFromJson(List<String> jsonTexts) {
        List<FishingHandbookEntry> loaded = new ArrayList<>();
        List<String> raw = new ArrayList<>();
        for (String text : jsonTexts) {
            try {
                JsonObject json = JsonParser.parseString(text).getAsJsonObject();
                loaded.add(FishingHandbookEntry.fromJson(json));
                raw.add(text);
            } catch (Exception ex) {
                LOGGER.error("[FIDA手册] 解析同步条目失败：{}", ex.getMessage());
            }
        }
        assign(loaded, raw);
        LOGGER.info("[FIDA手册] 已同步 {} 个钓获条目", loaded.size());
    }

// 排序后替换当前条目；raw 与 loaded 一一对应，供同步回传给客户端
    private void assign(List<FishingHandbookEntry> loaded, List<String> raw) {
// 丢弃战利品在注册表里不存在的条目（对应模组未安装），避免手册出现钓不上来的死条目
        List<FishingHandbookEntry> usable = new ArrayList<>(loaded.size());
        List<String> usableRaw = new ArrayList<>(raw.size());
        for (int i = 0; i < loaded.size(); i++) {
            if (loaded.get(i).available()) {
                usable.add(loaded.get(i));
                usableRaw.add(raw.get(i));
            }
        }
// order > 0 的条目置顶（按权重升序），其余按显示名排序
        usable.sort(Comparator
                .comparingInt((FishingHandbookEntry e) -> e.order > 0 ? e.order : Integer.MAX_VALUE)
                .thenComparing(FishingHandbookEntry::displayName));
        this.entries = List.copyOf(usable);
        this.rawJson = List.copyOf(usableRaw);
    }

// 已加载条目的原始 JSON 文本（服务端全量同步给客户端用）
    public List<String> rawJson() {
        return this.rawJson;
    }

// 全部条目（按显示名排序）
    public List<FishingHandbookEntry> all() {
        return this.entries;
    }

// 按图鉴记账键（战利品物品 / 实体 id）查找条目；未收录返回 null
    public FishingHandbookEntry find(ResourceLocation key) {
        if (key == null) {
            return null;
        }
        for (FishingHandbookEntry entry : this.entries) {
            if (key.equals(entry.caughtKey())) {
                return entry;
            }
        }
        return null;
    }

// 应用筛选后取条目；筛选集合为空表示该维度不限
    public List<FishingHandbookEntry> filter(
            Set<FishingHandbookEntry.Habitat> habitats,
            Set<FishingHandbookEntry.Diet> diets,
            Set<FishingHandbookEntry.Activity> activities,
            Set<FishingHandbookEntry.Alertness> alertnesses,
            Set<FishingHandbookEntry.FeedingSpeed> speeds) {
        return this.entries.stream()
                .filter(e -> e.matches(habitats, diets, activities, alertnesses, speeds))
                .toList();
    }
}
