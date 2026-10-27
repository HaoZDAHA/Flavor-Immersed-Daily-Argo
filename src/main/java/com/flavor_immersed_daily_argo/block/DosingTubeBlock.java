package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

// 加料阀。
//
// 它是管网里的一个环节：和水管一样会参与 WaterPipeBlock#refreshNetwork 的连通判定，
// 所以可以直接串在管路的任意一段中间——六面都能接，不管竖直还是水平。
//
// 加料：手持金坷垃右键阀体，投一颗进罐里，一颗生效 5 分钟（#DURATION_PER_JINKELA）。
// 生效期间管网所有喷口的浇灌都会带上一点随机催熟（见 WaterPipeBlock.irrigate）——
// 具体是整张网上的 WaterPipeBlock#FERTILIZED 被刷成 true，喷口据此多施一次骨粉。
//
// 含水：实现 SimpleWaterloggedBlock，可以直接埋在管沟的水里而不把水顶掉。
public class DosingTubeBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {

    public static final MapCodec<DosingTubeBlock> CODEC = simpleCodec(DosingTubeBlock::new);

// 一颗金坷垃的生效时长：5 分钟
    public static final int DURATION_PER_JINKELA = 5 * 60 * 20;

// 管网是否充水；与水管共用同一个属性对象，刷新时一并写回
    public static final BooleanProperty FILLED = WaterPipeBlock.FILLED;

// 里面有没有正在生效的金坷垃，管网刷新时据此决定整张网要不要带催熟
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

// 本格是否被水浸着（含水方块）
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

// 碰撞箱：按模型三段拼出来（阀体 / 顶部小管 / 外壳）
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(5.0, 3.0, 5.0, 11.0, 13.0, 11.0),
            Block.box(6.0, 12.0, 6.0, 10.0, 16.0, 10.0),
            Block.box(4.0, 5.0, 4.0, 12.0, 12.0, 12.0)).optimize();

    public DosingTubeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FILLED, false)
                .setValue(ACTIVE, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends DosingTubeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILLED, ACTIVE, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluid.is(FluidTags.WATER));
    }

// 准星指示箱：故意给成一整格，阀体不大，跟着模型走不好瞄
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

// 碰撞箱：仍然贴合模型
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

// 管网

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !WaterPipeBlock.isNetwork(oldState)) {
            WaterPipeBlock.refreshNetwork(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide && !WaterPipeBlock.isNetwork(newState)) {
// 阀没了，原来的管网会断成几截（催熟加成也会跟着消失），各截重新判断
            WaterPipeBlock.refreshNetwork(level, pos);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
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

// 加料

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModItems.JINKELA.get())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof DosingTubeBlockEntity tube) {
                tube.insertJinkela();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return stack.isEmpty()
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

// 方块实体

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DosingTubeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null : (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof DosingTubeBlockEntity tube) {
                DosingTubeBlockEntity.tick(tickLevel, pos, tube);
            }
        };
    }
}
