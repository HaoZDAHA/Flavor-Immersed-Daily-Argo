package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

// 化粪发酵桶
//
// 两格方块：和原版床、门一样，两个格子各是一块真方块，破坏与放置都占两格实际空间。
// 放下时，玩家点到的那一格是 Part#LEFT，模型伸出去的那一格是 Part#RIGHT
// （沿 FACING 的逆时针方向，也就是玩家正对桶时的右手边）。
//
// 整个模型跨两格（沿自身 X 轴 -16~16）：LEFT 那一格画模型左半，
// Part#RIGHT 那一格画模型右半，两半拼起来才是完整的一个桶。两半各自占满自己那一格，
// 都是看得见、点得到的真方块，所以瞄准任意一半都能打中，并且一眼就能看出打的是哪一半。
//
// 左半（Part#LEFT）放 / 取粪便，右半（Part#RIGHT）取有机肥料；
// 准星指上去时屏幕上会提示这一半是干什么用的、里面存了多少（见 HerdingHintHud）。
// 槽位、收集与发酵逻辑全部挂在左半的 SepticTankBlockEntity 上，右半只是个「延伸的真方块」，
// 交互时统一转回左半处理。
public class SepticTankBlock extends BaseEntityBlock {

    public static final MapCodec<SepticTankBlock> CODEC = simpleCodec(SepticTankBlock::new);

// 水平朝向：正面朝玩家放置，模型与半格方向都跟着转
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 这是桶的两半中的哪一半
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public SepticTankBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.LEFT));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

// 两半的互相定位

// 从这一半指向另一半的方向。
//
// LEFT 是基准格，另一半在它朝向的逆时针一侧（北→西、东→北、南→东、西→南），
// 与模型在 y 轴不旋转时向 X 负方向铺开的样子对得上。
    private static Direction towardsOther(BlockState state) {
        Direction counterClockWise = state.getValue(FACING).getCounterClockWise();
        return state.getValue(PART) == Part.LEFT ? counterClockWise : counterClockWise.getOpposite();
    }

// 这一半所对应的「左半」坐标——也就是放方块实体、存东西的那一格
    public static BlockPos mainPos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == Part.LEFT ? pos : pos.relative(towardsOther(state));
    }

// 命中的是不是放粪便的左半
    public static boolean isPoopSide(BlockState state) {
        return state.getValue(PART) == Part.LEFT;
    }

// 放置与拆除

// 正面朝向玩家，并把另一半的位置一并占好；另一半被挡住就放不下来
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos other = context.getClickedPos().relative(facing.getCounterClockWise());
        return context.getLevel().getBlockState(other).canBeReplaced(context)
                ? this.defaultBlockState().setValue(FACING, facing)
                : null;
    }

// 主格放好之后立刻补上延伸出去的那一半（和原版床一致）
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockPos other = pos.relative(towardsOther(state));
            level.setBlock(other, state.setValue(PART, Part.RIGHT), 3);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

// 任一半消失，另一半跟着变空气，不会留下半截桶。
//
// 走的是普通的方块替换，所以不会额外掉一份物品——玩家挖哪一半都只掉一个桶
// （掉落物和槽里的东西由方块实体那一半的 #onRemove 负责）。
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction != towardsOther(state)) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }
        return neighborState.is(this) && neighborState.getValue(PART) != state.getValue(PART)
                ? state
                : Blocks.AIR.defaultBlockState();
    }

// 拆桶前的收尾：
//
// 挖右半时右半没有方块实体，先把左半槽里的东西倒出来再挖，
// 免得随后左半被更新成空气时来不及掉东西；
// 创造模式挖左半时顺手清掉右半，免得飞出去的那半还留在原地（原版床的做法）。
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            if (state.getValue(PART) == Part.RIGHT) {
                BlockPos main = mainPos(state, pos);
                if (level.getBlockEntity(main) instanceof SepticTankBlockEntity tank) {
                    Containers.dropContents(level, pos, tank.getInventory());
                    tank.getInventory().clearContent();
                }
            } else if (player.isCreative()) {
                BlockPos other = pos.relative(towardsOther(state));
                BlockState otherState = level.getBlockState(other);
                if (otherState.is(this) && otherState.getValue(PART) == Part.RIGHT) {
                    level.setBlock(other, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, other, Block.getId(otherState));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

// 渲染与方块实体

// 两半各画自己那一半模型，拼起来是一个完整的桶；都是看得见、点得到的真方块
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

// 槽位与逻辑只有一份，放在玩家放置的左半
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.LEFT ? new SepticTankBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(PART) != Part.LEFT) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.SEPTIC_TANK_ENTITY.get(),
                SepticTankBlockEntity::serverTick);
    }

// 交互

// 把手里的一摞待发酵物倒进桶里，打哪一半都行（统一转回左半的方块实体）。
//
// 返回 true 表示命中的确实是化粪桶；客户端只回成功、不改本地数据。
    public static boolean insertPoop(Level level, BlockPos pos, ItemStack hand) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SepticTankBlock)) {
            return false;
        }
        if (!level.isClientSide && level.getBlockEntity(mainPos(state, pos)) instanceof SepticTankBlockEntity tank) {
            tank.insertPoop(hand);
        }
        return true;
    }

// 手里拿着待发酵物怼在桶上：整摞倒进去。
//
// 粪便与高蛋白聚合物都收；打左半、右半都行。潜行时原版会跳过这里、交给物品的 useOn，
// 所以 com.flavor_immersed_daily_argo.item.PoopItem 里也接了同一条路。
//
// 手里拿着别的东西（比如想摆到桶上方的沼气罐）时直接放行给物品去放置或交互：
// 绝不能落到「空手默认交互」上，否则会把桶里的东西吸出来、沼气罐也永远放不上去。
    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModItems.POOP.get()) || stack.is(ModItems.HIGH_PROTEIN_SUBSTANCE.get())) {
// 创造模式下传进来的 stack 可能是副本，改真正手持的那一份才扣得掉
            if (insertPoop(level, pos, player.getItemInHand(hand))) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
// 空手才留给 useWithoutItem（取东西）；拿着物品就放行给物品放置/交互
// （SKIP_DEFAULT_BLOCK_INTERACTION：不消费、也不落到空手默认交互上）
        return stack.isEmpty()
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

// 空手右键：打左半取粪便，打右半取有机肥料。右半没有方块实体，一律转回左半
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos main = mainPos(state, pos);
        if (!level.isClientSide && level.getBlockEntity(main) instanceof SepticTankBlockEntity tank) {
            tank.extractTo(player, state.getValue(PART) == Part.LEFT);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 拆掉时把两半槽里的东西都倒出来（右半没有方块实体，这里只会命左半）
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof SepticTankBlockEntity tank) {
            Containers.dropContents(level, pos, tank.getInventory());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

// 桶的两半
    public enum Part implements StringRepresentable {

// 玩家放置的那一格，模型左半，放 / 取粪便
        LEFT("left"),
// 模型伸出去的那一格，取有机肥料
        RIGHT("right");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
