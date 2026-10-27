# 商业电脑 + 店铺系统 规格说明（add-shopping-computer-shop）

## Why

模组已具备「银行卡 / 自助取款机 / 纸币」这套货币闭环，但钱只能存取、外币兑换，缺少**消费与经营**的场景：玩家没有地方花掉卡面账户里的额度，也没有办法把自己的产物卖给别人。

本规格新增一台家电方块 **商业电脑（shopping_computer）**，一台机器同时提供三条业务线：

1. **新闻页面**（暂为占位空白，留给后续内容）；
2. **网购页面**：浏览店铺（数据包店铺 / 配置文件店铺 / 玩家店铺）→ 加购物车 → 增减数量 → 一次性用银行卡结账；
3. **开店页面**：多人联机下玩家自己开店、上架、改价、补货、提现，全世界的人都能来买。

默认附带两家**系统店铺**（可改可删，作为后来开发者编写 `data/<ns>/shop/*.json` 的数据格式示范）。

## What Changes

- 新增方块 **`shopping_computer`（商业电脑）**：水平朝向（`HORIZONTAL_FACING`）+ 粗碰撞箱；物块模型由 `畜牧/经济系列/商业电脑/shopping_computer.json`（Blockbench）转标准方块模型，贴图用同目录 `shopping_computer.png`。空手/持物右键均打开界面（不需持卡）。
- 新增**店铺数据层**（包 `com.flavor_immersed_daily_argo.economy.shop`）：
  - `ShopEntry`（`item / count / price / stock`，`stock < 0` 表示无限库存，系统店铺用）；
  - `Shop`（`id / title / ownerName / entries / balance`，`ownerName` 为空 = 系统店铺）；
  - `ShopDataLoader`（`SimpleJsonResourceReloadListener`，读 `data/<ns>/shop/*.json`，支持 `item` 与 `tag` 两种条目，`tag` 在加载期展开为逐物品条目）；
  - `ShopManager`（`SavedData`，全局单例挂在主世界 `getDataStorage()`，保存**玩家店铺**，键 `flavor_immersed_daily_argo_shops`）。
- 新增**配置文件商店**：`EconomyConfig` 新增 `shop` 段（`player_shops_enabled` / `max_shops_per_player` / `extra_entries`），`extra_entries` 每行 `物品注册名,数量,价格`，并入一个由配置驱动的系统店铺（这就是「适应 config」那一路）。
- 新增**菜单** `MarketMenu`（银行卡槽 + 操作槽 + 玩家背包）与**屏幕** `MarketScreen`（三个页签：新闻 / 网购 / 开店；购物车；开店管理；无贴图背景，照 `AtmScreen` 用 `gui.fill` 暗底 + `gui.renderItem` 行内图标）。
- 新增**网络包**：`ShopSyncPacket`（服务端 → 客户端，全量下发店铺列表）、`ShopCheckoutPacket`（客户端 → 服务端，提交购物车结账）、`ShopManagePacket`（客户端 → 服务端，开店动作：建店 / 删店 / 上架 / 下架 / 改价 / 补货 / 提现）。
- 注册收尾：`ModBlocks` / `ModItems` / `ModCreativeTabs`（`economy_life` 分区 18 → 19）/ `ModMenus` / 主类 `registerPackets` / 客户端 `registerScreens` / `FIDItemModelProvider` / lang（`zh_cn.json` + `ModLangs`）。
- 资源：`models/block/shopping_computer.json`（手写）、`blockstates/shopping_computer.json`（facing × 4）、`textures/block/shopping_computer.png`（拷贝）；数据包新增 `shop/star_farm_market.json`、`shop/mineral_market.json`、`tags/item/livestock_spawn_eggs.json`。

**BREAKING**：无（不改变任何既有方块/物品/菜单/包的行为；`EconomyConfig` 仅**新增**一个配置段）。

## Impact

