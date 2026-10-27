package com.flavor_immersed_daily_argo.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import java.util.List;

// 生鲜方块
//
// 8 种立体（3D）生鲜海产共用的通用方块类：徒手瞬间破坏、不做碰撞、不遮挡相邻方块面，
// 并带有一个水平朝向 state（facing），放置时正面朝向玩家，与原版熔炉等一致。
public class RawSeafoodBlock extends HorizontalDirectionalBlock implements PlaceableFood {

    public static final MapCodec<RawSeafoodBlock> CODEC = simpleCodec(RawSeafoodBlock::new);

    public RawSeafoodBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends RawSeafoodBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        this.addPlaceableTooltip(tooltip);
    }
}
