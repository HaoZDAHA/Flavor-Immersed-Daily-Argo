package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

// 导航手机。
//
// 两个状态，用 ModDataComponents#PHONE_OPEN 记着：
//
// 潜行右键——展开 / 收起，来回切换；
// 展开状态直接右键——向服务端要一份外卖订单，弹出接单界面。
public class NavPhoneItem extends Item {

    public NavPhoneItem(Properties properties) {
        super(properties);
    }

    public static boolean isOpen(ItemStack stack) {
        return ModDataComponents.PHONE_OPEN.get().getOrDefault(stack, false);
    }

    public static void setOpen(ItemStack stack, boolean open) {
        ModDataComponents.PHONE_OPEN.get().set(stack, open);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
// 潜行右键：切换展开 / 收起。两端都落笔，客户端立即换贴图，服务端负责存档与同步
        if (player.isSecondaryUseActive()) {
            setOpen(stack, !isOpen(stack));
            if (!level.isClientSide) {
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
// 展开状态下直接右键：打开接单界面（服务端刷新后把订单发回来）
        if (isOpen(stack)) {
            if (level.isClientSide) {
                PacketDistributor.sendToServer(new PhoneOrderRequestPacket());
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.nav_phone"));
        tooltip.add(Component.translatable(isOpen(stack)
                ? "tooltip.flavor_immersed_daily_argo.nav_phone_open"
                : "tooltip.flavor_immersed_daily_argo.nav_phone_closed"));
    }
}
