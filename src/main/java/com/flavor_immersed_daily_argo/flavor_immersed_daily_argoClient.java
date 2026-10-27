package com.flavor_immersed_daily_argo;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModBlocks;
import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModFluidTypes;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.all.ModMenus;
import com.flavor_immersed_daily_argo.client.block.BraisedPrawnsItemRenderer;
import com.flavor_immersed_daily_argo.client.block.BraisedPrawnsRenderer;
import com.flavor_immersed_daily_argo.client.block.DonkeyFeastItemRenderer;
import com.flavor_immersed_daily_argo.client.block.DonkeyFeastRenderer;
import com.flavor_immersed_daily_argo.client.block.FishDisplayBoardRenderer;
import com.flavor_immersed_daily_argo.client.block.PotStewGooseItemRenderer;
import com.flavor_immersed_daily_argo.client.block.PotStewGooseRenderer;
import com.flavor_immersed_daily_argo.client.block.RawSwordfishItemRenderer;
import com.flavor_immersed_daily_argo.client.block.RawSwordfishRenderer;
import com.flavor_immersed_daily_argo.client.block.RiverSiltRenderer;
import com.flavor_immersed_daily_argo.client.block.RodPodRenderer;
import com.flavor_immersed_daily_argo.client.block.RotatingWaterPipeRenderer;
import com.flavor_immersed_daily_argo.client.block.SewingMachineRenderer;
import com.flavor_immersed_daily_argo.client.block.WaterwheelRenderer;
import com.flavor_immersed_daily_argo.client.block.WeatherInstrumentRenderer;
import com.flavor_immersed_daily_argo.client.render.ClientSilkArmorPreview;
import com.flavor_immersed_daily_argo.client.render.SilkDesignLayer;
import com.flavor_immersed_daily_argo.client.entity.model.BraisedPrawnsStage0Model;
import com.flavor_immersed_daily_argo.client.entity.model.BraisedPrawnsStage1Model;
import com.flavor_immersed_daily_argo.client.entity.model.BraisedPrawnsStage2Model;
import com.flavor_immersed_daily_argo.client.entity.model.BraisedPrawnsStage3Model;
import com.flavor_immersed_daily_argo.client.entity.model.DonkeyFeastModel;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage0Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage10Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage11Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage1Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage2Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage3Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage4Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage5Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage6Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage7Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage8Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage9Model;
import com.flavor_immersed_daily_argo.client.entity.model.RawSwordfishModel;
import com.flavor_immersed_daily_argo.client.entity.model.PoopModel;
import com.flavor_immersed_daily_argo.client.entity.model.CartonModel;
import com.flavor_immersed_daily_argo.client.entity.model.CustomerModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.CartonRenderer;
import com.flavor_immersed_daily_argo.client.entity.renderer.CustomerRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.TakeawayBagModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.TakeawayBagRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.DeliveryRiderModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.DeliveryPointRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.ScarecrowModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.ScarecrowRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.BambooratModel;
import com.flavor_immersed_daily_argo.client.entity.model.MouseModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.MouseRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.PoisonBaitStationModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.PoisonBaitStationRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.UavModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.UavRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.RotatingWaterPipeModel;
import com.flavor_immersed_daily_argo.client.entity.model.WaterwheelModel;
import com.flavor_immersed_daily_argo.client.entity.model.WeatherInstrumentModel;
import com.flavor_immersed_daily_argo.client.entity.model.BullfrogModel;
import com.flavor_immersed_daily_argo.client.entity.model.DuckModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.DuckRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.GooseModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.GooseRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.GrassCarpModel;
import com.flavor_immersed_daily_argo.client.entity.model.HandcartModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.HandcartRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.HarvesterModel;
import com.flavor_immersed_daily_argo.client.entity.model.MandarinFishModel;
import com.flavor_immersed_daily_argo.client.entity.model.PigeonModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.PigeonRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.QuailModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.QuailRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.TenchModel;
import com.flavor_immersed_daily_argo.client.entity.model.TurkeyModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.TurkeyRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.BassModel;
import com.flavor_immersed_daily_argo.client.entity.model.BigheadCarpModel;
import com.flavor_immersed_daily_argo.client.entity.model.BlackCarpModel;
import com.flavor_immersed_daily_argo.client.entity.model.BlackFishModel;
import com.flavor_immersed_daily_argo.client.entity.model.CatfishModel;
import com.flavor_immersed_daily_argo.client.entity.model.ChineseCarpModel;
import com.flavor_immersed_daily_argo.client.entity.model.CrucianCarpModel;
import com.flavor_immersed_daily_argo.client.entity.model.GobyModel;
import com.flavor_immersed_daily_argo.client.entity.model.LoachModel;
import com.flavor_immersed_daily_argo.client.entity.model.PlecoModel;
import com.flavor_immersed_daily_argo.client.entity.model.RectumModel;
import com.flavor_immersed_daily_argo.client.entity.model.RedfinModel;
import com.flavor_immersed_daily_argo.client.entity.model.SilverCarpModel;
import com.flavor_immersed_daily_argo.client.entity.model.TilapiaModel;
import com.flavor_immersed_daily_argo.client.entity.model.AnglerfishModel;
import com.flavor_immersed_daily_argo.client.entity.model.BrownCrabModel;
import com.flavor_immersed_daily_argo.client.entity.model.ChineseMackerelModel;
import com.flavor_immersed_daily_argo.client.entity.model.EelModel;
import com.flavor_immersed_daily_argo.client.entity.model.FishingFloatingPlatformModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.FishingFloatingPlatformRenderer;
import com.flavor_immersed_daily_argo.client.entity.renderer.FallingFruitEntityRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.HairtailModel;
import com.flavor_immersed_daily_argo.client.entity.model.KingCrabModel;
import com.flavor_immersed_daily_argo.client.entity.model.LobsterModel;
import com.flavor_immersed_daily_argo.client.entity.model.TurtleModel;
import com.flavor_immersed_daily_argo.client.entity.model.HairyCrabModel;
import com.flavor_immersed_daily_argo.client.entity.model.PrawnModel;
import com.flavor_immersed_daily_argo.client.entity.model.SwimmingCrabModel;
import com.flavor_immersed_daily_argo.client.entity.model.MudCrabModel;
import com.flavor_immersed_daily_argo.client.entity.model.SardineModel;
import com.flavor_immersed_daily_argo.client.entity.model.GrouperModel;
import com.flavor_immersed_daily_argo.client.entity.model.PacificSauryModel;
import com.flavor_immersed_daily_argo.client.entity.model.SilveryPomfretModel;
import com.flavor_immersed_daily_argo.client.entity.model.JapaneseHalfbeakModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.PoopRenderer;
import com.flavor_immersed_daily_argo.client.entity.renderer.ModFishingHookRenderer;
import com.flavor_immersed_daily_argo.client.entity.model.SwordfishModel;
import com.flavor_immersed_daily_argo.client.entity.model.TunaModel;
import com.flavor_immersed_daily_argo.client.entity.renderer.SimpleMobRenderer;
import com.flavor_immersed_daily_argo.fishing.FishingBoxScreen;
import com.flavor_immersed_daily_argo.fishing.FishingRodModMenu;
import com.flavor_immersed_daily_argo.fishing.FishingRodModScreen;
import com.flavor_immersed_daily_argo.fishing.FishingRodModelOverrides;
import com.flavor_immersed_daily_argo.fishing.FishingRodStats;
import com.flavor_immersed_daily_argo.fishing.SteelyardScreen;
import com.flavor_immersed_daily_argo.forestry.InsectIncubatorScreen;
import com.flavor_immersed_daily_argo.herding.ChickenCoopScreen;
import com.flavor_immersed_daily_argo.herding.PurchasingBoxScreen;
import com.flavor_immersed_daily_argo.item.PoultryCatchingNetItem;
import com.flavor_immersed_daily_argo.textile.SewingMachineScreen;
import com.flavor_immersed_daily_argo.economy.AtmScreen;
import com.flavor_immersed_daily_argo.economy.MarketScreen;
import com.flavor_immersed_daily_argo.economy.NavPhoneItem;
import com.flavor_immersed_daily_argo.economy.ReceiptPrinterScreen;
import com.flavor_immersed_daily_argo.textile.SilkArmorPreview;
import com.flavor_immersed_daily_argo.uav.UavClient;
import com.flavor_immersed_daily_argo.uav.UavScreen;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@Mod(flavor_immersed_daily_argo.MODID)
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class flavor_immersed_daily_argoClient {

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DuckModel.LAYER_LOCATION, DuckModel::createBodyLayer);
        event.registerLayerDefinition(GooseModel.LAYER_LOCATION, GooseModel::createBodyLayer);
        event.registerLayerDefinition(PigeonModel.LAYER_LOCATION, PigeonModel::createBodyLayer);
        event.registerLayerDefinition(QuailModel.LAYER_LOCATION, QuailModel::createBodyLayer);
        event.registerLayerDefinition(TurkeyModel.LAYER_LOCATION, TurkeyModel::createBodyLayer);
        event.registerLayerDefinition(BullfrogModel.LAYER_LOCATION, BullfrogModel::createBodyLayer);
        event.registerLayerDefinition(BambooratModel.LAYER_LOCATION, BambooratModel::createBodyLayer);
        event.registerLayerDefinition(MouseModel.LAYER_LOCATION, MouseModel::createBodyLayer);
        event.registerLayerDefinition(GrassCarpModel.LAYER_LOCATION, GrassCarpModel::createBodyLayer);
        event.registerLayerDefinition(HarvesterModel.LAYER_LOCATION, HarvesterModel::createBodyLayer);
        event.registerLayerDefinition(TenchModel.LAYER_LOCATION, TenchModel::createBodyLayer);
        event.registerLayerDefinition(MandarinFishModel.LAYER_LOCATION, MandarinFishModel::createBodyLayer);
        event.registerLayerDefinition(LoachModel.LAYER_LOCATION, LoachModel::createBodyLayer);
        event.registerLayerDefinition(PlecoModel.LAYER_LOCATION, PlecoModel::createBodyLayer);
        event.registerLayerDefinition(GobyModel.LAYER_LOCATION, GobyModel::createBodyLayer);
        event.registerLayerDefinition(SilverCarpModel.LAYER_LOCATION, SilverCarpModel::createBodyLayer);
        event.registerLayerDefinition(TilapiaModel.LAYER_LOCATION, TilapiaModel::createBodyLayer);
        event.registerLayerDefinition(RedfinModel.LAYER_LOCATION, RedfinModel::createBodyLayer);
        event.registerLayerDefinition(BlackCarpModel.LAYER_LOCATION, BlackCarpModel::createBodyLayer);
        event.registerLayerDefinition(RectumModel.LAYER_LOCATION, RectumModel::createBodyLayer);
        event.registerLayerDefinition(BassModel.LAYER_LOCATION, BassModel::createBodyLayer);
        event.registerLayerDefinition(ChineseCarpModel.LAYER_LOCATION, ChineseCarpModel::createBodyLayer);
        event.registerLayerDefinition(CrucianCarpModel.LAYER_LOCATION, CrucianCarpModel::createBodyLayer);
        event.registerLayerDefinition(CatfishModel.LAYER_LOCATION, CatfishModel::createBodyLayer);
        event.registerLayerDefinition(BigheadCarpModel.LAYER_LOCATION, BigheadCarpModel::createBodyLayer);
        event.registerLayerDefinition(BlackFishModel.LAYER_LOCATION, BlackFishModel::createBodyLayer);
        event.registerLayerDefinition(KingCrabModel.LAYER_LOCATION, KingCrabModel::createBodyLayer);
        event.registerLayerDefinition(SwordfishModel.LAYER_LOCATION, SwordfishModel::createBodyLayer);
        event.registerLayerDefinition(HairtailModel.LAYER_LOCATION, HairtailModel::createBodyLayer);
        event.registerLayerDefinition(TunaModel.LAYER_LOCATION, TunaModel::createBodyLayer);
        event.registerLayerDefinition(BrownCrabModel.LAYER_LOCATION, BrownCrabModel::createBodyLayer);
        event.registerLayerDefinition(AnglerfishModel.LAYER_LOCATION, AnglerfishModel::createBodyLayer);
        event.registerLayerDefinition(ChineseMackerelModel.LAYER_LOCATION, ChineseMackerelModel::createBodyLayer);
        event.registerLayerDefinition(EelModel.LAYER_LOCATION, EelModel::createBodyLayer);
        event.registerLayerDefinition(LobsterModel.LAYER_LOCATION, LobsterModel::createBodyLayer);
        event.registerLayerDefinition(TurtleModel.LAYER_LOCATION, TurtleModel::createBodyLayer);
        event.registerLayerDefinition(HairyCrabModel.LAYER_LOCATION, HairyCrabModel::createBodyLayer);
        event.registerLayerDefinition(PrawnModel.LAYER_LOCATION, PrawnModel::createBodyLayer);
        event.registerLayerDefinition(SwimmingCrabModel.LAYER_LOCATION, SwimmingCrabModel::createBodyLayer);
        event.registerLayerDefinition(MudCrabModel.LAYER_LOCATION, MudCrabModel::createBodyLayer);
        event.registerLayerDefinition(SardineModel.LAYER_LOCATION, SardineModel::createBodyLayer);
        event.registerLayerDefinition(GrouperModel.LAYER_LOCATION, GrouperModel::createBodyLayer);
        event.registerLayerDefinition(PacificSauryModel.LAYER_LOCATION, PacificSauryModel::createBodyLayer);
        event.registerLayerDefinition(SilveryPomfretModel.LAYER_LOCATION, SilveryPomfretModel::createBodyLayer);
        event.registerLayerDefinition(JapaneseHalfbeakModel.LAYER_LOCATION, JapaneseHalfbeakModel::createBodyLayer);
        event.registerLayerDefinition(RawSwordfishModel.LAYER_LOCATION, RawSwordfishModel::createBodyLayer);
        event.registerLayerDefinition(PoopModel.LAYER_LOCATION, PoopModel::createBodyLayer);
        event.registerLayerDefinition(CartonModel.LAYER_LOCATION, CartonModel::createBodyLayer);
        event.registerLayerDefinition(TakeawayBagModel.LAYER_LOCATION, TakeawayBagModel::createBodyLayer);
        event.registerLayerDefinition(DeliveryRiderModel.LAYER_LOCATION, DeliveryRiderModel::createBodyLayer);
        event.registerLayerDefinition(ScarecrowModel.LAYER_LOCATION, ScarecrowModel::createBodyLayer);
        event.registerLayerDefinition(PoisonBaitStationModel.LAYER_LOCATION, PoisonBaitStationModel::createBodyLayer);
        event.registerLayerDefinition(HandcartModel.LAYER_LOCATION, HandcartModel::createBodyLayer);
        event.registerLayerDefinition(FishingFloatingPlatformModel.LAYER_LOCATION, FishingFloatingPlatformModel::createBodyLayer);
        event.registerLayerDefinition(UavModel.LAYER_LOCATION, UavModel::createBodyLayer);
        event.registerLayerDefinition(WaterwheelModel.LAYER_LOCATION, WaterwheelModel::createBodyLayer);
        event.registerLayerDefinition(RotatingWaterPipeModel.LAYER_LOCATION, RotatingWaterPipeModel::createBodyLayer);
        event.registerLayerDefinition(WeatherInstrumentModel.LAYER_LOCATION, WeatherInstrumentModel::createBodyLayer);
        event.registerLayerDefinition(CustomerModel.LAYER_LOCATION, CustomerModel::createBodyLayer);
