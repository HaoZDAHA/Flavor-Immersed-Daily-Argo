package com.flavor_immersed_daily_argo.forestry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

// 一个林业网点：玩家用护林员写字板标记的一株树的某格原木。
//
// 网点以「区块」为唯一单位（一个区块只允许一处），以被标记的那格原木作为定位点。
// 评分范围 0~100，新建时为 #START_SCORE；处理掉一次随机情况加分，
// 拖着不处理则逐日扣分。评分达到 #PROSPERITY_SCORE 时该网点「兴盛」，
// 所在区块享受各项加成。
public final class ForestryPoint {

    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 100;
// 新建网点的初始评分
    public static final int START_SCORE = 60;
// 达到这个评分即为「兴盛」网点，所在区块享受加成
    public static final int PROSPERITY_SCORE = 80;
// 处理一次随机情况加多少分
    public static final int REWARD = 12;
// 随机情况冒头后每拖一天扣多少分
    public static final int DECAY_PER_DAY = 8;
// 随机情况出现后的宽限天数，超过这个天数才开始逐日扣分
    public static final int GRACE_DAYS = 2;
// 网点名字的最大长度
    public static final int MAX_NAME_LENGTH = 24;

// 水质指标全部正常时的默认值（溶解氧 / pH / 有机物质 / 电导率 / 浊度）
    private static final int[] DEFAULT_METRICS = {70, 55, 55, 55, 45};
// 默认水位（米）
    private static final int DEFAULT_LEVEL = (ForestryWater.LEVEL_MIN + ForestryWater.LEVEL_MAX) / 2;

    private final ResourceLocation dimension;
    private final BlockPos pos;
    private int score;
    private ForestryEventType event;
    private long eventSinceDay;
    private long lastTreatedDay;
// 玩家给网点起的名字；空串表示使用默认名字（界面上按坐标生成）
    private String name;

// 水位（米），正常 60~66，洪水 / 干涸时异常
    private int waterLevel;
// 溶解氧 0~100
    private int oxygen;
// pH 0~100（50 对应中性 7.0）
    private int ph;
// 有机物质指标 0~100
    private int organic;
// 电导率 0~100
    private int conductivity;
// 浊度 0~100
    private int turbidity;
// 玩家用水质检测器上报的水体灾难；ForestryEventType#NONE 表示未上报 / 水质正常
    private ForestryEventType waterReport;

    public ForestryPoint(ResourceLocation dimension, BlockPos pos, int score,
                         ForestryEventType event, long eventSinceDay, long lastTreatedDay, String name,
                         int waterLevel, int oxygen, int ph, int organic, int conductivity, int turbidity,
                         ForestryEventType waterReport) {
        this.dimension = dimension;
        this.pos = pos.immutable();
        this.score = clamp(score);
        this.event = event == null ? ForestryEventType.NONE : event;
        this.eventSinceDay = eventSinceDay;
        this.lastTreatedDay = lastTreatedDay;
        this.name = sanitize(name);
        this.waterLevel = waterLevel;
        this.oxygen = oxygen;
        this.ph = ph;
        this.organic = organic;
        this.conductivity = conductivity;
        this.turbidity = turbidity;
        this.waterReport = waterReport == null ? ForestryEventType.NONE : waterReport;
    }

// 新建一个平安无事的网点（名字留空，界面按坐标显示默认名；水质取健康默认值）
    public static ForestryPoint create(ResourceLocation dimension, BlockPos pos, long day) {
        return new ForestryPoint(dimension, pos, START_SCORE, ForestryEventType.NONE, -1L, day, "",
                DEFAULT_LEVEL, DEFAULT_METRICS[0], DEFAULT_METRICS[1], DEFAULT_METRICS[2],
                DEFAULT_METRICS[3], DEFAULT_METRICS[4], ForestryEventType.NONE);
    }

// 清洗名字：去掉会破坏同步文本的竖线与换行，并限制长度（空串保留为「用默认名」）
    public static String sanitize(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.replace("|", "").replace("\n", "").replace("\r", "").trim();
        return cleaned.length() > MAX_NAME_LENGTH ? cleaned.substring(0, MAX_NAME_LENGTH) : cleaned;
    }

    public static int clamp(int value) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, value));
    }

    public ResourceLocation dimension() {
        return this.dimension;
    }

    public BlockPos pos() {
        return this.pos;
    }

    public int score() {
        return this.score;
    }

    public ForestryEventType event() {
        return this.event;
    }

    public long eventSinceDay() {
        return this.eventSinceDay;
    }

    public long lastTreatedDay() {
        return this.lastTreatedDay;
    }

// 玩家起的名字；空串表示用默认名字
    public String name() {
        return this.name;
    }

    public void setName(String name) {
        this.name = sanitize(name);
    }

    public int waterLevel() {
        return this.waterLevel;
    }

    public int oxygen() {
        return this.oxygen;
    }

    public int ph() {
        return this.ph;
    }

    public int organic() {
        return this.organic;
    }

    public int conductivity() {
        return this.conductivity;
    }

    public int turbidity() {
        return this.turbidity;
    }

    public void setWaterLevel(int level) {
        this.waterLevel = level;
    }

// 覆盖五项水质指标（溶解氧 / pH / 有机物质 / 电导率 / 浊度）
    public void setMetrics(int[] metrics) {
        if (metrics.length >= 5) {
            this.oxygen = metrics[0];
            this.ph = metrics[1];
            this.organic = metrics[2];
            this.conductivity = metrics[3];
            this.turbidity = metrics[4];
        }
    }

