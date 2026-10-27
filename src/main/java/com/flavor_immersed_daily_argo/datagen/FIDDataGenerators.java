package com.flavor_immersed_daily_argo.datagen;

import com.flavor_immersed_daily_argo.datagen.lang.ModLangs;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

// 数据生成总入口（对应 gradle 的 data run）
public final class FIDDataGenerators {

    private FIDDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

// 物品模型
        generator.addProvider(event.includeClient(), new FIDItemModelProvider(packOutput, existingFileHelper));

// 英文语言文件
        generator.addProvider(event.includeClient(), new ModLangs(packOutput, "en_us"));
    }
}
