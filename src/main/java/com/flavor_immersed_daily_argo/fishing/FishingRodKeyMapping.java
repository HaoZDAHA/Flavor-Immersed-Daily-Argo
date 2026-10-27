package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.ModFishingRodItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public class FishingRodKeyMapping {

    public static final KeyMapping OPEN_ROD_MOD = new KeyMapping(
            "key.flavor_immersed_daily_argo.open_rod_mod",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.flavor_immersed_daily_argo.fishing"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ROD_MOD);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
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
