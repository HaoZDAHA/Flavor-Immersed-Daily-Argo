package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 状态效果注册
public final class ModMobEffects {

    private ModMobEffects() {
    }

    public static final DeferredRegister<MobEffect> REGISTRY =
            DeferredRegister.create(Registries.MOB_EFFECT, flavor_immersed_daily_argo.MODID);

// 产卵：水产生物被水产饲料喂食后获得的 5 分钟 buff，
// 到期时在原地生成一只同类生物（见 herding/AquaticSpawningHandler）。
    public static final DeferredHolder<MobEffect, MobEffect> SPAWNING =
            REGISTRY.register("spawning", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x5BD1FF) {
            });

// 妊娠：被「筒装生物遗传液」扎中的家养动物获得的 8 分钟 buff，
// 到期时消耗掉四条状态栏、在原地生下 3~5 只遗传液里记着的那种幼崽
// （见 herding/PregnancyHandler）。
    public static final DeferredHolder<MobEffect, MobEffect> PREGNANT =
            REGISTRY.register("pregnant", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFF9BD1) {
            });

// 蝇虫叮咬：动物长期泡在粪便、尸体堆里招来的虫子，沾上就是永久的
// （MobEffectInstance 的无限时长），只能靠体外驱虫药清掉。
//
// 拥有期间清洁度掉得飞快，而且再洗也回不到 5 以上
// （见 herding/AnimalStatsManager）。
    public static final DeferredHolder<MobEffect, MobEffect> FLY_BITE =
            REGISTRY.register("fly_bite", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x6B7A29) {
            });

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
