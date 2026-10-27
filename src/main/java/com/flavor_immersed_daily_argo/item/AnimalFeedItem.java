package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.herding.AnimalStatsManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// 繁殖用饲料：右键喂给名单内的动物，让它进入繁殖状态；喂给幼崽则催熟生长。
//
// 名单外的生物一律放行不处理；已经在繁殖状态的成体不吃。
// 需要先驯服的（猫、狗、马、驴、骆驼、羊驼）未驯服时也不吃。
public class AnimalFeedItem extends Item {

// 草谷饲料：猪、牛、绵羊、山羊、马、驴、兔子、骆驼、羊驼、竹鼠、牛蛙
    public static final List<EntityType<?>> GRASS_TARGETS = List.of(
            EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.GOAT,
            EntityType.HORSE, EntityType.DONKEY, EntityType.RABBIT, EntityType.CAMEL, EntityType.LLAMA,
            ModEntities.BAMBOORAT.get(), ModEntities.BULLFROG.get());

// 稻谷饲料：鸡、鸭、鹅、鹌鹑、鸽子、火鸡
    public static final List<EntityType<?>> SEED_TARGETS = List.of(
            EntityType.CHICKEN,
            ModEntities.DUCK.get(), ModEntities.GOOSE.get(), ModEntities.QUAIL.get(),
            ModEntities.PIGEON.get(), ModEntities.TURKEY.get());

// 鲜肉饲料：猫、狗
    public static final List<EntityType<?>> MEAT_TARGETS = List.of(
            EntityType.CAT, EntityType.WOLF);

    private final List<EntityType<?>> targets;

    public AnimalFeedItem(Item.Properties properties, List<EntityType<?>> targets) {
        super(properties);
        this.targets = targets;
    }

// 这份饲料认哪些动物（吸引、喂食、催熟都用它）
    public List<EntityType<?>> getTargets() {
        return this.targets;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!this.targets.contains(target.getType()) || !(target instanceof Animal animal)) {
            return InteractionResult.PASS;
        }
        if (!animal.isAlive()) {
            return InteractionResult.PASS;
        }
// 幼崽：一口饲料催熟一段生长期
        if (feedBaby(animal, stack, player)) {
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        if (animal.isInLove()) {
            return InteractionResult.PASS;
        }
// 猫狗、马驴骆驼羊驼都要先驯服才能繁殖
        if (animal instanceof TamableAnimal tamable && !tamable.isTame()) {
            return InteractionResult.PASS;
        }
        if (animal instanceof AbstractHorse horse && !horse.isTamed()) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide) {
            animal.setInLove(player);
            stack.consume(1, player);
// 喂食同时让动物获得（或补充）四项状态栏
            AnimalStatsManager.onFed(animal);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

// 给幼崽喂一口饲料，推进剩余生长期；是幼崽就返回 true（服务端同时扣掉一个饲料）。
    static boolean feedBaby(Animal animal, ItemStack stack, Player player) {
        if (!animal.isBaby()) {
            return false;
        }
        if (!player.level().isClientSide) {
            growUpOneStep(animal);
            stack.consume(1, player);
        }
        return true;
    }

// 推进幼崽剩余生长期的 10%（和原版小麦喂幼崽同一手感），由调用方负责扣饲料
    static void growUpOneStep(Animal animal) {
        int remainingSeconds = Math.max(1, -animal.getAge() / 20);
        animal.ageUp(Math.max(1, Math.round(remainingSeconds * 0.1F)), true);
    }
}
