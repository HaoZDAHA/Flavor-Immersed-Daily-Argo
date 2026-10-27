package com.flavor_immersed_daily_argo.recipe;

import com.flavor_immersed_daily_argo.all.ModItems;
import net.minecraft.world.item.Item;

import java.util.List;

// 捕虫网的战利品：用捕虫网破坏草时，每种昆虫各自按 #chance() 独立判定一次。
//
// 这组数据同时供 JEI 与 EMI 的「捕虫」分类读取，保证两边的展示与实际掉落一致。
public record InsectNetDrop(Item item, float chance) {

// 每次破坏一丛草，单种昆虫的掉落概率
    public static final float DROP_CHANCE = 0.10F;

// 捕虫网能捉到的四种昆虫
    public static List<InsectNetDrop> drops() {
        return List.of(
                new InsectNetDrop(ModItems.BEE.get(), DROP_CHANCE),
                new InsectNetDrop(ModItems.CICADA.get(), DROP_CHANCE),
                new InsectNetDrop(ModItems.LOCUST.get(), DROP_CHANCE),
                new InsectNetDrop(ModItems.SILKWORM.get(), DROP_CHANCE)
        );
    }
}
