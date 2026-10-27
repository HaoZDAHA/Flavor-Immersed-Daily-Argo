package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.entity.DeliveryRider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// 小票打印机方块实体。
//
// 内部分成两件事：
//
// 一是「今日菜单」——DeliveryFoodPool 按游戏天数抽出的 40 种食物，灌进一个只展示不取放的
// 容器里供界面读取。菜单只存在内存里，换天（或第一次 tick / 读档）时重算一遍即可，因此容器内容不写档。
//
// 二是「出单」——只有在玩家于界面里把开关打开（#isEnabled()）之后，机器才会在
// state=1 安静一阵子后切到出单态（state=2）：模型上多吐出一张小票，机器附近刷新最多 3 个
// DeliveryRider 外卖小哥。等身边的小哥都走了，就回到常态；把开关关掉则立刻赶走小哥并回到常态。
//
// 容器不实现 WorldlyContainer，也不给方块注册物品能力，因此漏斗无从下手——恰好就是「仅展示」。
public class ReceiptPrinterBlockEntity extends BlockEntity implements MenuProvider {

// 展示槽位数：每天 40 种食物
    public static final int SLOT_COUNT = DeliveryFoodPool.DAILY_COUNT;

// 常态下切出单态前至少要安静这么久
    private static final int MIN_IDLE_TICKS = 200;
// 常态下每 tick 切出单态的概率倒数（平均一分钟出一次单）
    private static final int IDLE_PERIOD = 1200;
// 出单态里每隔这么多 tick 尝试补一个小哥
    private static final int SPAWN_INTERVAL = 60;
// 同时最多几个小哥
    private static final int MAX_RIDERS = 3;
// 统计 / 清理小哥时的搜索半径
    private static final double RIDER_RANGE = 16.0D;
// 出单态至少维持多久才允许因为「没有小哥」而弹回常态
    private static final int STATE2_MIN_TICKS = SPAWN_INTERVAL * 2;

    private static final String TAG_STATE_TICKS = "StateTicks";
    private static final String TAG_ENABLED = "Enabled";

// 今日菜单的展示容器：只展示不取放，漏斗也接不进来
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT);

// 出单开关：默认关着，玩家在界面里打开后机器才会进入出单态
    private boolean enabled;

// 当前菜单对应的游戏天数；Long#MIN_VALUE 表示还没算过
    private long menuDay = Long.MIN_VALUE;
// 当前状态已持续多少 tick
    private int stateTicks;

// 同步给界面的开关读数：0 = 关闭，1 = 打开
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return ReceiptPrinterBlockEntity.this.enabled ? 1 : 0;
        }

        @Override
        public void set(int index, int value) {
            ReceiptPrinterBlockEntity.this.enabled = value != 0;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public ReceiptPrinterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECEIPT_PRINTER_ENTITY.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public ContainerData getData() {
        return this.data;
    }

// 出单开关

// 开关是否打开
    public boolean isEnabled() {
        return this.enabled;
    }

// 拨动开关：开机立刻进入出单态，关机赶走身边的小哥并回到常态
    public void setEnabled(boolean value) {
        if (this.enabled == value) {
            return;
        }
        this.enabled = value;
        if (this.level instanceof ServerLevel serverLevel) {
            this.stateTicks = 0;
            BlockState state = this.getBlockState();
            if (value) {
// 开机：立刻切到出单态，并先派一个小哥出来
                if (state.getValue(ReceiptPrinterBlock.STATE) != 2) {
                    serverLevel.setBlock(this.worldPosition, state.setValue(ReceiptPrinterBlock.STATE, 2), 3);
                }
                this.spawnRider(serverLevel, this.worldPosition);
            } else {
// 关机：赶走身边的小哥并回到常态
                this.discardRiders();
                if (state.getValue(ReceiptPrinterBlock.STATE) != 1) {
                    serverLevel.setBlock(this.worldPosition, state.setValue(ReceiptPrinterBlock.STATE, 1), 3);
                }
            }
        }
        this.setChanged();
    }

// 每日菜单

// 按当前游戏天数把 40 种食物灌进展示容器（同一天只算一次）
    private void refreshMenu(long day) {
        this.menuDay = day;
        List<Item> foods = DeliveryFoodPool.dailyFoods(day);
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.inventory.setItem(i, i < foods.size() ? new ItemStack(foods.get(i)) : ItemStack.EMPTY);
        }
    }

// 从今日菜单里随手拿一样食物（含随机数量）当订单；菜单为空时返回空栈
    private ItemStack buildOrder(RandomSource random) {
        List<ItemStack> foods = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) {
                foods.add(stack);
            }
        }
        if (foods.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack order = foods.get(random.nextInt(foods.size())).copy();
        order.setCount(1 + random.nextInt(3));
        return order;
    }

