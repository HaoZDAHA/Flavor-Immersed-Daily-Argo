package com.flavor_immersed_daily_argo.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

// 扇形水管（管网末端的扇形洒水器）。
//
// 形态是静态方块模型（底板 + 立柱 + 顶部横杆），不进方块实体，由 blockstate 按
// #FACE / #FACING 选模型并旋转。接到水以后朝正前方水平区域呈扇形喷水；
// 灌溉也把浇灌方块朝前方挪一挪，只浇前面那一片。
//
// 两种装法：点在方块顶面上就平放在地上（FLOOR，用竖着的
// sector_water_pipe 模型）；点在其他面上就贴墙（WALL，用横过来的
// sector_water_pipe_wall 模型），横杆始终与 #FACING 垂直，扇形朝正面喷。
//
// 管网接入、隔一格探测、灌溉见基类 WaterSprinklerBlock。
public class SectorWaterPipeBlock extends WaterSprinklerBlock {

    public static final MapCodec<SectorWaterPipeBlock> CODEC = simpleCodec(SectorWaterPipeBlock::new);

// 正面朝向：朝这一侧喷水
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 前方扇形张角（度）
    public static final float FAN_ANGLE_DEGREES = 60.0F;

// 扇形喷洒最远半径（格）
    private static final double FAN_RADIUS = 3.0D;

// 贴墙时的喷洒高度（格）：侧面那套模型的横杆在 y=8 像素上下
    private static final double SPRAY_HEIGHT_WALL = 0.6D;

// 平放时的喷洒高度（格）：竖着那套模型的横杆在 y=9..11 像素，取中间 10 像素
    private static final double SPRAY_HEIGHT_FLOOR = 10.0D / 16.0D;

// 碰撞箱（平放模型）：横杆沿 X 时整体是 3..13 / 5..11，高 11 像素
    private static final VoxelShape SHAPE_FLOOR_NS = Block.box(3.0, 0.0, 5.0, 13.0, 11.0, 11.0);
    private static final VoxelShape SHAPE_FLOOR_EW = Block.box(5.0, 0.0, 3.0, 11.0, 11.0, 13.0);

// 碰撞箱（贴墙模型）：朝北那套的整体包围盒，其余朝向由它绕竖轴转到各自的墙面上
    private static final VoxelShape SHAPE_NORTH = Block.box(3.0, 5.0, 5.0, 13.0, 11.0, 16.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 5.0, 3.0, 11.0, 11.0, 13.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(3.0, 5.0, 0.0, 13.0, 11.0, 11.0);
    private static final VoxelShape SHAPE_WEST = Block.box(5.0, 5.0, 3.0, 16.0, 11.0, 13.0);

    public SectorWaterPipeBlock(Properties properties) {
        super(properties);
// FACE 的枚举默认值是 FLOOR，这里显式写回 WALL：新老存档里没写过 FACE 的方块仍是贴墙那套
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FILLED, false)
                .setValue(FERTILIZED, false)
                .setValue(FACE, AttachFace.WALL)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends SectorWaterPipeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

// 摆放时定姿态：点在方块顶面上就平放在地上；点在其他面上就贴在点到的那个竖直侧面上
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
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape collisionShape(BlockState state) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(FACE) == AttachFace.FLOOR) {
// 平放：横杆与正面垂直，南北向时杆沿 X，东西向时杆沿 Z
            return facing.getAxis() == Direction.Axis.Z ? SHAPE_FLOOR_NS : SHAPE_FLOOR_EW;
        }
        return switch (facing) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            default -> SHAPE_WEST;
        };
    }

    @Override
    protected void irrigateAt(ServerLevel level, BlockState state, BlockPos pos) {
// 只浇前方：把浇灌方块朝 FACING 前移两格，覆盖扇形区域
        WaterPipeBlock.irrigateArea(level,
                pos.relative(state.getValue(FACING), 2), state.getValue(FERTILIZED), 3, 2);
    }

    @Override
    protected void emitSpray(Level level, BlockPos pos, BlockState state, RandomSource random) {
        Direction facing = state.getValue(FACING);
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
// 水从横杆上出来：平放与贴墙的横杆高度不一样，各取各的
        double y = pos.getY()
                + (state.getValue(FACE) == AttachFace.FLOOR ? SPRAY_HEIGHT_FLOOR : SPRAY_HEIGHT_WALL);
// 本刻在前方扇形里撒几颗水珠
        for (int i = 0; i < 4; i++) {
            double spread = Math.toRadians((random.nextFloat() - 0.5F) * FAN_ANGLE_DEGREES);
            double c = Math.cos(spread);
            double s = Math.sin(spread);
            double vx = facing.getStepX() * c - facing.getStepZ() * s;
            double vz = facing.getStepX() * s + facing.getStepZ() * c;
            double r = 0.5D + random.nextDouble() * FAN_RADIUS;
            level.addParticle(ParticleTypes.SPLASH,
                    centerX + vx * r, y, centerZ + vz * r,
                    vx * 0.06D, 0.02D, vz * 0.06D);
            if (random.nextFloat() < 0.25F) {
                level.addParticle(ParticleTypes.FALLING_WATER,
                        centerX + vx * r, y, centerZ + vz * r, 0.0D, 0.0D, 0.0D);
            }
        }
// 水里掺了金坷垃：偶尔冒一点绿色的开心粒子
        if (state.getValue(FERTILIZED) && random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER, centerX, y, centerZ, 0.0D, 0.0D, 0.0D);
        }
    }
}