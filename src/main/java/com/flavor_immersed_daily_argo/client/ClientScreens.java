package com.flavor_immersed_daily_argo.client;

import com.flavor_immersed_daily_argo.fishing.FishingHandbookScreen;
import com.flavor_immersed_daily_argo.forestry.ForestryHandbookScreen;
import net.minecraft.client.Minecraft;

// 打开界面的唯一入口，只会在客户端被加载。
//
// 物品 / 方块这些公共包里的类不能直接引用 Minecraft 或 Screen：
// 专用服务器会剥离客户端类，公共类在注册阶段被校验时就会抛
// invalid dist DEDICATED_SERVER。公共类只调用本类的静态方法，
// 本类也只有在客户端真正点右键时才会被加载。
public final class ClientScreens {

    private ClientScreens() {
    }

    public static void openFishingHandbook() {
        Minecraft.getInstance().setScreen(new FishingHandbookScreen());
    }

    public static void openForestryHandbook() {
        Minecraft.getInstance().setScreen(new ForestryHandbookScreen());
    }
}
