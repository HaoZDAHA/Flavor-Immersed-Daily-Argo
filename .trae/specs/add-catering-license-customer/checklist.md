# 验收清单（add-catering-license-customer）

- [x] `SeatManager extends SavedData` 已实现，按维度存 `Set<BlockPos>`（tag key `Seats`），并提供 `get(ServerLevel)` / `isSeat` / `toggle` / `mark` / `unmark` / `seats()`；标记变更后置 dirty。
- [x] `seat_cushion` 已注册（id `seat_cushion`，`SeatCushionItem extends TooltipItem`），右键方块切换标记并在 `actionBar` 提示；对已标记方块再次右键取消。
- [x] `SeatCushionEvents` 监听 `BlockEvent.BreakEvent`，被破坏的已标记方块会取消标记。
- [x] `models/item/seat_cushion.json` 为手写占位模型（`minecraft:block/white_wool` 作 layer0），且未被 `FIDItemModelProvider` 重复生成。
- [x] `FoodOrder` 接口存在（`getRequiredFood()` / `getReward()`）；`DeliveryRider implements FoodOrder` 且行为、存档格式未变。
- [x] `RiderHintHud` 用 `FoodOrder` 判定，外卖小哥与顾客都能显示同一块提示，且不再 import `DeliveryRider`。
- [x] `customer` 实体已注册（`0.6×1.8`、`CREATURE`、`clientTrackingRange(10)`），属性已挂（HP 20 / 速度 0.25）。
- [x] `Customer` 具备 `DATA_SEATED` / `DATA_ORDER` / `DATA_REWARD` 同步数据，`home`/`seat`/`age` 会存读；`LIFETIME_TICKS = 6000`、`SEAT_RANGE = 16.0D`、`MAX_CUSTOMERS = 6` 与 spec 一致。
- [x] `Customer` 只把**已标记**的座位作为目标：`findFreeSeat()` 经 `SeatManager.isSeat` 过滤，并排除已被其他顾客占据的座位、上方被堵住的座位。
- [x] 未就座的顾客会寻路到最近空座位并坐下：`sitOn` 里 `setNoAi(true)`、清零速度、坐标对齐方块中心 `(x+0.5, y+1.0, z+0.5)`、朝向营业执照、`DATA_SEATED=true`。
- [x] 座位失效（取消标记 / 方块被破坏 / 上方被堵）时顾客 `standUp()` 恢复寻路；`home` 处方块不再是营业执照或已打烊时顾客 `discard()`。
  > 改由执照侧清理：`Customer.tick()` 只做「座位失效则起身」，不再轮询执照；执照打烊 / 被拆时 `discardCustomers()` 清掉 `home == 执照坐标` 的顾客。行为等价，避免执照 ↔ 顾客类互相引用。
- [x] `Customer.mobInteract` 交付逻辑与 `DeliveryRider` 一致（消耗物品、`spawnMoney`、音效、粒子、提示、discard），拿错东西只提示剩余等待时间。
- [x] 顾客订单来自 `DeliveryFoodPool.dailyFoods(day)` 随机一种、数量 1~3、`reward = count * (3 + rand(4))`。
- [x] `CustomerModel` 用 `PlayerModel.createMesh(CubeDeformation.NONE, false)` 建层，`LAYER_LOCATION` 已注册；`setupAnim` 在 `isSeated()` 时对双腿（及 pants）施加坐姿旋转 `-1.4137167F`。
- [x] `CustomerRenderer extends LivingEntityRenderer` 已注册，纹理取原版默认皮肤（按 UUID 随机），`scale()` 在就座时施加 `SEAT_SINK` 位移常量使髋部落到座垫顶面。
- [x] `catering_business_license` 方块已注册：`FACING` + `OPEN` 两个状态、四个按朝向的 `VoxelShape`（`getShape` 与 `getCollisionShape` 均生效）、`getStateForPlacement` 用 `getHorizontalDirection().getOpposite()`。
- [x] `canSurvive` 校验墙面且 `updateShape` 在不满足时返回空气；`rotate`/`mirror` 同步 `FACING`；`getRenderShape() = MODEL`。
  > 该类 `extends Block`（非 `BaseEntityBlock`），`RenderShape` 默认即 `MODEL`，无需覆写。
- [x] 空手右键营业执照切换 `OPEN`（开业立刻尝试生成 1 位顾客；打烊后归属顾客自行消失），并有音效与 `actionBar` 提示。
  > 打烊由执照主动 `discardCustomers()` 清掉归属顾客（不是顾客自己消失）；另外 `useItemOn` 手持物品时返回 `SKIP_DEFAULT_BLOCK_INTERACTION`，不会因为手持方块右击而误触开业。
- [x] `randomTick` 在 `open=true` 时把 `home == 执照坐标` 的顾客补足到最多 6 个，找不到可站立落脚点时不生成。
  > 实际改为**预定刻循环**：`useWithoutItem` 开业时 `scheduleTick(pos, this, 60)`，`Block#tick` 每 60 tick（约 3 秒）检查一次并自我续期，店没坐满就派一位。原因：随机刻平均约 68 秒才轮到这块牌子一次，用户体感就是「开业只生了一个」。`randomTick` 保留为保险，只在发现预定刻丢失时重新挂上。
- [x] 营业执照资源齐备：`blockstates/catering_business_license.json`（仅按 `facing` 分支，north=0/east=90/south=180/west=270）、`models/block/catering_business_license.json`（贴图路径已改为 `flavor_immersed_daily_argo:block/catering_business_license`）、`textures/block/catering_business_license.png`（素材已拷贝）、`loot_table/blocks/catering_business_license.json`。
  > blockstates 实为 `facing × open` 共 8 条 variants（模型相同）：因为方块带 `OPEN` 布尔状态，缺 `open` 分支会导致状态无模型可渲染。
- [x] `ModCreativeTabs.economy_life` 分区计数为 18，且执照与座垫标记已在 `RECEIPT_PRINTER` 之后 `output.accept`。
- [x] `FIDItemModelProvider` 为 `catering_business_license` 生成了继承方块模型的物品模型（`seat_cushion` 不写入 provider）。
- [x] `ModLangs`（en_us）与 `zh_cn.json` 均新增了方块名、物品名、实体名、两条 tooltip、`customer.done`/`customer.remaining`、`license.open`/`license.closed`、`seat.marked`/`seat.unmarked`。
- [x] `.\gradlew compileJava --offline -q` 通过，无编译错误。
- [x] `.\gradlew runData --offline -q` 通过，`lang/en_us.json` 与 `models/item/catering_business_license.json` 正常生成且无重复项。
- [x] 现有功能未被破坏：`DeliveryRider` 的订单/交付/寿命逻辑与存档字段未变；`RiderHintHud` 对外卖小哥的显示不变。
