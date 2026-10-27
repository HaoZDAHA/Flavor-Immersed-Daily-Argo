package com.flavor_immersed_daily_argo.herding;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import com.flavor_immersed_daily_argo.all.ModBlocks;

// 晒盐场
//
// 平时是干燥的整格盐田（stage=0）。顶面接触到水就变成含水纹理（stage=1），
// 约十秒后把它上方的水吸掉，并在正上方结出一块独立的盐堆（SaltPileBlock），
// 自身回到干燥状态。盐堆会自己一级级长高，挖掉盐堆只会掉盐，不会伤到盐田。
public class SaltPanBlock extends Block {

// 阶段：0 干燥盐田，1 含水盐田
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);

// 含水的盐田把上方水吸干所需的刻数（10 秒）
    private static final int ABSORB_DELAY = 200;

    public SaltPanBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

// 直接放进水里时就已经是含水状态
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        return isWaterAbove(context.getLevel(), context.getClickedPos())
                ? state.setValue(STAGE, 1)
                : state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getValue(STAGE) == 1) {
            level.scheduleTick(pos, this, ABSORB_DELAY);
        }
    }

// 水从上方漫过来时，干燥的盐田转成含水状态
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide || state.getValue(STAGE) != 0 || !isWaterAbove(level, pos)) {
            return;
        }
        level.setBlock(pos, state.setValue(STAGE, 1), Block.UPDATE_ALL);
        level.scheduleTick(pos, this, ABSORB_DELAY);
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.6F, 1.2F);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(STAGE) != 1) {
            return;
        }
        BlockPos above = pos.above();
// 先把上方的水吸掉；水再漫回来，就过一会儿再吸
        if (isWaterAbove(level, pos)) {
            level.setBlockAndUpdate(above, Blocks.AIR.defaultBlockState());
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.6F);
        }
        if (!level.isEmptyBlock(above)) {
            level.scheduleTick(pos, this, ABSORB_DELAY);
            return;
        }
// 水干了：盐田回到干燥状态，正上方结出第一级盐堆（独立的方块，挖盐不会伤到盐田）
        level.setBlock(pos, state.setValue(STAGE, 0), Block.UPDATE_ALL);
        level.setBlockAndUpdate(above,
                ModBlocks.SALT_PILE.get().defaultBlockState().setValue(SaltPileBlock.STAGE, 0));
        level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.5F, 1.3F);
    }

// 顶面接触水：正上方是水就算
    private static boolean isWaterAbove(Level level, BlockPos pos) {
        return level.getFluidState(pos.above()).is(FluidTags.WATER);
    }
}
