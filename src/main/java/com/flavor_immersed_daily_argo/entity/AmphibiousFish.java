package com.flavor_immersed_daily_argo.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.level.Level;

// 两栖甲壳类（龙虾、帝王蟹、面包蟹）的通用基类。
//
// 原版 AbstractFish 有两处"只能活在水里"的逻辑，不适合能上岸活动的生物，
// 这里统一屏蔽：
//
// 溺水：WaterAnimal#baseTick 在离水时每刻扣呼吸值，扣到 -20 后开始造成溺水伤害
// → 覆写 #handleAirSupply(int)，恒定保持满呼吸值。
// 扑腾：AbstractFish#aiStep 在陆地（离水 + 站在地面 + 竖直方向被碰撞修正）会每刻
// 给自己叠加一个 0.4 的向上冲量并播放扑腾音效，也就是玩家看到的"陆地溺水跳跃"
// → 覆写 #aiStep()，让该分支的判定条件不成立。
public abstract class AmphibiousFish extends AbstractFish {

    protected AmphibiousFish(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
    }

// 离水后呼吸值不再消耗，因此不会因缺氧掉血死亡，可以在陆地上正常存活。
    @Override
    protected void handleAirSupply(int airSupply) {
        this.setAirSupply(300);
    }

    @Override
    public void aiStep() {
// AbstractFish#aiStep 的扑腾分支需要同时满足 !isInWater() && onGround() && verticalCollision。
// verticalCollision 在整个原版（含 NeoForge 补丁）代码里只有该扑腾分支会读取，
// 所以在陆地时先把它清掉即可让扑腾分支不成立；随后的 Entity#move 会把它重新写成真实值，
// 不会影响本刻其它逻辑（落地伤害用的是 fallDistance，与它无关）。
        if (!this.isInWater()) {
            this.verticalCollision = false;
        }
        super.aiStep();
    }
}