- 影响能力：经济与生活（消费 / 经营）、多人联机交易。
- 影响代码：
  - 新建（`economy/shop/`）：`ShopEntry.java`、`Shop.java`、`ShopDataLoader.java`、`ShopManager.java`、`ShopClientData.java`、`CartLine.java`、`ShopSyncPacket.java`、`ShopCheckoutPacket.java`、`ShopManagePacket.java`、`ShopNetwork.java`；`economy/ShoppingComputerBlock.java`、`economy/MarketMenu.java`、`economy/MarketScreen.java`。
  - 修改：`all/ModBlocks.java`、`all/ModItems.java`、`all/ModCreativeTabs.java`、`all/ModMenus.java`、`flavor_immersed_daily_argo.java`（`registerPackets` + `AddReloadListenerEvent`）、`flavor_immersed_daily_argoClient.java`（`registerScreens`）、`economy/EconomyConfig.java`、`datagen/FIDItemModelProvider.java`、`datagen/lang/ModLangs.java`。
  - 资源：新建 `blockstates/shopping_computer.json`、`models/block/shopping_computer.json`、`textures/block/shopping_computer.png`、`data/.../shop/{star_farm_market,mineral_market}.json`、`data/.../tags/item/livestock_spawn_eggs.json`；修改 `lang/zh_cn.json`。

## 已校准的既有约定（实现前须知）

- **Blockbench → 方块模型**：元素与 `uv` / `texture_size` 原样照搬，补 `"parent": "block/block"`，把 `textures.0` 与 `particle` 改写成 `flavor_immersed_daily_argo:block/shopping_computer`。（对照 `catering_business_license.json` 已验证：`texture_size` 与 `uv` 均未做缩放。）
- **朝向旋转**：本项目约定为 `facing=north → y=0`（模型正视面朝向 **+z / south**，与 `catering_business_license` 一致），`east → y=90`、`south → y=180`、`west → y=270`。
  商业电脑的屏幕在模型 **−x / west** 侧，若直接套用该组映射会出现屏幕背对玩家；因此本规格**为商业电脑单独定义**其朝向映射（见下「商业电脑方块」），并让**碰撞箱与该映射严格一致**，保证「碰撞箱始终贴合旋转后的模型」。
- **注册**：datagen 只产出 `models/item/*` 与 `lang/en_us.json`；blockstates / 方块模型 / 贴图**手写**；**每个新物品必须同步登记进 `FIDItemModelProvider`**，否则下次 `runData` 的 HashCache 会静默删掉它的 `models/item/*.json`。
- **物品注册风格**：`TooltipBlockItem`（构造签名照抄 `ModItems` 中 `CATERING_BUSINESS_LICENSE`）。

## ADDED Requirements

### Requirement: 商业电脑方块（shopping_computer）

系统 SHALL 提供方块 `shopping_computer`（中文「商业电脑」），带水平朝向，粗碰撞箱与旋转后的模型一致；右键（空手或持物）打开市场界面。

- 注册 id：`flavor_immersed_daily_argo:shopping_computer`，类 `ShoppingComputerBlock extends Block`，`CODEC = simpleCodec(ShoppingComputerBlock::new)`。
- 属性：`BlockBehaviour.Properties.of().strength(2.0F).noOcclusion().sound(SoundType.METAL)`。
- 方块状态：`FACING = BlockStateProperties.HORIZONTAL_FACING`，默认 `NORTH`；`createBlockStateDefinition` 只加 `FACING`。
- 放置：`getStateForPlacement` 用 `context.getHorizontalDirection().getOpposite()`。
- `rotate` / `mirror` 同步旋转 `FACING`（照抄 `AtmBlock`）。
- **商业电脑专用朝向映射**（屏幕在模型 −x 侧，使屏幕朝向玩家；与 `catering_business_license` 的 `+z` 正视模型不同，故单独约定）：
  - `facing=north → y=90`、`east → y=180`、`south → y=270`、`west → y=0`。
