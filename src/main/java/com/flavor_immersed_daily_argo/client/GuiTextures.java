package com.flavor_immersed_daily_argo.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

// 全部 GUI / JEI 底图的集中索引，避免每个界面各写一遍路径
public final class GuiTextures {

    private static final String GUI = "textures/gui/";
    private static final String JEI = "textures/gui/jei/";

    public static final ResourceLocation CHICKEN_COOP = gui("chicken_coop.png");
    public static final ResourceLocation FISHING_BOX = gui("fishing_box.png");
    public static final ResourceLocation PURCHASING_BOX = gui("purchasing_box.png");
    public static final ResourceLocation STEELYARD = gui("steelyard.png");
    public static final ResourceLocation UAV = gui("uav.png");
    public static final ResourceLocation INSECT_INCUBATOR = gui("insect_incubator.png");
    public static final ResourceLocation ROD_MOD = gui("rod_mod.png");

    public static final ResourceLocation JEI_INSECT_BREEDING = jei("insect_breeding.png");
    public static final ResourceLocation JEI_INSECT_NET = jei("insect_net.png");

    private GuiTextures() {
    }

// 1:1 画一整张底图。
//
// 别用 6 参的 blit：那个重载把贴图当成 256×256 来取 UV，底图矮于 256 时只会取到左上角一块再拉伸。
// 这里显式把贴图实际尺寸传进去。
    public static void drawBackground(GuiGraphics gui, ResourceLocation texture, int x, int y, int width, int height) {
        gui.blit(texture, x, y, width, height, 0.0F, 0.0F, width, height, width, height);
    }

    private static ResourceLocation gui(String path) {
        return new ResourceLocation("flavor_immersed_daily_argo", GUI + path);
    }

    private static ResourceLocation jei(String path) {
        return new ResourceLocation("flavor_immersed_daily_argo", JEI + path);
    }
}
