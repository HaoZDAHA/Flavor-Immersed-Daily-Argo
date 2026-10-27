package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.creative.SectionedCreativeModeTab;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

// 创造模式物品栏注册：整个模组只有一个标签页「东篱南山居」。
public final class ModCreativeTabs {

    private ModCreativeTabs() {
    }

    public static final DeferredRegister<CreativeModeTab> REGISTRY =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, flavor_immersed_daily_argo.MODID);

// 标签页里的分区横幅，第一条「初级农牧产品」排在最前。
//
// 顺序与下面的 displayItems 一一对应：初级农牧产品 212 件
// （1 种作物 + 1 种作物种子 + 3 种果实 + 3 种树苗 + 3 种果树叶
// + 5 种禽类尸体 + 5 种其他动物尸体 + 16 种生鸭子 + 6 种生鹅 + 6 种生禽类
// + 12 种生肉与内脏 + 5 种禽蛋 + 2 种奶类 + 7 种小型鱼屠宰产物
// + 54 种水产 + 27 种桶装水产 + 6 种昆虫系列（蜜蜂 / 知了猴 / 蚂蚱 / 蚕 + 蚕茧 / 生蚕蛹）+ 43 个刷怪蛋），
// 农业耕种 26 件，畜禽饲养 20 件，水产渔业 44 件
// （钓鱼手册 + 南山钓竿本体 + 33 种钓具配件 + 抄网 + 充气钓鱼浮台
// + 鱼护 / 钓鱼箱 / 竿架 / 杆秤 / 钓鱼展示架 / 鱼缸 / 晒盐场），
// 林木生态 22 件（护林员写字板 / 树漆 / 灭火器 / 气象仪器 / 除草剂 / 测距绳 / 水位尺 / 水质检测器
// + 昆虫培育箱 / 纸板蜂巢 / 轻薄木板 / 捕虫网 / 四种昆虫卵 / 高蛋白聚合物
// + 活性炭颗粒 / 蝉蜕酸 / 生物碱 / 水体供氧机 / 水体菌种培养皿），
// 食物佳肴 177 件（14 种烤鸭 + 4 种烤鹅 + 6 种动物内脏 + 6 种原版动物肉 + 26 种水产 + 17 种烤鱼 + 野生狗奶）。
// 经济与生活 22 件（南山-教程卷轴 / 南山开发者指导手册 / 自助取款机 / 银行卡 / 导航手机 / 六种纸币 / 丝绸 / 缝纫机 / 空白丝绸四件套）。刷怪蛋一律排在其所属横幅的末位。
//
// id 同时用作折叠状态的键与横幅纹理名（textures/gui/creative_banner/<id>.png）。
    private static final List<SectionedCreativeModeTab.Section> SECTIONS = List.of(
            new SectionedCreativeModeTab.Section("primary_products",
                    Component.translatable("gui.flavor_immersed_daily_argo.creative_section.primary_products"),
                    bannerTexture("primary_products"), 212),
            new SectionedCreativeModeTab.Section("farming",
                    Component.translatable("gui.flavor_immersed_daily_argo.creative_section.farming"),
                    bannerTexture("farming"), 26),
            new SectionedCreativeModeTab.Section("herding",
                    Component.translatable("gui.flavor_immersed_daily_argo.creative_section.herding"),
                    bannerTexture("herding"), 20),
            new SectionedCreativeModeTab.Section("fishing",
                    Component.translatable("gui.flavor_immersed_daily_argo.creative_section.fishing"),
                    bannerTexture("fishing"), 44),
            new SectionedCreativeModeTab.Section("forestry",
                    Component.translatable("gui.flavor_immersed_daily_argo.creative_section.forestry"),
                    bannerTexture("forestry"), 22),
            new SectionedCreativeModeTab.Section("food_delicacies",
                    Component.translatable("gui.flavor_immersed_daily_argo.creative_section.food_delicacies"),
                    bannerTexture("food_delicacies"), 177),
            new SectionedCreativeModeTab.Section("economy_life",
                    Component.translatable("gui.flavor_immersed_daily_argo.creative_section.economy_life"),
                    bannerTexture("economy_life"), 22));

// 横幅纹理：textures/gui/creative_banner/<名字>.png，固定 162×18
    private static ResourceLocation bannerTexture(String name) {
        return new ResourceLocation(flavor_immersed_daily_argo.MODID,
                "textures/gui/creative_banner/" + name + ".png");
    }

// 本标签页的整幅背景图：点开这一页时，原版创造模式面板整块换成这张图。
//
// 画布必须 256×256，可见内容画在左上角 195×136 里：GuiGraphics#blit(location, x, y, u, v, w, h)
// 内部固定按 256×256 换算 UV，画布小于 256 只会把左上角一小块拉伸铺满整块面板
// （原版 tab_items.png 同样是一张 256×256）。backgroundTexture 要带 textures/ 前缀的完整路径。
//
// 图里请自带右侧滚动条凹槽（滑块由原版另画在上面），并把标题直接画进图里——
// 本标签页调用了 CreativeModeTab.Builder#hideTitle()，原版的标题文字不再绘制。
    public static final ResourceLocation TAB_BACKGROUND =
            new ResourceLocation(flavor_immersed_daily_argo.MODID,
                    "textures/gui/container/creative_inventory/sundries.png");