- **粗碰撞箱**（`getShape` 与 `getCollisionShape` 同，两盒并集；`Shapes.or`）：
  - 桌面/底座盒（所有朝向相同）：`Block.box(0, 0, 0, 16, 2, 16)`；
  - 屏幕盒（随 `FACING` 旋转，由模型空间 `x∈[11,13], y∈[2,16], z∈[0,16]` 绕方块中心旋转后取外接盒）：
    - `NORTH` → `Block.box(0, 2, 11, 16, 16, 13)`
    - `EAST`  → `Block.box(3, 2, 0, 5, 16, 16)`
    - `SOUTH` → `Block.box(0, 2, 3, 16, 16, 5)`
    - `WEST`  → `Block.box(11, 2, 0, 13, 16, 16)`
  - 说明：屏幕盒与 blockstate 的 `y` 旋转出自同一套映射，故碰撞箱**永远贴合当前朝向的模型**（不需要「精密」）。
- 交互：
  - `useItemOn`（手持物品）：服务端 `player.openMenu(...)` 打开市场界面，返回 `ItemInteractionResult.sidedSuccess(level.isClientSide)`（**不禁卡**，持任何物品都能开）。
  - `useWithoutItem`（空手）：同样打开界面，返回 `InteractionResult.sidedSuccess(level.isClientSide)`。
  - 打开用 `SimpleMenuProvider((id, inv, p) -> new MarketMenu(id, inv), Component.translatable("gui.flavor_immersed_daily_argo.market"))`；`openMenu` 的 `extraData` 里写入**全量店铺列表**（`ShopManager.allShops(server)` 编码，见 `ShopNetwork#writeShops`），客户端据此在构造界面时填充列表。
- 无 BlockEntity。

#### Scenario: 摆放朝向

- **WHEN** 玩家面向某方向放置商业电脑
- **THEN** 方块 `FACING = context.getHorizontalDirection().getOpposite()`，blockstate 按上表旋转模型，屏幕朝向玩家，碰撞箱随之旋转到屏幕所在侧

#### Scenario: 打开界面

- **WHEN** 玩家（空手或手持任意物品）右键商业电脑
- **THEN** 服务端打开 `MarketMenu` 界面，并把全量店铺列表随登录包下发；客户端 `MarketScreen` 立即有三页签可用

### Requirement: 店铺数据（ShopEntry / Shop / ShopDataLoader）

系统 SHALL 用数据包 JSON 定义**系统店铺**，格式对附属模组开放。

- 数据包目录：`data/<命名空间>/shop/<名称>.json`，仅服务端加载（走 `AddReloadListenerEvent`），再全量下发客户端，保证多端一致。
- JSON 格式：
  ```json
  {
    "title": "起点农贸市场",
    "entries": [
      {"item": "flavor_immersed_daily:temperatewildfruit", "count": 1, "price": 10},
      {"tag": "flavor_immersed_daily_argo:livestock_spawn_eggs", "count": 1, "price": 50}
    ]
  }
  ```
- `ShopEntry`：`record ShopEntry(Item item, int count, int price, int stock)`；`count` = 一次购买得到的件数（例如「一组 64」= 64），`price` = 一次购买的价格（元），`stock < 0` = 无限库存。
- `Shop`：`record Shop(String id, String title, String ownerName, List<ShopEntry> entries, long balance, boolean playerShop)`；`ownerName` 为空串 = 系统店铺；`id` 系统店铺取 `命名空间:名称`，玩家店铺取 `player:<ownerName>:<序号>`。
- `ShopDataLoader extends SimpleJsonResourceReloadListener`：
  - 目录 `shop`，`GSON` 解析；跳过 `_` 前缀文件。
  - **`tag` 条目在加载期展开**：用 `AddReloadListenerEvent#getRegistryAccess()` 拿到 `RegistryAccess`，`registryAccess.registryOrThrow(Registries.ITEM).getTag(TagKey.create(Registries.ITEM, tagId))`，把标签内每个物品各生成一条 `count/price` 相同的 `ShopEntry`（`stock=-1`）。
  - 产出 `List<Shop>` 存入静态 `ShopDataLoader.SHOPS`（仅在服务端资源重载后写入；客户端不注册该监听器，避免服务端空数据覆盖客户端——见既有教训）。
  - 只含服务端可用信息（内容随登录包/同步包下发）。
