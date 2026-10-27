package com.flavor_immersed_daily_argo;

import com.flavor_immersed_daily_argo.all.ModAttachments;
import com.flavor_immersed_daily_argo.all.ModArmorMaterials;
import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModBlocks;
import com.flavor_immersed_daily_argo.all.ModCreativeTabs;
import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModFluidTypes;
import com.flavor_immersed_daily_argo.all.ModFluids;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.all.ModMenus;
import com.flavor_immersed_daily_argo.all.ModMobEffects;
import com.flavor_immersed_daily_argo.all.ModSounds;
import com.flavor_immersed_daily_argo.config.Config;
import com.flavor_immersed_daily_argo.datagen.FIDDataGenerators;
import com.flavor_immersed_daily_argo.entity.Harvester;
import com.flavor_immersed_daily_argo.recipe.ModRecipes;
import com.flavor_immersed_daily_argo.entity.Scarecrow;
import com.flavor_immersed_daily_argo.entity.Swordfish;
import com.flavor_immersed_daily_argo.entity.Uav;
import com.flavor_immersed_daily_argo.fishing.FishingCaughtRecords;
import com.flavor_immersed_daily_argo.fishing.FishingCaughtSyncPacket;
import com.flavor_immersed_daily_argo.fishing.FishingDataSyncPacket;
import com.flavor_immersed_daily_argo.fishing.FishingFightClient;
import com.flavor_immersed_daily_argo.economy.AtmMenu;
import com.flavor_immersed_daily_argo.economy.AtmExchangePacket;
import com.flavor_immersed_daily_argo.economy.AtmSetOwnerPacket;
import com.flavor_immersed_daily_argo.economy.DeliveryManager;
import com.flavor_immersed_daily_argo.economy.EconomyConfig;
import com.flavor_immersed_daily_argo.economy.MarketMenu;
import com.flavor_immersed_daily_argo.economy.NavPhoneClientData;
import com.flavor_immersed_daily_argo.economy.PhoneAcceptOrderPacket;
import com.flavor_immersed_daily_argo.economy.PhoneCancelOrderPacket;
import com.flavor_immersed_daily_argo.economy.PhoneOrderRequestPacket;
import com.flavor_immersed_daily_argo.economy.PhoneOrderSyncPacket;
import com.flavor_immersed_daily_argo.economy.shop.ShopCheckoutPacket;
import com.flavor_immersed_daily_argo.economy.shop.ShopCheckoutResultPacket;
import com.flavor_immersed_daily_argo.economy.shop.ShopClientData;
import com.flavor_immersed_daily_argo.economy.shop.ShopManagePacket;
import com.flavor_immersed_daily_argo.economy.shop.ShopManager;
import com.flavor_immersed_daily_argo.economy.shop.ShopSyncPacket;
import com.flavor_immersed_daily_argo.fishing.FishingFightInputPacket;
import com.flavor_immersed_daily_argo.fishing.FishingFightSyncPacket;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookData;
import com.flavor_immersed_daily_argo.fishing.FishingRodModMenu;
import com.flavor_immersed_daily_argo.fishing.FishingRodModSavePacket;
import com.flavor_immersed_daily_argo.fishing.FishingRodPartData;
import com.flavor_immersed_daily_argo.fishing.ModFishingHook;
import com.flavor_immersed_daily_argo.fishing.OpenRodModPacket;
import com.flavor_immersed_daily_argo.fishing.WeighingTypeData;
import com.flavor_immersed_daily_argo.forestry.ForestryClientData;
import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import com.flavor_immersed_daily_argo.forestry.ForestryRemovePacket;
import com.flavor_immersed_daily_argo.forestry.ForestryRenamePacket;
import com.flavor_immersed_daily_argo.forestry.ForestrySyncPacket;
import com.flavor_immersed_daily_argo.herding.AnimalRenamePacket;
import com.flavor_immersed_daily_argo.herding.AnimalStatsManager;
import com.flavor_immersed_daily_argo.herding.PoopSplashHud;
import com.flavor_immersed_daily_argo.herding.PoopSplashPacket;
import com.flavor_immersed_daily_argo.herding.PurchasingBoxMenu;
import com.flavor_immersed_daily_argo.herding.PurchasingBoxSellPacket;
import com.flavor_immersed_daily_argo.item.UavControllerItem;
import com.flavor_immersed_daily_argo.textile.SewingMachineEditPacket;
import com.flavor_immersed_daily_argo.textile.SewingMachineMenu;
import com.flavor_immersed_daily_argo.textile.SewingMachineSyncPacket;
import com.flavor_immersed_daily_argo.uav.TaczGunCompat;
import com.flavor_immersed_daily_argo.uav.TaczGunSoundClient;
import com.flavor_immersed_daily_argo.uav.UavActionPacket;
import com.flavor_immersed_daily_argo.uav.UavFlightInputPacket;
import com.flavor_immersed_daily_argo.uav.UavGunSoundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(flavor_immersed_daily_argo.MODID)
public class flavor_immersed_daily_argo {

