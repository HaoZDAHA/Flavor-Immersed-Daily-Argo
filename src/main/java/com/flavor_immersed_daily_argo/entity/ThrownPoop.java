package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.herding.PoopSplashPacket;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

// 被扔出去的粪便（纯投掷物）。
//
// 它跟地上那坨 Poop 是两回事：这里只是一枚以粪便贴图的样子飞出去的弹射物，
// 砸到方块或砸到生物都当场碎裂消失，绝不会落在地上变成一坨。
//
// 砸中的只要是玩家（包括扔的人自己），都会给 ta 糊一屏 shitgui.png。
public class ThrownPoop extends ThrowableItemProjectile {

// 砸中玩家后屏幕覆层停留的时长（4 秒）
    private static final int SPLASH_TICKS = 80;
// 碎裂时喷出的碎片粒子数量
    private static final int SHATTER_PARTICLES = 8;

    public ThrownPoop(EntityType<? extends ThrownPoop> type, Level level) {
        super(type, level);
    }

    public ThrownPoop(Level level, LivingEntity thrower) {
        super(ModEntities.THROWN_POOP.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.POOP.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }
// 砸中的只要是玩家就生效——扔的人自己也算：原版 Projectile.leftOwner 保证了刚出手那几刻
// 不会认主人，等它飞远了再落回来砸到自己头上，照样糊一脸
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof ServerPlayer target) {
            PacketDistributor.sendToPlayer(target, new PoopSplashPacket(SPLASH_TICKS));
        }
// 砸到什么都就地碎裂：广播事件 3 让客户端喷碎片粒子，然后消失
        this.level().broadcastEntityEvent(this, (byte) 3);
        this.discard();
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
                    this.random.nextGaussian() * 0.05D,
                    this.random.nextGaussian() * 0.05D + 0.1D,
                    this.random.nextGaussian() * 0.05D);
        }
    }
}
