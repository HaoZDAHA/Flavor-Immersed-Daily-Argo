package com.flavor_immersed_daily_argo.fishing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// 由六个改装槽位实时推算出的钓竿实际参数。
//
// 槽位顺序：0 鱼线、1 竿体、2 线轮、3 鱼饵、4 浮漂、5 铅坠。
//
// 计算公式（锚点：基准耐久 60、基准拉力 10、基准容错 30s、基准动作 7 次、默认浮漂深度 64；
// 未安装的配件各系数按 0）：
//
// 临界耐久 = min(竿体耐久, 鱼线耐久)，某一侧未装时该侧取 60（都没装即 60）
// 容错时长 = 基准 30s + 线轮泄力 × 2.0 + 鱼线延展 × 1.5 - 竿体硬度 × 1.0
// 收杆动作次数 = 基准 7 - 线轮收线速度 + 竿体硬度，下限 3
// 抗流能力 = 铅坠重量 + 浮漂抗流 + 竿体硬度，下限 1
// 深度区间 = 浮漂设定深度 ± 铅坠重量 × 0.5（未装浮漂取默认深度 64，即 [64,64]）
// 饵料 = 鱼饵注册名
// 最大拉力 = min(竿体拉力, 鱼线拉力)，某一侧未装时该侧取 10（都没装即 10）
// 钓组显眼度 = (鱼线粗细 + 浮漂大小) / 抗流能力
public final class FishingRodStats {

    public static final int SLOT_LINE = 0;
    public static final int SLOT_BODY = 1;
    public static final int SLOT_REEL = 2;
    public static final int SLOT_BAIT = 3;
    public static final int SLOT_FLOAT = 4;
    public static final int SLOT_SINKER = 5;

// 基准容错时长（秒）
    public static final double BASE_TOLERANCE_SECONDS = 30.0D;
// 基准收杆动作次数
    public static final int BASE_REEL_ACTIONS = 7;
// 收杆动作次数下限
    public static final int MIN_REEL_ACTIONS = 3;
// 基准耐久：也是「无竿体配件」「无鱼线配件」时各自的默认耐久
    public static final double BASE_DURABILITY = 60.0D;
// 无竿体 / 无鱼线配件时的默认拉力
    public static final double DEFAULT_TENSION = 10.0D;
// 无浮漂配件时的默认设定深度（水面下 64）
    public static final double DEFAULT_FLOAT_DEPTH = 64.0D;
// 未装竿体 / 鱼线时的最大拉力（= 默认拉力），供未收录战利品兜底
    public static final double DEFAULT_MAX_TENSION = DEFAULT_TENSION;
// 每 1 点抗流能力为收杆时间带来的加成比例
    public static final double ANTI_CURRENT_TIME_BONUS = 0.25D;

    public final double criticalDurability;
    public final double toleranceSeconds;
    public final int reelActions;
    public final double currentResistance;
    public final double depthMin;
    public final double depthMax;
// 鱼饵注册名；未装为 null
    public final ResourceLocation baitId;
    public final double maxTension;
// 钓组显眼度
    public final double conspicuousness;

    private FishingRodStats(double criticalDurability, double toleranceSeconds, int reelActions,
                            double currentResistance, double depthMin, double depthMax, ResourceLocation baitId,
                            double maxTension, double conspicuousness) {
        this.criticalDurability = criticalDurability;
        this.toleranceSeconds = toleranceSeconds;
        this.reelActions = reelActions;
        this.currentResistance = currentResistance;
        this.depthMin = depthMin;
        this.depthMax = depthMax;
        this.baitId = baitId;
        this.maxTension = maxTension;
        this.conspicuousness = conspicuousness;
    }

