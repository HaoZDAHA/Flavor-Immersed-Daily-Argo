package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

// 剑鱼
//
// 行动方式：平时在水中原地小幅度悬浮；每隔数秒朝一片随机水域做一次高速直线冲刺，
// 冲刺途中不断喷出大量气泡并播放游泳音效，被吻部撞到的生物会受到伤害。
//
// 属于中立生物：不会主动追击玩家，但受到攻击后会锁定攻击者，用同样的冲刺反复撞击反击，
// 直到对方死亡、离开水面或逃出追击范围。
public class Swordfish extends AbstractFish {

// 循环游动动画 swordfish_swim
    public final AnimationState swimAnimationState = new AnimationState();

// 是否正在冲刺（仅服务端有意义），供 #aiStep() / #travel(Vec3) 区分状态。
    private boolean charging;

    public Swordfish(EntityType<? extends Swordfish> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractFish.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
// 完全接管 AI，不调用 super：
//  - 去掉 AbstractFish 的 PanicGoal / AvoidEntityGoal（原版鱼类见玩家会逃跑）；
//  - 去掉 FishSwimGoal（随机缓速游动），改由 SwordfishChargeGoal 负责冲刺，
//    未冲刺时的悬浮由 aiStep() 完成。
        this.goalSelector.addGoal(0, new SwordfishChargeGoal(this));
// 中立生物的反击：被攻击后把攻击者设为目标，冲刺目标会朝该目标反复冲刺
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
// 悬浮：抵消水中重力，只保留缓慢的小幅上下浮动，使剑鱼基本停在原地。
        if (!this.level().isClientSide && !this.charging && this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            double bob = Math.sin((this.tickCount + this.getId() * 7) * 0.25D) * 0.012D;
            this.setDeltaMovement(motion.x * 0.6D, bob, motion.z * 0.6D);
        }
        super.aiStep();
    }

    @Override
    public void travel(Vec3 input) {
// 覆盖原版鱼类移动（缓速游动 + 每刻持续下沉），改为完全由速度控制：
// 冲刺时由 SwordfishChargeGoal 每刻重置为匀速直线速度，平时则是 aiStep() 给出的悬浮速度。
        if (this.isEffectiveAi() && this.isInWater()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            return;
        }
        super.travel(input);
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.COD_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COD_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COD_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.COD_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation(flavor_immersed_daily_argo.MODID, "entities/swordfish");
    }

// 剑鱼冲刺：沿一条直线高速冲刺。
//
// 没有目标时，每隔数秒朝一片随机水域冲刺一次；受到攻击后（getTarget() 不为空），
// 则以很短的间隔反复朝攻击者冲刺，形成反击。
    static class SwordfishChargeGoal extends Goal {

// 平时两次冲刺之间的最小 / 最大间隔（tick）
        private static final int MIN_INTERVAL = 60;
        private static final int MAX_INTERVAL = 140;
// 反击时两次冲刺之间的间隔（tick）
        private static final int RETALIATE_INTERVAL = 10;
// 单次冲刺持续的最大 tick 数
        private static final int CHARGE_TICKS = 16;
// 冲刺速度（格 / tick）
        private static final double CHARGE_SPEED = 0.6D;
// 攻击判定：沿冲刺方向向前延伸 / 四周外扩的距离（格）
        private static final double HIT_REACH = 1.5D;
        private static final double HIT_PADDING = 0.5D;
// 每 tick 喷出的气泡数量
        private static final int BUBBLES_PER_TICK = 12;
// 每隔多少 tick 播放一次游泳音效
        private static final int SOUND_INTERVAL = 4;

        private final Swordfish swordfish;
        private final Set<Integer> damagedEntities = new HashSet<>();

        private Vec3 direction = Vec3.ZERO;
        private int remainingTicks;
        private int nextChargeTick;

        SwordfishChargeGoal(Swordfish swordfish) {
            this.swordfish = swordfish;
        }

        @Override
        public boolean canUse() {
            return this.swordfish.isAlive()
                    && this.swordfish.isInWater()
                    && this.swordfish.tickCount >= this.nextChargeTick;
        }

        @Override
        public boolean canContinueToUse() {
            return this.remainingTicks > 0
                    && this.swordfish.isAlive()
                    && this.swordfish.isInWater()
                    && !this.swordfish.horizontalCollision;
        }