// 玩家上报的水体灾难；ForestryEventType#NONE 表示未上报 / 水质正常
    public ForestryEventType waterReport() {
        return this.waterReport;
    }

    public void setWaterReport(ForestryEventType type) {
        this.waterReport = type == null ? ForestryEventType.NONE : type;
    }

// 所在区块的打包坐标，用来判断「同一区块只能有一处网点」
    public long chunkKey() {
        return ChunkPos.asLong(this.pos);
    }

// 评分够高即为「兴盛」网点
    public boolean prosperous() {
        return this.score >= PROSPERITY_SCORE;
    }

    public void addScore(int delta) {
        this.score = clamp(this.score + delta);
    }

// 触发一次随机情况
    public void startEvent(ForestryEventType type, long day) {
        this.event = type;
        this.eventSinceDay = day;
    }

// 处理完当前随机情况：清除事件、记下处理日，并加分
    public void resolve(long day) {
        this.event = ForestryEventType.NONE;
        this.eventSinceDay = -1L;
        this.lastTreatedDay = day;
        addScore(REWARD);
    }

// 只清除事件、不加分（例如火被玩家徒手打掉，或事件无法落地）
    public void clearEvent() {
        this.event = ForestryEventType.NONE;
        this.eventSinceDay = -1L;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", this.dimension.toString());
        tag.putLong("pos", this.pos.asLong());
        tag.putInt("score", this.score);
        tag.putInt("event", this.event.ordinal());
        tag.putLong("event_since", this.eventSinceDay);
        tag.putLong("treated", this.lastTreatedDay);
        tag.putString("name", this.name);
        tag.putInt("water_level", this.waterLevel);
        tag.putInt("oxygen", this.oxygen);
        tag.putInt("ph", this.ph);
        tag.putInt("organic", this.organic);
        tag.putInt("conductivity", this.conductivity);
        tag.putInt("turbidity", this.turbidity);
        tag.putInt("water_report", this.waterReport.ordinal());
        return tag;
    }

// 从存档读取；维度非法时返回 null，由调用方丢弃这一条
    public static ForestryPoint load(CompoundTag tag) {
        ResourceLocation dim = ResourceLocation.tryParse(tag.getString("dim"));
        if (dim == null) {
            return null;
        }
        return new ForestryPoint(dim, BlockPos.of(tag.getLong("pos")), tag.getInt("score"),
                ForestryEventType.byOrdinal(tag.getInt("event")),
                tag.getLong("event_since"), tag.getLong("treated"), tag.getString("name"),
                tag.contains("water_level") ? tag.getInt("water_level") : DEFAULT_LEVEL,
                tag.contains("oxygen") ? tag.getInt("oxygen") : DEFAULT_METRICS[0],
                tag.contains("ph") ? tag.getInt("ph") : DEFAULT_METRICS[1],
                tag.contains("organic") ? tag.getInt("organic") : DEFAULT_METRICS[2],
                tag.contains("conductivity") ? tag.getInt("conductivity") : DEFAULT_METRICS[3],
                tag.contains("turbidity") ? tag.getInt("turbidity") : DEFAULT_METRICS[4],
                tag.contains("water_report")
                        ? ForestryEventType.byOrdinal(tag.getInt("water_report")) : ForestryEventType.NONE);
    }

// 打包成一行文本，随同步包发给客户端
    public String encode() {
        return this.dimension + "|" + this.pos.asLong() + "|" + this.score + "|"
                + this.event.ordinal() + "|" + this.eventSinceDay + "|" + this.lastTreatedDay
                + "|" + this.name + "|" + this.waterLevel + "|" + this.oxygen + "|" + this.ph
                + "|" + this.organic + "|" + this.conductivity + "|" + this.turbidity
                + "|" + this.waterReport.ordinal();
    }

    public static ForestryPoint decode(String text) {
        String[] parts = text.split("\\|", -1);
        if (parts.length < 6) {
            return null;
        }
        ResourceLocation dim = ResourceLocation.tryParse(parts[0]);
        if (dim == null) {
            return null;
        }
        try {
            return new ForestryPoint(dim, BlockPos.of(Long.parseLong(parts[1])),
                    Integer.parseInt(parts[2]), ForestryEventType.byOrdinal(Integer.parseInt(parts[3])),
                    Long.parseLong(parts[4]), Long.parseLong(parts[5]),
                    parts.length >= 7 ? parts[6] : "",
                    parts.length >= 8 ? Integer.parseInt(parts[7]) : DEFAULT_LEVEL,
                    parts.length >= 9 ? Integer.parseInt(parts[8]) : DEFAULT_METRICS[0],
                    parts.length >= 10 ? Integer.parseInt(parts[9]) : DEFAULT_METRICS[1],
                    parts.length >= 11 ? Integer.parseInt(parts[10]) : DEFAULT_METRICS[2],
                    parts.length >= 12 ? Integer.parseInt(parts[11]) : DEFAULT_METRICS[3],
                    parts.length >= 13 ? Integer.parseInt(parts[12]) : DEFAULT_METRICS[4],
                    parts.length >= 14 ? ForestryEventType.byOrdinal(Integer.parseInt(parts[13]))
                            : ForestryEventType.NONE);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
