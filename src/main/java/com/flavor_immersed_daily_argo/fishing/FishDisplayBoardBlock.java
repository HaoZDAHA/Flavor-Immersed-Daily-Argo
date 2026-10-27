package com.flavor_immersed_daily_argo.fishing;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

// 钓鱼展示架
//
// 一块立在方块一侧的木板，手持钓上来的水产右键就能挂上去——挂上去渲染的是这条水产的
// 活体实体模型（静止不动），用来把大货摆出来给人看。空手右键取回来，拆掉方块也会把
// 挂着的那条原样掉出来（称过的重量一并保留）。
//
// 带水平朝向 #FACING：摆放后展示面朝向玩家。模型是贴着方块一侧的一块薄板，
// 因此碰撞箱也按那块板来（随朝向转），不是整格。
//
// 能不能挂由钓鱼手册数据决定：只有 data/<命名空间>/fishing_handbook/*.json 里
// 写明了对应实体（entity 字段）的水产才有实体模型可画，其余一律挂不上去。
public class FishDisplayBoardBlock extends BaseEntityBlock {

    public static final MapCodec<FishDisplayBoardBlock> CODEC = simpleCodec(FishDisplayBoardBlock::new);

// 水平朝向：摆放后展示面朝向玩家
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 木板那一层：贴着方块某一侧、离地 3px 起、高 10px、厚 2px 的薄板，碰撞箱与模型一致
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 3.0D, 14.0D, 16.0D, 13.0D, 16.0D);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0D, 3.0D, 0.0D, 2.0D, 13.0D, 16.0D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 3.0D, 0.0D, 16.0D, 13.0D, 2.0D);
    private static final VoxelShape SHAPE_WEST = Block.box(14.0D, 3.0D, 0.0D, 16.0D, 13.0D, 16.0D);

    public FishDisplayBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

// 指示箱取木板本身，避免整格指示箱把薄板挡在中间点时摸不着
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeOf(state);
    }

// 碰撞箱：那一块薄板，随朝向转
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeOf(state);
    }

    private static VoxelShape shapeOf(BlockState state) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FishDisplayBoardBlockEntity(pos, state);
    }

// 挂鱼 / 取鱼

// 刚空手取下过鱼的玩家。按住右键不放时，客户端每 4 刻会重复触发一次交互：第一次空手取回后
// 鱼已经握到手上，紧接着的下一次就会被当成「要挂鱼」又挂回去，看起来像取不下来。
// 这里记一个保护窗口，窗口内的挂鱼请求直接忽略并把窗口顺延——只要手还按着就一直挂不回去，
// 松手后窗口自然失效，想重新挂上再点一次即可。
    private static final Map<Player, Integer> RECENT_TAKE = new WeakHashMap<>();
// 保护窗口时长（tick），要大于重复交互的 4 刻间隔，松手后才会失效
    private static final int TAKE_GUARD_TICKS = 10;

// 是否还在「刚取下」的保护窗口里；还在窗口内就顺手把窗口顺延
    private static boolean guarded(Player player) {
        Integer last = RECENT_TAKE.get(player);
        if (last == null || player.tickCount - last >= TAKE_GUARD_TICKS) {
            return false;
        }
        RECENT_TAKE.put(player, player.tickCount);
        return true;
    }

// 这个物品能不能挂上去：得在钓鱼手册里有对应实体。没有实体可画的水产（煮熟的、内脏等）
// 一律挂不上，免得架子上挂个空白。
    public static boolean isDisplayable(ItemStack stack) {
        return !stack.isEmpty() && entityOf(stack.getItem()) != null;
    }

// 该物品对应的展示实体 id；手册里没收录或没有实体时返回 null
    @Nullable
    public static ResourceLocation entityOf(Item item) {
        FishingHandbookEntry entry = FishingHandbookData.getInstance().find(BuiltInRegistries.ITEM.getKey(item));
        return entry != null ? entry.entityId : null;
    }

// 手持水产右键挂上去；架子上已经有的话，先把旧的还给玩家再换新的。
//
// 手里拿着东西时一律放行给物品自己用（不落到空手交互上），只有空手才走取鱼逻辑。
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FishDisplayBoardBlockEntity board)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!isDisplayable(stack)) {
            return stack.isEmpty()
                    ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                    : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
// 刚取下来的那条还在手上，这次多半是按住右键的重复触发，别又挂回去
        if (guarded(player)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            ItemStack old = board.take();
            board.place(stack.copyWithCount(1));
            if (!old.isEmpty()) {
                giveOrDrop(player, old);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

// 空手右键：把挂着的那条取回来
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FishDisplayBoardBlockEntity board) || !board.hasFish()) {
            return InteractionResult.PASS;
        }
        RECENT_TAKE.put(player, player.tickCount);
        if (!level.isClientSide) {
            giveOrDrop(player, board.take());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 拆掉展示架时把挂着的那条原样掉出来（含称过的重量）
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof FishDisplayBoardBlockEntity board) {
            ItemStack fish = board.take();
            if (!fish.isEmpty()) {
                Block.popResource(level, pos, fish);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

// 先塞主手，塞不下进背包，再塞不下就丢在脚边
    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        } else if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
