package com.flavor_immersed_daily_argo.herding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.EnumSet;

// 游荡模式：在宠物窝周围几格区块内自己溜达。
//
// 只占 MOVE 标记，不碰 LOOK，这样原版的「看向玩家」之类的小动作还能继续跑，宠物显得有生气。
public class PetWanderGoal extends Goal {

// 游荡半径：1.5 区块
    private static final int WANDER_RADIUS = 24;
// 竖直搜索范围
    private static final int WANDER_Y_RANGE = 7;
// 走两步歇一会儿，别一直溜达
    private static final int WANDER_INTERVAL = 120;
// 离家超过这个水平距离（2 区块）就直接往回走
    private static final double RETURN_DISTANCE = 32.0D;
    private static final double SPEED = 1.0D;

    private final TamableAnimal pet;

    private double wantedX;
    private double wantedY;
    private double wantedZ;

    public PetWanderGoal(TamableAnimal pet) {
        this.pet = pet;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (PetNestFolk.getMode(this.pet) != PetNestFolk.PetMode.WANDER) {
            return false;
        }
        BlockPos nestPos = PetNestFolk.nestPos(this.pet);
        if (nestPos == null || !PetNestFolk.isBound(this.pet, this.pet.level())) {
            return false;
        }
        if (this.pet.getRandom().nextInt(reducedTickDelay(WANDER_INTERVAL)) != 0) {
            return false;
        }
        Vec3 target = this.pickTarget(nestPos);
        if (target == null) {
            return false;
        }
        this.wantedX = target.x;
        this.wantedY = target.y;
        this.wantedZ = target.z;
        return true;
    }

// 平常在窝附近随机挑个点；离得太远就先往窝走
    @Nullable
    private Vec3 pickTarget(BlockPos nestPos) {
        Vec3 center = Vec3.atCenterOf(nestPos);
        if (Math.sqrt(this.pet.distanceToSqr(center)) > RETURN_DISTANCE) {
            return center;
        }
        return DefaultRandomPos.getPosTowards(this.pet, WANDER_RADIUS, WANDER_Y_RANGE, center, WANDER_RADIUS);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.pet.getNavigation().isDone()
                && PetNestFolk.getMode(this.pet) == PetNestFolk.PetMode.WANDER;
    }

    @Override
    public void start() {
        this.pet.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, SPEED);
    }

    @Override
    public void stop() {
        this.pet.getNavigation().stop();
    }
}