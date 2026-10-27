package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

public class Quail extends Chicken {

    private int quailEggTime = this.random.nextInt(6000) + 6000;

    public Quail(EntityType<? extends Quail> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public Quail getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.QUAIL.get().create(level);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation(flavor_immersed_daily_argo.MODID, "entities/quail");
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.eggTime = 6000;
        this.flap = 0.0F;
        this.flapSpeed = 0.0F;
        this.oFlap = 0.0F;
        this.oFlapSpeed = 0.0F;
        if (!this.level().isClientSide && this.isAlive() && !this.isBaby()) {
            if (--this.quailEggTime <= 0) {
                this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                Item egg = BuiltInRegistries.ITEM.get(new ResourceLocation("flavor_immersed_daily_argo", "quail_egg"));
                if (egg != Items.AIR) {
                    this.spawnAtLocation(egg);
                }
                this.quailEggTime = this.random.nextInt(6000) + 6000;
            }
        }
    }
}
