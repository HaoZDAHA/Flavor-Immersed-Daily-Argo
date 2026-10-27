package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.entity.Scarecrow;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

// 稻草人（手持物品）：对着方块右键，把 Scarecrow 放在准星指到的那个点上。
//
// 落点是准星命中的实际坐标而不是整格中心，朝向直接取玩家的水平朝向，所以插下去就是玩家摆的那个姿势；
// 放下去之后交给实体自己的重力，落点悬空的话会自己掉到地面上。
public class ScarecrowItem extends TooltipItem {

    public ScarecrowItem(Item.Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
// 方块底面点不到，也不该往地下插
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();

        if (level instanceof ServerLevel serverLevel) {
            Vec3 spawn = context.getClickLocation();
            Scarecrow scarecrow = new Scarecrow(ModEntities.SCARECROW.get(), level);
// yaw 直接用玩家的水平朝向，实体模型正面就会和玩家看的方向一致
            scarecrow.moveTo(spawn.x, spawn.y, spawn.z, context.getRotation(), 0.0F);
            serverLevel.addFreshEntity(scarecrow);
            level.playSound(null, scarecrow.getX(), scarecrow.getY(), scarecrow.getZ(),
                    SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
            scarecrow.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
