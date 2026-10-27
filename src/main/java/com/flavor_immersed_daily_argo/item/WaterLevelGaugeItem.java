package com.flavor_immersed_daily_argo.item;

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

// 水位尺：右键测量当前所在（或最近）网点的水位。
//
// 正常水位在 ForestryWater#LEVEL_MIN~ForestryWater#LEVEL_MAX 米之间；
// 网点遭遇洪水或干涸时会明显偏离这个区间。
public class WaterLevelGaugeItem extends TooltipItem {

    public WaterLevelGaugeItem(Properties properties, String tooltipKey) {
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
        int waterLevel = point.waterLevel();
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.forestry.gauge.level", waterLevel), false);

        String stateKey;
        if (waterLevel > ForestryWater.LEVEL_MAX) {
            stateKey = "message.flavor_immersed_daily_argo.forestry.gauge.flood";
        } else if (waterLevel < ForestryWater.LEVEL_MIN) {
            stateKey = "message.flavor_immersed_daily_argo.forestry.gauge.drought";
        } else {
            stateKey = "message.flavor_immersed_daily_argo.forestry.gauge.normal";
        }
        player.displayClientMessage(Component.translatable(stateKey), false);
    }
}
