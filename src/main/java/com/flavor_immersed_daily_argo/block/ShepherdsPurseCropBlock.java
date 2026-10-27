package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// 荠菜作物。贴合原版 CropBlock，另外参考父模组 FIDCropBlock 的右键收获：
// 成熟后空手 / 持物右键，掉落 3 棵荠菜 + 1 颗种子，并回到幼苗阶段，不必把整株打掉。
//
// 种子与产物都用 ModItems 的延迟注册，不在构造期取实例，
// 因此不依赖父模组，也不会和注册顺序耦合。
public class ShepherdsPurseCropBlock extends CropBlock {

// 成熟阶段：贴图给了 0~4 五个模型
    public static final int MAX_AGE = 4;

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(4, 0, 4, 12, 4, 12),
            Block.box(3, 0, 3, 13, 6, 13),
            Block.box(2, 0, 2, 14, 9, 14),
            Block.box(0, 0, 0, 16, 12, 16)
    };

    public ShepherdsPurseCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.SHEPHERDS_PURSE_SEEDS.get();
    }

// 成熟后收获的产物（供镰刀 / 无人机等外部收割逻辑使用）
    public ItemLike getCropItem() {
        return ModItems.SHEPHERDS_PURSE.get();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[Math.min(this.getAge(state), MAX_AGE)];
    }

// 右键收获：一次 3 棵荠菜 + 1 颗种子，作物回到幼苗阶段
    public void harvest(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide) {
            Block.popResource(level, pos, new ItemStack(ModItems.SHEPHERDS_PURSE.get(), 3));
            Block.popResource(level, pos, new ItemStack(ModItems.SHEPHERDS_PURSE_SEEDS.get()));
// 2001：播放方块破坏音效并生成破坏粒子
            level.levelEvent(null, 2001, pos, Block.getId(state));
            level.setBlock(pos, this.getStateForAge(0), Block.UPDATE_CLIENTS);
        }
    }

// 覆盖 useItemOn，保证手持任意物品右键成熟作物也进入收获，
// 不会被种子等物品自身的 useOn 抢占。
    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (this.isMaxAge(state)) {
            this.harvest(state, level, pos);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (this.isMaxAge(state)) {
            this.harvest(state, level, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
