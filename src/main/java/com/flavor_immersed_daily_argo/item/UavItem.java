package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.entity.Uav;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

// 多功能无人机（手持物品）：对着方块右键，把 Uav 放在准星指到的那个点上。
//
// 刚放下的无人机电源是关着的，所以既不会飞也没有动作。
public class UavItem extends TooltipItem {

    public UavItem(Item.Properties properties, String tooltipKey) {
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
            Uav uav = new Uav(ModEntities.UAV.get(), level);
            uav.moveTo(spawn.x, spawn.y, spawn.z, context.getRotation(), 0.0F);
            serverLevel.addFreshEntity(uav);
            uav.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
