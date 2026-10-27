package com.flavor_immersed_daily_argo.economy.product;

import net.minecraft.world.item.Item;

// 一样经济产物（农业作物 / 畜牧产物 / 水产）。
//
// price 是它的基准收购价（元 / 每组 64 个），新闻里的每日涨跌幅度以它为基准算。
public record EconomicProduct(Item item, int price) {
}
