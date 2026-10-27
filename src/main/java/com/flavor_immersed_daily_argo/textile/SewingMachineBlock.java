package com.flavor_immersed_daily_argo.textile;


import net.minecraftforge.network.NetworkHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import javax.annotation.Nullable;

// 缝纫机
//
// 纺织系列的机器方块：有水平朝向，摆放后正面朝玩家。手持空白丝绸装备右键会把装备放到台面上，
// 空手（或台面上已经有东西时）右键打开缝纫界面，在里面画花纹、着装护甲。
public class SewingMachineBlock extends BaseEntityBlock {


// 水平朝向：放置时正面朝玩家，模型随之旋转
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SewingMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

// 正面朝向玩家
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

// 默认是 INVISIBLE，这里要让方块实体把模型正常画出来
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SewingMachineBlockEntity(pos, state);
    }

// 手持空白丝绸装备右键：把它放到台面上。
//
// 台面上已经有东西（或手里拿的不是丝绸装备）就返回
// InteractionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION，交给默认交互去开界面。
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
        if (!(stack.getItem() instanceof SilkArmorItem)) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(pos) instanceof SewingMachineBlockEntity machine && !machine.hasGarment()) {
            if (!level.isClientSide) {
                machine.setGarment(stack.copyWithCount(1));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

// 空手（或拿着别的东西）右键打开界面
    private InteractionResult useEmpty(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof SewingMachineBlockEntity machine) {
            NetworkHooks.openScreen(serverPlayer, machine, buffer -> buffer.writeBlockPos(pos));
// 界面包已经在 openMenu 里发出去了，这里再补一份台面状态，客户端才拿得到
            PacketDistributor.sendToPlayer(serverPlayer, new SewingMachineSyncPacket(machine.getGarment().copy()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 缝纫机被拆掉时把台面装备和两个槽位里的东西都掉出来
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof SewingMachineBlockEntity machine) {
            if (machine.hasGarment()) {
                Block.popResource(level, pos, machine.getGarment());
            }
            for (int i = 0; i < machine.getInventory().getContainerSize(); i++) {
                ItemStack stack = machine.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos, stack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
