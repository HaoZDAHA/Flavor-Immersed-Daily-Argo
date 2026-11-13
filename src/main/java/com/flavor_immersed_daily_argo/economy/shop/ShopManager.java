package com.flavor_immersed_daily_argo.economy.shop;

import com.flavor_immersed_daily_argo.economy.CreditCardItem;
import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import com.flavor_immersed_daily_argo.economy.product.EconomicNews;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 玩家店铺存档。
//
// 保存「玩家开的店」（系统店铺由数据包 / 配置文件驱动，不落存档），以及被管理员
// 删掉的系统店铺 id（落存档后重新加载数据包也不会再出现）。挂在主世界的 getDataStorage()，
// 键 flavor_immersed_daily_argo_shops。多人联机下全服共一份，任何人在商业电脑里都能买到。
public class ShopManager extends SavedData {

    public static final String DATA_NAME = "flavor_immersed_daily_argo_shops";

    private static final SavedData.Factory<ShopManager> FACTORY =
            new SavedData.Factory<>(ShopManager::new, ShopManager::new, null);

    private final List<Shop> shops = new ArrayList<>();
    private final Set<String> removedSystemShops = new HashSet<>();

    public ShopManager() {
    }

    private ShopManager(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = tag.getList("Shops", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            List<ShopEntry> entries = new ArrayList<>();
            ListTag entriesTag = entry.getList("Entries", Tag.TAG_COMPOUND);
            for (int j = 0; j < entriesTag.size(); j++) {
                CompoundTag e = entriesTag.getCompound(j);
                ItemStack goods;
                if (e.contains("Stack", Tag.TAG_COMPOUND)) {
                    goods = ItemStack.parse(registries, e.getCompound("Stack")).orElse(ItemStack.EMPTY);
                } else {
                    ResourceLocation rl = ResourceLocation.tryParse(e.getString("Item"));
                    Item item = rl == null ? Items.AIR : BuiltInRegistries.ITEM.get(rl);
                    goods = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
                }
                if (goods.isEmpty()) {
                    continue;
                }
                // Count was already the purchase lot size in old saves. Never reinterpret
                // Count=1 as a whole stack: doing so would multiply both stock and delivered goods.
                int count = e.contains("Count", Tag.TAG_ANY_NUMERIC) ? e.getInt("Count") : 1;
                int price = e.getInt("Price");
                int stock = e.getInt("Stock");
                if (count <= 0 || price < 0 || stock < 0 || (long) count * stock > Integer.MAX_VALUE) {
                    continue;
                }
                entries.add(new ShopEntry(goods, count, price, stock));
            }
            shops.add(new Shop(entry.getString("Id"), entry.getString("Title"), entry.getString("Owner"),
                    List.copyOf(entries), entry.getLong("Balance"), true));
        }
        ListTag removed = tag.getList("RemovedSystemShops", Tag.TAG_STRING);
        for (int i = 0; i < removed.size(); i++) {
            removedSystemShops.add(removed.getString(i));
        }
        normalizeIds();
    }

// 店铺编号必须是全服唯一的正整数；旧存档的 player:<名>:<序号> 在这里迁移成数字编号
    private void normalizeIds() {
        Set<Integer> used = new HashSet<>();
        for (Shop shop : this.shops) {
            int id = parseId(shop.id());
            if (id > 0) {
                used.add(id);
            }
        }
        Set<Integer> assigned = new HashSet<>();
        for (int i = 0; i < this.shops.size(); i++) {
            Shop shop = this.shops.get(i);
            int id = parseId(shop.id());
            if (id > 0 && assigned.add(id)) {
                continue;
            }
            int next = 1;
            while (used.contains(next)) {
                next++;
            }
            used.add(next);
            assigned.add(next);
            this.shops.set(i, new Shop(Integer.toString(next), shop.title(), shop.ownerName(),
                    shop.entries(), shop.balance(), shop.playerShop()));
        }
    }

