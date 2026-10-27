package com.flavor_immersed_daily_argo.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// 一条外汇兑换条目：花卡面账户里的钱（按「本模组货币 × 数量」折算成金额），换一份其他模组的物品。
//
// 配置与指令里的写法是 本模组货币物品,数量,其他模组物品,数量，例如默认的
// flavor_immersed_daily_argo:money_5,64,minecraft:emerald,1
// —— 64 张五元纸币折合 320，换 1 个绿宝石。
public record ExchangeEntry(Item source, int sourceCount, Item target, int targetCount) {

// 这次兑换要从卡面账户里扣掉的金额：本模组货币的面额 × 数量
    public long price() {
        int unit = MoneyItem.valueOf(new ItemStack(this.source));
        return (long) this.sourceCount * Math.max(1, unit);
    }

// 折算成配置里的一行
    public String serialize() {
        return BuiltInRegistries.ITEM.getKey(this.source) + "," + this.sourceCount + ","
                + BuiltInRegistries.ITEM.getKey(this.target) + "," + this.targetCount;
    }

// 解析配置里的一行；格式不对或物品不存在就返回 null
    public static ExchangeEntry parse(String raw) {
        if (raw == null) {
            return null;
        }
        String[] parts = raw.split(",");
        if (parts.length != 4) {
            return null;
        }
        Item source = itemOrNull(parts[0].trim());
        Item target = itemOrNull(parts[2].trim());
        if (source == null || target == null) {
            return null;
        }
        int sourceCount = parsePositive(parts[1].trim());
        int targetCount = parsePositive(parts[3].trim());
        if (sourceCount <= 0 || targetCount <= 0) {
            return null;
        }
        return new ExchangeEntry(source, sourceCount, target, targetCount);
    }

    private static Item itemOrNull(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null || !BuiltInRegistries.ITEM.containsKey(key)) {
            return null;
        }
        return BuiltInRegistries.ITEM.get(key);
    }

    private static int parsePositive(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

// 写进「打开界面」的登录包，供客户端界面读取
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(this.source));
        buf.writeVarInt(this.sourceCount);
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(this.target));
        buf.writeVarInt(this.targetCount);
    }

    public static ExchangeEntry read(FriendlyByteBuf buf) {
        ResourceLocation source = buf.readResourceLocation();
        int sourceCount = buf.readVarInt();
        ResourceLocation target = buf.readResourceLocation();
        int targetCount = buf.readVarInt();
        return new ExchangeEntry(BuiltInRegistries.ITEM.get(source), sourceCount,
                BuiltInRegistries.ITEM.get(target), targetCount);
    }
}
