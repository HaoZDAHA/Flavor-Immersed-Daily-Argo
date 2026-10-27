# 餐饮营业执照 + 顾客 规格说明（add-catering-license-customer）

## Why

模组已有「小票打印机 → 外卖小哥」这条经济闭环，但缺少**堂食**这一环：玩家需要一个能挂在墙上的营业执照，开业后自动招来顾客就座、点单、等餐、交付、付钱。同时需要一个把普通方块「标成座位」的小道具，让顾客有明确的就座目标。

## What Changes

- 新增方块 **`catering_business_license`（餐饮营业执照）**：水平朝向（`HORIZONTAL_FACING`）的墙挂薄板，带 `open`（开业/打烊）状态；空手右键切换开业/打烊；开业后由方块自身（`randomTick`）在附近维持最多 **6 个顾客**。
- 新增物品 **`seat_cushion`（座垫标记）**：手持它右键任意方块将该方块标记为「座位」，再次右键取消；被标记的方块被破坏时自动取消标记。座位集合按维度存进 `SavedData`。
- 新增实体 **`customer`（顾客）**：与原版 Steve 相同的玩家模型与动作（`PlayerModel` + 原版默认皮肤随机）；主动寻路到**最近的空座位**坐下，坐下时禁用 AI 并摆出坐姿；订单/等待/交付/金额与「外卖小哥」完全一致（今日菜品来自 `DeliveryFoodPool.dailyFoods(day)`，最大 6 个/执照）。
- 新增共享接口 **`FoodOrder`**：`DeliveryRider` 与 `Customer` 共同实现，`RiderHintHud` 改为识别该接口（复用现有两条文案）。
- 注册与资源：`ModItems` / `ModBlocks` / `ModEntities` / `ModCreativeTabs`（`economy_life` 分区 16 → 18）/ 主类属性 / 客户端渲染注册；blockstate、方块模型、方块贴图、loot table、`zh_cn.json`、`ModLangs`（en_us）。

**BREAKING**：无（不改变任何既有方块/物品/实体的行为；`DeliveryRider` 仅新增 `implements FoodOrder`，`RiderHintHud` 仅放宽识别范围）。

## Impact

- 影响能力：经济与生活（堂食客流）、实体渲染、座位数据持久化。
- 影响代码：
  - 新建：`economy/SeatManager.java`、`item/SeatCushionItem.java`、`economy/SeatCushionEvents.java`、`economy/FoodOrder.java`、`economy/CateringBusinessLicenseBlock.java`、`entity/Customer.java`、`Client/entityClient/Model/CustomerModel.java`、`Client/entityClient/Renderer/CustomerRenderer.java`
  - 修改：`all/ModItems.java`、`all/ModBlocks.java`、`all/ModEntities.java`、`all/ModCreativeTabs.java`、`flavor_immersed_daily_argo.java`、`flavor_immersed_daily_argoClient.java`、`entity/DeliveryRider.java`、`economy/RiderHintHud.java`、`datagen/FIDItemModelProvider.java`、`datagen/lang/ModLangs.java`
  - 资源：新建 `blockstates/catering_business_license.json`、`models/block/catering_business_license.json`、`models/item/seat_cushion.json`、`textures/block/catering_business_license.png`、`data/.../loot_table/blocks/catering_business_license.json`；修改 `lang/zh_cn.json`

## ADDED Requirements

### Requirement: 座位标记（seat_cushion）

系统 SHALL 提供物品 `seat_cushion`（中文「座垫标记」），用于把任意方块标记为顾客可坐的座位；标记集合按维度持久化保存。

- 注册 id：`flavor_immersed_daily_argo:seat_cushion`，类 `SeatCushionItem extends TooltipItem`，默认 `Item.Properties()`（可堆叠 64），tooltip key `tooltip.flavor_immersed_daily_argo.seat_cushion`。
- 存储：`SeatManager extends SavedData`，数据名 `flavor_immersed_daily_argo_seats`，内部 `Set<BlockPos>`；`save` 时写成 `LongArray`（tag key `Seats`）。
- 物品模型暂用手写占位（无自有贴图）：`models/item/seat_cushion.json` → `{"parent":"minecraft:item/generated","textures":{"layer0":"minecraft:block/white_wool"}}`。

#### Scenario: 标记座位

- **WHEN** 玩家手持 `seat_cushion` 右键点击一个方块
- **THEN** 该方块坐标被写入 `SeatManager`，`actionBar` 提示「已标记该方块为座位」，返回 `InteractionResult.sidedSuccess`

#### Scenario: 取消标记