    private static int parseId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public static ShopManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

// 本存档里的玩家店铺（可变列表）
    public List<Shop> shops() {
        return this.shops;
    }

// 全量店铺 = 系统店铺（数据包） + 配置文件商店 + 玩家店铺；被删掉的系统店铺会过滤掉
    public List<Shop> allShops(MinecraftServer server) {
        List<Shop> result = new ArrayList<>();
        for (Shop shop : ShopDataLoader.SHOPS) {
            if (!removedSystemShops.contains(shop.id())) {
                result.add(shop);
            }
        }
        Shop configShop = EconomyConfig.configShop();
        if (configShop != null && !removedSystemShops.contains(configShop.id())) {
            result.add(configShop);
        }
        result.addAll(this.shops);
        return result;
    }

// 开店

// 按店主统计已开店铺数
    public int countShops(String ownerName) {
        int n = 0;
        for (Shop shop : this.shops) {
            if (shop.ownerName().equals(ownerName)) {
                n++;
            }
        }
        return n;
    }

// 创建店铺：受配置开关与数量上限限制；成功返回 true 并标脏
    public boolean createShop(ServerPlayer player, String title) {
        String owner = player.getName().getString();
        if (!EconomyConfig.PLAYER_SHOPS_ENABLED.get()) {
            return false;
        }
        if (countShops(owner) >= EconomyConfig.MAX_SHOPS_PER_PLAYER.get()) {
            return false;
        }
        // Empty is the persisted default-title marker; never render localized text on the server.
        String cleanTitle = title == null || title.isBlank() ? "" : title.trim();
        if (cleanTitle.length() > 64) {
            cleanTitle = cleanTitle.substring(0, 64);
        }
        this.shops.add(new Shop(nextShopId(), cleanTitle, owner, List.of(), 0L, true));
        this.setDirty();
        return true;
    }

// 分配全服最小的可用数字编号
    private String nextShopId() {
        Set<Integer> used = new HashSet<>();
        for (Shop shop : this.shops) {
            int id = parseId(shop.id());
            if (id > 0) {
                used.add(id);
            }
        }
        int index = 1;
        while (used.contains(index)) {
            index++;
        }
        return Integer.toString(index);
    }

// 删店：只有店主能删
    public boolean deleteShop(ServerPlayer player, String shopId) {
        return deleteShop(player, shopId, false);
    }

// 按店铺编号删除。创造模式可删任意店铺，系统店铺也会被记入存档黑名单；非创造只能删自己的玩家店铺
    public boolean deleteShopByNumber(ServerPlayer player, int number, boolean ignoreOwner) {
        List<Shop> all = allShops(player.server);
        Shop target = null;
        for (Shop candidate : all) {
            if (candidate.numberIn(all) == number) {
                target = candidate;
                break;
            }
        }
        if (target == null) {
            return false;
        }
        if (target.isSystem()) {
            if (!ignoreOwner) {
                return false;
            }
            this.removedSystemShops.add(target.id());
            this.setDirty();
            return true;
        }
        if (!ignoreOwner && !target.ownerName().equals(player.getName().getString())) {
            return false;
        }
        Shop shop = target;
        this.shops.removeIf(candidate -> candidate.id().equals(shop.id()));
        this.setDirty();
        return true;
    }

// 创造模式可无视店主身份直接删除（供管理员指令使用）
    public boolean deleteShop(ServerPlayer player, String shopId, boolean ignoreOwner) {
        Shop shop = ignoreOwner ? findShop(shopId) : findOwned(player, shopId);
        if (shop == null) {
            return false;
        }
        this.shops.remove(shop);
        this.setDirty();
        return true;
    }

// 上架：perStack=false 论个（一次买 1 件），true 论组（一次买一整组）。
// 返回实际从手上消耗的物品数，0 表示上架失败；论组时手上不足一组也返回 0
    public int addEntry(ServerPlayer player, String shopId, ItemStack goods, int price, boolean perStack) {
        Shop shop = findOwned(player, shopId);
        if (shop == null || goods == null || goods.isEmpty() || price <= 0) {
            return 0;
        }
        int unit = perStack ? Math.max(1, goods.getMaxStackSize()) : 1;
        int units = goods.getCount() / unit;
        if (units <= 0) {
            return 0;
        }
        List<ShopEntry> entries = new ArrayList<>(shop.entries());
        for (int i = 0; i < entries.size(); i++) {
            ShopEntry e = entries.get(i);
            if (e.matches(goods) && e.price() == price && e.count() == unit) {
                if (!canIncreaseStock(e, units)) {
                    return 0;
                }
                entries.set(i, e.withStock(e.stock() + units));
                replace(shop.id(), entries, shop.balance());
                this.setDirty();
                return units * unit;
            }
        }
        entries.add(new ShopEntry(goods, unit, price, units));
        replace(shop.id(), entries, shop.balance());
        this.setDirty();
        return units * unit;
    }

// 下架：把剩余库存退回店主背包，删除条目
    public boolean removeEntry(ServerPlayer player, String shopId, int index) {
        Shop shop = findOwned(player, shopId);
        if (shop == null || index < 0 || index >= shop.entries().size()) {
            return false;
        }
        ShopEntry entry = shop.entries().get(index);
        int back = entry.infinite() ? 0 : Math.max(0, entry.count() * entry.stock());
        if (back > 0) {
            give(player, entry.createStack(back));
        }
        List<ShopEntry> entries = new ArrayList<>(shop.entries());
        entries.remove(index);
        replace(shop.id(), entries, shop.balance());
        this.setDirty();
        return true;
    }

// 改价
    public boolean setPrice(ServerPlayer player, String shopId, int index, int price) {
        Shop shop = findOwned(player, shopId);
        if (shop == null || index < 0 || index >= shop.entries().size() || price <= 0) {
            return false;
        }
        List<ShopEntry> entries = new ArrayList<>(shop.entries());
        ShopEntry e = entries.get(index);
        entries.set(index, e.withPrice(price));
        replace(shop.id(), entries, shop.balance());
        this.setDirty();
        return true;
    }

// 补货：按条目原本的出售单位（论个 / 论组）折算，返回实际消耗的物品数，0 表示失败
    public int restock(ServerPlayer player, String shopId, int index, ItemStack goods) {
        Shop shop = findOwned(player, shopId);
        if (shop == null || index < 0 || index >= shop.entries().size() || goods == null || goods.isEmpty()) {
            return 0;
        }
        List<ShopEntry> entries = new ArrayList<>(shop.entries());
        ShopEntry e = entries.get(index);
        if (!e.matches(goods)) {
            return 0;
        }
        int unit = Math.max(1, e.count());
        int units = goods.getCount() / unit;
        if (units <= 0 || !canIncreaseStock(e, units)) {
            return 0;
        }
        entries.set(index, e.withStock(e.stock() + units));
        replace(shop.id(), entries, shop.balance());
        this.setDirty();
        return units * unit;
    }

