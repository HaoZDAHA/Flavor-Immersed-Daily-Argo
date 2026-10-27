package com.flavor_immersed_daily_argo.economy.shop;

import com.flavor_immersed_daily_argo.economy.CreditCardItem;
import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import com.flavor_immersed_daily_argo.economy.product.EconomicNews;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import java.util.List;

// 玩家店铺存档。
//
// 只保存「玩家开的店」（系统店铺由数据包 / 配置文件驱动，不落存档），挂在主世界的 getDataStorage()，
// 键 flavor_immersed_daily_argo_shops。多人联机下全服共一份，任何人在商业电脑里都能买到。
public class ShopManager extends SavedData {

    public static final String DATA_NAME = "flavor_immersed_daily_argo_shops";

    private static final SavedData.Factory<ShopManager> FACTORY =
            new SavedData.Factory<>(ShopManager::new, (tag, provider) -> new ShopManager(tag), null);

    private final List<Shop> shops = new ArrayList<>();

    public ShopManager() {
    }

    private ShopManager(CompoundTag tag) {
        ListTag list = tag.getList("Shops", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            List<ShopEntry> entries = new ArrayList<>();
            ListTag entriesTag = entry.getList("Entries", Tag.TAG_COMPOUND);
            for (int j = 0; j < entriesTag.size(); j++) {
                CompoundTag e = entriesTag.getCompound(j);
                ResourceLocation rl = ResourceLocation.tryParse(e.getString("Item"));
                Item item = rl == null ? Items.AIR : BuiltInRegistries.ITEM.get(rl);
                if (item == Items.AIR) {
                    continue;
                }
                entries.add(new ShopEntry(item, e.getInt("Count"), e.getInt("Price"), e.getInt("Stock")));
            }
            shops.add(new Shop(entry.getString("Id"), entry.getString("Title"), entry.getString("Owner"),
                    List.copyOf(entries), entry.getLong("Balance"), true));
        }
    }

