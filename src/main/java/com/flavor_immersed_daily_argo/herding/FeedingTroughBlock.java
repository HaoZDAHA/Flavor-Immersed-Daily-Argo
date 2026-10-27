package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// 牧场食槽
//
// 放置后是空的阶段 1，里面只有一格槽位：可以放饲料（本模组的四种饲料，或者原版的小麦、胡萝卜），
// 也可以放 flavor_immersed_daily:tidywater（纯净水）。槽里有东西后模型换成对应的
// food / water 阶段，被吃空又变回空的阶段。
//
// 具体的进食逻辑在 FeedingTroughBlockEntity：附近的动物哪项数值掉下去之后，
// 自己走过来吃掉一份，补上对应的数值。
public class FeedingTroughBlock extends BaseEntityBlock {

    public static final MapCodec<FeedingTroughBlock> CODEC = simpleCodec(FeedingTroughBlock::new);

// 槽里装的是饲料还是纯净水，决定用哪一套模型
    public static final EnumProperty<TroughContent> CONTENT =
            EnumProperty.create("content", TroughContent.class);

// 水平朝向：放置时正面朝玩家，模型随之旋转
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 纯净水（flavor_immersed_daily 提供）。按注册名运行时查找，不产生编译期依赖
    private static final ResourceLocation TIDY_WATER =
            ResourceLocation.fromNamespaceAndPath("flavor_immersed_daily", "tidywater");

    public FeedingTroughBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(CONTENT, TroughContent.EMPTY)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTENT, FACING);
    }

// 正面朝向玩家
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeedingTroughBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.FEEDING_TROUGH_ENTITY.get(),
                        FeedingTroughBlockEntity::serverTick);
    }

// 随机刻到了才去扫一遍周围的动物。
//
// 随机刻本身就是概率触发：默认 randomTickSpeed=3 时，一个方块平均约 68 秒才轮得到一次，
// 比每 20 刻（1 秒）扫一次便宜两个数量级，而且放多少个食槽都不会互相拖累。
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            trough.serverRandomTick();
        }
    }

// 这个物品放进槽里算哪一类内容；放不进去就返回 null。
//
// 纯净水必须排在饲料前面判断：万一以后有同名同 id 的饲料，水优先。
    @Nullable
    public static TroughContent contentFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        Item tidyWater = BuiltInRegistries.ITEM.getOptional(TIDY_WATER).orElse(null);
        if (tidyWater != null && stack.is(tidyWater)) {
            return TroughContent.WATER;
        }
        if (stack.is(ModItems.FEED_GRASS.get())
                || stack.is(ModItems.FEED_SEED.get())
                || stack.is(ModItems.FEED_AQUATIC.get())
                || stack.is(ModItems.FEED_MEAT.get())
                || stack.is(Items.WHEAT)
                || stack.is(Items.CARROT)) {
            return TroughContent.FOOD;
        }
        return null;
    }

// 手里拿着饲料 / 纯净水右键：整摞倒进槽里
    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (contentFor(stack) == null || !(level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
// 创造模式下传进来的 stack 可能是副本，改真正手持的那一份才扣得掉
            trough.insert(player.getItemInHand(hand));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

// 空手右键：把槽里的东西取回来
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            trough.extractTo(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 食槽被拆掉时把槽里的东西掉出来
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            Containers.dropContents(level, pos, trough.getInventory());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

// 食槽的三个阶段
    public enum TroughContent implements StringRepresentable {
// 空槽
        EMPTY("empty"),
// 装着饲料
        FOOD("food"),
// 装着纯净水
        WATER("water");

        private final String name;

        TroughContent(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