- 默认数据包店铺（两个，可改可删）：
  - `star_farm_market.json` —— 起点农贸市场：
    - 8 个野生作物堆（`flavor_immersed_daily:` 的 `temperatewildfruit / tropicalwild_fruit / wildfruitincoldzone / wildflowerandleaf / wildgrainplant / wildmushroomplant / wildseedplant / wildtuberplants`）各 `count=1, price=10`；
    - `tag = flavor_immersed_daily_argo:livestock_spawn_eggs` → `count=1, price=50`（展开为 8 个刷怪蛋条目）。
  - `mineral_market.json` —— 矿物材料市场：`minecraft:coal/64/100`、`minecraft:iron_ingot/64/200`、`minecraft:copper_ingot/64/100`。
- `tags/item/livestock_spawn_eggs.json`：`{"replace": false, "values": [8 个刷怪蛋 id]}`，八个 id 为 `flavor_immersed_daily_argo:{duck,goose,quail,pigeon,turkey,bullfrog,bamboorat,mouse}_spawn_egg`。

#### Scenario: 加载系统店铺

- **WHEN** 服务端 `/reload` 或首次世界加载
- **THEN** `ShopDataLoader` 读全部 `shop/*.json`，展开 `tag` 条目，生成 `List<Shop>`；下次有人打开商业电脑时随登录包下发

### Requirement: 玩家店铺持久化（ShopManager）

系统 SHALL 在多人联机下持久化玩家创建的店铺，全服任何人都能购买。

- `ShopManager extends SavedData`，`DATA_NAME = "flavor_immersed_daily_argo_shops"`，`FACTORY = new SavedData.Factory<>(ShopManager::new, (tag, provider) -> new ShopManager(tag), null)`；取用 `server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME)`。
- NBT 结构：`Shops` 为 `ListTag`，每个 `CompoundTag` 存 `Id / Title / Owner / Balance(Long)` + `Entries`（`ListTag`：`Item`(注册名)、`Count`、`Price`、`Stock`）。
- 方法与约束：
  - `List<Shop> allShops(MinecraftServer)`：`ShopDataLoader` 的系统店铺 + 配置文件商店 `+` 本存档的玩家店铺。
  - `boolean createShop(ServerPlayer, String title)`：受 `EconomyConfig.PLAYER_SHOPS_ENABLED` 与 `MAX_SHOPS_PER_PLAYER` 限制（按 `ownerName` 统计）；成功返回 true 并 `setDirty()`。
  - `boolean deleteShop(ServerPlayer, String shopId)`：仅店主可删。
  - `boolean addEntry(ServerPlayer, String shopId, ItemStack goods, int price)`：取走操作槽的整叠物品作为库存，条目不存在则新建；同物品同价则并入库存增量（`setDirty`）。
  - `boolean removeEntry(ServerPlayer, String shopId, int index)`：把剩余库存退回店主背包，删除条目。
  - `boolean setPrice(ServerPlayer, String shopId, int index, int price)`。
  - `boolean restock(ServerPlayer, String shopId, int index, ItemStack goods)`：操作槽物品须与条目同物品，整叠并入该条目库存。
  - `boolean withdraw(ServerPlayer, String shopId, int cardSlot)`：把 `balance` 写进该玩家银行卡槽位里那张卡的**卡面账户**（`CreditCardItem.setAccount`），`balance` 清零。
  - `static void sync(ServerPlayer)` / `static void syncAll(MinecraftServer)`：向客户端广播 `ShopSyncPacket` 全量列表。
- 玩家店铺的 `stock ≥ 0`（有限库存，售完即 `stock=0`，`加减数量` 受库存夹取）；系统店铺 `stock = -1`。

#### Scenario: 创建店铺

- **WHEN** 玩家在「开店」页输入店名并点「创建店铺」
- **THEN** 服务端 `ShopManager.createShop` 成功则写入 NBT 并广播 `ShopSyncPacket`，客户端列表出现该店；超出 `max_shops_per_player` 或功能被关闭则提示失败（`actionBar`）

