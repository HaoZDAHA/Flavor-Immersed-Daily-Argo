package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModMenus;
import com.flavor_immersed_daily_argo.economy.product.EconomicProductNetwork;
import com.flavor_immersed_daily_argo.economy.product.WeightPriceNetwork;
import com.flavor_immersed_daily_argo.economy.shop.ShopClientData;
import com.flavor_immersed_daily_argo.economy.shop.ShopNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

// 商业电脑界面。
//
// 不放任何槽位：买东西在「结账」时从背包里挑银行卡；上架 / 补货直接用手上拿的物品，
// 所以界面上不需要卡槽、操作槽，也不用画玩家背包，窗口可以做得又宽又矮。
//
// 方块负责把全量店铺列表与「今日经济产物涨跌」塞进登录包，客户端构造时读进 ShopClientData。
public class MarketMenu extends AbstractContainerMenu {

// 又宽又矮的窗口：340 × 200，省掉底部的背包格
    public static final int IMAGE_WIDTH = 340;
    public static final int IMAGE_HEIGHT = 200;

// 客户端：从登录包读全量店铺列表、今日经济产物新闻与按重量收购的价目
    public MarketMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv);
        ShopClientData.setShops(ShopNetwork.readShops(extraData));
        ShopClientData.setNews(EconomicProductNetwork.readNews(extraData));
        ShopClientData.setAnimalPrices(WeightPriceNetwork.readAnimals(extraData));
        ShopClientData.setFishPrices(WeightPriceNetwork.readFish(extraData));
    }

// 服务端：无参版本开界面用
    public MarketMenu(int containerId, Inventory playerInv) {
        super(ModMenus.MARKET_MENU.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
