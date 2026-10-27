package com.flavor_immersed_daily_argo.herding;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

// 宠物窝的共用工具。
//
// 绑定关系（认了哪个窝、在哪个维度）与「跟随 / 蹲下 / 游荡」三种模式全部记在宠物自己的持久化数据里，
// 这样宠物读档、跨区块都不会丢状态，宠物窝只需要按坐标去反查。
public final class PetNestFolk {

// 宠物侧记录的窝坐标（BlockPos.asLong()）
    private static final String NEST_KEY = "FlavorImmersedDailyPetNest";
// 宠物侧记录的窝所在维度
    private static final String NEST_DIM_KEY = "FlavorImmersedDailyPetNestDim";
// 宠物侧记录的行为模式
    private static final String MODE_KEY = "FlavorImmersedDailyPetMode";

// 跟随 / 蹲下模式回原版数值：猫停 5 格、狗停 2 格（抄自 1.21.1 Cat / Wolf 的 registerGoals）
    private static final int FOLLOW_OWNER_PRIORITY = 6;
    private static final float CAT_FOLLOW_STOP_DISTANCE = 5.0F;
    private static final float WOLF_FOLLOW_STOP_DISTANCE = 2.0F;

// 游荡目标的优先级：压得住原版闲逛（猫 11 / 狗 8），又能被攻击目标（猫 9 / 狗 5）顶掉
    private static final int CAT_WANDER_PRIORITY = 10;
    private static final int WOLF_WANDER_PRIORITY = 7;
// 猎鼠目标优先级：排在所有目标最前，范围也更远
    private static final int HUNT_PRIORITY = 0;

    private PetNestFolk() {
    }

// 行为模式：绑定宠物窝后的猫狗除了原版的跟随、蹲下，还多出一个游荡
    public enum PetMode {
// 跟随主人（原版行为）
        FOLLOW,
// 原地蹲下（原版行为）
        SIT,
// 在窝周围游荡，附近有老鼠时迅速索敌
        WANDER;

        public PetMode next() {
            return switch (this) {
                case FOLLOW -> SIT;
                case SIT -> WANDER;
                case WANDER -> FOLLOW;
            };
        }
    }

// 绑定关系

// 把宠物记在这个窝名下（只写宠物侧，窝侧由调用方补）
    public static void bind(TamableAnimal pet, BlockPos nestPos, ResourceKey<Level> dimension) {
        CompoundTag data = pet.getPersistentData();
        data.putLong(NEST_KEY, nestPos.asLong());
        data.putString(NEST_DIM_KEY, dimension.location().toString());
    }

// 解除绑定并回到跟随模式（窝被拆、宠物过世时调用）
    public static void unbind(TamableAnimal pet) {
        CompoundTag data = pet.getPersistentData();
        data.remove(NEST_KEY);
        data.remove(NEST_DIM_KEY);
        setMode(pet, PetMode.FOLLOW);
    }

// 宠物认的窝在哪一格；没认过则为 null
    @Nullable
    public static BlockPos nestPos(TamableAnimal pet) {
        CompoundTag data = pet.getPersistentData();
        return data.contains(NEST_KEY) ? BlockPos.of(data.getLong(NEST_KEY)) : null;
    }

// 宠物认的窝是否就在这个维度（跨维度视为没绑定）
    public static boolean isBound(TamableAnimal pet, Level level) {
        CompoundTag data = pet.getPersistentData();
        if (!data.contains(NEST_KEY) || !data.contains(NEST_DIM_KEY)) {
            return false;
        }
        return data.getString(NEST_DIM_KEY).equals(level.dimension().location().toString());
    }

// 宠物是否已经认了别的窝（换个坐标或换个维度都算）
    public static boolean settledElsewhere(TamableAnimal pet, BlockPos nestPos, ResourceKey<Level> dimension) {
        CompoundTag data = pet.getPersistentData();
        if (!data.contains(NEST_KEY)) {
            return false;
        }
        return data.getLong(NEST_KEY) != nestPos.asLong()
                || !data.getString(NEST_DIM_KEY).equals(dimension.location().toString());
    }

// 行为模式

