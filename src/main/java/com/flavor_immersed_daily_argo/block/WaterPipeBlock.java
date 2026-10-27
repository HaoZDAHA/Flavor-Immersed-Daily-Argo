package com.flavor_immersed_daily_argo.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

// 水管。
//
// 只用一个方块、一个六位的 shape 状态：每一位表示某个面有没有接到相邻的水管，
// 再由 blockstates/water_pipe.json 把这个掩码映射到各套模型 + 对应的旋转，
// 于是无论玩家怎么搭、朝哪个方向，管子都能自动接上。
//
// 位序与 Direction#ordinal() 对齐：0=下 1=上 2=北 3=南 4=西 5=东。
//
// 充水：整张管网里只要挂着至少一台泡在水里的 WaterPumpBlock，
// 网上所有管子的 #FILLED 就会被刷成 true（见 #refreshNetwork）。
// 充水后，管网末端（只有一个接口的那根管子）会朝敞口方向喷出一道抛物线水柱，见
// #animateTick。
//
// 喷水灌溉：喷出去的水落到哪，那一片的耕地就按“贴着水源”处理——落点周围水平 ±4、
// 竖直 ±2（允许耕地有一点小落差）的 FarmBlock 湿度直接顶满，见 #irrigate。
// 喷水期间每隔 #IRRIGATE_INTERVAL 刻浇一遍（#tick），随机刻（#randomTick）
// 负责在区块重载丢掉计划刻之后把节拍续上。
//
// 碰撞箱：按 shape 给出中心方柱 + 各连通方向那一截的并集，
// 与 blockstate 实际挑中的模型一致（含缺模型时被近似的几种，见 #renderedMask）。
// 准星轮廓（#getShape）则故意给成一整格——管子细，跟着模型走会很难瞄准。
//
// 含水：实现 SimpleWaterloggedBlock，可以直接放在水里、水里流过来也能把它
// 泡上（#WATERLOGGED），于是管网埋在水下时不会把水顶掉。
public class WaterPipeBlock extends Block implements SimpleWaterloggedBlock {

    public static final MapCodec<WaterPipeBlock> CODEC = simpleCodec(WaterPipeBlock::new);

// 六位连接掩码，含义见类注释
    public static final IntegerProperty SHAPE = IntegerProperty.create("shape", 0, 63);

// 是否充水：管网里接了一台泡在水里的水泵就是 true
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

// 本格是否被水浸着（含水方块）
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

// 浇出去的水有没有掺上金坷垃：整张管网里只要挂着一个正在生效的
// DosingTubeBlock 加料阀就是 true，由 #refreshOne 统一写回，
// #irrigate 据此让落点里的作物带一点随机的催熟。
    public static final BooleanProperty FERTILIZED = BooleanProperty.create("fertilized");

// 中心的四方柱
    private static final VoxelShape CORE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);

// 每个方向伸出去的那一截
    private static final VoxelShape[] ARMS = new VoxelShape[6];

    static {
        ARMS[Direction.DOWN.ordinal()] = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
        ARMS[Direction.UP.ordinal()] = Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);
        ARMS[Direction.NORTH.ordinal()] = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
        ARMS[Direction.SOUTH.ordinal()] = Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
        ARMS[Direction.WEST.ordinal()] = Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
        ARMS[Direction.EAST.ordinal()] = Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    }

// shape(0..63) → 碰撞箱，与 blockstate 实际挑中的模型一一对应
    private static final VoxelShape[] SHAPES = buildShapes();

// 防止 setBlock 触发的 onPlace / onRemove 反过来再刷一次，形成无限递归
    private static boolean refreshing = false;

    public WaterPipeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, 0)
                .setValue(FILLED, false)
                .setValue(FERTILIZED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends WaterPipeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, FILLED, FERTILIZED, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

// 连接识别

// 是不是管网构件：水管本身、加料阀、以及两种末端洒水器。
// 凡是接进管网的东西都得能互相认出来；洒水器直接贴着管子时，管子会长出管臂插进洒水器，
// 于是那根管子不再是「单口末端」，会把喷水让给洒水器。
    public static boolean isNetwork(BlockState state) {
        return state.getBlock() instanceof WaterPipeBlock
                || state.getBlock() instanceof DosingTubeBlock
                || state.getBlock() instanceof WaterSprinklerBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
// 在同一个格子里瞄着水放下时，水要留下（含水）
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return withConnections(this.defaultBlockState(), context.getLevel(), context.getClickedPos())
                .setValue(WATERLOGGED, fluid.is(FluidTags.WATER));
    }

// 邻居变动时只改自己对应的那一位：邻居也成了管网构件就接上，否则断开。
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
// 含水的话得让水照常结算，不然水会一直挂在这一格不动
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        int bit = 1 << direction.ordinal();
        int shape = state.getValue(SHAPE);
        shape = isNetwork(neighborState) ? (shape | bit) : (shape & ~bit);
        return state.setValue(SHAPE, shape);
    }

    private BlockState withConnections(BlockState state, Level level, BlockPos pos) {
        int shape = 0;
        for (Direction direction : Direction.values()) {
            if (isNetwork(level.getBlockState(pos.relative(direction)))) {
                shape |= 1 << direction.ordinal();
            }
        }
        return state.setValue(SHAPE, shape);
    }

