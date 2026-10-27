# 任务清单（add-shopping-computer-shop）

> 约定：Java 代码放在 `com.flavor_immersed_daily_argo` 下；中文注释照抄邻近同类文件（`AtmBlock` / `AtmMenu` / `AtmScreen` / `SeatManager`）。
> 每个 Task 完成后执行 `.\gradlew compileJava --offline -q`（项目根，PowerShell）确认可编译。

- [x] Task 1: 方块资源与注册（shopping_computer）
  - [x] SubTask 1.1: 新建 `src/main/java/com/flavor_immersed_daily_argo/economy/ShoppingComputerBlock.java`
  - [x] SubTask 1.2: 修改 `all/ModBlocks.java`：在 `CATERING_BUSINESS_LICENSE` 之后新增 `SHOPPING_COMPUTER`
  - [x] SubTask 1.3: 新增方块资源（手写：`models/block/shopping_computer.json`、`blockstates/shopping_computer.json`、`textures/block/shopping_computer.png`）

- [x] Task 2: 店铺数据层
  - [x] SubTask 2.1: 新建 `economy/shop/ShopEntry.java`
  - [x] SubTask 2.2: 新建 `economy/shop/Shop.java`
  - [x] SubTask 2.3: 新建 `economy/shop/ShopDataLoader.java`
  - [x] SubTask 2.4: 新建 `economy/shop/ShopManager.java`
  - [x] SubTask 2.5: 新建 `economy/shop/ShopClientData.java`（+ `CartLine.java`）
  - [x] SubTask 2.6: 修改 `economy/EconomyConfig.java`：新增 `shop` 段 + `configShop()`

- [x] Task 3: 网络包
  - [x] SubTask 3.1: 新建 `economy/shop/ShopNetwork.java`
  - [x] SubTask 3.2: 新建 `economy/shop/ShopSyncPacket.java`
  - [x] SubTask 3.3: 新建 `economy/shop/ShopCheckoutPacket.java`（结账逻辑在 `ShopManager.checkout`）
  - [x] SubTask 3.4: 新建 `economy/shop/ShopManagePacket.java`
  - [x] SubTask 3.5: 修改 `flavor_immersed_daily_argo.java#registerPackets`：注册三个包

- [x] Task 4: 菜单与界面
  - [x] SubTask 4.1: 修改 `all/ModMenus.java`：新增 `MARKET_MENU`
  - [x] SubTask 4.2: 新建 `economy/MarketMenu.java`
  - [x] SubTask 4.3: 新建 `economy/MarketScreen.java`（三页签 + 购物车 + 开店管理）
  - [x] SubTask 4.4: 修改 `flavor_immersed_daily_argoClient.java#registerScreens`

- [x] Task 5: 注册收尾与物品
  - [x] SubTask 5.1: 修改 `all/ModItems.java`：新增 `SHOPPING_COMPUTER`
  - [x] SubTask 5.2: 修改 `all/ModCreativeTabs.java`：`economy_life` 计数 `18` → `19` + accept
  - [x] SubTask 5.3: 修改 `datagen/FIDItemModelProvider.java`
  - [x] SubTask 5.4: 新增 `economy/ShopDataEvents.java`（`AddReloadListenerEvent` 注册 `ShopDataLoader`）

- [x] Task 6: 默认数据包
  - [x] SubTask 6.1: 新建 `data/flavor_immersed_daily_argo/shop/star_farm_market.json`
  - [x] SubTask 6.2: 新建 `data/flavor_immersed_daily_argo/shop/mineral_market.json`
  - [x] SubTask 6.3: 新建 `data/flavor_immersed_daily_argo/tags/item/livestock_spawn_eggs.json`

- [x] Task 7: 语言键
  - [x] SubTask 7.1: 修改 `lang/zh_cn.json` 与 `datagen/lang/ModLangs.java`（en_us）

