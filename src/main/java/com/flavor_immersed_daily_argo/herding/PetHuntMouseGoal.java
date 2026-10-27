package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.entity.Mouse;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

// 游荡模式的猎鼠目标：范围扩到 3 区块，让在窝附近晃悠的猫狗也能远远发现老鼠并扑过去。
//
// 攻击动作完全复用原版（猫的 OcelotAttackGoal、狗的 MeleeAttackGoal），
// 这里只负责把老鼠设成目标。
public class PetHuntMouseGoal extends NearestAttackableTargetGoal<Mouse> {

// 索敌半径：3 区块。必须是常量——父类构造函数里就会调用 #getFollowDistance()
    private static final double HUNT_RANGE = 48.0D;

    private final TamableAnimal pet;

    public PetHuntMouseGoal(TamableAnimal pet) {
        super(pet, Mouse.class, 10, true, false, null);
        this.pet = pet;
    }

    @Override
    protected double getFollowDistance() {
        return HUNT_RANGE;
    }

    @Override
    public boolean canUse() {
        return PetNestFolk.getMode(this.pet) == PetNestFolk.PetMode.WANDER
                && PetNestFolk.isBound(this.pet, this.pet.level())
                && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return PetNestFolk.getMode(this.pet) == PetNestFolk.PetMode.WANDER && super.canContinueToUse();
    }
}