    public static PetMode getMode(TamableAnimal pet) {
        CompoundTag data = pet.getPersistentData();
        if (!data.contains(MODE_KEY)) {
            return PetMode.FOLLOW;
        }
        String name = data.getString(MODE_KEY);
        for (PetMode mode : PetMode.values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        return PetMode.FOLLOW;
    }

// 记下模式并把对应的 AI 目标换上去
    public static void setMode(TamableAnimal pet, PetMode mode) {
        pet.getPersistentData().putString(MODE_KEY, mode.name());
        applyMode(pet, mode);
    }

// 切到下一个模式。
//
// 还没在宠物窝安家的宠物不允许进入游荡模式：退回跟随并返回 null，
// 由调用方给玩家一句提示。
    @Nullable
    public static PetMode cycleMode(TamableAnimal pet) {
        PetMode next = getMode(pet).next();
        if (next == PetMode.WANDER && !isBound(pet, pet.level())) {
            setMode(pet, PetMode.FOLLOW);
            return null;
        }
        setMode(pet, next);
        return next;
    }

// AI 目标

// 按模式装配目标：游荡模式摘掉跟随、装上闲逛；其它模式反过来
    public static void applyMode(TamableAnimal pet, PetMode mode) {
        if (mode == PetMode.WANDER) {
            removeFollowOwnerGoal(pet);
            ensureWanderGoal(pet);
        } else {
            removeWanderGoal(pet);
            ensureFollowOwnerGoal(pet);
        }
// 只有确实进过本系统（留下过模式记录）的宠物才由这里接管蹲下状态；
// 否则老存档里原版右键按下的「蹲下」会在加载时被无端掀掉
        if (pet.getPersistentData().contains(MODE_KEY)) {
            pet.setOrderedToSit(mode == PetMode.SIT);
        }
    }

// 宠物一进世界就把游荡与猎鼠目标挂上（读档也会走这里，所以必须按类名去重）
    public static void ensureGoals(TamableAnimal pet) {
        ensureWanderGoal(pet);
        ensureHuntGoal(pet);
    }

    private static void ensureWanderGoal(TamableAnimal pet) {
        if (hasGoal(pet, PetWanderGoal.class)) {
            return;
        }
        int priority = pet instanceof Wolf ? WOLF_WANDER_PRIORITY : CAT_WANDER_PRIORITY;
        pet.goalSelector.addGoal(priority, new PetWanderGoal(pet));
    }

    private static void ensureFollowOwnerGoal(TamableAnimal pet) {
        if (hasGoal(pet, FollowOwnerGoal.class)) {
            return;
        }
        float stopDistance = pet instanceof Wolf ? WOLF_FOLLOW_STOP_DISTANCE : CAT_FOLLOW_STOP_DISTANCE;
        pet.goalSelector.addGoal(FOLLOW_OWNER_PRIORITY, new FollowOwnerGoal(pet, 1.0D, 10.0F, stopDistance, false));
    }

    private static void removeWanderGoal(Mob pet) {
        pet.goalSelector.removeAllGoals(goal -> goal instanceof PetWanderGoal);
    }

    private static void removeFollowOwnerGoal(Mob pet) {
        pet.goalSelector.removeAllGoals(goal -> goal instanceof FollowOwnerGoal);
    }

    private static void ensureHuntGoal(TamableAnimal pet) {
        if (hasTargetGoal(pet, PetHuntMouseGoal.class)) {
            return;
        }
        pet.targetSelector.addGoal(HUNT_PRIORITY, new PetHuntMouseGoal(pet));
    }

    private static boolean hasGoal(Mob pet, Class<? extends Goal> type) {
        for (WrappedGoal wrapped : pet.goalSelector.getAvailableGoals()) {
            if (type.isInstance(wrapped.getGoal())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTargetGoal(Mob pet, Class<? extends Goal> type) {
        for (WrappedGoal wrapped : pet.targetSelector.getAvailableGoals()) {
            if (type.isInstance(wrapped.getGoal())) {
                return true;
            }
        }
        return false;
    }
}