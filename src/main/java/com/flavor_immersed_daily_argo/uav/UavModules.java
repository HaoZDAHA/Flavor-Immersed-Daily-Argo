package com.flavor_immersed_daily_argo.uav;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.entity.Uav;
import com.flavor_immersed_daily_argo.herding.ChickenCoopBlockEntity;
import com.flavor_immersed_daily_argo.herding.CoopEggs;
import com.flavor_immersed_daily_argo.herding.FeedingTroughBlock;
import com.flavor_immersed_daily_argo.herding.FeedingTroughBlockEntity;
import com.flavor_immersed_daily_argo.item.SickleItem;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.IItemHandler;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// 无人机模块的具体行为。
//
// 看护 / 收获属于「自由模式」：遥控器上圈出两个对角坐标得到一个范围，无人机自己飞到范围中心，
// 每隔 Uav#WORK_INTERVAL 刻照着 #tickCare / #tickHarvest 干一次活。
//
// 战斗属于「附身」：玩家第一人称操作无人机时，左键发射、右键先试着食用使用槽里的食物。
// 发射基本都交给物品自己的逻辑去完成，消耗的是无人机背包里的箭矢 / 弹药 / 投掷物
// （见 #combatFire）。
//
// 所有取用都从无人机自己的槽位（模组槽 + 使用槽 + 4 个背包槽）里扣，所以它得先被喂饱补给。
public final class UavModules {

// 一次工作最多催熟几株作物（免得一屏作物瞬间长满）
    private static final int BONEMEAL_BUDGET = 2;
// 收获模块两次扫描目标之间隔多少 tick
    private static final int TARGET_SCAN_INTERVAL = 10;
// 无人机离目标方块多近就算到了（格）；贴着干活，别看着像隔空取物
    private static final double WORK_REACH = 2.0D;
// 找容器时把记录范围往外扩几格：箱子常常就摆在田边
    private static final int CONTAINER_MARGIN = 8;
// 背上最多攒多少摞收获物就先送去容器，免得一直收的田里永远不送货
    private static final int CARRIED_LIMIT = 16;

    private UavModules() {
    }

// 看护模块：自由模式

// 给范围内的食槽补水 / 添食、给作物施肥、把鸡窝里的蛋挪进孵化格
    public static void tickCare(ServerLevel level, Uav uav, BlockPos min, BlockPos max) {
        fillTroughs(level, uav, min, max);
        fertilize(level, uav, min, max);
        moveEggsIntoHatch(level, min, max);
    }

// 找范围内的食槽：缺了就按槽里已有的种类（空槽则任意）从背包补上一摞
    private static void fillTroughs(ServerLevel level, Uav uav, BlockPos min, BlockPos max) {
        SimpleContainer inventory = uav.getInventory();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!(level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough)) {
                continue;
            }
            ItemStack slot = trough.getInventory().getItem(FeedingTroughBlockEntity.SLOT);
            FeedingTroughBlock.TroughContent content = FeedingTroughBlock.contentFor(slot);
            if (content != null && slot.getCount() >= slot.getMaxStackSize()) {
                continue; // 槽已经满了
            }
            ItemStack stock = findTroughStock(inventory, content);
            if (stock.isEmpty()) {
                continue;
            }
// insert 会把 stock 这一摞按需倒进去并缩短它
            trough.insert(stock);
        }
    }

// 从无人机背包里找一份能进食槽的东西：槽里已有种类就补同一种，空槽则任意
    private static ItemStack findTroughStock(
            SimpleContainer inventory, @Nullable FeedingTroughBlock.TroughContent content) {
        for (int i = Uav.USE_SLOT; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            FeedingTroughBlock.TroughContent candidate = FeedingTroughBlock.contentFor(stack);
            if (candidate == null || (content != null && candidate != content)) {
                continue;
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }

// 给范围内还没长成的作物撒肥料
    private static void fertilize(ServerLevel level, Uav uav, BlockPos min, BlockPos max) {
        SimpleContainer inventory = uav.getInventory();
        int budget = BONEMEAL_BUDGET;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (budget <= 0) {
                return;
            }
            ItemStack fertilizer = findFertilizer(inventory);
            if (fertilizer.isEmpty()) {
                return; // 没肥料了，收工
            }
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BonemealableBlock crop)
                    || !crop.isValidBonemealTarget(level, pos, state, false)
                    || !crop.isBonemealSuccess(level, level.random, pos, state)) {
                continue;
            }
            crop.performBonemeal(level, level.random, pos, state);
            fertilizer.shrink(1);
            budget--;
        }
    }

