package com.flavor_immersed_daily_argo.economy.compat;

import com.flavor_immersed_daily_argo.economy.CreditCardItem;
import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import com.flavor_immersed_daily_argo.economy.shop.ShopManager;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 经济联动的统一入口（1.20.1 Forge 版）。
//
// SDM 的余额是 double，本模组的钱是整数，double 到这一层为止：
// 读向下取整、付款写成「余额 - 金额」、收入直接累加。
// NaN / Infinity / 负数 / 超 2^53 一律当失败。
public final class EconomyCompat {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] SHOP_MOD_IDS = {"sdmshop", "sdmshoprework"};
    private static final String[] ECONOMY_MOD_IDS = {"sdmeconomy", "sdm_economy"};

// 回读校验的容差
    private static final double EPSILON = 1.0E-6D;

    private static volatile Boolean shared;

// 实际使用的货币 id
    private static volatile String currencyId = SdmEconomyBridge.defaultCurrencyId();

// 客户端侧的同步状态：0 = 还没收到服务端包，1 = 开，2 = 关
    private static volatile int clientState;
    private static volatile double clientBalance;

    private EconomyCompat() {
    }

// 联动是否生效
    public static boolean isShared() {
        int state = clientState;
        if (state != 0) {
            return state == 1;
        }
        return detect();
    }

    private static boolean detect() {
        Boolean cached = shared;
        if (cached != null) {
            return cached;
        }
        synchronized (EconomyCompat.class) {
            if (shared != null) {
                return shared;
            }
            boolean result = false;
            if (!anyLoaded(SHOP_MOD_IDS)) {
// 没装 SDM Shop 就什么都不做
                result = false;
            } else if (!anyLoaded(ECONOMY_MOD_IDS)) {
                LOGGER.warn("[FIDA Economy] SDM Shop is loaded but SDM Economy is not; using the built-in economy");
            } else if (!SdmEconomyBridge.available()) {
                result = false;
            } else {
                result = resolveCurrency();
            }
            shared = result;
            LOGGER.info("[FIDA Economy] Shared account {}", result
                    ? "enabled (SDM Economy: " + currencyId + ")"
                    : "disabled, using the built-in economy");
            return result;
        }
    }

// 选货币：配置项 > 默认主货币 > 唯一候选
    private static boolean resolveCurrency() {
        List<String> registered = SdmEconomyBridge.registeredCurrencies();
        String configured = "";
        try {
            configured = EconomyConfig.COMPAT_CURRENCY_ID.get();
        } catch (Throwable t) {
            LOGGER.debug("[FIDA Economy] Cannot read compat.currency_id: {}", t.toString());
        }
        configured = configured == null ? "" : configured.trim();

        if (!configured.isEmpty()) {
            for (String id : registered) {
                if (id.equalsIgnoreCase(configured)) {
                    currencyId = id;
                    return true;
                }
            }
            LOGGER.warn("[FIDA Economy] Configured currency {} is not registered in SDM Economy; using the built-in economy. Available: {}",
                    configured, registered);
            return false;
        }
        for (String id : registered) {
            if (id.equalsIgnoreCase(SdmEconomyBridge.defaultCurrencyId())) {
                currencyId = id;
                return true;
            }
        }
        if (registered.size() == 1) {
            currencyId = registered.get(0);
            LOGGER.warn("[FIDA Economy] Default currency {} not found; only one currency is registered, using {}",
                    SdmEconomyBridge.defaultCurrencyId(), currencyId);
            return true;
        }
        LOGGER.warn("[FIDA Economy] Default currency {} not found; registered: {}. Set compat.currency_id to choose one",
                SdmEconomyBridge.defaultCurrencyId(), registered);
        return false;
    }

    private static boolean anyLoaded(String[] ids) {
        for (String id : ids) {
            if (ModList.get().isLoaded(id)) {
                return true;
            }
        }
        return false;
    }

// 客户端收到服务端下发的状态
    public static void acceptClientSync(boolean active, double balance) {
        clientState = active ? 1 : 2;
        if (active && isSane(balance)) {
            clientBalance = balance;
        }
    }

// 客户端缓存的余额
    public static long clientBalance() {
        return spendable(clientBalance);
    }

// 两个 long 相加，到顶就停在 Long.MAX_VALUE
    public static long saturatingAdd(long a, long b) {
        return SharedEconomyData.safeAdd(a, b);
    }

