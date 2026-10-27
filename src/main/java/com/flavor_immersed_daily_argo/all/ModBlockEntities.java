package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.block.BraisedPrawnsBlockEntity;
import com.flavor_immersed_daily_argo.block.DonkeyFeastBlockEntity;
import com.flavor_immersed_daily_argo.block.DosingTubeBlockEntity;
import com.flavor_immersed_daily_argo.block.PotStewGooseBlockEntity;
import com.flavor_immersed_daily_argo.block.RawSwordfishBlockEntity;
import com.flavor_immersed_daily_argo.block.RotatingWaterPipeBlockEntity;
import com.flavor_immersed_daily_argo.block.WaterwheelBlockEntity;
import com.flavor_immersed_daily_argo.block.WeatherInstrumentBlockEntity;
import com.flavor_immersed_daily_argo.economy.ReceiptPrinterBlockEntity;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.fishing.FishDisplayBoardBlockEntity;
import com.flavor_immersed_daily_argo.fishing.FishingBoxBlockEntity;
import com.flavor_immersed_daily_argo.fishing.RodPodBlockEntity;
import com.flavor_immersed_daily_argo.forestry.InsectIncubatorBlockEntity;
import com.flavor_immersed_daily_argo.herding.ChickenCoopBlockEntity;
import com.flavor_immersed_daily_argo.herding.FeedingTroughBlockEntity;
import com.flavor_immersed_daily_argo.herding.MethaneTankBlockEntity;
import com.flavor_immersed_daily_argo.herding.PetNestBlockEntity;
import com.flavor_immersed_daily_argo.herding.PurchasingBoxBlockEntity;
import com.flavor_immersed_daily_argo.herding.RiverSiltBlockEntity;
import com.flavor_immersed_daily_argo.herding.SepticTankBlockEntity;
import com.flavor_immersed_daily_argo.textile.SewingMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 方块实体注册
//
// 目前只有生剑鱼需要：它的模型超出单方块范围，必须由方块实体渲染器绘制。
public final class ModBlockEntities {

    private ModBlockEntities() {
    }

    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, flavor_immersed_daily_argo.MODID);

// 生剑鱼
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RawSwordfishBlockEntity>> RAW_SWORDFISH_ENTITY =
            REGISTRY.register("raw_swordfish_entity",
                    () -> BlockEntityType.Builder.of(RawSwordfishBlockEntity::new, ModBlocks.RAW_SWORDFISH.get()).build(null));

// 鸡窝
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChickenCoopBlockEntity>> CHICKEN_COOP_ENTITY =
            REGISTRY.register("chicken_coop_entity",
                    () -> BlockEntityType.Builder.of(ChickenCoopBlockEntity::new, ModBlocks.CHICKEN_COOP.get()).build(null));

// 牧场食槽
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FeedingTroughBlockEntity>> FEEDING_TROUGH_ENTITY =
            REGISTRY.register("feeding_trough_entity",
                    () -> BlockEntityType.Builder.of(FeedingTroughBlockEntity::new, ModBlocks.FEEDING_TROUGH.get()).build(null));

// 化粪发酵桶
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SepticTankBlockEntity>> SEPTIC_TANK_ENTITY =
            REGISTRY.register("septic_tank_entity",
                    () -> BlockEntityType.Builder.of(SepticTankBlockEntity::new, ModBlocks.SEPTIC_TANK.get()).build(null));

// 沼气罐
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MethaneTankBlockEntity>> METHANE_TANK_ENTITY =
            REGISTRY.register("methane_tank_entity",
                    () -> BlockEntityType.Builder.of(MethaneTankBlockEntity::new, ModBlocks.METHANE_TANK.get()).build(null));

// 河道淤泥：记录苗种、产物与生长进度，并把苗种同步给方块实体渲染器
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiverSiltBlockEntity>> RIVER_SILT_ENTITY =
            REGISTRY.register("river_silt_entity",
                    () -> BlockEntityType.Builder.of(RiverSiltBlockEntity::new, ModBlocks.RIVER_SILT.get()).build(null));

// 宠物窝：猫狗在此安家，过世后次日在此复活
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PetNestBlockEntity>> PET_NEST_ENTITY =
            REGISTRY.register("pet_nest_entity",
                    () -> BlockEntityType.Builder.of(PetNestBlockEntity::new, ModBlocks.PET_NEST.get()).build(null));

// 灌溉水车：模型约 3.8 格高，由方块实体渲染器绘制；tick 负责刷新工作状态与出水花
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterwheelBlockEntity>> WATERWHEEL_ENTITY =
            REGISTRY.register("waterwheel_entity",
                    () -> BlockEntityType.Builder.of(WaterwheelBlockEntity::new, ModBlocks.WATERWHEEL.get()).build(null));

// 气象仪器：模型约 2.8 格高，由方块实体渲染器绘制；tick 负责查询该区块是否有非常规天气
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WeatherInstrumentBlockEntity>> WEATHER_INSTRUMENT_ENTITY =
            REGISTRY.register("weather_instrument_entity",
                    () -> BlockEntityType.Builder.of(WeatherInstrumentBlockEntity::new, ModBlocks.WEATHER_INSTRUMENT.get()).build(null));

