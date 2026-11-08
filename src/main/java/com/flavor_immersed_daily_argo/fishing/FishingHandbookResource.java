package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.integration.jei.FIDAJEIPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, value = Dist.CLIENT)
public final class FishingHandbookResource {
    private static List<FishingHandbookEntry> entries = List.of();
    private static boolean refreshPending;

    public static List<FishingHandbookEntry> load() {
        return entries;
    }

    public static void onDataSynced(List<String> handbook) {
        if (Minecraft.getInstance().getConnection() == null) {
            return;
        }
        // 客户端单独解析，不能覆盖集成服务端正在使用的数据。
        FishingHandbookData.getClientInstance().applyFromJson(handbook);
        entries = FishingHandbookData.getClientInstance().all().stream()
                .filter(entry -> !entry.icon().isEmpty()).toList();
        refreshPending = ModList.get().isLoaded("jei");
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && refreshPending
                && Minecraft.getInstance().level != null
                && FIDAJEIPlugin.refreshFishingRecipes()) {
            refreshPending = false;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        entries = List.of();
        FishingHandbookData.getClientInstance().applyFromJson(List.of());
        refreshPending = false;
        if (ModList.get().isLoaded("jei")) {
            FIDAJEIPlugin.refreshFishingRecipes();
        }
    }

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
            if (text.length() > 0) {
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
