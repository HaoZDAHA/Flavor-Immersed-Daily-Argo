package com.flavor_immersed_daily_argo.economy.shop;

import com.flavor_immersed_daily_argo.economy.product.EconomicNews;
import com.flavor_immersed_daily_argo.economy.product.WeightPriceData;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// 客户端侧的市场数据缓存。
//
// SHOPS 由服务端随登录包 / 同步包全量下发后写入；NEWS 是服务端算好的「今日经济产物
// 涨跌」；CART 是购物车，纯客户端状态，界面关闭也不清空，方便玩家接着逛。
public final class ShopClientData {

    private ShopClientData() {
    }

// 全量店铺列表（服务端下发）
    private static List<Shop> SHOPS = List.of();

// 今日经济产物涨跌（服务端下发，已按涨跌从高到低排好）
    private static List<EconomicNews> NEWS = List.of();

// 按重量收购的畜类价目（服务端下发）
    private static List<WeightPriceData.AnimalPrice> ANIMAL_PRICES = List.of();

// 按重量收购的生鱼价目（服务端下发）
    private static List<WeightPriceData.FishPrice> FISH_PRICES = List.of();

// 购物车（界面关闭不清空）
    private static final List<CartLine> CART = new ArrayList<>();

    public static List<Shop> shops() {
        return SHOPS;
    }

    public static void setShops(List<Shop> shops) {
        SHOPS = shops == null ? List.of() : List.copyOf(shops);
    }

    public static List<EconomicNews> news() {
        return NEWS;
    }

    public static void setNews(List<EconomicNews> news) {
        NEWS = news == null ? List.of() : List.copyOf(news);
    }

    public static List<WeightPriceData.AnimalPrice> animalPrices() {
        return ANIMAL_PRICES;
    }

    public static void setAnimalPrices(List<WeightPriceData.AnimalPrice> prices) {
        ANIMAL_PRICES = prices == null ? List.of() : List.copyOf(prices);
    }

    public static List<WeightPriceData.FishPrice> fishPrices() {
        return FISH_PRICES;
    }

    public static void setFishPrices(List<WeightPriceData.FishPrice> prices) {
        FISH_PRICES = prices == null ? List.of() : List.copyOf(prices);
    }

    public static Shop findShop(String id) {
        for (Shop shop : SHOPS) {
            if (shop.id().equals(id)) {
                return shop;
            }
        }
        return null;
    }

    public static List<CartLine> cart() {
        return CART;
    }

// 某条商品当前在购物车里的次数；没有就是 0
    public static int qty(String shopId, int entryIndex) {
        for (CartLine line : CART) {
            if (line.shopId().equals(shopId) && line.entryIndex() == entryIndex) {
                return line.qty();
            }
        }
        return 0;
    }

// 加一次（购物车里的数量 +1）
    public static void addLine(String shopId, int entryIndex) {
        setQty(shopId, entryIndex, qty(shopId, entryIndex) + 1);
    }

// 减一次；减到 0 就从购物车移除
    public static void removeLine(String shopId, int entryIndex) {
        setQty(shopId, entryIndex, qty(shopId, entryIndex) - 1);
    }

    private static void setQty(String shopId, int entryIndex, int qty) {
        for (int i = 0; i < CART.size(); i++) {
            CartLine line = CART.get(i);
            if (line.shopId().equals(shopId) && line.entryIndex() == entryIndex) {
                if (qty <= 0) {
                    CART.remove(i);
                } else {
                    CART.set(i, new CartLine(shopId, entryIndex, qty));
                }
                return;
            }
        }
        if (qty > 0) {
            CART.add(new CartLine(shopId, entryIndex, qty));
        }
    }

// 清空购物车
    public static void clearCart() {
        CART.clear();
    }

// 结账 / 操作的界面反馈

// 最近一条操作反馈（结账成功 / 各种失败原因），到点自动消失
    private static Component feedback = Component.empty();
    private static boolean feedbackSuccess = false;
    private static long feedbackUntil = 0L;

// 服务端回执：结账成功就顺手把购物车清空，并把这条消息显示在界面上。
//
// 界面上不能用 actionBar：容器界面打开时原版根本不画 HUD，消息会在关闭界面之后才一闪而过，
// 所以这里由 MarketScreen 自己把反馈画在窗口里。
    public static void applyResult(boolean success, Component message) {
        feedback = message == null ? Component.empty() : message;
        feedbackSuccess = success;
        feedbackUntil = System.currentTimeMillis() + 3500L;
        if (success) {
            clearCart();
        }
    }

// 当前要显示的反馈；没有就返回空组件
    public static Component feedback() {
        return System.currentTimeMillis() < feedbackUntil ? feedback : Component.empty();
    }

// 当前反馈是否是成功提示（决定文字颜色）
    public static boolean feedbackSuccess() {
        return feedbackSuccess;
    }
}