    private static boolean canIncreaseStock(ShopEntry entry, int units) {
        long stock = (long) entry.stock() + units;
        return entry.stock() >= 0 && entry.count() > 0 && stock <= Integer.MAX_VALUE
                && stock * entry.count() <= Integer.MAX_VALUE;
    }

// 提现：把店铺余额写进这张卡的卡面账户，余额清零
    public boolean withdraw(ServerPlayer player, String shopId, ItemStack card) {
        Shop shop = findOwned(player, shopId);
        if (shop == null || card == null || !(card.getItem() instanceof CreditCardItem) || shop.balance() <= 0L) {
            return false;
        }
        long account = CreditCardItem.getAccount(card);
        if (account > Long.MAX_VALUE - shop.balance()) {
            return false;
        }
        CreditCardItem.setAccount(card, account + shop.balance());
        replace(shop.id(), shop.entries(), 0L);
        player.getInventory().setChanged();
        this.setDirty();
        return true;
    }

// 结账

// 结账：校验「购物车非空 / 选中的背包格是银行卡 / 卡面账户 ≥ 合计 / 有限库存足量」，
// 通过则扣卡面账户、发物品、扣玩家店铺库存、把货款累加进店主 balance。
// 任一失败都用 actionBar 提示对应语言键且不扣钱。
//
// cardSlot：玩家在背包里选中的银行卡所在格（0–35 主背包 / 40 副手）
    public static void checkout(ServerPlayer player, List<CartLine> cart, int cardSlot) {
        if (cart == null || cart.isEmpty()) {
            message(player, "cart_empty");
            return;
        }
        if (!((cardSlot >= 0 && cardSlot < 36) || cardSlot == 40)) {
            message(player, "no_card");
            return;
        }
        ItemStack card = player.getInventory().getItem(cardSlot);
        if (!(card.getItem() instanceof CreditCardItem)) {
            message(player, "no_card");
            return;
        }
        ShopManager manager = get(player.server);
        List<Shop> all = manager.allShops(player.server);

// 先合并重复行再整单验证：不能用多行分别通过库存校验来超卖，也不能以负数量反向扣费。
        if (cart.size() > ShopCheckoutPacket.MAX_LINES) {
            message(player, "invalid_cart");
            return;
        }
        record EntryKey(String shopId, int index) { }
        record Purchase(Shop shop, int index, ShopEntry entry, int qty) { }
        Map<EntryKey, Integer> quantities = new LinkedHashMap<>();
        for (CartLine line : cart) {
            if (line == null || line.shopId() == null || line.qty() <= 0
                    || line.qty() > ShopCheckoutPacket.MAX_QTY_PER_ENTRY) {
                message(player, "invalid_cart");
                return;
            }
            EntryKey key = new EntryKey(line.shopId(), line.entryIndex());
            long combined = (long) quantities.getOrDefault(key, 0) + line.qty();
            if (combined > ShopCheckoutPacket.MAX_QTY_PER_ENTRY) {
                message(player, "invalid_cart");
                return;
            }
            quantities.put(key, (int) combined);
        }
        List<Purchase> purchases = new ArrayList<>();
        Map<String, Long> sellerReceipts = new LinkedHashMap<>();
        long total = 0L;
        long itemTotal = 0L;
        for (Map.Entry<EntryKey, Integer> line : quantities.entrySet()) {
            Shop shop = find(all, line.getKey().shopId());
            int index = line.getKey().index();
            int qty = line.getValue();
            if (shop == null || index < 0 || index >= shop.entries().size()) {
                message(player, "invalid_cart");
                return;
            }
            ShopEntry entry = shop.entries().get(index);
            if (entry.count() <= 0 || entry.price() < 0 || entry.item() == Items.AIR
                    || (shop.playerShop() && entry.infinite())) {
                message(player, "invalid_cart");
                return;
            }
            if (!entry.infinite() && entry.stock() < qty) {
                message(player, "out_of_stock");
                return;
            }
            long items = (long) entry.count() * qty;
            itemTotal += items;
            if (items > ShopCheckoutPacket.MAX_ITEMS_PER_CHECKOUT
                    || itemTotal > ShopCheckoutPacket.MAX_ITEMS_PER_CHECKOUT) {
                message(player, "invalid_cart");
                return;
            }
            long cost = (long) entry.price() * qty;
            total = Math.addExact(total, cost);
            if (shop.playerShop()) {
                long receipt = sellerReceipts.getOrDefault(shop.id(), 0L) + cost;
                if (shop.balance() < 0 || shop.balance() > Long.MAX_VALUE - receipt) {
                    message(player, "invalid_cart");
                    return;
                }
                sellerReceipts.put(shop.id(), receipt);
            }
            purchases.add(new Purchase(shop, index, entry, qty));
        }
        if (CreditCardItem.getSpendable(card) < total) {
            message(player, "insufficient");
            return;
        }

// 所有校验完成后再扣钱、发货；count 是每份件数，price 是每份总价，二者不会重复相乘。
        if (!CreditCardItem.tryPay(card, total)) {
            message(player, "insufficient");
            return;
        }
        for (Purchase purchase : purchases) {
            ShopEntry entry = purchase.entry();
            give(player, entry.createStack(entry.count() * purchase.qty()));
            if (purchase.shop().playerShop()) {
                manager.applyPurchase(purchase.shop().id(), purchase.index(), purchase.qty(), entry.price());
            }
        }
        player.getInventory().setChanged();
// 玩家自己的背包这一份也要立刻重发，否则买到的物品要等关掉界面才显形
        player.inventoryMenu.broadcastFullState();
        manager.setDirty();
        syncAll(player.server);
// 成功回执：客户端据此清空购物车并提示金额
        Component done = Component.translatable(
                "message.flavor_immersed_daily_argo.shop.checked_out", total);
        player.displayClientMessage(done, true);
        PacketDistributor.sendToPlayer(player, new ShopCheckoutResultPacket(true, done));
    }

// 买家付的钱累加进店主余额，有限库存按购买次数扣减
    private void applyPurchase(String shopId, int index, int qty, int price) {
        for (int i = 0; i < this.shops.size(); i++) {
            Shop shop = this.shops.get(i);
            if (!shop.id().equals(shopId)) {
                continue;
            }
            List<ShopEntry> entries = new ArrayList<>(shop.entries());
            ShopEntry e = entries.get(index);
            int stock = e.infinite() ? e.stock() : Math.max(0, e.stock() - qty);
            entries.set(index, e.withStock(stock));
            this.shops.set(i, new Shop(shop.id(), shop.title(), shop.ownerName(), List.copyOf(entries),
                    shop.balance() + (long) price * qty, true));
            return;
        }
    }

// 同步

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ShopSyncPacket(
                get(player.server).allShops(player.server), todayNews(player.server)));
    }

    public static void syncAll(MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(new ShopSyncPacket(
                get(server).allShops(server), todayNews(server)));
    }