// 充水刷新

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
// 只是充水状态在变（oldState 还是管网构件）就没必要再刷一遍
        if (!level.isClientSide && !isNetwork(oldState)) {
            refreshNetwork(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide && !isNetwork(newState)) {
// 管子没了，管网会断成几截：每截各自重新判断
            for (Direction direction : Direction.values()) {
                if (isNetwork(level.getBlockState(pos.relative(direction)))) {
                    refreshNetwork(level, pos.relative(direction));
                }
            }
        }
    }

// 从 origin 出发刷新管网充水状态。origin 既可以是水管（刷新它所在的那一整张网），
// 也可以是水泵 / 加料阀（把四周每一截管网分别刷新一遍）。
    public static void refreshNetwork(Level level, BlockPos origin) {
        if (refreshing) {
            return;
        }
        refreshing = true;
        try {
            if (isNetwork(level.getBlockState(origin))) {
                refreshOne(level, origin);
            } else {
                for (Direction direction : Direction.values()) {
                    BlockPos near = origin.relative(direction);
                    if (isNetwork(level.getBlockState(near))) {
                        refreshOne(level, near);
                    }
                }
            }
        } finally {
            refreshing = false;
        }
    }

// 广度优先走完一截连通的管网，看它够不够得着一台泡在水里的水泵
// （决定 #FILLED）、有没有挂着一个正在生效的加料阀
// （决定 #FERTILIZED），再统一写回。
    private static void refreshOne(Level level, BlockPos start) {
        Set<BlockPos> network = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        network.add(start);
        queue.add(start);
        boolean filled = false;
        boolean fertilized = false;
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos near = pos.relative(direction);
                BlockState nearState = level.getBlockState(near);
                if (isNetwork(nearState)) {
                    if (network.add(near)) {
                        queue.add(near);
                    }
                    if (nearState.getBlock() instanceof DosingTubeBlock
                            && nearState.getValue(DosingTubeBlock.ACTIVE)) {
                        fertilized = true;
                    }
                } else if (nearState.getBlock() instanceof WaterPumpBlock
                        && WaterPumpBlock.isInWater(level, near)) {
                    filled = true;
                }
            }
        }
        for (BlockPos pos : network) {
            BlockState state = level.getBlockState(pos);
            if (!state.hasProperty(FILLED)) {
                continue;
            }
            BlockState updated = state;
            boolean changed = false;
            if (state.getValue(FILLED) != filled) {
                updated = updated.setValue(FILLED, filled);
                changed = true;
            }
// 催熟加成跟着充水状态走：没水自然也浇不出去
            if (updated.hasProperty(FERTILIZED) && updated.getValue(FERTILIZED) != (filled && fertilized)) {
                updated = updated.setValue(FERTILIZED, filled && fertilized);
                changed = true;
            }
            if (!changed) {
                continue;
            }
// 只发客户端更新，不引起邻块连锁，免得又绕回 onPlace
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
// 刚通水又正好是末端（单口）的，把喷水灌溉的节拍起起来（已经在跑就不重复起）
            if (filled && updated.hasProperty(SHAPE) && Integer.bitCount(updated.getValue(SHAPE)) == 1
                    && !level.getBlockTicks().hasScheduledTick(pos, updated.getBlock())) {
                level.scheduleTick(pos, updated.getBlock(), IRRIGATE_INTERVAL);
            }
        }
    }

// 碰撞箱

// 准星指示箱：故意给成一整格，管子细，跟着模型走会很难瞄
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

// 碰撞箱：仍然贴合模型
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(SHAPE)];
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape[] shapes = new VoxelShape[64];
        for (int s = 0; s < 64; s++) {
            int mask = renderedMask(s);
            VoxelShape shape = CORE;
            for (Direction direction : Direction.values()) {
                if ((mask & (1 << direction.ordinal())) != 0) {
                    shape = Shapes.or(shape, ARMS[direction.ordinal()]);
                }
            }
            shapes[s] = shape.optimize();
        }
        return shapes;
    }

