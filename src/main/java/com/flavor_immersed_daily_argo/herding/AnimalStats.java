package com.flavor_immersed_daily_argo.herding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

// 一只家养动物的状态栏：饱食度、口渴度、清洁度、心情值，外加一条体重（kg）。
//
// 四项状态取值范围都是 0~10，默认 0。active 表示这只动物是否已经
// 「被玩家喂食过」从而拥有状态栏 —— 没有状态栏的动物这几项恒为 0，也不参与衰减，
// 因此需要一枚标记把它们和「尚未开始饲养」的同类区分开。
//
// weight 是体重，单位 kg。每种动物有自己的体重标准（见
// AnimalStatsManager#weightStandard）：刚接手饲养时按标准给，之后状态好就往上长、
// 状态差就往下掉。猫狗没有体重标准，它们的 weight 恒为 0，面板上也不显示这一行。
// 旧存档里没有这一项的动物读出来是 0，下一次结算时会按标准补上。
public record AnimalStats(int satiety, int thirst, int cleanliness, int mood, float weight, boolean active) {

// 单条状态栏的上限
    public static final int MAX = 10;

// 尚未拥有状态栏时的取值（四项为 0、体重 0，active 为 false）
    public static final AnimalStats NONE = new AnimalStats(0, 0, 0, 0, 0.0F, false);

    public static final Codec<AnimalStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("satiety").forGetter(AnimalStats::satiety),
            Codec.INT.fieldOf("thirst").forGetter(AnimalStats::thirst),
            Codec.INT.fieldOf("cleanliness").forGetter(AnimalStats::cleanliness),
            Codec.INT.fieldOf("mood").forGetter(AnimalStats::mood),
// 旧存档没有这一项，缺省 0 = 「还没按标准初始化」
            Codec.FLOAT.optionalFieldOf("weight", 0.0F).forGetter(AnimalStats::weight),
            Codec.BOOL.fieldOf("active").forGetter(AnimalStats::active)
    ).apply(instance, AnimalStats::new));

    public static final StreamCodec<ByteBuf, AnimalStats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AnimalStats::satiety,
            ByteBufCodecs.VAR_INT, AnimalStats::thirst,
            ByteBufCodecs.VAR_INT, AnimalStats::cleanliness,
            ByteBufCodecs.VAR_INT, AnimalStats::mood,
            ByteBufCodecs.FLOAT, AnimalStats::weight,
            ByteBufCodecs.BOOL, AnimalStats::active,
            AnimalStats::new);

// 刚获得状态栏时的取值：四项都是 0，体重按该动物的标准给
    public static AnimalStats fresh(float weight) {
        return new AnimalStats(0, 0, 0, 0, weight, true);
    }

    private static int clamp(int value) {
        return Mth.clamp(value, 0, MAX);
    }

    public AnimalStats withSatiety(int value) {
        return new AnimalStats(clamp(value), this.thirst, this.cleanliness, this.mood, this.weight, this.active);
    }

    public AnimalStats withThirst(int value) {
        return new AnimalStats(this.satiety, clamp(value), this.cleanliness, this.mood, this.weight, this.active);
    }

    public AnimalStats withCleanliness(int value) {
        return new AnimalStats(this.satiety, this.thirst, clamp(value), this.mood, this.weight, this.active);
    }

// 带上自定义上限的清洁度。
//
// 被蝇虫叮咬的动物再干净也只能到 AnimalStatsManager#FLY_BITE_CLEANLINESS_MAX，
// 所以这一条不能走固定上限 #MAX。
    public AnimalStats withCleanliness(int value, int limit) {
        return new AnimalStats(this.satiety, this.thirst, Mth.clamp(value, 0, limit), this.mood, this.weight, this.active);
    }

    public AnimalStats withMood(int value) {
        return new AnimalStats(this.satiety, this.thirst, this.cleanliness, clamp(value), this.weight, this.active);
    }

// 体重（kg）
    public AnimalStats withWeight(float value) {
        return new AnimalStats(this.satiety, this.thirst, this.cleanliness, this.mood, value, this.active);
    }
}
