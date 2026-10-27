package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.all.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 银行卡
//
// 一张卡身上存着三样东西（都用数据组件 / NBT 保存）：
//
// 持卡主人：绑定一位玩家；空着就是还没绑定，谁打开卡（直接右键或走自助取款机）就先绑谁。
// 卡面账户 / 金融价值：存款余额。存钱时把卡里槽位的纸币折算进来，取钱时再换回纸币。
// 卡内 9 个槽位：只能放六种纸币。纯存储——里面的钱不参与卡面账户结算，
// 想让它变成账户里的钱，得拿着卡去自助取款机主动「存入」。
//
// 右键这张卡就能直接打开卡内 9 格的存储界面存取纸币；存/取账户、改主人则要拿着卡去自助取款机。
public class CreditCardItem extends Item {

// 卡内槽位数：1×9
    public static final int SLOT_COUNT = 9;

// 取钱时从大到小贪心兑换的面额
    public static final int[] DENOMINATIONS = {100, 50, 20, 10, 5, 1};

    public CreditCardItem(Properties properties) {
        super(properties);
    }

// 是不是六种纸币之一
    public static boolean isMoney(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MoneyItem;
    }

// 面额对应的纸币物品；不认识的面额返回空
    public static ItemStack moneyFor(int denomination) {
        return switch (denomination) {
            case 100 -> new ItemStack(ModItems.MONEY_100.get());
            case 50 -> new ItemStack(ModItems.MONEY_50.get());
            case 20 -> new ItemStack(ModItems.MONEY_20.get());
            case 10 -> new ItemStack(ModItems.MONEY_10.get());
            case 5 -> new ItemStack(ModItems.MONEY_5.get());
            case 1 -> new ItemStack(ModItems.MONEY_1.get());
            default -> ItemStack.EMPTY;
        };
    }

// 持卡主人

// 持卡主人名；没绑定时是空串
    public static String getOwner(ItemStack card) {
        return card.getOrDefault(ModDataComponents.CARD_OWNER.get(), "");
    }

    public static void setOwner(ItemStack card, String owner) {
        card.set(ModDataComponents.CARD_OWNER.get(), owner == null ? "" : owner);
    }

// 卡面账户 / 金融价值

    public static long getAccount(ItemStack card) {
        return card.getOrDefault(ModDataComponents.CARD_ACCOUNT.get(), 0L);
    }

    public static void setAccount(ItemStack card, long value) {
        card.set(ModDataComponents.CARD_ACCOUNT.get(), Math.max(0L, value));
    }

// 卡内 9 槽

// 读卡内槽位；返回的列表一定正好 #SLOT_COUNT 格（没有组件或数量对不上就归一）。
// 老存档里存的是 27 格，这里只保留前 9 格，其余丢弃，避免定长组件读不出来。
    public static List<ItemStack> getItems(ItemStack card) {
        List<ItemStack> stored = card.get(ModDataComponents.CARD_ITEMS.get());
        List<ItemStack> items = new ArrayList<>(Collections.nCopies(SLOT_COUNT, ItemStack.EMPTY));
        if (stored != null) {
            for (int i = 0; i < Math.min(SLOT_COUNT, stored.size()); i++) {
                items.set(i, stored.get(i));
            }
        }
        return items;
    }

    public static void setItems(ItemStack card, List<ItemStack> items) {
        card.set(ModDataComponents.CARD_ITEMS.get(), items);
    }

// 右键直接打开这张卡自己的 9 格存储界面（不需要自助取款机）。
//
// 这里只是存取纸币；把卡里的钱转进卡面账户、或从账户取回纸币，要拿着卡去自助取款机操作。
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            int slot = hand == InteractionHand.MAIN_HAND
                    ? player.getInventory().selected
                    : Inventory.SLOT_OFFHAND;
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new AtmMenu(id, inv, slot, false),
                    Component.translatable("gui.flavor_immersed_daily_argo.bank_card")
            ), buf -> {
                buf.writeVarInt(slot);
                buf.writeBoolean(false);
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.credit_card"));
        String owner = getOwner(stack);
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.card_owner",
                owner.isEmpty()
                        ? Component.translatable("tooltip.flavor_immersed_daily_argo.card_owner_none")
                        : Component.literal(owner)));
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.card_value", getAccount(stack)));
    }
}
