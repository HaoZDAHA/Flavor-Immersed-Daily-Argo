package com.flavor_immersed_daily_argo.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// 耙子：右键一次把以点击处为中心的 3×3 土地耕成耕地，整次只消耗 1 点耐久。
//
// 高度落差：3×3 里每一格都按「点击高度、上方一格、下方一格」的顺序找地，
// 所以田埂、梯田这种高低差一格的地面也能被同一把耙子带过去，不必一格一格对齐。
//
// 清理植被：草方块上长着草、蕨、花、树苗这些杂物时，原版会因为「上方不是空气」而
// 拒绝耕地。本工具会先顺手把这类植被清掉再耕；如果清掉之后那块地其实耕不动（例如杂草原本长在
// 石头上），就把杂草原样放回去，不会白清。
//
// 耕地沿用原版锄头的判定链（ItemAbilities#HOE_TILL → 方块自身的
// getToolModifiedState），因此草地 / 土径 / 泥土变耕地、粗泥变泥土、缠根泥土掉垂根这些
// 原版规则，以及其它模组通过 BlockToolModificationEvent 注册的耕地规则都会生效，
// 不需要在这里写死方块名单。
//
// 范围内一块都耕不动时放行给其它交互，不影响右键别的方块。
public class HarrowItem extends TooltipItem {

// 耕地范围：以点击处为中心的水平半径（1 → 3×3）
    private static final int RADIUS = 1;

// 找地的顺序：先点击所在的高度，再看上方、下方各一格，照顾有落差的地面
    private static final int[] HEIGHT_OFFSETS = {0, 1, -1};

    public HarrowItem(Item.Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

// 让原版把耙子当成能耕地的工具，getToolModifiedState 才会给出耕地状态
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return itemAbility == ItemAbilities.HOE_TILL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
// 服务端说了算：耕到了就消耗这次动作，没耕到就放行给其它交互
            return till(serverLevel, context, player) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
// 客户端只做预测（决定要不要挥手），真正耕没耕到以服务端为准
        return hasTillable(level, context) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

// 范围内只要有一格耕得动就动手，否则放行给其它交互
    private static boolean hasTillable(Level level, UseOnContext context) {
        for (BlockPos column : columns(context.getClickedPos())) {
            for (int dy : HEIGHT_OFFSETS) {
                if (canTill(level, context, column.offset(0, dy, 0))) {
                    return true;
                }
            }
        }
        return false;
    }

// 这一格能不能耕：直接能耕，或者清掉上面的杂草后能耕（耕地时会顺带清掉杂草）
    private static boolean canTill(Level level, UseOnContext context, BlockPos pos) {
        if (tilledState(level, context, pos, true) != null) {
            return true;
        }
// 盖着杂草时原版会因「上方不是空气」拒绝耕地，但本工具会先清掉杂草，所以也算能耕
        return !level.getBlockState(pos).isAir() && isVegetation(level.getBlockState(pos.above()));
    }

// 把范围内能耕的地都耕一遍，整次只扣 1 点耐久；返回是否耕到了
    private static boolean till(ServerLevel level, UseOnContext context, Player player) {
        BlockPos center = context.getClickedPos();
        boolean changed = false;
        for (BlockPos column : columns(center)) {
            if (tillColumn(level, context, player, column)) {
                changed = true;
            }
        }
        if (!changed) {
            return false;
        }
        level.playSound(null, center, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        return true;
    }

// 在某一列里挑一格耕掉（点击高度优先，再看上下各一格），返回这一列是否耕到了
    private static boolean tillColumn(ServerLevel level, UseOnContext context, Player player, BlockPos column) {
        for (int dy : HEIGHT_OFFSETS) {
            BlockPos pos = column.offset(0, dy, 0);
            BlockState tilled = tilledState(level, context, pos, false);
            if (tilled == null) {
                continue;
            }
            level.setBlock(pos, tilled, 3);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, tilled));
            return true;
        }
        return false;
    }

// 该位置耕完会变成什么方块；耕不动返回 null。
// 上方盖着杂草植被时原版会因为「上方不是空气」而拒绝判定，所以这里先把杂草清掉再判一次：
// 清完能耕就保持清掉，清完还是耕不动就把杂草原样放回去并返回 null。
// simulate 为 true 时只探路、不改世界（清杂草那一路也会跳过，交给
// #canTill 的宽松判定兜着）。
    @Nullable
    private static BlockState tilledState(Level level, UseOnContext context, BlockPos pos, boolean simulate) {
        BlockState state = level.getBlockState(pos);
        BlockState tilled = state.getToolModifiedState(contextAt(context, pos), ItemAbilities.HOE_TILL, simulate);
        if (tilled != null || simulate) {
            return tilled;
        }
        return clearVegetationAndRetill(level, context, pos, state);
    }

// 清掉这一格上方的杂草植被后再判一次耕地：能耕就返回耕地状态（杂草保持清掉），
// 耕不动就把杂草放回去并返回 null。
    @Nullable
    private static BlockState clearVegetationAndRetill(Level level, UseOnContext context, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return null;
        }
        List<BlockPos> cleared = new ArrayList<>();
        List<BlockState> originals = new ArrayList<>();
// 高草这类两格高的植被要整株一起清，免得上面半截悬在空中
        for (BlockPos veg = pos.above(); isVegetation(level.getBlockState(veg)); veg = veg.above()) {
            originals.add(level.getBlockState(veg));
            cleared.add(veg);
            level.setBlock(veg, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        if (cleared.isEmpty()) {
            return null;
        }
        BlockState tilled = state.getToolModifiedState(contextAt(context, pos), ItemAbilities.HOE_TILL, false);
        if (tilled == null) {
// 清错了：这块地本来也耕不动，把杂草原样放回去
            for (int i = 0; i < cleared.size(); i++) {
                level.setBlock(cleared.get(i), originals.get(i), Block.UPDATE_CLIENTS);
            }
        }
        return tilled;
    }

// 是不是长在土地上、可以随手清掉的杂草植被：草、蕨、花、树苗、枯木之类。
// 明确排除作物（CropBlock），免得把玩家种的庄稼当杂草薅了。
    private static boolean isVegetation(BlockState state) {
        return state.getBlock() instanceof BushBlock && !(state.getBlock() instanceof CropBlock);
    }

// 复制一份「准星落在 pos 顶面」的上下文，让方块自带的耕地判定拿到正确的位置
    private static UseOnContext contextAt(UseOnContext original, BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3.atBottomCenterOf(pos.above()), Direction.UP, pos, false);
        return new UseOnContext(original.getLevel(), original.getPlayer(), original.getHand(),
                original.getItemInHand(), hit);
    }

// 以点击处为中心的水平 3×3
    private static Iterable<BlockPos> columns(BlockPos center) {
        return BlockPos.betweenClosed(
                center.offset(-RADIUS, 0, -RADIUS),
                center.offset(RADIUS, 0, RADIUS));
    }
}