// 面板可见区域尺寸，与 vanilla 的 imageWidth / imageHeight 一致
    public static final int TAB_WIDTH = 195;
    public static final int TAB_HEIGHT = 136;

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SUNDRIES_TAB = REGISTRY.register("sundries",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.flavor_immersed_daily_argo.sundries"))
                    .icon(() -> ModItems.HARVESTER_SPAWN_EGG.get().getDefaultInstance())
// 点开本标签页时整幅 GUI 换成这张图（原版会按 195×136 铺在 leftPos/topPos 处）
                    .withBackgroundLocation(TAB_BACKGROUND)
// 左上角那行标签页名字不画，免得压在自定义背景上（名字画进背景图里）
                    .hideTitle()
                    .withTabFactory(builder -> new SectionedCreativeModeTab(builder, SECTIONS))
                    .displayItems((parameters, output) -> {
// 初级农牧产品：农作物（荠菜与种子，柚子 / 板栗 / 番石榴三种果实放一起，树苗放一起）
                        output.accept(ModItems.SHEPHERDS_PURSE.get());
                        output.accept(ModItems.SHEPHERDS_PURSE_SEEDS.get());
                        output.accept(ModItems.POMELO.get());
                        output.accept(ModItems.CHESTNUT.get());
                        output.accept(ModItems.GUAVA.get());
                        output.accept(ModItems.POMELO_SAPLING.get());
                        output.accept(ModItems.CHESTNUT_SAPLING.get());
                        output.accept(ModItems.GUAVA_SAPLING.get());
// 初级农牧产品：果树叶（三种放一起）
                        output.accept(ModItems.POMELO_FRUITING_LEAVES.get());
                        output.accept(ModItems.CHESTNUT_FRUITING_LEAVES.get());
                        output.accept(ModItems.GUAVA_FRUITING_LEAVES.get());
// 初级农牧产品：五种禽类的尸体
                        output.accept(ModItems.DEAD_DUCK.get());
                        output.accept(ModItems.DEAD_GOOSE.get());
                        output.accept(ModItems.DEAD_QUAIL.get());
                        output.accept(ModItems.DEAD_PIGEON.get());
                        output.accept(ModItems.DEAD_TURKEY.get());
// 初级农牧产品：其他动物的尸体（狼 / 驴 / 骆驼 / 兔子 / 羊驼，同样用斩骨刀猎获）
                        output.accept(ModItems.DEAD_DOG.get());
                        output.accept(ModItems.DEAD_DONKEY.get());
                        output.accept(ModItems.DEAD_CAMEL.get());
                        output.accept(ModItems.DEAD_RABBIT.get());
                        output.accept(ModItems.DEAD_LLAMA.get());
// 初级农牧产品：生鸭子（放血后的鸭子、白条鸭排最前）
                        output.accept(ModItems.BLED_DUCK.get());
                        output.accept(ModItems.RAW_DRESSED_DUCK.get());
                        output.accept(ModItems.RAW_DUCK_THIGH.get());
                        output.accept(ModItems.RAW_DUCK_RUMP.get());
                        output.accept(ModItems.RAW_DUCK_HEART.get());
                        output.accept(ModItems.RAW_DUCK_WEB.get());
                        output.accept(ModItems.RAW_DUCK_WING.get());
                        output.accept(ModItems.RAW_DUCK_MEAT.get());
                        output.accept(ModItems.RAW_DUCK_LIVER.get());
                        output.accept(ModItems.RAW_DUCK_INTESTINE.get());
                        output.accept(ModItems.RAW_DUCK_GIZZARD.get());
                        output.accept(ModItems.RAW_DUCK_NECK.get());
                        output.accept(ModItems.RAW_DUCK_LEG.get());
                        output.accept(ModItems.RAW_DUCK_TONGUE.get());
                        output.accept(ModItems.RAW_DUCK_BLOOD.get());
                        output.accept(ModItems.RAW_DUCK_HEAD.get());
// 初级农牧产品：生鹅（放血后的鹅、白条鹅排最前）
                        output.accept(ModItems.BLED_GOOSE.get());
                        output.accept(ModItems.RAW_DRESSED_GOOSE.get());
                        output.accept(ModItems.RAW_GOOSE_MEAT.get());
                        output.accept(ModItems.RAW_GOOSE_LIVER.get());
                        output.accept(ModItems.RAW_GOOSE_INTESTINE.get());
                        output.accept(ModItems.RAW_GOOSE_LEG.get());
// 初级农牧产品：生禽类（鹌鹑 / 鸽子 / 火鸡，放血后 + 白条）
                        output.accept(ModItems.BLED_QUAIL.get());
                        output.accept(ModItems.RAW_DRESSED_QUAIL.get());
                        output.accept(ModItems.BLED_PIGEON.get());
                        output.accept(ModItems.RAW_DRESSED_PIGEON.get());
                        output.accept(ModItems.BLED_TURKEY.get());
                        output.accept(ModItems.RAW_DRESSED_TURKEY.get());
// 初级农牧产品：屠宰动物获得的生肉与内脏
                        output.accept(ModItems.RAW_RABBIT_HEAD.get());
                        output.accept(ModItems.RAW_DOG_MEAT.get());
                        output.accept(ModItems.RAW_LLAMA_MEAT.get());
                        output.accept(ModItems.RAW_DONKEY_MEAT.get());
                        output.accept(ModItems.RAW_CAMEL_HUMP.get());
                        output.accept(ModItems.RAW_CAMEL_MEAT.get());
                        output.accept(ModItems.RAW_ANIMAL_HEART.get());
                        output.accept(ModItems.RAW_ANIMAL_LIVER.get());
                        output.accept(ModItems.RAW_ANIMAL_INTESTINE.get());
                        output.accept(ModItems.RAW_ANIMAL_LUNG.get());
                        output.accept(ModItems.RAW_ANIMAL_STOMACH.get());
                        output.accept(ModItems.RAW_ANIMAL_BLOOD.get());
// 初级农牧产品：五种禽类的蛋
                        output.accept(ModItems.DUCK_EGG.get());
                        output.accept(ModItems.GOOSE_EGG.get());
                        output.accept(ModItems.QUAIL_EGG.get());
                        output.accept(ModItems.PIGEON_EGG.get());
                        output.accept(ModItems.TURKEY_EGG.get());
// 初级农牧产品：奶类（取奶器取对应动物）
                        output.accept(ModItems.FRESH_GOAT_MILK.get());
                        output.accept(ModItems.CAMEL_MILK.get());
// 初级农牧产品：小型鱼屠宰产物
                        output.accept(ModItems.RAW_FISH_HEAD.get());
                        output.accept(ModItems.RAW_FISH_SWIM_BLADDER.get());
                        output.accept(ModItems.RAW_FISH_SKIN.get());
                        output.accept(ModItems.RAW_FISH_ROE.get());
                        output.accept(ModItems.RAW_FISH_LIVER.get());
                        output.accept(ModItems.FISH_BONE.get());
                        output.accept(ModItems.FISH_SCALE.get());
// 初级农牧产品：全部水产
                        output.accept(ModItems.RAW_GRASS_CARP.get());
                        output.accept(ModItems.RAW_TENCH.get());
                        output.accept(ModItems.RAW_MANDARIN_FISH.get());
                        output.accept(ModItems.RAW_BLACK_FISH.get());
                        output.accept(ModItems.RAW_RECTUM.get());
                        output.accept(ModItems.RAW_CRUCIAN_CARP.get());
                        output.accept(ModItems.RAW_CHINESE_CARP.get());
                        output.accept(ModItems.RAW_SILVER_CARP.get());
                        output.accept(ModItems.RAW_BASS.get());
                        output.accept(ModItems.RAW_TILAPIA.get());
                        output.accept(ModItems.RAW_LOACH.get());
                        output.accept(ModItems.RAW_CATFISH.get());
                        output.accept(ModItems.RAW_GOBY.get());
                        output.accept(ModItems.RAW_BLACK_CARP.get());
                        output.accept(ModItems.RAW_REDFIN.get());
                        output.accept(ModItems.RAW_PLECO.get());
                        output.accept(ModItems.RAW_BIGHEAD_CARP.get());
                        output.accept(ModItems.RAW_HAIRY_CRAB.get());
                        output.accept(ModItems.RAW_PRAWN.get());
                        output.accept(ModItems.RAW_CLAM.get());
                        output.accept(ModItems.RAW_TURTLE.get());
                        output.accept(ModItems.RAW_MUD_CRAB.get());
                        output.accept(ModItems.RAW_SWIMMING_CRAB.get());
                        output.accept(ModItems.RAW_RIVER_SNAIL.get());
                        output.accept(ModItems.RAW_CRAYFISH.get());
                        output.accept(ModItems.RAW_CHINESE_MACKEREL.get());
                        output.accept(ModItems.RAW_SILVERY_POMFRET.get());
                        output.accept(ModItems.RAW_HAIRTAIL.get());
                        output.accept(ModItems.RAW_EEL.get());
                        output.accept(ModItems.RAW_TUNA.get());
                        output.accept(ModItems.RAW_ANGLERFISH.get());
                        output.accept(ModItems.RAW_KING_CRAB.get());
                        output.accept(ModItems.RAW_LOBSTER.get());
                        output.accept(ModItems.RAW_BROWN_CRAB.get());
                        output.accept(ModItems.RAW_SWORDFISH.get());
                        output.accept(ModItems.RAW_PACIFIC_SAURY.get());
                        output.accept(ModItems.RAW_SARDINE.get());
                        output.accept(ModItems.RAW_GROUPER.get());
                        output.accept(ModItems.RAW_JAPANESE_HALFBEAK.get());
                        output.accept(ModItems.RAW_RAZOR_CLAM.get());
                        output.accept(ModItems.RAW_SEA_CUCUMBER.get());
                        output.accept(ModItems.RAW_SPOON_WORM.get());
                        output.accept(ModItems.RAW_SEA_URCHIN.get());
                        output.accept(ModItems.RAW_SEA_SNAIL.get());
                        output.accept(ModItems.RAW_JELLYFISH.get());
                        output.accept(ModItems.RAW_AKIAMI_SHRIMP.get());
                        output.accept(ModItems.RAW_MANTIS_SHRIMP.get());
                        output.accept(ModItems.RAW_SCALLOP.get());
                        output.accept(ModItems.RAW_OYSTER.get());
                        output.accept(ModItems.RAW_MUD_SHRIMP.get());
                        output.accept(ModItems.RAW_ABALONE.get());
// 初级农牧产品：鱿鱼（整只鱿鱼放上砧板剁成肉与须）
                        output.accept(ModItems.RAW_SQUID.get());
                        output.accept(ModItems.RAW_SQUID_MEAT.get());
                        output.accept(ModItems.RAW_SQUID_TENTACLE.get());
// 初级农牧产品：桶装水产（紧接在水产品后面）
                        output.accept(ModItems.GRASS_CARP_BUCKET.get());
                        output.accept(ModItems.TENCH_BUCKET.get());
                        output.accept(ModItems.MANDARIN_FISH_BUCKET.get());
                        output.accept(ModItems.BLACK_FISH_BUCKET.get());
                        output.accept(ModItems.RECTUM_BUCKET.get());
                        output.accept(ModItems.CRUCIAN_CARP_BUCKET.get());
                        output.accept(ModItems.CHINESE_CARP_BUCKET.get());
                        output.accept(ModItems.SILVER_CARP_BUCKET.get());
                        output.accept(ModItems.BASS_BUCKET.get());
                        output.accept(ModItems.TILAPIA_BUCKET.get());
                        output.accept(ModItems.LOACH_BUCKET.get());
                        output.accept(ModItems.CATFISH_BUCKET.get());
                        output.accept(ModItems.GOBY_BUCKET.get());
                        output.accept(ModItems.REDFIN_BUCKET.get());
                        output.accept(ModItems.BLACK_CARP_BUCKET.get());
                        output.accept(ModItems.PLECO_BUCKET.get());
                        output.accept(ModItems.BIGHEAD_CARP_BUCKET.get());
                        output.accept(ModItems.HAIRY_CRAB_BUCKET.get());
                        output.accept(ModItems.PRAWN_BUCKET.get());
                        output.accept(ModItems.TURTLE_BUCKET.get());
                        output.accept(ModItems.MUD_CRAB_BUCKET.get());
                        output.accept(ModItems.SWIMMING_CRAB_BUCKET.get());
                        output.accept(ModItems.SILVERY_POMFRET_BUCKET.get());
                        output.accept(ModItems.PACIFIC_SAURY_BUCKET.get());
                        output.accept(ModItems.SARDINE_BUCKET.get());
                        output.accept(ModItems.GROUPER_BUCKET.get());
                        output.accept(ModItems.JAPANESE_HALFBEAK_BUCKET.get());
// 初级农牧产品：大型鱼的生肉与部位（食用数值同生肉）
                        output.accept(ModItems.RAW_ANGLERFISH_LIVER.get());
                        output.accept(ModItems.RAW_MACKEREL_MEAT.get());
                        output.accept(ModItems.RAW_HAIRTAIL_SEGMENT.get());
                        output.accept(ModItems.RAW_SWORDFISH_MEAT.get());
                        output.accept(ModItems.RAW_TUNA_MEAT.get());
                        output.accept(ModItems.RAW_EEL_SEGMENT.get());
// 初级农牧产品：昆虫系列（蜜蜂 / 知了猴 / 蚂蚱 / 蚕），后跟纺织系列的蚕茧 / 生蚕蛹
                        output.accept(ModItems.BEE.get());
                        output.accept(ModItems.CICADA.get());
                        output.accept(ModItems.CICADA_SLOUGH.get());
                        output.accept(ModItems.LOCUST.get());
                        output.accept(ModItems.SILKWORM.get());
                        output.accept(ModItems.SILKWORM_COCOON.get());
                        output.accept(ModItems.RAW_SILKWORM_CHRYSALIS.get());
// 初级农牧产品：本模组的刷怪蛋，统一排在本横幅末位
                        output.accept(ModItems.DUCK_SPAWN_EGG.get());
                        output.accept(ModItems.GOOSE_SPAWN_EGG.get());
                        output.accept(ModItems.QUAIL_SPAWN_EGG.get());
                        output.accept(ModItems.PIGEON_SPAWN_EGG.get());
                        output.accept(ModItems.TURKEY_SPAWN_EGG.get());
                        output.accept(ModItems.BULLFROG_SPAWN_EGG.get());
                        output.accept(ModItems.BAMBOORAT_SPAWN_EGG.get());
                        output.accept(ModItems.GRASS_CARP_SPAWN_EGG.get());
                        output.accept(ModItems.TENCH_SPAWN_EGG.get());
                        output.accept(ModItems.MANDARIN_FISH_SPAWN_EGG.get());
                        output.accept(ModItems.LOACH_SPAWN_EGG.get());
                        output.accept(ModItems.PLECO_SPAWN_EGG.get());
                        output.accept(ModItems.GOBY_SPAWN_EGG.get());
                        output.accept(ModItems.SILVER_CARP_SPAWN_EGG.get());
                        output.accept(ModItems.TILAPIA_SPAWN_EGG.get());
                        output.accept(ModItems.REDFIN_SPAWN_EGG.get());
                        output.accept(ModItems.BLACK_CARP_SPAWN_EGG.get());
                        output.accept(ModItems.RECTUM_SPAWN_EGG.get());
                        output.accept(ModItems.BASS_SPAWN_EGG.get());
                        output.accept(ModItems.CHINESE_CARP_SPAWN_EGG.get());
                        output.accept(ModItems.CRUCIAN_CARP_SPAWN_EGG.get());
                        output.accept(ModItems.CATFISH_SPAWN_EGG.get());
                        output.accept(ModItems.BIGHEAD_CARP_SPAWN_EGG.get());
                        output.accept(ModItems.BLACK_FISH_SPAWN_EGG.get());
                        output.accept(ModItems.KING_CRAB_SPAWN_EGG.get());
                        output.accept(ModItems.SWORDFISH_SPAWN_EGG.get());
                        output.accept(ModItems.HAIRTAIL_SPAWN_EGG.get());
                        output.accept(ModItems.TUNA_SPAWN_EGG.get());
                        output.accept(ModItems.BROWN_CRAB_SPAWN_EGG.get());
                        output.accept(ModItems.ANGLERFISH_SPAWN_EGG.get());
                        output.accept(ModItems.CHINESE_MACKEREL_SPAWN_EGG.get());
                        output.accept(ModItems.EEL_SPAWN_EGG.get());
                        output.accept(ModItems.LOBSTER_SPAWN_EGG.get());
                        output.accept(ModItems.TURTLE_SPAWN_EGG.get());
                        output.accept(ModItems.HAIRY_CRAB_SPAWN_EGG.get());
                        output.accept(ModItems.PRAWN_SPAWN_EGG.get());
                        output.accept(ModItems.SWIMMING_CRAB_SPAWN_EGG.get());
                        output.accept(ModItems.MUD_CRAB_SPAWN_EGG.get());
                        output.accept(ModItems.SARDINE_SPAWN_EGG.get());
                        output.accept(ModItems.GROUPER_SPAWN_EGG.get());
                        output.accept(ModItems.PACIFIC_SAURY_SPAWN_EGG.get());
                        output.accept(ModItems.SILVERY_POMFRET_SPAWN_EGG.get());
                        output.accept(ModItems.JAPANESE_HALFBEAK_SPAWN_EGG.get());
// 农业耕种
                        output.accept(ModItems.JINKELA.get());
                        output.accept(ModItems.ORGANIC_FERTILIZER.get());
                        output.accept(ModItems.SICKLE.get());
                        output.accept(ModItems.WATERING_CAN.get());
                        output.accept(ModItems.HARROW.get());
                        output.accept(ModItems.STRAW_HAT.get());
                        output.accept(ModItems.FERTILE_FARMLAND.get());
                        output.accept(ModItems.WATERWHEEL.get());
                        output.accept(ModItems.WATER_PIPE.get());
                        output.accept(ModItems.WATER_PUMP.get());
                        output.accept(ModItems.DOSING_TUBE.get());
                        output.accept(ModItems.ROTATING_WATER_PIPE.get());
                        output.accept(ModItems.SECTOR_WATER_PIPE.get());
                        output.accept(ModItems.SCARECROW.get());
                        output.accept(ModItems.POISON_BAIT_STATION.get());
                        output.accept(ModItems.HANDCART.get());
                        output.accept(ModItems.UAV.get());
                        output.accept(ModItems.UAV_CONTROLLER.get());
                        output.accept(ModItems.UAV_MODULE_CARE.get());
                        output.accept(ModItems.UAV_MODULE_COMBAT.get());
                        output.accept(ModItems.UAV_MODULE_HARVEST.get());
                        output.accept(ModItems.UAV_MODULE_TRANSPORT.get());
                        output.accept(ModItems.UAV_MODULE_HIGHLIGHT.get());
                        output.accept(ModItems.CARTON.get());
// 刷怪蛋排在本横幅末位
                        output.accept(ModItems.HARVESTER_SPAWN_EGG.get());
                        output.accept(ModItems.MOUSE_SPAWN_EGG.get());
// 畜禽饲养
                        output.accept(ModItems.CHICKEN_COOP.get());
                        output.accept(ModItems.FEEDING_TROUGH.get());
                        output.accept(ModItems.RIVER_SILT.get());
                        output.accept(ModItems.FEED_GRASS.get());
                        output.accept(ModItems.FEED_SEED.get());
                        output.accept(ModItems.FEED_AQUATIC.get());
                        output.accept(ModItems.FEED_MEAT.get());
                        output.accept(ModItems.POULTRY_CATCHING_NET.get());
                        output.accept(ModItems.FISH_NET.get());
                        output.accept(ModItems.MILKER.get());
                        output.accept(ModItems.POOP.get());
                        output.accept(ModItems.SEPTIC_TANK.get());
                        output.accept(ModItems.METHANE_TANK.get());
                        output.accept(ModItems.MARSH_GAS.get());
                        output.accept(ModItems.PET_NEST.get());
                        output.accept(ModItems.SYRINGE.get());
                        output.accept(ModItems.LIVESTOCK_BRUSH.get());
                        output.accept(ModItems.STOCK_WHIP.get());
                        output.accept(ModItems.WHISTLE.get());
                        output.accept(ModItems.FLEA_TREATMENT.get());
// 水产渔业
                        output.accept(ModItems.FISHING_HANDBOOK.get());
                        output.accept(ModItems.MOD_FISHING_ROD.get());
// 南山钓竿配件：鱼线
                        output.accept(ModItems.LINE_COTTON.get());
                        output.accept(ModItems.LINE_NYLON.get());
                        output.accept(ModItems.LINE_FLUOROCARBON.get());
                        output.accept(ModItems.LINE_BRAID.get());
                        output.accept(ModItems.LINE_STEEL.get());
                        output.accept(ModItems.LINE_GLOW.get());
// 南山钓竿配件：浮漂
                        output.accept(ModItems.FLOAT_REED.get());
                        output.accept(ModItems.FLOAT_BALSA.get());
                        output.accept(ModItems.FLOAT_NANO.get());
                        output.accept(ModItems.FLOAT_PEACOCK.get());
                        output.accept(ModItems.FLOAT_GLOW.get());
                        output.accept(ModItems.FLOAT_ISO.get());
// 南山钓竿配件：铅坠
                        output.accept(ModItems.SINKER_SPLIT.get());
                        output.accept(ModItems.SINKER_BULLET.get());
                        output.accept(ModItems.SINKER_DROP.get());
                        output.accept(ModItems.SINKER_SHEET.get());
                        output.accept(ModItems.SINKER_BOTTOM.get());
// 南山钓竿配件：杆体
                        output.accept(ModItems.ROD_BAMBOO.get());
                        output.accept(ModItems.ROD_FIBERGLASS.get());
                        output.accept(ModItems.ROD_CARBON.get());
                        output.accept(ModItems.ROD_RESIN.get());
                        output.accept(ModItems.ROD_HARDWOOD.get());
                        output.accept(ModItems.ROD_COMPOSITE.get());
// 南山钓竿配件：线轮
                        output.accept(ModItems.REEL_HAND.get());
                        output.accept(ModItems.REEL_SPINNING.get());
                        output.accept(ModItems.REEL_DRUM.get());
                        output.accept(ModItems.REEL_BAITCAST.get());
                        output.accept(ModItems.REEL_DRAG.get());
                        output.accept(ModItems.REEL_JIGGING.get());
// 南山钓竿配件：鱼饵
                        output.accept(ModItems.BAIT_GRASS.get());
                        output.accept(ModItems.BAIT_MEAT.get());
                        output.accept(ModItems.BAIT_ROT.get());
                        output.accept(ModItems.BAIT_WORM.get());
                        output.accept(ModItems.FISHING_NET.get());
// 充气钓鱼浮台：水面上撑开的 5×5 平台
                        output.accept(ModItems.FISHING_FLOATING_PLATFORM.get());
// 鱼护：含水的网笼，抄网抄到的活鱼养在里面
                        output.accept(ModItems.FISH_PROTECTION.get());
// 钓鱼箱：36 格容器，没拿抄网时钓到的东西都收进来
                        output.accept(ModItems.FISHING_BOX.get());
// 竿架：架着南山钓竿替人守竿，咬钩后提醒最近的玩家
                        output.accept(ModItems.ROD_POD.get());
// 钓鱼展示架：把手持的水产挂上去，渲染那条鱼的活体实体模型
                        output.accept(ModItems.FISH_DISPLAY_BOARD.get());
// 杆秤：右键开界面称重，重量按数据包里的分布抽
                        output.accept(ModItems.STEELYARD.get());
// 鱼缸：没有底的玻璃缸，可含水，相邻的自动连成一片
                        output.accept(ModItems.FISH_BOWL.get());
// 晒盐场：放在水边吸一遍水，再靠日晒一级级结出盐
                        output.accept(ModItems.SALT_PAN.get());
// 林木生态
                        output.accept(ModItems.RANGER_CLIPBOARD.get());
                        output.accept(ModItems.TREE_PAINT.get());
                        output.accept(ModItems.FIRE_EXTINGUISHER.get());
                        output.accept(ModItems.WEATHER_INSTRUMENT.get());
                        output.accept(ModItems.HERBICIDE.get());
                        output.accept(ModItems.RANGING_ROPE.get());
                        output.accept(ModItems.WATER_LEVEL_GAUGE.get());
                        output.accept(ModItems.WATER_QUALITY_DETECTOR.get());
// 林木生态：昆虫系列
                        output.accept(ModItems.INSECT_INCUBATOR.get());
                        output.accept(ModItems.CARDBOARD_HONEYCOMB.get());
                        output.accept(ModItems.INSECT_BOARD.get());
                        output.accept(ModItems.INSECT_NET.get());
// 昆虫系列：四种昆虫卵（蜜蜂卵 / 蝉卵 / 蚂蚱卵 / 蚕卵）
                        output.accept(ModItems.BEE_ROE.get());
                        output.accept(ModItems.CICADA_ROE.get());
                        output.accept(ModItems.LOCUST_ROE.get());
                        output.accept(ModItems.SILKWORM_ROE.get());
                        output.accept(ModItems.HIGH_PROTEIN_SUBSTANCE.get());
// 林木生态：水质药剂（活性炭颗粒 / 蝉蜕酸 / 生物碱 / 水体供氧机 / 水体菌种培养皿）
                        output.accept(ModItems.ACTIVATED_CARBON_PARTICLES.get());
                        output.accept(ModItems.PROTOCATECHUIC_ACID.get());
                        output.accept(ModItems.OXYPHYLLINE.get());
                        output.accept(ModItems.WATER_OXYGEN_MACHINE.get());
                        output.accept(ModItems.MICROBE_POWDER.get());
// 食物佳肴：可摆上桌的菜统一排在本横幅最前（整禽 / 大菜）
                        output.accept(ModItems.PEKING_ROAST_DUCK.get());
                        output.accept(ModItems.BRAISED_DUCK_PLATTER.get());
                        output.accept(ModItems.ROAST_DUCK.get());
                        output.accept(ModItems.POT_STEW_GOOSE.get());
                        output.accept(ModItems.ROASTED_GOOSE.get());
                        output.accept(ModItems.PEIXIAN_DOG_MEAT.get());
                        output.accept(ModItems.FOIE_GRAS.get());
                        output.accept(ModItems.DONKEY_FEAST.get());
// 食物佳肴：可摆上桌的蟹与虾
                        output.accept(ModItems.STEAMED_HAIRY_CRAB.get());
                        output.accept(ModItems.SCALDED_PRAWNS.get());
                        output.accept(ModItems.BRAISED_PRAWNS.get());
// 食物佳肴：可摆上桌的水产菜
                        output.accept(ModItems.TURTLE_PIGEON_SOUP.get());
                        output.accept(ModItems.STIR_FRIED_CLAMS.get());
                        output.accept(ModItems.TYPHOON_SHELTER_PRAWN.get());
                        output.accept(ModItems.SPICY_MACKEREL_CRUMBLE.get());
                        output.accept(ModItems.CLAM_CHIVE_DOUGH_DROP_SOUP.get());
                        output.accept(ModItems.SPICY_CRAYFISH.get());
// 食物佳肴：可摆上桌、暂无 16x16 图标（蒸帝王蟹 / 蒸面包蟹 / 春秋剑鱼）
                        output.accept(ModItems.STEAMED_LOBSTER.get());
                        output.accept(ModItems.STEAMED_KING_CRAB.get());
                        output.accept(ModItems.STEAMED_BROWN_CRAB.get());
                        output.accept(ModItems.STINKY_SWORDFISH.get());
// 食物佳肴：果食（柚子 / 番石榴的果酱 / 冰淇淋 / 棒冰 / 果汁）
                        output.accept(ModItems.POMELO_JAM.get());
                        output.accept(ModItems.GUAVA_JAM.get());
                        output.accept(ModItems.POMELO_ICECREAM.get());
                        output.accept(ModItems.GUAVA_ICECREAM.get());
                        output.accept(ModItems.POMELO_POPSICLE.get());
                        output.accept(ModItems.GUAVA_POPSICLE.get());
                        output.accept(ModItems.POMELO_JUICE.get());
                        output.accept(ModItems.GUAVA_JUICE.get());
// 食物佳肴：板栗的加工品（粉 / 面团 / 烤栗子 / 糖炒栗子 / 糕）
                        output.accept(ModItems.CHESTNUT_FLOUR.get());
                        output.accept(ModItems.CHESTNUT_DOUGH.get());
                        output.accept(ModItems.ROASTED_CHESTNUT.get());
                        output.accept(ModItems.SUGAR_ROASTED_CHESTNUT.get());
                        output.accept(ModItems.CHESTNUT_CAKE.get());
// 食物佳肴：鸭菜（卤味 7 种 / 鸭蛋 2 种 / 鸭血粉丝汤）
                        output.accept(ModItems.BRAISED_DUCK_HEAD.get());
                        output.accept(ModItems.BRAISED_DUCK_WEB.get());
                        output.accept(ModItems.BRAISED_DUCK_INTESTINE.get());
                        output.accept(ModItems.BRAISED_DUCK_GIZZARD.get());
                        output.accept(ModItems.BRAISED_DUCK_NECK.get());
                        output.accept(ModItems.BRAISED_DUCK_LEG.get());
                        output.accept(ModItems.BRAISED_DUCK_TONGUE.get());
                        output.accept(ModItems.OOZING_SALTED_DUCK_EGG.get());
                        output.accept(ModItems.BOILED_DUCK_EGG.get());
                        output.accept(ModItems.DUCK_BLOOD_VERMICELLI_SOUP.get());
// 食物佳肴：禽畜熟食（汤 / 酱肉 / 烤禽 / 卤蛋 / 火烧 / 兔头 / 白煮蛋）
                        output.accept(ModItems.PIGEON_SOUP.get());
                        output.accept(ModItems.BRAISED_DONKEY_MEAT.get());
                        output.accept(ModItems.ROAST_QUAIL.get());
                        output.accept(ModItems.ROAST_PIGEON.get());
                        output.accept(ModItems.ROAST_TURKEY.get());
                        output.accept(ModItems.BRAISED_QUAIL_EGG.get());
                        output.accept(ModItems.BRAISED_CAMEL_MEAT.get());
                        output.accept(ModItems.DONKEY_MEAT_BURGER.get());
                        output.accept(ModItems.SPICY_RABBIT_HEAD.get());
                        output.accept(ModItems.BOILED_QUAIL_EGG.get());
                        output.accept(ModItems.BOILED_GOOSE_EGG.get());
                        output.accept(ModItems.BOILED_PIGEON_EGG.get());
                        output.accept(ModItems.BOILED_TURKEY_EGG.get());
// 食物佳肴：野生狗奶（取奶器取野生狼获得，紫色品质）
                        output.accept(ModItems.WILD_DOG_MILK.get());
// 食物佳肴：调味料（不可直接食用）
                        output.accept(ModItems.OYSTER_SAUCE.get());
                        output.accept(ModItems.GARLIC_MINCE.get());
                        output.accept(ModItems.SHRIMP_PASTE.get());
                        output.accept(ModItems.CRAB_ROE.get());
                        output.accept(ModItems.FISH_LIVER_OIL.get());
                        output.accept(ModItems.DEEP_SEA_FISH_LIVER_OIL.get());
// 食物佳肴：水产熟食（食用数值暂同熟肉）
                        output.accept(ModItems.PICKLED_SWORDFISH.get());
                        output.accept(ModItems.MACKEREL_PANCAKE_ROLL.get());
                        output.accept(ModItems.RAZOR_CLAM_SCRAMBLED_EGGS.get());
                        output.accept(ModItems.SCALLION_SEA_CUCUMBER.get());
                        output.accept(ModItems.SPOON_WORM_RICE.get());
                        output.accept(ModItems.SEA_URCHIN_LUFFA_SOUP.get());
                        output.accept(ModItems.SEA_URCHIN_STEAMED_EGG.get());
                        output.accept(ModItems.BRAISED_ABALONE.get());
                        output.accept(ModItems.BRAISED_HAIRTAIL.get());
                        output.accept(ModItems.BRAISED_FISH_ROE.get());
                        output.accept(ModItems.SALT_PEPPER_MANTIS_SHRIMP.get());
                        output.accept(ModItems.TUNA_SUSHI.get());
                        output.accept(ModItems.ROAST_MACKEREL.get());
                        output.accept(ModItems.ROAST_SILKWORM_CHRYSALIS.get());
                        output.accept(ModItems.ROAST_HAIRTAIL_SEGMENT.get());
                        output.accept(ModItems.ROAST_SWORDFISH.get());
                        output.accept(ModItems.ROAST_TUNA.get());
                        output.accept(ModItems.ROAST_LOCUST.get());
                        output.accept(ModItems.ROAST_EEL_SEGMENT.get());
                        output.accept(ModItems.ROAST_CICADA.get());
                        output.accept(ModItems.CHILI_FRIED_MACKEREL_CRUMBLE.get());
                        output.accept(ModItems.VINEGAR_JELLYFISH_HEAD.get());
                        output.accept(ModItems.COLD_FISH_SKIN.get());
                        output.accept(ModItems.SPICY_RIVER_SNAIL.get());
                        output.accept(ModItems.EEL_RICE.get());
                        output.accept(ModItems.STEAMED_GROUPER.get());
                        output.accept(ModItems.STEAMED_MUD_CRAB.get());
                        output.accept(ModItems.STEAMED_SWIMMING_CRAB.get());
                        output.accept(ModItems.COOKED_KING_CRAB_LEG.get());
                        output.accept(ModItems.COOKED_LOBSTER_MEAT.get());
                        output.accept(ModItems.COOKED_SQUID_MEAT.get());
                        output.accept(ModItems.COOKED_SQUID_TENTACLE.get());
                        output.accept(ModItems.COOKED_BROWN_CRAB_MEAT.get());
                        output.accept(ModItems.BOILED_PRAWN.get());
                        output.accept(ModItems.GARLIC_VERMICELLI_SCALLOP.get());
                        output.accept(ModItems.GARLIC_VERMICELLI_OYSTER.get());
                        output.accept(ModItems.OIL_SOAKED_SNAIL_SLICES.get());
                        output.accept(ModItems.FISH_MAW_TOFU_SOUP.get());
                        output.accept(ModItems.FRIED_SILKWORM_CHRYSALIS.get());
                        output.accept(ModItems.FRIED_HAIRTAIL_SEGMENT.get());
                        output.accept(ModItems.FRIED_LOCUST.get());
                        output.accept(ModItems.FRIED_CICADA.get());
// 食物佳肴：烤鸭（14 种，与生鸭子一一对应）
                        output.accept(ModItems.ROAST_DUCK_HEAD.get());
                        output.accept(ModItems.ROAST_DUCK_GIZZARD.get());
                        output.accept(ModItems.ROAST_DUCK_HEART.get());
                        output.accept(ModItems.ROAST_DUCK_WEB.get());
                        output.accept(ModItems.ROAST_DUCK_BLOOD_CAKE.get());
                        output.accept(ModItems.ROAST_DUCK_LEG.get());
                        output.accept(ModItems.ROAST_DUCK_RUMP.get());
                        output.accept(ModItems.ROAST_DUCK_TONGUE.get());
                        output.accept(ModItems.ROAST_DUCK_MEAT.get());
                        output.accept(ModItems.ROAST_DUCK_THIGH.get());
                        output.accept(ModItems.ROAST_DUCK_LIVER.get());
                        output.accept(ModItems.ROAST_DUCK_WING.get());
                        output.accept(ModItems.ROAST_DUCK_INTESTINE.get());
                        output.accept(ModItems.ROAST_DUCK_NECK.get());
// 食物佳肴：烤鹅（4 种，与生鹅一一对应）
                        output.accept(ModItems.ROAST_GOOSE_INTESTINE.get());
                        output.accept(ModItems.ROAST_GOOSE_LIVER.get());
                        output.accept(ModItems.ROAST_GOOSE_MEAT.get());
                        output.accept(ModItems.ROAST_GOOSE_LEG.get());
// 食物佳肴：动物内脏（6 种，与屠宰产物一一对应）
                        output.accept(ModItems.ROAST_ANIMAL_INTESTINE.get());
                        output.accept(ModItems.ROAST_ANIMAL_LUNG.get());
                        output.accept(ModItems.ROAST_ANIMAL_LIVER.get());
                        output.accept(ModItems.ROAST_ANIMAL_STOMACH.get());
                        output.accept(ModItems.ROAST_ANIMAL_HEART.get());
                        output.accept(ModItems.ROAST_ANIMAL_BLOOD_CAKE.get());
// 食物佳肴：原版动物肉（6 种，与屠宰产物一一对应）
                        output.accept(ModItems.ROAST_RABBIT_HEAD.get());
                        output.accept(ModItems.ROAST_DOG_MEAT.get());
                        output.accept(ModItems.ROAST_LLAMA_MEAT.get());
                        output.accept(ModItems.ROAST_DONKEY_MEAT.get());
                        output.accept(ModItems.ROAST_CAMEL_HUMP.get());
                        output.accept(ModItems.ROAST_CAMEL_MEAT.get());
// 食物佳肴：水产（24 种，与生鲜水产一一对应）
                        output.accept(ModItems.ROAST_SWIMMING_CRAB.get());
                        output.accept(ModItems.ROAST_SPOON_WORM.get());
                        output.accept(ModItems.ROAST_SILVERY_POMFRET.get());
                        output.accept(ModItems.ROAST_SEA_URCHIN.get());
                        output.accept(ModItems.ROAST_SEA_SNAIL.get());
                        output.accept(ModItems.ROAST_SEA_CUCUMBER.get());
                        output.accept(ModItems.ROAST_SCALLOP.get());
                        output.accept(ModItems.ROAST_SARDINE.get());
                        output.accept(ModItems.ROAST_RIVER_SNAIL.get());
                        output.accept(ModItems.ROAST_RAZOR_CLAM.get());
                        output.accept(ModItems.SHRIMP_MEAT.get());
                        output.accept(ModItems.ROAST_PACIFIC_SAURY.get());
                        output.accept(ModItems.ROAST_MUD_CRAB.get());
                        output.accept(ModItems.ROAST_MANTIS_SHRIMP.get());
                        output.accept(ModItems.ROAST_JELLYFISH.get());
                        output.accept(ModItems.ROAST_JAPANESE_HALFBEAK.get());
                        output.accept(ModItems.ROAST_HAIRY_CRAB.get());
                        output.accept(ModItems.ROAST_MUD_SHRIMP.get());
                        output.accept(ModItems.ROAST_GROUPER.get());
                        output.accept(ModItems.ROAST_OYSTER.get());
                        output.accept(ModItems.ROAST_CRAYFISH.get());
                        output.accept(ModItems.ROAST_CLAM.get());
                        output.accept(ModItems.DRIED_SHRIMP.get());
                        output.accept(ModItems.ROAST_ABALONE.get());
// 食物佳肴：烤鱼（整鱼，17 种）
                        output.accept(ModItems.ROAST_GRASS_CARP.get());
                        output.accept(ModItems.ROAST_TENCH.get());
                        output.accept(ModItems.ROAST_MANDARIN_FISH.get());
                        output.accept(ModItems.ROAST_BLACK_FISH.get());
                        output.accept(ModItems.ROAST_RECTUM.get());
                        output.accept(ModItems.ROAST_CRUCIAN_CARP.get());
                        output.accept(ModItems.ROAST_CHINESE_CARP.get());
                        output.accept(ModItems.ROAST_SILVER_CARP.get());
                        output.accept(ModItems.ROAST_BASS.get());
                        output.accept(ModItems.ROAST_TILAPIA.get());
                        output.accept(ModItems.ROAST_LOACH.get());
                        output.accept(ModItems.ROAST_CATFISH.get());
                        output.accept(ModItems.ROAST_GOBY.get());
                        output.accept(ModItems.ROAST_REDFIN.get());
                        output.accept(ModItems.ROAST_BLACK_CARP.get());
                        output.accept(ModItems.ROAST_PLECO.get());
                        output.accept(ModItems.ROAST_BIGHEAD_CARP.get());
// 经济与生活：两本手册排在最前（教程卷轴 → 开发者指导手册）
                        output.accept(ModItems.GUIDE_BOOK.get());
                        output.accept(ModItems.DEVELOPER_GUIDE_BOOK.get());
// 经济与生活：经济系列（自助取款机 / 小票打印机 / 银行卡 / 六种纸币）紧随其后
                        output.accept(ModItems.ATM.get());
                        output.accept(ModItems.RECEIPT_PRINTER.get());
                        output.accept(ModItems.CATERING_BUSINESS_LICENSE.get());
                        output.accept(ModItems.SHOPPING_COMPUTER.get());
                        output.accept(ModItems.PURCHASING_BOX.get());
                        output.accept(ModItems.SEAT_CUSHION.get());
                        output.accept(ModItems.CREDIT_CARD.get());
                        output.accept(ModItems.NAV_PHONE.get());
                        output.accept(ModItems.MONEY_1.get());
                        output.accept(ModItems.MONEY_5.get());
                        output.accept(ModItems.MONEY_10.get());
                        output.accept(ModItems.MONEY_20.get());
                        output.accept(ModItems.MONEY_50.get());
                        output.accept(ModItems.MONEY_100.get());
// 经济与生活：纺织系列
                        output.accept(ModItems.SILK.get());
                        output.accept(ModItems.SEWING_MACHINE.get());
                        output.accept(ModItems.BLANK_SILK_HAT.get());
                        output.accept(ModItems.BLANK_SILK_TUNIC.get());
                        output.accept(ModItems.BLANK_SILK_LEGGINGS.get());
                        output.accept(ModItems.BLANK_SILK_BOOTS.get());
                    }).build());

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
