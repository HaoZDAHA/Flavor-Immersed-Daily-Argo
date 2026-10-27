package com.flavor_immersed_daily_argo.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

// 旋转水管（管网末端的旋转洒水器）。
//
// 接到水以后播放 Blockbench 导出的 round 动画：旋转臂（bone2）绕竖轴
// 每 2 秒转一圈。模型外形超出单方块能表达的动画范围，所以不参与区块网格烘焙，整体交给
// RotatingWaterPipeRenderer 绘制（RenderShape#ENTITYBLOCK_ANIMATED）。
//
// 两种装法（#FACE）：
//
// 平放（FLOOR，点在方块顶面上）：模型就按 Blockbench 里的竖姿态立在地上，
// 旋转臂绕竖轴转，水花朝四外甩成一圈；这是导出文件 rotating_water_pipe.java 的原姿态。
// 贴墙（WALL，点在其他面上）：整个模型横过来贴在点到的那个竖直侧面上，
// 转轴随之变成水平并朝外（#FACING），水花在竖直平面里画圆；
// 对应导出文件 rotating_water_pipe2.java。
//
// 渲染器、碰撞箱与水花都按同一套姿态换算，于是看到的水一定跟着模型走。
//
// 喷洒时从两根镜像臂端各沿甩出方向拉一条长水痕、并跟着臂一起转：渲染器与粒子共用
// #phaseDegrees 这一个相位公式，保证看到的水跟着模型走。
//
// 管网接入、隔一格探测、灌溉见基类 WaterSprinklerBlock。
public class RotatingWaterPipeBlock extends WaterSprinklerBlock implements EntityBlock {


// 朝外方向（背离墙面）：贴墙时是转轴指向，平放时只用来定扇形朝向（旋转喷洒其实四面一样）
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 转一圈的时长（tick）：导出动画 round 长 2 秒
    public static final float TURN_TICKS = 2.0F * 20.0F;

// 喷嘴离转轴的水平距离（格）：模型臂端约 6 像素
    private static final double NOZZLE_RADIUS = 6.0D / 16.0D;

// 喷嘴离地的高度（格）：模型臂端约 y=15 像素（模型地面在 y=24 像素）
    private static final double NOZZLE_HEIGHT = (24.0D - 15.0D) / 16.0D;

// 贴墙时喷嘴沿朝外方向离方块中心的距离（格）：臂略离墙面
    private static final double NOZZLE_OFFSET = NOZZLE_HEIGHT - 0.5D;

// 每根臂甩出的水痕长度（格）与连成水痕的水花数。
//
// 原版的 animateTick 是「每刻在玩家周围抽 667 个坐标」抽着调的，同一格平均
// 每秒只摊到一两次；要是只在臂端撒一小撮，看着就只是一个点。这里改成一次沿甩出方向
// 拉一条长水痕（越往外越稀、越快），两臂就是两条被甩出去的长水痕，转起来才像在洒水。
    private static final double STREAK_LENGTH = 1.3D;

// 一条水痕用几颗水花连成
    private static final int STREAK_POINTS = 14;

// 平放（立在地上）时的碰撞箱：底板 + 立柱 + 旋转臂，臂端半径 6 像素、整体 11 像素高
    private static final VoxelShape SHAPE_FLOOR = Block.box(2.0, 0.0, 2.0, 14.0, 11.0, 14.0);

// 碰撞箱（贴墙模型）：朝北那套整体包围盒，其余朝向由它绕竖轴转到各自墙面
    private static final VoxelShape SHAPE_NORTH = Block.box(2.0, 2.0, 5.0, 14.0, 14.0, 16.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 2.0, 2.0, 11.0, 14.0, 14.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 11.0);
    private static final VoxelShape SHAPE_WEST = Block.box(5.0, 2.0, 2.0, 16.0, 14.0, 14.0);

    public RotatingWaterPipeBlock(Properties properties) {
        super(properties);
// FACE 的枚举默认值是 FLOOR，这里显式写回 WALL：新老存档里没写过 FACE 的方块仍是贴墙那套
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FILLED, false)
                .setValue(FERTILIZED, false)
                .setValue(FACE, AttachFace.WALL)
                .setValue(FACING, Direction.NORTH));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

// 摆放时定姿态：点在方块顶面上就平放在地上（模型立着，水花甩一圈）；
// 点在其他面上就贴墙——贴在点到的那个竖直侧面上、转轴朝外，朝向取那个侧面
// （点天花板/地面侧面时退回玩家面向）。
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Direction facing = face.getAxis().isHorizontal()
                ? face
                : horizontalFacingFor(context);
        return this.defaultBlockState()
                .setValue(FACE, attachFaceFor(context))
                .setValue(FACING, facing);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

// 模型整体交给方块实体渲染器画（要播放 round 动画），本格自身不进区块网格
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape collisionShape(BlockState state) {
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            return SHAPE_FLOOR;
        }
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            default -> SHAPE_WEST;
        };
    }

