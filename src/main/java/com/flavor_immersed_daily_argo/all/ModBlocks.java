package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.block.BraisedPrawnsBlockEntity;
import com.flavor_immersed_daily_argo.block.DonkeyFeastBlockEntity;
import com.flavor_immersed_daily_argo.block.DosingTubeBlock;
import com.flavor_immersed_daily_argo.block.EntityFoodDishBlock;
import com.flavor_immersed_daily_argo.block.FertileFarmlandBlock;
import com.flavor_immersed_daily_argo.block.ForestryFireBlock;
import com.flavor_immersed_daily_argo.block.FruitingLeavesBlock;
import com.flavor_immersed_daily_argo.block.HangingFruitBlock;
import com.flavor_immersed_daily_argo.block.MultiStageFoodBlock;
import com.flavor_immersed_daily_argo.block.PotStewGooseBlockEntity;
import com.flavor_immersed_daily_argo.block.RawSeafoodBlock;
import com.flavor_immersed_daily_argo.block.RawSwordfishBlock;
import com.flavor_immersed_daily_argo.block.RotatingWaterPipeBlock;
import com.flavor_immersed_daily_argo.block.SectorWaterPipeBlock;
import com.flavor_immersed_daily_argo.block.ShepherdsPurseCropBlock;
import com.flavor_immersed_daily_argo.block.WaterPipeBlock;
import com.flavor_immersed_daily_argo.block.WaterPumpBlock;
import com.flavor_immersed_daily_argo.block.WaterwheelBlock;
import com.flavor_immersed_daily_argo.block.WeatherInstrumentBlock;
import com.flavor_immersed_daily_argo.economy.AtmBlock;
import com.flavor_immersed_daily_argo.economy.CateringBusinessLicenseBlock;
import com.flavor_immersed_daily_argo.economy.ReceiptPrinterBlock;
import com.flavor_immersed_daily_argo.economy.ShoppingComputerBlock;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.fishing.FishBowlBlock;
import com.flavor_immersed_daily_argo.fishing.FishDisplayBoardBlock;
import com.flavor_immersed_daily_argo.fishing.FishProtectionBlock;
import com.flavor_immersed_daily_argo.fishing.FishingBoxBlock;
import com.flavor_immersed_daily_argo.fishing.RodPodBlock;
import com.flavor_immersed_daily_argo.forestry.InsectIncubatorBlock;
import com.flavor_immersed_daily_argo.herding.ChickenCoopBlock;
import com.flavor_immersed_daily_argo.herding.FeedingTroughBlock;
import com.flavor_immersed_daily_argo.herding.FishingNetBlock;
import com.flavor_immersed_daily_argo.herding.MethaneTankBlock;
import com.flavor_immersed_daily_argo.herding.PetNestBlock;
import com.flavor_immersed_daily_argo.herding.PurchasingBoxBlock;
import com.flavor_immersed_daily_argo.herding.RiverSiltBlock;
import com.flavor_immersed_daily_argo.herding.SaltPanBlock;
import com.flavor_immersed_daily_argo.herding.SaltPileBlock;
import com.flavor_immersed_daily_argo.herding.SepticTankBlock;
import com.flavor_immersed_daily_argo.textile.SewingMachineBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

// 方块注册
//
// 目前只包含 8 种立体（3D）生鲜海产：它们既是可手持食用的食物，也可以右键地面放置成装饰方块，
// 方块与物品共用同一套模型与贴图。
public final class ModBlocks {

    private ModBlocks() {
    }

