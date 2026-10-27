# 任务清单（add-catering-license-customer）

> 约定：所有 Java 代码放在包 `com.flavor_immersed_daily_argo` 下；中文注释风格照抄邻近同类文件（`ReceiptPrinterBlock` / `DeliveryRider`）。
> 每个任务完成后必须执行 `.\gradlew compileJava --offline -q`（根目录）确认可编译。

- [x] Task 1: 座位标记系统（seat_cushion + SeatManager + 破坏取消监听）
  - [x] SubTask 1.1: 新建 `src/main/java/com/flavor_immersed_daily_argo/economy/SeatManager.java`
    - `public class SeatManager extends SavedData`，常量 `DATA_NAME = "flavor_immersed_daily_argo_seats"`、tag key `"Seats"`。
    - 字段 `private final Set<BlockPos> seats = new HashSet<>()`。
    - `public static SeatManager get(ServerLevel level)`：`level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME)`；`FACTORY = new SavedData.Factory<>(SeatManager::new, (tag, provider) -> new SeatManager(tag), null)`。
    - `public boolean isSeat(BlockPos)`、`public boolean toggle(BlockPos)`（返回切换后的状态）、`public void mark/unmark(BlockPos)`、`public Set<BlockPos> seats()`。
    - 标记/取消后调用 `setDirty()`。
    - `@Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries)`：把 `seats` 写成 `LongArray`（`tag.putLongArray("Seats", pos.asLong() 数组)`）。
    - 编译若报 `setDirty()` 不存在，改用 `setDirty(true)`；若 `SavedData.Factory` 第三参为空不支持，则去掉该参。
  - [x] SubTask 1.2: 新建 `src/main/java/com/flavor_immersed_daily_argo/item/SeatCushionItem.java`
    - `public class SeatCushionItem extends TooltipItem`，构造 `(Item.Properties properties, String tooltipKey)`。
    - `@Override public InteractionResult useOn(UseOnContext context)`：客户端直接 `return InteractionResult.SUCCESS`；服务端 `SeatManager mgr = SeatManager.get((ServerLevel) context.getLevel())`，`boolean now = mgr.toggle(pos)`，按结果给 `player.displayClientMessage(Component.translatable("message.flavor_immersed_daily_argo.seat.marked"/"seat.unmarked"), true)`，返回 `InteractionResult.sidedSuccess(level.isClientSide)`。
  - [x] SubTask 1.3: 新建 `src/main/java/com/flavor_immersed_daily_argo/economy/SeatCushionEvents.java`
    - `@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME)`。
    - `@SubscribeEvent public static void onBlockBreak(BlockEvent.BreakEvent event)`：仅当 `event.getLevel() instanceof ServerLevel sl` 且 `SeatManager.get(sl).isSeat(event.getPos())` 时 `unmark(event.getPos())`。
  - [x] SubTask 1.4: 修改 `all/ModItems.java`：在「经济与生活」段 `RECEIPT_PRINTER` 之后新增（每项一行注释 + 一行注册）
    - `// 座垫标记：手持右键方块标记/取消座位，暂用原版占位贴图`
    - `public static final DeferredHolder<Item, Item> SEAT_CUSHION = REGISTRY.register("seat_cushion", () -> new SeatCushionItem(new Item.Properties(), "tooltip.flavor_immersed_daily_argo.seat_cushion"));`
    - 补 `import com.flavor_immersed_daily_argo.item.SeatCushionItem;`
  - [x] SubTask 1.5: 新建 `src/main/resources/assets/flavor_immersed_daily_argo/models/item/seat_cushion.json`（手写，勿放进 datagen）
    - `{"parent": "minecraft:item/generated", "textures": {"layer0": "minecraft:block/white_wool"}}`

- [x] Task 2: FoodOrder 接口与提示改造（可与 Task 1 并行）
  - [x] SubTask 2.1: 新建 `src/main/java/com/flavor_immersed_daily_argo/economy/FoodOrder.java`
    - `public interface FoodOrder { ItemStack getRequiredFood(); long getReward(); }`（带中文注释说明由外卖小哥与顾客共同实现）。
  - [x] SubTask 2.2: 修改 `entity/DeliveryRider.java`：`public class DeliveryRider extends Animal implements FoodOrder`，`getRequiredFood()` / `getReward()` 补 `@Override`，补 import。
  - [x] SubTask 2.3: 修改 `economy/RiderHintHud.java`：把 `hit.getEntity() instanceof DeliveryRider rider` 改为 `hit.getEntity() instanceof FoodOrder order`（变量名同步改名，`rider.getRequiredFood()` → `order.getRequiredFood()`，`rider.getReward()` → `order.getReward()`），去掉不再需要的 `DeliveryRider` import。

