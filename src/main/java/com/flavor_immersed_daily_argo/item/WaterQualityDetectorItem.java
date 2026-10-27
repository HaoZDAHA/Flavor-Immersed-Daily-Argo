package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.forestry.ForestryEventType;
import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import com.flavor_immersed_daily_argo.forestry.ForestryPoint;
import com.flavor_immersed_daily_argo.forestry.ForestryWater;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;

// 水质检测器：右键测量当前所在（或最近）网点的水质，并把测到的确切灾难名上报到护林员写字板。
//
// 五项指标为溶解氧、pH、有机污染指标、电导率、浊度，另附一个综合水质评分。任一指标越界
// 即对应一种水体灾难；水质灾难不会自动出现在写字板上（最多降评分），必须由玩家测出来、
// 把明确的灾难名传上去。
public class WaterQualityDetectorItem extends TooltipItem {

    public WaterQualityDetectorItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            measure(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void measure(ServerPlayer player) {
        ForestryPoint point = ForestryNetworkManager.nearestPoint(player, player.blockPosition());
        if (point == null) {
            player.displayClientMessage(
                    Component.translatable("message.flavor_immersed_daily_argo.forestry.water.no_point"), false);
            return;
        }

        int oxygen = point.oxygen();
        int ph = point.ph();
        int organic = point.organic();
        int conductivity = point.conductivity();
        int turbidity = point.turbidity();
        int comprehensive = ForestryWater.comprehensive(oxygen, ph, organic, conductivity, turbidity);

        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.forestry.detector.header",
                point.pos().getX(), point.pos().getZ()), false);
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.forestry.detector.oxygen", oxygen), false);
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.forestry.detector.ph",
                String.format(Locale.ROOT, "%.1f", ForestryWater.phValue(ph))), false);
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.forestry.detector.organic", organic), false);
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.forestry.detector.conductivity", conductivity), false);
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.forestry.detector.turbidity", turbidity), false);
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.forestry.detector.comprehensive",
                comprehensive, ForestryWater.qualityText(comprehensive)), false);

// 把测到的首要灾难名上报到写字板；没有灾难则清除之前的上报
        ForestryEventType primary = ForestryWater.primaryDisaster(oxygen, ph, organic, conductivity);
        ForestryNetworkManager.reportWater(player, point, primary);
        if (primary == ForestryEventType.NONE) {
            player.displayClientMessage(
                    Component.translatable("message.flavor_immersed_daily_argo.forestry.detector.clean"), false);
        } else {
            player.displayClientMessage(Component.translatable(
                    "message.flavor_immersed_daily_argo.forestry.detector.reported",
                    Component.translatable(primary.translationKey())), false);
        }
    }
}
