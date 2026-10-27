package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModMobEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

// 水产饲料：右键使用，给玩家周围一定范围内所有名单内的水产生物附加「产卵」buff。
//
// 与繁殖饲料不同，它不对准单个目标而是范围生效；buff 到期后由
// com.flavor_immersed_daily_argo.herding.AquaticSpawningHandler 在原地生成一只同类生物。
//
// 已经在产卵 / 妊娠状态的水产不会再被计入，一份饲料只喂真正需要的那些，
// 避免对着同一群反复右键白白消耗。
public class AquaticFeedItem extends Item {

// 生效半径：玩家周围 30 格
    private static final double RADIUS = 30.0D;

// 产卵 buff 时长：5 分钟
    public static final int BUFF_DURATION = 6000;

// 水产饲料名单：17 种鱼 + 鱿鱼 + 5 种甲壳 / 龟鳖
//
// 实体类型注册得比物品晚，物品注册期间不能提前 .get()，改成第一次用到时再取
    private static List<EntityType<?>> aquaticTargets;

    public static List<EntityType<?>> aquaticTargets() {
        if (aquaticTargets == null) {
            aquaticTargets = List.of(
                    ModEntities.GRASS_CARP.get(), ModEntities.TENCH.get(), ModEntities.MANDARIN_FISH.get(),
                    ModEntities.BLACK_FISH.get(), ModEntities.RECTUM.get(), ModEntities.CRUCIAN_CARP.get(),
                    ModEntities.CHINESE_CARP.get(), ModEntities.SILVER_CARP.get(), ModEntities.BASS.get(),
                    ModEntities.TILAPIA.get(), ModEntities.LOACH.get(), ModEntities.CATFISH.get(),
                    ModEntities.GOBY.get(), ModEntities.REDFIN.get(), ModEntities.BLACK_CARP.get(),
                    ModEntities.PLECO.get(), ModEntities.BIGHEAD_CARP.get(), EntityType.SQUID,
                    ModEntities.HAIRY_CRAB.get(), ModEntities.PRAWN.get(), ModEntities.TURTLE.get(),
                    ModEntities.MUD_CRAB.get(), ModEntities.SWIMMING_CRAB.get());
        }
        return aquaticTargets;
    }

    private final java.util.function.Supplier<List<EntityType<?>>> targets;

    public AquaticFeedItem(Item.Properties properties, java.util.function.Supplier<List<EntityType<?>>> targets) {
        super(properties);
        this.targets = targets;
    }

// 这份饲料认哪些动物（吸引、催熟都用它）
    public List<EntityType<?>> getTargets() {
        return this.targets.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        AABB box = player.getBoundingBox().inflate(RADIUS);
        List<LivingEntity> fed = level.getEntitiesOfClass(LivingEntity.class, box, this::canFeed);
        if (fed.isEmpty()) {
// 附近没有需要喂的（或都已经在产卵），不消耗饲料
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            for (LivingEntity entity : fed) {
// 幼崽（比如小海龟）直接催熟；成体才挂产卵 buff
                if (entity instanceof Animal animal && animal.isBaby()) {
                    AnimalFeedItem.growUpOneStep(animal);
                } else {
                    entity.addEffect(new MobEffectInstance(ModMobEffects.SPAWNING.get(), BUFF_DURATION, 0, false, true));
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

// 名单内、且还没在产卵 / 妊娠状态的才吃；幼崽一律可以催熟
    private boolean canFeed(LivingEntity entity) {
        if (!this.getTargets().contains(entity.getType())) {
            return false;
        }
        if (entity instanceof Animal animal && animal.isBaby()) {
            return true;
        }
        return !entity.hasEffect(ModMobEffects.SPAWNING.get()) && !entity.hasEffect(ModMobEffects.PREGNANT.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.flavor_immersed_daily_argo.feed_aquatic"));
        tooltipComponents.add(Component.translatable("tooltip.flavor_immersed_daily_argo.feed_aquatic.spawning"));
    }
}
