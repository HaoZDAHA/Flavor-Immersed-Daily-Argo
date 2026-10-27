package com.flavor_immersed_daily_argo.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

// 由方块实体渲染器绘制的可放置食物（铁锅炖大鹅 / 全驴宴）。
//
// 和 MultiStageFoodBlock 的玩法完全一致：摆好后每右键一次就吃掉一口、并把
// #STAGE 往前推一格；推到 lastStage 后再右键，整份菜连锅带桌一并消失，
// 而且不掉落任何东西（掉落表为空）。
//
// 区别在于模型：这两种食物的 Blockbench 模型都带多段嵌套旋转，导出成了实体模型，
// 没法写成方块模型 JSON，所以这里不参与区块网格烘焙（RenderShape#ENTITYBLOCK_ANIMATED），
// 改由各自的 BlockEntityRenderer 按原始尺寸绘制。方块实体本身不存数据，
// 渲染器只读方块状态里的 #STAGE / #FACING。
//
// #STAGE 的取值范围对两种食物都够用：铁锅炖大鹅 12 个模型用到 0~11，
// 全驴宴只用一个模型、纯靠这个数字记“吃了几口”，20 口吃到 0~19。
public class EntityFoodDishBlock extends Block implements EntityBlock, PlaceableFood {

// 阶段 / 口数进度：0 是完整的一份，数值越大剩得越少
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 19);

// 水平朝向：摆下时正面朝玩家，之后可以用旋转类工具转向
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 该食物最末的阶段序号（= 模型数 - 1，或总共能吃几口 - 1）
    private final int lastStage;

// 每吃掉一口恢复的饥饿值与饱和度系数
    private final int biteNutrition;
    private final float biteSaturation;

// 只占住模型真正挡住的那一块，盘沿之外仍然能走人
    private final VoxelShape shape;

// 方块实体的构造器（两种食物各有一种方块实体，这里由注册时传进来）
    private final BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFactory;

    public EntityFoodDishBlock(int lastStage, int biteNutrition, float biteSaturation, VoxelShape shape,
                               BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFactory,
                               Properties properties) {
        super(properties);
        this.lastStage = lastStage;
        this.biteNutrition = biteNutrition;
        this.biteSaturation = biteSaturation;
        this.shape = shape;
        this.blockEntityFactory = blockEntityFactory;
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
// 正面朝向玩家（玩家朝北摆放，方块正面朝南）
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

// 模型整体交给方块实体渲染器画，方块自身不进区块网格
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.blockEntityFactory.apply(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        int stage = state.getValue(STAGE);
        if (stage < this.lastStage) {
            level.setBlock(pos, state.setValue(STAGE, stage + 1), Block.UPDATE_ALL);
        } else {
// 最后一口：吃完连盛器一起收走，不掉落任何东西
            level.removeBlock(pos, false);
        }

        player.getFoodData().eat(this.biteNutrition, this.biteSaturation);
        level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        this.addPlaceableTooltip(tooltip);
    }
}
