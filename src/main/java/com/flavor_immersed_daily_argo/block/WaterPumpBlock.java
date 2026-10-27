package com.flavor_immersed_daily_argo.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// 水泵。
//
// 把它埋进水里，与它相连的整张 WaterPipeBlock 管网就会进入充水状态；
// 管网末端（单口的那根管子）朝外敞着的一截会往外冒水花。是否“泡在水里”看
// #isInWater：本格或六个面里任意一面贴着水就算。
//
// 水泵自己不是水管，所以它只把两侧的管网各接起来（不会让两侧互相连通）。
//
// 含水：实现 SimpleWaterloggedBlock，可以直接放在水里（#WATERLOGGED），
// 于是“埋在水里”既可以靠本格浸水，也可以靠相邻的水面，见 #isInWater。
public class WaterPumpBlock extends Block implements SimpleWaterloggedBlock {


// 本格是否被水浸着（含水方块）
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

// 碰撞箱：按模型三段拼出来
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(5.0, 3.0, 5.0, 11.0, 13.0, 11.0),
            Block.box(6.0, 12.0, 6.0, 10.0, 16.0, 10.0),
            Block.box(4.0, 5.0, 4.0, 12.0, 12.0, 12.0)).optimize();

    public WaterPumpBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluid.is(FluidTags.WATER));
    }

// 准星指示箱：故意给成一整格，水泵本体不大，跟着模型走不好瞄
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

// 碰撞箱：仍然贴合模型
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            WaterPipeBlock.refreshNetwork(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide) {
            WaterPipeBlock.refreshNetwork(level, pos);
        }
    }

// 身边的水一变（水漫过来或被抽走），重新判断管网充不充水
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
// 含水的话得让水照常结算，不然水会一直挂在这一格不动
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (level instanceof Level realLevel && !realLevel.isClientSide) {
            WaterPipeBlock.refreshNetwork(realLevel, pos);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

// 埋在水里：本格被水浸着，或六个面里任意一面贴着水
    public static boolean isInWater(BlockGetter level, BlockPos pos) {
        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
}
