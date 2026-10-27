package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.herding.CoopEggs;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

// 被扔出去的禽蛋。行为照抄原版 ThrownEgg，只把孵出的「鸡」换成蛋所对应的禽类。
//
// 对应关系复用鸡窝认蛋的那套规则（见 CoopEggs#entityFor(ItemStack)）：
// 蛋的注册名去掉 _egg 后缀就是同名实体（duck_egg → duck）。
// 因此五个禽蛋共用这一个弹射物，真正孵出什么取决于扔出去的是哪个蛋。
public class ThrownPoultryEgg extends ThrowableItemProjectile {

// 孵出 1 只的概率分母（与原版鸡蛋一致）
    private static final int HATCH_CHANCE = 8;
// 一次孵出 4 只的概率分母（与原版鸡蛋一致）
    private static final int MULTI_HATCH_CHANCE = 32;
// 碎裂时喷出的碎片粒子数量（与原版鸡蛋一致）
    private static final int SHATTER_PARTICLES = 8;

    public ThrownPoultryEgg(EntityType<? extends ThrownPoultryEgg> type, Level level) {
        super(type, level);
    }

    public ThrownPoultryEgg(Level level, LivingEntity thrower) {
        super(ModEntities.THROWN_POULTRY_EGG.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DUCK_EGG.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != 3) {
            super.handleEntityEvent(id);
            return;
        }
        for (int i = 0; i < SHATTER_PARTICLES; i++) {
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5D) * 0.08D,
                    (this.random.nextDouble() - 0.5D) * 0.08D,
                    (this.random.nextDouble() - 0.5D) * 0.08D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }
        EntityType<?> type = CoopEggs.entityFor(this.getItem());
        if (type != null && this.random.nextInt(HATCH_CHANCE) == 0) {
            int count = this.random.nextInt(MULTI_HATCH_CHANCE) == 0 ? 4 : 1;
            for (int i = 0; i < count; i++) {
                Entity baby = type.create(this.level());
                if (baby == null) {
                    continue;
                }
                if (baby instanceof AgeableMob ageable) {
                    ageable.setAge(-24000);
                }
                baby.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                this.level().addFreshEntity(baby);
            }
        }
// 碎裂：广播事件 3 让客户端喷蛋壳粒子，然后消失
        this.level().broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }
}