// 背包里的肥料：原版骨粉，或本模组的有机肥料
    private static ItemStack findFertilizer(SimpleContainer inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.BONE_MEAL) || stack.is(ModItems.ORGANIC_FERTILIZER.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

// 把鸡窝收集槽里的蛋挪一个进孵化格（只挪能孵出禽类的蛋）
    private static void moveEggsIntoHatch(ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!(level.getBlockEntity(pos) instanceof ChickenCoopBlockEntity coop)) {
                continue;
            }
            SimpleContainer inventory = coop.getInventory();
            ItemStack hatch = inventory.getItem(ChickenCoopBlockEntity.HATCH_SLOT);
            for (int i = 1; i < ChickenCoopBlockEntity.SLOT_COUNT; i++) {
                ItemStack egg = inventory.getItem(i);
                if (egg.isEmpty() || CoopEggs.entityFor(egg) == null) {
                    continue;
                }
// 孵化格里有别的种类的蛋就先不动，免得混在一起
                if (!hatch.isEmpty() && !ItemStack.isSameItemSameTags(hatch, egg)) {
                    continue;
                }
                if (hatch.isEmpty()) {
                    inventory.setItem(ChickenCoopBlockEntity.HATCH_SLOT, egg.copyWithCount(1));
                } else {
                    hatch.grow(1);
                }
                egg.shrink(1);
                inventory.setChanged();
                break;
            }
        }
    }

// 收获模块：自由模式

// 收获模块：无人机自己飞过去干活，而不是隔空收。
//
// 每次挑范围内离自己最近的一株成熟作物 / 挂果树叶，飞到它正上方再动手；田里没有可收的就飞到
// 最近的容器（记录范围往外多找 #CONTAINER_MARGIN 格）把背上的东西塞进去，实在找不到容器
// 就丢在脚下。作物走 SickleItem#isMatureCrop 那套通用判据（原版与模组作物都认），果树则是
// 「方块的 fruiting 属性为真」——摘取直接借用方块自己的空手右键逻辑，不必知道它掉什么果实。
    public static void tickHarvest(ServerLevel level, Uav uav, BlockPos min, BlockPos max) {
        BlockPos target = uav.getWorkTarget();
        if (target != null) {
            if (!stillWorkable(level, target, uav.isWorkTargetStorage())) {
                uav.setWorkTarget(null, false); // 目标没了（被收走 / 箱子满了），下一 tick 重新找
                return;
            }
            if (uav.position().distanceToSqr(Vec3.atCenterOf(target)) > WORK_REACH * WORK_REACH) {
                return; // 还在路上，flyAuto 会继续把它送过去
            }
            if (uav.isWorkTargetStorage()) {
                emptyLoot(level, uav, target);
            } else {
                harvestOne(level, uav, target);
            }
            uav.setWorkTarget(null, false);
            uav.setWorkCooldown(TARGET_SCAN_INTERVAL);
            return;
        }

        if (uav.getWorkCooldown() > 0) {
            uav.setWorkCooldown(uav.getWorkCooldown() - 1);
            return;
        }
        uav.setWorkCooldown(TARGET_SCAN_INTERVAL);

        if (uav.getCarriedLoot().size() >= CARRIED_LIMIT) {
            deliverOrDrop(level, uav, min, max); // 背满了，先去送货
            return;
        }
        BlockPos next = findHarvestTarget(level, uav, min, max);
        if (next != null) {
            uav.setWorkTarget(next, false);
            return;
        }
// 田里没得收了：把背上的东西送进最近的容器，找不到就丢在脚下
        if (!uav.getCarriedLoot().isEmpty()) {
            deliverOrDrop(level, uav, min, max);
        }
    }

// 送一趟货：找范围内（含田边几格）最近的容器飞过去，找不到就地把东西丢下
    private static void deliverOrDrop(ServerLevel level, Uav uav, BlockPos min, BlockPos max) {
        BlockPos storage = findNearestStorage(level, uav,
                min.offset(-CONTAINER_MARGIN, -4, -CONTAINER_MARGIN),
                max.offset(CONTAINER_MARGIN, 4, CONTAINER_MARGIN));
        if (storage != null) {
            uav.setWorkTarget(storage, true);
        } else {
            dropCarried(uav);
        }
    }