// 铁锅炖大鹅：12 个模型 = 12 个食用阶段，都由方块实体渲染器绘制
        event.registerLayerDefinition(PotStewGooseStage0Model.LAYER_LOCATION, PotStewGooseStage0Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage1Model.LAYER_LOCATION, PotStewGooseStage1Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage2Model.LAYER_LOCATION, PotStewGooseStage2Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage3Model.LAYER_LOCATION, PotStewGooseStage3Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage4Model.LAYER_LOCATION, PotStewGooseStage4Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage5Model.LAYER_LOCATION, PotStewGooseStage5Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage6Model.LAYER_LOCATION, PotStewGooseStage6Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage7Model.LAYER_LOCATION, PotStewGooseStage7Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage8Model.LAYER_LOCATION, PotStewGooseStage8Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage9Model.LAYER_LOCATION, PotStewGooseStage9Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage10Model.LAYER_LOCATION, PotStewGooseStage10Model::createBodyLayer);
        event.registerLayerDefinition(PotStewGooseStage11Model.LAYER_LOCATION, PotStewGooseStage11Model::createBodyLayer);
// 全驴宴：只有一个模型，圆桌按导出的 round 动画慢慢转
        event.registerLayerDefinition(DonkeyFeastModel.LAYER_LOCATION, DonkeyFeastModel::createBodyLayer);