- [x] Task 3: 顾客实体（依赖 Task 1.1、Task 2.1）
  - [x] SubTask 3.1: 修改 `all/ModEntities.java`：新增
    - `public static final DeferredHolder<EntityType<?>, EntityType<Customer>> CUSTOMER = REGISTRY.register("customer", () -> EntityType.Builder.of(Customer::new, MobCategory.CREATURE).sized(0.6F, 1.8F).clientTrackingRange(10).build("customer"));` + import。
  - [x] SubTask 3.2: 新建 `src/main/java/com/flavor_immersed_daily_argo/entity/Customer.java`（照抄 `DeliveryRider` 的骨架）
    - 常量 `LIFETIME_TICKS = 6000`、`SEAT_RANGE = 16.0D`、`MAX_CUSTOMERS = 6`；同步数据 `DATA_SEATED`(BOOLEAN，默认 false)、`DATA_ORDER`(ITEM_STACK)、`DATA_REWARD`(LONG)。
    - 字段 `@Nullable BlockPos home`、`@Nullable BlockPos seat`、`int age`。
    - `isSeated()` 读 `DATA_SEATED`；`getSeat()`；`getHome()`；`setHome(BlockPos)` → `restrictTo(home, 16)`。
    - `setOrder(ItemStack, long)`、`getRequiredFood()`、`getReward()`（`@Override`）、`getRemainingTicks()`、`formatRemaining()` 同小哥。
    - `registerGoals()`：`FloatGoal(0)`、`new SitGoal(this)(1)`、`MoveTowardsRestrictionGoal(0.7)(3)`、`WaterAvoidingRandomStrollGoal(0.5)(4)`、`LookAtPlayerGoal(Player.class, 6.0F)(5)`、`RandomLookAroundGoal(6)`。
    - 内部 `private static class SitGoal extends Goal`：`canUse()` 在未就座且 `tickCount % 20 == 0` 时调用 `findFreeSeat()`；`start()` 用 `getNavigation().moveTo(seat 中心, 0.6D)`；`tick()` 判断距座位中心 < 1.6 或导航结束后 `sitOn(seat)`；`stop()` 停导航。
    - `findFreeSeat()`：遍历 `SeatManager.get(sl).seats()`，过滤「距 `home` ≤ SEAT_RANGE」「`isSeatValid`」「未被其他顾客占据（`getEntitiesOfClass(Customer.class, AABB(pos).inflate(0.5), c -> pos.equals(c.getSeat()))` 为空）」，取最近的一个。
    - `isSeatValid(BlockPos)`: `SeatManager.isSeat(pos)` 且 `!level.getBlockState(pos).isAir()` 且 `level.getBlockState(pos.above()).getCollisionShape(...).isEmpty()`。
    - `sitOn(BlockPos)`：记 `seat`、`DATA_SEATED=true`、`setNoAi(true)`、`setDeltaMovement(Vec3.ZERO)`、`getNavigation().stop()`、`setPos(x+0.5, y+1.0, z+0.5)`、`setYRot(朝向 home 的水平角)`、`setYHeadRot(同值)`。
    - `standUp()`：`seat=null`、`DATA_SEATED=false`、`setNoAi(false)`。
    - `tick()`（服务端分支）：`++age > LIFETIME_TICKS → discard()`；`home` 处方块不是 `CateringBusinessLicenseBlock` 或 `!OPEN → discard()`；已就座但 `!isSeatValid(seat) → standUp()`。
    - `mobInteract`：照抄 `DeliveryRider.mobInteract`，仅把文案 key 换成 `message.flavor_immersed_daily_argo.customer.done` / `.customer.remaining`。
    - `isFood=false`、`getBreedOffspring=null`、`removeWhenFarAway=false`；`addAdditionalSaveData`/`readAdditionalSaveData` 存读 `Age`/`Home`/`Seat`/`Order`/`Reward`。
  - [x] SubTask 3.3: 新建 `Client/entityClient/Model/CustomerModel.java` 与 `Client/entityClient/Renderer/CustomerRenderer.java`，按 spec「顾客渲染」小节实现；修改 `flavor_immersed_daily_argoClient.java`：`RegisterLayerDefinitions` 注册 `CustomerModel.LAYER_LOCATION`，`registerRenderers` 注册 `ModEntities.CUSTOMER.get() → CustomerRenderer::new`。
    - 注意 `DefaultPlayerSkin.getDefaultSkin(UUID)` 若签名不符，改为读取原版皮肤资源路径（`textures/entity/player/wide/steve.png` / `.../slim/alex.png`）按 UUID 奇偶取其一。
    - 注意 `addLayers` 会自动给 `HumanoidModel` 渲染器挂 `SilkDesignLayer`；本任务不改该逻辑，验证时确认顾客身上无异常花纹叠加。

