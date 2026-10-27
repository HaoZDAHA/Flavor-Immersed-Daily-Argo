package com.flavor_immersed_daily_argo.economy.shop;

import java.util.List;

// 一家店铺。
//
// id 在系统店铺取「命名空间:名称」，玩家店铺取 player:<ownerName>:<序号>。
// ownerName 为空串表示系统店铺（数据包 / 配置文件商店），非空表示玩家开的店：
// 只有玩家店铺参与 balance 结算与库存扣减，全服任何人（含未打开界面者）买到的货
// 都会把货款记进店主的 balance。
public record Shop(String id, String title, String ownerName, List<ShopEntry> entries, long balance, boolean playerShop) {

// 是不是系统店铺：没有店主就是
    public boolean isSystem() {
        return this.ownerName == null || this.ownerName.isEmpty();
    }
}