// 缺模型的那几个 shape 会被 blockstate 近似到别的模型上，碰撞箱得跟着模型走，
// 否则会跟看到的形状对不上。
    private static int renderedMask(int shape) {
        return switch (shape) {
            case 0, 1, 2 -> 3;          // 没有 / 只有一个竖向接口 → 竖向直通
            case 4, 8 -> 12;            // 只接北或南 → 水平直通（南北）
            case 16, 32 -> 48;          // 只接西或东 → 水平直通（东西）
            case 31, 47, 55, 59 -> 63;  // 五面通 → 六面全通
            default -> shape;
        };
    }

// 末端喷水

// 水柱水平初速
    private static final double SPRAY_SPEED = 0.18;
// 水柱初始上抛速度（朝下喷口的除外）
    private static final double SPRAY_LIFT = 0.28;
// 每刻重力
    private static final double SPRAY_GRAVITY = 0.04;
// 每刻速度衰减
    private static final double SPRAY_FRICTION = 0.98;
// 轨迹最多算多少刻，防止落点算出十万八千里
    private static final int SPRAY_MAX_STEPS = 40;

// 灌溉间隔（tick）。末端洒水器也按这个节拍续自己的计划刻。
    public static final int IRRIGATE_INTERVAL = 20;
// 灌溉范围：落点水平 ±4 格
    private static final int IRRIGATE_RADIUS_H = 4;
// 灌溉范围：落点竖直 ±2 格，允许耕地有一点小落差
    private static final int IRRIGATE_RADIUS_V = 2;

// 掺了金坷垃的水，每次浇灌时每株作物被随机催熟的几率（「少量」）
    private static final float FERTILIZE_CHANCE = 0.05F;

// 是不是在喷水的管子：充水 + 只有单口（管网末端）
    private static boolean isSpraying(BlockState state) {
        return state.getValue(FILLED) && Integer.bitCount(state.getValue(SHAPE)) == 1;
    }

// 敞口方向 = 唯一接口的反方向
    private static Direction openDirection(BlockState state) {
        int shape = state.getValue(SHAPE);
        return Direction.values()[Integer.numberOfTrailingZeros(shape)].getOpposite();
    }

// 从喷口按抛物线一步步算水柱轨迹，写进 out（每步 x,y,z 三个 double），返回步数。
// 撞到方块或出界就停。同一套参数既给粒子用、也给灌溉落点用，保证看到的水弧和浇到的地方对得上。
    private static int traceSpray(LevelReader level, BlockPos pipe, Direction open, double[] out) {
        double vx = open.getStepX() * SPRAY_SPEED;
        double vy = open == Direction.DOWN ? -SPRAY_SPEED : SPRAY_LIFT;
        double vz = open.getStepZ() * SPRAY_SPEED;
        double x = pipe.getX() + 0.5 + open.getStepX() * 0.5;
        double y = pipe.getY() + 0.5 + open.getStepY() * 0.5;
        double z = pipe.getZ() + 0.5 + open.getStepZ() * 0.5;
        int n = 0;
        for (int step = 0; step < SPRAY_MAX_STEPS; step++) {
            vy -= SPRAY_GRAVITY;
            x += vx;
            y += vy;
            z += vz;
            vx *= SPRAY_FRICTION;
            vy *= SPRAY_FRICTION;
            vz *= SPRAY_FRICTION;
            BlockPos p = BlockPos.containing(x, y, z);
            if (p.getY() < level.getMinBuildHeight() || p.getY() >= level.getMaxBuildHeight()
                    || !level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) {
                break;
            }
            out[n++] = x;
            out[n++] = y;
            out[n++] = z;
// 撞到有碰撞箱的方块就到头了
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
                break;
            }
        }
        return n / 3;
    }

// 水柱落点：轨迹的最后一格
    private static BlockPos landing(LevelReader level, BlockPos pipe, Direction open) {
        double[] out = new double[SPRAY_MAX_STEPS * 3];
        int steps = traceSpray(level, pipe, open, out);
        if (steps == 0) {
            return pipe;
        }
        int i = (steps - 1) * 3;
        return BlockPos.containing(out[i], out[i + 1], out[i + 2]);
    }

