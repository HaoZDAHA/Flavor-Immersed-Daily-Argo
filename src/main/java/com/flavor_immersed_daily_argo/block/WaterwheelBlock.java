package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

import java.util.Optional;

// 灌溉水车
//
// 竖直三格多方块（1×1×3）：玩家放下的是 part=bottom 那一格，另外两格
// middle / top 由放置时一并生成，并用 #updateShape 相互联动——
// 任一格被移除，其余两格自动变空气。
//
// 模型与动画由 WaterwheelRenderer（只挂在 bottom 的方块实体上）整体绘制，
// 三格自身都不进区块网格（INVISIBLE）。碰撞箱是底下那根从脚下顶到三格高的实心柱
// （1×1×3），上两格补位但不再额外挡人。
//
// 相当于三格高的水源：放在水源上方（放下时或之后一秒内通水）就会“通水”，此后 working
// 一直为 true、且不会再变回去——水车抽的是一股活水。于是这三格的流体状态都返回
// WaterwheelWaterFluid（功能上跟水源一样，但不参与渲染），耕地近水判定
// （canBeHydrated）命中、其它模组对 FluidTags.WATER 的检查也会把这三格都当成水；
// 三格各自实现 BucketPickup，用空桶对准任意一格都能打出水桶，而且是取之不尽的
// （不改方块、也不消耗这里的水）。反过来，即使把脚下那格真水源抽走，水车依旧通水，
// 既不会停转也不会消失。
//
// 脚下或本格只要有水（不论是不是水源），就持续冒水花并偶尔下水声，见
// WaterwheelBlockEntity 的客户端 tick。
public class WaterwheelBlock extends BaseEntityBlock implements BucketPickup {


// 竖直三格中的哪一格：底下那格由玩家放置，另外两格是延伸
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

// 水平朝向：水轮立面的朝向（轮轴与它垂直）
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 是否正立在水源上方；为 true 时这三格对外都是一个水源
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

// 碰撞箱：1×1×3，从脚下一直顶到三格高（只算在 bottom 那一格上）
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 48.0, 16.0);

    public WaterwheelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, Part.BOTTOM)
                .setValue(FACING, Direction.NORTH)
                .setValue(WORKING, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING, WORKING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
// 竖直三格：上面两格都得空出来才放得下，否则整次放置取消
        if (!level.getBlockState(pos.above()).canBeReplaced(context)
                || !level.getBlockState(pos.above(2)).canBeReplaced(context)) {
            return null;
        }
// 放下时就先判断一次脚下是不是水源，免得刚放好要等一秒才“通水”
        FluidState below = level.getFluidState(pos.below());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WORKING, below.is(FluidTags.WATER) && below.isSource());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
// 一次把上面两格补上，工作状态与朝向跟底下那格保持一致
            level.setBlock(pos.above(), state.setValue(PART, Part.MIDDLE), Block.UPDATE_ALL);
            level.setBlock(pos.above(2), state.setValue(PART, Part.TOP), Block.UPDATE_ALL);
        }
    }

// 竖直方向的邻居一变就检查这段三格是否还完整：少了哪一格，整段就散掉。
//
// 返回空气走的是普通方块替换，不会递归掉物品（物品只在 bottom 那一格的掉落表里给）。
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        boolean intact = switch (state.getValue(PART)) {
            case BOTTOM -> direction != Direction.UP || isPart(level, pos.above(), Part.MIDDLE);
            case MIDDLE -> (direction != Direction.UP || isPart(level, pos.above(), Part.TOP))
                    && (direction != Direction.DOWN || isPart(level, pos.below(), Part.BOTTOM));
            case TOP -> direction != Direction.DOWN || isPart(level, pos.below(), Part.MIDDLE);
        };
        return intact ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
                : Blocks.AIR.defaultBlockState();
    }

    private static boolean isPart(BlockGetter level, BlockPos pos, Part part) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof WaterwheelBlock && state.getValue(PART) == part;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

// 准星轮廓只画底下那格（上两格不参与瞄准，免得挡住射线与选块框）
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART) == Part.BOTTOM ? Shapes.block() : Shapes.empty();
    }

// 碰撞箱 1×1×3：整根柱子都挡人，但只记在 bottom 那一格上
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART) == Part.BOTTOM ? SHAPE : Shapes.empty();
    }

// 模型整体交给方块实体渲染器画（只挂在 bottom 上），三格自身都不进区块网格
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == Part.BOTTOM ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WORKING) ? ModFluids.WATERWHEEL_WATER.get().defaultFluidState() : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.BOTTOM ? new WaterwheelBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(PART) != Part.BOTTOM) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.WATERWHEEL_ENTITY.get(), WaterwheelBlockEntity::tick);
    }

// 空桶右键：直接从水车里打一桶水，想打多少打多少。
//
// 三格都要各自实现，因为空桶只会看“命中的那一格”是不是 BucketPickup。
// 这里既不挖掉水车、也不动这三格的水，所以水是无限的：打完水车照旧通水、照旧能灌溉。
    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        return state.getValue(WORKING) ? new ItemStack(Items.WATER_BUCKET) : ItemStack.EMPTY;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    @Override
    public Optional<SoundEvent> getPickupSound(BlockState state) {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

// 脚下（或本格）有水就算“泡在水里”：水花粒子与水声都在方块实体的客户端 tick 里出
    public static boolean hasWater(BlockGetter level, BlockPos pos) {
        return isWater(level, pos) || isWater(level, pos.below());
    }

    private static boolean isWater(BlockGetter level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

// 竖直三格的上下段标识
    public enum Part implements StringRepresentable {
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