// 油焖大虾：4 个模型 = 4 个食用阶段
        event.registerLayerDefinition(BraisedPrawnsStage0Model.LAYER_LOCATION, BraisedPrawnsStage0Model::createBodyLayer);
        event.registerLayerDefinition(BraisedPrawnsStage1Model.LAYER_LOCATION, BraisedPrawnsStage1Model::createBodyLayer);
        event.registerLayerDefinition(BraisedPrawnsStage2Model.LAYER_LOCATION, BraisedPrawnsStage2Model::createBodyLayer);
        event.registerLayerDefinition(BraisedPrawnsStage3Model.LAYER_LOCATION, BraisedPrawnsStage3Model::createBodyLayer);

// 空白丝绸装备穿在身上时的花纹叠加层：比原版盔甲模型各向外扩一点，压在纯白盔甲之上
        event.registerLayerDefinition(SilkDesignLayer.OUTER_LAYER, SilkDesignLayer::outerDefinition);
        event.registerLayerDefinition(SilkDesignLayer.INNER_LAYER, SilkDesignLayer::innerDefinition);
    }

// 给玩家（两种手臂模型）以及所有用人形模型的生物挂上丝绸花纹渲染层
    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new SilkDesignLayer(renderer));
            }
        }
        for (EntityType<?> type : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
// 玩家在上面按皮肤已经挂过一次了，这里别重复挂
            if (type == EntityType.PLAYER) {
                continue;
            }
            LivingEntityRenderer<?, ?> living = livingRenderer(event, type);
            if (living != null && living.getModel() instanceof HumanoidModel) {
                living.addLayer(new SilkDesignLayer(living));
            }
        }
    }