- **WHEN** 玩家对**已被标记**的方块再次右键
- **THEN** 该坐标从 `SeatManager` 移除，提示「已取消该方块的座位标记」

#### Scenario: 方块被破坏

- **WHEN** 玩家破坏掉一个已被标记的方块（`BlockEvent.BreakEvent`）
- **THEN** 该坐标的标记被移除；若此时有顾客正坐在这里，该顾客自行起身

### Requirement: 餐饮营业执照方块

系统 SHALL 提供墙挂方块 `catering_business_license`，水平朝向 + 开业状态；开业后维持最多 6 个顾客。

- 注册 id：`flavor_immersed_daily_argo:catering_business_license`，类 `CateringBusinessLicenseBlock extends Block`，`CODEC = simpleCodec(CateringBusinessLicenseBlock::new)`。
- 属性：`.strength(1.0F).noOcclusion().sound(SoundType.WOOD).randomTicks()`。
- 方块状态：`FACING = BlockStateProperties.HORIZONTAL_FACING`（默认 `NORTH`）、`OPEN = BooleanProperty.create("open")`（默认 `false`）。
- 形状（薄板在模型 +z 侧，与 `fish_display_board` 约定一致）：
  - `NORTH = Block.box(1, 3, 15, 15, 13, 16)`
  - `EAST  = Block.box(0, 3, 1, 1, 13, 15)`
  - `SOUTH = Block.box(1, 3, 0, 15, 13, 1)`
  - `WEST  = Block.box(15, 3, 1, 16, 13, 15)`
  - `getShape` 与 `getCollisionShape` 均按 `FACING` 返回对应形状。
- 放置：`getStateForPlacement` 用 `context.getHorizontalDirection().getOpposite()` 作为 `FACING`。
- 存活：`canSurvive` 要求 `pos.relative(facing.getOpposite())` 处的方块在该方向上 `isFaceSturdy`；`updateShape` 在 `!canSurvive` 时返回 `Blocks.AIR.defaultBlockState()`。
- `rotate`/`mirror` 同步旋转 `FACING`（照抄 `ReceiptPrinterBlock`）。
- 无 BlockEntity；顾客的维持靠 `randomTick` 完成。

#### Scenario: 挂牌并开业

- **WHEN** 玩家空手右键一块 `open=false` 的营业执照
- **THEN** 服务端把 `OPEN` 置为 `true`，播放音效、提示「已开业」，并立刻尝试生成 1 个顾客

#### Scenario: 打烊

- **WHEN** 玩家空手右键一块 `open=true` 的营业执照
- **THEN** `OPEN` 置为 `false`，提示「已打烊」，属于该执照的所有顾客（`home == 该坐标`）在下一 tick 自行消失

#### Scenario: 维持顾客数量

- **WHEN** 营业执照 `open=true` 且随机刻触发
- **THEN** 若 `home == 该坐标` 的顾客数 < 6，则在执照附近找一格可站立的落脚点生成 1 个顾客（订单/报酬随机）；找不到落脚点则本次不生成

### Requirement: 顾客实体（customer）

系统 SHALL 提供实体 `customer`，使用原版玩家模型与默认皮肤，主动寻路到空的已标记座位并坐下，交付流程与外卖小哥一致。

- 注册 id：`flavor_immersed_daily_argo:customer`，类 `Customer extends Animal implements FoodOrder`，尺寸 `sized(0.6F, 1.8F)`，`clientTrackingRange(10)`，`MobCategory.CREATURE`。
- 属性：`Animal.createMobAttributes()` + `MAX_HEALTH 20.0` + `MOVEMENT_SPEED 0.25`。
- 同步数据：`DATA_SEATED(boolean)`、`DATA_ORDER(ItemStack)`、`DATA_REWARD(long)`。
- 常量：`LIFETIME_TICKS = 6000`（5 分钟后自行离开，与小哥一致）、`SEAT_RANGE = 16.0D`（找座位半径）、`MAX_CUSTOMERS = 6`（执照侧统计用）。
- 订单：取当天 `DeliveryFoodPool.dailyFoods(level.getDayTime() / 24000L)` 中的随机一种，数量 1~3，`reward = count * (3 + random.nextInt(4))`。
- goals：`FloatGoal(0)` → `SitGoal(1)` → `MoveTowardsRestrictionGoal(0.7, 3)` → `WaterAvoidingRandomStrollGoal(0.5, 4)` → `LookAtPlayerGoal(6.0F, 5)` → `RandomLookAroundGoal(6)`。
- `setHome(BlockPos licensePos)`：记录归属并 `restrictTo(home, 16)`。
- 存读：`Age` / `Home` / `Seat` / `Order` / `Reward`。

