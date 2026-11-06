package com.flavor_immersed_daily_argo.economy.compat;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

// 经济联动的触发点。
//
// 把还没折算的旧卡余额早点转进共享账户。卡可能在背包、末影箱、箱子、潜影盒、
// 展示框里，钩子就铺在登录、打开容器、右键展示框；ATM 和商店结账内部自带。
//
// 每个钩子都包了 try/catch。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class EconomyCompatEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    private EconomyCompatEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        try {
            EconomyCompat.onPlayerJoin(player);
        } catch (Throwable t) {
            LOGGER.warn("[FIDA Economy] Login maintenance failed; continuing with the built-in economy: {}", t.toString());
        }
    }

// 打开容器
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        try {
            EconomyCompat.migrateCardsIn(player, event.getContainer().getItems());
        } catch (Throwable t) {
            LOGGER.warn("[FIDA Economy] Card migration inside a container failed: {}", t.toString());
        }
    }

// 右键展示框
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getTarget() instanceof ItemFrame frame)) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        try {
            EconomyCompat.migrateCard(player, frame.getItem());
        } catch (Throwable t) {
            LOGGER.warn("[FIDA Economy] Card migration in an item frame failed: {}", t.toString());
        }
    }
}
