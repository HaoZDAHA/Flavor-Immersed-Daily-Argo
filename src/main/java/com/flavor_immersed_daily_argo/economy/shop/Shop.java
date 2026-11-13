package com.flavor_immersed_daily_argo.economy.shop;

import java.util.List;
import net.minecraft.network.chat.Component;

// 一家店铺。
//
// id 在系统店铺取「命名空间:名称」，玩家店铺取全服唯一的数字编号（1、2、3…）。
// ownerName 为空串表示系统店铺（数据包 / 配置文件商店），非空表示玩家开的店：
// 只有玩家店铺参与 balance 结算与库存扣减，全服任何人（含未打开界面者）买到的货
// 都会把货款记进店主的 balance。
public record Shop(String id, String title, String ownerName, List<ShopEntry> entries, long balance, boolean playerShop) {

    public Component titleComponent() {
        if (this.playerShop && (this.title == null || this.title.isBlank()
                || this.title.equals(this.ownerName + "的小店"))) {
            return Component.translatable("gui.flavor_immersed_daily_argo.market.default_shop_name", this.ownerName);
        }
        return this.playerShop ? Component.literal(this.title) : Component.translatable(this.title);
    }

// 是不是系统店铺：没有店主就是
    public boolean isSystem() {
        return this.ownerName == null || this.ownerName.isEmpty();
    }

// 在全量店铺列表里的编号：系统店铺按顺序 1..k，玩家店铺接在系统店铺之后，
// 用内部数字 id 偏移，删店不会让其他店的编号前移。
    public int numberIn(List<Shop> all) {
        int systemCount = 0;
        int position = 0;
        for (Shop candidate : all) {
            if (!candidate.isSystem()) {
                continue;
            }
            systemCount++;
            if (candidate.id.equals(this.id)) {
                position = systemCount;
            }
        }
        if (this.isSystem()) {
            return position;
        }
        try {
            return systemCount + Integer.parseInt(this.id);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