#### Scenario: 寻路就座

- **WHEN** 顾客未就座，且 `SEAT_RANGE` 内存在一个**已标记**、**上方两格无碰撞**、**没有其他顾客占据**的座位
- **THEN** 顾客寻路走到该座位，抵达后 `setNoAi(true)`、`setDeltaMovement(0,0,0)`、坐标对齐到方块中心（`x+0.5, y+1.0, z+0.5`）、朝向营业执照、`DATA_SEATED=true`

#### Scenario: 座位失效则起身

- **WHEN** 正坐着的座位被取消标记 / 座位方块被破坏 / 座位上方被堵住
- **THEN** 顾客 `setNoAi(false)`、`DATA_SEATED=false`、清空 `Seat`，恢复寻路（若已无有效座位则就地漫游）

#### Scenario: 归属失效则消失

- **WHEN** 顾客 tick 时发现 `home` 处的方块不再是 `catering_business_license`，或该执照 `open=false`
- **THEN** 顾客 `discard()`

#### Scenario: 交付订单

- **WHEN** 玩家手持与订单同种类、数量不少于订单的物品右键顾客
- **THEN** 按 `DeliveryRider` 现有逻辑：消耗物品、`DeliveryManager.spawnMoney(level, position(), reward)` 撒钱、`SoundEvents.VILLAGER_YES`、`ParticleTypes.HAPPY_VILLAGER`、提示「顾客已收货！获得 %s 元」、`discard()`

#### Scenario: 拿错东西

- **WHEN** 玩家手持不匹配的物品右键顾客
- **THEN** 只提示「要交的东西不对，这位顾客还会等 %s」（剩余时间格式化同 `DeliveryRider.formatRemaining`），不消耗物品

### Requirement: 顾客渲染（原版玩家模型 + 默认皮肤）

系统 SHALL 用原版 `PlayerModel` 渲染顾客，纹理从原版默认皮肤中按实体 UUID 随机取，并在就座时摆出坐姿。

- `CustomerModel extends PlayerModel<Customer>`，`LAYER_LOCATION = new ModelLayerLocation(modLoc("customer"), "main")`，`createBodyLayer()` = `LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64)`。
- `CustomerModel.setupAnim`：先 `super.setupAnim(...)`；若 `entity.isSeated()`，把 `rightLeg`/`leftLeg` 及对应的 `rightPants`/`leftPants` 的 `xRot` 设为 `-1.4137167F`（原版坐姿角度），并同步 `yRot = 0`、`zRot = 0`。
- `CustomerRenderer extends LivingEntityRenderer<Customer, CustomerModel>`，`shadowRadius = 0.5F`。
- 纹理：`DefaultPlayerSkin.getDefaultSkin(entity.getUUID())`（UUID 随机 → 皮肤随机）。
- 就座时的模型下沉：重写 `scale(Customer, PoseStack, float)`，当 `entity.isSeated()` 时施加固定位移常量 `SEAT_SINK = 0.75F`（常量置于类顶部，便于后续按观感微调），使髋部落到座垫顶面。
- 客户端注册：`RegisterLayerDefinitions` 里注册 `CustomerModel::createBodyLayer`；`registerRenderers` 里注册 `CustomerRenderer::new`。

## MODIFIED Requirements

### Requirement: 外卖小哥订单提示（RiderHintHud）

`RiderHintHud` SHALL 改为识别接口 `FoodOrder`：准星命中 `DeliveryRider` **或** `Customer` 时都显示同一块提示（图标 + 「需要交付：%s × %s」+「报酬：%s 元」），文案 key 与绘制方式保持不变。

### Requirement: DeliveryRider

`DeliveryRider` SHALL 声明 `implements FoodOrder`（`getRequiredFood()` / `getReward()` 已存在，仅补 `@Override`），行为与存档格式不变。

### Requirement: 餐饮营业执照方块物品

`ModItems` SHALL 新增 `CATERING_BUSINESS_LICENSE = TooltipBlockItem(ModBlocks.CATERING_BUSINESS_LICENSE.get(), new Item.Properties(), "tooltip.flavor_immersed_daily_argo.catering_business_license")`、`SEAT_CUSHION = TooltipItem(new Item.Properties(), "tooltip.flavor_immersed_daily_argo.seat_cushion")`。

### Requirement: 创造模式标签页

`economy_life` 分区计数 SHALL 由 16 改为 18，并在 `ModItems.RECEIPT_PRINTER.get()` 之后依次 `output.accept` 营业执照与座垫标记。

## REMOVED Requirements

无。
