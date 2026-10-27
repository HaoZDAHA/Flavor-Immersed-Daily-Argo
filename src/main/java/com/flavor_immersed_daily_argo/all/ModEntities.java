package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.entity.Anglerfish;
import com.flavor_immersed_daily_argo.entity.Bamboorat;
import com.flavor_immersed_daily_argo.entity.Bass;
import com.flavor_immersed_daily_argo.entity.BigheadCarp;
import com.flavor_immersed_daily_argo.entity.BlackCarp;
import com.flavor_immersed_daily_argo.entity.BlackFish;
import com.flavor_immersed_daily_argo.entity.BrownCrab;
import com.flavor_immersed_daily_argo.entity.Bullfrog;
import com.flavor_immersed_daily_argo.entity.Carton;
import com.flavor_immersed_daily_argo.entity.Catfish;
import com.flavor_immersed_daily_argo.entity.ChineseCarp;
import com.flavor_immersed_daily_argo.entity.ChineseMackerel;
import com.flavor_immersed_daily_argo.entity.CrucianCarp;
import com.flavor_immersed_daily_argo.entity.Customer;
import com.flavor_immersed_daily_argo.entity.Duck;
import com.flavor_immersed_daily_argo.entity.DeliveryPoint;
import com.flavor_immersed_daily_argo.entity.DeliveryRider;
import com.flavor_immersed_daily_argo.entity.Eel;
import com.flavor_immersed_daily_argo.entity.FallingFruit;
import com.flavor_immersed_daily_argo.entity.FishingFloatingPlatform;
import com.flavor_immersed_daily_argo.entity.Goby;
import com.flavor_immersed_daily_argo.entity.Goose;
import com.flavor_immersed_daily_argo.entity.GrassCarp;
import com.flavor_immersed_daily_argo.entity.Grouper;
import com.flavor_immersed_daily_argo.entity.Hairtail;
import com.flavor_immersed_daily_argo.entity.HairyCrab;
import com.flavor_immersed_daily_argo.entity.Handcart;
import com.flavor_immersed_daily_argo.entity.Harvester;
import com.flavor_immersed_daily_argo.entity.JapaneseHalfbeak;
import com.flavor_immersed_daily_argo.entity.KingCrab;
import com.flavor_immersed_daily_argo.entity.Loach;
import com.flavor_immersed_daily_argo.entity.Lobster;
import com.flavor_immersed_daily_argo.entity.MandarinFish;
import com.flavor_immersed_daily_argo.entity.Mouse;
import com.flavor_immersed_daily_argo.entity.MudCrab;
import com.flavor_immersed_daily_argo.entity.PacificSaury;
import com.flavor_immersed_daily_argo.entity.Pigeon;
import com.flavor_immersed_daily_argo.entity.Pleco;
import com.flavor_immersed_daily_argo.entity.PoisonBaitStation;
import com.flavor_immersed_daily_argo.entity.Poop;
import com.flavor_immersed_daily_argo.entity.ThrownPoop;
import com.flavor_immersed_daily_argo.entity.ThrownMarshGas;
import com.flavor_immersed_daily_argo.entity.ThrownPoultryEgg;
import com.flavor_immersed_daily_argo.entity.Uav;
import com.flavor_immersed_daily_argo.entity.Prawn;
import com.flavor_immersed_daily_argo.entity.Quail;
import com.flavor_immersed_daily_argo.entity.Rectum;
import com.flavor_immersed_daily_argo.entity.Redfin;
import com.flavor_immersed_daily_argo.entity.Sardine;
import com.flavor_immersed_daily_argo.entity.Scarecrow;
import com.flavor_immersed_daily_argo.entity.SilverCarp;
import com.flavor_immersed_daily_argo.entity.SilveryPomfret;
import com.flavor_immersed_daily_argo.entity.SwimmingCrab;
import com.flavor_immersed_daily_argo.entity.Swordfish;
import com.flavor_immersed_daily_argo.entity.TakeawayBag;
import com.flavor_immersed_daily_argo.entity.Tench;
import com.flavor_immersed_daily_argo.entity.Tilapia;
import com.flavor_immersed_daily_argo.entity.Tuna;
import com.flavor_immersed_daily_argo.entity.Turkey;
import com.flavor_immersed_daily_argo.entity.Turtle;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 实体类型注册
public final class ModEntities {

