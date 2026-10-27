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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Supplier;

// 可放置的多阶段食物方块（整鸭菜：北京烤鸭 / 卤鸭拼盘 / 手撕烤鸭）。
//
// 物品直接吃恢复大量饱食度；右键地面则摆成这道菜。摆好后每右键一次就是吃掉一口、
// 并推进到下一个阶段的模型；吃到最后一个阶段再右键，连盘子一起收走，而且不掉落任何东西
// （掉落表为空）。
//
// 也可以让右键改成“取出成品”而不是吃掉一口：注册时传进一个 #harvestItem，
// 每右键一次就把一个成品放进玩家背包（背包满了掉在脚下），其余规则完全一致
// ——春秋剑鱼取出腌剑鱼肉，鲅鱼煎饼套餐取出鲅鱼卷煎饼。
//
// 阶段数 = 该菜拥有的模型数：北京烤鸭 / 卤鸭拼盘各 3 个模型，就只用 stage=0 / 1 / 2；
// 手撕烤鸭 4 个模型，用到 stage=0 / 1 / 2 / 3；广式烧鹅 6 个模型，用到 stage=0 ~ 5。
//
// 方块状态是在 super(...) 期间注册的，那时子类字段还没赋值，所以这里用一份带
// 9 个取值的静态 #STAGE 给所有菜共用：模型少的菜走不到后面的阶段，
// 对应模型文件里那些条目只是把状态空间补全，避免出现没有模型的方块状态。
public class MultiStageFoodBlock extends Block implements PlaceableFood {

// 阶段进度：0 是完整的菜，数值越大剩得越少（蒸帝王蟹有 9 个模型，所以最多到 8）
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 8);

// 水平朝向：摆下时正面朝玩家，之后可以用旋转类工具转向
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 只占下半格，盘沿之外仍然能走人
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 6.0, 15.0);

// 该菜最末的阶段序号（= 模型数 - 1），走到它就说明只剩最后一口
    private final int lastStage;

// 每吃掉一口恢复的饥饿值与饱和度系数
    private final int biteNutrition;
    private final float biteSaturation;

// 右键取出的成品；为 null 时右键就是吃一口。
//
// 用 Supplier 而不是直接持有物品，是为了避免方块注册与物品注册互相触发类初始化。
    private final Supplier<Item> harvestItem;

// 最后一份换成别的成品（例如蒸海鲜最后取出蟹黄）；为 null 时最后一份仍是 #harvestItem
    private final Supplier<Item> lastHarvestItem;

// 最后一份成品的数量
    private final int lastHarvestCount;

// 每右键一次吃掉一口的可放置食物
    public MultiStageFoodBlock(int lastStage, int biteNutrition, float biteSaturation, Properties properties) {
        this(lastStage, biteNutrition, biteSaturation, null, null, 1, properties);
    }

// 每右键一次取出一个成品、而不是吃掉一口的可放置食物
    public MultiStageFoodBlock(int lastStage, int biteNutrition, float biteSaturation,
                               Supplier<Item> harvestItem, Properties properties) {
        this(lastStage, biteNutrition, biteSaturation, harvestItem, null, 1, properties);
    }

// 每次右键取出成品，最后一份换成另一种成品若干（蒸海鲜最后一份取蟹黄）
    public MultiStageFoodBlock(int lastStage, int biteNutrition, float biteSaturation,
                               Supplier<Item> harvestItem, Supplier<Item> lastHarvestItem, int lastHarvestCount,
                               Properties properties) {
        super(properties);
        this.lastStage = lastStage;
        this.biteNutrition = biteNutrition;
        this.biteSaturation = biteSaturation;
        this.harvestItem = harvestItem;
        this.lastHarvestItem = lastHarvestItem;
        this.lastHarvestCount = lastHarvestCount;
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        int stage = state.getValue(STAGE);
        if (stage < this.lastStage) {
            level.setBlock(pos, state.setValue(STAGE, stage + 1), Block.UPDATE_ALL);
        } else {
// 最后一口：吃完连盘子一起收走，不掉落任何东西
            level.removeBlock(pos, false);
        }

        if (this.harvestItem != null) {
// 取出成品：背包放得下就进背包，放不下直接掉在脚下；最后一份可能换成别的成品
            ItemStack product = stage == this.lastStage && this.lastHarvestItem != null
                    ? new ItemStack(this.lastHarvestItem.get(), this.lastHarvestCount)
                    : new ItemStack(this.harvestItem.get());
            if (!player.getInventory().add(product)) {
                player.drop(product, false);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6F, 1.2F);
        } else {
            player.getFoodData().eat(this.biteNutrition, this.biteSaturation);
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        this.addPlaceableTooltip(tooltip);
    }
}