// 旋转臂当前相位（度，0..360）。渲染器与粒子共用这一个公式，
// 于是水花必然跟着臂转、多台洒水器相位也天然对齐。
    public static float phaseDegrees(Level level) {
        return (level.getGameTime() % (long) TURN_TICKS) / TURN_TICKS * 360.0F;
    }

    @Override
    protected void irrigateAt(ServerLevel level, BlockState state, BlockPos pos) {
// 旋转喷洒：以本格为中心浇一圈
        WaterPipeBlock.irrigateArea(level, pos, state.getValue(FERTILIZED), 4, 2);
    }

    @Override
    protected void emitSpray(Level level, BlockPos pos, BlockState state, RandomSource random) {
// 相位只算一次，cos/sin 两根臂端共用：循环里不再做三角函数
        double rad = Math.toRadians(phaseDegrees(level));
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            sprayFromFloor(level, pos, state, random, cos, sin);
        } else {
            sprayFromWall(level, pos, state, random, cos, sin);
        }
    }

// 平放：转轴竖直，两根臂端在水平面里画圆，水花从臂端朝四外甩出去再落到地上
    private void sprayFromFloor(Level level, BlockPos pos, BlockState state, RandomSource random,
                                double cos, double sin) {
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + NOZZLE_HEIGHT;
        double centerZ = pos.getZ() + 0.5D;
        for (int i = 0; i < 2; i++) {
            double dir = i == 0 ? 1.0D : -1.0D;
// 甩出方向与臂端位置同源，都从相位角推出来：水必然跟着臂走。
// 模型空间翻过 x/y（渲染器的 scale(-1,-1,1)）后再叠加 bone2 的 yRot = -相位，
// 落到世界上臂端就是 (0.5 ∓ R·cos, 0.5 ± R·sin)，所以 x 这一项要取负号。
            double nx = -cos * dir;
            double nz = sin * dir;
// 从臂端沿甩出方向拉一条长水痕，两臂就是两条被甩出去的水线
            flingStreak(level, random,
                    centerX + nx * NOZZLE_RADIUS, centerY, centerZ + nz * NOZZLE_RADIUS,
                    nx, 0.05D, nz);
            if (random.nextFloat() < 0.5F) {
                level.addParticle(ParticleTypes.FALLING_WATER,
                        centerX + nx * NOZZLE_RADIUS, centerY - 0.05D, centerZ + nz * NOZZLE_RADIUS,
                        0.0D, 0.0D, 0.0D);
            }
        }
        happyVillager(level, state, random, centerX, centerY, centerZ);
    }

// 贴墙：转轴水平朝外，两根臂端在竖直平面里画圆，水花从臂端朝外下方甩
    private void sprayFromWall(Level level, BlockPos pos, BlockState state, RandomSource random,
                               double cos, double sin) {
        Direction facing = state.getValue(FACING);
// 转轴水平朝外：臂端在「竖直平面」里画圆，水平那根轴取 facing 的右旋方向
        Direction side = facing.getClockWise();
        double centerX = pos.getX() + 0.5D + facing.getStepX() * NOZZLE_OFFSET;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D + facing.getStepZ() * NOZZLE_OFFSET;
        for (int i = 0; i < 2; i++) {
            double dir = i == 0 ? 1.0D : -1.0D;
            double along = cos * NOZZLE_RADIUS * dir;
            double vert = -sin * NOZZLE_RADIUS * dir;
            double ox = side.getStepX() * along;
            double oz = side.getStepZ() * along;
// 甩出方向 = 臂端朝外的径向（水平那根轴 + 竖直），再带一点背离墙面的推力
            flingStreak(level, random,
                    centerX + ox, centerY + vert, centerZ + oz,
                    side.getStepX() * cos * dir + facing.getStepX() * 0.25D,
                    -sin * dir,
                    side.getStepZ() * cos * dir + facing.getStepZ() * 0.25D);
            if (random.nextFloat() < 0.3F) {
                level.addParticle(ParticleTypes.FALLING_WATER,
                        centerX + ox, centerY + vert - 0.1D, centerZ + oz, 0.0D, 0.0D, 0.0D);
            }
        }
        happyVillager(level, state, random, centerX, centerY, centerZ);
    }

// 从喷嘴沿甩出方向拉一条长水痕：越往外水花越稀、速度越大，看起来像被臂甩出去的水线。
//
// dx：甩出方向（单位向量）
    private static void flingStreak(Level level, RandomSource random,
                                    double x, double y, double z, double dx, double dy, double dz) {
        for (int n = 0; n < STREAK_POINTS; n++) {
            double t = (n + random.nextDouble() * 0.7D) / STREAK_POINTS;
            double dist = t * STREAK_LENGTH;
            double speed = 0.06D + t * 0.25D;
            level.addParticle(ParticleTypes.SPLASH,
                    x + dx * dist, y + dy * dist, z + dz * dist,
                    dx * speed, dy * speed, dz * speed);
        }
    }

// 水里掺了金坷垃：偶尔冒一点绿色的开心粒子，提示这一片在催熟
    private static void happyVillager(Level level, BlockState state, RandomSource random,
                                      double x, double y, double z) {
        if (state.getValue(FERTILIZED) && random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RotatingWaterPipeBlockEntity(pos, state);
    }
}
