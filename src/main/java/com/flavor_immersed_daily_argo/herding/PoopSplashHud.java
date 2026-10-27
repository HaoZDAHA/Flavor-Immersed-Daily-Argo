package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.event.RenderGuiEvent;

// 被粪便砸中之后，糊在屏幕上的那层脏东西。
//
// 贴图整张拉伸铺满窗口（中央是不透明的污渍、四周透明），只渲染不下判断，
// 倒计时由 PoopSplashPacket 到达时开启，逻辑与 AnimalStatsHud 一致。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class PoopSplashHud {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "textures/gui/shitgui.png");
// 贴图本身的边长，铺满窗口时按它做 UV 基准
    private static final int TEXTURE_SIZE = 256;

    private static int ticksLeft;

    private PoopSplashHud() {
    }

// 服务端通知「被砸中了」：开一段倒计时（同一瞬间多次命中取更长的那个）
    public static void show(int ticks) {
        ticksLeft = Math.max(ticksLeft, ticks);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
            if (ticksLeft > 0) {
            ticksLeft--;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (ticksLeft <= 0) {
            return;
        }
        GuiGraphics gui = event.getGuiGraphics();
        gui.blit(TEXTURE, 0, 0, gui.guiWidth(), gui.guiHeight(),
                0.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
