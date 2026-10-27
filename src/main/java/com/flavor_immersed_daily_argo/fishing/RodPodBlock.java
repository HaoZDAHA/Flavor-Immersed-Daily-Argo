package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.item.ModFishingRodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

// 竿架
//
// 先抛竿、再把南山钓竿架上去替人守竿：附近有玩家时竿架会自己等鱼咬钩，咬钩后提醒最近的玩家；
// 玩家右键取回竿子，若正好有鱼咬钩就在原来的落点重新放钩并顺手进入遛鱼阶段。架上的竿子由
// RodPodRenderer 画在模型上。
public class RodPodBlock extends BaseEntityBlock {


// 水平朝向：摆放后正面朝玩家
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 立柱那一小块
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 15.0D, 11.0D);

    public RodPodBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RodPodBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.ROD_POD_ENTITY.get(), RodPodBlockEntity::serverTick);
    }

// 交互

// 拿着南山钓竿怼上去 → 架竿；架子上有竿子 → 取竿。
//
// 架竿的前提是这一竿已经抛出去了（player.fishing 上挂着鱼钩）：竿架接过的就是
// 玩家正在守的那一竿，鱼钩收回来由竿架代管，落点记在方块实体里，取竿时在原处重开。
// 手上没在钓的话只提示一句，不占用这一竿。
//
// 拿着别的东西时放行给物品自己去用，不落到空手交互上。
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            InteractionResult result = this.useWithItem(state, level, pos, player, hand, hitResult);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return this.useEmpty(state, level, pos, player, hitResult);
    }

    private InteractionResult useWithItem(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level.getBlockEntity(pos) instanceof RodPodBlockEntity pod)) {
            return InteractionResult.PASS;
        }
        if (pod.hasRod()) {
            pod.takeRod(player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
// 创造模式下传进来的 stack 可能是副本，改真正手持的那一份才扣得掉
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof ModFishingRodItem)) {
            return InteractionResult.SUCCESS;
        }
        if (!(player.fishing instanceof ModFishingHook hook) || hook.isFighting()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("msg.flavor_immersed_daily_argo.rod_pod_need_cast"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (pod.placeRod(held, hook.getX(), hook.getY(), hook.getZ())) {
// 这一竿交给竿架：浮漂收回来，水面上的浮漂与鱼线由竿架按记下的落点补画。
// 创造模式也照常从手上扣掉，否则架一根、手上还留一根，能反复刷
            hook.discard();
            held.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 空手右键：取竿（有鱼咬钩的话顺手开遛鱼）
    private InteractionResult useEmpty(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof RodPodBlockEntity pod && pod.hasRod()) {
            pod.takeRod(player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

// 拆掉竿架时把架上的竿子掉出来
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof RodPodBlockEntity pod) {
            pod.dropRod(level);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
