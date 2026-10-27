package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.economy.product.EconomicNews;
import com.flavor_immersed_daily_argo.economy.product.WeightPriceData;
import com.flavor_immersed_daily_argo.item.PoultryCatchingNetItem;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 收购箱方块实体：27 格容器，把经济产物放进去点「出售」按今天的收购价换成纸币。
//
// 箱格内容与钓鱼箱一样落进方块实体存档；空手右键打开界面。
public class PurchasingBoxBlockEntity extends BlockEntity implements MenuProvider {

// 箱格数量：3 行 × 9 列，与原版单箱一致
    public static final int SLOT_COUNT = 27;

    private static final String INVENTORY_TAG = "Inventory";

// 纸币，面额从大到小；出售后按这个顺序找零，先产大票
    private static final List<DeferredHolder<Item, Item>> MONEY_ITEMS = List.of(
            ModItems.MONEY_100, ModItems.MONEY_50, ModItems.MONEY_20,
            ModItems.MONEY_10, ModItems.MONEY_5, ModItems.MONEY_1);
    private static final int[] MONEY_VALUES = {100, 50, 20, 10, 5, 1};

// 箱格内容；改动后写回方块实体，存档才能拿走东西
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            PurchasingBoxBlockEntity.this.setChanged();
        }
    };

    public PurchasingBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PURCHASING_BOX_ENTITY.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

// 出售

// 把箱内所有经济产物按第 day 天的价格卖掉，钱换成纸币原样放回箱子。
//
// 结算分两路：
//
// 按数量：价格是「每组（EconomicNews#PRICE_UNIT 个）」的价，一件产物的钱即
// 价格 × 数量 ÷ 每组个数。先把所有产物的分子累加成整数再一次除，避免逐件取整丢钱；
// 按重量：装着有体重标准的动物的编制麻袋、以及能称重的生鱼。这两种直接就是「一件多少钱」
// （见 WeightPriceData，已各自向上取整），不再除以每组个数。
//
// 纸币按面额从大到小塞，先产大票。
//
// 放着不卖的东西（不是经济产物）原封不动。整单要么全部成交，要么因为箱子塞不下纸币
// 而直接作废——不会出现产物没了钱也没了。
//
// 返回：卖得的总额；没东西可卖或箱子空间不够时返回 0
    public int sell(long day) {
        Map<Item, Integer> prices = EconomicNews.todayPrices(day);
        long numerator = 0;
        int weighedMoney = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            Integer price = prices.get(stack.getItem());
            if (price != null) {
                numerator += (long) price * stack.getCount();
                continue;
            }
            weighedMoney += weighedEachPrice(stack, day) * stack.getCount();
        }
        int money = (int) (numerator / EconomicNews.PRICE_UNIT) + weighedMoney;
        if (money <= 0) {
            return 0;
        }
// 先照着「卖掉产物 + 放进纸币」在副本上算一遍，塞不下就整单取消
        List<ItemStack> simulate = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.inventory.getItem(i);
            simulate.add(isSold(stack, prices, day) ? ItemStack.EMPTY : stack.copy());
        }
        if (!insertMoney(simulate, money)) {
            return 0;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.inventory.setItem(i, simulate.get(i));
        }
        return money;
    }

// 这一格的东西会不会被卖掉
    private static boolean isSold(ItemStack stack, Map<Item, Integer> prices, long day) {
        if (stack.isEmpty()) {
            return false;
        }
        return prices.containsKey(stack.getItem()) || weighedEachPrice(stack, day) > 0;
    }

// 按重量收购的单件价；不是按重量卖的东西返回 0
    private static int weighedEachPrice(ItemStack stack, long day) {
        if (stack.getItem() instanceof PoultryCatchingNetItem) {
            return WeightPriceData.sackPrice(stack, day);
        }
        return WeightPriceData.fishPrice(stack, day);
    }

// 把 money 元按面额从大到小换成纸币塞进 slots；塞不下返回 false
    private static boolean insertMoney(List<ItemStack> slots, int money) {
        for (int i = 0; i < MONEY_VALUES.length; i++) {
            int count = money / MONEY_VALUES[i];
            money -= count * MONEY_VALUES[i];
            while (count > 0) {
                int size = Math.min(count, 64);
                if (!insert(slots, new ItemStack(MONEY_ITEMS.get(i).get(), size))) {
                    return false;
                }
                count -= size;
            }
        }
        return true;
    }

// 先并入同类未满的格，再找空格；都放不下返回 false
    private static boolean insert(List<ItemStack> slots, ItemStack stack) {
        for (ItemStack slot : slots) {
            if (slot.isEmpty() || !ItemStack.isSameItemSameTags(slot, stack)) {
                continue;
            }
            int room = slot.getMaxStackSize() - slot.getCount();
            if (room <= 0) {
                continue;
            }
            int move = Math.min(room, stack.getCount());
            slot.grow(move);
            stack.shrink(move);
            if (stack.isEmpty()) {
                return true;
            }
        }
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).isEmpty()) {
                slots.set(i, stack.copy());
                return true;
            }
        }
        return false;
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INVENTORY_TAG, InventoryNbt.save(this.inventory));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        InventoryNbt.load(this.inventory, tag, INVENTORY_TAG);
    }

// 界面

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.flavor_immersed_daily_argo.purchasing_box");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PurchasingBoxMenu(containerId, playerInventory, this);
    }
}