// 把当前状态与余额推给这位玩家
    public static void syncBalance(ServerPlayer player) {
        if (player == null) {
            return;
        }
        boolean active = isShared();
        double balance = active ? rawBalance(player) : 0.0D;
        PacketDistributor.sendToPlayer(player, new SharedBalancePacket(active, balance));
    }

// 共享账户余额的整数部分（向下取整）。联动关闭 / 读取失败时返回 0。
    public static long balance(ServerPlayer player) {
        if (!isShared() || player == null) {
            return 0L;
        }
        return spendable(rawBalance(player));
    }

// 精确余额，失败返回 NaN
    public static double rawBalance(ServerPlayer player) {
        if (!isShared() || player == null) {
            return Double.NaN;
        }
        Double value = SdmEconomyBridge.balanceOrNull(player, currencyId);
        if (value == null) {
            return Double.NaN;
        }
        return value;
    }

// 够不够付
    public static boolean canAfford(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (!isShared() || player == null) {
            return false;
        }
        double raw = rawBalance(player);
        return isSane(raw) && raw >= (double) amount;
    }

// 从共享账户扣钱
    public static boolean pay(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (!isShared() || player == null) {
            return false;
        }
        if (amount > SdmEconomyBridge.MAX_EXACT) {
            LOGGER.warn("[FIDA Economy] Payment {} is beyond the exact integer range of double; rejected", amount);
            return false;
        }
        double before = rawBalance(player);
        if (!isSane(before) || before < (double) amount) {
            return false;
        }
        double target = before - (double) amount;
        if (target < 0.0D) {
// 不允许负余额
            target = 0.0D;
        }
        if (!SdmEconomyBridge.set(player, currencyId, target)) {
            LOGGER.warn("[FIDA Economy] Payment failed for {}: balance {}, required {}",
                    player.getName().getString(), before, amount);
            return false;
        }
        double after = rawBalance(player);
        if (!isSane(after) || Math.abs(after - target) > tolerance(target)) {
            LOGGER.warn("[FIDA Economy] Payment mismatch: expected {}, got {}; rolling back", target, after);
            SdmEconomyBridge.set(player, currencyId, before);
            return false;
        }
        syncBalance(player);
        return true;
    }

// 入账，返回真正到账的金额
    public static long credit(ServerPlayer player, long amount) {
        if (amount <= 0L || !isShared() || player == null) {
            return 0L;
        }
        if (amount > SdmEconomyBridge.MAX_EXACT) {
            LOGGER.warn("[FIDA Economy] Credit {} is beyond the exact integer range of double; rejected", amount);
            return 0L;
        }
        return creditTo(player, amount);
    }

// 按 UUID 入账；online 非空时顺便把新余额推给它
    private static long creditTo(ServerPlayer online, long amount) {
        double before = rawBalance(online);
        if (!isSane(before)) {
            LOGGER.warn("[FIDA Economy] Cannot read the balance before crediting; credit cancelled");
            return 0L;
        }
        if (before > (double) SdmEconomyBridge.MAX_EXACT - (double) amount) {
// 再加就超出 double 的精确整数范围
            LOGGER.warn("[FIDA Economy] Balance too large ({}); refusing to credit {}", before, amount);
            return 0L;
        }
        if (!SdmEconomyBridge.add(online, currencyId, (double) amount)) {
            LOGGER.warn("[FIDA Economy] Credit failed: expected {}", amount);
            return 0L;
        }
        double after = rawBalance(online);
        if (isSane(after) && after + EPSILON < before + (double) amount) {
            LOGGER.warn("[FIDA Economy] Credit mismatch: {} -> {} (expected +{}); rolling back", before, after, amount);
            SdmEconomyBridge.set(online, currencyId, before);
            return 0L;
        }
        if (online != null) {
            syncBalance(online);
        }
        return amount;
    }

// 以银行卡为凭据的余额
    public static long cardBalance(ServerPlayer player, ItemStack card) {
        if (isShared()) {
            migrateCard(player, card);
            return balance(player);
        }
        return CreditCardItem.getAccount(card);
    }

// 以银行卡为凭据的扣款；联动开启时卡面账户不参与，直接扣共享账户
    public static boolean cardPay(ServerPlayer player, ItemStack card, long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (isShared()) {
            migrateCard(player, card);
            return pay(player, amount);
        }
        long account = CreditCardItem.getAccount(card);
        if (account < amount) {
            return false;
        }
        CreditCardItem.setAccount(card, account - amount);
        return true;
    }