    private ModEntities() {
    }

    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.ENTITY_TYPE, flavor_immersed_daily_argo.MODID);

// 鸭
    public static final DeferredHolder<EntityType<?>, EntityType<Duck>> DUCK =
            REGISTRY.register("duck", () -> EntityType.Builder.of(Duck::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.7F)
                    .clientTrackingRange(10)
                    .build("duck"));

// 鹅
    public static final DeferredHolder<EntityType<?>, EntityType<Goose>> GOOSE =
            REGISTRY.register("goose", () -> EntityType.Builder.of(Goose::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.8F)
                    .clientTrackingRange(10)
                    .build("goose"));

// 鹌鹑
    public static final DeferredHolder<EntityType<?>, EntityType<Quail>> QUAIL =
            REGISTRY.register("quail", () -> EntityType.Builder.of(Quail::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.6F)
                    .clientTrackingRange(10)
                    .build("quail"));

// 鸽子
    public static final DeferredHolder<EntityType<?>, EntityType<Pigeon>> PIGEON =
            REGISTRY.register("pigeon", () -> EntityType.Builder.of(Pigeon::new, MobCategory.CREATURE)
                    .sized(0.35F, 0.6F)
                    .clientTrackingRange(10)
                    .build("pigeon"));

// 火鸡
    public static final DeferredHolder<EntityType<?>, EntityType<Turkey>> TURKEY =
            REGISTRY.register("turkey", () -> EntityType.Builder.of(Turkey::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.0F)
                    .clientTrackingRange(10)
                    .build("turkey"));