// 目标还算不算数：容器要还有空位，方块要还是成熟作物 / 挂果树叶
    private static boolean stillWorkable(ServerLevel level, BlockPos pos, boolean storage) {
        if (storage) {
            Container container = containerAt(level, pos);
            if (container != null) {
                return hasSpace(container);
            }
            IItemHandler handler = handlerAt(level, pos);
            return handler != null && hasSpace(handler);
        }
        BlockState state = level.getBlockState(pos);
        return SickleItem.isMatureCrop(state) || isFruiting(state);
    }

// 范围内离无人机最近的成熟作物 / 挂果树叶
    @Nullable
    private static BlockPos findHarvestTarget(ServerLevel level, Uav uav, BlockPos min, BlockPos max) {
        BlockPos origin = uav.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (!SickleItem.isMatureCrop(state) && !isFruiting(state)) {
                continue;
            }
            double distance = pos.distSqr(origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

// 收掉脚下这一格：作物先让它自己按右键收，挂果树叶也借方块自己的右键逻辑摘
    private static void harvestOne(ServerLevel level, Uav uav, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Player owner = ownerOrFake(level, uav);
        if (SickleItem.isMatureCrop(state)) {
            harvestCrop(level, owner, pos, state, uav.getCarriedLoot());
        } else {
            harvestFruit(level, owner, pos, state, uav.getCarriedLoot());
        }
    }

// 把背上的收获物倒进这个方块。
//
// 优先直接写方块自己的 Container（箱子界面读的就是这份数据），只有方块本身
// 不是容器时（别的模组的机器）才退回按物品能力塞。
//
// 塞完以容器里「实际多出来多少个」为准：有些处理器收下了却不往容器里存，
// 这类差额会补丢在无人机脚下，绝不会凭空消失。
    private static void emptyLoot(ServerLevel level, Uav uav, BlockPos pos) {
        Container container = containerAt(level, pos);
        IItemHandler handler = container == null ? handlerAt(level, pos) : null;
        if (container == null && handler == null) {
            dropCarried(uav);
            return;
        }
        List<ItemStack> given = new ArrayList<>();
        for (ItemStack stack : uav.getCarriedLoot()) {
            given.add(stack.copy());
        }
        int before = container != null ? count(container) : count(handler);
        for (ItemStack stack : uav.getCarriedLoot()) {
            ItemStack remainder = container != null ? insert(container, stack) : insert(handler, stack);
            stack.setCount(remainder.getCount());
        }
        int accepted = (container != null ? count(container) : count(handler)) - before;
        uav.getCarriedLoot().clear();
        for (ItemStack stack : given) {
            if (accepted >= stack.getCount()) {
                accepted -= stack.getCount();
                continue;
            }
            uav.dropItem(stack.copyWithCount(stack.getCount() - accepted));
            accepted = 0;
        }
    }

// 方块自己的容器（箱子 / 木桶 / 潜影盒 / 熔炉都算），没有就返回 null
    @Nullable
    private static Container containerAt(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container container ? container : null;
    }

    private static int count(Container container) {
        int total = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            total += container.getItem(i).getCount();
        }
        return total;
    }

// 数一数容器里现在有多少个物品（用来核对塞进去的东西是不是真的在里面）
    private static int count(IItemHandler handler) {
        int total = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            total += handler.getStackInSlot(i).getCount();
        }
        return total;
    }

// 把背上的收获物全丢在无人机脚下
    private static void dropCarried(Uav uav) {
        for (ItemStack stack : uav.getCarriedLoot()) {
            uav.dropItem(stack);
        }
        uav.getCarriedLoot().clear();
    }

