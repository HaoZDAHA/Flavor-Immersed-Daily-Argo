package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.entity.FallingFruit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;
import net.minecraft.world.InteractionHand;

// 悬挂的果子：挂在果树叶下方的那颗果实，形制照搬父模组 flavor_immersed_daily 的 FallingFruitBlock。
//
// 只能挂在树叶下面（#canSurvive）。刚长出来时有概率直接掉落、上方树叶没了也会掉下来，
// 掉落时会生成 FallingFruit 实体；摘果时一并复位上方那块果树叶，让它能重新结果。
// 徒手右键即可摘下果实。
public class HangingFruitBlock extends Block {

// 刚挂出果实时直接掉落到地上的概率（与父模组 Config.fallingFruitChance 一致）
    private static final double FALL_CHANCE = 0.30D;

// 每个区块最多同时存在的坠落果子实体数（与父模组 Config.maxFruitsPerChunk 一致），防止地上堆满
    private static final int MAX_FRUITS_PER_CHUNK = 5;

// 从果实往上穿过连续树叶、找挂着它的那块果树叶的最多层数
    private static final int SUPPORT_SEARCH_DEPTH = 12;

    private final Supplier<Item> fruitItem;

    public HangingFruitBlock(Supplier<Item> fruitItem, BlockBehaviour.Properties properties) {
        super(properties);
        this.fruitItem = fruitItem;
    }

// 这颗果实对应的物品，供果实回找与树干交互比对
    public Item getFruitItem() {
        return fruitItem.get();
    }


    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
// 刚长出来的果实有概率直接落果：复位上方果树叶、移除方块，生成一个从空中落下的坠落果子
        if (serverLevel.random.nextDouble() < FALL_CHANCE
                && countFruitEntitiesInChunk(serverLevel, pos) < MAX_FRUITS_PER_CHUNK) {
            clearSupportingLeaf(serverLevel, pos);
            level.removeBlock(pos, false);
            spawnFallingFruit(serverLevel, pos, state);
        } else if (!canSurvive(state, level, pos)) {
            serverLevel.scheduleTick(pos, this, 1);
        }
    }

// 只能挂在树叶下方
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).getBlock() instanceof LeavesBlock;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP && !canSurvive(state, level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.removeBlock(pos, false);
            tryDropFruit(state, level, pos, random);
        }
    }

// 上方树叶没了：有概率生成坠落果子实体，否则直接掉果实物品
    private void tryDropFruit(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextDouble() < FALL_CHANCE
                && countFruitEntitiesInChunk(level, pos) < MAX_FRUITS_PER_CHUNK) {
            spawnFallingFruit(level, pos, blockState);
        } else {
            popResource(level, pos, new ItemStack(fruitItem.get()));
        }
    }

// 在指定位置生成一颗坠落果子，落地后停在地上、可右键拾取
    private void spawnFallingFruit(ServerLevel level, BlockPos pos, BlockState blockState) {
        FallingFruit entity = new FallingFruit(ModEntities.FALLING_FRUIT.get(), level);
        entity.setPos(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
        entity.setBlockState(blockState);
        entity.setDropItem(new ItemStack(fruitItem.get()));
        level.addFreshEntity(entity);
    }

// 当前区块内已有的坠落果子实体数量
    private static int countFruitEntitiesInChunk(ServerLevel level, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        return level.getEntitiesOfClass(FallingFruit.class,
                new AABB(chunkX << 4, level.getMinBuildHeight(), chunkZ << 4,
                        (chunkX << 4) + 16, level.getMaxBuildHeight(), (chunkZ << 4) + 16)).size();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel && harvestFruit(serverLevel, pos, pos)) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 摘下这颗果实：掉落果实物品、移除方块，并复位上方那块果树叶。
//
// 返回：这个位置已经不是本方块时返回 false
    public boolean harvestFruit(ServerLevel level, BlockPos pos, BlockPos dropAt) {
        if (!level.getBlockState(pos).is(this)) {
            return false;
        }
        clearSupportingLeaf(level, pos);
        popResource(level, dropAt, new ItemStack(fruitItem.get()));
        level.removeBlock(pos, false);
        return true;
    }

// 从果实往上、穿过连续的树叶找到挂着它的那块果树叶，把它复位成「未挂果」
    private void clearSupportingLeaf(ServerLevel level, BlockPos fruitPos) {
        BlockPos.MutableBlockPos mutable = fruitPos.above().mutable();
        for (int i = 0; i < SUPPORT_SEARCH_DEPTH; i++) {
            BlockState state = level.getBlockState(mutable);
            if (state.getBlock() instanceof FruitingLeavesBlock leaves
                    && leaves.getFruitItem() == fruitItem.get()
                    && state.getValue(FruitingLeavesBlock.FRUITING)) {
                level.setBlock(mutable, state.setValue(FruitingLeavesBlock.FRUITING, false)
                        .setValue(FruitingLeavesBlock.MATURITY, 0), 3);
                return;
            }
            if (!(state.getBlock() instanceof LeavesBlock)) {
                return;
            }
            mutable.move(Direction.UP);
        }
    }
}