// getRenderer 内部直接做 (LivingEntityRenderer) 强制转换，而 marker、经验球等用的是
// NoopRenderer，遍历全部实体类型时必然抛 ClassCastException，这里吞掉并当作没有渲染器
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static LivingEntityRenderer<?, ?> livingRenderer(EntityRenderersEvent.AddLayers event, EntityType<?> type) {
        try {
            return event.getRenderer((EntityType) type);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DUCK.get(), DuckRenderer::new);
        event.registerEntityRenderer(ModEntities.GOOSE.get(), GooseRenderer::new);
        event.registerEntityRenderer(ModEntities.PIGEON.get(), PigeonRenderer::new);
        event.registerEntityRenderer(ModEntities.QUAIL.get(), QuailRenderer::new);
        event.registerEntityRenderer(ModEntities.TURKEY.get(), TurkeyRenderer::new);
        event.registerEntityRenderer(ModEntities.BULLFROG.get(), SimpleMobRenderer.provider(BullfrogModel.LAYER_LOCATION, BullfrogModel::new, "bullfrog", 0.3F));
        event.registerEntityRenderer(ModEntities.BAMBOORAT.get(), SimpleMobRenderer.provider(BambooratModel.LAYER_LOCATION, BambooratModel::new, "bamboorat", 0.3F));
// 老鼠：田里的小害兽，走路播 go、吃东西播 eat，啃食时手上捧着一粒种子
        event.registerEntityRenderer(ModEntities.MOUSE.get(), MouseRenderer::new);
// 以下生物共用 SimpleMobRenderer：固定模型 + 固定贴图，没有幼体 / 染色等特判
        event.registerEntityRenderer(ModEntities.GRASS_CARP.get(), SimpleMobRenderer.provider(GrassCarpModel.LAYER_LOCATION, GrassCarpModel::new, "grass_carp", 0.3F));
        event.registerEntityRenderer(ModEntities.HARVESTER.get(), SimpleMobRenderer.provider(HarvesterModel.LAYER_LOCATION, HarvesterModel::new, "harvester", 1.5F));
        event.registerEntityRenderer(ModEntities.TENCH.get(), SimpleMobRenderer.provider(TenchModel.LAYER_LOCATION, TenchModel::new, "tench", 0.3F));
        event.registerEntityRenderer(ModEntities.MANDARIN_FISH.get(), SimpleMobRenderer.provider(MandarinFishModel.LAYER_LOCATION, MandarinFishModel::new, "mandarin_fish", 0.3F));
        event.registerEntityRenderer(ModEntities.LOACH.get(), SimpleMobRenderer.provider(LoachModel.LAYER_LOCATION, LoachModel::new, "loach", 0.3F));
        event.registerEntityRenderer(ModEntities.PLECO.get(), SimpleMobRenderer.provider(PlecoModel.LAYER_LOCATION, PlecoModel::new, "pleco", 0.3F));
        event.registerEntityRenderer(ModEntities.GOBY.get(), SimpleMobRenderer.provider(GobyModel.LAYER_LOCATION, GobyModel::new, "goby", 0.3F));
        event.registerEntityRenderer(ModEntities.SILVER_CARP.get(), SimpleMobRenderer.provider(SilverCarpModel.LAYER_LOCATION, SilverCarpModel::new, "silver_carp", 0.3F));
        event.registerEntityRenderer(ModEntities.TILAPIA.get(), SimpleMobRenderer.provider(TilapiaModel.LAYER_LOCATION, TilapiaModel::new, "tilapia", 0.3F));
        event.registerEntityRenderer(ModEntities.REDFIN.get(), SimpleMobRenderer.provider(RedfinModel.LAYER_LOCATION, RedfinModel::new, "redfin", 0.3F));
        event.registerEntityRenderer(ModEntities.BLACK_CARP.get(), SimpleMobRenderer.provider(BlackCarpModel.LAYER_LOCATION, BlackCarpModel::new, "black_carp", 0.3F));
        event.registerEntityRenderer(ModEntities.RECTUM.get(), SimpleMobRenderer.provider(RectumModel.LAYER_LOCATION, RectumModel::new, "rectum", 0.3F));
        event.registerEntityRenderer(ModEntities.BASS.get(), SimpleMobRenderer.provider(BassModel.LAYER_LOCATION, BassModel::new, "bass", 0.3F));
        event.registerEntityRenderer(ModEntities.CHINESE_CARP.get(), SimpleMobRenderer.provider(ChineseCarpModel.LAYER_LOCATION, ChineseCarpModel::new, "chinese_carp", 0.3F));
        event.registerEntityRenderer(ModEntities.CRUCIAN_CARP.get(), SimpleMobRenderer.provider(CrucianCarpModel.LAYER_LOCATION, CrucianCarpModel::new, "crucian_carp", 0.3F));
        event.registerEntityRenderer(ModEntities.CATFISH.get(), SimpleMobRenderer.provider(CatfishModel.LAYER_LOCATION, CatfishModel::new, "catfish", 0.3F));
        event.registerEntityRenderer(ModEntities.BIGHEAD_CARP.get(), SimpleMobRenderer.provider(BigheadCarpModel.LAYER_LOCATION, BigheadCarpModel::new, "bighead_carp", 0.3F));
        event.registerEntityRenderer(ModEntities.BLACK_FISH.get(), SimpleMobRenderer.provider(BlackFishModel.LAYER_LOCATION, BlackFishModel::new, "black_fish", 0.3F));
        event.registerEntityRenderer(ModEntities.KING_CRAB.get(), SimpleMobRenderer.provider(KingCrabModel.LAYER_LOCATION, KingCrabModel::new, "king_crab", 0.3F));
        event.registerEntityRenderer(ModEntities.SWORDFISH.get(), SimpleMobRenderer.provider(SwordfishModel.LAYER_LOCATION, SwordfishModel::new, "swordfish", 0.3F));
        event.registerEntityRenderer(ModEntities.HAIRTAIL.get(), SimpleMobRenderer.provider(HairtailModel.LAYER_LOCATION, HairtailModel::new, "hairtail", 0.3F));
        event.registerEntityRenderer(ModEntities.TUNA.get(), SimpleMobRenderer.provider(TunaModel.LAYER_LOCATION, TunaModel::new, "tuna", 0.3F));
        event.registerEntityRenderer(ModEntities.BROWN_CRAB.get(), SimpleMobRenderer.provider(BrownCrabModel.LAYER_LOCATION, BrownCrabModel::new, "brown_crab", 0.3F));
        event.registerEntityRenderer(ModEntities.ANGLERFISH.get(), SimpleMobRenderer.provider(AnglerfishModel.LAYER_LOCATION, AnglerfishModel::new, "anglerfish", 0.3F));
        event.registerEntityRenderer(ModEntities.CHINESE_MACKEREL.get(), SimpleMobRenderer.provider(ChineseMackerelModel.LAYER_LOCATION, ChineseMackerelModel::new, "chinese_mackerel", 0.3F));
        event.registerEntityRenderer(ModEntities.EEL.get(), SimpleMobRenderer.provider(EelModel.LAYER_LOCATION, EelModel::new, "eel", 0.3F));
        event.registerEntityRenderer(ModEntities.LOBSTER.get(), SimpleMobRenderer.provider(LobsterModel.LAYER_LOCATION, LobsterModel::new, "lobster", 0.3F));
        event.registerEntityRenderer(ModEntities.TURTLE.get(), SimpleMobRenderer.provider(TurtleModel.LAYER_LOCATION, TurtleModel::new, "turtle", 0.3F));
        event.registerEntityRenderer(ModEntities.HAIRY_CRAB.get(), SimpleMobRenderer.provider(HairyCrabModel.LAYER_LOCATION, HairyCrabModel::new, "hairy_crab", 0.3F));
        event.registerEntityRenderer(ModEntities.PRAWN.get(), SimpleMobRenderer.provider(PrawnModel.LAYER_LOCATION, PrawnModel::new, "prawn", 0.3F));
        event.registerEntityRenderer(ModEntities.SWIMMING_CRAB.get(), SimpleMobRenderer.provider(SwimmingCrabModel.LAYER_LOCATION, SwimmingCrabModel::new, "swimming_crab", 0.3F));
        event.registerEntityRenderer(ModEntities.MUD_CRAB.get(), SimpleMobRenderer.provider(MudCrabModel.LAYER_LOCATION, MudCrabModel::new, "mud_crab", 0.3F));
        event.registerEntityRenderer(ModEntities.SARDINE.get(), SimpleMobRenderer.provider(SardineModel.LAYER_LOCATION, SardineModel::new, "sardine", 0.3F));
        event.registerEntityRenderer(ModEntities.GROUPER.get(), SimpleMobRenderer.provider(GrouperModel.LAYER_LOCATION, GrouperModel::new, "grouper", 0.3F));
        event.registerEntityRenderer(ModEntities.PACIFIC_SAURY.get(), SimpleMobRenderer.provider(PacificSauryModel.LAYER_LOCATION, PacificSauryModel::new, "pacific_saury", 0.3F));
        event.registerEntityRenderer(ModEntities.SILVERY_POMFRET.get(), SimpleMobRenderer.provider(SilveryPomfretModel.LAYER_LOCATION, SilveryPomfretModel::new, "silvery_pomfret", 0.3F));
        event.registerEntityRenderer(ModEntities.JAPANESE_HALFBEAK.get(), SimpleMobRenderer.provider(JapaneseHalfbeakModel.LAYER_LOCATION, JapaneseHalfbeakModel::new, "japanese_halfbeak", 0.3F));
        event.registerEntityRenderer(ModEntities.POOP.get(), PoopRenderer::new);
// 纸箱：无人机投放的箱子，用 Blockbench 导出的模型渲染
        event.registerEntityRenderer(ModEntities.CARTON.get(), CartonRenderer::new);
// 外卖袋：出发点路边放着的袋子，用 Blockbench 导出的模型渲染
        event.registerEntityRenderer(ModEntities.TAKEAWAY_BAG.get(), TakeawayBagRenderer::new);
// 配送点：目的地站着不动的收件人，借原版村民模型 + 默认皮肤渲染
        event.registerEntityRenderer(ModEntities.DELIVERY_POINT.get(), DeliveryPointRenderer::new);
// 外卖小哥：小票打印机出单时刷出的被动生物，用 Blockbench 导出的模型渲染
        event.registerEntityRenderer(ModEntities.DELIVERY_RIDER.get(), SimpleMobRenderer.provider(DeliveryRiderModel.LAYER_LOCATION, DeliveryRiderModel::new, "delivery_rider", 0.5F));
// 顾客：营业执照招来的堂食客人，用原版玩家模型 + 默认皮肤渲染
        event.registerEntityRenderer(ModEntities.CUSTOMER.get(), CustomerRenderer::new);
// 被扔出去的粪便：纯弹射物，直接用物品贴图的样子飞
        event.registerEntityRenderer(ModEntities.THROWN_POOP.get(), ThrownItemRenderer::new);
// 被扔出去的加压沼气：同样是纯弹射物，用加压沼气的物品贴图飞
        event.registerEntityRenderer(ModEntities.THROWN_MARSH_GAS.get(), ThrownItemRenderer::new);
// 被扔出去的禽蛋：纯弹射物，用对应蛋的物品贴图飞（机制同原版鸡蛋）
        event.registerEntityRenderer(ModEntities.THROWN_POULTRY_EGG.get(), ThrownItemRenderer::new);
// 稻草人：插在田里的静物，用 Blockbench 导出的模型渲染
        event.registerEntityRenderer(ModEntities.SCARECROW.get(), ScarecrowRenderer::new);
// 毒饵站：立在稻草人位置的小静物，罩住周围不让老鼠生成
        event.registerEntityRenderer(ModEntities.POISON_BAIT_STATION.get(), PoisonBaitStationRenderer::new);
// 拉力木车：右键挂在玩家身后跟着走的木车，模型带 free / free2 两套动画
        event.registerEntityRenderer(ModEntities.HANDCART.get(), HandcartRenderer::new);
// 充气钓鱼浮台：水面上的 5×5 平台，模型带 swim 起伏动画
        event.registerEntityRenderer(ModEntities.FISHING_FLOATING_PLATFORM.get(), FishingFloatingPlatformRenderer::new);
// 坠落的果子：直接把悬挂果实的方块模型画出来，落地后带随机翻滚角度
        event.registerEntityRenderer(ModEntities.FALLING_FRUIT.get(), FallingFruitEntityRenderer::new);
// 多功能无人机：通电悬停 / 飞行时播 go 动画，断电没有动作
        event.registerEntityRenderer(ModEntities.UAV.get(), UavRenderer::new);

// 钓鱼浮漂：覆盖原版鱼钩渲染器，改用钓竿浮漂槽位里的浮漂贴图（未装浮漂时仍用原版）
        event.registerEntityRenderer(EntityType.FISHING_BOBBER, ModFishingHookRenderer::new);

// 生剑鱼：模型超出单方块范围，由方块实体渲染器绘制
        event.registerBlockEntityRenderer(ModBlockEntities.RAW_SWORDFISH_ENTITY.get(), RawSwordfishRenderer::new);
// 灌溉水车：约 3.8 格高，同样走方块实体渲染器，轮子按游戏时间匀速转
        event.registerBlockEntityRenderer(ModBlockEntities.WATERWHEEL_ENTITY.get(), WaterwheelRenderer::new);
// 旋转水管：旋转臂播放 round 动画，整体由方块实体渲染器绘制
        event.registerBlockEntityRenderer(ModBlockEntities.ROTATING_WATER_PIPE_ENTITY.get(), RotatingWaterPipeRenderer::new);
// 气象仪器：放置后风车盘按游戏时间转，出现非常规天气时切换成 heavy 动画
        event.registerBlockEntityRenderer(ModBlockEntities.WEATHER_INSTRUMENT_ENTITY.get(), WeatherInstrumentRenderer::new);
// 竿架：把架上的南山钓竿画出来
        event.registerBlockEntityRenderer(ModBlockEntities.ROD_POD_ENTITY.get(), RodPodRenderer::new);
// 钓鱼展示架：把挂着的那条水产的活体实体模型画出来
        event.registerBlockEntityRenderer(ModBlockEntities.FISH_DISPLAY_BOARD_ENTITY.get(), FishDisplayBoardRenderer::new);
// 缝纫机：把台面上正在加工的空白丝绸装备平铺在台面板上
        event.registerBlockEntityRenderer(ModBlockEntities.SEWING_MACHINE_ENTITY.get(), SewingMachineRenderer::new);
// 河道淤泥：把养在淤泥里的苗种（成熟后三份产物）平铺在方块顶面
        event.registerBlockEntityRenderer(ModBlockEntities.RIVER_SILT_ENTITY.get(), RiverSiltRenderer::new);
// 铁锅炖大鹅：整锅带饼子，按 stage 换 12 个模型
        event.registerBlockEntityRenderer(ModBlockEntities.POT_STEW_GOOSE_ENTITY.get(), PotStewGooseRenderer::new);
// 全驴宴：3 格长的圆桌，桌面那圈菜按游戏时间慢慢转
        event.registerBlockEntityRenderer(ModBlockEntities.DONKEY_FEAST_ENTITY.get(), DonkeyFeastRenderer::new);
// 油焖大虾：整锅带一圈虾，按 stage 换 4 个模型
        event.registerBlockEntityRenderer(ModBlockEntities.BRAISED_PRAWNS_ENTITY.get(), BraisedPrawnsRenderer::new);
    }

    // 生剑鱼 / 铁锅炖大鹅 / 全驴宴 / 油焖大虾的客户端扩展在各自的物品类里通过 initializeClient 注册，