#### Scenario: 上架与补货

- **WHEN** 玩家把要卖的物品放进「操作槽」，在「开店」页输入价格并点「上架」
- **THEN** 服务端取走操作槽整叠物品，在其店铺新建/并入库存条目，广播同步

#### Scenario: 提现

- **WHEN** 店主的店 `balance > 0`，在银行卡槽放入自己的银行卡并点「提现」
- **THEN** `balance` 累加到该卡的卡面账户，`balance` 清零，广播同步

### Requirement: 配置文件商店

系统 SHALL 由 `EconomyConfig` 的 `shop` 段驱动一个系统店铺（「适应 config」）。

- `shop.player_shops_enabled`（boolean，默认 `true`）：是否允许玩家开店。
- `shop.max_shops_per_player`（int，`defineInRange`，默认 `3`，范围 `0..64`）。
- `shop.extra_entries`（`defineListAllowEmpty`，默认 `[]`）：每行 `物品注册名,数量,价格`（例如 `minecraft:apple,4,6`），全部并入一个系统店铺（标题用 lang 键 `gui.flavor_immersed_daily_argo.shop.config_shop`）；坏行忽略。

#### Scenario: 配置加货

- **WHEN** 管理端在 `shop.extra_entries` 增加一行 `minecraft:apple,4,6` 并保存配置
- **THEN** 玩家打开商业电脑时，多出一个「配置文件商店」中 4 个苹果 6 元的条目

### Requirement: 市场菜单与界面（MarketMenu / MarketScreen）

系统 SHALL 提供一个三页签界面，承载新闻 / 网购 / 开店三块功能。

- `MarketMenu extends AbstractContainerMenu`（`ModMenus.MARKET_MENU = IMenuTypeExtension.create(MarketMenu::new)`）：
  - 槽位 0：**银行卡槽**（`SimpleContainer(1)`，`mayPlace = stack.getItem() instanceof CreditCardItem`）；
  - 槽位 1：**操作槽**（任意物品，`mayPlace` 恒 true；用于上架 / 补货的「货物来源」）；
  - 玩家背包 3×9 + 快捷栏 9；
  - `quickMoveStack`：把玩家背包里的银行卡优先并入卡槽，其余照常；`stillValid` 恒 `true`；`removed(Player)` 把卡槽与操作槽物品还给玩家（防止丢件）。
  - 构造：`(int id, Inventory inv, FriendlyByteBuf extraData)` 读 `ShopNetwork.readShops(buf)` 存入客户端 `ShopClientData`；`(int id, Inventory inv)` 无参版供服务端开界面用。
- `MarketScreen extends AbstractContainerScreen<MarketMenu>`（无贴图背景，`gui.fill` 暗底）：
  - 顶部两个页签按钮：**新闻** / **网购** / **开店**（三个页签按钮，切换 `page`）；
  - **新闻页**：仅显示占位文案 `gui.flavor_immersed_daily_argo.market.news_placeholder`（本规格不实现内容）；
  - **网购页**：
    - 左列：店铺列表（可滚动，点击进入该店「子目录」）；进入子目录后顶部出现「← 返回」；
    - 右列：当前店铺的条目列表（物品图标 + `x数量` + 价格 + 购物车 `+/-`）；点击条目加入购物车，`+/-` 增减该条目数量；
    - 底部：购物车合计、`清空购物车`、`结账` 按钮；
    - `结账` → `PacketDistributor.sendToServer(new ShopCheckoutPacket(ShopClientData.cart()))`；服务端成功后逐条校验。
  - **开店页**：
    - 店铺列表（自己的店高亮，可选中）；`创建店铺`（EditBox 店名 + 按钮）、`删除店铺`；
    - 选中自己的店后：条目列表 + `上架`（EditBox 价格；物品取操作槽）、`下架`、`改价`、`补货`（EditBox 价格可复用，物品取操作槽）；
    - `提现`（把店铺余额提到银行卡槽的那张卡）。
  - 所有服务端动作都发 `ShopManagePacket`，返回后由 `ShopSyncPacket` 刷新列表。
