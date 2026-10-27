package com.flavor_immersed_daily_argo.economy.shop;

// 购物车里的一行：某家店的第 entryIndex 条商品，要买 qty 次。
public record CartLine(String shopId, int entryIndex, int qty) {
}
