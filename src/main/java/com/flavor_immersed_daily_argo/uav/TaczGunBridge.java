package com.flavor_immersed_daily_argo.uav;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

// TaCZ 枪械的实际开火入口。
//
// 只有在 TaCZ 已加载时才会被 TaczGunCompat 调用；这里全部走反射，
// 因此本模组编译期不需要 TaCZ 的任何类。TaCZ 的接口若与预期不符，
// 一律当作「这一枪没打出去」处理，不影响无人机继续飞。
public final class TaczGunBridge {

    private static final String OPERATOR_CLASS = "com.tacz.guns.api.entity.IGunOperator";

    private TaczGunBridge() {
    }

    public static boolean fire(FakePlayer shooter, ItemStack gun, Entity soundSource) {
        try {
            Class<?> operatorClass = Class.forName(OPERATOR_CLASS);
            Object operator = operatorClass.getMethod("fromLivingEntity",
                    net.minecraft.world.entity.LivingEntity.class).invoke(null, shooter);
            if (operator == null) {
                return false;
            }
            operatorClass.getMethod("shoot").invoke(operator);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