// 收一株成熟作物。
//
// 先像玩家右键那样交给方块自己收：模组的作物（如 FIDCropBlock）是在右键里直接
// Block.popResource 掉果实的，战利品表反而是空的，只按掉落表收就会「作物没了、东西也没了」。
// 它刚丢到地上的掉落物由 #collectNewDrops 接住，跟摘果子走同一套。
//
// 方块不认右键的（原版小麦、胡萝卜这些只靠战利品表）再退回按掉落表收，然后补种回幼苗。
    private static void harvestCrop(
            ServerLevel level, @Nullable Player owner, BlockPos pos, BlockState state, List<ItemStack> collected) {
        AABB area = new AABB(pos).inflate(1.5D);
        Set<Integer> existing = itemEntityIds(level, area);
        InteractionResult result = state.use(level, owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        collectNewDrops(level, area, existing, collected);
        if (result.consumesAction()) {
            return; // 方块自己收过、也自己重置成幼苗了
        }
        for (ItemStack drop : Block.getDrops(state, level, pos,
                level.getBlockEntity(pos), owner, ItemStack.EMPTY)) {
            if (!drop.isEmpty()) {
                collected.add(drop);
            }
        }
        level.setBlock(pos, SickleItem.replantedState(state), Block.UPDATE_ALL);
    }

// 摘一丛挂着果的果树方块：调用方块自己的空手右键（模组的挂果树叶就是这么把果子摘下来的），
// 再把它刚丢到地上的果实接住，好一起送进存储方块。摘到了返回 true。
    private static boolean harvestFruit(
            ServerLevel level, @Nullable Player owner, BlockPos pos, BlockState state, List<ItemStack> collected) {
        if (!isFruiting(state)) {
            return false;
        }
        AABB area = new AABB(pos).inflate(1.5D);
        Set<Integer> existing = itemEntityIds(level, area);
        state.use(level, owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        collectNewDrops(level, area, existing, collected);
        return true;
    }

// 把这块区域里新冒出来的掉落物收进 collected 并抹掉实体，免得散一地
    private static void collectNewDrops(ServerLevel level, AABB area, Set<Integer> existing, List<ItemStack> collected) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (existing.contains(item.getId())) {
                continue;
            }
            collected.add(item.getItem().copy());
            item.discard();
        }
    }

// 方块状态里名为 fruiting 的布尔属性（挂果标记），没有则返回 null
    @Nullable
    private static BooleanProperty fruitingProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty bool && property.getName().equals("fruiting")) {
                return bool;
            }
        }
        return null;
    }

// 这格是不是挂着果（fruiting 为真）
    private static boolean isFruiting(BlockState state) {
        BooleanProperty fruiting = fruitingProperty(state);
        return fruiting != null && state.getValue(fruiting);
    }

// 这块区域里现有掉落物的实体 id，用来认出摘果子时新冒出来的那几个
    private static Set<Integer> itemEntityIds(ServerLevel level, AABB area) {
        Set<Integer> ids = new HashSet<>();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            ids.add(item.getId());
        }
        return ids;
    }

// 无人机的主人（在线才有），摘果子时借他的身份给方块逻辑用
    @Nullable
    private static Player ownerOf(ServerLevel level, Uav uav) {
        UUID bound = uav.getBoundPlayer().orElse(null);
        return bound == null ? null : level.getServer().getPlayerList().getPlayer(bound);
    }

// 干活时借用的玩家：优先用主人本人（有些方块会把成品直接塞进他背包，看得见也拿得回），
// 主人不在线就用替身玩家，保证方块逻辑里拿到的一定不是 null。
    private static Player ownerOrFake(ServerLevel level, Uav uav) {
        Player owner = ownerOf(level, uav);
        return owner != null ? owner : FakePlayerFactory.get(level, new GameProfile(uav.getUUID(), "[UAV]"));
    }

