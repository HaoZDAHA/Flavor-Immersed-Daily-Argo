package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.integration.emi.FIDEmiPlugin;
import com.flavor_immersed_daily_argo.integration.jei.FIDAJEIPlugin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// 只发布当前连接收到的数据，避免单人模式与集成服务端共享单例造成旧数据泄漏。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class FishingHandbookResource {
    private static volatile List<FishingHandbookEntry> entries = List.of();
    private static boolean emiRefreshPending;

    public static List<FishingHandbookEntry> load() {
        return entries;
    }

    public static void onDataSynced() {
        entries = FishingHandbookData.getInstance().all().stream()
                .filter(entry -> !entry.icon().isEmpty()).toList();
        emiRefreshPending = ModList.get().isLoaded("emi");
        if (ModList.get().isLoaded("jei")) {
            FIDAJEIPlugin.refreshFishingRecipes();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (emiRefreshPending && Minecraft.getInstance().level != null
                && FIDEmiPlugin.refreshFishingRecipes()) {
            emiRefreshPending = false;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        entries = List.of();
        emiRefreshPending = false;
    }

// 条目参数，一行一条，供 JEI / EMI 的条目页直接展示
    public static List<Component> lines(FishingHandbookEntry entry) {
        List<Component> lines = new ArrayList<>(7);
        lines.add(line("habitat", Component.translatable(
                "book.flavor_immersed_daily_argo.habitat." + entry.habitat.name().toLowerCase(Locale.ROOT))));
        lines.add(line("depth", entry.depth == null || entry.depth.isBlank()
                ? Component.translatable("book.flavor_immersed_daily_argo.depth.unlimited")
                : Component.literal(entry.depth)));
        lines.add(line("diet", Component.translatable(entry.diet.translationKey())));
        lines.add(line("activity", activity(entry)));
        lines.add(line("alertness", Component.translatable(
                "book.flavor_immersed_daily_argo.alertness." + entry.alertness.name().toLowerCase(Locale.ROOT))));
        lines.add(line("feeding", Component.translatable(
                "book.flavor_immersed_daily_argo.feeding." + entry.feedingSpeed.name().toLowerCase(Locale.ROOT))));
        lines.add(line("tension", Component.literal(String.valueOf(entry.struggleTension))));
        return lines;
    }

    private static Component line(String key, Component value) {
        return Component.translatable("jei.flavor_immersed_daily_argo.fish." + key, value);
    }

    private static Component activity(FishingHandbookEntry entry) {
        if (entry.activity.isEmpty()) {
            return Component.translatable("book.flavor_immersed_daily_argo.activity.all_time");
        }
        StringBuilder text = new StringBuilder();
        for (FishingHandbookEntry.Activity activity : entry.activity) {
            if (!text.isEmpty()) {
                text.append(" / ");
            }
            text.append(Component.translatable(
                    "book.flavor_immersed_daily_argo.activity." + activity.name().toLowerCase(Locale.ROOT)).getString());
        }
        return Component.literal(text.toString());
    }

    private FishingHandbookResource() {
    }
}
