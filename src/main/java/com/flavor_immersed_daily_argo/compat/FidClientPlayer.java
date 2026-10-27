package com.flavor_immersed_daily_argo.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

// 单独放一个类，专供客户端分支调用：专用服务器上不会加载到 Minecraft。
public final class FidClientPlayer {

    private FidClientPlayer() {
    }

    public static LocalPlayer get() {
        return Minecraft.getInstance().player;
    }
}