// 范围内离无人机最近、还放得下东西的存储方块（箱子 / 木桶 / 漏斗，以及别的模组带物品能力的方块）
    @Nullable
    private static BlockPos findNearestStorage(ServerLevel level, Uav uav, BlockPos min, BlockPos max) {
        BlockPos origin = uav.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!hasSpaceAt(level, pos)) {
                continue;
            }
            double distance = pos.distSqr(origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

    private static boolean hasSpaceAt(ServerLevel level, BlockPos pos) {
        Container container = containerAt(level, pos);
        if (container != null) {
            return hasSpace(container);
        }
        IItemHandler handler = handlerAt(level, pos);
        return handler != null && hasSpace(handler);
    }

// 方块上的物品能力：1.20.1 没有 level.getCapability(cap, pos, side)，要先取方块实体再问它
    @Nullable
    private static IItemHandler handlerAt(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null
                ? null
                : blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
    }

    private static boolean hasSpace(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slot = container.getItem(i);
            int limit = Math.min(container.getMaxStackSize(), slot.isEmpty() ? 64 : slot.getMaxStackSize());
            if (slot.getCount() < limit) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSpace(IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            int limit = Math.min(handler.getSlotLimit(i), slot.isEmpty() ? 64 : slot.getMaxStackSize());
            if (slot.getCount() < limit) {
                return true;
            }
        }
        return false;
    }

// 直接往方块自己的容器里塞（箱子界面读的就是这份数据），返回塞不下的部分
    private static ItemStack insert(Container container, ItemStack stack) {
        ItemStack remaining = stack.copy();
        int containerLimit = container.getMaxStackSize();
// 第一轮并到同类物品上，第二轮才占空槽
        for (int pass = 0; pass < 2 && !remaining.isEmpty(); pass++) {
            for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
                ItemStack slot = container.getItem(i);
                if (pass == 0) {
                    if (slot.isEmpty() || !ItemStack.isSameItemSameTags(slot, remaining)) {
                        continue;
                    }
                    int space = Math.min(containerLimit, slot.getMaxStackSize()) - slot.getCount();
                    if (space <= 0) {
                        continue;
                    }
                    int move = Math.min(space, remaining.getCount());
                    slot.grow(move);
                    container.setItem(i, slot);
                    remaining.shrink(move);
                } else {
                    if (!slot.isEmpty() || !container.canPlaceItem(i, remaining)) {
                        continue;
                    }
                    int move = Math.min(remaining.getCount(),
                            Math.min(containerLimit, remaining.getMaxStackSize()));
                    container.setItem(i, remaining.copyWithCount(move));
                    remaining.shrink(move);
                }
            }
        }
        container.setChanged();
        return remaining;
    }

// 逐格塞进存储方块，返回塞不下的部分
    private static ItemStack insert(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack;
        for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
            remaining = handler.insertItem(i, remaining, false);
        }
        return remaining;
    }

// 战斗模块：附身

// 使用槽往后的槽位（使用槽 + 4 个背包槽），开火时整段借给替身玩家当背包
    private static final int SHARED_SLOTS = Uav.SLOT_COUNT - Uav.USE_SLOT;
// 枪口离无人机中心多远（格）：必须比无人机碰撞箱的半对角线大。
//
// TaCZ 判实体命中只看 isPickable()（不看 canBeHitByProjectile()），
// 而它的子弹就生成在射手的 x / z 上——要是把替身玩家摆在机身正中央，往下一压枪，
// 弹道就穿过无人机自己了。
    private static final double MUZZLE_OFFSET = 1.5D;
// TaCZ 的子弹取的是「眼睛再往下 0.1」，摆替身玩家时要把这一截按回去
    private static final double BULLET_UNDER_EYE = 0.1D;
// 替身玩家手上那格放着无人机使用槽里的武器，剩下的格子才是背包
    private static final int SHOOTER_WEAPON_SLOT = 0;

// 附身时右键：使用槽里是可食用食物就吃掉，否则当发射用
    public static boolean combatUse(ServerLevel level, Uav uav, Player player) {
        ItemStack use = uav.getInventory().getItem(Uav.USE_SLOT);
        FoodProperties food = use.getItem().getFoodProperties();
        if (food != null && player.canEat(food.canAlwaysEat())) {
            player.eat(level, use);
            return true;
        }
        return combatFire(level, uav);
    }

// 附身时发射一发。
//
// 分三档，越往后越通用：
//
// 弓 / 弩这种「要蓄力才算数」的，由我们代为发射，弹药从无人机背包里掏；
// TaCZ 的枪不重写 use，只能叫它自己的开火接口（见 TaczGunCompat）；
// 其余全部把使用槽里那件东西交给它自己的 use——模组投掷物、右键能开火的枪械
// 都在这条路上，不用为每种弹射物各写一套。
    public static boolean combatFire(ServerLevel level, Uav uav) {
        ItemStack weapon = uav.getInventory().getItem(Uav.USE_SLOT);
        if (weapon.isEmpty()) {
            return false;
        }
        if (weapon.getItem() instanceof ProjectileWeaponItem) {
            return shootArrow(level, uav, weapon);
        }
        if (TaczGunCompat.isGun(weapon)) {
            return withShooter(level, uav, (shooter, ignored) -> TaczGunCompat.fire(shooter, weapon, uav));
        }
        return withShooter(level, uav, (shooter, ignored) -> shooter.getMainHandItem()
                .use(level, shooter, InteractionHand.MAIN_HAND).getResult().consumesAction());
    }

