package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.ModFishingRodItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

// 键位本身在 flavor_immersed_daily_argoClient#registerKeyMappings（MOD 总线）注册。
// 注册必须挂在 MOD 总线上，RegisterKeyMappingsEvent 不会发到 GAME 总线——
// 挂错总线时这个键位不在 Options.keyMappings 里，改键位后就彻底收不到按键了。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class FishingRodKeyMapping {

    public static final KeyMapping OPEN_ROD_MOD = new KeyMapping(
            "key.flavor_immersed_daily_argo.open_rod_mod",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.flavor_immersed_daily_argo.fishing"
    );

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) return;

        while (OPEN_ROD_MOD.consumeClick()) {
            if (player.getMainHandItem().getItem() instanceof ModFishingRodItem ||
                    player.getOffhandItem().getItem() instanceof ModFishingRodItem) {
                PacketDistributor.sendToServer(new OpenRodModPacket());
            }
        }
    }
}
