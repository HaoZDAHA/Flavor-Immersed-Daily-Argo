package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModAttachments;
import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.all.ModMobEffects;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.SyringeItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

// 针管的两步操作：扎家畜抽遗传液、把遗传液扎进状态正好的家畜让它受孕。
//
// 两步都走「攻击」（左键）而不是右键 —— 因为扎针本来就是一戳，用攻击最顺手。
// 这一下不算伤害，事件直接取消，动物不会掉血。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class SyringeHandler {

// 妊娠时长：8 分钟
    public static final int PREGNANCY_TICKS = 8 * 60 * 20;

// 「状态都不错」的判定线：饱食、口渴、清洁、心情四项都要到这条线以上才怀得上
    private static final int CONDITION_LINE = 7;

    private SyringeHandler() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.SYRINGE.get())) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity animal) || !AnimalStatsManager.isTracked(animal)) {
            return;
        }
// 扎针不算攻击：取消掉，动物不掉血也不被打退
        event.setCanceled(true);
        if (player.level().isClientSide) {
            return;
        }
        if (SyringeItem.hasDna(stack)) {
            inject(player, animal, stack);
        } else {
            draw(player, animal, stack);
        }
    }

// 抽血：把被扎动物的种类封进针管
    private static void draw(Player player, LivingEntity animal, ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        ModDataComponents.SYRINGE_DNA.get().set(stack, id.toString());

        player.level().playSound(null, animal.blockPosition(),
                SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIT,
                    animal.getX(), animal.getY() + animal.getBbHeight() * 0.5D, animal.getZ(),
                    8, 0.3D, 0.3D, 0.3D, 0.05D);
        }
    }

// 注射：状态正好的成年家畜怀上遗传液里那种生物的崽。
//
// 受孕成功就把遗传液用掉，针管退回普通针管；到期产崽在 PregnancyHandler。
    private static void inject(Player player, LivingEntity animal, ItemStack stack) {
        if (!(animal instanceof AgeableMob ageable) || ageable.isBaby()) {
            return;
        }
// 已经怀着就别再扎一针
        if (animal.hasEffect(ModMobEffects.PREGNANT.get())) {
            return;
        }
        AnimalStats stats = AnimalStatsManager.getStats(animal);
        if (stats == null || !isConditionGood(stats)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.flavor_immersed_daily_argo.syringe.not_healthy"), true);
            }
            return;
        }

        String dna = ModDataComponents.SYRINGE_DNA.get().get(stack);
        ModAttachments.PREGNANCY_DNA.get().set(animal, dna == null ? "" : dna);
        animal.addEffect(new MobEffectInstance(ModMobEffects.PREGNANT.get(), PREGNANCY_TICKS, 0, false, true));
// 遗传液用掉了，手里变回一支空针管
        ModDataComponents.SYRINGE_DNA.get().remove(stack);

        player.level().playSound(null, animal.blockPosition(),
                SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HEART,
                    animal.getX(), animal.getY() + animal.getBbHeight() * 0.6D, animal.getZ(),
                    6, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

// 四项状态是不是都到了判定线以上
    private static boolean isConditionGood(AnimalStats stats) {
        return stats.satiety() >= CONDITION_LINE
                && stats.thirst() >= CONDITION_LINE
                && stats.cleanliness() >= CONDITION_LINE
                && stats.mood() >= CONDITION_LINE;
    }
}