// 今日经济产物涨跌（按当前游戏天数算）
    private static List<EconomicNews> todayNews(MinecraftServer server) {
        return EconomicNews.today(server.overworld().getDayTime() / 24000L);
    }

// 工具

    private Shop findShop(String shopId) {
        for (Shop shop : this.shops) {
            if (shop.id().equals(shopId)) {
                return shop;
            }
        }
        return null;
    }

    private Shop findOwned(ServerPlayer player, String shopId) {
        Shop shop = findShop(shopId);
        String owner = player.getName().getString();
        return shop != null && shop.ownerName().equals(owner) ? shop : null;
    }

    private static Shop find(List<Shop> shops, String id) {
        for (Shop shop : shops) {
            if (shop.id().equals(id)) {
                return shop;
            }
        }
        return null;
    }

// 用新的条目 / 余额替换某家店
    private void replace(String shopId, List<ShopEntry> entries, long balance) {
        for (int i = 0; i < this.shops.size(); i++) {
            Shop shop = this.shops.get(i);
            if (shop.id().equals(shopId)) {
                this.shops.set(i, new Shop(shop.id(), shop.title(), shop.ownerName(), List.copyOf(entries), balance, true));
                return;
            }
        }
    }

// 还给玩家：按最大堆叠数分块发放。
//
// 一次下架可能要退还几百件，直接塞进一个超过最大堆叠数的 ItemStack 交给背包，
// 背包只收得下一部分、剩下的会被静默丢掉；分块后每块都能单独进背包，塞不下就掉在脚边。
    private static void give(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        int max = Math.max(1, stack.getMaxStackSize());
        ItemStack rest = stack.copy();
        while (!rest.isEmpty()) {
            int size = Math.min(rest.getCount(), max);
            ItemStack part = rest.copyWithCount(size);
            if (!player.getInventory().add(part) && !part.isEmpty()) {
                player.drop(part, false);
            }
            rest.shrink(size);
        }
    }

