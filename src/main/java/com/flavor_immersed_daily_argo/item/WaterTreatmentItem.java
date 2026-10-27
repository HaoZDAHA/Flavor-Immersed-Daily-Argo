package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import com.flavor_immersed_daily_argo.forestry.ForestryPoint;
import com.flavor_immersed_daily_argo.forestry.ForestryWater;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;

// 水质药剂 / 器具：右键水体后调整最近网点的一项或几项水质指标。
//
// 投放有两种方式：Effect 里带恒定增量的（升 / 降某项），以及增量为 0 的「平衡」
// （把指标拨向 #BALANCE_TARGET 对应的健康中点）。指标统一夹在 0~100；
// 按 consumable 决定是否每次成功投放消耗一个，指标已经到位时不再消耗。
public class WaterTreatmentItem extends TooltipItem {

// 每次投放改动的幅度
    private static final int STEP = 15;

// 「平衡」类药剂的目标值：把指标拨向该值（各健康区间的中点）。
// 顺序为 溶解氧 / pH / 有机物质 / 电导率 / 浊度。
    private static final int[] BALANCE_TARGET = {62, 50, 55, 55, 45};

// 一次投放对某项水质指标的操作
    public enum Effect {
// 降低浊度
        TURBIDITY_DOWN(4, -STEP, "message.flavor_immersed_daily_argo.forestry.treatment.turbidity"),
// 平衡电导率：拨向健康中点
        CONDUCTIVITY_BALANCE(3, 0, "message.flavor_immersed_daily_argo.forestry.treatment.conductivity"),
// 降低 pH（减碱度）
        PH_DOWN(1, -STEP, "message.flavor_immersed_daily_argo.forestry.treatment.ph_down"),
// 升高 pH（减酸度）
        PH_UP(1, STEP, "message.flavor_immersed_daily_argo.forestry.treatment.ph_up"),
// 平衡溶解氧：拨向健康中点
        OXYGEN_BALANCE(0, 0, "message.flavor_immersed_daily_argo.forestry.treatment.oxygen"),
// 减少有机污染指数
        ORGANIC_DOWN(2, -STEP, "message.flavor_immersed_daily_argo.forestry.treatment.organic");

        private final int metricIndex;
// 增量；为 0 表示「平衡」模式，拨向 #BALANCE_TARGET
        private final int delta;
        private final String messageKey;

        Effect(int metricIndex, int delta, String messageKey) {
            this.metricIndex = metricIndex;
            this.delta = delta;
            this.messageKey = messageKey;
        }

// 由当前值算出投放后的新值（已夹在 0~100）
        int apply(int current) {
            if (this.delta != 0) {
                return Mth.clamp(current + this.delta, 0, 100);
            }
            int target = BALANCE_TARGET[this.metricIndex];
            if (current == target) {
                return current;
            }
// 每次最多挪 STEP，靠近但不越过目标值
            int step = current < target
                    ? Math.min(STEP, target - current)
                    : -Math.min(STEP, current - target);
            return Mth.clamp(current + step, 0, 100);
        }
    }

    private final boolean consumable;
    private final Effect[] effects;

    public WaterTreatmentItem(Properties properties, String tooltipKey, boolean consumable, Effect... effects) {
        super(properties, tooltipKey);
        this.consumable = consumable;
        this.effects = effects;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            treat(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void treat(ServerPlayer player, ItemStack stack) {
        ForestryPoint point = ForestryNetworkManager.nearestPoint(player, player.blockPosition());
        if (point == null) {
            player.displayClientMessage(
                    Component.translatable("message.flavor_immersed_daily_argo.forestry.water.no_point"), false);
            return;
        }

        int[] metrics = {point.oxygen(), point.ph(), point.organic(), point.conductivity(), point.turbidity()};
        boolean changed = false;
        for (Effect effect : this.effects) {
            int before = metrics[effect.metricIndex];
            int after = effect.apply(before);
            if (after != before) {
                metrics[effect.metricIndex] = after;
                changed = true;
                player.displayClientMessage(
                        Component.translatable(effect.messageKey, display(effect, after)), false);
            }
        }
        if (!changed) {
            player.displayClientMessage(
                    Component.translatable("message.flavor_immersed_daily_argo.forestry.treatment.limit"), false);
            return;
        }

        ForestryNetworkManager.saveWater(player, point, metrics);
        if (this.consumable && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

// 消息里显示的数值：pH 用 0~14 的显示值，其余用 0~100 指标值
    private static Object display(Effect effect, int value) {
        return effect.metricIndex == 1
                ? String.format(Locale.ROOT, "%.1f", ForestryWater.phValue(value))
                : value;
    }
}
