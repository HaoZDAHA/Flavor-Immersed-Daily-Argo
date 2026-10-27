package com.flavor_immersed_daily_argo.economy.product;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

// 按重量收购价目的编解码：商业电脑「行情」页要显示畜类与生鱼的价目，由服务端算好后随界面下发。
public final class WeightPriceNetwork {

    private WeightPriceNetwork() {
    }

    public static void writeAnimals(FriendlyByteBuf buf, List<WeightPriceData.AnimalPrice> animals) {
        buf.writeVarInt(animals.size());
        for (WeightPriceData.AnimalPrice price : animals) {
            buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(price.type()));
            buf.writeDouble(price.basePrice());
            buf.writeDouble(price.extraPerKg());
        }
    }

    public static List<WeightPriceData.AnimalPrice> readAnimals(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<WeightPriceData.AnimalPrice> animals = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buf.readResourceLocation();
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            double basePrice = buf.readDouble();
            double extraPerKg = buf.readDouble();
            animals.add(new WeightPriceData.AnimalPrice(type, basePrice, extraPerKg));
        }
        return animals;
    }

    public static void writeFish(FriendlyByteBuf buf, List<WeightPriceData.FishPrice> fish) {
        buf.writeVarInt(fish.size());
        for (WeightPriceData.FishPrice price : fish) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(price.item()));
            buf.writeDouble(price.perJin());
        }
    }

    public static List<WeightPriceData.FishPrice> readFish(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<WeightPriceData.FishPrice> fish = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buf.readResourceLocation();
            Item item = BuiltInRegistries.ITEM.get(id);
            double perJin = buf.readDouble();
            fish.add(new WeightPriceData.FishPrice(item, perJin));
        }
        return fish;
    }
}