// 失败提示：actionBar + 界面内反馈（容器界面打开时 actionBar 看不见，得靠回执包）
    private static void message(ServerPlayer player, String key) {
        Component text = Component.translatable("message.flavor_immersed_daily_argo.shop." + key);
        player.displayClientMessage(text, true);
        PacketDistributor.sendToPlayer(player, new ShopCheckoutResultPacket(false, text));
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Shop shop : this.shops) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", shop.id());
            entry.putString("Title", shop.title());
            entry.putString("Owner", shop.ownerName());
            entry.putLong("Balance", shop.balance());
            ListTag entriesTag = new ListTag();
            for (ShopEntry e : shop.entries()) {
                CompoundTag eTag = new CompoundTag();
                eTag.putString("Item", BuiltInRegistries.ITEM.getKey(e.item()).toString());
                eTag.put("Stack", e.prototype().save(registries));
                eTag.putInt("Count", e.count());
                eTag.putInt("Price", e.price());
                eTag.putInt("Stock", e.stock());
                entriesTag.add(eTag);
            }
            entry.put("Entries", entriesTag);
            list.add(entry);
        }
        tag.put("Shops", list);
        ListTag removed = new ListTag();
        for (String id : this.removedSystemShops) {
            removed.add(StringTag.valueOf(id));
        }
        tag.put("RemovedSystemShops", removed);
        return tag;
    }
}
