package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// 自助取款机 / 银行卡界面
//
// 只认「玩家背包里那 9 格卡内槽位 + 卡面账户」。菜单本身不挂在方块上，方块只负责把
// 「卡在哪个背包槽位」传进来，所有读写都落在那张卡的组件上，跟钓竿改装菜单一个路子。
//
// 两种打开方式：拿着卡右键自助取款机（atmMode = true，可存/取账户、改主人），
// 或直接右键那张卡（atmMode = false，只存取卡内纸币）。
public class AtmMenu extends AbstractContainerMenu {

// 菜单按钮：存入（取出改用面额本身当按钮 id：1/5/10/20/50/100）
    public static final int BUTTON_DEPOSIT = 0;

// 卡内槽位所在的列（1 行 9 列，两种界面都一样）
    public static final int CARD_X = 8;

// 自助取款机界面：顶部要留给主人 / 账户读数与按钮（含外汇页），卡槽只有 1 行
    public static final int ATM_CARD_Y = 116;
    public static final int ATM_INV_Y = 154;
    public static final int ATM_HOTBAR_Y = 212;

// 直接右键银行卡的界面：只有读数，槽位紧跟其后，界面更矮
    public static final int CARD_CARD_Y = 44;
    public static final int CARD_INV_Y = 82;
    public static final int CARD_HOTBAR_Y = 140;

    private final Player player;
    private final int cardSlot;
    private final SimpleContainer cardInv;
// true = 自助取款机界面；false = 直接右键银行卡，只存取纸币
    private final boolean atmMode;
// 外汇兑换页的条目：服务端读配置，客户端从登录包读
    private List<ExchangeEntry> exchanges = List.of();
// 装载初始槽位期间不写回，避免把空槽覆盖到卡上
    private boolean commitEnabled;

    public AtmMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, extraData.readVarInt(), extraData.readBoolean());
// 取款机模式的登录包里还带着外汇条目，读进来给界面用
        if (this.atmMode) {
            this.exchanges = EconomyConfig.readEntries(extraData);
        }
    }

// atmMode：true = 自助取款机界面；false = 直接右键银行卡
    public AtmMenu(int containerId, Inventory playerInv, int cardSlot, boolean atmMode) {
        super(ModMenus.ATM_MENU.get(), containerId);
        this.player = playerInv.player;
        this.cardSlot = cardSlot;
        this.atmMode = atmMode;
        this.exchanges = atmMode ? EconomyConfig.entries() : List.of();
// 槽位一有变化就立刻写回银行卡：不存在「未保存」的中间状态
        this.cardInv = new SimpleContainer(CreditCardItem.SLOT_COUNT) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (commitEnabled && !player.level().isClientSide()) {
                    commitItems();
                }
            }
        };

// 载入卡里已有的 9 格
        List<ItemStack> items = CreditCardItem.getItems(card());
        for (int i = 0; i < CreditCardItem.SLOT_COUNT; i++) {
            cardInv.setItem(i, items.get(i).copy());
        }
        this.commitEnabled = true;

// 卡还没主人的话，谁打开界面就把谁绑成主人
        if (!player.level().isClientSide() && CreditCardItem.getOwner(card()).isEmpty()) {
            CreditCardItem.setOwner(card(), player.getName().getString());
            player.getInventory().setChanged();
        }

// 槽位坐标随界面不同：取款机模式顶部还有操作区，整体靠下
        int cardY = atmMode ? ATM_CARD_Y : CARD_CARD_Y;
        int invY = atmMode ? ATM_INV_Y : CARD_INV_Y;
        int hotbarY = atmMode ? ATM_HOTBAR_Y : CARD_HOTBAR_Y;

// 卡内 9 格：只收六种纸币
        for (int col = 0; col < CreditCardItem.SLOT_COUNT; col++) {
            addSlot(new Slot(cardInv, col, CARD_X + col * 18, cardY) {
                @Override
                public boolean mayPlace(ItemStack s) {
                    return CreditCardItem.isMoney(s);
                }
            });
        }

// 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
// 玩家快捷栏
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

// 是不是自助取款机界面
    public boolean isAtmMode() {
        return this.atmMode;
    }

// 界面里那张卡（每次现取，避免物品栈被替换后引用失效）
    public ItemStack card() {
        return this.player.getInventory().getItem(this.cardSlot);
    }

    public long getAccount() {
        return CreditCardItem.getAccount(card());
    }

    public String getOwnerName() {
        return CreditCardItem.getOwner(card());
    }

// 当前打开界面的玩家是不是持卡主人
    public boolean isOwner() {
        String owner = CreditCardItem.getOwner(card());
        return !owner.isEmpty() && owner.equals(this.player.getName().getString());
    }

// 外汇兑换条目（服务端读配置，客户端读登录包）
    public List<ExchangeEntry> getExchanges() {
        return this.exchanges;
    }

