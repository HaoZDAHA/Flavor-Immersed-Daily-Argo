package com.flavor_immersed_daily_argo.economy.shop;

import net.minecraft.world.item.Item;

// 店铺里的一条商品。
//
// count 是「一次购买到手几件」（数据包可以写成一组 64），price 是「买一次花多少元」，
// stock 是还剩几次可买：数据包 / 配置文件驱动的系统店铺是 -1（无限），玩家店铺是
// >= 0 的有限库存，卖完即 0。
public record ShopEntry(Item item, int count, int price, int stock) {

// 是不是无限库存（系统店铺）：stock < 0 即无限
    public boolean infinite() {
        return this.stock < 0;
    }
}
