package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.block.FruitingLeavesBlock;
import com.flavor_immersed_daily_argo.block.HangingFruitBlock;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 果树树干交互：照搬父模组 flavor_immersed_daily 的 FruitTreeRipeningHandler / FruitHarvestHandler。
//
// 对着树干（原木方块）右键：
//
// 手拿骨粉 —— 按骨粉催熟概率，把与树干相连的整棵果树叶各随机催熟一级；
// 手拿木棍 —— 一次性摘下与树干相连的所有果实（消耗饱食度并扫击粒子/音效）。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.FORGE)
public final class FruitTreeInteractHandler {

    private static final int SEARCH_RADIUS = 7;
    private static final int MAX_SEARCHED_BLOCKS = 512;
// 木棍整棵收货一次消耗的饱食度
    private static final float EXHAUSTION_COST = 6.0F;

// 手拿骨粉右键树干：整棵催熟
    @SubscribeEvent
    public static void onBoneMealTrunk(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(Items.BONE_MEAL)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RotatedPillarBlock)) {
            return;
        }

        if (level.isClientSide) {
            event.getEntity().swing(event.getHand());
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        if (ripenConnectedFruit(serverLevel, pos, state.getBlock())) {
            if (!event.getEntity().getAbilities().instabuild) { event.getItemStack().shrink(1); }
            serverLevel.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    15, 0.5, 0.5, 0.5, 0.0);
            event.setCanceled(true);
        }
    }

// 手拿木棍右键树干：整棵收货
    @SubscribeEvent
    public static void onStickHarvestTrunk(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!event.getItemStack().is(net.minecraft.world.item.Items.STICK)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RotatedPillarBlock) || player.getFoodData().getFoodLevel() <= 0) {
            return;
        }

        if (level.isClientSide) {
            player.swing(event.getHand());
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        if (harvestConnectedFruit(serverLevel, pos, state.getBlock(), player.blockPosition())) {
            player.getFoodData().addExhaustion(EXHAUSTION_COST);
            serverLevel.playSound(null, pos, SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);
            event.setCanceled(true);
        }
    }

// 广度优先遍历与树干相连的树叶，逐块随机催熟；返回本次是否至少催熟了一块
    private static boolean ripenConnectedFruit(ServerLevel level, BlockPos origin, Block trunkBlock) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);
        boolean ripened = false;

        while (!queue.isEmpty() && visited.size() < MAX_SEARCHED_BLOCKS) {
            BlockPos currentPos = queue.removeFirst();
            if (!visited.add(currentPos) || !isWithinSearchRadius(origin, currentPos)) {
                continue;
            }

            BlockState currentState = level.getBlockState(currentPos);
            Block currentBlock = currentState.getBlock();
            if (currentBlock != trunkBlock && !(currentBlock instanceof LeavesBlock)) {
                continue;
            }

            if (currentBlock instanceof FruitingLeavesBlock leaves
                    && !currentState.getValue(FruitingLeavesBlock.FRUITING)) {
                ripened |= leaves.tryMatureFruit(level, currentPos, currentState,
                        level.getRandom(), FruitingLeavesBlock.BONE_MEAL_MATURITY_CHANCE);
            }

            enqueueNeighbors(queue, visited, origin, currentPos);
        }
        return ripened;
    }

// 广度优先遍历与树干相连的树叶与悬挂果实，逐个摘下；返回本次是否至少摘下一颗
    private static boolean harvestConnectedFruit(ServerLevel level, BlockPos origin, Block trunkBlock, BlockPos dropAt) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> fruitPositions = new ArrayList<>();
        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < MAX_SEARCHED_BLOCKS) {
            BlockPos currentPos = queue.removeFirst();
            if (!visited.add(currentPos) || !isWithinSearchRadius(origin, currentPos)) {
                continue;
            }

            BlockState currentState = level.getBlockState(currentPos);
            Block currentBlock = currentState.getBlock();
            if (currentBlock != trunkBlock
                    && !(currentBlock instanceof LeavesBlock)
                    && !(currentBlock instanceof HangingFruitBlock)) {
                continue;
            }

            if (currentBlock instanceof FruitingLeavesBlock || currentBlock instanceof HangingFruitBlock) {
                fruitPositions.add(currentPos);
            }

            enqueueNeighbors(queue, visited, origin, currentPos);
        }

// 先收集再统一摘，避免遍历途中改动方块状态影响 BFS
        boolean harvested = false;
        for (BlockPos fruitPos : fruitPositions) {
            BlockState fruitState = level.getBlockState(fruitPos);
            if (fruitState.getBlock() instanceof FruitingLeavesBlock leaves) {
                harvested |= leaves.harvestFruit(level, fruitPos, dropAt);
            } else if (fruitState.getBlock() instanceof HangingFruitBlock hanging) {
                harvested |= hanging.harvestFruit(level, fruitPos, dropAt);
            }
        }
        return harvested;
    }

    private static void enqueueNeighbors(ArrayDeque<BlockPos> queue, Set<BlockPos> visited,
                                         BlockPos origin, BlockPos currentPos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = currentPos.relative(direction);
            if (!visited.contains(neighborPos) && isWithinSearchRadius(origin, neighborPos)) {
                queue.addLast(neighborPos);
            }
        }
    }

    private static boolean isWithinSearchRadius(BlockPos origin, BlockPos pos) {
        return Math.abs(pos.getX() - origin.getX()) <= SEARCH_RADIUS
                && Math.abs(pos.getY() - origin.getY()) <= SEARCH_RADIUS
                && Math.abs(pos.getZ() - origin.getZ()) <= SEARCH_RADIUS;
    }
}
