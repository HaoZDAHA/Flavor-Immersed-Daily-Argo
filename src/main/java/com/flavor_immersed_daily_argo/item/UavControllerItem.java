package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import javax.annotation.Nullable;

import java.util.List;
import java.util.UUID;

// 无人机遥控器。
//
// 「绑给了哪架无人机」记在物品自己的数据组件 ModDataComponents#UAV_BOUND_DRONE 上，
// 所以遥控器离手也记得住、tooltip 也读得出来：有组件就是已绑定，右键随便点哪儿都能开操作界面。
public class UavControllerItem extends TooltipItem {

    public UavControllerItem(Properties properties) {
        super(properties, "tooltip.flavor_immersed_daily_argo.uav_controller");
    }

// 读出手里这把遥控器绑的无人机；没绑或者数据坏了都返回 null
    @Nullable
    public static UUID getBoundDrone(ItemStack stack) {
        String raw = ModDataComponents.UAV_BOUND_DRONE.get().get(stack);
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

// 把这把遥控器绑到某架无人机上
    public static void bind(ItemStack stack, UUID droneId) {
        ModDataComponents.UAV_BOUND_DRONE.get().set(stack, droneId.toString());
    }

// 解绑：把遥控器上的绑定信息抹掉
    public static void unbind(ItemStack stack) {
        ModDataComponents.UAV_BOUND_DRONE.get().remove(stack);
    }

// 服务端解绑时，把玩家背包里所有绑着这架无人机的遥控器一并抹掉绑定信息
    public static void unbindAll(Player player, UUID droneId) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof UavControllerItem && droneId.equals(getBoundDrone(stack))) {
                unbind(stack);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(getBoundDrone(stack) == null
                ? "tooltip.flavor_immersed_daily_argo.uav_controller.unbound"
                : "tooltip.flavor_immersed_daily_argo.uav_controller.bound"));
    }
}