// 枪口位置：从无人机中心沿机头方向往前推 #MUZZLE_OFFSET 格
    private static Vec3 muzzleOf(Uav uav) {
        return uav.position().add(Vec3.directionFromRotation(uav.getXRot(), uav.getYRot()).scale(MUZZLE_OFFSET));
    }

// 弓 / 弩：从无人机背包掏一根箭，按无人机朝向直接射出去
    private static boolean shootArrow(ServerLevel level, Uav uav, ItemStack weapon) {
        ItemStack ammo = findArrow(uav.getInventory());
        if (ammo.isEmpty()) {
            return false;
        }
        Vec3 muzzle = muzzleOf(uav);
// 1.20.1 的 Arrow 没有「带弹药 / 武器」的构造器：先按坐标造出来，再补上归属、
// 药水效果与伤害加成（武器只影响基础伤害，这里按无人机的空手伤害算）
        Arrow arrow = new Arrow(level, muzzle.x, muzzle.y, muzzle.z);
        arrow.setOwner(uav);
        arrow.setEffectsFromItem(ammo.copyWithCount(1));
        arrow.shootFromRotation(uav, uav.getXRot(), uav.getYRot(), 0.0F, 3.0F, 1.0F);
        level.addFreshEntity(arrow);
        ammo.shrink(1);
        level.playSound(null, muzzle.x, muzzle.y, muzzle.z,
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

// 无人机背包里的箭
    private static ItemStack findArrow(SimpleContainer inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

// 一次「借用替身玩家来开火」的动作
    private interface ShotAction {
        boolean fire(FakePlayer shooter, SimpleContainer inventory);
    }

// 借一个站在机头前方、朝无人机方向、背着无人机背包的替身玩家（FakePlayer）来开火。
//
// 站位刻意放在机身外面的枪口上（见 #MUZZLE_OFFSET），弹射物才不会一出手
// 就命中无人机自己；借过去的背包是同一批 ItemStack，弹药从「玩家背包」里扣掉之后
// 也就直接落回无人机背包。
    private static boolean withShooter(ServerLevel level, Uav uav, ShotAction action) {
        SimpleContainer inventory = uav.getInventory();
        FakePlayer shooter = shooterFor(level, uav);
        Inventory shooterInventory = shooter.getInventory();

// 弹射物是按射手的眼睛（TaCZ 再往下 0.1）生成的，所以把脚底按回去，让弹着点正好在枪口
        Vec3 muzzle = muzzleOf(uav);
        shooter.moveTo(muzzle.x, muzzle.y + BULLET_UNDER_EYE - shooter.getEyeHeight(), muzzle.z,
                uav.getYRot(), uav.getXRot());
        shooter.setYHeadRot(uav.getYRot());
// 手上那格不还回去：TaCZ 靠 currentGunItem 认「还是同一杆枪」，每发都还回去会被
// 当成换枪而 reset 射速计时（见 TaczGunBridge）。
        shooterInventory.setItem(SHOOTER_WEAPON_SLOT, inventory.getItem(Uav.USE_SLOT));
        for (int i = SHOOTER_WEAPON_SLOT + 1; i < SHARED_SLOTS; i++) {
            shooterInventory.setItem(i, inventory.getItem(Uav.USE_SLOT + i));
        }
        shooterInventory.selected = SHOOTER_WEAPON_SLOT;
        try {
            return action.fire(shooter, inventory);
        } finally {
            for (int i = SHOOTER_WEAPON_SLOT + 1; i < SHARED_SLOTS; i++) {
                inventory.setItem(Uav.USE_SLOT + i, shooterInventory.getItem(i));
                shooterInventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

// 每台无人机配一个替身玩家。
//
// TaCZ 的射速计时、拉栓状态都记在替身自己身上，全服共用一个替身会让两台无人机的
// 状态互相打架，所以按无人机的 UUID 各取一个。
    private static FakePlayer shooterFor(ServerLevel level, Uav uav) {
        return FakePlayerFactory.get(level, new GameProfile(uav.getUUID(), "[UAV]"));
    }
}