// 1.20.1 没有统一的 RegisterClientExtensionsEvent。

// 无人机遥控器的七个按键（前后左右 / 起降 / 退出操作视角），键位常量在 UavClient 里
    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(UavClient.UAV_FORWARD);
        event.register(UavClient.UAV_BACK);
        event.register(UavClient.UAV_LEFT);
        event.register(UavClient.UAV_RIGHT);
        event.register(UavClient.UAV_UP);
        event.register(UavClient.UAV_DOWN);
        event.register(UavClient.UAV_EXIT);
    }

    @SubscribeEvent
    static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ROD_MOD_MENU.get(), FishingRodModScreen::new);
            MenuScreens.register(ModMenus.CHICKEN_COOP_MENU.get(), ChickenCoopScreen::new);
            MenuScreens.register(ModMenus.INSECT_INCUBATOR_MENU.get(), InsectIncubatorScreen::new);
            MenuScreens.register(ModMenus.UAV_MENU.get(), UavScreen::new);
            MenuScreens.register(ModMenus.FISHING_BOX_MENU.get(), FishingBoxScreen::new);
            MenuScreens.register(ModMenus.STEELYARD_MENU.get(), SteelyardScreen::new);
            MenuScreens.register(ModMenus.SEWING_MACHINE_MENU.get(), SewingMachineScreen::new);
            MenuScreens.register(ModMenus.ATM_MENU.get(), AtmScreen::new);
            MenuScreens.register(ModMenus.RECEIPT_PRINTER_MENU.get(), ReceiptPrinterScreen::new);
            MenuScreens.register(ModMenus.MARKET_MENU.get(), MarketScreen::new);
            MenuScreens.register(ModMenus.PURCHASING_BOX_MENU.get(), PurchasingBoxScreen::new);
        });
    }