// 以银行卡为凭据的入账；联动开启时进共享账户，卡面账户保持 0
    public static long cardCredit(ServerPlayer player, ItemStack card, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        if (isShared()) {
            migrateCard(player, card);
            return credit(player, amount);
        }
        long before = CreditCardItem.getAccount(card);
        long after = SharedEconomyData.safeAdd(before, amount);
        CreditCardItem.setAccount(card, after);
        return after - before;
    }

// 店铺货款：店主在线直接入账，不在线挂待结算
    public static long creditOwner(MinecraftServer server, String ownerName, long amount) {
        if (!isShared() || amount <= 0L || ownerName == null || ownerName.isBlank()) {
            return 0L;
        }
        ServerPlayer online = server.getPlayerList().getPlayerByName(ownerName);
        if (online != null) {
            long credited = credit(online, amount);
            if (credited == amount) {
                return credited;
            }
        }
        SharedEconomyData data = SharedEconomyData.get(server);
        data.addPending(ownerKey(server, ownerName), amount);
        data.setDirty();
        return 0L;
    }

// 店主名字 → 待结算 key，依次查映射表、在线玩家、profile 缓存
    private static String ownerKey(MinecraftServer server, String ownerName) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(ownerName);
        if (online != null) {
            SharedEconomyData.get(server).rememberOwner(online.getName().getString(), online.getUUID());
            return online.getUUID().toString();
        }
        UUID id = resolveOwnerUuid(server, SharedEconomyData.get(server), ownerName);
        if (id != null) {
            return id.toString();
        }
        return SharedEconomyData.nameKey(ownerName);
    }

// 名字 → UUID，查到就记进映射表
    private static UUID resolveOwnerUuid(MinecraftServer server, SharedEconomyData data, String ownerName) {
        UUID known = data.ownerUuid(ownerName);
        if (known != null) {
            return known;
        }
        try {
            Optional<GameProfile> profile = server.getProfileCache() == null
                    ? Optional.empty() : server.getProfileCache().get(ownerName);
            if (profile.isPresent() && profile.get().getId() != null) {
                data.rememberOwner(ownerName, profile.get().getId());
                return profile.get().getId();
            }
        } catch (Throwable t) {
            LOGGER.debug("[FIDA Economy] Failed to resolve player UUID: {}", t.toString());
        }
        return null;
    }

// 玩家上线：迁移旧余额、结算待结算、同步余额
    public static void onPlayerJoin(ServerPlayer player) {
        syncBalance(player);
        if (!isShared() || player == null) {
            return;
        }
        MinecraftServer server = player.server;
        SharedEconomyData data = SharedEconomyData.get(server);
        String uuidKey = player.getUUID().toString();

// 记下名字 → UUID
        data.rememberOwner(player.getName().getString(), player.getUUID());

// 上次没确认的入账，交人工核对
        long unconfirmed = data.takeInflight(uuidKey);
        if (unconfirmed > 0L) {
            data.addReview(player.getName().getString(), uuidKey, unconfirmed, "unconfirmed_settlement");
        }

// 旧店铺余额折进待结算
        ShopManager.get(server).migrateOwnerBalances(player, data);

// 背包与末影箱里的银行卡逐个迁移
        migrateContainer(player);

// 待结算收入转入共享账户
        settlePending(server, player, data);

        data.setDirty();
        SharedEconomyData.flush(server);
        syncBalance(player);
    }

// 待结算收入转入共享账户
    private static void settlePending(MinecraftServer server, ServerPlayer player, SharedEconomyData data) {
        String uuidKey = player.getUUID().toString();
        long total = data.takePendingFor(player.getUUID(), player.getName().getString());
        if (total <= 0L) {
            return;
        }
        data.setInflight(uuidKey, total);
        data.setDirty();
        SharedEconomyData.flush(server);

        long credited = credit(player, total);

        data.setInflight(uuidKey, 0L);
        if (credited < total) {
            data.addPending(uuidKey, total - credited);
            LOGGER.warn("[FIDA Economy] Partial payout to {}: expected {}, credited {}; the rest stays pending",
                    player.getName().getString(), total, credited);
        }
        data.setDirty();
        SharedEconomyData.flush(server);
    }

// 背包 + 末影箱里所有银行卡都过一遍迁移
    private static void migrateContainer(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            migrateCard(player, player.getInventory().getItem(i));
        }
        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            migrateCard(player, player.getEnderChestInventory().getItem(i));
        }
    }

