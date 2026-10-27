package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.entity.Handcart;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

// 拉力木车（手持物品）：对着方块右键，把 Handcart 放在准星指到的那个点上。
//
// 朝向取玩家的水平朝向：车把（模型 -z）正对着玩家，放下去就能直接右键牵走。
public class HandcartItem extends TooltipItem {

    public HandcartItem(Item.Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
// 方块底面点不到，也不该往地下塞
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();

        if (level instanceof ServerLevel serverLevel) {
            Vec3 spawn = context.getClickLocation();
            Handcart handcart = new Handcart(ModEntities.HANDCART.get(), level);
// yaw 用玩家的水平朝向：车头顺着玩家的视线，车把正好朝着玩家
            handcart.moveTo(spawn.x, spawn.y, spawn.z, context.getRotation(), 0.0F);
            serverLevel.addFreshEntity(handcart);
            handcart.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