// 昆虫培育箱：4 个输入槽 + 6 个产出槽的容器，逻辑暂未实现
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InsectIncubatorBlockEntity>> INSECT_INCUBATOR_ENTITY =
            REGISTRY.register("insect_incubator_entity",
                    () -> BlockEntityType.Builder.of(InsectIncubatorBlockEntity::new, ModBlocks.INSECT_INCUBATOR.get()).build(null));

// 缝纫机：1 个输入槽的容器，配方在界面里匹配
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SewingMachineBlockEntity>> SEWING_MACHINE_ENTITY =
            REGISTRY.register("sewing_machine_entity",
                    () -> BlockEntityType.Builder.of(SewingMachineBlockEntity::new, ModBlocks.SEWING_MACHINE.get()).build(null));

// 钓鱼箱：36 格容器，收岸边钓获用
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishingBoxBlockEntity>> FISHING_BOX_ENTITY =
            REGISTRY.register("fishing_box_entity",
                    () -> BlockEntityType.Builder.of(FishingBoxBlockEntity::new, ModBlocks.FISHING_BOX.get()).build(null));

// 收购箱：27 格容器，把经济产物放进去点界面里的「出售」按今日收购价换成纸币
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PurchasingBoxBlockEntity>> PURCHASING_BOX_ENTITY =
            REGISTRY.register("purchasing_box_entity",
                    () -> BlockEntityType.Builder.of(PurchasingBoxBlockEntity::new, ModBlocks.PURCHASING_BOX.get()).build(null));

// 竿架：架着南山钓竿替玩家守竿，竿子由方块实体渲染器画出来
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RodPodBlockEntity>> ROD_POD_ENTITY =
            REGISTRY.register("rod_pod_entity",
                    () -> BlockEntityType.Builder.of(RodPodBlockEntity::new, ModBlocks.ROD_POD.get()).build(null));

// 钓鱼展示架：挂着的那条水产以物品栈记下来，随方块同步给客户端画实体模型
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishDisplayBoardBlockEntity>> FISH_DISPLAY_BOARD_ENTITY =
            REGISTRY.register("fish_display_board_entity",
                    () -> BlockEntityType.Builder.of(FishDisplayBoardBlockEntity::new, ModBlocks.FISH_DISPLAY_BOARD.get()).build(null));

// 加料阀：存罐里金坷垃还剩多久生效，tick 里倒数并把管网上的催熟加成刷新
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DosingTubeBlockEntity>> DOSING_TUBE_ENTITY =
            REGISTRY.register("dosing_tube_entity",
                    () -> BlockEntityType.Builder.of(DosingTubeBlockEntity::new, ModBlocks.DOSING_TUBE.get()).build(null));

// 旋转水管：无数据，只为挂上 RotatingWaterPipeRenderer 绘制 round 旋转动画
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RotatingWaterPipeBlockEntity>> ROTATING_WATER_PIPE_ENTITY =
            REGISTRY.register("rotating_water_pipe_entity",
                    () -> BlockEntityType.Builder.of(RotatingWaterPipeBlockEntity::new, ModBlocks.ROTATING_WATER_PIPE.get()).build(null));

// 小票打印机：每日菜单只展示 + 出单态刷新外卖小哥，tick 里驱动状态切换
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReceiptPrinterBlockEntity>> RECEIPT_PRINTER_ENTITY =
            REGISTRY.register("receipt_printer_entity",
                    () -> BlockEntityType.Builder.of(ReceiptPrinterBlockEntity::new, ModBlocks.RECEIPT_PRINTER.get()).build(null));

// 铁锅炖大鹅：无数据，只为挂上 PotStewGooseRenderer 按 stage 换 12 个阶段的锅
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PotStewGooseBlockEntity>> POT_STEW_GOOSE_ENTITY =
            REGISTRY.register("pot_stew_goose_entity",
                    () -> BlockEntityType.Builder.of(PotStewGooseBlockEntity::new, ModBlocks.POT_STEW_GOOSE.get()).build(null));

// 全驴宴：无数据，只为挂上 DonkeyFeastRenderer 画那张会转的圆桌
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DonkeyFeastBlockEntity>> DONKEY_FEAST_ENTITY =
            REGISTRY.register("donkey_feast_entity",
                    () -> BlockEntityType.Builder.of(DonkeyFeastBlockEntity::new, ModBlocks.DONKEY_FEAST.get()).build(null));

// 油焖大虾：无数据，只为挂上 BraisedPrawnsRenderer 按 stage 换 4 个阶段的锅
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BraisedPrawnsBlockEntity>> BRAISED_PRAWNS_ENTITY =
            REGISTRY.register("braised_prawns_entity",
                    () -> BlockEntityType.Builder.of(BraisedPrawnsBlockEntity::new, ModBlocks.BRAISED_PRAWNS.get()).build(null));

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
