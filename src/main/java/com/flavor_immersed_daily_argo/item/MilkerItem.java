package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.herding.AnimalStats;
import com.flavor_immersed_daily_argo.herding.AnimalStatsManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// 挤奶器：右键成年牛挤出 2~5 瓶牛奶，每次挤奶消耗这头动物 1 点心情值。
//
// 牛沿用 flavor_immersed_daily 模组提供的牛奶瓶，这里按注册名在运行时查找，
// 不产生编译期依赖；该模组不在时，对牛右键仍然放行给别的交互。
//
// 山羊 → 新鲜羊奶，骆驼 → 新鲜骆驼奶，野生狼（未驯服）→ 野生狗奶；
// 取完狼的奶会把它暂时激怒。
public class MilkerItem extends TooltipItem {

// 每次挤出的瓶数范围（含两端）
    private static final int MIN_BOTTLES = 2;
    private static final int MAX_BOTTLES = 5;
// 每次挤奶消耗的心情值
    private static final int MOOD_COST = 1;
// 牛奶瓶的注册名（来自 flavor_immersed_daily 模组）
    private static final ResourceLocation MILK_BOTTLE_ID =
            ResourceLocation.fromNamespaceAndPath("flavor_immersed_daily", "milkbottle");

    public MilkerItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target.isBaby()) {
            return InteractionResult.PASS;
        }
        if (target instanceof Cow cow) {
// 找不到牛奶瓶说明没装 flavor_immersed_daily，挤奶器无从下手
            Item milkBottle = BuiltInRegistries.ITEM.getOptional(MILK_BOTTLE_ID).orElse(null);
            if (milkBottle == null) {
                return InteractionResult.PASS;
            }
            if (!player.level().isClientSide) {
                milk(cow, player, milkBottle);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        Item milk = milkFor(target);
        if (milk == null) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide) {
            milk(target, player, milk);
            if (target instanceof Wolf wolf) {
// 取完野生狼的奶，狼会记恨一阵子
                wolf.setPersistentAngerTarget(player.getUUID());
                wolf.startPersistentAngerTimer();
                wolf.setTarget(player);
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

// 这只动物能挤什么奶；不能挤返回 null
    @Nullable
    private static Item milkFor(LivingEntity target) {
        if (target instanceof Goat) {
            return ModItems.FRESH_GOAT_MILK.get();
        }
        if (target instanceof Camel) {
            return ModItems.CAMEL_MILK.get();
        }
        if (target instanceof Wolf wolf && !wolf.isTame()) {
            return ModItems.WILD_DOG_MILK.get();
        }
        return null;
    }

    private static void milk(LivingEntity animal, Player player, Item milkItem) {
// 消耗一点心情值；这头动物还没有状态栏（没被喂过）时也就没有心情可扣
        AnimalStats stats = AnimalStatsManager.getStats(animal);
        if (stats != null) {
            AnimalStatsManager.setStats(animal, stats.withMood(stats.mood() - MOOD_COST));
        }

        ItemStack milk = new ItemStack(milkItem, Mth.nextInt(player.getRandom(), MIN_BOTTLES, MAX_BOTTLES));
        if (!player.getInventory().add(milk)) {
            player.drop(milk, false);
        }

        player.level().playSound(null, animal.blockPosition(), SoundEvents.COW_MILK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    animal.getX(), animal.getY() + animal.getBbHeight() * 0.5D, animal.getZ(),
                    5, 0.3D, 0.3D, 0.3D, 0.01D);
        }
    }
}
