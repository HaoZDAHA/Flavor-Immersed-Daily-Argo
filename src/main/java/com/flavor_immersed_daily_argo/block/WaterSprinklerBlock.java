package com.flavor_immersed_daily_argo.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// 管网末端洒水器（旋转水管 / 扇形水管的公共部分）。
//
// 它接在管网末尾：只要四邻有一个管网构件（WaterPipeBlock#isNetwork），
// 那根管子就会长出管臂插进来、不再是「单口末端」，喷水交由本洒水器接管；本格自身的
// #FILLED / #FERTILIZED 直接复用管网属性对象，由 WaterPipeBlock.refreshOne
// 随管网一起刷出来。
//
// 隔一格也能工作：若四邻都没接管网（例如中间夹着一个固体方块），BFS 走不过来，
// 本洒水器就在自己的计划刻里朝 6 个方向各探两格——中间那格是固体方块、外圈是管网构件，
// 就按外圈的充水 / 催熟状态同步自己。探测每 WaterPipeBlock#IRRIGATE_INTERVAL 刻一次。
//
// 灌溉：只要充水，就每 WaterPipeBlock#IRRIGATE_INTERVAL 刻浇一遍地，
// 复用 WaterPipeBlock#irrigateArea（湿度顶满 + 金坷垃催熟）。
//
// 两种装法：#FACE 决定模型与喷洒的姿态——点在方块顶面上就 FLOOR，
// 平放在地上（用竖着的那套模型）；点在其他面上就 WALL，横过来贴在侧面上
// （用侧面的那套模型）。吊顶那套没有模型，朝下点也归到 WALL，朝向取玩家面向。
public abstract class WaterSprinklerBlock extends Block {

// 管网是否充水：与水管共用同一个属性对象，刷新时一并写回
    public static final BooleanProperty FILLED = WaterPipeBlock.FILLED;

// 浇出去的水有没有掺金坷垃：同样复用管网属性
    public static final BooleanProperty FERTILIZED = WaterPipeBlock.FERTILIZED;

// 安装面：平放在方块顶面（FLOOR）或贴在竖直侧面（WALL），与 #FACING 配合定姿态
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    protected WaterSprinklerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FILLED, false)
                .setValue(FERTILIZED, false)
                .setValue(FACE, AttachFace.WALL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILLED, FERTILIZED, FACE);
    }

// 摆放时的安装面：点在方块顶面上就平放在地上（AttachFace#FLOOR），
// 点在其他面上都按贴墙处理（AttachFace#WALL）。
    protected static AttachFace attachFaceFor(BlockPlaceContext context) {
        return context.getClickedFace() == Direction.UP ? AttachFace.FLOOR : AttachFace.WALL;
    }

// 平放（立在地上）时的朝向：取玩家面向的反方向，也就是洒水器背对玩家、朝前喷
    protected static Direction horizontalFacingFor(BlockPlaceContext context) {
        return context.getHorizontalDirection().getOpposite();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACE, attachFaceFor(context));
    }

// 管网接入

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !WaterPipeBlock.isNetwork(oldState)) {
            WaterPipeBlock.refreshNetwork(level, pos);
// 起一条自己的计划刻节拍：隔一格探测与浇灌都靠它
            level.scheduleTick(pos, this, WaterPipeBlock.IRRIGATE_INTERVAL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide && !WaterPipeBlock.isNetwork(newState)) {
// 洒水器没了，管网会断成几截：各自重新判断
            WaterPipeBlock.refreshNetwork(level, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof Level realLevel && !realLevel.isClientSide) {
            WaterPipeBlock.refreshNetwork(realLevel, pos);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

// 定时刷新

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        serverTick(level, pos, state);
        level.scheduleTick(pos, this, WaterPipeBlock.IRRIGATE_INTERVAL);
    }

// 兜底：区块重载会把计划刻丢掉，靠随机刻把节拍重新续上（已经在跑的就别再加一条链）
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            serverTick(level, pos, state);
            level.scheduleTick(pos, this, WaterPipeBlock.IRRIGATE_INTERVAL);
        }
    }

// 先把「跟管网的关系」对齐，再浇一遍地。
//
// 四邻若直接接着管网，充水 / 催熟由 refreshOne 统一刷，这里不再插手；
// 只有四邻都没接管网时，才朝外探两格采纳远端管网的状态。
    private static void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        boolean attached = false;
        for (Direction direction : Direction.values()) {
            if (WaterPipeBlock.isNetwork(level.getBlockState(pos.relative(direction)))) {
                attached = true;
                break;
            }
        }
        if (!attached) {
            boolean filled = false;
            boolean fertilized = false;
            for (Direction direction : Direction.values()) {
                BlockPos mid = pos.relative(direction);
// 中间那格必须是个固体方块（若它是管网方块，上面就已判为 attached）
                if (level.getBlockState(mid).getCollisionShape(level, mid).isEmpty()) {
                    continue;
                }
                BlockState far = level.getBlockState(pos.relative(direction, 2));
                if (!WaterPipeBlock.isNetwork(far)) {
                    continue;
                }
                if (far.hasProperty(FILLED) && far.getValue(FILLED)) {
                    filled = true;
                }
                if (far.hasProperty(FERTILIZED) && far.getValue(FERTILIZED)) {
                    fertilized = true;
                }
            }
            BlockState current = level.getBlockState(pos);
            boolean wantFertilized = filled && fertilized;
            if (current.getValue(FILLED) != filled || current.getValue(FERTILIZED) != wantFertilized) {
                level.setBlock(pos, current
                        .setValue(FILLED, filled)
                        .setValue(FERTILIZED, wantFertilized), Block.UPDATE_CLIENTS);
            }
        }
        BlockState now = level.getBlockState(pos);
        if (now.getValue(FILLED) && now.getBlock() instanceof WaterSprinklerBlock sprinkler) {
            sprinkler.irrigateAt(level, now, pos);
        }
    }

// 形状

// 贴合各自模型、随朝向旋转的碰撞箱
    protected abstract VoxelShape collisionShape(BlockState state);

// 准星轮廓：故意给成一整格，模型细，跟着模型走会很难瞄
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return collisionShape(state);
    }

// 喷洒

// 充水时每拍浇一遍地；浇到哪里由各自的喷水范围决定
    protected abstract void irrigateAt(ServerLevel level, BlockState state, BlockPos pos);

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(FILLED)) {
            emitSpray(level, pos, state, random);
        }
    }

// 客户端粒子：按各自的喷水形状撒水花
    protected abstract void emitSpray(Level level, BlockPos pos, BlockState state, RandomSource random);
}