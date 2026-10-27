package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

// 带工作范围的无人机模块（看护 / 收获）。
//
// 范围直接记在模块自己身上：对着两个方块右键各记一个对角点，模块放进无人机的模组槽后，
// 无人机自己读出这两个点、飞到范围中心常驻干活，不必再去遥控器界面里手填坐标。
// 手持时右键空气可以把记录的两点一起抹掉。
public class UavRangeModuleItem extends TooltipItem {

// 记录点相关的文案前缀（看护 / 收获共用）
    private static final String POINT_TOOLTIP = "tooltip.flavor_immersed_daily_argo.uav_module_range";
    private static final String POINT_MESSAGE = "message.flavor_immersed_daily_argo.uav_module_range";

    public UavRangeModuleItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            BlockPos pos = context.getClickedPos();
            List<BlockPos> points = getPoints(stack);
            if (points.size() >= 2) {
// 两个点都记满了：这一个当成新的 A，重新开始记
                setPoints(stack, List.of(pos));
                player.displayClientMessage(Component.translatable(
                        POINT_MESSAGE + ".point_a", format(pos)), true);
            } else if (points.size() == 1) {
                setPoints(stack, List.of(points.get(0), pos));
                player.displayClientMessage(Component.translatable(
                        POINT_MESSAGE + ".point_b", format(pos)), true);
            } else {
                setPoints(stack, List.of(pos));
                player.displayClientMessage(Component.translatable(
                        POINT_MESSAGE + ".point_a", format(pos)), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 右键空气：把记录的两点抹掉，下次重新记
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && ModDataComponents.UAV_MODULE_POINTS.get().has(stack)) {
            ModDataComponents.UAV_MODULE_POINTS.get().remove(stack);
            player.displayClientMessage(
                    Component.translatable(POINT_MESSAGE + ".cleared"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        List<BlockPos> points = getPoints(stack);
        if (points.isEmpty()) {
            tooltipComponents.add(Component.translatable(POINT_TOOLTIP + ".how_to"));
        } else {
            tooltipComponents.add(Component.translatable(POINT_TOOLTIP + ".point_a", format(points.get(0))));
            tooltipComponents.add(points.size() < 2
                    ? Component.translatable(POINT_TOOLTIP + ".point_b_empty")
                    : Component.translatable(POINT_TOOLTIP + ".point_b", format(points.get(1))));
        }
    }

// 模块上记录的坐标（0 个或 1 个点也算有效，无人机只认两个点齐全的情况）
    public static List<BlockPos> getPoints(ItemStack stack) {
        List<Integer> raw = ModDataComponents.UAV_MODULE_POINTS.get().get(stack);
        if (raw == null || raw.size() < 3) {
            return List.of();
        }
        List<BlockPos> points = new ArrayList<>(raw.size() / 3);
        for (int i = 0; i + 2 < raw.size(); i += 3) {
            points.add(new BlockPos(raw.get(i), raw.get(i + 1), raw.get(i + 2)));
        }
        return points;
    }

    private static void setPoints(ItemStack stack, List<BlockPos> points) {
        List<Integer> raw = new ArrayList<>(points.size() * 3);
        for (BlockPos pos : points) {
            raw.add(pos.getX());
            raw.add(pos.getY());
            raw.add(pos.getZ());
        }
        ModDataComponents.UAV_MODULE_POINTS.get().set(stack, raw);
    }

// 只显示水平坐标：竖直方向无人机自己按记录点算，记在哪一格都一样
    private static String format(BlockPos pos) {
        return pos.getX() + ", " + pos.getZ();
    }
}
