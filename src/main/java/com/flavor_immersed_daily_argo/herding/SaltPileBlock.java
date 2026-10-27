package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// 盐堆
//
// 晒盐场把上方的水吸干后，在它正上方结出的独立方块。stage=0/1/2 分别对应盐的
// 第 1/2/3 阶段，靠随机刻一级级长高。
//
// 硬度很低，徒手就能敲；破坏时按阶段掉落 1、2、3 份 flavor_immersed_daily:salt，
// 并受时运附魔加成（见 loot_table/blocks/salt_pile.json）。盐堆是独立方块，
// 挖掉它不会损坏下面的晒盐场。
public class SaltPileBlock extends Block {

// 阶段：0/1/2 对应盐的第 1/2/3 阶段
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 2);

// 每次随机刻往上长一级的概率（1/3）
    private static final int GROW_CHANCE = 3;

// 三个阶段的碰撞/选取高度：1 像素、4 像素、6 像素
    private static final VoxelShape[] SHAPES = {
            Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D / 16.0D, 1.0D),
            Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 4.0D / 16.0D, 1.0D),
            Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 6.0D / 16.0D, 1.0D)
    };

    public SaltPileBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(STAGE)];
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        if (stage >= 2) {
            return;
        }
// 上方被方块挡住就不长
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }
        if (random.nextInt(GROW_CHANCE) != 0) {
            return;
        }
        level.setBlock(pos, state.setValue(STAGE, stage + 1), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.4F, 1.4F);
    }

// 底下的晒盐场没了就跟着掉落，不留悬空的盐
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && !level.getBlockState(pos.below()).is(ModBlocks.SALT_PAN.get())) {
            level.destroyBlock(pos, true);
        }
    }
}