// 丝绸装备 tooltip 里的 3D 模型预览：把通用的 SilkArmorPreview 交给客户端渲染实现
    @SubscribeEvent
    static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(SilkArmorPreview.class, ClientSilkArmorPreview::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
// 南山钓竿：注册原版 cast 谓词，抛竿后（存在活跃鱼钩时）自动切换到抛竿纹理模型
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.MOD_FISHING_ROD.get(),
                new ResourceLocation("minecraft", "cast"),
                (stack, clientLevel, entity, seed) ->
                        FishingRodModelOverrides.isCasting(stack, entity) ? 1.0F : 0.0F
        ));
// 竿体的 body 谓词：装了竿体就把原版钓竿贴图换成竿体贴图，取值 1~6 对应 ModItems.ROD_BODIES 的顺序。
// 线轮 / 鱼线 / 浮漂 / 铅坠不用谓词，改由 FishingRodModelOverrides 按槽位动态叠渲染层。
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.MOD_FISHING_ROD.get(),
                new ResourceLocation(flavor_immersed_daily_argo.MODID, "body"),
                (stack, clientLevel, entity, seed) ->
                        FishingRodModelOverrides.partVariant(stack, FishingRodStats.SLOT_BODY, ModItems.ROD_BODIES)
        ));
// 编制麻袋的 filled 谓词：袋子里装了生物就换成 full 贴图
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.POULTRY_CATCHING_NET.get(),
                new ResourceLocation(flavor_immersed_daily_argo.MODID, "filled"),
                (stack, clientLevel, entity, seed) -> PoultryCatchingNetItem.isFull(stack) ? 1.0F : 0.0F
        ));
