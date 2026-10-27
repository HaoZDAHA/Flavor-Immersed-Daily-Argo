package com.flavor_immersed_daily_argo.client.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 把「稻草人头顶的名字」换成「那个正版玩家的皮肤」。
//
// 分两步查：名字 → UUID 走 Mojang 的公开接口（api.mojang.com/users/profiles/minecraft/<name>），
// UUID → 皮肤交给原版——先用 MinecraftSessionService#fetchProfile 拿到带 textures 属性的档案
// （authlib 内部缓存 6 小时），再交给 SkinManager 解包、下载并把贴图注册进贴图管理器，
// 于是皮肤和披风的缓存、限流全都复用原版那一套。
//
// 整个流程都在后台线程跑，渲染线程只读缓存：查到之前照常画普通稻草人，查到之后下一帧自动换成皮肤。
// 名字不是正版玩家（或没网）就永久记进 #UNKNOWN，不再反复重试。
public final class ScarecrowSkins {

    private static final Logger LOGGER = LogUtils.getLogger();

// 正版 ID 最长 16 个字符，超过这个长度就不可能是玩家名
    private static final int MAX_NAME_LENGTH = 16;

    private static final String NAME_TO_UUID = "https://api.mojang.com/users/profiles/minecraft/";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

// 名字（小写）→ 已经拿到的皮肤贴图
    private static final Map<String, ResourceLocation> SKINS = new ConcurrentHashMap<>();
// 名字（小写）→ 已经确认查不到，不再重试
    private static final Set<String> UNKNOWN = ConcurrentHashMap.newKeySet();
// 名字（小写）→ 正在查，避免同一帧里重复发请求
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();

    private ScarecrowSkins() {
    }

// 取这个名字对应的皮肤，取不到就先返回 null（并在后台开始查）。
//
// customName：稻草人的自定义名字，没名字时传 null
    @Nullable
    public static ResourceLocation get(@Nullable Component customName) {
        if (customName == null) {
            return null;
        }
        String name = customName.getString().trim();
        if (!isPlayerName(name)) {
            return null;
        }
        String key = name.toLowerCase(Locale.ROOT);
        ResourceLocation cached = SKINS.get(key);
        if (cached != null) {
            return cached;
        }
        if (!UNKNOWN.contains(key) && LOADING.add(key)) {
// 查名字要发 HTTP、还会阻塞，扔到原版那条可无限扩线程的 I/O 池上，别占住后台计算池
            Util.ioPool().execute(() -> load(key, name));
        }
        return null;
    }

// 正版 ID 只可能是 3~16 位的字母、数字和下划线。
//
// 先把明显不是玩家名的名字（比如玩家自己取的「老王」）挡掉，免得白跑一次网络查询。
    private static boolean isPlayerName(String name) {
        if (name.length() < 3 || name.length() > MAX_NAME_LENGTH) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean allowed = c == '_' || c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
            if (!allowed) {
                return false;
            }
        }
        return true;
    }

    private static void load(String key, String name) {
        boolean handedOff = false;
        try {
            UUID uuid = lookupUuid(name);
            if (uuid != null) {
                GameProfile profile = Minecraft.getInstance().getMinecraftSessionService()
                        .fillProfileProperties(new GameProfile(uuid, name), false);
                if (profile != null) {
// 1.20.1 的皮肤直接就是一张贴图路径，由 SkinManager 解包 textures 属性后给出
                    ResourceLocation skin = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(profile);
                    finish(key, skin);
                    handedOff = true;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to look up the skin for scarecrow name {}", name, e);
        }
        if (!handedOff) {
            UNKNOWN.add(key);
            LOADING.remove(key);
        }
    }

    private static void finish(String key, @Nullable ResourceLocation skin) {
        if (skin != null) {
            SKINS.put(key, skin);
        } else {
            UNKNOWN.add(key);
        }
        LOADING.remove(key);
    }

// 名字 → UUID。接口对不存在的名字返回 204/404，这里统一当成「查不到」。
    @Nullable
    private static UUID lookupUuid(String name) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(NAME_TO_UUID + name))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!json.has("id")) {
            return null;
        }
        String id = json.get("id").getAsString();
        if (id.length() != 32) {
            return null;
        }
        return new UUID(Long.parseUnsignedLong(id.substring(0, 16), 16),
                Long.parseUnsignedLong(id.substring(16), 16));
    }
}
