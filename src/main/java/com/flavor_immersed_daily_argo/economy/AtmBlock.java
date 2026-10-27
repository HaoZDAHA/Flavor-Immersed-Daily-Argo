package com.flavor_immersed_daily_argo.economy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

// 自助取款机
//
// 两格高的机器方块：模型从脚下一直长到两格高（0~32），因此方块本身只有一格，
// 但碰撞箱与准星选择体积都做成两格高。有水平朝向，摆放后正面朝玩家，模型随朝向旋转。
//
// 没有方块实体：模型是普通方块模型（models/block/atm.json），走后 cutout 渲染。
// 拿着一张银行卡右键它就会打开银行卡界面；空手右键只提示要先拿卡。
public class AtmBlock extends Block {


// 水平朝向：正面朝玩家，模型随之旋转
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 碰撞箱与选择体积：1×1×2，从脚下顶到两格高
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 32.0, 16.0);

    public AtmBlock(Properties properties) {
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

// 准星轮廓与碰撞箱都是两格高（模型就这么高）
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

// 手持银行卡右键：打开这张卡的界面；没拿卡就提示
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            InteractionResult result = this.useWithItem(state, level, pos, player, hand, hitResult);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return this.useEmpty(state, level, pos, player, hand, hitResult);
    }

    private InteractionResult useWithItem(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof CreditCardItem) {
            if (player instanceof ServerPlayer serverPlayer) {
                open(serverPlayer, hand);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

// 空手右键：提醒要先拿出一张银行卡
        private InteractionResult useEmpty(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.flavor_immersed_daily_argo.atm.need_card"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 把「玩家背包里的哪个槽位放着这张卡」告诉菜单，菜单据此读写卡上的组件
    private static void open(ServerPlayer player, InteractionHand hand) {
        int slot = hand == InteractionHand.MAIN_HAND
                ? player.getInventory().selected
                : Inventory.SLOT_OFFHAND;
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, inv, p) -> new AtmMenu(id, inv, slot, true),
                Component.translatable("gui.flavor_immersed_daily_argo.atm")
        ), buf -> {
            buf.writeVarInt(slot);
            buf.writeBoolean(true);
// 外汇兑换条目随界面一起下发，客户端据此画兑换页
            EconomyConfig.writeEntries(buf);
        });
    }
}
