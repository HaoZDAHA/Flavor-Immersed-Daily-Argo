package com.flavor_immersed_daily_argo.forestry;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

// 网点水质模型：五项指标（溶解氧、pH、有机物质、电导率、浊度）与由此推出的水质灾难。
//
// 指标统一用 0~100 表示（pH 亦如此，50 对应中性 7.0）。每日随机漂移一次；
// 浊度过低会拉低有机物质指标，过高会拉低溶解氧。指标越界即对应一种水质灾难，
// 由玩家用水质检测器测量后上报到写字板。
public final class ForestryWater {

// 正常水位下限（米）
    public static final int LEVEL_MIN = 60;
// 正常水位上限（米）
    public static final int LEVEL_MAX = 66;

// 溶解氧过低阈值
    private static final int OXYGEN_LOW = 30;
// 溶解氧过高阈值
    private static final int OXYGEN_HIGH = 90;
// pH 过低阈值
    private static final int PH_LOW = 30;
// pH 过高阈值
    private static final int PH_HIGH = 85;
// 有机物质过低阈值
    private static final int ORGANIC_LOW = 25;
// 有机物质过高阈值
    private static final int ORGANIC_HIGH = 85;
// 电导率过低阈值
    private static final int CONDUCTIVITY_LOW = 20;
// 电导率过高阈值
    private static final int CONDUCTIVITY_HIGH = 90;
// 浊度偏低（营养不足）阈值
    private static final int TURBIDITY_LOW = 20;
// 浊度偏高（水体浑浊）阈值
    private static final int TURBIDITY_HIGH = 80;

    private ForestryWater() {
    }

// 由当前五项指标推出全部处于激活状态的水质灾难（按固定优先级排列）
    public static List<ForestryEventType> disasters(int oxygen, int ph, int organic, int conductivity) {
        List<ForestryEventType> out = new ArrayList<>(2);
        if (oxygen < OXYGEN_LOW) {
            out.add(ForestryEventType.HYPOXIA);
        } else if (oxygen > OXYGEN_HIGH) {
            out.add(ForestryEventType.OXYGEN_OVER);
        }
        if (ph < PH_LOW) {
            out.add(ForestryEventType.ACID_RAIN);
        } else if (ph > PH_HIGH) {
            out.add(ForestryEventType.STRONG_ALKALI);
        }
        if (organic < ORGANIC_LOW) {
            out.add(ForestryEventType.BARREN_WATER);
        } else if (organic > ORGANIC_HIGH) {
            out.add(ForestryEventType.STENCH);
        }
        if (conductivity < CONDUCTIVITY_LOW || conductivity > CONDUCTIVITY_HIGH) {
            out.add(ForestryEventType.OSMOSIS);
        }
        return out;
    }

// 取优先级最高的一条水质灾难；一切正常时返回 ForestryEventType#NONE
    public static ForestryEventType primaryDisaster(int oxygen, int ph, int organic, int conductivity) {
        List<ForestryEventType> list = disasters(oxygen, ph, organic, conductivity);
        return list.isEmpty() ? ForestryEventType.NONE : list.get(0);
    }

// 综合水质评分 0~100：五项指标各自给分后取平均，越接近理想区间分越高
    public static int comprehensive(int oxygen, int ph, int organic, int conductivity, int turbidity) {
        int total = metricScore(oxygen, 45, 80) + metricScore(ph, 40, 65)
                + metricScore(organic, 35, 70) + metricScore(conductivity, 35, 75)
                + metricScore(turbidity, 25, 65);
        return Mth.clamp(total / 5, 0, 100);
    }

// 单项指标得分：落在 [lowGood, highGood] 内满分，越往外线性递减
    private static int metricScore(int value, int lowGood, int highGood) {
        if (value >= lowGood && value <= highGood) {
            return 100;
        }
        int distance = value < lowGood ? lowGood - value : value - highGood;
        return Mth.clamp(100 - distance * 2, 0, 100);
    }

// pH 指标显示值（0~14，保留一位小数）
    public static float phValue(int ph) {
        return Mth.clamp(ph / 100.0F * 14.0F, 0.0F, 14.0F);
    }

// 综合水质文案
    public static String qualityText(int comprehensive) {
        if (comprehensive >= 85) {
            return "优";
        }
        if (comprehensive >= 70) {
            return "良";
        }
        if (comprehensive >= 50) {
            return "中";
        }
        return "差";
    }

// 每日随机漂移：五项指标小幅游走，并让浊度反过来影响有机物质与溶解氧
    public static int[] drift(RandomSource random, int oxygen, int ph, int organic, int conductivity, int turbidity) {
        int nextOxygen = oxygen + random.nextInt(11) - 5;
        int nextPh = ph + random.nextInt(9) - 4;
        int nextOrganic = organic + random.nextInt(9) - 4;
        int nextConductivity = conductivity + random.nextInt(9) - 4;
        int nextTurbidity = turbidity + random.nextInt(11) - 5;

// 浊度太低 → 有机物质指标下降；浊度太高 → 溶解氧下降
        if (nextTurbidity < TURBIDITY_LOW) {
            nextOrganic -= 3;
        } else if (nextTurbidity > TURBIDITY_HIGH) {
            nextOxygen -= 3;
        }
        return new int[]{
                Mth.clamp(nextOxygen, 0, 100), Mth.clamp(nextPh, 0, 100),
                Mth.clamp(nextOrganic, 0, 100), Mth.clamp(nextConductivity, 0, 100),
                Mth.clamp(nextTurbidity, 0, 100)
        };
    }

// 随机一个正常水位（60~66 米）
    public static int normalLevel(RandomSource random) {
        return LEVEL_MIN + random.nextInt(LEVEL_MAX - LEVEL_MIN + 1);
    }

// 洪水 / 干涸时的异常水位
    public static int anomalousLevel(RandomSource random, ForestryEventType type) {
        if (type == ForestryEventType.FLOOD) {
            return LEVEL_MAX + 6 + random.nextInt(9);
        }
        if (type == ForestryEventType.DROUGHT) {
            return Math.max(20, LEVEL_MIN - 12 - random.nextInt(12));
        }
        return normalLevel(random);
    }

// 把某一项指标强行压到能触发指定水质灾难的区间（指令 / 调试用），其余指标保持不变。
// 传入的数组顺序为 溶解氧 / pH / 有机物质 / 电导率 / 浊度。
    public static int[] applyDisaster(int[] metrics, ForestryEventType type) {
        int oxygen = metrics[0];
        int ph = metrics[1];
        int organic = metrics[2];
        int conductivity = metrics[3];
        int turbidity = metrics[4];
        switch (type) {
            case HYPOXIA -> oxygen = 10;
            case OXYGEN_OVER -> oxygen = 98;
            case ACID_RAIN -> ph = 10;
            case STRONG_ALKALI -> ph = 98;
            case BARREN_WATER -> organic = 8;
            case STENCH -> organic = 98;
            case OSMOSIS -> conductivity = 98;
            default -> {
            }
        }
        return new int[]{oxygen, ph, organic, conductivity, turbidity};
    }

// 随机一组初始水质指标（大体处于健康区间）
    public static int[] initialMetrics(RandomSource random) {
        return new int[]{
                50 + random.nextInt(31),   // 溶解氧 50~80
                45 + random.nextInt(21),   // pH 45~65
                45 + random.nextInt(21),   // 有机物质 45~65
                40 + random.nextInt(31),   // 电导率 40~70
                30 + random.nextInt(31)    // 浊度 30~60
        };
    }
}