// 每刻

    public static void serverTick(Level level, BlockPos pos, BlockState state, ReceiptPrinterBlockEntity printer) {
        printer.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        long day = level.getDayTime() / 24000L;
        if (this.menuDay != day) {
            this.refreshMenu(day);
        }

        if (state.getValue(ReceiptPrinterBlock.STATE) == 2) {
            this.tickState2(level, pos, state);
        } else {
            this.tickState1(level, pos, state);
        }
    }

// 常态：开关打开后安静一阵子，有概率切到出单态
    private void tickState1(Level level, BlockPos pos, BlockState state) {
        if (!this.enabled) {
            return;
        }
        this.stateTicks++;
        if (this.stateTicks > MIN_IDLE_TICKS && level.random.nextInt(IDLE_PERIOD) == 0) {
            this.setState(level, pos, state, 2);
            this.stateTicks = 0;
        }
    }

// 出单态：补齐到 3 个外卖小哥；身边一个小哥都没有了就回到常态
    private void tickState2(Level level, BlockPos pos, BlockState state) {
        this.stateTicks++;
        List<DeliveryRider> riders = this.findRiders(level, pos);
        if (riders.isEmpty() && this.stateTicks > STATE2_MIN_TICKS) {
            this.setState(level, pos, state, 1);
            this.stateTicks = 0;
            return;
        }
        if (riders.size() < MAX_RIDERS && this.stateTicks % SPAWN_INTERVAL == 0) {
            this.spawnRider(level, pos);
        }
    }

    private void setState(Level level, BlockPos pos, BlockState state, int value) {
        if (state.getValue(ReceiptPrinterBlock.STATE) != value) {
            level.setBlock(pos, state.setValue(ReceiptPrinterBlock.STATE, value), 3);
        }
    }

// 外卖小哥

// 归属这台机器、且还在附近的小哥
    private List<DeliveryRider> findRiders(Level level, BlockPos pos) {
        return level.getEntitiesOfClass(DeliveryRider.class, new AABB(pos).inflate(RIDER_RANGE),
                rider -> pos.equals(rider.getHome()));
    }

// 派一个小哥出去：挑个能站人的落脚点，随手拿一样当日食物当订单
    private void spawnRider(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack order = this.buildOrder(serverLevel.random);
        if (order.isEmpty()) {
            return;
        }
        DeliveryRider rider = ModEntities.DELIVERY_RIDER.get().create(serverLevel);
        if (rider == null) {
            return;
        }
        BlockPos spot = findSpawnSpot(serverLevel, pos);
        if (spot == null) {
            return;
        }
        long reward = (long) order.getCount() * (3 + serverLevel.random.nextInt(4));
        rider.setHome(pos);
        rider.setOrder(order, reward);
        rider.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D,
                serverLevel.random.nextFloat() * 360.0F, 0.0F);
        serverLevel.addFreshEntity(rider);
    }

// 在机器周围找一格能站人的地面（与机器同高）；找不到返回 null
    @Nullable
    private static BlockPos findSpawnSpot(ServerLevel level, BlockPos pos) {
        for (int attempt = 0; attempt < 16; attempt++) {
            int dx = level.random.nextInt(9) - 4;
            int dz = level.random.nextInt(9) - 4;
            if (dx == 0 && dz == 0) {
                continue;
            }
            BlockPos candidate = pos.offset(dx, 0, dz);
            if (canStand(level, candidate)) {
                return candidate;
            }
        }
        BlockPos above = pos.above();
        return canStand(level, above) ? above : null;
    }

// 该格上方两格空着、脚下是结实地面就能站人
    private static boolean canStand(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

// 机器被拆掉时，把身边还没走的小哥一并清掉
    public void discardRiders() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (DeliveryRider rider : this.findRiders(serverLevel, this.worldPosition)) {
            rider.discard();
        }
    }

// 存读

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_STATE_TICKS, this.stateTicks);
        tag.putBoolean(TAG_ENABLED, this.enabled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
// 展示容器不写档，读档后强制在下一 tick 重算一次菜单，否则容器会一直是空的
        this.menuDay = Long.MIN_VALUE;
        this.stateTicks = tag.getInt(TAG_STATE_TICKS);
        this.enabled = tag.getBoolean(TAG_ENABLED);
    }

// 界面

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.flavor_immersed_daily_argo.receipt_printer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ReceiptPrinterMenu(containerId, playerInventory, this);
    }
}
