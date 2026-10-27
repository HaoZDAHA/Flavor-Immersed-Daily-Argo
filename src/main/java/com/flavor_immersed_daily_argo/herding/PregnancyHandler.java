package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModAttachments;
import com.flavor_immersed_daily_argo.all.ModMobEffects;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

// 「妊娠」buff 到期的结算：把四条状态栏掏空，并在原地生下遗传液里记着的那种幼崽。
//
// 生的是哪种不取决于母体，全看 ModAttachments#PREGNANCY_DNA —— 给猪扎了牛的遗传液，
// 它就会产下 3~5 头小牛。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class PregnancyHandler {

// 一次生几只（含两端）
    private static final int MIN_BABIES = 3;
    private static final int MAX_BABIES = 5;
// 幼崽落地时相对母体的水平散布半径
    private static final double SPREAD = 1.5D;

    private PregnancyHandler() {
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || instance.getEffect().value() != ModMobEffects.PREGNANT.get()) {
            return;
        }
        LivingEntity mother = event.getEntity();
        if (!(mother.level() instanceof ServerLevel level)) {
            return;
        }

// 生崽把四条状态栏一次掏空
        AnimalStats stats = AnimalStatsManager.getStats(mother);
        if (stats != null) {
            AnimalStatsManager.setStats(mother, stats.withSatiety(0).withThirst(0)
                    .withCleanliness(0).withMood(0));
        }

        String dna = mother.getData(ModAttachments.PREGNANCY_DNA.get());
        mother.setData(ModAttachments.PREGNANCY_DNA.get(), "");
        if (dna == null || dna.isEmpty()) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(dna)).orElse(null);
        if (type == null) {
            return;
        }

        int count = Mth.nextInt(level.random, MIN_BABIES, MAX_BABIES);
        for (int i = 0; i < count; i++) {
            Entity baby = type.create(level);
            if (baby == null) {
                continue;
            }
            if (baby instanceof AgeableMob ageable) {
                ageable.setBaby(true);
            }
// 免得这群小家伙一读档就没了
            if (baby instanceof Mob mob) {
                mob.setPersistenceRequired();
            }
            baby.moveTo(mother.getX() + Mth.nextDouble(level.random, -SPREAD, SPREAD), mother.getY(),
                    mother.getZ() + Mth.nextDouble(level.random, -SPREAD, SPREAD),
                    level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(baby);
        }
    }
}
