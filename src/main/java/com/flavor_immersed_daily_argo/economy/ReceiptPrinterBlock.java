package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

// 小票打印机。
//
// 立在路边的一台打印机。空手右键打开界面，界面里是这台机器「今天的菜单」——
// 从全模组的食物里抽出的 40 种，只展示不能拿取。机器会时不时切到出单态（state=2），
// 此时模型上会多吐出一张小票，周围同时刷新最多 3 个外卖小哥（见
// com.flavor_immersed_daily_argo.entity.DeliveryRider）。小哥都走了就回到常态。
//
// 碰撞箱跟着 FACING 一起转，贴合模型（宽 10、深 13、高 7 像素）。
public class ReceiptPrinterBlock extends BaseEntityBlock {

    public static final MapCodec<ReceiptPrinterBlock> CODEC = simpleCodec(ReceiptPrinterBlock::new);

// 水平朝向：放置时正面朝玩家，碰撞箱与模型随之旋转
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 机器状态：1 = 常态；2 = 出单态（多一张小票，周围刷新外卖小哥）
    public static final IntegerProperty STATE = IntegerProperty.create("state", 1, 2);

// 南北朝向（模型正面朝北）：模型 x 3-13、y 0-7、z 2-15
    private static final VoxelShape SHAPE_NS = Block.box(3.0D, 0.0D, 2.0D, 13.0D, 7.0D, 15.0D);
// 东西朝向：把上面那个箱子转 90°，x/z 互换
    private static final VoxelShape SHAPE_EW = Block.box(2.0D, 0.0D, 3.0D, 15.0D, 7.0D, 13.0D);

    public ReceiptPrinterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATE, 1));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE);
    }

// 正面朝向玩家
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(STATE, 1);
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
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeOf(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeOf(state);
    }

    private static VoxelShape shapeOf(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

// 默认是 INVISIBLE，这里要让方块实体把模型正常画出来
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReceiptPrinterBlockEntity(pos, state);
    }

// 只在服务端跑：每日菜单刷新、出单态判定与外卖小哥维护
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.RECEIPT_PRINTER_ENTITY.get(),
                        ReceiptPrinterBlockEntity::serverTick);
    }

// 空手右键打开界面；手里拿着东西时仍走放置逻辑
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ReceiptPrinterBlockEntity printer) {
            serverPlayer.openMenu(printer, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 被拆掉时把周围还没走的小哥一并清掉（菜单只是展示，没有东西可掉）
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ReceiptPrinterBlockEntity printer) {
            printer.discardRiders();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
