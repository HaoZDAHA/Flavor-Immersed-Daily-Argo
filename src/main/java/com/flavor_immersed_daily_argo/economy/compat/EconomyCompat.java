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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 经济联动的统一入口，全模组只在这里判断要不要走 SDM Economy。
//
//   cardBalance / cardPay / cardCredit  以银行卡为凭据的读写，联动关着时落回卡面账户
//   balance / pay / credit              直接读写玩家在共享账户里的钱
//   creditOwner                         店铺货款，店主在线直接进账，不在线挂待结算
//   migrateCard / onPlayerJoin          旧银行卡余额的一次性迁移
//
// 换算固定 1 元 = 1 basic_money，只在服务端读写，客户端只接收余额用于显示。
public final class EconomyCompat {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] SHOP_MOD_IDS = {"sdmshop", "sdmshoprework"};
    private static final String[] ECONOMY_MOD_IDS = {"sdm_economy", "sdmeconomy"};

// 检测结果缓存：null = 还没查
    private static volatile Boolean shared;

// 实际使用的货币 id
    private static volatile String currencyId = SdmEconomyBridge.DEFAULT_CURRENCY_ID;

// 客户端同步状态：0 = 未收到，1 = 开，2 = 关
    private static volatile int clientState;
    private static volatile long clientBalance;

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
// 列表读不到，按配置直接用
            if (registered.isEmpty()) {
                currencyId = configured;
                LOGGER.warn("[FIDA Economy] Cannot read the currency list; using the configured {} (writes are verified by read-back)", configured);
                return true;
            }
            LOGGER.warn("[FIDA Economy] Configured currency {} is not registered in SDM Economy; using the built-in economy. Available: {}",
                    configured, registered);
            return false;
        }
        if (registered.isEmpty()) {
// 老版本没有 getAllCurrencyKeys，按默认货币走
            currencyId = SdmEconomyBridge.DEFAULT_CURRENCY_ID;
            return true;
        }
        for (String id : registered) {
            if (id.equals(SdmEconomyBridge.DEFAULT_CURRENCY_ID)) {
                currencyId = id;
                return true;
            }
        }
        if (registered.size() == 1) {
            currencyId = registered.get(0);
            LOGGER.warn("[FIDA Economy] Default currency {} not found; only one currency is registered, using {}",
                    SdmEconomyBridge.DEFAULT_CURRENCY_ID, currencyId);
            return true;
        }
        LOGGER.warn("[FIDA Economy] Default currency {} not found; registered: {}. Set compat.currency_id to choose one",
                SdmEconomyBridge.DEFAULT_CURRENCY_ID, registered);
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
    public static void acceptClientSync(boolean active, long balance) {
        clientState = active ? 1 : 2;
        if (active) {
            clientBalance = Math.max(0L, balance);
        }
    }

// 客户端缓存的余额
    public static long clientBalance() {
        return clientBalance;
    }

// 两个金额相加，到顶停在 Long.MAX_VALUE
    public static long saturatingAdd(long a, long b) {
        return SharedEconomyData.safeAdd(a, b);
    }

// 把当前状态与余额推给这位玩家
    public static void syncBalance(ServerPlayer player) {
        if (player == null) {
            return;
        }
        boolean active = isShared();
        long balance = active ? balance(player) : 0L;
        PacketDistributor.sendToPlayer(player, new SharedBalancePacket(active, balance));
    }

// 共享账户余额，未联动返回 0
    public static long balance(ServerPlayer player) {
        if (!isShared() || player == null) {
            return 0L;
        }
        return SdmEconomyBridge.get(player, currencyId);
    }

// 够不够付
    public static boolean canAfford(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        return isShared() && balance(player) >= amount;
    }

// 从共享账户扣钱
    public static boolean pay(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (!isShared() || player == null) {
            return false;
        }
        long before = SdmEconomyBridge.get(player, currencyId);
        if (before < amount) {
            return false;
        }
        if (!SdmEconomyBridge.set(player, currencyId, before - amount)) {
            LOGGER.warn("[FIDA Economy] Payment failed for {}: balance {}, required {}",
                    player.getName().getString(), before, amount);
            return false;
        }
        syncBalance(player);
        return true;
    }

// 入账，返回真正入账的金额
    public static long credit(ServerPlayer player, long amount) {
        if (amount <= 0L || !isShared() || player == null) {
            return 0L;
        }
        long before = SdmEconomyBridge.get(player, currencyId);
        long target = before > Long.MAX_VALUE - amount ? Long.MAX_VALUE : before + amount;
        if (!SdmEconomyBridge.set(player, currencyId, target)) {
            LOGGER.warn("[FIDA Economy] Credit failed for {}: balance {}, attempted {}",
                    player.getName().getString(), before, amount);
            return 0L;
        }
        syncBalance(player);
        return target - before;
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

// 店主名字 → 待结算 key
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

// 名字 → UUID
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
}
