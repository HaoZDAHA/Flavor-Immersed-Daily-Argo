package com.flavor_immersed_daily_argo.economy.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// SDM Economy 货币 API 的反射桥。
//
// 用反射而不是编译期依赖，没装 SDM 时也能构建、能跑。
// 只调公开 API（net.sixik.sdm_economy.api.CurrencyHelper），货币 id 由上层决定。
final class SdmEconomyBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String HELPER_CLASS = "net.sixik.sdm_economy.api.CurrencyHelper";

// 内置基础货币
    static final String DEFAULT_CURRENCY_ID = "basic_money";

// 精确整数的上限 2^53
    static final long MAX_EXACT = 9_007_199_254_740_992L;

// 解析结果：null = 还没试过，false = 不可用
    private static Boolean available;
    private static Method getMoney;
    private static Method setMoney;
    private static Method getAllCurrencyKeys;

    private SdmEconomyBridge() {
    }

// 探测一次并缓存
    static synchronized boolean available() {
        if (available != null) {
            return available;
        }
        try {
            Class<?> helper = Class.forName(HELPER_CLASS);
            getMoney = helper.getMethod("getMoney", Player.class, String.class);
            setMoney = helper.getMethod("setMoney", Player.class, String.class, long.class);
            if (getMoney.getReturnType() != long.class || setMoney.getReturnType() != void.class) {
                LOGGER.warn("[FIDA Economy] SDM Economy currency API signature mismatch; using the built-in economy");
                available = Boolean.FALSE;
                return false;
            }
            try {
                getAllCurrencyKeys = helper.getMethod("getAllCurrencyKeys");
            } catch (NoSuchMethodException ignored) {
                getAllCurrencyKeys = null;
            }
            available = Boolean.TRUE;
            LOGGER.info("[FIDA Economy] Hooked into the SDM Economy currency API");
        } catch (Throwable t) {
            LOGGER.warn("[FIDA Economy] Cannot access the SDM Economy currency API; using the built-in economy: {}", t.toString());
            available = Boolean.FALSE;
        }
        return available;
    }

// 对方当前注册了哪些货币，读不到返回空表
    static List<String> registeredCurrencies() {
        List<String> names = new ArrayList<>();
        if (!available() || getAllCurrencyKeys == null) {
            return names;
        }
        try {
            Object result = getAllCurrencyKeys.invoke(null);
            if (result instanceof List<?> list) {
                for (Object value : list) {
                    if (value instanceof String text && !text.isBlank()) {
                        names.add(text);
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("[FIDA Economy] Failed to read the SDM currency list: {}", t.toString());
        }
        return names;
    }

// 读余额，出错返回 0
    static long get(Player player, String currencyId) {
        if (!available() || player == null || currencyId == null) {
            return 0L;
        }
        try {
            Object value = getMoney.invoke(null, player, currencyId);
            return value instanceof Long amount ? Math.max(0L, amount) : 0L;
        } catch (Throwable t) {
            LOGGER.warn("[FIDA Economy] Failed to read the shared balance: {}", t.toString());
            return 0L;
        }
    }

// 写余额并回读校验
    static boolean set(Player player, String currencyId, long amount) {
        if (!available() || player == null || currencyId == null) {
            return false;
        }
        long target = Math.max(0L, amount);
        try {
            setMoney.invoke(null, player, currencyId, target);
        } catch (Throwable t) {
            LOGGER.warn("[FIDA Economy] Failed to write the shared balance: {}", t.toString());
            return false;
        }
        long after = get(player, currencyId);
        if (after != target) {
            LOGGER.warn("[FIDA Economy] Balance write did not take effect: expected {}, got {}", target, after);
            return false;
        }
        return true;
    }
}
