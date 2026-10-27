package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.block.WaterwheelWaterFluid;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 流体注册
//
// 目前只有灌溉水车用的那一格“水”（WaterwheelWaterFluid）：
// 功能上是水源，视觉上什么都不画。
public final class ModFluids {

    private ModFluids() {
    }

    public static final DeferredRegister<Fluid> REGISTRY =
            DeferredRegister.create(Registries.FLUID, flavor_immersed_daily_argo.MODID);

// 灌溉水车的水
    public static final DeferredHolder<Fluid, WaterwheelWaterFluid> WATERWHEEL_WATER =
            REGISTRY.register("waterwheel_water", WaterwheelWaterFluid::new);

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
