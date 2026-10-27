package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModAttachments;
import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.all.ModMobEffects;
import com.flavor_immersed_daily_argo.entity.Poop;
import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

// 家养动物的状态栏（饱食度 / 口渴度 / 清洁度 / 心情值 / 体重）的规则中心。
//
// 数值变化（全部在服务端结算，每 5 秒一轮、每 30 秒一次衰减）：
//
// 衰减：饱食度、口渴度、清洁度每 30 秒各 -1，最低 0；
// 心情值：同上节奏 —— 三项都 ≥ 7 时 +1，任意一项掉到 0 时 -1，否则不动；
// 体重：和心情值同一个判定 —— 状态好就往上长一点、状态差就往下掉一点，
// 幅度是各自体重标准的一个比例，收口在标准的
// #WEIGHT_MIN_RATIO~#WEIGHT_MAX_RATIO 之间（见 #WEIGHT_STANDARDS）；
// 口渴度：身边 2 格内有水就自动饮水，每 5 秒 +1；
// 清洁度：泡在水里或淋雨时每 5 秒 +1；
// 饱食度：被喂食 +3、心情值 +1（见 #onFed）。
//
// 状态栏本身不会凭空出现：只有被玩家喂食过的动物才会有。拥有状态栏的动物生下的后代
// 会继承；在拥有状态栏的同类成年个体身边长大的幼崽，长大成人时也会补上。
public final class AnimalStatsManager {

// 拥有状态栏的动物：禽类、家畜、宠物
    public static final Set<EntityType<?>> TRACKED = Set.of(
            EntityType.CHICKEN, EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.GOAT,
            EntityType.RABBIT, EntityType.CAT, EntityType.WOLF, EntityType.HORSE, EntityType.DONKEY,
            EntityType.CAMEL, EntityType.LLAMA,
            ModEntities.DUCK.get(), ModEntities.GOOSE.get(), ModEntities.QUAIL.get(),
            ModEntities.PIGEON.get(), ModEntities.TURKEY.get(),
            ModEntities.BAMBOORAT.get(), ModEntities.BULLFROG.get());

// 各动物的体重标准（kg）。
//
// 只有列在这里的动物才有体重：猫狗不在其中 —— 它们没有体重标准，体重恒为 0，
// 面板上也不显示体重那一行。刚接手的动物按标准给体重，之后每 30 秒的衰减结算里
// 随状态好坏上下浮动（好就长 #WEIGHT_STEP_RATIO 个标准，差就掉同样多）。
    public static final Map<EntityType<?>, Float> WEIGHT_STANDARDS = weightStandards();

    private static Map<EntityType<?>, Float> weightStandards() {
        Map<EntityType<?>, Float> map = new LinkedHashMap<>();
// 家禽
        map.put(EntityType.CHICKEN, 2.5F);
        map.put(ModEntities.DUCK.get(), 3.0F);
        map.put(ModEntities.GOOSE.get(), 4.5F);
        map.put(ModEntities.QUAIL.get(), 0.3F);
        map.put(ModEntities.PIGEON.get(), 0.4F);
        map.put(ModEntities.TURKEY.get(), 9.0F);
// 家畜
        map.put(EntityType.PIG, 120.0F);
        map.put(EntityType.COW, 450.0F);
        map.put(EntityType.SHEEP, 65.0F);
        map.put(EntityType.GOAT, 45.0F);
        map.put(EntityType.RABBIT, 2.0F);
        map.put(EntityType.HORSE, 480.0F);
        map.put(EntityType.DONKEY, 150.0F);
        map.put(EntityType.CAMEL, 480.0F);
        map.put(EntityType.LLAMA, 140.0F);
// 本地特有种
        map.put(ModEntities.BAMBOORAT.get(), 2.5F);
        map.put(ModEntities.BULLFROG.get(), 0.6F);
        return map;
    }

// 这种动物的体重标准（kg）；没有登记体重的动物（猫狗）返回 null
    @Nullable
    public static Float weightStandard(EntityType<?> type) {
        return WEIGHT_STANDARDS.get(type);
    }

// 状态好 / 坏时体重每次结算浮动的幅度（占体重标准的比例）
    private static final float WEIGHT_STEP_RATIO = 0.02F;
// 体重能掉到 / 长到标准的多少倍
    private static final float WEIGHT_MIN_RATIO = 0.7F;
    private static final float WEIGHT_MAX_RATIO = 1.3F;

// 饮水 / 洗澡的结算间隔（5 秒）
    private static final int RESTORE_INTERVAL = 100;
// 衰减的结算间隔（30 秒），必须是 #RESTORE_INTERVAL 的整数倍
    private static final int DECAY_INTERVAL = 600;
// 判断「身边有水」的范围（水平半径 2 格、上下各 1 格）
    private static final int WATER_RADIUS = 2;
// 判断幼崽「在谁身边长大」的范围
    private static final double RAISE_RADIUS = 8.0D;

// 被喂食一次涨多少饱食度 / 心情值
    private static final int FEED_SATIETY = 3;
    private static final int FEED_MOOD = 1;
// 心情好 / 心情差的判定线
    private static final int MOOD_GOOD_LINE = 7;

// 饱食度每消耗一次就拉一坨的概率
    private static final float POOP_CHANCE = 0.25F;
// 同一个区块里最多允许多少坨粪便，超了就这次不拉
    private static final int POOP_PER_CHUNK = 6;

// 身边多少格内的粪便 / 尸体算「接触过多」
    private static final double FILTH_RADIUS = 3.0D;
// 触发蝇虫叮咬需要接触的粪便 / 尸体数量
    private static final int FILTH_THRESHOLD = 3;
// 蝇虫叮咬期间清洁度每次衰减掉的点数（正常是 1）
    private static final int FLY_BITE_DECAY = 5;
// 蝇虫叮咬期间清洁度能到的最高值
    public static final int FLY_BITE_CLEANLINESS_MAX = 5;

// 死动物物品（尸体）：和粪便一起算作招惹蝇虫的「脏东西」
    private static Set<Item> carcasses;

