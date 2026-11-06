package com.flavor_immersed_daily_argo.economy.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

// SDM Economy（1.20.1）货币 API 的反射桥，两代接口都认：
//
//   1.7.x  net.sixik.sdm_economy.api.CurrencyHelper   long 余额，默认 basic_money
//   2.x    net.sixik.sdmeconomy.api.EconomyAPI        double 余额，货币叫 sdm_coin
//
// 对上层暴露 double，旧版写入时取整。
final class SdmEconomyBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

// 两代 API 的类名
    private static final String LEGACY_HELPER = "net.sixik.sdm_economy.api.CurrencyHelper";
    private static final String MODERN_API = "net.sixik.sdmeconomy.api.EconomyAPI";
    private static final String MODERN_SERVER = "net.sixik.sdmeconomy.currencies.data.CurrencyPlayerData$Server";
    private static final String MODERN_STRUCT = "net.sixik.sdmeconomy.utils.ErrorCodeStruct";

// 两代默认货币名（都可被配置项覆盖）
    static final String LEGACY_DEFAULT_CURRENCY = "basic_money";
    static final String MODERN_DEFAULT_CURRENCY = "sdm_coin";

// double 能精确表示全部整数的上限 2^53
    static final long MAX_EXACT = 9_007_199_254_740_992L;

// 识别到的 API 代次
    enum Kind {
        NONE,
// 1.7.x：CurrencyHelper + long
        LEGACY,
// 2.x：EconomyAPI + double
        MODERN
    }

    private static Kind kind;

// 旧版
    private static Method legacyGetMoney;
    private static Method legacySetMoney;
    private static Method legacyAddMoney;
    private static Method legacyCurrencyKeys;

// 新版
    private static Method modernGetServerData;
    private static Method modernGetAllCurrency;
    private static Method modernGetBalance;
    private static Method modernAddValue;
    private static Method modernSetValue;
    private static Field structValue;
    private static Field structCodes;

    private SdmEconomyBridge() {
    }

// 探测一次并缓存
    static synchronized Kind kind() {
        if (kind != null) {
            return kind;
        }
        if (probeModern()) {
            kind = Kind.MODERN;
            LOGGER.info("[FIDA Economy] Hooked into the SDM Economy 2.x currency API (double balance, default {})",
                    MODERN_DEFAULT_CURRENCY);
            return kind;
        }
        if (probeLegacy()) {
            kind = Kind.LEGACY;
            LOGGER.info("[FIDA Economy] Hooked into the SDM Economy 1.x currency API (long balance, default {})",
                    LEGACY_DEFAULT_CURRENCY);
            return kind;
        }
        kind = Kind.NONE;
        LOGGER.warn("[FIDA Economy] No usable SDM Economy currency API found; using the built-in economy");
        return kind;
    }

    static boolean available() {
        return kind() != Kind.NONE;
    }

// 当前代次对应的默认货币 id
    static String defaultCurrencyId() {
        return kind() == Kind.LEGACY ? LEGACY_DEFAULT_CURRENCY : MODERN_DEFAULT_CURRENCY;
    }

    private static boolean probeModern() {
        try {
            Class<?> api = Class.forName(MODERN_API);
            Class<?> serverClass = Class.forName(MODERN_SERVER);
            Class<?> structClass = Class.forName(MODERN_STRUCT);

            modernGetServerData = api.getMethod("getPlayerCurrencyServerData");
            modernGetAllCurrency = api.getMethod("getAllCurrency");
            modernGetBalance = serverClass.getMethod("getBalance", UUID.class, String.class);
            modernAddValue = serverClass.getMethod("addCurrencyValue", UUID.class, String.class, double.class);
            modernSetValue = serverClass.getMethod("setCurrencyValue", UUID.class, String.class, double.class);
            structValue = structClass.getField("value");
            structCodes = structClass.getField("codes");

            if (modernGetBalance.getReturnType() != structClass
                    || modernAddValue.getReturnType() == void.class
                    || modernSetValue.getReturnType() == void.class) {
                LOGGER.warn("[FIDA Economy] SDM Economy 2.x API signature mismatch; trying the legacy API");
                return false;
            }
            return true;
        } catch (Throwable t) {
            LOGGER.debug("[FIDA Economy] No SDM Economy 2.x API: {}", t.toString());
            return false;
        }
    }

    private static boolean probeLegacy() {
        try {
            Class<?> helper = Class.forName(LEGACY_HELPER);
            legacyGetMoney = helper.getMethod("getMoney", net.minecraft.world.entity.player.Player.class, String.class);
            legacySetMoney = helper.getMethod("setMoney", net.minecraft.world.entity.player.Player.class,
                    String.class, long.class);
            legacyAddMoney = helper.getMethod("addMoney", net.minecraft.world.entity.player.Player.class,
                    String.class, long.class);
            if (legacyGetMoney.getReturnType() != long.class) {
                LOGGER.warn("[FIDA Economy] SDM Economy 1.x API signature mismatch");
                return false;
            }
            try {
                legacyCurrencyKeys = helper.getMethod("getAllCurrencyKeys");
            } catch (NoSuchMethodException ignored) {
                legacyCurrencyKeys = null;
            }
            return true;
        } catch (Throwable t) {
            LOGGER.debug("[FIDA Economy] No SDM Economy 1.x API: {}", t.toString());
            return false;
        }
    }