    public static FishingRodStats of(List<ItemStack> mods) {
        FishingRodPartData data = FishingRodPartData.getInstance();
        FishingRodPartData.PartStats line = data.statsOf(mods, SLOT_LINE);
        FishingRodPartData.PartStats body = data.statsOf(mods, SLOT_BODY);
        FishingRodPartData.PartStats reel = data.statsOf(mods, SLOT_REEL);
        FishingRodPartData.PartStats floater = data.statsOf(mods, SLOT_FLOAT);
        FishingRodPartData.PartStats sinker = data.statsOf(mods, SLOT_SINKER);

        boolean hasLine = isInstalled(mods, SLOT_LINE);
        boolean hasBody = isInstalled(mods, SLOT_BODY);
        boolean hasFloat = isInstalled(mods, SLOT_FLOAT);

// 1. 临界耐久 = min(竿体耐久, 鱼线耐久)；某一侧未装（或未定义数值）时该侧取基准耐久 60
        double bodyDurability = hasBody && body.durability() > 0 ? body.durability() : BASE_DURABILITY;
        double lineDurability = hasLine && line.durability() > 0 ? line.durability() : BASE_DURABILITY;
        double criticalDurability = Math.min(bodyDurability, lineDurability);

// 2. 容错时长
        double toleranceSeconds = BASE_TOLERANCE_SECONDS
                + reel.drag() * 2.0D
                + line.stretch() * 1.5D
                - body.hardness() * 1.0D;

// 3. 收杆动作次数
        int reelActions = Math.max(MIN_REEL_ACTIONS,
                (int) Math.round(BASE_REEL_ACTIONS - reel.reelSpeed() + body.hardness()));

// 4. 抗流能力
        double currentResistance = Math.max(1.0D,
                sinker.weight() + floater.antiCurrent() + body.hardness());

// 5. 深度区间 = 浮漂设定深度 ± 铅坠重量 × 0.5；未装浮漂（或未定义）时设定深度取 64
        double setDepth = hasFloat && floater.setDepth() > 0 ? floater.setDepth() : DEFAULT_FLOAT_DEPTH;
        double depthMin = setDepth - sinker.weight() * 0.5D;
        double depthMax = setDepth + sinker.weight() * 0.5D;

// 6. 饵料
        ResourceLocation baitId = isInstalled(mods, SLOT_BAIT)
                ? BuiltInRegistries.ITEM.getKey(mods.get(SLOT_BAIT).getItem())
                : null;

// 7. 最大拉力 = min(竿体拉力, 鱼线拉力)；某一侧未装（或未定义数值）时该侧取默认拉力 10
        double bodyTension = hasBody && body.tension() > 0 ? body.tension() : DEFAULT_TENSION;
        double lineTension = hasLine && line.tension() > 0 ? line.tension() : DEFAULT_TENSION;
        double maxTension = Math.min(bodyTension, lineTension);

// 8. 钓组显眼度
        double conspicuousness = (line.thickness() + floater.size()) / currentResistance;

        return new FishingRodStats(criticalDurability, toleranceSeconds, reelActions, currentResistance,
                depthMin, depthMax, baitId, maxTension, conspicuousness);
    }

    private static boolean isInstalled(List<ItemStack> mods, int slot) {
        return slot >= 0 && slot < mods.size() && !mods.get(slot).isEmpty();
    }

// 拉力比 = ceil(鱼的最大拉力 / 鱼竿最大拉力)，下限 1。
// 既是每次成功拉扯消耗的临界耐久点数，也是判定断线 / 断杆的依据。
    public int tensionRatioTo(double fishTension) {
        return Math.max(1, (int) Math.ceil(fishTension / Math.max(0.01D, this.maxTension)));
    }

// 玩家拖拽收杆的有效反应时间（秒）= 容错时长 / 鱼进食速度 × 抗流加成。
// 抗流能力越强，钓组越不容易被水流带动、鱼越难借流挣脱，玩家可用的收杆窗口越长。
    public double reactionSeconds(double feedingFactor) {
        return this.toleranceSeconds / Math.max(0.01D, feedingFactor)
                * (1.0D + this.currentResistance * ANTI_CURRENT_TIME_BONUS);
    }

// 有效咬钩率 = max(5%, 90% × (1 - 鱼警惕性 × 钓组显眼度))
    public double biteChance(double alertnessFactor) {
        return Math.max(0.05D, 0.9D * (1.0D - alertnessFactor * this.conspicuousness));
    }
}
