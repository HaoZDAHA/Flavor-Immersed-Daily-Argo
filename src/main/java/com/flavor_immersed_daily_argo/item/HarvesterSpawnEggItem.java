package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class HarvesterSpawnEggItem extends Item {
    public HarvesterSpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.flavor_immersed_daily_argo.harvester_spawn_egg"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack itemstack = context.getItemInHand();
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState blockstate = level.getBlockState(blockpos);

        BlockPos spawnPos;
        if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
            spawnPos = blockpos;
        } else {
            spawnPos = blockpos.relative(direction);
        }

        Mob mob = (Mob) ModEntities.HARVESTER.get().create(level);
        if (mob != null) {
            mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            mob.yBodyRot = context.getRotation();
            level.addFreshEntity(mob);
            itemstack.shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
