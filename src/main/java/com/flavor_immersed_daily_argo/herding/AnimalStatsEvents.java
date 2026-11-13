package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.AnimalFeedItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

// 状态栏服务端规则。普通食物需要交互后确认确实消耗或产生喂食效果，不能仅右键就授予资格。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AnimalStatsEvents {
    private record PendingFeed(Animal animal, ItemStack stack, int count, int age, float health,
                               boolean inLove, boolean tame, boolean tamingOnly) {
        boolean consumed() {
            boolean newlyTamed = !tame && animal instanceof TamableAnimal tamable && tamable.isTame();
            // Bones are a taming interaction, not ordinary food: a failed attempt never qualifies,
            // even if a survival player spent a bone. Creative success has no stack reduction.
            if (tamingOnly) {
                return newlyTamed;
            }
            // A normal baby ages by one tick on its own; that must not count as player feeding.
            return newlyTamed || stack.getCount() < count || (age < 0 && animal.getAge() - age > 1)
                    || animal.getHealth() > health || (!inLove && animal.isInLove());
        }
    }

    private static final List<PendingFeed> PENDING_FEEDS = new ArrayList<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.level().isClientSide) {
            return;
        }
        if (AnimalStatsManager.isTracked(living)) {
            AnimalStatsManager.serverTick(living);
        }
    }

    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (event.getChild() != null) {
            AnimalStatsManager.inheritToChild(event.getChild(), event.getParentA(), event.getParentB());
        }
    }

    // Native name tags, buckets, saddles, empty-hand interactions etc. never enter this food-only path.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().level().isClientSide || event.getEntity().isSpectator()
                || !(event.getTarget() instanceof Animal animal) || !AnimalStatsManager.isTracked(animal)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() instanceof AnimalFeedItem || !animal.isAlive()) {
            return;
        }
        boolean boneTaming = animal instanceof Wolf wolf && !wolf.isTame() && !wolf.isAngry()
                && stack.is(Items.BONE);
        if (!animal.isFood(stack) && !boneTaming) {
            return;
        }
        boolean untamed = animal instanceof TamableAnimal tamable && !tamable.isTame();
        boolean mayEat = boneTaming || untamed || animal.isBaby() || (!animal.isInLove() && animal.getAge() == 0)
                || animal.getHealth() < animal.getMaxHealth();
        if (!mayEat) {
            return;
        }
        boolean pending = PENDING_FEEDS.stream().anyMatch(feed -> feed.animal() == animal);
        if (!AnimalStatsManager.canFeed(animal) || pending) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        PENDING_FEEDS.add(new PendingFeed(animal, stack, stack.getCount(), animal.getAge(),
                animal.getHealth(), animal.isInLove(), animal instanceof TamableAnimal tamable && tamable.isTame(), boneTaming));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING_FEEDS.clear();
    }

    @SubscribeEvent
    public static void confirmPlayerFeeds(ServerTickEvent.Post event) {
        // No delayed persistent references survive a tick, disconnect, or world change.
        List<PendingFeed> pending = List.copyOf(PENDING_FEEDS);
        PENDING_FEEDS.clear();
        for (PendingFeed feed : pending) {
            Animal animal = feed.animal();
            if (!animal.isRemoved() && animal.isAlive() && animal.level().getServer() == event.getServer()
                    && feed.consumed() && AnimalStatsManager.canFeed(animal)) {
                AnimalStatsManager.onFed(animal);
            }
        }
    }
}