    public static final String MODID = "flavor_immersed_daily_argo";

    public flavor_immersed_daily_argo(IEventBus modEventBus, ModContainer modContainer) {
// 各注册表
        ModEntities.register(modEventBus);
        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModAttachments.register(modEventBus);

// 事件监听
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerPackets);
// 未安装帕秋莉手册时，登录时提醒玩家（「南山-教程卷轴」需要它才能翻阅）
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);

// 数据生成（gradlew runData）
        modEventBus.addListener(FIDDataGenerators::gatherData);

// 配置
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, EconomyConfig.SPEC,
                "flavor_immersed_daily_argo-economy.toml");
    }

// 登录时：未装帕秋莉则提醒；并按配置给每个玩家发一次「南山-教程卷轴」
    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!ModList.get().isLoaded("patchouli")) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message." + MODID + ".need_patchouli"), false);
        }
// 果树的三份树形借的是前置模组的木头，没装前置时树苗只能长成橡树，登录时说一声
        if (Config.WARN_MISSING_PARENT.get() && !ModList.get().isLoaded("flavor_immersed_daily")) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message." + MODID + ".need_parent"), false);
        }
// 开局赠书：走成就「南山居 → 初到南山」发放。
// 成就进度由原版存档持久化，反复进出存档不会再重复发书
        if (Config.GIVE_GUIDE_BOOK_ON_FIRST_JOIN.get()) {
            awardGuideBook(serverPlayer);
        }
    }

// 完成开局赠书成就（物品由成就的奖励函数 give_guide_book 发放）；已完成则跳过
    private static void awardGuideBook(ServerPlayer player) {
        AdvancementHolder advancement = player.server.getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(MODID, "guide_book"));
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            return;
        }
        player.getAdvancements().award(advancement, "given");
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DUCK.get(), Chicken.createAttributes().build());
        event.put(ModEntities.GOOSE.get(), Chicken.createAttributes().build());
        event.put(ModEntities.QUAIL.get(), Chicken.createAttributes().build());
        event.put(ModEntities.PIGEON.get(), Chicken.createAttributes().build());
        event.put(ModEntities.TURKEY.get(), Chicken.createAttributes().build());
        event.put(ModEntities.BULLFROG.get(), Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .build());
        event.put(ModEntities.BAMBOORAT.get(), Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .build());
// 老鼠：跑得比玩家(0.1)快，速度与猫(0.3)相当，血少
        event.put(ModEntities.MOUSE.get(), Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .build());
        event.put(ModEntities.GRASS_CARP.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.TENCH.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.MANDARIN_FISH.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.LOACH.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.PLECO.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.GOBY.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.SILVER_CARP.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.TILAPIA.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.REDFIN.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.BLACK_CARP.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.RECTUM.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.BASS.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.CHINESE_CARP.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.CRUCIAN_CARP.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.CATFISH.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.BIGHEAD_CARP.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.BLACK_FISH.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.KING_CRAB.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.SWORDFISH.get(), Swordfish.createAttributes().build());
        event.put(ModEntities.HAIRTAIL.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.TUNA.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.BROWN_CRAB.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.ANGLERFISH.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.CHINESE_MACKEREL.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.EEL.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.LOBSTER.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.TURTLE.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.HAIRY_CRAB.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.PRAWN.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.SWIMMING_CRAB.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.MUD_CRAB.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.SARDINE.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.GROUPER.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.PACIFIC_SAURY.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.SILVERY_POMFRET.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.JAPANESE_HALFBEAK.get(), AbstractFish.createAttributes().build());
        event.put(ModEntities.HARVESTER.get(), Harvester.createAttributes());
// 外卖小哥：普通被动生物属性，会在打印机附近走动等待交付
        event.put(ModEntities.DELIVERY_RIDER.get(), Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .build());
// 顾客：与外卖小哥同样的被动生物属性
        event.put(ModEntities.CUSTOMER.get(), Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .build());
    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OpenRodModPacket.TYPE, OpenRodModPacket.STREAM_CODEC,
                (packet, context) -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    int slot = player.getInventory().selected;
                    player.openMenu(new SimpleMenuProvider(
                            (id, inv, p) -> new FishingRodModMenu(id, inv, slot),
                            Component.translatable("gui.flavor_immersed_daily_argo.rod_mod")
                    ), buf -> buf.writeVarInt(slot));
                });
