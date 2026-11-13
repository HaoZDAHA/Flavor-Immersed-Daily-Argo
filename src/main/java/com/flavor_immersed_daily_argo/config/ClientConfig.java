package com.flavor_immersed_daily_argo.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Local presentation preferences; no gameplay rules or server-side client classes. */
public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_FISHING_PULL_DIRECTION;
    public static final ModConfigSpec.BooleanValue FISHING_PULL_RESISTANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("fishing");
        SHOW_FISHING_PULL_DIRECTION = builder
                .comment("钓鱼遛鱼时在准星周围显示 FPS 受击式拉力方向弧，默认开启。",
                        "亮弧所在侧是鱼的即时拉扯方向；反方向转动视角顶住。关闭只隐藏提示。",
                        "Show an FPS-style pull bearing arc around the crosshair during a fight.")
                .define("showPullDirection", true);
        FISHING_PULL_RESISTANCE = builder
                .comment("反向拉扯时增加短暂鼠标阻滞、镜头咬住与释放反馈，默认开启。",
                        "只影响遛鱼时的本地手感，不暂停游戏，不改写鼠标设置。",
                        "Enable brief counter-pull mouse resistance and contact feedback.")
                .define("pullResistance", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}