    private static Set<Item> carcasses() {
        if (carcasses == null) {
            carcasses = Set.of(
                    ModItems.DEAD_DUCK.get(), ModItems.DEAD_GOOSE.get(), ModItems.DEAD_QUAIL.get(),
                    ModItems.DEAD_PIGEON.get(), ModItems.DEAD_TURKEY.get(),
                    ModItems.DEAD_DOG.get(), ModItems.DEAD_DONKEY.get(), ModItems.DEAD_CAMEL.get(),
                    ModItems.DEAD_RABBIT.get(), ModItems.DEAD_LLAMA.get());
        }
        return carcasses;
    }

    private AnimalStatsManager() {
    }

    public static boolean isTracked(Entity entity) {
        return TRACKED.contains(entity.getType());
    }

// 这只动物的状态栏；还没被喂食过则返回 null
    @Nullable
    public static AnimalStats getStats(LivingEntity entity) {
        AnimalStats stats = ModAttachments.ANIMAL_STATS.get().getOrNull(entity);
        return stats != null && stats.active() ? stats : null;
    }

    public static void setStats(LivingEntity entity, AnimalStats stats) {
// 数值没变就不写回：这个附件是同步的，多余的写回等于给附近客户端多发一个包
        if (stats.equals(ModAttachments.ANIMAL_STATS.get().getOrNull(entity))) {
            return;
        }
        ModAttachments.ANIMAL_STATS.get().set(entity, stats);
    }

// 被玩家喂食：第一次喂食让动物获得状态栏，之后每次饱食度 +3、心情值 +1
    public static void onFed(LivingEntity entity) {
        AnimalStats stats = getStats(entity);
        if (stats == null) {
            setStats(entity, AnimalStats.fresh(initialWeight(entity))
                    .withSatiety(FEED_SATIETY).withMood(FEED_MOOD));
            return;
        }
        setStats(entity, stats.withSatiety(stats.satiety() + FEED_SATIETY).withMood(stats.mood() + FEED_MOOD));
    }

// 刚获得状态栏时的体重：有体重标准的按标准给，没标准的（猫狗）为 0
    private static float initialWeight(LivingEntity entity) {
        Float standard = weightStandard(entity.getType());
        return standard == null ? 0.0F : standard;
    }

// 体重还没初始化过（旧存档里的动物）就按标准补上；没有标准的原样返回
    private static AnimalStats ensureWeight(LivingEntity entity, AnimalStats stats) {
        if (stats.weight() > 0.0F) {
            return stats;
        }
        Float standard = weightStandard(entity.getType());
        return standard == null ? stats : stats.withWeight(standard);
    }

// 每 tick 的结算

// 服务端每 tick 调用一次。真正的结算被拆到 5 秒 / 30 秒两个节拍上，
// 并用实体 id 错开，避免所有动物挤在同一 tick 里算。
    public static void serverTick(LivingEntity entity) {
        Level level = entity.level();
        long clock = level.getGameTime() + entity.getId();
        if (clock % RESTORE_INTERVAL != 0) {
            return;
        }
        AnimalStats stats = getStats(entity);
// 蝇虫叮咬跟状态栏无关：只要身边脏东西够多就沾上
        tryInfectFlies(entity);
        if (stats == null) {
// 还没有状态栏：只处理「在同类身边长大」这一条
            checkRaised(entity);
            return;
        }
        stats = ensureWeight(entity, stats);
        if (clock % DECAY_INTERVAL == 0) {
// 只有本来就有饱食度可消耗，才谈得上「消化完拉一坨」
            boolean hadSatiety = stats.satiety() > 0;
            stats = decay(entity, stats);
// 所在区块有「兴盛」林业网点时，牲畜心情额外 +1（林业网点的加成之一）
            if (ForestryNetworkManager.isProsperous(level, entity.blockPosition())) {
                stats = stats.withMood(stats.mood() + 1);
            }
            if (hadSatiety) {
                tryDropPoop(entity);
            }
        }
        stats = restore(level, entity, stats);
        setStats(entity, stats);
    }

// 蝇虫叮咬：身边堆着的粪便 / 尸体够多就会招来蝇虫，一旦沾上就是永久的
// （无限时长的 buff），而且当场把清洁度压到 #FLY_BITE_CLEANLINESS_MAX 以下。
//
// 只能靠体外驱虫药清掉，见 FleaTreatmentItem。
    private static void tryInfectFlies(LivingEntity entity) {
        if (entity.hasEffect(ModMobEffects.FLY_BITE.get())) {
            return;
        }
        Level level = entity.level();
        AABB box = entity.getBoundingBox().inflate(FILTH_RADIUS);
        int filth = level.getEntitiesOfClass(Poop.class, box).size();
        if (filth < FILTH_THRESHOLD) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
                if (carcasses().contains(item.getItem().getItem())) {
                    filth++;
                }
            }
        }
        if (filth < FILTH_THRESHOLD) {
            return;
        }
        entity.addEffect(new MobEffectInstance(ModMobEffects.FLY_BITE.get(), MobEffectInstance.INFINITE_DURATION, 0, false, false));
        AnimalStats stats = getStats(entity);
        if (stats != null) {
            setStats(entity, stats.withCleanliness(stats.cleanliness(), cleanlinessLimit(entity)));
        }
    }