// 改装页面：保存配件与名称（必须由服务端写入钓竿，否则配件会丢失）
        registrar.playToServer(FishingRodModSavePacket.TYPE, FishingRodModSavePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null && player.containerMenu instanceof FishingRodModMenu menu) {
                        menu.saveAll(packet.customName());
                    }
                }));
// 自助取款机：改银行卡持卡主人（必须由服务端核对主人身份后写回物品组件）
        registrar.playToServer(AtmSetOwnerPacket.TYPE, AtmSetOwnerPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null && player.containerMenu instanceof AtmMenu menu) {
                        menu.changeOwner(player, packet.owner());
                    }
                }));
// 自助取款机：外汇兑换（服务端核对主人 / 余额后扣账户、发物品）
        registrar.playToServer(AtmExchangePacket.TYPE, AtmExchangePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null && player.containerMenu instanceof AtmMenu menu) {
                        menu.exchange(player, packet.index());
                    }
                }));
// 遛鱼阶段：客户端上报本 tick 是否操作正确
        registrar.playToServer(FishingFightInputPacket.TYPE, FishingFightInputPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null
                            && player.level().getEntity(packet.hookId()) instanceof ModFishingHook hook
                            && hook.getPlayerOwner() == player) {
                        hook.acceptInput(packet.correct());
                    }
                }));
// 遛鱼阶段：服务端同步鱼当前的发力方向 / 阶段结束
        registrar.playToClient(FishingFightSyncPacket.TYPE, FishingFightSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> FishingFightClient.handleSync(packet)));
// 钓鱼手册：服务端同步本玩家已钓获的条目集合
        registrar.playToClient(FishingCaughtSyncPacket.TYPE, FishingCaughtSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> FishingCaughtRecords.applyClient(packet.caught())));
// 数据包内容（手册条目 / 配件参数 / 称重分类）：客户端读不到 data 目录，登录 / 重载时由服务端全量下发
        registrar.playToClient(FishingDataSyncPacket.TYPE, FishingDataSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    FishingHandbookData.getInstance().applyFromJson(packet.handbook());
                    FishingRodPartData.getInstance().applyFromJson(packet.parts());
                    WeighingTypeData.getInstance().applyFromJson(packet.weighing());
                }));
// 缝纫机：画幅落笔（服务端结算，方块工具要扣丝绸）
        registrar.playToServer(SewingMachineEditPacket.TYPE, SewingMachineEditPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null && player.containerMenu instanceof SewingMachineMenu menu) {
                        menu.blockEntity().applyEdit(packet.mode(), packet.cube(), packet.face(),
                                packet.x1(), packet.y1(), packet.z1(),
                                packet.x2(), packet.y2(), packet.z2(), packet.color(), packet.beginStroke());
                    }
                }));
// 缝纫机：台面装备与花纹同步给客户端
        registrar.playToClient(SewingMachineSyncPacket.TYPE, SewingMachineSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    if (context.player().containerMenu instanceof SewingMachineMenu menu) {
                        menu.applySync(packet.garment());
                    }
                }));
// 粪便：扔出去砸中玩家后，由服务端通知客户端糊一屏 shitgui
        registrar.playToClient(PoopSplashPacket.TYPE, PoopSplashPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> PoopSplashHud.show(packet.ticks())));
// 无人机开火：TaCZ 的枪声是客户端按「开枪的实体」去找人再播的，替身玩家客户端上不存在，
// 所以这边换成无人机的身份重发一份，由客户端自己去取这杆枪的声音资源来放
        registrar.playToClient(UavGunSoundPacket.TYPE, UavGunSoundPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    if (TaczGunCompat.isLoaded()) {
                        TaczGunSoundClient.play(packet);
                    }
                }));
// 家养动物 / 稻草人：客户端在取名界面确认后，由服务端写入实体的自定义名称
        registrar.playToServer(AnimalRenamePacket.TYPE, AnimalRenamePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    Entity target = player == null ? null : player.level().getEntity(packet.entityId());
                    if (target == null || player.distanceToSqr(target) > 64.0D) {
                        return;
                    }
                    boolean scarecrow = target instanceof Scarecrow;
                    if (!scarecrow && !(target instanceof LivingEntity living && AnimalStatsManager.isTracked(living))) {
                        return;
                    }
                    String name = packet.name().trim();
                    target.setCustomName(name.isEmpty() ? null : Component.literal(name));
// 动物的名字显示在状态栏面板上，不挂头顶；稻草人没有面板，名字就挂在头顶
                    target.setCustomNameVisible(scarecrow);
                }));
