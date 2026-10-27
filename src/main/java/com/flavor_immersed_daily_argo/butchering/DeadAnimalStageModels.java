package com.flavor_immersed_daily_argo.butchering;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ModelEvent;

// 死动物屠宰阶段模型的侧载注册。
//
// 这些模型（block/dead_<动物>_<阶段> 与 block/bighook/<禽类>_<阶段>）
// 只由父模组的大挂钩渲染器（BighookRenderer.renderCustomModel）按 id 通过
// ModelManager 取用，没有挂在任何物品或方块状态上，正常烘焙流程不会加载它们，
// 因此在此按「侧载模型」显式注册（与 FishingRodModelOverrides 用的是同一机制）。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DeadAnimalStageModels {

// 死动物种类，与 butchering JSON 里的模型 id 一致
    private static final String[] ANIMALS = {"dog", "donkey", "camel", "rabbit", "llama"};

// 每种死动物提供的阶段模型数量（放血 / 剥皮 / 掏空 / 切割）
    private static final int STAGE_MODELS = 4;

// 禽类屠宰阶段模型，路径为 block/bighook/<鸟>_<阶段>。
// 阶段号沿用原素材，并不连续：鸭 / 鹅含 1、2、5、6（悬挂 / 放血 / 拔毛 / 掏空），
// 鸽 / 鹌鹑 / 火鸡只含 1、2。
    private static final String[] BIRD_STAGE_MODELS = {
            "duck_1", "duck_2", "duck_5", "duck_6",
            "goose_1", "goose_2", "goose_5", "goose_6",
            "pigeon_1", "pigeon_2",
            "quail_1", "quail_2",
            "turkey_1", "turkey_2",
    };

    private DeadAnimalStageModels() {
    }

    @SubscribeEvent
    static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (String animal : ANIMALS) {
            for (int stage = 1; stage <= STAGE_MODELS; stage++) {
                event.register(model("block/dead_" + animal + "_" + stage));
            }
        }
        for (String bird : BIRD_STAGE_MODELS) {
            event.register(model("block/bighook/" + bird));
        }
    }

    private static ResourceLocation model(String path) {
        return new ResourceLocation(flavor_immersed_daily_argo.MODID, path);
    }
}