// 把落点周围（含小落差）的耕地都浇上：湿度直接顶满，跟贴着水源一个效果。
//
// 管网里挂了正在生效的加料阀（#FERTILIZED）时，这一片耕地上方的作物
// 还会按 #FERTILIZE_CHANCE 随机被催熟一次——加料阀里那颗金坷垃的效果。
    private static void irrigate(ServerLevel level, BlockState state, BlockPos pos) {
        BlockPos center = landing(level, pos, openDirection(state));
        irrigateArea(level, center, state.getValue(FERTILIZED), IRRIGATE_RADIUS_H, IRRIGATE_RADIUS_V);
    }

// 把 center 周围水平 ±radiusH、竖直 ±radiusV 的耕地都浇上：湿度直接顶满，跟贴着水源一个效果。
//
// fertilized 为 true 时，这一片耕地上方的作物还会按 #FERTILIZE_CHANCE
// 随机被催熟一次——加料阀里那颗金坷垃的效果。末端两种洒水器也复用这套浇灌。
    public static void irrigateArea(ServerLevel level, BlockPos center, boolean fertilized, int radiusH, int radiusV) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radiusH, -radiusV, -radiusH),
                center.offset(radiusH, radiusV, radiusH))) {
            BlockState target = level.getBlockState(p);
            if (!(target.getBlock() instanceof FarmBlock)) {
                continue;
            }
            if (target.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                level.setBlock(p, target.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
            }
            if (fertilized) {
                tryFertilize(level, p);
            }
        }
    }

// 给耕地上方那株作物顺手催熟一下（随机、少量）
    private static void tryFertilize(ServerLevel level, BlockPos farmland) {
        if (level.random.nextFloat() >= FERTILIZE_CHANCE) {
            return;
        }
        BlockPos cropPos = farmland.above();
        BlockState crop = level.getBlockState(cropPos);
        if (crop.getBlock() instanceof BonemealableBlock bonemealable
                && bonemealable.isValidBonemealTarget(level, cropPos, crop)
                && bonemealable.isBonemealSuccess(level, level.random, cropPos, crop)) {
            bonemealable.performBonemeal(level, level.random, cropPos, crop);
        }
    }

// 喷水期间定期浇一遍落点周围的耕地
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isSpraying(state)) {
            irrigate(level, state, pos);
            level.scheduleTick(pos, this, IRRIGATE_INTERVAL);
        }
    }

// 兜底：区块重载会把计划刻丢掉，靠随机刻把灌溉重新续上（已经在跑的就别再加一条链）
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isSpraying(state) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            irrigate(level, state, pos);
            level.scheduleTick(pos, this, IRRIGATE_INTERVAL);
        }
    }

// 喷水：喷口先来一小簇水花，再沿算出来的抛物线一路撒水滴，形成饱满的水弧。
//
// SPLASH（原版 SplashParticle）在竖直速度为 0 时会采用传进去的水平速度，
// 所以这里给一点朝外的水平分量，水才会有“往外喷”的劲；竖直方向交给它自己（固定上抛 0.1）。
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isSpraying(state)) {
            return;
        }
        Direction open = openDirection(state);
        double sprayX = open.getStepX() * 0.08;
        double sprayZ = open.getStepZ() * 0.08;
        double mx = pos.getX() + 0.5 + open.getStepX() * 0.5;
        double my = pos.getY() + 0.5 + open.getStepY() * 0.5;
        double mz = pos.getZ() + 0.5 + open.getStepZ() * 0.5;

// 喷口：一小簇水花，显得水是满的
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.SPLASH,
                    mx + (random.nextDouble() - 0.5) * 0.3,
                    my + (random.nextDouble() - 0.5) * 0.3,
                    mz + (random.nextDouble() - 0.5) * 0.3,
                    sprayX * 2.0, 0.0, sprayZ * 2.0);
        }

// 水里掺了金坷垃：喷口偶尔冒一点绿色的开心粒子，提示这一片在催熟
        if (state.getValue(FERTILIZED) && random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    mx, my, mz, 0.0, 0.0, 0.0);
        }

// 沿抛物线撒水滴，越往外越稀
        double[] out = new double[SPRAY_MAX_STEPS * 3];
        int steps = traceSpray(level, pos, open, out);
        for (int i = 0; i < steps; i++) {
            if (random.nextFloat() > 0.55F) {
                continue;
            }
            double x = out[i * 3];
            double y = out[i * 3 + 1];
            double z = out[i * 3 + 2];
            level.addParticle(ParticleTypes.SPLASH,
                    x + (random.nextDouble() - 0.5) * 0.12,
                    y + (random.nextDouble() - 0.5) * 0.12,
                    z + (random.nextDouble() - 0.5) * 0.12,
                    sprayX, 0.0, sprayZ);
            if (random.nextFloat() < 0.3F) {
                level.addParticle(ParticleTypes.FALLING_WATER, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