// 外汇兑换：按 ExchangeEntry#price() 从卡面账户扣钱，把目标物品发进玩家背包。
// 只有持卡主人能兑换，余额不够或下标越界就不动。
    public void exchange(ServerPlayer player, int index) {
        if (player != this.player || !this.atmMode || !isOwner()) {
            return;
        }
        if (index < 0 || index >= this.exchanges.size()) {
            return;
        }
        ExchangeEntry entry = this.exchanges.get(index);
        long price = entry.price();
        long account = CreditCardItem.getAccount(card());
        if (price <= 0L || account < price) {
            return;
        }
        CreditCardItem.setAccount(card(), account - price);
        ItemStack reward = new ItemStack(entry.target(), entry.targetCount());
        if (!this.player.getInventory().add(reward)) {
            this.player.drop(reward, false);
        }
        this.player.getInventory().setChanged();
        broadcastChanges();
    }

// 把 9 格内容写回银行卡
    private void commitItems() {
        List<ItemStack> items = new ArrayList<>(CreditCardItem.SLOT_COUNT);
        for (int i = 0; i < CreditCardItem.SLOT_COUNT; i++) {
            items.add(cardInv.getItem(i).copy());
        }
        CreditCardItem.setItems(card(), items);
    }

// 界面按钮：只有持卡主人能存 / 取。存入用 #BUTTON_DEPOSIT；
// 取出直接拿面额当按钮 id（1/5/10/20/50/100），点哪个面额就取哪个。
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player != this.player || !this.atmMode || !isOwner()) {
            return false;
        }
        if (id == BUTTON_DEPOSIT) {
            deposit();
            return true;
        }
        if (isDenomination(id)) {
            withdraw(id);
            return true;
        }
        return false;
    }

// 是不是六种纸币之一的面额
    public static boolean isDenomination(int value) {
        for (int denom : CreditCardItem.DENOMINATIONS) {
            if (denom == value) {
                return true;
            }
        }
        return false;
    }

// 存钱：把 9 格里所有纸币按面额折算进卡面账户，格子清空
    private void deposit() {
        long gained = 0L;
        for (int i = 0; i < CreditCardItem.SLOT_COUNT; i++) {
            ItemStack s = cardInv.getItem(i);
            if (CreditCardItem.isMoney(s)) {
                gained += (long) MoneyItem.valueOf(s) * s.getCount();
                cardInv.setItem(i, ItemStack.EMPTY);
            }
        }
        if (gained > 0L) {
            CreditCardItem.setAccount(card(), CreditCardItem.getAccount(card()) + gained);
        }
        commitItems();
        broadcastChanges();
    }

// 取钱：只取指定的面额，单次最多一整组（64 张），并且只往卡里已有的同类堆叠或空槽里塞，
// 塞不完的仍留在卡面账户——所以点一下最多占掉一格，不会一口气把整张卡塞爆。
    private void withdraw(int denomination) {
        long account = CreditCardItem.getAccount(card());
        if (account < denomination) {
            return;
        }
        int budget = (int) Math.min(64L, account / denomination);
        int placed = 0;

// 先并入卡里已有的同面额堆叠
        for (int i = 0; i < CreditCardItem.SLOT_COUNT && placed < budget; i++) {
            ItemStack s = cardInv.getItem(i);
            if (s.is(CreditCardItem.moneyFor(denomination).getItem())) {
                int add = Math.min(s.getMaxStackSize() - s.getCount(), budget - placed);
                if (add > 0) {
                    s.grow(add);
                    placed += add;
                }
            }
        }
// 再往空槽里放
        for (int i = 0; i < CreditCardItem.SLOT_COUNT && placed < budget; i++) {
            if (cardInv.getItem(i).isEmpty()) {
                int add = Math.min(64, budget - placed);
                cardInv.setItem(i, new ItemStack(CreditCardItem.moneyFor(denomination).getItem(), add));
                placed += add;
            }
        }

        if (placed > 0) {
            CreditCardItem.setAccount(card(), account - (long) placed * denomination);
            commitItems();
            broadcastChanges();
        }
    }

// 改持卡主人：只有现任主人能改；输入空串就是解绑
    public void changeOwner(ServerPlayer player, String newOwner) {
        if (player != this.player || !isOwner()) {
            return;
        }
        String trimmed = newOwner == null ? "" : newOwner.trim();
        CreditCardItem.setOwner(card(), trimmed);
        player.getInventory().setChanged();
        broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        int cardSlots = CreditCardItem.SLOT_COUNT;
        if (index < cardSlots) {
            if (!this.moveItemStackTo(stack, cardSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, cardSlots, false)) {
            return ItemStack.EMPTY;
        }
        slot.setChanged();
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(this.cardSlot).getItem() instanceof CreditCardItem;
    }
}