// 针管的 dna 谓词：封了遗传液就换成「筒装生物遗传液」的贴图
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.SYRINGE.get(),
                new ResourceLocation(flavor_immersed_daily_argo.MODID, "dna"),
                (stack, clientLevel, entity, seed) ->
                        ModDataComponents.SYRINGE_DNA.get().has(stack) ? 1.0F : 0.0F
        ));
// 导航手机的 open 谓词：潜行右键展开后换成展开状态的贴图
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.NAV_PHONE.get(),
                new ResourceLocation(flavor_immersed_daily_argo.MODID, "open"),
                (stack, clientLevel, entity, seed) -> NavPhoneItem.isOpen(stack) ? 1.0F : 0.0F
        ));
// 草帽的 worn 谓词：戴在头上时换成 3D 头戴模型，否则用平面手持图。
// 头部渲染走 CustomHeadLayer，它把头部槽位的那个 ItemStack 实例原样交给渲染器，
// 所以这里用引用相等就能判断「当前渲染的正是戴在头上的那顶」——手持另一顶时不会误判。
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.STRAW_HAT.get(),
                new ResourceLocation(flavor_immersed_daily_argo.MODID, "worn"),
                (stack, clientLevel, entity, seed) ->
                        entity != null
                                && entity.getItemBySlot(EquipmentSlot.HEAD) == stack ? 1.0F : 0.0F
        ));