- 购物车状态存客户端 `ShopClientData`（`List<CartLine>`，`CartLine(shopId, entryIndex, qty)`）；界面关闭不清空（便于继续逛）。

#### Scenario: 加购物车并结账

- **WHEN** 玩家在网购页把若干条目加入购物车（可增减数量），把银行卡放进卡槽，点「结账」
- **THEN** 服务端校验「购物车非空 / 卡槽有银行卡 / 卡面账户 ≥ 合计 / 有限库存足量」，通过则：扣除卡面账户、按 `count × qty` 发物品到背包（满则掉落）、扣减玩家店铺库存、把货款累加到店主 `balance`；任一条不满足则 `actionBar` 提示对应错误（余额不足 / 购物车空 / 没放银行卡 / 库存不足）且不扣钱

#### Scenario: 清空购物车

- **WHEN** 玩家点「清空购物车」
- **THEN** 客户端 `ShopClientData.cart()` 清空，界面立即刷新（不涉及服务端）

### Requirement: 网络包

系统 SHALL 用三个包承载所有跨端交互，注册进 `flavor_immersed_daily_argo#registerPackets`。

- `ShopSyncPacket(List<Shop>, long serverTime?)`：`playToClient`，客户端收到后写入 `ShopClientData.SHOPS`（含玩家店铺余额刷新）。
- `ShopCheckoutPacket(List<CartLine>)`：`playToServer`，处理器用 `player.containerMenu instanceof MarketMenu menu` 取卡槽/操作槽，调用结账逻辑。
- `ShopManagePacket(Action action, String shopId, String text, int index, int price)`：`playToServer`，`Action = {CREATE, DELETE, ADD_ENTRY, REMOVE_ENTRY, SET_PRICE, RESTOCK, WITHDRAW}`。
- `ShopNetwork`：静态 `writeShops(FriendlyByteBuf, List<Shop>)` / `readShops(FriendlyByteBuf)`，按注册名（`Item` → `ResourceLocation`）编码，避免直接序列化 `Item` 实例。
- 所有服务端处理器在 `context.enqueueWork(...)` 中执行，且用玩家名 / UUID 再校验归属。

#### Scenario: 全量下发

- **WHEN** 任意玩家建店 / 删店 / 改价 / 上架 / 结账导致数据变化
- **THEN** 服务端调用 `ShopManager.syncAll(server)`，所有打开着界面的客户端列表刷新；未打开界面的玩家在下次打开时由登录包获得最新列表

## MODIFIED Requirements

### Requirement: 经济配置（EconomyConfig）

`EconomyConfig` SHALL 在 `exchange` / `takeout` 之外**新增** `shop` 段，并提供静态读取方法与 `FriendlyByteBuf` 读写（供配置项 `extra_entries` 下发用），既有键与格式不变。

### Requirement: 创造模式标签页

`economy_life` 分区计数 SHALL 由 18 改为 19，并在 `ModItems.CATERING_BUSINESS_LICENSE.get()` 之后追加 `output.accept(ModItems.SHOPPING_COMPUTER.get())`（经济系列内）。

### Requirement: 菜单 / 界面注册

`ModMenus` SHALL 新增 `MARKET_MENU`；`flavor_immersed_daily_argoClient#registerScreens` SHALL 注册 `ModMenus.MARKET_MENU.get() → MarketScreen::new`。

## REMOVED Requirements

无。

## 验收与验证

- `.\gradlew compileJava --offline -q`（项目根，PowerShell）通过。
- `.\gradlew runData --offline -q` 通过且无 `removed stale`；`src/generated/resources/assets/flavor_immersed_daily_argo/models/item/shopping_computer.json` 存在（由 `withExistingParent` 生成）。
- 手动检查：blockstate 四个 variant 齐全且 `y` 与上表一致；`models/block/shopping_computer.json` 能被解析（元素含 `rotation`、`uv`、`texture_size`）。
- 逻辑自查：结账四类失败分支均不扣钱；玩家店铺库存下限 0；`withdraw` 只写卡面账户不清空店内库存。
