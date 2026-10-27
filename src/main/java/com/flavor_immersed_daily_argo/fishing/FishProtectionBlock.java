package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.item.FishingNetItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// 鱼护（鱼笼）
//
// 放在水里把钓到的活鱼养在里面的网笼。实现 SimpleWaterloggedBlock：放进水里会照样保住那格水源，
// 但放在干地上不会凭空生出水来（干的就是干的）。
//
// 四面围栏按相邻鱼护动态隐藏——相邻格子也是鱼护时，那一面不画围栏，多个鱼护能连成一片，
// 与栅栏 / 墙的连接逻辑同理。连接状态用 north/east/south/west 四个布尔属性表达
// （true 表示该侧有相邻鱼护），blockstate 用 16 条精确组合，每种连接状态挑一个现成模型（含旋转）。
//
// 鱼护不存任何数据：钓上来的活鱼直接生成成实体养在笼子里（围栏把鱼挡在里面），
// 抄网里兜着的鱼右键也能倒进来，同样是把实体放进笼内。拆掉笼子鱼就自由了。
public class FishProtectionBlock extends Block implements SimpleWaterloggedBlock {

    public static final MapCodec<FishProtectionBlock> CODEC = simpleCodec(FishProtectionBlock::new);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
// 该侧是否有相邻鱼护（有则隐藏这一面的围栏）
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

// 底盘：底网那一层，玩家能站上去
    private static final VoxelShape BASE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
// 四面网墙的碰撞箱：沿该侧 2px 厚的竖直挡板，主要用来把鱼挡在网里
    private static final VoxelShape WALL_NORTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
    private static final VoxelShape WALL_EAST = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WALL_SOUTH = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WALL_WEST = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);

// 16 种连接状态各自的碰撞箱，先按「该侧没有相邻鱼护 → 这一面立网墙」烘焙好。
//
// 下标按位拼装：1 = 北墙、2 = 东墙、4 = 南墙、8 = 西墙；0 表示四面都有相邻鱼护，
// 只剩底盘。碰撞箱必须按位图缓存，避免每个实体每 tick 都重新拼一次形状。
    private static final VoxelShape[] COLLISION_SHAPES = buildCollisionShapes();

    private static VoxelShape[] buildCollisionShapes() {
        VoxelShape[] shapes = new VoxelShape[16];
        for (int mask = 0; mask < shapes.length; mask++) {
            VoxelShape shape = BASE_SHAPE;
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

// 把四个连接属性折算成网墙位图
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

    public FishProtectionBlock(Properties properties) {
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
        return this.defaultBlockState()
// 只是「能含水」：放在水里才跟着变水方块，干地放置就是干的
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER)
                .setValue(NORTH, isProtection(level, pos.north()))
                .setValue(EAST, isProtection(level, pos.east()))
                .setValue(SOUTH, isProtection(level, pos.south()))
                .setValue(WEST, isProtection(level, pos.west()));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
// 含水的话得让水照常结算，不然水会一直挂在这一格不动
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state.setValue(NORTH, isProtection(level, pos.north()))
                .setValue(EAST, isProtection(level, pos.east()))
                .setValue(SOUTH, isProtection(level, pos.south()))
                .setValue(WEST, isProtection(level, pos.west()));
    }

// 含水时才对外是一个满格水源，干放时就是普通方块
    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private static boolean isProtection(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof FishProtectionBlock;
    }

// 鼠标指示 / 点击箱与碰撞箱一致：底盘 + 网墙。
//
// 不能取整格——整格的点击箱会把笼子里游的鱼整个挡住，玩家既点不到鱼也抄不到，
// 每次都得先把鱼护拆了。笼内留空之后，射线能直接打到里面的实体。
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPES[wallMask(state)];
    }

// 碰撞箱跟着模型走：底盘 + 每一面没连成片的网墙，把鱼挡在网里
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPES[wallMask(state)];
    }

// 只有抄网装着鱼时才拦下这次右键：把网里的鱼倒进笼子里（实体直接生成在这一格内）。
// 其他物品照常交给物品 / 方块自己处理。
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof FishingNetItem && !FishingNetItem.isEmpty(stack)
                && state.getValue(WATERLOGGED)) {
            if (!level.isClientSide) {
                FishingNetItem.releaseAll(level, player, stack, Vec3.atCenterOf(pos));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return stack.isEmpty()
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }
}
