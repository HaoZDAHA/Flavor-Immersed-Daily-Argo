package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModMobEffects;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

// 水产饲料带来的「产卵」buff 到期结算：在原地生成一只同类生物。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class AquaticSpawningHandler {

    private AquaticSpawningHandler() {
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || instance.getEffect().value() != ModMobEffects.SPAWNING.get()) {
            return;
        }
        LivingEntity parent = event.getEntity();
        if (!(parent.level() instanceof ServerLevel level)) {
            return;
        }
        Entity spawned = parent.getType().create(level);
        if (spawned == null) {
            return;
        }
        spawned.moveTo(parent.getX(), parent.getY(), parent.getZ(), level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(spawned);
    }
}