// 牛蛙
    public static final DeferredHolder<EntityType<?>, EntityType<Bullfrog>> BULLFROG =
            REGISTRY.register("bullfrog", () -> EntityType.Builder.of(Bullfrog::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .build("bullfrog"));

// 竹鼠
    public static final DeferredHolder<EntityType<?>, EntityType<Bamboorat>> BAMBOORAT =
            REGISTRY.register("bamboorat", () -> EntityType.Builder.of(Bamboorat::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .build("bamboorat"));

// 老鼠
// 田里的害兽：作物附近自然生成（见 entity/MouseSpawning），可用 /mouse spawn 开关
    public static final DeferredHolder<EntityType<?>, EntityType<Mouse>> MOUSE =
            REGISTRY.register("mouse", () -> EntityType.Builder.of(Mouse::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.4F)
                    .clientTrackingRange(10)
                    .build("mouse"));

// 草鱼
    public static final DeferredHolder<EntityType<?>, EntityType<GrassCarp>> GRASS_CARP =
            REGISTRY.register("grass_carp", () -> EntityType.Builder.of(GrassCarp::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("grass_carp"));

// 丁桂鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Tench>> TENCH =
            REGISTRY.register("tench", () -> EntityType.Builder.of(Tench::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("tench"));

// 鳜鱼
    public static final DeferredHolder<EntityType<?>, EntityType<MandarinFish>> MANDARIN_FISH =
            REGISTRY.register("mandarin_fish", () -> EntityType.Builder.of(MandarinFish::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("mandarin_fish"));

// 泥鳅
    public static final DeferredHolder<EntityType<?>, EntityType<Loach>> LOACH =
            REGISTRY.register("loach", () -> EntityType.Builder.of(Loach::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("loach"));

// 清道夫
    public static final DeferredHolder<EntityType<?>, EntityType<Pleco>> PLECO =
            REGISTRY.register("pleco", () -> EntityType.Builder.of(Pleco::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("pleco"));

// 爬地虎
    public static final DeferredHolder<EntityType<?>, EntityType<Goby>> GOBY =
            REGISTRY.register("goby", () -> EntityType.Builder.of(Goby::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("goby"));

// 白鲢
    public static final DeferredHolder<EntityType<?>, EntityType<SilverCarp>> SILVER_CARP =
            REGISTRY.register("silver_carp", () -> EntityType.Builder.of(SilverCarp::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("silver_carp"));

// 罗非鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Tilapia>> TILAPIA =
            REGISTRY.register("tilapia", () -> EntityType.Builder.of(Tilapia::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("tilapia"));

// 翘嘴
    public static final DeferredHolder<EntityType<?>, EntityType<Redfin>> REDFIN =
            REGISTRY.register("redfin", () -> EntityType.Builder.of(Redfin::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("redfin"));

// 青鱼
    public static final DeferredHolder<EntityType<?>, EntityType<BlackCarp>> BLACK_CARP =
            REGISTRY.register("black_carp", () -> EntityType.Builder.of(BlackCarp::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("black_carp"));

// 鮰鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Rectum>> RECTUM =
            REGISTRY.register("rectum", () -> EntityType.Builder.of(Rectum::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("rectum"));

// 鲈鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Bass>> BASS =
            REGISTRY.register("bass", () -> EntityType.Builder.of(Bass::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("bass"));

// 鲤鱼
    public static final DeferredHolder<EntityType<?>, EntityType<ChineseCarp>> CHINESE_CARP =
            REGISTRY.register("chinese_carp", () -> EntityType.Builder.of(ChineseCarp::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("chinese_carp"));

// 鲫鱼
    public static final DeferredHolder<EntityType<?>, EntityType<CrucianCarp>> CRUCIAN_CARP =
            REGISTRY.register("crucian_carp", () -> EntityType.Builder.of(CrucianCarp::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("crucian_carp"));

// 鲶鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Catfish>> CATFISH =
            REGISTRY.register("catfish", () -> EntityType.Builder.of(Catfish::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("catfish"));

// 鳙鱼
    public static final DeferredHolder<EntityType<?>, EntityType<BigheadCarp>> BIGHEAD_CARP =
            REGISTRY.register("bighead_carp", () -> EntityType.Builder.of(BigheadCarp::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("bighead_carp"));

// 黑鱼
    public static final DeferredHolder<EntityType<?>, EntityType<BlackFish>> BLACK_FISH =
            REGISTRY.register("black_fish", () -> EntityType.Builder.of(BlackFish::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("black_fish"));

// 帝王蟹
    public static final DeferredHolder<EntityType<?>, EntityType<KingCrab>> KING_CRAB =
            REGISTRY.register("king_crab", () -> EntityType.Builder.of(KingCrab::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("king_crab"));

// 剑鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Swordfish>> SWORDFISH =
            REGISTRY.register("swordfish", () -> EntityType.Builder.of(Swordfish::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("swordfish"));

// 带鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Hairtail>> HAIRTAIL =
            REGISTRY.register("hairtail", () -> EntityType.Builder.of(Hairtail::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("hairtail"));

// 金枪鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Tuna>> TUNA =
            REGISTRY.register("tuna", () -> EntityType.Builder.of(Tuna::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("tuna"));

// 面包蟹
    public static final DeferredHolder<EntityType<?>, EntityType<BrownCrab>> BROWN_CRAB =
            REGISTRY.register("brown_crab", () -> EntityType.Builder.of(BrownCrab::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("brown_crab"));

// 鮟鱇鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Anglerfish>> ANGLERFISH =
            REGISTRY.register("anglerfish", () -> EntityType.Builder.of(Anglerfish::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("anglerfish"));

// 鲅鱼
    public static final DeferredHolder<EntityType<?>, EntityType<ChineseMackerel>> CHINESE_MACKEREL =
            REGISTRY.register("chinese_mackerel", () -> EntityType.Builder.of(ChineseMackerel::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("chinese_mackerel"));

// 鳗鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Eel>> EEL =
            REGISTRY.register("eel", () -> EntityType.Builder.of(Eel::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("eel"));

// 龙虾
    public static final DeferredHolder<EntityType<?>, EntityType<Lobster>> LOBSTER =
            REGISTRY.register("lobster", () -> EntityType.Builder.of(Lobster::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("lobster"));

// 中华鳖
    public static final DeferredHolder<EntityType<?>, EntityType<Turtle>> TURTLE =
            REGISTRY.register("turtle", () -> EntityType.Builder.of(Turtle::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("turtle"));

// 大闸蟹
    public static final DeferredHolder<EntityType<?>, EntityType<HairyCrab>> HAIRY_CRAB =
            REGISTRY.register("hairy_crab", () -> EntityType.Builder.of(HairyCrab::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("hairy_crab"));

// 对虾
    public static final DeferredHolder<EntityType<?>, EntityType<Prawn>> PRAWN =
            REGISTRY.register("prawn", () -> EntityType.Builder.of(Prawn::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("prawn"));

// 梭子蟹
    public static final DeferredHolder<EntityType<?>, EntityType<SwimmingCrab>> SWIMMING_CRAB =
            REGISTRY.register("swimming_crab", () -> EntityType.Builder.of(SwimmingCrab::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("swimming_crab"));

// 青蟹
    public static final DeferredHolder<EntityType<?>, EntityType<MudCrab>> MUD_CRAB =
            REGISTRY.register("mud_crab", () -> EntityType.Builder.of(MudCrab::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("mud_crab"));

// 沙丁鱼
    public static final DeferredHolder<EntityType<?>, EntityType<Sardine>> SARDINE =
            REGISTRY.register("sardine", () -> EntityType.Builder.of(Sardine::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("sardine"));

// 老虎斑
    public static final DeferredHolder<EntityType<?>, EntityType<Grouper>> GROUPER =
            REGISTRY.register("grouper", () -> EntityType.Builder.of(Grouper::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("grouper"));

// 秋刀鱼
    public static final DeferredHolder<EntityType<?>, EntityType<PacificSaury>> PACIFIC_SAURY =
            REGISTRY.register("pacific_saury", () -> EntityType.Builder.of(PacificSaury::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("pacific_saury"));

// 银鲳
    public static final DeferredHolder<EntityType<?>, EntityType<SilveryPomfret>> SILVERY_POMFRET =
            REGISTRY.register("silvery_pomfret", () -> EntityType.Builder.of(SilveryPomfret::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("silvery_pomfret"));

// 马步鱼
    public static final DeferredHolder<EntityType<?>, EntityType<JapaneseHalfbeak>> JAPANESE_HALFBEAK =
            REGISTRY.register("japanese_halfbeak", () -> EntityType.Builder.of(JapaneseHalfbeak::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5F, 0.3F)
                    .clientTrackingRange(4)
                    .build("japanese_halfbeak"));

// 粪便
// 小体积静物：动物拉下后就躺在原地，玩家右键可以收回成物品（见 entity/Poop）
    public static final DeferredHolder<EntityType<?>, EntityType<Poop>> POOP =
            REGISTRY.register("poop", () -> EntityType.Builder.of(Poop::new, MobCategory.MISC)
                    .sized(0.4F, 0.3F)
                    .clientTrackingRange(10)
                    .build("poop"));

// 稻草人
// 插在田里的静物：不移动也不碰撞，右键可收回。高度取模型实际高度（木杆到草垛顶约 2.31 格）
    public static final DeferredHolder<EntityType<?>, EntityType<Scarecrow>> SCARECROW =
            REGISTRY.register("scarecrow", () -> EntityType.Builder.of(Scarecrow::new, MobCategory.MISC)
                    .sized(0.7F, 2.3F)
                    .clientTrackingRange(10)
                    .build("scarecrow"));

// 毒饵站
// 用毒饵站物品右键稻草人放下（见 item/PoisonBaitStationItem），罩住周围 16 格不让老鼠生成
    public static final DeferredHolder<EntityType<?>, EntityType<PoisonBaitStation>> POISON_BAIT_STATION =
            REGISTRY.register("poison_bait_station", () -> EntityType.Builder.of(PoisonBaitStation::new, MobCategory.MISC)
                    .sized(0.5F, 0.25F)
                    .clientTrackingRange(10)
                    .build("poison_bait_station"));

// 拉力木车
// 像木船的木车：有碰撞体积、右键挂到玩家身后跟着走、潜行松开。
// 这里的尺寸只当名义大小（车宽 2.8 含两侧车轮、高 1.5）；实际碰撞箱由 Handcart#makeBoundingBox
// 按朝向贴着车斗摆，免得箱体甩到车前方的空气里
    public static final DeferredHolder<EntityType<?>, EntityType<Handcart>> HANDCART =
            REGISTRY.register("handcart", () -> EntityType.Builder.of(Handcart::new, MobCategory.MISC)
                    .sized(2.8F, 1.5F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("handcart"));

// 多功能无人机
// 遥控的四旋翼：通电后悬停 / 飞行（旋翼转起来），断电则缓慢落地。
// 旋翼张开后横跨约 1.8 格，机身不高，碰撞箱按这个量
    public static final DeferredHolder<EntityType<?>, EntityType<Uav>> UAV =
            REGISTRY.register("uav", () -> EntityType.Builder.of(Uav::new, MobCategory.MISC)
                    .sized(1.8F, 0.7F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("uav"));

// 纸箱
// 无人机运输模式投放的箱子：装着打包好的货物从空中缓缓落地，右键收货 / 左键砸开，一次性不可回收
    public static final DeferredHolder<EntityType<?>, EntityType<Carton>> CARTON =
            REGISTRY.register("carton", () -> EntityType.Builder.of(Carton::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("carton"));

// 被扔出去的粪便
// 纯投掷物：只以物品贴图的样子飞出去，砸到方块或生物当场碎裂消失，不会落地变成一坨
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownPoop>> THROWN_POOP =
            REGISTRY.register("thrown_poop", () -> EntityType.Builder.<ThrownPoop>of(ThrownPoop::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("thrown_poop"));

// 被扔出去的加压沼气
// 纯投掷物：落地爆开，只伤实体不炸方块
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownMarshGas>> THROWN_MARSH_GAS =
            REGISTRY.register("thrown_marsh_gas", () -> EntityType.Builder.<ThrownMarshGas>of(ThrownMarshGas::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("thrown_marsh_gas"));

// 被扔出去的禽蛋
// 纯投掷物：原版鸡蛋的机制，孵出的是蛋所对应的禽类（见 herding/CoopEggs），五个禽蛋共用
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownPoultryEgg>> THROWN_POULTRY_EGG =
            REGISTRY.register("thrown_poultry_egg", () -> EntityType.Builder.<ThrownPoultryEgg>of(ThrownPoultryEgg::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("thrown_poultry_egg"));

// 收割机
    public static final DeferredHolder<EntityType<?>, EntityType<Harvester>> HARVESTER =
            REGISTRY.register("harvester", () -> EntityType.Builder.of(Harvester::new, MobCategory.MISC)
                    .sized(3.0F, 3.0F)
                    .clientTrackingRange(10)
                    .build("harvester"));

// 充气钓鱼浮台
// 水面上撑开的一块 5×5 平台：像木船一样有碰撞体积、任何生物都能踩上去，
// 但坐标钉死（不落、不漂、推不动），被玩家连击几下后散掉掉回物品
    public static final DeferredHolder<EntityType<?>, EntityType<FishingFloatingPlatform>> FISHING_FLOATING_PLATFORM =
            REGISTRY.register("fishing_floating_platform", () -> EntityType.Builder.of(FishingFloatingPlatform::new, MobCategory.MISC)
                    .sized(5.0F, 1.0F)
                    .clientTrackingRange(10)
                    .build("fishing_floating_platform"));

// 坠落的果子
// 悬挂的果子掉下来时生成的实体：带着悬挂果实的方块模型落地，落地后停在原处、可右键拾取
    public static final DeferredHolder<EntityType<?>, EntityType<FallingFruit>> FALLING_FRUIT =
            REGISTRY.register("falling_fruit", () -> EntityType.Builder.of(FallingFruit::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("falling_fruit"));

// 外卖袋
// 出发地路边放着的袋子：带着整条外卖订单，右键捡起变成「打包好的外卖」
    public static final DeferredHolder<EntityType<?>, EntityType<TakeawayBag>> TAKEAWAY_BAG =
            REGISTRY.register("takeaway_bag", () -> EntityType.Builder.of(TakeawayBag::new, MobCategory.MISC)
                    .sized(0.75F, 0.9F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("takeaway_bag"));

// 配送点
// 目的地地表上不动的收件人（渲染成村民）：手持「打包好的外卖」右键即可交付
    public static final DeferredHolder<EntityType<?>, EntityType<DeliveryPoint>> DELIVERY_POINT =
            REGISTRY.register("delivery_point", () -> EntityType.Builder.of(DeliveryPoint::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("delivery_point"));

// 外卖小哥
// 小票打印机出单时在机器附近刷出的被动生物：守在原地偶尔走两步，交对食物即结单消失，五分钟后自行离开
    public static final DeferredHolder<EntityType<?>, EntityType<DeliveryRider>> DELIVERY_RIDER =
            REGISTRY.register("delivery_rider", () -> EntityType.Builder.of(DeliveryRider::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("delivery_rider"));

// 顾客
// 餐饮营业执照开业后招来的堂食客人，用原版玩家模型 + 默认皮肤渲染
    public static final DeferredHolder<EntityType<?>, EntityType<Customer>> CUSTOMER =
            REGISTRY.register("customer", () -> EntityType.Builder.of(Customer::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F).clientTrackingRange(10).build("customer"));

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
