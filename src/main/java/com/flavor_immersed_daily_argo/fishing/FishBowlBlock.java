package com.flavor_immersed_daily_argo.fishing;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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

// 鱼缸
//
// 四面玻璃、没有底的一块缸：摆在水中就能盛住那格水（实现 SimpleWaterloggedBlock，
// 放水里保住水源，放干地上就是干的），相邻的鱼缸会自动连成一片——相邻格子也是鱼缸时，
// 那一面玻璃不立，横着拼成一大缸、往上摞成一摞都行。因为没有底，上下相邻也无需换另一套模型，
// 这一套玻璃就够了。
//
// 连接状态与捕捞渔网同款，用 north/east/south/west 四个布尔属性表达
// （true 表示该侧有相邻鱼缸），blockstate 用精确组合各挑一个现成模型（含旋转）。
// 连接只看同一层的邻格，见 #refresh——往上摞高时上面那层照自己那一圈重新算，
// 因此做缸壁只要摆外圈、内侧也不会多出一圈玻璃；而单独摞上去的一格始终四面立着玻璃。
//
// 碰撞箱按立着的那几面玻璃给：连成一片的那一面不通不挡，玩家能顺着合并后的缸走进去；
// 玻璃只有 2px 厚、各面都不算实心面，水可以直接流进来，缸里那格照着
// SimpleWaterloggedBlock 的规矩变成水源，于是水在缸里流动、汇聚、存住。
public class FishBowlBlock extends Block implements SimpleWaterloggedBlock {

    public static final MapCodec<FishBowlBlock> CODEC = simpleCodec(FishBowlBlock::new);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
// 该侧是否有相邻鱼缸（有则这一面不立玻璃）
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

// 四面玻璃的碰撞箱：沿该侧 2px 厚的竖直挡板，与模型里那张薄片对齐
    private static final VoxelShape WALL_NORTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
    private static final VoxelShape WALL_EAST = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WALL_SOUTH = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WALL_WEST = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);

// 16 种连接状态各自的碰撞箱，先按「该侧没有相邻鱼缸 → 这一面立玻璃」烘焙好。
//
// 下标按位拼装：1 = 北墙、2 = 东墙、4 = 南墙、8 = 西墙；0 表示四面都有相邻鱼缸，
// 一格玻璃都不立。碰撞箱必须按位图缓存，避免每个实体每 tick 都重新拼一次形状。
    private static final VoxelShape[] COLLISION_SHAPES = buildCollisionShapes();

    private static VoxelShape[] buildCollisionShapes() {
        VoxelShape[] shapes = new VoxelShape[16];
        for (int mask = 0; mask < shapes.length; mask++) {
            VoxelShape shape = Shapes.empty();
            if ((mask & 1) != 0) {
                shape = Shapes.or(shape, WALL_NORTH);
            }
            if ((mask & 2) != 0) {
                shape = Shapes.or(shape, WALL_EAST);
            }
            if ((mask & 4) != 0) {
                shape = Shapes.or(shape, WALL_SOUTH);
            }
            if ((mask & 8) != 0) {
                shape = Shapes.or(shape, WALL_WEST);
            }
            shapes[mask] = shape.optimize();
        }
        return shapes;
    }

// 把四个连接属性折算成玻璃位图
    private static int wallMask(BlockState state) {
        int mask = 0;
        if (!state.getValue(NORTH)) {
            mask |= 1;
        }
        if (!state.getValue(EAST)) {
            mask |= 2;
        }
        if (!state.getValue(SOUTH)) {
            mask |= 4;
        }
        if (!state.getValue(WEST)) {
            mask |= 8;
        }
        return mask;
    }

    public FishBowlBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return refresh(this.defaultBlockState()
// 只是「能含水」：放在水里才跟着变水方块，干地放置就是干的
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER), level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
// 含水的话得让水照常结算，不然水会一直挂在这一格不动
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return refresh(state, level, pos);
    }

// 按周围环境刷新四向连接：邻格也是鱼缸时，这一面不立玻璃。
//
// 连接只看同一层的邻格：往上摞高时，上面那一层照着自己那一圈邻格重新算一遍，
// 因此「外圈立玻璃、内圈相连」的形态本来就会一层层续上去，不必参考下方那一格。
// 反过来若跟着下面那格走，孤零零摞在实心缸中间上方的一格就会把四面玻璃全隐掉——那正是要避免的。
    private static BlockState refresh(BlockState state, BlockGetter level, BlockPos pos) {
        return state.setValue(NORTH, isBowl(level, pos.north()))
                .setValue(EAST, isBowl(level, pos.east()))
                .setValue(SOUTH, isBowl(level, pos.south()))
                .setValue(WEST, isBowl(level, pos.west()));
    }

// 含水时才对外是一个满格水源，干放时就是普通方块
    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private static boolean isBowl(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof FishBowlBlock;
    }

// 鼠标指示箱取整格，方便对准玻璃右键
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

// 碰撞箱跟着模型走：立着的那几面玻璃，连成一片的那面没有
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPES[wallMask(state)];
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}