    public static final DeferredRegister.Blocks REGISTRY =
            DeferredRegister.createBlocks(flavor_immersed_daily_argo.MODID);

// 生鲜方块的通用属性。
//
// 这些模型都是随手一放的鱼虾蟹，尺寸普遍超出单个方块（有的长达两格），
// 因此不设碰撞箱、不遮挡相邻方块面，徒手即可瞬间破坏。
    private static BlockBehaviour.Properties rawSeafood() {
        return BlockBehaviour.Properties.of()
                .instabreak()
                .noCollission()
                .noOcclusion()
                .sound(SoundType.SLIME_BLOCK);
    }

// 生鲅鱼
    public static final DeferredBlock<Block> RAW_CHINESE_MACKEREL =
            REGISTRY.register("raw_chinese_mackerel", () -> new RawSeafoodBlock(rawSeafood()));

// 生带鱼
    public static final DeferredBlock<Block> RAW_HAIRTAIL =
            REGISTRY.register("raw_hairtail", () -> new RawSeafoodBlock(rawSeafood()));

// 生黄鳍金枪鱼
    public static final DeferredBlock<Block> RAW_TUNA =
            REGISTRY.register("raw_tuna", () -> new RawSeafoodBlock(rawSeafood()));

// 生鳗鱼
    public static final DeferredBlock<Block> RAW_EEL =
            REGISTRY.register("raw_eel", () -> new RawSeafoodBlock(rawSeafood()));

// 生鮟鱇鱼
    public static final DeferredBlock<Block> RAW_ANGLERFISH =
            REGISTRY.register("raw_anglerfish", () -> new RawSeafoodBlock(rawSeafood()));

// 生阿拉斯加帝王蟹
    public static final DeferredBlock<Block> RAW_KING_CRAB =
            REGISTRY.register("raw_king_crab", () -> new RawSeafoodBlock(rawSeafood()));

// 生美洲螯龙虾
    public static final DeferredBlock<Block> RAW_LOBSTER =
            REGISTRY.register("raw_lobster", () -> new RawSeafoodBlock(rawSeafood()));

// 生面包蟹
    public static final DeferredBlock<Block> RAW_BROWN_CRAB =
            REGISTRY.register("raw_brown_crab", () -> new RawSeafoodBlock(rawSeafood()));

// 生剑鱼（模型约 4.9 格长，超出单方块范围，由方块实体渲染器渲染）
    public static final DeferredBlock<Block> RAW_SWORDFISH =
            REGISTRY.register("raw_swordfish", () -> new RawSwordfishBlock(rawSeafood()));

// 鸡窝：无碰撞箱可穿过；吸引禽类驻留、孵化放置的蛋、收集周围的蛋
    public static final DeferredBlock<Block> CHICKEN_COOP =
            REGISTRY.register("chicken_coop", () -> new ChickenCoopBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noCollission()
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 牧场食槽：一格槽位放饲料或纯净水，附近的动物饿了渴了自己过来吃；三个阶段靠 content 属性切换模型
// randomTicks：靠随机刻来触发「扫一遍周围动物」，不用每 tick 扫，开销极低
    public static final DeferredBlock<Block> FEEDING_TROUGH =
            REGISTRY.register("feeding_trough", () -> new FeedingTroughBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noCollission()
                    .noOcclusion()
                    .randomTicks()
                    .sound(SoundType.WOOD)));

// 化粪发酵桶：两格宽的机器，碰撞箱与模型都跟着 facing 转；自动收周围粪便，攒着慢慢发酵成有机肥料
// noOcclusion：模型超出自身那一格，不能让相邻方块面被剔掉
    public static final DeferredBlock<Block> SEPTIC_TANK =
            REGISTRY.register("septic_tank", () -> new SepticTankBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 沼气罐：单方块，只有 facing 四个朝向，模型跨格的部分靠 blockstate 旋转；架在化粪桶上方存加压沼气
// noOcclusion：模型超出自身那一格，不能让相邻方块面被剔掉
    public static final DeferredBlock<Block> METHANE_TANK =
            REGISTRY.register("methane_tank", () -> new MethaneTankBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 河道淤泥：整格高的水底淤泥。干燥时是 texture=0；铺在水下用饲料右键后变成 texture=1，可播种养殖水产
// 生长进度与苗种由方块实体记录，方块实体渲染器把苗种平铺在顶面
    public static final DeferredBlock<Block> RIVER_SILT =
            REGISTRY.register("river_silt", () -> new RiverSiltBlock(BlockBehaviour.Properties.of()
                    .strength(0.6F)
                    .sound(SoundType.MUD)));

// 晒盐场：整格盐田。顶面接触水后变成含水纹理并吸掉上方的水，水干后在正上方结出独立的盐堆
// 盐堆分三级，挖掉盐堆只会掉盐（1/2/3 份，带时运），晒盐场本身不会损坏
    public static final DeferredBlock<Block> SALT_PAN =
            REGISTRY.register("salt_pan", () -> new SaltPanBlock(BlockBehaviour.Properties.of()
                    .strength(0.2F)
                    .sound(SoundType.SAND)));

// 盐堆：晒盐场晒出的独立方块，只占方块底部几像素，靠随机刻自己从 1 级长到 3 级
// 硬度很低、徒手可敲；底下没有晒盐场时自动掉落，不留悬空的盐
    public static final DeferredBlock<Block> SALT_PILE =
            REGISTRY.register("salt_pile", () -> new SaltPileBlock(BlockBehaviour.Properties.of()
                    .strength(0.1F)
                    .noOcclusion()
                    .randomTicks()
                    .sound(SoundType.SAND)));

// 宠物窝：无碰撞箱（宠物要能站进窝里），驯服后的猫狗会寻路过来安家，过世后次日在此复活
// 带朝向：摆放后正面朝玩家
    public static final DeferredBlock<Block> PET_NEST =
            REGISTRY.register("pet_nest", () -> new PetNestBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noCollission()
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 肥沃耕地：外观与手感照原版耕地来，随机刻时额外把上方作物催一阶
// randomTicks：湿度衰减与本次催熟都走随机刻，不额外加 ticker
// isViewBlocking / isSuffocating 取 false：原版耕地那两条用的是私有的 Blocks::never，跨类引用不到，改用等价 lambda
    public static final DeferredBlock<Block> FERTILE_FARMLAND =
            REGISTRY.register("fertile_farmland", () -> new FertileFarmlandBlock(BlockBehaviour.Properties.of()
                    .randomTicks()
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)));

// 灌溉水车：碰撞箱 1×1×3，模型超出自身那一格（约 3.8 格高），由方块实体渲染器绘制
// noOcclusion：模型跨到上方两格，不能让相邻方块面被剔掉
    public static final DeferredBlock<Block> WATERWHEEL =
            REGISTRY.register("waterwheel", () -> new WaterwheelBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 林业火灾：林业网点闹火灾时冒出的火。不排随机刻（不蔓延、不熄灭），canBurn 恒为 false（不烧方块），
// 只能拿灭火器扑灭；noLootTable 破坏后不掉落任何东西
    public static final DeferredBlock<Block> FORESTRY_FIRE =
            REGISTRY.register("forestry_fire", () -> new ForestryFireBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .noOcclusion()
                    .lightLevel(state -> 15)
                    .sound(SoundType.WOOL)
                    .noLootTable()));

// 气象仪器：模型约 2.8 格高，由方块实体渲染器绘制；该区块闹非常规天气时风车盘转得飞快
// noOcclusion：模型超出自身那一格，不能让相邻方块面被剔掉
    public static final DeferredBlock<Block> WEATHER_INSTRUMENT =
            REGISTRY.register("weather_instrument", () -> new WeatherInstrumentBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

// 昆虫培育箱：带界面的培育机器，空手右键打开界面；有朝向，摆放后正面朝玩家
// noOcclusion：模型侧面内缩了一像素，按整方块遮挡会把贴着它的相邻方块那面剔掉，露出缝隙
    public static final DeferredBlock<Block> INSECT_INCUBATOR =
            REGISTRY.register("insect_incubator", () -> new InsectIncubatorBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 缝纫机：带界面的纺织机器，界面与切石机一致；有朝向，摆放后正面朝玩家
// noOcclusion：模型带透明通道走高 cutout，不能让相邻方块面被剔掉
    public static final DeferredBlock<Block> SEWING_MACHINE =
            REGISTRY.register("sewing_machine", () -> new SewingMachineBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 水管：单方块，靠 shape 状态自动认周围的管子，再由 blockstate 挑模型与旋转
// noOcclusion：管身只有 4×4 粗细，不该把相邻方块的面剔掉
// randomTicks：给喷水灌溉做个兜底，区块重载后靠随机刻把节拍续上
    public static final DeferredBlock<Block> WATER_PIPE =
            REGISTRY.register("water_pipe", () -> new WaterPipeBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noOcclusion()
                    .randomTicks()
                    .sound(SoundType.METAL)));

// 水泵：埋进水里后，与它相连的整张水管管网都会进入充水状态，管网末端会往外冒水花
// noOcclusion：模型只有中间一小坨，不该把相邻方块的面剔掉
    public static final DeferredBlock<Block> WATER_PUMP =
            REGISTRY.register("water_pump", () -> new WaterPumpBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

// 加料阀：管网里的一个环节，放金坷垃进去后让流经它的灌溉水带上催熟
// noOcclusion：阀体比整格小，不该把相邻方块的面剔掉
    public static final DeferredBlock<Block> DOSING_TUBE =
            REGISTRY.register("dosing_tube", () -> new DosingTubeBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

// 旋转水管：接在管网末端的旋转洒水器，通水后旋转臂播放 round 动画并撒一圈水花
// randomTicks：隔一格探测与灌溉靠计划刻，随机刻做区块重载的兜底
    public static final DeferredBlock<Block> ROTATING_WATER_PIPE =
            REGISTRY.register("rotating_water_pipe", () -> new RotatingWaterPipeBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .noOcclusion()
                    .randomTicks()
                    .sound(SoundType.METAL)));

// 扇形水管：接在管网末端的扇形洒水器，通水后朝正面水平扇形喷水
// randomTicks：隔一格探测与灌溉靠计划刻，随机刻做区块重载的兜底
    public static final DeferredBlock<Block> SECTOR_WATER_PIPE =
            REGISTRY.register("sector_water_pipe", () -> new SectorWaterPipeBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .noOcclusion()
                    .randomTicks()
                    .sound(SoundType.METAL)));

// 捕捞渔网：放进水里当渔网，随机刻在网面上刷出淡水/海水水产活体；玩家可穿过、其他生物被挡
// noOcclusion：网面很薄，不该把相邻方块的面剔掉
// randomTicks：刷新水产靠随机刻
// isViewBlocking / isSuffocating 取 false：网是通透的，玩家钻进网里时不该被 ScreenEffectRenderer 全屏叠一层网的贴图
    public static final DeferredBlock<Block> FISH_NET =
            REGISTRY.register("fish_net", () -> new FishingNetBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noOcclusion()
                    .randomTicks()
                    .sound(SoundType.WOOL)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)));

// 鱼护：放在水里养钓到的活鱼。可含水，四面围栏按相邻鱼护自动连接/隐藏
// noOcclusion：围栏只有一两像素厚，不该把相邻方块的面剔掉
    public static final DeferredBlock<Block> FISH_PROTECTION =
            REGISTRY.register("fish_protection", () -> new FishProtectionBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 钓鱼箱：36 格容器，收岸边钓获用；有朝向，摆放后正面朝玩家
    public static final DeferredBlock<Block> FISHING_BOX =
            REGISTRY.register("fishing_box", () -> new FishingBoxBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));

// 收购箱：27 格容器，把经济产物放进去点界面里的「出售」按今日收购价换成纸币
    public static final DeferredBlock<Block> PURCHASING_BOX =
            REGISTRY.register("purchasing_box", () -> new PurchasingBoxBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));

// 竿架：架着南山钓竿替玩家守竿，竿子由方块实体渲染器画出来
    public static final DeferredBlock<Block> ROD_POD =
            REGISTRY.register("rod_pod", () -> new RodPodBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 钓鱼展示架：一块立在方块一侧的木板，手持钓上来的水产右键挂上去，渲染那条鱼的实体模型
// 带水平朝向（摆放后展示面朝玩家），薄板的碰撞箱随朝向转
// noOcclusion：木板只有两像素厚，不该把相邻方块的面剔掉
    public static final DeferredBlock<Block> FISH_DISPLAY_BOARD =
            REGISTRY.register("fish_display_board", () -> new FishDisplayBoardBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 鱼缸：没有底的玻璃缸，可含水；相邻鱼缸四面玻璃按连接自动隐藏，横着拼、往上摞都行
// noOcclusion：玻璃只有一像素厚，不该把相邻方块的面剔掉
    public static final DeferredBlock<Block> FISH_BOWL =
            REGISTRY.register("fish_bowl", () -> new FishBowlBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .noOcclusion()
                    .sound(SoundType.GLASS)));

// 农作物（果树苗）

// 果树苗：种在地上会按 treeKey 对应的 configured_feature 长成一棵树。
//
// 树形模板借用父模组 flavor_immersed_daily 的三种木（清韵木 / 核毅木 / 虬藤木），
// 只按注册名引用、不做编译期依赖。
    private static DeferredBlock<Block> fruitSapling(String name, String treeKey) {
        return REGISTRY.register(name, () -> new SaplingBlock(treeGrower(name, treeKey),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.GRASS)));
    }

// 果树苗对应的树形。
//
// 那三份 configured_feature 引用了父模组的木头，所以都加了「装了父模组才加载」的条件；
// 没装父模组时注册表里根本没有这几棵树，树苗再指过去就会在生长时崩掉，
// 因此这里退化成原版橡树（登录时另有提示让玩家去装前置）。
    private static AbstractTreeGrower treeGrower(String name, String treeKey) {
        return new AbstractTreeGrower() {
            @Override
            protected ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> getConfiguredFeature(
                    net.minecraft.util.RandomSource random, boolean hasFlowers) {
// 没装父模组时退化成原版橡树
                if (!ModList.get().isLoaded("flavor_immersed_daily")) {
                    return net.minecraft.data.worldgen.features.TreeFeatures.OAK;
                }
                return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                        new ResourceLocation(flavor_immersed_daily_argo.MODID, treeKey));
            }
        };
    }

// 柚子苗：长成清韵木树形的柚子树
    public static final DeferredBlock<Block> POMELO_SAPLING = fruitSapling("pomelo_sapling", "pomelo_tree");

// 板栗苗：长成核毅木树形的板栗树
    public static final DeferredBlock<Block> CHESTNUT_SAPLING = fruitSapling("chestnut_sapling", "chestnut_tree");

// 番石榴苗：长成虬藤木树形的番石榴树
    public static final DeferredBlock<Block> GUAVA_SAPLING = fruitSapling("guava_sapling", "guava_tree");

// 农作物（果树叶与悬挂的果子）

// 悬挂的果子：挂在果树叶下方那颗果实，徒手右键可摘下。
//
// 不用随机刻，靠 scheduleTick 在树叶消失时掉落，所以不必 randomTicks()。
    private static DeferredBlock<Block> hangingFruit(String name, Supplier<Item> fruit) {
        return REGISTRY.register(name, () -> new HangingFruitBlock(fruit, BlockBehaviour.Properties.of()
                .strength(0.2F)
                .noCollission()
                .noOcclusion()
                .instabreak()
                .sound(SoundType.GRASS)));
    }

// 果树叶：随机刻累积成熟度，挂果后概率在下方长出 hanging 对应的悬挂果实。
//
// 树叶的常规方块属性（距离/枯萎/水生）沿用原版 LeavesBlock。
    private static DeferredBlock<Block> fruitingLeaves(String name, Supplier<Item> fruit,
                                                        Supplier<? extends Block> hanging) {
        return REGISTRY.register(name, () -> new FruitingLeavesBlock(fruit, hanging, BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

// 悬挂的柚子：挂在柚子树叶下
    public static final DeferredBlock<Block> HANGING_POMELO =
            hangingFruit("hanging_pomelo", () -> ModItems.POMELO.get());

// 悬挂的板栗：挂在板栗树叶下
    public static final DeferredBlock<Block> HANGING_CHESTNUT =
            hangingFruit("hanging_chestnut", () -> ModItems.CHESTNUT.get());

// 悬挂的番石榴：挂在番石榴树叶下
    public static final DeferredBlock<Block> HANGING_GUAVA =
            hangingFruit("hanging_guava", () -> ModItems.GUAVA.get());

// 柚子树叶：清韵木树形的果树叶
    public static final DeferredBlock<Block> POMELO_FRUITING_LEAVES =
            fruitingLeaves("pomelo_fruiting_leaves", () -> ModItems.POMELO.get(), () -> HANGING_POMELO.get());

// 板栗树叶：核毅木树形的果树叶
    public static final DeferredBlock<Block> CHESTNUT_FRUITING_LEAVES =
            fruitingLeaves("chestnut_fruiting_leaves", () -> ModItems.CHESTNUT.get(), () -> HANGING_CHESTNUT.get());

// 番石榴树叶：虬藤木树形的果树叶
    public static final DeferredBlock<Block> GUAVA_FRUITING_LEAVES =
            fruitingLeaves("guava_fruiting_leaves", () -> ModItems.GUAVA.get(), () -> HANGING_GUAVA.get());

// 食物佳肴：可摆上桌的整鸭菜

// 整鸭菜的方块属性：模型只占下半格，所以不遮挡相邻方块面；徒手即可瞬间破坏。
//
// 摆在地上之后没有掉落物（见 loot_table/blocks 里那三张空掉落表）。
    private static BlockBehaviour.Properties placeableDish() {
        return BlockBehaviour.Properties.of()
                .instabreak()
                .noOcclusion()
                .sound(SoundType.WOOD);
    }

// 北京烤鸭：3 个模型 = 3 个阶段，每口恢复 4 点饥饿值
    public static final DeferredBlock<Block> PEKING_ROAST_DUCK =
            REGISTRY.register("peking_roast_duck", () -> new MultiStageFoodBlock(2, 4, 0.8F, placeableDish()));

// 卤鸭拼盘：3 个模型 = 3 个阶段，每口恢复 4 点饥饿值
    public static final DeferredBlock<Block> BRAISED_DUCK_PLATTER =
            REGISTRY.register("braised_duck_platter", () -> new MultiStageFoodBlock(2, 4, 0.8F, placeableDish()));

// 手撕烤鸭：4 个模型 = 4 个阶段，每口恢复 3 点饥饿值
    public static final DeferredBlock<Block> ROAST_DUCK =
            REGISTRY.register("roast_duck", () -> new MultiStageFoodBlock(3, 3, 0.8F, placeableDish()));

// 走方块实体渲染器的可放置食物属性：模型远超单方块、又不进区块网格，所以不遮挡相邻方块面；
// 徒手即可瞬间破坏，摆好后没有掉落物（见 loot_table/blocks 里那两张空掉落表）。
    private static BlockBehaviour.Properties renderedDish() {
        return BlockBehaviour.Properties.of()
                .instabreak()
                .noOcclusion()
                .sound(SoundType.WOOD);
    }

// 铁锅炖大鹅：12 个模型 = 12 个阶段，整锅带饼子、多段嵌套旋转，只能由方块实体渲染器绘制
    public static final DeferredBlock<Block> POT_STEW_GOOSE =
            REGISTRY.register("pot_stew_goose", () -> new EntityFoodDishBlock(11, 2, 0.6F,
                    Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0),
                    PotStewGooseBlockEntity::new, renderedDish()));

// 全驴宴：只有一个模型，靠 stage 记吃了 20 口，吃完消失；3 格长的圆桌由方块实体渲染器绘制
    public static final DeferredBlock<Block> DONKEY_FEAST =
            REGISTRY.register("donkey_feast", () -> new EntityFoodDishBlock(19, 2, 0.6F,
                    Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
                    DonkeyFeastBlockEntity::new, renderedDish()));

// 法式鹅肝：1 个模型 = 1 个阶段，一小盘只能吃一口，每口恢复 6 点饥饿值
    public static final DeferredBlock<Block> FOIE_GRAS =
            REGISTRY.register("foie_gras", () -> new MultiStageFoodBlock(0, 6, 0.8F, placeableDish()));

// 广式烧鹅：6 个模型 = 6 个阶段，每口恢复 2 点饥饿值
    public static final DeferredBlock<Block> ROASTED_GOOSE =
            REGISTRY.register("roasted_goose", () -> new MultiStageFoodBlock(5, 2, 0.6F, placeableDish()));

// 沛县狗肉：4 个模型 = 4 个阶段，每口恢复 3 点饥饿值
    public static final DeferredBlock<Block> PEIXIAN_DOG_MEAT =
            REGISTRY.register("peixian_dog_meat", () -> new MultiStageFoodBlock(3, 3, 0.6F, placeableDish()));

// 清蒸大闸蟹：2 个模型 = 2 个阶段，每口恢复 4 点饥饿值
    public static final DeferredBlock<Block> STEAMED_HAIRY_CRAB =
            REGISTRY.register("steamed_hairy_crab", () -> new MultiStageFoodBlock(1, 4, 0.8F, placeableDish()));

// 白灼虾：4 个模型 = 4 个阶段，每口恢复 3 点饥饿值
    public static final DeferredBlock<Block> SCALDED_PRAWNS =
            REGISTRY.register("scalded_prawns", () -> new MultiStageFoodBlock(3, 3, 0.6F, placeableDish()));

// 油焖大虾：4 个实体模型 = 4 个阶段，多段嵌套旋转只能由方块实体渲染器绘制
    public static final DeferredBlock<Block> BRAISED_PRAWNS =
            REGISTRY.register("braised_prawns", () -> new EntityFoodDishBlock(3, 4, 0.8F,
                    Block.box(1.0, 0.0, 1.0, 15.0, 6.0, 15.0),
                    BraisedPrawnsBlockEntity::new, renderedDish()));

// 食物佳肴：本批新增的可摆放菜品

// 八卦汤：4 个模型 = 4 个阶段，每口恢复 3 点饥饿值
    public static final DeferredBlock<Block> TURTLE_PIGEON_SOUP =
            REGISTRY.register("turtle_pigeon_soup", () -> new MultiStageFoodBlock(3, 3, 0.6F, placeableDish()));

// 鲅鱼煎饼套餐：5 个模型 = 5 个阶段，右键取出一份鲅鱼卷煎饼（不吃）
    public static final DeferredBlock<Block> SPICY_MACKEREL_CRUMBLE =
            REGISTRY.register("spicy_mackerel_crumble", () -> new MultiStageFoodBlock(4, 3, 0.6F,
                    () -> ModItems.MACKEREL_PANCAKE_ROLL.get(), placeableDish()));

// 爆炒花蛤：3 个模型 = 3 个阶段，每口恢复 3 点饥饿值
    public static final DeferredBlock<Block> STIR_FRIED_CLAMS =
            REGISTRY.register("stir_fried_clams", () -> new MultiStageFoodBlock(2, 3, 0.6F, placeableDish()));

// 避风塘炒虾：3 个模型 = 3 个阶段，每口恢复 3 点饥饿值
    public static final DeferredBlock<Block> TYPHOON_SHELTER_PRAWN =
            REGISTRY.register("typhoon_shelter_prawn", () -> new MultiStageFoodBlock(2, 3, 0.6F, placeableDish()));

// 春秋剑鱼：6 个模型 = 6 个阶段，右键取出一份腌剑鱼肉（不吃）
    public static final DeferredBlock<Block> STINKY_SWORDFISH =
            REGISTRY.register("stinky_swordfish", () -> new MultiStageFoodBlock(5, 3, 0.6F,
                    () -> ModItems.PICKLED_SWORDFISH.get(), placeableDish()));

// 花蛤韭菜疙瘩汤：4 个模型 = 4 个阶段，每口恢复 3 点饥饿值
    public static final DeferredBlock<Block> CLAM_CHIVE_DOUGH_DROP_SOUP =
            REGISTRY.register("clam_chive_dough_drop_soup", () -> new MultiStageFoodBlock(3, 3, 0.6F, placeableDish()));

// 麻辣小龙虾：4 个模型 = 4 个阶段，每口恢复 3 点饥饿值
    public static final DeferredBlock<Block> SPICY_CRAYFISH =
            REGISTRY.register("spicy_crayfish", () -> new MultiStageFoodBlock(3, 3, 0.6F, placeableDish()));

// 蒸龙虾：3 个模型 = 3 个阶段；右键取一份熟龙虾肉，最后一份换成 4 个蟹黄
    public static final DeferredBlock<Block> STEAMED_LOBSTER =
            REGISTRY.register("steamed_lobster", () -> new MultiStageFoodBlock(2, 3, 0.6F,
                    () -> ModItems.COOKED_LOBSTER_MEAT.get(), () -> ModItems.CRAB_ROE.get(), 4,
                    placeableDish()));

// 蒸帝王蟹：9 个模型 = 9 个阶段；右键取一根熟帝王蟹腿棒，最后一份换成 4 个蟹黄
    public static final DeferredBlock<Block> STEAMED_KING_CRAB =
            REGISTRY.register("steamed_king_crab", () -> new MultiStageFoodBlock(8, 3, 0.6F,
                    () -> ModItems.COOKED_KING_CRAB_LEG.get(), () -> ModItems.CRAB_ROE.get(), 4,
                    placeableDish()));

// 蒸面包蟹：3 个模型 = 3 个阶段；右键取一份熟面包蟹肉，最后一份换成 4 个蟹黄
    public static final DeferredBlock<Block> STEAMED_BROWN_CRAB =
            REGISTRY.register("steamed_brown_crab", () -> new MultiStageFoodBlock(2, 3, 0.6F,
                    () -> ModItems.COOKED_BROWN_CRAB_MEAT.get(), () -> ModItems.CRAB_ROE.get(), 4,
                    placeableDish()));


// 经济与生活

// 自助取款机：两格高的机器方块，模型从脚下长到两格高、走 cutout；有朝向，摆放后正面朝玩家
// noOcclusion：模型跨到上方那一格，不能让相邻方块面被剔掉
    public static final DeferredBlock<Block> ATM =
            REGISTRY.register("atm", () -> new AtmBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

// 小票打印机：小巧的台面机器，有水平朝向；state=2 时吐出一张小票并刷新外卖小哥
// noOcclusion：模型只在单格里，但让出朝向面以免影响相邻面的剔除
    public static final DeferredBlock<Block> RECEIPT_PRINTER =
            REGISTRY.register("receipt_printer", () -> new ReceiptPrinterBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

// 餐饮营业执照：挂在墙上的牌匾，有水平朝向；开业后随机刻维持最多 6 个顾客，打烊则清空顾客
// noOcclusion：薄板贴着墙面，不让相邻方块面被剔掉
    public static final DeferredBlock<Block> CATERING_BUSINESS_LICENSE =
            REGISTRY.register("catering_business_license", () -> new CateringBusinessLicenseBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0F)
                            .noOcclusion()
                            .randomTicks()
                            .sound(SoundType.WOOD)));

// 商业电脑：家电方块，有水平朝向（屏幕朝玩家）；空手 / 持物右键打开市场界面
// noOcclusion：屏幕立在台面上，模型超出自身那一格不多的部分也让出相邻面
    public static final DeferredBlock<Block> SHOPPING_COMPUTER =
            REGISTRY.register("shopping_computer", () -> new ShoppingComputerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .noOcclusion()
                            .sound(SoundType.METAL)));

// 荠菜：大田作物，五个生长阶段（模型见 block/shepherds_purse_stage0~4）；
// 方块本身没有 BlockItem，种植用的种子物品在 ModItems 里单独注册
    public static final DeferredBlock<ShepherdsPurseCropBlock> SHEPHERDS_PURSE =
            REGISTRY.register("shepherds_purse", () -> new ShepherdsPurseCropBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .randomTicks()
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.CROP)
                            .pushReaction(PushReaction.DESTROY)));

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