// 饱食度消耗掉一点时，按概率在脚下拉一坨粪便。
//
// 同一个区块里最多 #POOP_PER_CHUNK 坨，免得圈里的动物一刻不停地拉、堆成山。
    private static void tryDropPoop(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (level.random.nextFloat() >= POOP_CHANCE) {
            return;
        }
        BlockPos pos = entity.blockPosition();
        int minX = (pos.getX() >> 4) << 4;
        int minZ = (pos.getZ() >> 4) << 4;
        AABB chunkBox = new AABB(minX, level.getMinBuildHeight(), minZ,
                minX + 16, level.getMaxBuildHeight(), minZ + 16);
        if (level.getEntitiesOfClass(Poop.class, chunkBox).size() >= POOP_PER_CHUNK) {
            return;
        }
        level.addFreshEntity(Poop.placed(level, entity.getX(), entity.getY(), entity.getZ()));
    }

// 自然衰减：三项基础状态各 -1，心情值与体重随基础状态的好坏浮动。
//
// 身上带着蝇虫叮咬时清洁度掉得飞快（一次 #FLY_BITE_DECAY 点），
// 而且这辈子都别想干净到 #FLY_BITE_CLEANLINESS_MAX 以上。
    private static AnimalStats decay(LivingEntity entity, AnimalStats stats) {
        boolean bitten = entity.hasEffect(ModMobEffects.FLY_BITE.get());
        int satiety = Math.max(0, stats.satiety() - 1);
        int thirst = Math.max(0, stats.thirst() - 1);
        int cleanliness = Math.max(0, stats.cleanliness() - (bitten ? FLY_BITE_DECAY : 1));
        int mood = stats.mood();
// 体重跟心情值走同一套判定：状态好一起长，状态差一起掉
        int weightTrend = 0;
        if (satiety >= MOOD_GOOD_LINE && thirst >= MOOD_GOOD_LINE && cleanliness >= MOOD_GOOD_LINE) {
            mood++;
            weightTrend = 1;
        } else if (satiety == 0 || thirst == 0 || cleanliness == 0) {
            mood--;
            weightTrend = -1;
        }
        AnimalStats result = stats.withSatiety(satiety).withThirst(thirst)
                .withCleanliness(cleanliness, cleanlinessLimit(entity))
                .withMood(mood);
        return applyWeight(entity, result, weightTrend);
    }

