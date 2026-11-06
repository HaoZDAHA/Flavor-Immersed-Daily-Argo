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

// 1.20.1 没有 renderFluid 钩子，没法直接跳过绘制，
// 所以用两张全透明的自制贴图让水柱「画了但不可见」。
//
// 这里不能用原版水贴图再配 alpha=0 的染色：原版 LiquidBlockRenderer 给顶点写死的 alpha 是 1，
// 染色里的 alpha 会被忽略，水柱照样可见。贴图本身全透明才是稳的。
//
// 另外 1.20.1 Forge 的 IClientFluidTypeExtensions 默认实现把 still / flowing / overlay
// 全部返回 null（不会回落到原版水），漏掉任何一个都会让 ForgeHooksClient.getFluidSprites
// 拿 null 去查图集，崩「Tesselating liquid in world」。
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                private static final ResourceLocation STILL =
                        new ResourceLocation(flavor_immersed_daily_argo.MODID, "block/waterwheel_water_still");
                private static final ResourceLocation FLOW =
                        new ResourceLocation(flavor_immersed_daily_argo.MODID, "block/waterwheel_water_flow");

                @Override
                public ResourceLocation getStillTexture() {
                    return STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return FLOW;
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return STILL;
                }

                @Override
                public int getTintColor() {
                    return 0xFFFFFFFF;
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
