package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Consumer;

// 流体类型注册
//
// 目前只有灌溉水车用的“水”：canHydrate(true) 让耕地、甘蔗、珊瑚等原版判定
// 把水车三格当成水源；同时因为它是独立类型，客户端可以单独注册扩展把水“画都不画”。
public final class ModFluidTypes {

    private ModFluidTypes() {
    }

    private static final class WaterwheelWaterType extends FluidType {

        WaterwheelWaterType(Properties properties) {
            super(properties);
        }

// 1.20.1 没有 renderFluid 钩子，改用「全透明槽色」：水依然在，但画出来看不见
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public int getTintColor() {
                    return 0x00FFFFFF;
                }
            });
        }
    }

    public static final DeferredRegister<FluidType> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, flavor_immersed_daily_argo.MODID);

// 灌溉水车的水：可灌溉、不渲染
    public static final DeferredHolder<FluidType, FluidType> WATERWHEEL_WATER_TYPE =
            REGISTRY.register("waterwheel_water", () -> new WaterwheelWaterType(FluidType.Properties.create()
                    .canHydrate(true)
                    .density(1000)
                    .viscosity(1000)));

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