// 无人机遥控器：解绑 / 开关电源 / 设定自动航线 / 起降操控。只有把这架无人机绑给自己的人能操作
        registrar.playToServer(UavActionPacket.TYPE, UavActionPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
// 遥控器是远程操作：不限制玩家与无人机的距离（无人机飞自动航线会跑很远），
// 只认「这架无人机绑的是不是他」以及实体本身还在不在
                    if (player == null
                            || !(player.level().getEntity(packet.entityId()) instanceof Uav uav)
                            || !uav.isBoundTo(player)) {
                        return;
                    }
                    switch (packet.action()) {
                        case UavActionPacket.UNBIND -> {
// 无人机那头松绑的同时，也把遥控器物品上的绑定信息抹掉，两边保持一致
                            UavControllerItem.unbindAll(player, uav.getUUID());
                            uav.setBoundPlayer(null);
                        }
                        case UavActionPacket.TOGGLE_POWER -> uav.setPowered(!uav.isPowered());
                        case UavActionPacket.OPERATE -> uav.startOperate(player);
                        case UavActionPacket.STOP_OPERATE -> uav.stopOperate();
// 物品栏：把实体 id 一并写进附加数据，客户端才找得回这架无人机
                        case UavActionPacket.OPEN_INVENTORY ->
                                player.openMenu(uav, buffer -> buffer.writeVarInt(uav.getId()));
// 附身战斗：左键发射弹射物，右键先试着食用使用槽的物品
                        case UavActionPacket.COMBAT_FIRE -> uav.combatFire(player);
                        case UavActionPacket.COMBAT_USE -> uav.combatUse(player);
// 附身高亮标记：把右键的那格记下来高亮
                        case UavActionPacket.MARK_BLOCK -> uav.markBlock(player,
                                BlockPos.containing(packet.x1(), packet.y1(), packet.z1()));
                        default -> {
                        }
                    }
                }));
// 第一人称操作：操作者每 tick 上报一次飞行输入，服务端照着把无人机推着走
        registrar.playToServer(UavFlightInputPacket.TYPE, UavFlightInputPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null
                            && player.level().getEntity(packet.entityId()) instanceof Uav uav
                            && uav.isOperatedBy(player)) {
                        uav.setFlightInput(packet.keys(), packet.yaw(), packet.pitch());
                    }
                }));
// 林业网点：服务端在本玩家网点表变动 / 登录时全量下发
        registrar.playToClient(ForestrySyncPacket.TYPE, ForestrySyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> ForestryClientData.apply(packet.points())));
// 林业网点：客户端在护林员写字板界面里点击「解除网点」
        registrar.playToServer(ForestryRemovePacket.TYPE, ForestryRemovePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null) {
                        ForestryNetworkManager.remove(player, packet.dimension(), packet.pos());
                    }
                }));
// 林业网点：客户端在护林员写字板界面里给网点改名
        registrar.playToServer(ForestryRenamePacket.TYPE, ForestryRenamePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null) {
                        ForestryNetworkManager.rename(player, packet.dimension(), packet.pos(), packet.name());
                    }
                }));
// 导航手机：客户端在展开状态下右键请求接单界面；服务端刷新一批订单后发回（open=true：要弹界面）
        registrar.playToServer(PhoneOrderRequestPacket.TYPE, PhoneOrderRequestPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null) {
                        PacketDistributor.sendToPlayer(player,
                                new PhoneOrderSyncPacket(DeliveryManager.refresh(player), true));
                    }
                }));
// 导航手机：客户端收到订单后更新缓存，需要时弹出接单界面
        registrar.playToClient(PhoneOrderSyncPacket.TYPE, PhoneOrderSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(
                        () -> NavPhoneClientData.apply(packet.orders(), packet.open())));
// 导航手机：客户端在接单界面点「接单」，服务端接单后回发最新列表（open=true：刷新界面）
        registrar.playToServer(PhoneAcceptOrderPacket.TYPE, PhoneAcceptOrderPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null) {
                        PacketDistributor.sendToPlayer(player,
                                new PhoneOrderSyncPacket(DeliveryManager.accept(player, packet.orderId()), true));
                    }
                }));