- [x] Task 4: 餐饮营业执照方块（依赖 Task 3.2）
  - [x] SubTask 4.1: 新建 `economy/CateringBusinessLicenseBlock.java`，按 spec「餐饮营业执照方块」小节实现（`CODEC`、`FACING`、`OPEN`、四个 `VoxelShape`、`getStateForPlacement`、`canSurvive`、`updateShape`、`rotate`/`mirror`、`getShape`/`getCollisionShape`、`useWithoutItem` 切换 `OPEN`、`randomTick` 维持顾客）。
    - `useWithoutItem`：服务端 `setBlock(pos, state.setValue(OPEN, !open), 3)`；开业时 `spawnCustomer(level, pos)` 并播放 `SoundEvents.VILLAGER_YES`，提示「已开业」；打烊时不主动清理（顾客下一 tick 自行消失），提示「已打烊」。
      > 实际实现：**与 spec 的措辞不同**。打烊时由执照主动 `discardCustomers()`（因为顾客侧已解耦，改成由执照清理），并播放 `SoundEvents.VILLAGER_NO`。
    - `randomTick`：`open && countCustomers < 6 → spawnCustomer`。
    - `countCustomers(level, pos)` / `spawnCustomer(level, pos)` / `findSpawnSpot`+`canStand` 照抄 `ReceiptPrinterBlockEntity` 私有静态方法（半径 16，按 `home.equals(pos)` 过滤）；订单构造：`List<Item> foods = DeliveryFoodPool.dailyFoods(level.getDayTime()/24000L)`，随机取一种，`count = 1 + random.nextInt(3)`，`reward = count * (3 + random.nextInt(4))`。
      > 额外实现：`useItemOn` 手持物品返回 `SKIP_DEFAULT_BLOCK_INTERACTION`、空手返回 `PASS_TO_DEFAULT_BLOCK_INTERACTION`（避免手持方块右击被当成开业）；`onRemove` 清理归属顾客；`findSpawnSpot` 的 y 允许比牌子低 0~3 格（墙挂牌匾也能落到地面）。因存在 `OPEN` 状态，blockstate 按 `facing × open` 共 8 条 variants（同一模型）。
  - [x] SubTask 4.2: 修改 `all/ModBlocks.java`：在 `RECEIPT_PRINTER` 之后新增
    - `public static final DeferredBlock<Block> CATERING_BUSINESS_LICENSE = REGISTRY.register("catering_business_license", () -> new CateringBusinessLicenseBlock(BlockBehaviour.Properties.of().strength(1.0F).noOcclusion().sound(SoundType.WOOD).randomTicks()));` + import。
  - [x] SubTask 4.3: 新增方块资源（手写）
    - `assets/flavor_immersed_daily_argo/blockstates/catering_business_license.json`：variants 只按 `facing` 分支（`north` y=0、`east` y=90、`south` y=180、`west` y=270），模型均指向 `flavor_immersed_daily_argo:block/catering_business_license`。
    - `assets/flavor_immersed_daily_argo/models/block/catering_business_license.json`：把 `畜牧/经济系列/餐饮营业执照/catering_business_license.json` 的 `textures.0` / `particle` 改成 `flavor_immersed_daily_argo:block/catering_business_license`，并补 `"parent": "block/block"`（其余原样保留，含 `gui_light: front` 与 display 段）。
    - `assets/flavor_immersed_daily_argo/textures/block/catering_business_license.png`：拷贝 `畜牧/经济系列/餐饮营业执照/catering_business_license.png`。
    - `data/flavor_immersed_daily_argo/loot_table/blocks/catering_business_license.json`：照抄 `receipt_printer.json`，把 `name` 换成长 ID。