- [x] Task 8: 构建与数据生成验证
  - [x] SubTask 8.1: `.\gradlew compileJava --offline -q` 通过
  - [x] SubTask 8.2: `.\gradlew runData --offline -q` 通过（`removed stale: 0`，`models/item/shopping_computer.json` 已生成）
  - [x] SubTask 8.3: 复核 blockstate 四个 variant 与碰撞箱朝向一致、默认店铺 json 与 tag 语法正确
  - [x] SubTask 8.4: 修复「删除店铺」在列表视图不可用的问题（改到管理视图内，按钮指向当前 `manageId`）

- [x] Task 9: 收尾
  - [x] SubTask 9.1: 把「商业电脑 + 店铺系统」的约定与教训追加到 `project_memory.md`

- [x] Task 10: 界面改版（用户反馈：上下太长、左右太窄、文字叠在一起；不要物品栏，结账时选银行卡）
  - [x] SubTask 10.1: `MarketMenu` 去掉全部槽位（银行卡槽 / 操作槽 / 玩家背包），窗口改为 340×200
  - [x] SubTask 10.2: `MarketScreen` 重排：左列 130 宽（店铺 / 管理条目）、右列 186 宽（条目 / 管理按钮），
        行高 20、每页 6 行，条目行改两行式布局（第一行 图标+数量+库存+价格，第二行 `- [n] +`），文字不再重叠
  - [x] SubTask 10.3: 新增银行卡选择面板（模态覆盖层）：点「结账」或「提现」时扫描背包列出所有银行卡
        （含副手），点卡片行即付款 / 提现，点「取消」或 ESC 关闭
  - [x] SubTask 10.4: `ShopCheckoutPacket` 增加 `cardSlot`；`ShopManager.checkout` 改为按背包槽位取卡
  - [x] SubTask 10.5: 上架 / 补货改为取「主手整叠物品」；`ShopManagePacket.WITHDRAW` 用 `index` 携带银行卡槽位；
        主类新增 `takeMainHand` 与 `no_goods` 提示
  - [x] SubTask 10.6: 语言键增补（`pick_shop_hint` / `drag_item_hint` / `choose_card` / `choose_card_withdraw` /
        `cancel` / `no_card` / `card_anonymous` / `no_goods`），中英各一份；`list_item` / `restock` / `shop_name_hint` 文案更新
  - [x] SubTask 10.7: `.\gradlew compileJava --offline -q` 与 `.\gradlew runData --offline -q` 复验通过

- [x] Task 11: 修复「选卡后没反应、关掉界面才看到物品」
  - [x] SubTask 11.1: `ShopManager.checkout` 成功后调用 `player.inventoryMenu.broadcastFullState()`，
        并给上架 / 补货后的主类处理器也加上，解决「容器界面开着时玩家自己背包不同步」的问题
  - [x] SubTask 11.2: 新增 `ShopCheckoutResultPacket(boolean success, Component message)`（服务端 → 客户端）
        并在主类 `playToClient` 注册；`ShopManager.message` 与结账成功分支都发这个回执
  - [x] SubTask 11.3: `ShopClientData.applyResult` 存反馈（3.5 秒过期）并在成功时清空购物车；
        `MarketScreen` 把反馈画在窗口底部（绿 = 成功 / 红 = 失败），不再依赖看不见的 actionBar
  - [x] SubTask 11.4: 新增 `message...shop.checked_out` 中英语言键；`compileJava` 与 `runData` 复验通过

# Task Dependencies

- Task 2 依赖 Task 3.1 的 `ShopNetwork` 编码约定（可先定接口签名，Task 3.1 与 2.x 交替推进）。
- Task 3 依赖 Task 2 的 `Shop` / `ShopEntry` / `ShopManager` / `ShopClientData`。
- Task 4 依赖 Task 2、Task 3、Task 1.1（`ShoppingComputerBlock.open`）。
- Task 5 依赖 Task 1.2（`ModBlocks.SHOPPING_COMPUTER`）与 Task 4.1（`ModMenus.MARKET_MENU`）。
- Task 6 / 7 可与 Task 2–5 并行。
- Task 8 依赖 Task 1–7 全部完成。
