package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

// 被扔出去的加压沼气（纯投掷物）。
//
// 砸到方块或生物就当场爆开：Level.ExplosionInteraction#NONE 让它只对实体造成伤害、
// 不破坏任何方块，适合在牧场里清怪又不心疼建筑。
public class ThrownMarshGas extends ThrowableItemProjectile {

// 爆炸威力：范围不大，够清掉近身的小怪
    private static final float EXPLOSION_POWER = 2.0F;

    public ThrownMarshGas(EntityType<? extends ThrownMarshGas> type, Level level) {
        super(type, level);
    }

    public ThrownMarshGas(Level level, LivingEntity thrower) {
        super(ModEntities.THROWN_MARSH_GAS.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MARSH_GAS.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }
// NONE：方块一个不炸，但爆炸伤害照常结算
        this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                EXPLOSION_POWER, Level.ExplosionInteraction.NONE);
        this.discard();
    }
}