// 生鲜方块用的是带透明通道的 3D 模型，必须走 cutout 渲染层，
// 否则透明区域会被渲染成黑色实心块；物品（含 GUI / 手持）会跟随方块使用同一层。
        RenderType cutout = RenderType.cutout();
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAW_CHINESE_MACKEREL.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAW_HAIRTAIL.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAW_TUNA.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAW_EEL.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAW_ANGLERFISH.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAW_KING_CRAB.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAW_LOBSTER.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAW_BROWN_CRAB.get(), cutout);
// 竿架 / 鱼护 / 钓鱼箱 / 钓鱼展示架同属带透明通道的模型，同样要走 cutout
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ROD_POD.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FISH_PROTECTION.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FISHING_BOX.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FISH_DISPLAY_BOARD.get(), cutout);
// 缝纫机同样是带透明通道的模型，走 cutout
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SEWING_MACHINE.get(), cutout);
// 昆虫培育箱的模型带透明区域：不登记 cutout 时，贴着的相邻方块面会被当成半透明遮住
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INSECT_INCUBATOR.get(), cutout);
// 蒸龙虾 / 蒸帝王蟹 / 蒸面包蟹 / 春秋剑鱼的贴图大片留空（占 78%~84%），
// 不登记 cutout 时透明像素会被画成黑色实心块；物品也跟随方块用同一层
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.STEAMED_LOBSTER.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.STEAMED_KING_CRAB.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.STEAMED_BROWN_CRAB.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.STINKY_SWORDFISH.get(), cutout);
// 荠菜作物是贴图留空的十字叶片模型，同样走 cutout
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHEPHERDS_PURSE.get(), cutout);
    }
}