    public static ShopManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

// 本存档里的玩家店铺（可变列表）
    public List<Shop> shops() {
        return this.shops;
    }

// 全量店铺 = 系统店铺（数据包） + 配置文件商店 + 玩家店铺
    public List<Shop> allShops(MinecraftServer server) {
        List<Shop> result = new ArrayList<>(ShopDataLoader.SHOPS);
        Shop configShop = EconomyConfig.configShop();
        if (configShop != null) {
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
        String cleanTitle = title == null || title.isBlank() ? owner + "的小店" : title.trim();
        this.shops.add(new Shop(nextShopId(owner), cleanTitle, owner, List.of(), 0L, true));
        this.setDirty();
        return true;
    }

// 给店主分配一个不冲突的序号，作为店铺 id
    private String nextShopId(String owner) {
        int index = 1;
        String prefix = "player:" + owner + ":";
        boolean clash = true;
        while (clash) {
            clash = false;
            String candidate = prefix + index;
            for (Shop shop : this.shops) {
                if (shop.id().equals(candidate)) {
                    clash = true;
                    index++;
                    break;
                }
            }
        }
        return prefix + index;
    }

// 删店：只有店主能删
    public boolean deleteShop(ServerPlayer player, String shopId) {
        Shop shop = findOwned(player, shopId);
        if (shop == null) {
            return false;
        }
        this.shops.remove(shop);
        this.setDirty();
        return true;
    }

// 上架：取走手上整叠物品作为库存；同物品同价则并入库存增量
    public boolean addEntry(ServerPlayer player, String shopId, ItemStack goods, int price) {
        Shop shop = findOwned(player, shopId);
        if (shop == null || goods == null || goods.isEmpty() || price < 0) {
            return false;
        }
        List<ShopEntry> entries = new ArrayList<>(shop.entries());
        int amount = goods.getCount();
        for (int i = 0; i < entries.size(); i++) {
            ShopEntry e = entries.get(i);
            if (e.item() == goods.getItem() && e.price() == price) {
                entries.set(i, new ShopEntry(e.item(), e.count(), e.price(), e.stock() + amount));
                replace(shop.id(), entries, shop.balance());
                this.setDirty();
                return true;
            }
        }
// 玩家上架：一次买 1 件，库存 = 整叠件数
        entries.add(new ShopEntry(goods.getItem(), 1, price, amount));
        replace(shop.id(), entries, shop.balance());
        this.setDirty();
        return true;
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
            give(player, new ItemStack(entry.item(), back));
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
        if (shop == null || index < 0 || index >= shop.entries().size() || price < 0) {
            return false;
        }
        List<ShopEntry> entries = new ArrayList<>(shop.entries());
        ShopEntry e = entries.get(index);
        entries.set(index, new ShopEntry(e.item(), e.count(), price, e.stock()));
        replace(shop.id(), entries, shop.balance());
        this.setDirty();
        return true;
    }

// 补货：手上整叠物品须与条目同物品，整叠并入该条目库存
    public boolean restock(ServerPlayer player, String shopId, int index, ItemStack goods) {
        Shop shop = findOwned(player, shopId);
        if (shop == null || index < 0 || index >= shop.entries().size() || goods == null || goods.isEmpty()) {
            return false;
        }
        List<ShopEntry> entries = new ArrayList<>(shop.entries());
        ShopEntry e = entries.get(index);
        if (e.item() != goods.getItem()) {
            return false;
        }
        entries.set(index, new ShopEntry(e.item(), e.count(), e.price(), e.stock() + goods.getCount()));
        replace(shop.id(), entries, shop.balance());
        this.setDirty();
        return true;
    }

// 提现：把店铺余额写进这张卡的卡面账户，余额清零
    public boolean withdraw(ServerPlayer player, String shopId, ItemStack card) {
        Shop shop = findOwned(player, shopId);
        if (shop == null || card == null || !(card.getItem() instanceof CreditCardItem) || shop.balance() <= 0L) {
            return false;
        }
        CreditCardItem.setAccount(card, CreditCardItem.getAccount(card) + shop.balance());
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
        if (cart.isEmpty()) {
            message(player, "cart_empty");
            return;
        }
        if (cardSlot < 0 || cardSlot > 40) {
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

// 先整体校验：店铺 / 下标 / 库存 / 余额（逐行对齐，无效行留空）
        long total = 0L;
        ShopEntry[] resolved = new ShopEntry[cart.size()];
        for (int i = 0; i < cart.size(); i++) {
            CartLine line = cart.get(i);
            Shop shop = find(all, line.shopId());
            if (shop == null || line.entryIndex() < 0 || line.entryIndex() >= shop.entries().size()) {
                continue;
            }
            ShopEntry entry = shop.entries().get(line.entryIndex());
            if (!entry.infinite() && entry.stock() < line.qty()) {
                message(player, "out_of_stock");
                return;
            }
            total += (long) entry.price() * line.qty();
            resolved[i] = entry;
        }
        long account = CreditCardItem.getAccount(card);
        if (account < total) {
            message(player, "insufficient");
            return;
        }

// 扣钱并发货
        CreditCardItem.setAccount(card, account - total);
        for (int i = 0; i < cart.size(); i++) {
            ShopEntry entry = resolved[i];
            if (entry == null) {
                continue;
            }
            CartLine line = cart.get(i);
            Shop shop = find(all, line.shopId());
            give(player, new ItemStack(entry.item(), entry.count() * line.qty()));
            if (shop != null && shop.playerShop()) {
                manager.applyPurchase(shop.id(), line.entryIndex(), line.qty(), entry.price());
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
            entries.set(index, new ShopEntry(e.item(), e.count(), e.price(), stock));
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

    private Shop findOwned(ServerPlayer player, String shopId) {
        String owner = player.getName().getString();
        for (Shop shop : this.shops) {
            if (shop.id().equals(shopId) && shop.ownerName().equals(owner)) {
                return shop;
            }
        }
        return null;
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

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!stack.isEmpty() && !player.getInventory().add(stack)) {
            player.drop(stack, false);
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
                eTag.putInt("Count", e.count());
                eTag.putInt("Price", e.price());
                eTag.putInt("Stock", e.stock());
                entriesTag.add(eTag);
            }
            entry.put("Entries", entriesTag);
            list.add(entry);
        }
        tag.put("Shops", list);
        return tag;
    }
}