        @Override
        public void start() {
            LivingEntity target = this.resolveTarget();
            this.direction = this.pickDirection(target);
            this.remainingTicks = target != null ? this.chargeTicksToReach(target) : CHARGE_TICKS;
            this.damagedEntities.clear();
            this.swordfish.charging = true;

// 冲刺由速度直接驱动，关闭寻路以免导航系统与直线冲刺互相拉扯
            this.swordfish.getNavigation().stop();

// 转向冲刺方向
            float yaw = (float) (Mth.atan2(this.direction.z, this.direction.x) * 180.0D / Math.PI) - 90.0F;
            this.swordfish.setYRot(yaw);
            this.swordfish.yRotO = yaw;
            this.swordfish.setYHeadRot(yaw);
            this.swordfish.yBodyRot = yaw;
            this.swordfish.yBodyRotO = yaw;
        }

        @Override
        public void tick() {
            this.remainingTicks--;

// 每刻重置速度，配合 travel() 得到不受重力与阻力影响的匀速直线冲刺
            this.swordfish.setDeltaMovement(this.direction.scale(CHARGE_SPEED));
            this.swordfish.hasImpulse = true;

            this.spawnBubbles();
            if (this.remainingTicks % SOUND_INTERVAL == 0) {
                this.swordfish.playSound(SoundEvents.DOLPHIN_SWIM, 0.7F,
                        0.9F + this.swordfish.getRandom().nextFloat() * 0.2F);
            }

            this.damageAlongPath();
        }

        @Override
        public void stop() {
            this.swordfish.charging = false;
            this.damagedEntities.clear();
// 反击期间保持高频冲刺，否则恢复平时悠闲的节奏
            int interval = this.resolveTarget() != null
                    ? RETALIATE_INTERVAL
                    : MIN_INTERVAL + this.swordfish.getRandom().nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
            this.nextChargeTick = this.swordfish.tickCount + interval;
        }

// 取出可作为反击对象的当前目标：目标须活着且在水中。
//
// 目标本身由 HurtByTargetGoal 维护（超出追击范围 / 长时间失去视线时它会自行清除），
// 这里只负责判断能否用冲刺反击，不会直接清空目标，避免与目标选择器互相打架。
        @Nullable
        private LivingEntity resolveTarget() {
            LivingEntity target = this.swordfish.getTarget();
            return target != null && target.isAlive() && target.isInWater() ? target : null;
        }

// 有反击目标时朝目标；否则朝一片随机水域。
        private Vec3 pickDirection(@Nullable LivingEntity target) {
            Vec3 delta = null;
            if (target != null) {
                delta = target.position().subtract(this.swordfish.position());
            } else {
                Vec3 swimmable = BehaviorUtils.getRandomSwimmablePos(this.swordfish, 12, 7);
                if (swimmable != null) {
                    delta = swimmable.subtract(this.swordfish.position());
                }
            }
            Vec3 horizontal = delta != null ? new Vec3(delta.x, 0.0D, delta.z) : Vec3.ZERO;
            return horizontal.lengthSqr() > 1.0E-4D
                    ? horizontal.normalize()
                    : Vec3.directionFromRotation(0.0F, this.swordfish.getYRot());
        }

// 反击时冲刺到刚好够得着目标即可，避免从目标身上冲过头。
        private int chargeTicksToReach(LivingEntity target) {
            Vec3 delta = target.position().subtract(this.swordfish.position());
            double distance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            return Mth.clamp((int) Math.ceil(distance / CHARGE_SPEED), 3, CHARGE_TICKS);
        }

        private void spawnBubbles() {
            if (!(this.swordfish.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            Vec3 pos = this.swordfish.position();
            float halfWidth = this.swordfish.getBbWidth() * 0.5F;
            float halfHeight = this.swordfish.getBbHeight() * 0.5F;
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    pos.x, pos.y + halfHeight, pos.z,
                    BUBBLES_PER_TICK,
                    halfWidth, halfHeight, halfWidth,
                    0.02D);
        }

// 撞伤沿途生物；同一次冲刺内每个生物只受一次伤害。
        private void damageAlongPath() {
// 判定范围在包围盒基础上沿冲刺方向向前延伸、四周外扩，形成较大的命中区
            AABB hitBox = this.swordfish.getBoundingBox()
                    .expandTowards(this.direction.x * HIT_REACH, 0.0D, this.direction.z * HIT_REACH)
                    .inflate(HIT_PADDING);
            for (LivingEntity target : this.swordfish.level().getEntitiesOfClass(LivingEntity.class, hitBox)) {
                if (target == this.swordfish || target instanceof Swordfish) {
                    continue;
                }
                if (this.damagedEntities.contains(target.getId())) {
                    continue;
                }
                if (target.hurt(this.swordfish.damageSources().mobAttack(this.swordfish),
                        (float) this.swordfish.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                    this.damagedEntities.add(target.getId());
                }
            }
        }
    }
}