// 导航手机：客户端在接单界面点「取消」，服务端作废该单后回发最新列表（open=true：刷新界面）
        registrar.playToServer(PhoneCancelOrderPacket.TYPE, PhoneCancelOrderPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null) {
                        PacketDistributor.sendToPlayer(player,
                                new PhoneOrderSyncPacket(DeliveryManager.cancel(player, packet.orderId()), true));
                    }
                }));
// 商业电脑：服务端全量下发店铺列表与今日经济产物新闻（任一玩家开店 / 结账后广播，客户端刷新）
        registrar.playToClient(ShopSyncPacket.TYPE, ShopSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ShopClientData.setShops(packet.shops());
                    ShopClientData.setNews(packet.news());
                }));
// 商业电脑：结账 / 开店动作的服务端回执，客户端画在窗口里（界面打开时 actionBar 看不见）
        registrar.playToClient(ShopCheckoutResultPacket.TYPE, ShopCheckoutResultPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(
                        () -> ShopClientData.applyResult(packet.success(), packet.message())));
// 商业电脑：客户端提交购物车结账（服务端核对选中的银行卡 / 余额 / 库存后扣钱发货）
        registrar.playToServer(ShopCheckoutPacket.TYPE, ShopCheckoutPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null && player.containerMenu instanceof MarketMenu) {
                        ShopManager.checkout(player, packet.cart(), packet.cardSlot());
                    }
                }));
// 商业电脑：开店页动作（建店 / 删店 / 上架 / 下架 / 改价 / 补货 / 提现）
        registrar.playToServer(ShopManagePacket.TYPE, ShopManagePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player == null || !(player.containerMenu instanceof MarketMenu)) {
                        return;
                    }
                    ShopManager manager = ShopManager.get(player.server);
                    switch (packet.action()) {
                        case CREATE -> {
                            if (!EconomyConfig.PLAYER_SHOPS_ENABLED.get()) {
                                shopMessage(player, "disabled");
                            } else if (manager.createShop(player, packet.text())) {
                                shopMessage(player, "created");
                            } else {
                                shopMessage(player, "limit_reached");
                            }
                        }
                        case DELETE -> shopMessage(player,
                                manager.deleteShop(player, packet.shopId()) ? "deleted" : "not_owner");
                        case ADD_ENTRY -> {
                            ItemStack goods = takeMainHand(player);
                            if (goods.isEmpty()) {
                                shopMessage(player, "no_goods");
                            } else {
                                shopMessage(player, manager.addEntry(player, packet.shopId(), goods, packet.price())
                                        ? "listed" : "not_owner");
                            }
                        }
                        case REMOVE_ENTRY -> shopMessage(player,
                                manager.removeEntry(player, packet.shopId(), packet.index()) ? "delisted" : "not_owner");
                        case SET_PRICE -> shopMessage(player,
                                manager.setPrice(player, packet.shopId(), packet.index(), packet.price())
                                        ? "repriced" : "not_owner");
                        case RESTOCK -> {
                            ItemStack goods = takeMainHand(player);
                            if (goods.isEmpty()) {
                                shopMessage(player, "no_goods");
                            } else {
                                shopMessage(player, manager.restock(player, packet.shopId(), packet.index(), goods)
                                        ? "restocked" : "not_owner");
                            }
                        }
                        case WITHDRAW -> {
// index 在这里是玩家在背包里选中的银行卡槽位
                            int slot = packet.index();
                            ItemStack card = slot < 0 || slot > 40
                                    ? ItemStack.EMPTY : player.getInventory().getItem(slot);
                            shopMessage(player,
                                    manager.withdraw(player, packet.shopId(), card) ? "withdrawn" : "no_card");
                        }
                    }
// 上架 / 补货会从主手拿走物品，背包这一份要立刻重发
                    player.inventoryMenu.broadcastFullState();
                    ShopManager.syncAll(player.server);
                }));
// 收购箱：客户端点了界面里的「出售」，服务端按今天的收购价把箱内经济产物变现成纸币
        registrar.playToServer(PurchasingBoxSellPacket.TYPE, PurchasingBoxSellPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player != null && player.containerMenu instanceof PurchasingBoxMenu menu) {
                        menu.sell(player.serverLevel().getDayTime() / 24000L);
                    }
                }));
    }

// 取走玩家主手整叠物品（上架 / 补货的货物来源），空手返回空栈
    private static ItemStack takeMainHand(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = held.copy();
        held.shrink(held.getCount());
        player.getInventory().setChanged();
        return taken;
    }

// 市场相关提示统一走 actionBar
    private static void shopMessage(ServerPlayer player, String key) {
        player.displayClientMessage(
                Component.translatable("message.flavor_immersed_daily_argo.shop." + key), true);
    }
}
