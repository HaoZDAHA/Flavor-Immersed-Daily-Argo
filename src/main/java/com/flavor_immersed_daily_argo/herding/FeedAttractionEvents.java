package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.AnimalFeedItem;
import com.flavor_immersed_daily_argo.item.AquaticFeedItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.stream.Stream;

// 饲料的「引兽」效果：把某一种饲料拿在手上，附近认这口食的动物会一路跟过来。
//
// 做法和原版用小麦引牛一样，给目标动物挂一个 TemptGoal 的子类；
// 判定条件换成「手上拿着的是这份饲料、且这份饲料的名单里有它」，所以四种饲料
// 各自引各自的动物，鱼也不例外。生物一进世界（含读档）挂一次，按目标类型去重。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class FeedAttractionEvents {

// 和原版引家畜同一个优先级
    private static final int TEMPT_PRIORITY = 3;
// 跟过来的速度：比原版引家畜稍快一点
    private static final double TEMPT_SPEED = 1.15D;

    private FeedAttractionEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof PathfinderMob mob)) {
            return;
        }
        if (!isFeedTarget(mob.getType()) || hasTemptGoal(mob)) {
            return;
        }
        mob.goalSelector.addGoal(TEMPT_PRIORITY, new FeedTemptGoal(mob));
    }

// 生物是否出现在任意一份饲料的名单里
    private static boolean isFeedTarget(EntityType<?> type) {
        return AnimalFeedItem.grassTargets().contains(type)
                || AnimalFeedItem.seedTargets().contains(type)
                || AnimalFeedItem.meatTargets().contains(type)
                || AquaticFeedItem.aquaticTargets().contains(type);
    }

// 手上这份饲料认不认这只动物
    private static boolean isFeedFor(Mob mob, ItemStack stack) {
        if (stack.getItem() instanceof AnimalFeedItem feed) {
            return feed.getTargets().contains(mob.getType());
        }
        if (stack.getItem() instanceof AquaticFeedItem feed) {
            return feed.getTargets().contains(mob.getType());
        }
        return false;
    }

    private static boolean hasTemptGoal(Mob mob) {
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof FeedTemptGoal) {
                return true;
            }
        }
        return false;
    }

    private static final class FeedTemptGoal extends TemptGoal {
        private FeedTemptGoal(PathfinderMob mob) {
            super(mob, TEMPT_SPEED, new FeedIngredient(mob), false);
        }
    }

// 1.20.1 的 TemptGoal 只认 Ingredient（1.21 才改成 Predicate），这里用一个能拿到目标动物的
// 子类把「手上拿着的是这份饲料、且这份饲料的名单里有它」这条判定塞进去
    private static final class FeedIngredient extends Ingredient {
        private final Mob mob;

        private FeedIngredient(Mob mob) {
            super(Stream.empty());
            this.mob = mob;
        }

        @Override
        public boolean test(ItemStack stack) {
            return isFeedFor(this.mob, stack);
        }
    }
}