// 按状态好坏调整体重：状态好往上长、状态差往下掉，幅度是体重标准的
// #WEIGHT_STEP_RATIO，最终收口在标准的 #WEIGHT_MIN_RATIO~#WEIGHT_MAX_RATIO 之间。
//
// 没有体重标准的动物（猫狗）不动。
    private static AnimalStats applyWeight(LivingEntity entity, AnimalStats stats, int trend) {
        Float standard = weightStandard(entity.getType());
        if (standard == null || trend == 0) {
            return stats;
        }
        return stats.withWeight(Mth.clamp(stats.weight() + standard * WEIGHT_STEP_RATIO * trend,
                standard * WEIGHT_MIN_RATIO, standard * WEIGHT_MAX_RATIO));
    }

// 玩家行为之外的自发回补：靠近水喝水、泡水或淋雨洗澡
    private static AnimalStats restore(Level level, LivingEntity entity, AnimalStats stats) {
        int thirst = hasWaterNearby(level, entity) ? stats.thirst() + 1 : stats.thirst();
        int cleanliness = entity.isInWater() || level.isRainingAt(entity.blockPosition())
                ? stats.cleanliness() + 1
                : stats.cleanliness();
        return stats.withThirst(thirst).withCleanliness(cleanliness, cleanlinessLimit(entity));
    }

// 这只动物清洁度能到的上限：正常是 AnimalStats#MAX，
// 身上带着蝇虫叮咬的只到 #FLY_BITE_CLEANLINESS_MAX。
    public static int cleanlinessLimit(LivingEntity entity) {
        return entity.hasEffect(ModMobEffects.FLY_BITE.get()) ? FLY_BITE_CLEANLINESS_MAX : AnimalStats.MAX;
    }

// 刷毛梳子：心情值、清洁度各 +1（各自按上限收口）。
//
// 没有状态栏的动物不参与。
    public static void onBrushed(LivingEntity entity) {
        AnimalStats stats = getStats(entity);
        if (stats == null) {
            return;
        }
        setStats(entity, stats.withMood(stats.mood() + 1)
                .withCleanliness(stats.cleanliness() + 1, cleanlinessLimit(entity)));
    }

    private static boolean hasWaterNearby(Level level, LivingEntity entity) {
        BlockPos origin = entity.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-WATER_RADIUS, -1, -WATER_RADIUS),
                origin.offset(WATER_RADIUS, 1, WATER_RADIUS))) {
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

// 状态栏的扩散

// 「在拥有状态栏的同类身边长大」：幼崽期间被打上标记，长大成人时才补上状态栏。
    private static void checkRaised(LivingEntity entity) {
        if (!(entity instanceof AgeableMob mob)) {
            return;
        }
        if (mob.isBaby()) {
            if (findMentor(mob) != null) {
                ModAttachments.RAISED_NEARBY.get().set(mob, true);
            }
            return;
        }
        if (Boolean.TRUE.equals(ModAttachments.RAISED_NEARBY.get().getOrNull(mob))) {
            ModAttachments.RAISED_NEARBY.get().set(mob, false);
            setStats(mob, AnimalStats.fresh(initialWeight(mob)));
        }
    }

// 附近同种、已有状态栏的成年个体
    @Nullable
    private static LivingEntity findMentor(AgeableMob baby) {
        AABB box = baby.getBoundingBox().inflate(RAISE_RADIUS);
        for (LivingEntity other : baby.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (other == baby || other.isBaby() || other.getType() != baby.getType()) {
                continue;
            }
            if (getStats(other) != null) {
                return other;
            }
        }
        return null;
    }

// 后代继承：父母双方都有状态栏时取各自四项的平均值，只有一方有则照抄那一方。
    public static void inheritToChild(AgeableMob child, LivingEntity parentA, LivingEntity parentB) {
        AnimalStats a = getStats(parentA);
        AnimalStats b = getStats(parentB);
        if (a == null && b == null) {
            return;
        }
        if (a == null || b == null) {
            setStats(child, a != null ? a : b);
            return;
        }
        setStats(child, new AnimalStats(
                (a.satiety() + b.satiety()) / 2,
                (a.thirst() + b.thirst()) / 2,
                (a.cleanliness() + b.cleanliness()) / 2,
                (a.mood() + b.mood()) / 2,
                (a.weight() + b.weight()) / 2.0F,
                true));
    }
}