// 单张卡的旧余额迁移，幂等。钱归卡主人，不归触发这次检查的人
    public static void migrateCard(ServerPlayer opener, ItemStack card) {
        if (!isShared() || opener == null || card == null || card.isEmpty()) {
            return;
        }
        if (!(card.getItem() instanceof CreditCardItem)) {
            return;
        }
        long legacy = CreditCardItem.getAccount(card);
        if (legacy <= 0L) {
            CreditCardItem.ensureUid(card);
            return;
        }
        MinecraftServer server = opener.server;
        SharedEconomyData data = SharedEconomyData.get(server);
        String key = cardKey(card);
        if (data.isMigrated(key)) {
// 迁移过的卡，记一条人工核对
            data.addReview(opener.getName().getString(), key, legacy, "already_migrated");
            if (CreditCardItem.ensureUid(card)) {
                data.markMigrated(cardKey(card));
            }
            data.setDirty();
            return;
        }

// 没有主人的卡算在触发者名下
        String ownerName = CreditCardItem.getOwner(card);
        UUID ownerId;
        if (ownerName.isEmpty()) {
            ownerId = opener.getUUID();
            ownerName = opener.getName().getString();
        } else {
            ServerPlayer online = server.getPlayerList().getPlayerByName(ownerName);
            if (online != null) {
                ownerId = online.getUUID();
                data.rememberOwner(online.getName().getString(), ownerId);
            } else {
                ownerId = resolveOwnerUuid(server, data, ownerName);
            }
        }
        if (ownerId == null) {
// 认不回主人，记人工核对
            data.addReview(ownerName, key, legacy, "owner_unknown");
            return;
        }

        ServerPlayer onlineOwner = server.getPlayerList().getPlayer(ownerId);
        if (onlineOwner != null) {
            long credited = credit(onlineOwner, legacy);
            if (credited <= 0L) {
// 入账失败就留着，下次再试
                LOGGER.warn("[FIDA Economy] Card balance migration failed for {}; will retry later", ownerName);
                return;
            }
        } else {
// 主人不在线，先落盘到待结算
            data.addPending(ownerId.toString(), legacy);
            data.setDirty();
            SharedEconomyData.flush(server);
        }

        CreditCardItem.setAccount(card, 0L);
        data.markMigrated(key);
        CreditCardItem.ensureUid(card);
// 新 UID 键也标记已迁移
        String uidKey = cardKey(card);
        if (!uidKey.equals(key)) {
            data.markMigrated(uidKey);
        }
        data.setDirty();
        LOGGER.info("[FIDA Economy] Migrated legacy card balance {} to {} ({})", legacy, ownerName,
                onlineOwner != null ? "shared account" : "pending, paid out on next login");
    }

// 扫一遍一组物品（容器界面里的槽位），遇到银行卡就尝试迁移
    public static void migrateCardsIn(ServerPlayer opener, List<ItemStack> items) {
        if (!isShared() || opener == null || items == null) {
            return;
        }
        for (ItemStack stack : items) {
            if (stack != null && !stack.isEmpty()) {
                migrateCard(opener, stack);
            }
        }
    }

// 迁移记录键：有 UID 的卡按 UID，老卡按「主人 + 金额 + 纸币指纹」
    private static String cardKey(ItemStack card) {
        String uid = CreditCardItem.getUid(card);
        if (!uid.isEmpty()) {
            return "card:uid:" + uid;
        }
        return "card:legacy:" + CreditCardItem.getOwner(card) + ":" + CreditCardItem.getAccount(card)
                + ":" + cardFingerprint(card);
    }

// 卡内 9 格纸币的指纹：物品注册名 + 数量的顺序拼接后取哈希
    private static String cardFingerprint(ItemStack card) {
        StringBuilder builder = new StringBuilder();
        for (ItemStack slot : CreditCardItem.getItems(card)) {
            if (slot.isEmpty()) {
                builder.append("-;");
                continue;
            }
            builder.append(BuiltInRegistries.ITEM.getKey(slot.getItem()))
                    .append('@').append(slot.getCount()).append(';');
        }
        return Integer.toHexString(builder.toString().hashCode());
    }

// 金额工具

// NaN / Infinity / 负数都不合法
    public static boolean isSane(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value >= 0.0D;
    }

// 可花的整数金额，向下取整
    public static long spendable(double value) {
        if (!isSane(value)) {
            return 0L;
        }
        if (value >= (double) SdmEconomyBridge.MAX_EXACT) {
            return SdmEconomyBridge.MAX_EXACT;
        }
        return (long) Math.floor(value);
    }

// 回读容差：小额用固定值，大额按相对误差放宽
    private static double tolerance(double expected) {
        return Math.max(EPSILON, Math.abs(expected) * 1.0E-9D);
    }
}
