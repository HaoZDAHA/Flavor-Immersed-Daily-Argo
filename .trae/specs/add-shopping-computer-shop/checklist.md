# 验收清单（add-shopping-computer-shop）

## 方块与资源

- [x] `shopping_computer` 已在 `ModBlocks` 注册，属性 `strength(2.0F).noOcclusion().sound(SoundType.METAL)`。
- [x] `FACING` 状态存在，摆放取 `getHorizontalDirection().getOpposite()`，`rotate`/`mirror` 已实现。
- [x] blockstate 四个 variant：`north→y=90`、`east→180`、`south→270`、`west→0`。
- [x] `models/block/shopping_computer.json` 含 `parent: block/block`、`texture_size`、`elements`（uv 原样），贴图指向 `flavor_immersed_daily_argo:block/shopping_computer`。
- [x] `textures/block/shopping_computer.png` 已拷贝。
- [x] `getShape` / `getCollisionShape` 返回 `BASE ∪ 屏幕盒`，且屏幕盒随 `FACING` 与模型旋转一致（`Shapes.or` + `shapeFor(state)`）。

## 物品与创造栏

- [x] `ModItems.SHOPPING_COMPUTER` 为 `TooltipBlockItem`，tooltip 键存在中/英。
- [x] `economy_life` 分区计数为 `19`，`ModCreativeTabs` 已 `output.accept`。
- [x] `FIDItemModelProvider` 已 `withExistingParent("shopping_computer", modLoc("block/shopping_computer"))`。

## 数据与配置

- [x] `data/.../shop/star_farm_market.json` 与 `mineral_market.json` 语法正确、条目价格/数量符合要求。
- [x] `data/.../tags/item/livestock_spawn_eggs.json` 含 8 个刷怪蛋 id。
- [x] `ShopDataLoader` 仅注册到服务端（`ShopDataEvents` 走 `AddReloadListenerEvent`），`tag` 条目加载期正确展开。
- [x] `EconomyConfig` 的 `shop` 段三个键存在且默认值正确（`true` / `3` / `[]`）。

## 交互与联机

- [x] 空手/持物右键商业电脑均能打开界面，持物不会误触发其他交互。
- [x] 网购页：加购物车、增减数量、清空购物车、结账流程可用。
- [x] 结账失败分支（无卡 / 购物车空 / 余额不足 / 库存不足）均 `actionBar` 提示且不扣钱。
- [x] 开店页：创建/删除/上架/下架/改价/补货/提现可用，非店主不能操作他人店铺（`findOwned` 校验归属）。
- [x] 玩家店铺库存售完为 0 不再可买；提现只写卡面账户，不清空库存。
- [x] 任何店铺数据变化都会广播 `ShopSyncPacket`，其他在线玩家界面刷新。
- [x] 界面不持有任何物品，关闭界面不会造成物品丢失（无槽位）。

## 构建

- [x] `.\gradlew compileJava --offline -q` 通过。
- [x] `.\gradlew runData --offline -q` 通过且无 `removed stale`（`removed stale: 0`）。
- [x] `src/generated/resources/assets/flavor_immersed_daily_argo/models/item/shopping_computer.json` 存在。

## 界面改版（第二轮）

- [x] `MarketMenu` 不含任何槽位，窗口为 340×200（又宽又矮，不画玩家背包）。
- [x] `MarketScreen` 左列 130、右列 186，行高 20、每页 6 行；条目行第一行为「图标 + 数量 + 库存 + 价格」，
      第二行为「`- [n] +`」，两行错位排布，不存在文字互相重叠。
- [x] 点「结账」弹出选卡面板：列出背包（含副手）里所有银行卡，显示主人与余额；点卡片行即付款。
- [x] 点「提现」使用同一个选卡面板，点卡片行即把营业额提现进该卡。
- [x] 选卡面板打开时页面控件不再响应；「取消」或 ESC 只关面板，不关整个界面。
- [x] 上架 / 补货改为取主手整叠物品；空手时提示 `message.shop.no_goods`。
- [x] 结账失败（没选到卡 / 空车 / 余额不足 / 库存不足）仍有 actionBar 提示且不扣钱。

## Bug 修复（第三轮）

- [x] 结账成功后 `player.inventoryMenu.broadcastFullState()` 被调用，买到的物品立刻出现在背包，
      不必等关闭界面（上架 / 补货取走主手物品同理）。
- [x] 结账成功 / 失败都会收到 `ShopCheckoutResultPacket`，反馈直接显示在窗口底部（绿 = 成功，红 = 失败），
      3.5 秒后自动消失。
- [x] 结账成功时客户端购物车自动清空。
