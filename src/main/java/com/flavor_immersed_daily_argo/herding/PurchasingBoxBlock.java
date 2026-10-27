package com.flavor_immersed_daily_argo.herding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// 收购箱
//
// 摆在农贸市场 / 牧场旁边的收货箱：里面是 27 格容器（与原版单箱一样大），
// 把经济产物放进去，点界面里的「出售」就按今天的收购价换成纸币。
// 模型就是个普通方块模型，没有朝向。
public class PurchasingBoxBlock extends BaseEntityBlock {

    public static final MapCodec<PurchasingBoxBlock> CODEC = simpleCodec(PurchasingBoxBlock::new);

    public PurchasingBoxBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PurchasingBoxBlockEntity(pos, state);
    }

// 空手右键打开收购箱界面
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof PurchasingBoxBlockEntity box) {
            serverPlayer.openMenu(box, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 被拆掉时把箱子里的东西掉出来
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof PurchasingBoxBlockEntity box) {
            for (int i = 0; i < box.getInventory().getContainerSize(); i++) {
                ItemStack stack = box.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos, stack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
