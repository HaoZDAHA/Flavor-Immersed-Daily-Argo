package com.flavor_immersed_daily_argo.integration.emi;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

// 订单实体按 R 时打开 EMI 的配方页。
//
// EMI 不在编译依赖里（本项目只用 JEI 做编译期兼容），这里用反射调用 EMI 的公开 API，
// 装没装 EMI 都不会影响主流程；调用失败就返回 false。
public final class FIDEmiRecipeLookup {

    private FIDEmiRecipeLookup() {
    }

    public static boolean showRecipes(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        try {
            Class<?> emiStackClass = Class.forName("dev.emi.emi.api.stack.EmiStack");
            Object emiStack = emiStackClass.getMethod("of", ItemStack.class).invoke(null, stack);
            Class<?> emiApi = Class.forName("dev.emi.emi.api.EmiApi");
            for (Method method : emiApi.getMethods()) {
                if (!"displayRecipes".equals(method.getName()) || method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> param = method.getParameterTypes()[0];
// EMI 的签名是 displayRecipes(EmiStack) 或 displayRecipes(EmiStack...)，两种都兼容
                if (param == emiStackClass) {
                    method.invoke(null, emiStack);
                    return true;
                }
                if (param.isArray() && param.getComponentType() == emiStackClass) {
                    Object array = java.lang.reflect.Array.newInstance(emiStackClass, 1);
                    java.lang.reflect.Array.set(array, 0, emiStack);
                    method.invoke(null, array);
                    return true;
                }
            }
        } catch (Throwable ignored) {
// EMI 未安装或 API 变动：当作没有配方查看器处理
        }
        return false;
    }
}
