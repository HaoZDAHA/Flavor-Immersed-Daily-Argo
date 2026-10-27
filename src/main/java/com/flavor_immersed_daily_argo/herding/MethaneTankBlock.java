package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
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

// 沼气罐
//
// 只有 facing（东南西北四个朝向）一个状态，模型跨两格的部分全靠 blockstate 的
// y 旋转解决：模型本身沿自身 +Z 从 0 铺到 30（约两格），摆到世界上时朝 facing
// 的逆时针一侧伸出去，正好盖在化粪桶伸出去的那一半上方。
//
// 功能很简单：化粪桶每发酵成功一次，就往它上方的沼气罐里塞一份加压沼气；罐子满了
// 就攒着。空手右键取出一摞加压沼气，准星指上去时屏幕上显示里面还剩多少（见
// HerdingHintHud）。
public class MethaneTankBlock extends BaseEntityBlock {

    public static final MapCodec<MethaneTankBlock> CODEC = simpleCodec(MethaneTankBlock::new);

// 水平朝向：决定模型往哪边伸出去
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public MethaneTankBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

// 摆在化粪桶上方时直接沿用它的朝向，这样模型伸出去的方向和桶对得上；否则正面朝玩家。
//
// 注意判断的是被点击的那一格（也就是新罐子脚下的粪桶），不是它下面那一格。
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState clicked = context.getLevel().getBlockState(context.getClickedPos());
        if (clicked.getBlock() instanceof SepticTankBlock) {
            return this.defaultBlockState().setValue(FACING, clicked.getValue(SepticTankBlock.FACING));
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

// 默认是 INVISIBLE，这里要让方块实体把模型正常画出来
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MethaneTankBlockEntity(pos, state);
    }

// 空手右键：取出一摞加压沼气
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MethaneTankBlockEntity tank) {
            int taken = tank.take(new ItemStack(ModItems.MARSH_GAS.get()).getMaxStackSize());
            if (taken > 0) {
                ItemStack stack = new ItemStack(ModItems.MARSH_GAS.get(), taken);
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 拆掉时把罐里的沼气都倒出来
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof MethaneTankBlockEntity tank) {
            int gas = tank.gas();
            if (gas > 0) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        new ItemStack(ModItems.MARSH_GAS.get(), gas));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}