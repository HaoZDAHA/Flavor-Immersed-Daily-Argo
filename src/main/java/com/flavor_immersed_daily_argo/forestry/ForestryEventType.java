package com.flavor_immersed_daily_argo.forestry;

// 林业网点可能出现的情况 / 灾害。
//
// 分三类：
//
// 常规林业事件（每日按概率自动冒出）：森林火灾 #FIRE、蚜虫侵蚀 #APHIDS、
// 杂草丛生 #WEEDS；
// 水文事件（每日按概率自动冒出，由气象 / 水位相关工具反映）：洪水 #FLOOD、
// 干涸 #DROUGHT；
// 水质灾难（不自动冒出，玩家用水质检测器测量后上报才会出现在写字板上）：
// #HYPOXIA、#OXYGEN_OVER、#ACID_RAIN、#STRONG_ALKALI、
// #BARREN_WATER、#STENCH、#OSMOSIS。
//
// #NONE 表示当前平安无事。
public enum ForestryEventType {
// 无事件
    NONE,
// 森林火灾：网点附近冒出不会蔓延的林业火灾，用灭火器扑灭
    FIRE,
// 蚜虫侵蚀：网点的树染上蚜虫，手持树漆右键即可清除
    APHIDS,
// 杂草丛生：网点区块内疯长杂草，用除草剂清除
    WEEDS,
// 洪水：水位暴涨，配合水位尺可观测到异常高水位
    FLOOD,
// 干涸：水位骤降，配合水位尺可观测到异常低水位
    DROUGHT,
// 水体缺氧：溶解氧过低
    HYPOXIA,
// 水氧过饱：溶解氧过高
    OXYGEN_OVER,
// 酸雨：pH 过低
    ACID_RAIN,
// 水体强碱：pH 过高
    STRONG_ALKALI,
// 水体贫瘠：有机物质指标过低
    BARREN_WATER,
// 水体发臭：有机物质指标过高
    STENCH,
// 渗透压失衡：电导率过低或过高
    OSMOSIS;

    private static final ForestryEventType[] VALUES = values();

// 按序号取值，越界一律当作 #NONE（存档 / 网络数据容错）
    public static ForestryEventType byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : NONE;
    }

// 是否属于「非常规天气事件」：气象仪器据此在 none / heavy 两套动画间切换
    public boolean isWeather() {
        return this == FIRE || this == FLOOD || this == DROUGHT || this == ACID_RAIN;
    }

// 是否为水质灾难：不自动冒出，必须由玩家用水质检测器测量并上报
    public boolean isWaterQuality() {
        return this == HYPOXIA || this == OXYGEN_OVER || this == ACID_RAIN
                || this == STRONG_ALKALI || this == BARREN_WATER || this == STENCH || this == OSMOSIS;
    }

// 是否参与每日随机掷骰（水质灾难不参与，只能测量上报）
    public boolean isRandomlyTriggered() {
        return this == FIRE || this == APHIDS || this == WEEDS || this == FLOOD || this == DROUGHT;
    }

// 对应的本地化键：gui.flavor_immersed_daily_argo.forestry.event.<小写枚举名>
    public String translationKey() {
        return "gui.flavor_immersed_daily_argo.forestry.event." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
