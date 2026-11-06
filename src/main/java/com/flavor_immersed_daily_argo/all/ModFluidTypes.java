package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.resources.ResourceLocation;
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

// 水车的水在世界里完全不绘制：WaterwheelWaterFluid#isEmpty() 让它报告成“空”，
// 区块构建时压根不会走流体绘制那条路，所以也不存在“用哪张贴图”的问题。
//
// 这里的客户端扩展只是兜底：万一别的模组把这个流体拿去画管道/水槽，
// 至少能拿到贴图，不会因为 still / flowing / overlay 为 null 崩在查图集那一步
// （1.20.1 Forge 的默认实现就是返回 null，不回落到原版水）。
// 同时染色 alpha 置 0，就算真被画出来也是全透明。
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return new ResourceLocation("block/water_still");
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return new ResourceLocation("block/water_flow");
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return new ResourceLocation("block/water_overlay");
                }

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
