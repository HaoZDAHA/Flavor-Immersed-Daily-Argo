package com.flavor_immersed_daily_argo.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

// 果树叶：能结出果实的树叶，形制照搬父模组 flavor_immersed_daily 的对应方块。
//
// 行为：自然/手放的树叶都会按随机刻累积 #MATURITY，攒满 #MAX_MATURITY
// 后置 #FRUITING 为真（模型切换成「结果的树叶」），并有一定概率在正下方长出
// 悬挂果实方块。徒手右键可直接摘下果实，果实会被取走、树叶复位成未挂果、下方的悬挂果实一并消失。
// 手拿骨粉右键则单独催熟这一块树叶。
public class FruitingLeavesBlock extends LeavesBlock {

// 是否已经挂果：模型据此在「普通树叶」与「结果的树叶」之间切换
    public static final BooleanProperty FRUITING = BooleanProperty.create("fruiting");

// 果实成熟进度 0..4；攒满后 #FRUITING 置真
    public static final IntegerProperty MATURITY = IntegerProperty.create("maturity", 0, 4);

    public static final int MAX_MATURITY = 4;

// 自然随机刻推进一级成熟度的概率（与父模组 Config.naturalFruitMaturityChance 一致）
    private static final double NATURAL_MATURITY_CHANCE = 0.33D;

// 骨粉催熟一级的概率（与父模组 Config.boneMealFruitMaturityChance 一致），树干催熟也用它
    public static final double BONE_MEAL_MATURITY_CHANCE = 0.35D;

// 刚挂果时在树叶正下方长出悬挂果实的概率（与父模组 Config.hangingFruitChance 一致）
    private static final double HANGING_FRUIT_CHANCE = 0.70D;

// 从树叶往下穿过连续树叶找悬挂果实的最多层数
    private static final int HANGING_SEARCH_DEPTH = 12;

    private final Supplier<Item> fruitItem;
    private final Supplier<? extends Block> hangingFruit;

// 这块果树叶结的果实物品，供悬挂果实/树干交互回找时比对
    public Item getFruitItem() {
        return fruitItem.get();
    }

    public FruitingLeavesBlock(Supplier<Item> fruitItem, Supplier<? extends Block> hangingFruit,
                               BlockBehaviour.Properties properties) {
        super(properties);
        this.fruitItem = fruitItem;
        this.hangingFruit = hangingFruit;
        registerDefaultState(stateDefinition.any()
                .setValue(DISTANCE, 1)
                .setValue(PERSISTENT, false)
                .setValue(WATERLOGGED, false)
                .setValue(FRUITING, false)
                .setValue(MATURITY, 0));
    }

    @Override
    public MapCodec<? extends FruitingLeavesBlock> codec() {
        return simpleCodec(properties -> new FruitingLeavesBlock(fruitItem, hangingFruit, properties));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT, WATERLOGGED, FRUITING, MATURITY);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
// 自然树叶要随机刻做距离/枯萎检查；没挂果的树叶（含玩家手放的 persistent 树叶）也要随机刻慢慢结果
        return !state.getValue(PERSISTENT) || !state.getValue(FRUITING);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
// 只有自然生成（非 persistent）的树叶参与原版距离/枯萎检查，玩家手放的不枯萎
        if (!state.getValue(PERSISTENT)) {
            super.randomTick(state, level, pos, random);
        }

// super 可能把这块树叶判掉了，这里重新取一次状态再决定要不要长果实
        BlockState current = level.getBlockState(pos);
        if (current.is(this) && !current.getValue(FRUITING)) {
            tryMatureFruit(level, pos, current, random, NATURAL_MATURITY_CHANCE);
        }
    }

// 推进一级成熟度。
//
// 返回：本次是否成功推进（概率没中返回 false）
    public boolean tryMatureFruit(ServerLevel level, BlockPos pos, BlockState state, RandomSource random,
                                  double chance) {
        if (random.nextDouble() >= chance) {
            return false;
        }

        int maturity = Math.min(MAX_MATURITY, state.getValue(MATURITY) + 1);
        BlockState matured = state.setValue(MATURITY, maturity);
        if (maturity < MAX_MATURITY) {
            level.setBlock(pos, matured, 3);
            return true;
        }

        level.setBlock(pos, matured.setValue(FRUITING, true), 3);
        if (random.nextDouble() < HANGING_FRUIT_CHANCE) {
            BlockPos below = pos.below();
            if (level.getBlockState(below).isAir()) {
                level.setBlock(below, hangingFruit.get().defaultBlockState(), 3);
            }
        }
        return true;
    }

// 摘下这一块树叶上的果实（掉落物落在树叶本身那格）
    public boolean harvestFruit(ServerLevel level, BlockPos pos) {
        return harvestFruit(level, pos, pos);
    }

// 摘下果实：清掉下方的悬挂果实、掉落果实物品，并把树叶恢复成未挂果。
//
// 返回：这块树叶当前没挂果、摘不到东西时返回 false
    public boolean harvestFruit(ServerLevel level, BlockPos pos, BlockPos dropAt) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this) || !state.getValue(FRUITING)) {
            return false;
        }

        BlockPos hangingPos = findHangingFruit(level, pos);
        if (hangingPos != null) {
            level.removeBlock(hangingPos, false);
        }
        popResource(level, dropAt, new ItemStack(fruitItem.get()));
        level.setBlock(pos, state.setValue(FRUITING, false).setValue(MATURITY, 0), 3);
        return true;
    }

// 从树叶往下、穿过连续的树叶找到悬挂果实，返回它的位置；没有则返回 null
    private BlockPos findHangingFruit(Level level, BlockPos leafPos) {
        BlockPos.MutableBlockPos mutable = leafPos.below().mutable();
        for (int i = 0; i < HANGING_SEARCH_DEPTH; i++) {
            BlockState state = level.getBlockState(mutable);
            if (state.getBlock() instanceof LeavesBlock) {
                mutable.move(Direction.DOWN);
                continue;
            }
            return state.is(hangingFruit.get()) ? mutable.immutable() : null;
        }
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
// 对树叶直接撒骨粉：单独催熟这一块树叶
        if (!stack.is(Items.BONE_MEAL)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        if (!state.getValue(FRUITING) && level instanceof ServerLevel serverLevel) {
            if (tryMatureFruit(serverLevel, pos, state, serverLevel.getRandom(), BONE_MEAL_MATURITY_CHANCE)) {
                stack.consume(1, player);
                serverLevel.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        15, 0.5, 0.5, 0.5, 0.0);
            }
        }
// 手拿骨粉右键树叶一律拦下，免得误触发空手采摘；骨粉只在催熟成功时消耗
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!state.getValue(FRUITING)) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel && harvestFruit(serverLevel, pos)) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