// 对方注册了哪些货币，读不到返回空表
    static List<String> registeredCurrencies() {
        List<String> names = new ArrayList<>();
        switch (kind()) {
            case LEGACY -> {
                if (legacyCurrencyKeys == null) {
                    return names;
                }
                try {
                    if (legacyCurrencyKeys.invoke(null) instanceof List<?> list) {
                        for (Object value : list) {
                            if (value instanceof String text && !text.isBlank()) {
                                names.add(text);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.debug("[FIDA Economy] Failed to read the SDM currency list: {}", t.toString());
                }
            }
            case MODERN -> {
                try {
                    Object struct = modernGetAllCurrency.invoke(null);
                    Object data = struct == null ? null : structValue.get(struct);
                    Object currencies = data == null ? null : data.getClass().getField("currencies").get(data);
                    if (currencies instanceof Collection<?> list) {
                        for (Object currency : list) {
                            if (currency == null) {
                                continue;
                            }
                            Object name = currency.getClass().getMethod("getName").invoke(currency);
                            if (name instanceof String text && !text.isBlank()) {
                                names.add(text);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.debug("[FIDA Economy] Failed to read the SDM currency list: {}", t.toString());
                }
            }
            default -> {
            }
        }
        return names;
    }

// 读余额，失败返回 null
    static Double balanceOrNull(ServerPlayer player, String currencyId) {
        if (player == null || currencyId == null) {
            return null;
        }
        switch (kind()) {
            case LEGACY -> {
                try {
                    Object value = legacyGetMoney.invoke(null, player, currencyId);
                    return value instanceof Long amount ? (double) Math.max(0L, amount) : null;
                } catch (Throwable t) {
                    LOGGER.warn("[FIDA Economy] Failed to read the shared balance: {}", t.toString());
                    return null;
                }
            }
            case MODERN -> {
                Object server = modernServerData();
                if (server == null) {
                    return null;
                }
                try {
                    Object struct = modernGetBalance.invoke(server, player.getUUID(), currencyId);
                    if (struct == null || !isSuccess(structCodes.get(struct))) {
                        return null;
                    }
                    Object value = structValue.get(struct);
                    return value instanceof Double amount ? amount : null;
                } catch (Throwable t) {
                    LOGGER.warn("[FIDA Economy] Failed to read the shared balance: {}", t.toString());
                    return null;
                }
            }
            default -> {
                return null;
            }
        }
    }

// 入账
    static boolean add(ServerPlayer player, String currencyId, double amount) {
        if (player == null || currencyId == null || !isSane(amount)) {
            return false;
        }
        switch (kind()) {
            case LEGACY -> {
                try {
// 旧版 addMoney 是 void，没抛异常就算成功
                    legacyAddMoney.invoke(null, player, currencyId, Math.round(amount));
                    return true;
                } catch (Throwable t) {
                    LOGGER.warn("[FIDA Economy] Failed to credit the shared account: {}", t.toString());
                    return false;
                }
            }
            case MODERN -> {
                Object server = modernServerData();
                if (server == null) {
                    return false;
                }
                try {
                    return isSuccess(modernAddValue.invoke(server, player.getUUID(), currencyId, amount));
                } catch (Throwable t) {
                    LOGGER.warn("[FIDA Economy] Failed to credit the shared account: {}", t.toString());
                    return false;
                }
            }
            default -> {
                return false;
            }
        }
    }

// 写入确定的余额，扣款用
    static boolean set(ServerPlayer player, String currencyId, double amount) {
        if (player == null || currencyId == null || !isSane(amount)) {
            return false;
        }
        switch (kind()) {
            case LEGACY -> {
                try {
                    legacySetMoney.invoke(null, player, currencyId, Math.round(Math.max(0.0D, amount)));
                    return true;
                } catch (Throwable t) {
                    LOGGER.warn("[FIDA Economy] Failed to write the shared balance: {}", t.toString());
                    return false;
                }
            }
            case MODERN -> {
                Object server = modernServerData();
                if (server == null) {
                    return false;
                }
                try {
                    return isSuccess(modernSetValue.invoke(server, player.getUUID(), currencyId,
                            Math.max(0.0D, amount)));
                } catch (Throwable t) {
                    LOGGER.warn("[FIDA Economy] Failed to write the shared balance: {}", t.toString());
                    return false;
                }
            }
            default -> {
                return false;
            }
        }
    }

    private static boolean isSane(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static Object modernServerData() {
        try {
            return modernGetServerData.invoke(null);
        } catch (Throwable t) {
            LOGGER.warn("[FIDA Economy] Failed to read SDM currency server data: {}", t.toString());
            return null;
        }
    }

// ErrorCodes 是枚举，只有 SUCCESS 算成功
    private static boolean isSuccess(Object code) {
        if (code == null) {
            return false;
        }
        return code instanceof Enum<?> value ? "SUCCESS".equals(value.name()) : "SUCCESS".equals(code.toString());
    }
}