- [x] Task 5: 注册收尾（依赖 Task 1.4、Task 4.2）
  - [x] SubTask 5.1: 修改 `all/ModItems.java`：新增 `CATERING_BUSINESS_LICENSE`（`TooltipBlockItem`，key `tooltip.flavor_immersed_daily_argo.catering_business_license`）。
  - [x] SubTask 5.2: 修改 `all/ModCreativeTabs.java`：`economy_life` 分区计数 `16` → `18`；在 `output.accept(ModItems.RECEIPT_PRINTER.get());` 之后加 `output.accept(ModItems.CATERING_BUSINESS_LICENSE.get());` 与 `output.accept(ModItems.SEAT_CUSHION.get());`。
  - [x] SubTask 5.3: 修改 `datagen/FIDItemModelProvider.java`：经济段加 `withExistingParent("catering_business_license", modLoc("block/catering_business_license"));`（`seat_cushion` 为手写模型，**不要**写进 provider）。
  - [x] SubTask 5.4: 修改 `flavor_immersed_daily_argo.java`：`registerAttributes` 末尾加 `event.put(ModEntities.CUSTOMER.get(), Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.25).build());`
  - [x] SubTask 5.5: 修改 `datagen/lang/ModLangs.java`（en_us）与 `assets/flavor_immersed_daily_argo/lang/zh_cn.json`，新增键：
    - `block.catering_business_license` = 「Catering Business License」/「餐饮营业执照」
    - `item.seat_cushion` = 「Seat Cushion」/「座垫标记」
    - `entity.customer` = 「Customer」/「顾客」
    - `tooltip.seat_cushion`（中文：右键方块标记为顾客可坐的座位，再次右键或打掉该方块取消）
    - `tooltip.catering_business_license`（中文：挂在墙上，空手右键开业/打烊；开业后最多招来 6 位顾客）
    - `message.customer.done` = 「Customer served! Earned %s yuan」/「顾客已收货！获得 %s 元」
    - `message.customer.remaining` = 「Wrong item — this customer will wait %s more」/「要交的东西不对，这位顾客还会等 %s」
    - `message.license.open` = 「Opened for business」/「已开业」
    - `message.license.closed` = 「Closed」/「已打烊」
    - `message.seat.marked` / `message.seat.unmarked` = 「Marked as a seat」/「已标记该方块为座位」、 「Seat mark removed」/「已取消该方块的座位标记」

- [x] Task 6: 构建与数据生成验证（依赖全部）
  - [x] SubTask 6.1: 根目录执行 `.\gradlew compileJava --offline -q`，修完所有编译错误（重点核对 `SavedData.Factory`、`setDirty`、`PlayerModel.createMesh`、`DefaultPlayerSkin.getDefaultSkin`、`BlockEvent.BreakEvent`、`useWithoutItem`、`randomTick` 的 1.21.1 签名）。
  - [x] SubTask 6.2: 根目录执行 `.\gradlew runData --offline -q`，确认 `models/item/catering_business_license.json` 生成、`lang/en_us.json` 生成且无重复/冲突。
  - [x] SubTask 6.3: 复核 `SeatManager` 与 `Customer` 的存档字段名与 spec 一致，且 `RiderHintHud` 已不再引用 `DeliveryRider`。

# Task Dependencies

- Task 1 与 Task 2 可并行。
- Task 3 依赖 Task 1.1（`SeatManager`）与 Task 2.1（`FoodOrder`）。
- Task 4 依赖 Task 3.2（`Customer`、`ModEntities.CUSTOMER`）。
- Task 5 依赖 Task 1.4 与 Task 4.2（物品/方块已注册），Task 5.5 的 lang key 依赖 Task 1.2 / Task 3.2 / Task 4.1 中实际使用的 key。
- Task 6 依赖 Task 1–5 全部完成。
