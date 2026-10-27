package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.item.UavRangeModuleItem;
import com.flavor_immersed_daily_argo.item.UavControllerItem;
import com.flavor_immersed_daily_argo.uav.UavMenu;
import com.flavor_immersed_daily_argo.uav.UavModules;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 多功能无人机。
//
// 放在地上就是一架静止的小飞机：电源关着的时候旋翼不转，模型也没有动作（导出的 uavno
// 是个空动画，所以「没有动作」就是不播任何动画）。
//
// 状态只有「通没通电」一条主线：
//
// 通电且没人管——原地悬停，旋翼转起来（uavgo 动画）；贴着地面时会先升到悬停高度。
// 断电——旋翼还转着缓缓落回地面，落地停稳才彻底不动。刚放下时电源是关的、也贴在地上，
// 所以「放置出来没有动作」。
// 自动模式——遥控器上输入的两个对角坐标圈出一块范围，无人机飞到范围中心悬停，
// 由模组槽里的看护 / 收获模块在范围内干活（见 UavModules）。
// 第一人称操作——把玩家相机挂到无人机上，按方向键操纵飞行（玩家本人留在原地不动）。
//
// 它是最朴素的原版 Entity：本身没有重力也没有 AI，升 / 降 / 飞全在 #tick 里自己算。
// 服务端算的是权威位置；正在操作的玩家那一端拿同一套逻辑也走一遍（输入键是本地就有的，
// 不必等网络包），所以手感才是即时的。其余客户端只按服务端发来的位置插值，免得各算各的跑偏。
public class Uav extends Entity implements MenuProvider {

// 物品栏

// 模组槽：只放五种无人机模块之一（五选一）
    public static final int MODULE_SLOT = 0;
// 使用槽：无人机干活时取用的东西
    public static final int USE_SLOT = 1;
// 背包槽数量，紧跟在模组槽与使用槽之后
    public static final int BACKPACK_SLOTS = 4;
// 槽位总数：模组槽 + 使用槽 + 背包槽
    public static final int SLOT_COUNT = 2 + BACKPACK_SLOTS;

// 模组槽 + 使用槽 + 背包槽。跟无人机一起存读，被打散时倒出来
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT);

// 悬停时离地多高（格）：从地上通电后会先升到这儿
    private static final double HOVER_HEIGHT = 1.2D;

// 升降速度（格 / tick）
    private static final double VERTICAL_SPEED = 0.06D;

// 手动飞行速度（格 / tick）
    private static final double FLY_SPEED = 0.35D;

// 断电后缓慢落地的速度（格 / tick）
    private static final double LAND_SPEED = 0.04D;

// 自动模式的巡航速度（格 / tick）
    private static final double AUTO_SPEED = 0.25D;

// 自动模式判定「到点」的距离（格）
    private static final double AUTO_ARRIVE = 0.8D;

// 找地面时往下探的最大距离（格）
    private static final double GROUND_SCAN_DOWN = 64.0D;

// 自动范围每一轴的最大跨度（格），免得圈出天那么大一块地每次扫描卡崩
    public static final int MAX_RANGE_SPAN = 24;

// 自由模式的模块两次干活之间的间隔（tick）
    public static final int WORK_INTERVAL = 20;

// 带范围的模块：无人机悬停在较高那个记录点的上方这么多格
    private static final double MODULE_HOVER_RISE = 3.0D;

// 收获模块飞到一个目标方块上方作业时，停在方块上方这么多格（贴着作物干活）
    private static final double WORK_HOVER = 1.1D;

// 带范围的模块的工作范围：从较低记录点往下留 16 格、从较高记录点往上留 8 格。
// 玩家通常点在食槽 / 耕地 / 树叶上，也就是「地面偏上」，所以往下多留一些更稳；
// 两边加起来正好 #MAX_RANGE_SPAN 格。
    private static final int MODULE_BELOW = 16;
    private static final int MODULE_ABOVE = 8;

// 飞行按键位（打包进一个 int 在双端 / 网络间传）

    public static final int KEY_FORWARD = 1;
    public static final int KEY_BACK = 1 << 1;
    public static final int KEY_LEFT = 1 << 2;
    public static final int KEY_RIGHT = 1 << 3;
    public static final int KEY_UP = 1 << 4;
    public static final int KEY_DOWN = 1 << 5;

// 同步数据

// 电源。开着就悬停 / 飞行（转旋翼），关了就往地上落
    private static final EntityDataAccessor<Boolean> DATA_POWERED =
            SynchedEntityData.defineId(Uav.class, EntityDataSerializers.BOOLEAN);

// 遥控器绑给了谁：绑定者右击才会开操作界面
    private static final EntityDataAccessor<Optional<UUID>> DATA_BOUND =
            SynchedEntityData.defineId(Uav.class, EntityDataSerializers.OPTIONAL_UUID);

// 正在第一人称操作的人。客户端靠它把自己的相机挂到无人机上
    private static final EntityDataAccessor<Optional<UUID>> DATA_OPERATOR =
            SynchedEntityData.defineId(Uav.class, EntityDataSerializers.OPTIONAL_UUID);

// 自动模式开关
    private static final EntityDataAccessor<Boolean> DATA_AUTO =
            SynchedEntityData.defineId(Uav.class, EntityDataSerializers.BOOLEAN);

// 高亮标记模块选中的方块。附身时右键方块写入，只给附身的那一端画线框
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_HIGHLIGHT =
            SynchedEntityData.defineId(Uav.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

// 通电时循环播放的 go 动画（旋翼自转 + 机身轻微俯仰）
    public final AnimationState goAnimationState = new AnimationState();

// 本机（客户端）是不是正在操作这架无人机。由 UavClient 每 tick 设置。
//
// 只有操作者这一端才自己跑飞行逻辑（预测），别的客户端老老实实按服务端发来的位置插值，
// 不然每台客户端都照着服务端那份输入算一遍，位置会各走各的。
    public transient boolean clientOperated;

// 当前飞行输入（按键位掩码）：服务端由包写入，操作者客户端直接写
    private int flightKeys;

// 当前机头朝向 / 俯仰：操作者的人转视角就抄过来，服务端也从包里收
    private float flightYaw;
    private float flightPitch;

// 自动范围的两个对角坐标（只在服务端有意义）
    @Nullable private Vec3 routeA;
    @Nullable private Vec3 routeB;

// 自动范围换算出的方块范围（含端点），看护 / 收获模块在里面干活
    @Nullable private BlockPos rangeMin;
    @Nullable private BlockPos rangeMax;

// 上一次从模块读到的两个记录点：模块刚放进槽、或记录点被改写时才会重新套用范围
    @Nullable private BlockPos modulePointA;
    @Nullable private BlockPos modulePointB;

// 带范围的模块还有几 tick 干下一次活（不受飞行状态影响）
    private int workCooldown;

// 收获模块正在赶过去的目标：成熟作物 / 挂果树叶，或者要投料的容器
    @Nullable private BlockPos workTarget;

// 当前目标是容器（到了就把背上的收获物塞进去），否则是作物
    private boolean workTargetIsStorage;

// 收获模块摘下来、还没送进容器的东西
    private final List<ItemStack> carriedLoot = new ArrayList<>();

// 上一 tick 模组槽里有没有本模组的工作模块。
// 只在「本来装着、现在被拿走」时收尾一次手里的活——读档第一帧还没轮到装箱子，
// 那时把范围当成没装会平白丢一次状态。
    private boolean hadRangeModule;

// 看护动作（补食槽 / 施肥 / 移蛋）还有几 tick 干下一次
    private int careCooldown;

// 运输任务的起飞点：任务开始那一刻无人机在哪儿，投放完纸箱就飞回这儿
    @Nullable private Vec3 transportHome;

// 运输任务已经投放完纸箱、正在返航
    private boolean transportReturning;

// 渲染用：旋翼这一帧该不该转。通电要转，断电后还在半空往下飘也要转，真正落地停稳才不转。
// 每 tick 在 #tick 里算一次，渲染线程只是读。
    private boolean rotorsSpinning;

// 客户端平滑：服务端发来的落点与剩余插值 tick 数（照抄 LivingEntity 那套）
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private float lerpYRot;
    private float lerpXRot;

    public Uav(EntityType<? extends Uav> type, Level level) {
        super(type, level);
    }

// 无人机的 6 个槽：模组槽 + 使用槽 + 4 个背包槽
    public SimpleContainer getInventory() {
        return this.inventory;
    }

// 电源开着没有
    public boolean isPowered() {
        return this.entityData.get(DATA_POWERED);
    }

// 自动模式开着没有
    public boolean isAuto() {
        return this.entityData.get(DATA_AUTO);
    }

// 渲染用：旋翼现在该不该转（go 动画开不开）。见 #rotorsSpinning
    public boolean shouldSpinRotors() {
        return this.rotorsSpinning;
    }

// 有人正在第一人称操作没有
    public boolean isOperated() {
        return this.entityData.get(DATA_OPERATOR).isPresent();
    }

// 遥控器绑给了谁（可能没人绑）
    public Optional<UUID> getBoundPlayer() {
        return this.entityData.get(DATA_BOUND);
    }

// 当前的操控者是谁（可能没人）
    public Optional<UUID> getOperator() {
        return this.entityData.get(DATA_OPERATOR);
    }

// 这架无人机是不是绑给了这个玩家
    public boolean isBoundTo(Player player) {
        return this.getBoundPlayer().filter(uuid -> uuid.equals(player.getUUID())).isPresent();
    }

// 这个玩家是不是正在操作这架无人机
    public boolean isOperatedBy(Player player) {
        return this.getOperator().filter(uuid -> uuid.equals(player.getUUID())).isPresent();
    }

// 必须覆写：Entity#isPickable() 默认 false，不改成 true 准星选不中它，
// 右键事件也就传不到 #interact，遥控器绑不上。
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

// 别让箭矢、投掷物把无人机当成靶子（玩家动手拆是允许的，见 #hurt）
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

// 电源 / 绑定 / 操作 / 自动航线

// 开关电源。断电时顺手把自动模式停掉、把操控者放下机
    public void setPowered(boolean powered) {
        this.entityData.set(DATA_POWERED, powered);
        if (!powered) {
            this.setAuto(false);
            this.stopOperate();
        }
    }

// 通电。悬停 / 飞行都靠它
    public void powerOn() {
        this.setPowered(true);
    }

    public void setBoundPlayer(@Nullable UUID uuid) {
        this.entityData.set(DATA_BOUND, Optional.ofNullable(uuid));
    }

    private void setOperator(@Nullable UUID uuid) {
        this.entityData.set(DATA_OPERATOR, Optional.ofNullable(uuid));
    }

    private void setAuto(boolean auto) {
        this.entityData.set(DATA_AUTO, auto);
        if (!auto) {
            this.workCooldown = 0;
            this.transportHome = null;
            this.transportReturning = false;
        }
    }

// 写入自动范围的两个对角坐标并起飞（服务端调用）。
//
// 自动模式只负责把无人机送到范围中心悬停，真正干活的是装在模组槽里的看护 / 收获模块。
    public void setRange(Vec3 a, Vec3 b) {
        this.routeA = a;
        this.routeB = b;
        this.applyRange();
        this.workCooldown = 0;
        this.powerOn();
        this.setAuto(true);
    }

// 把两个对角坐标换算成方块范围，并按 #MAX_RANGE_SPAN 夹一下大小
    private void applyRange() {
        if (this.routeA == null || this.routeB == null) {
            this.rangeMin = null;
            this.rangeMax = null;
            return;
        }
        BlockPos min = BlockPos.containing(
                Math.min(this.routeA.x, this.routeB.x),
                Math.min(this.routeA.y, this.routeB.y),
                Math.min(this.routeA.z, this.routeB.z));
        BlockPos max = BlockPos.containing(
                Math.max(this.routeA.x, this.routeB.x),
                Math.max(this.routeA.y, this.routeB.y),
                Math.max(this.routeA.z, this.routeB.z));
        this.rangeMin = min;
        this.rangeMax = new BlockPos(
                Math.min(max.getX(), min.getX() + MAX_RANGE_SPAN),
                Math.min(max.getY(), min.getY() + MAX_RANGE_SPAN),
                Math.min(max.getZ(), min.getZ() + MAX_RANGE_SPAN));
    }

// 模组槽里装的是哪个模块，空槽返回 null
    @Nullable
    public Item getModuleItem() {
        ItemStack stack = this.inventory.getItem(MODULE_SLOT);
        return stack.isEmpty() ? null : stack.getItem();
    }

// 带工作范围的模块（看护 / 收获）装进模组槽后就一直工作：每 tick 按模块记录的两个点把范围设好，
// 并且保持通电、自动模式常开，不看无人机飞到没有。
//
// 两个模块都干同一件主活：自己飞去最近的成熟作物 / 挂果树叶上方收一株，田里收完了再飞去
// 最近的容器把东西放下（见 UavModules#tickHarvest）。看护模块在此之外，每
// #WORK_INTERVAL 刻再补一遍食槽、撒一次肥、把鸡窝里的蛋挪进孵化格。
    private void tickInstalledModule() {
        ItemStack module = this.findRangeModule();
        boolean care = module.is(ModItems.UAV_MODULE_CARE.get());
        boolean harvest = module.is(ModItems.UAV_MODULE_HARVEST.get());
        if (!care && !harvest) {
// 只在「本来装着、现在被拿走」时收尾一次；读档第一帧还没轮到装箱子，什么都不做
            if (this.hadRangeModule) {
                this.hadRangeModule = false;
                this.modulePointA = null;
                this.modulePointB = null;
                this.stopModuleWork();
            }
            return;
        }
        this.hadRangeModule = true;
// 换上带范围的模块后，之前运输任务留下的起飞点 / 返航标记一律作废
        this.transportHome = null;
        this.transportReturning = false;
        List<BlockPos> points = UavRangeModuleItem.getPoints(module);
        if (points.size() < 2) {
            return;
        }
        BlockPos a = points.get(0);
        BlockPos b = points.get(1);
        if (!a.equals(this.modulePointA) || !b.equals(this.modulePointB)) {
            this.modulePointA = a;
            this.modulePointB = b;
            this.startModuleRange(a, b);
            this.workCooldown = 0;
            this.careCooldown = 0;
            this.workTarget = null;
            this.workTargetIsStorage = false;
        }
// 装着模块就保持通电 + 自动模式，断电 / 手动停掉都会在下一 tick 被拉回来
        if (!this.isPowered()) {
            this.powerOn();
        }
        if (!this.isAuto()) {
            this.workCooldown = 0;
            this.setAuto(true);
        }
        if (this.rangeMin == null || this.rangeMax == null) {
            return;
        }
        if (care && --this.careCooldown <= 0) {
            this.careCooldown = WORK_INTERVAL;
            UavModules.tickCare((ServerLevel) this.level(), this, this.rangeMin, this.rangeMax);
        }
// 收割 + 送货：两个模块都做，目标由 tickHarvest 自己安排
        UavModules.tickHarvest((ServerLevel) this.level(), this, this.rangeMin, this.rangeMax);
    }

// 模块被拿掉：把没收完的收获物丢出来，别再惦记着上一个目标
    private void stopModuleWork() {
        this.workTarget = null;
        this.workTargetIsStorage = false;
        if (!this.carriedLoot.isEmpty()) {
            for (ItemStack stack : this.carriedLoot) {
                this.dropItem(stack);
            }
            this.carriedLoot.clear();
        }
    }

// 找当前生效的工作模块：优先模组槽，模组槽没有看护 / 收获模块时再看一眼使用槽
// （界面上两个槽挨得近，放错了也让它能用，省得"看起来装了却不动"）。
    private ItemStack findRangeModule() {
        ItemStack module = this.inventory.getItem(MODULE_SLOT);
        if (isRangeModule(module)) {
            return module;
        }
        ItemStack use = this.inventory.getItem(USE_SLOT);
        return isRangeModule(use) ? use : ItemStack.EMPTY;
    }

    private static boolean isRangeModule(ItemStack stack) {
        return stack.is(ModItems.UAV_MODULE_CARE.get()) || stack.is(ModItems.UAV_MODULE_HARVEST.get());
    }

// 收获模块正在赶去干的活（作物或容器），没有就返回 null
    @Nullable
    public BlockPos getWorkTarget() {
        return this.workTarget;
    }

// 当前目标是容器还是作物
    public boolean isWorkTargetStorage() {
        return this.workTargetIsStorage;
    }

// 给收获模块指一个目标；null 表示干完了
    public void setWorkTarget(@Nullable BlockPos pos, boolean storage) {
        this.workTarget = pos;
        this.workTargetIsStorage = storage;
    }

// 收获模块摘下来、还没送进容器的东西（可读可改）
    public List<ItemStack> getCarriedLoot() {
        return this.carriedLoot;
    }

    public int getWorkCooldown() {
        return this.workCooldown;
    }

    public void setWorkCooldown(int ticks) {
        this.workCooldown = ticks;
    }

// 按模块记录的两个点开工。竖直方向不去猜地面：无人机就悬在较高的那个记录点上方，
// 工作范围以记录点为准，往下多留 #MODULE_BELOW 格、往上多留 #MODULE_ABOVE 格，
// 所以点在地面、点高一格或者点在作物上都一样能盖住整块地。
    private void startModuleRange(BlockPos a, BlockPos b) {
        int minX = Math.min(a.getX(), b.getX());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX());
        int maxZ = Math.max(a.getZ(), b.getZ());
        int lowY = Math.min(a.getY(), b.getY());
        int highY = Math.max(a.getY(), b.getY());
        double hoverY = highY + MODULE_HOVER_RISE;
        this.setRange(
                new Vec3(minX + 0.5D, hoverY, minZ + 0.5D),
                new Vec3(maxX + 0.5D, hoverY, maxZ + 0.5D));
        if (this.rangeMin != null && this.rangeMax != null) {
            int minY = lowY - MODULE_BELOW;
            this.rangeMin = new BlockPos(this.rangeMin.getX(), minY, this.rangeMin.getZ());
            this.rangeMax = new BlockPos(this.rangeMax.getX(),
                    Math.min(highY + MODULE_ABOVE, minY + MAX_RANGE_SPAN),
                    this.rangeMax.getZ());
        }
    }

// 取消自动模式
    public void cancelAuto() {
        this.setAuto(false);
    }

// 开始第一人称操作：通电、停掉自动模式、把自己标记成操控者。
//
// 刻意不让玩家骑上来——相机挂在无人机上时能看到自己骑在机身上，还会顺带露出玩家的手。
// 玩家本人留在原地，客户端那边把移动输入掐掉（见 UavClient），所以也不会走开。
    public void startOperate(Player player) {
        if (this.level().isClientSide) {
            return;
        }
        this.powerOn();
        this.setAuto(false);
        this.setOperator(player.getUUID());
        this.flightKeys = 0;
    }

// 结束第一人称操作：把操控者标记清掉
    public void stopOperate() {
        if (this.level().isClientSide) {
            return;
        }
        this.setOperator(null);
        this.flightKeys = 0;
    }

// 飞行输入

// 写入这一 tick 的操作输入（服务端来自包，操作者客户端来自键盘）
    public void setFlightInput(int keys, float yaw, float pitch) {
        this.flightKeys = keys;
        this.flightYaw = yaw;
        this.flightPitch = pitch;
    }

// tick

// 客户端平滑：把服务端发来的落点摊到 steps 个 tick 里走完，而不是收到包就瞬移。
// 原版 LivingEntity 的 lerpTo 就是这么写的。
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = steps;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
// 看护 / 收获模块装进模组槽就一直干活
            this.tickInstalledModule();
        }

        if (this.level().isClientSide && !this.clientOperated) {
// 不是本机在操作：位置听服务端的，只把收到的落点摊开走
            this.applyLerp();
        } else {
// 服务端：权威模拟。操作者客户端：拿本地按键把同一套逻辑走一遍，手感才不滞后
            this.simulate();
        }

// 旋翼视觉双端都在这里定：断电缓降途中还转，落地才算停
        this.rotorsSpinning = this.isPowered() || this.hasGroundClearance();
    }

// 机身离地没有（探不到地面时也算悬空，旋翼就继续转）
    private boolean hasGroundClearance() {
        double ground = this.groundBelow();
        return Double.isNaN(ground) || this.getY() > ground + 1.0E-2D;
    }

    private void simulate() {
        if (!this.isPowered()) {
            this.land();
            return;
        }
        if (this.isAuto()) {
            this.flyAuto();
        } else if (this.isOperated()) {
            this.flyManual();
        } else {
            this.hover();
        }
    }

// 悬停：贴着地面时先升到 #HOVER_HEIGHT，到了高度就原地停住（实体本身没有重力，不用管）。
// 探不到地面（悬在深谷上空）就原地不动。
    private void hover() {
        double ground = this.groundBelow();
        if (Double.isNaN(ground)) {
            return;
        }
        double hoverY = ground + HOVER_HEIGHT;
        if (this.getY() < hoverY - 1.0E-3D) {
            this.setPos(this.getX(), Math.min(hoverY, this.getY() + VERTICAL_SPEED), this.getZ());
        }
    }

// 断电：缓慢落回地面（不是自由落体），落到地面就停住
    private void land() {
        double ground = this.groundBelow();
        if (!Double.isNaN(ground) && this.getY() <= ground + 1.0E-3D) {
            return;
        }
        this.setPos(this.getX(), this.getY() - LAND_SPEED, this.getZ());
    }

// 手动飞行：朝向抄操作者的视角，前 / 后沿视线（含俯仰），左右平移，起 / 降走世界竖直方向
    private void flyManual() {
        this.setYRot(this.flightYaw);
        this.setXRot(this.flightPitch);

        int keys = this.flightKeys;
        if (keys == 0) {
            return;
        }

        Vec3 look = Vec3.directionFromRotation(this.flightPitch, this.flightYaw);
        if (look.lengthSqr() < 1.0E-6D) {
            look = new Vec3(0.0D, 0.0D, 1.0D);
        }
        look = look.normalize();

        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        Vec3 right;
        if (flat.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            flat = flat.normalize();
// 面朝 +z 时右手边是 -x
            right = new Vec3(-flat.z, 0.0D, flat.x);
        }

        Vec3 move = Vec3.ZERO;
        if ((keys & KEY_FORWARD) != 0) {
            move = move.add(look);
        }
        if ((keys & KEY_BACK) != 0) {
            move = move.subtract(look);
        }
        if ((keys & KEY_RIGHT) != 0) {
            move = move.add(right);
        }
        if ((keys & KEY_LEFT) != 0) {
            move = move.subtract(right);
        }
        if ((keys & KEY_UP) != 0) {
            move = move.add(0.0D, 1.0D, 0.0D);
        }
        if ((keys & KEY_DOWN) != 0) {
            move = move.subtract(0.0D, 1.0D, 0.0D);
        }

        if (move.lengthSqr() > 1.0E-6D) {
            this.move(MoverType.SELF, move.normalize().scale(FLY_SPEED));
        }
    }

// 自动模式：往 #autoDestination() 飞——收获模块有目标就飞到那格上方干活，
// 否则回两个对角坐标圈出的范围中心悬停（看护模块就在那儿隔一段时间扫一遍范围）。
//
// 运输则是「往返一趟」——飞到范围中心投放纸箱，再飞回起飞点收工（见 #flyTransport）。
// 没装模块（或装的是附身用的战斗 / 高亮模块）时就只是悬在那儿。
    private void flyAuto() {
        if (this.routeA == null || this.routeB == null) {
            this.setAuto(false);
            return;
        }
// 只有模组槽里真的是运输模块才走运输航线。以前这里还看「起飞点是否还在」，
// 可那是存档字段：一旦哪次运输半途存档，之后换上看护 / 收获模块也会被这段抢走航线，
// 表现为无人机一直悬在范围中心、怎么都不去干活。
        if (this.isTransportModule()) {
            this.flyTransport();
            return;
        }

        Vec3 destination = this.autoDestination();
        if (destination != null) {
            this.flyTo(destination);
        }
    }

// 自动模式这一 tick 要去的点：收获模块有目标（作物 / 容器）就飞去目标上方，
// 否则回范围中心悬停。
    @Nullable
    private Vec3 autoDestination() {
        if (this.workTarget != null) {
            return new Vec3(
                    this.workTarget.getX() + 0.5D,
                    this.workTarget.getY() + WORK_HOVER,
                    this.workTarget.getZ() + 0.5D);
        }
        return this.rangeCenter();
    }

// 模组槽里装的是不是运输模块
    private boolean isTransportModule() {
        return this.getModuleItem() == ModItems.UAV_MODULE_TRANSPORT.get();
    }

// 运输任务：飞到范围中心投放一只纸箱（把机舱里的货物全打包进去），投放完再飞回起飞点、自动模式收工。
//
// 起飞点就是「任务开始那一刻无人机在哪儿」。#transportHome 一直记着直到飞回家，
// 返航这一段才走得完。
    private void flyTransport() {
        if (this.transportHome == null) {
            this.transportHome = this.position();
        }
        if (this.transportReturning) {
            if (this.flyTo(this.transportHome)) {
                this.setAuto(false); // 到家，收工
            }
            return;
        }
        Vec3 destination = this.rangeCenter();
        if (destination == null) {
            this.setAuto(false);
            return;
        }
        if (this.flyTo(destination) && this.dropCarton()) {
            this.transportReturning = true;
        }
    }

// 在机身下方投放一只纸箱，把机舱里的货物全打包进去。
//
// 使用槽里没有纸箱就投不了（返回 false）。投放本身会从使用槽里扣掉一只纸箱，
// 但模组槽里的模块是无人机自己的「脑子」、使用槽里剩下的纸箱也留着下次用，这两样不打包；
// 其余槽位里的东西全部装箱清空。
    private boolean dropCarton() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        ItemStack cartonStack = this.inventory.getItem(USE_SLOT);
        if (!cartonStack.is(ModItems.CARTON.get())) {
            return false;
        }
        cartonStack.shrink(1);
        this.inventory.setChanged();

        Carton carton = new Carton(ModEntities.CARTON.get(), serverLevel);
        carton.setPos(this.getX(), this.getY() - 0.5D, this.getZ());
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            if (slot == MODULE_SLOT || slot == USE_SLOT) {
                continue;
            }
            ItemStack stack = this.inventory.getItem(slot);
            if (!stack.isEmpty()) {
                carton.pack(stack.copy());
                this.inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        serverLevel.addFreshEntity(carton);
        return true;
    }

// 自动范围中心的悬停点：XZ 取两角中点，Y 悬在地面之上（探不到地面就用两角里较高的那个）
    @Nullable
    private Vec3 rangeCenter() {
        if (this.routeA == null || this.routeB == null) {
            return null;
        }
        double centerX = (this.routeA.x + this.routeB.x) * 0.5D;
        double centerZ = (this.routeA.z + this.routeB.z) * 0.5D;
        double topY = Math.max(this.routeA.y, this.routeB.y);
        double ground = this.groundAt(centerX, centerZ, topY + 4.0D);
        double hoverY = Double.isNaN(ground) ? topY : Math.max(topY, ground + HOVER_HEIGHT);
        return new Vec3(centerX, hoverY, centerZ);
    }

// 朝目标点飞一步（顺带把机头转过去）。飞到了（距离在 #AUTO_ARRIVE 之内）返回 true
    private boolean flyTo(Vec3 target) {
        Vec3 delta = target.subtract(this.position());
        double distance = delta.length();
        if (distance <= AUTO_ARRIVE) {
            return true;
        }
// 自动航线是空中直线飞行：直接挪位置，不走方块碰撞。
// 走 move 的话，半路上被树冠 / 屋檐挡住就永远到不了悬停点，模块也就一直不干活了
        Vec3 step = delta.normalize().scale(Math.min(AUTO_SPEED, distance));
        this.setPos(this.getX() + step.x, this.getY() + step.y, this.getZ() + step.z);
        Vec3 flat = new Vec3(delta.x, 0.0D, delta.z);
        if (flat.lengthSqr() > 1.0E-4D) {
            this.setYRot((float) Math.toDegrees(Mth.atan2(-flat.x, flat.z)));
            this.setXRot((float) -Math.toDegrees(Mth.atan2(delta.y, flat.length())));
        }
        return false;
    }

// 当前位置正下方最近的地面高度：从机身上方往下打一条方块射线，取撞到的那个表面。
// 比查高度图靠谱——站在屋檐下时高度图给的是屋顶。探不到就返回 NaN。
    private double groundBelow() {
        return this.groundAt(this.getX(), this.getZ(), this.getY() + 1.0D);
    }

// 从 (x, fromY, z) 竖直往下打一条射线取地面高度；探不到返回 NaN
    private double groundAt(double x, double z, double fromY) {
        BlockHitResult hit = this.level().clip(new ClipContext(
                new Vec3(x, fromY, z),
                new Vec3(x, fromY - GROUND_SCAN_DOWN, z),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));
        return hit.getType() == HitResult.Type.MISS ? Double.NaN : hit.getLocation().y;
    }

// 原版的「收包插值」：把落点摊到 lerpSteps 个 tick 里走完
    private void applyLerp() {
        if (this.lerpSteps <= 0) {
            return;
        }
        this.setPos(
                this.getX() + (this.lerpX - this.getX()) / this.lerpSteps,
                this.getY() + (this.lerpY - this.getY()) / this.lerpSteps,
                this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps
        );
        this.setYRot(this.getYRot() + Mth.wrapDegrees(this.lerpYRot - this.getYRot()) / this.lerpSteps);
        this.setXRot(this.getXRot() + (this.lerpXRot - this.getXRot()) / this.lerpSteps);
        --this.lerpSteps;
    }

// 交互 / 收回

// 手持遥控器右键：还没绑过就把这架无人机绑到自己名下、顺手给遥控器写上绑定信息，
// 并给玩家一条提示。
//
// 遥控器已经绑过东西的话这里什么都不做——那一下由客户端直接开操作界面（见 UavClient），
// 根本轮不到服务端。别人绑过的无人机不认。
//
// 如果遥控器上记的那架无人机早就被拆了，这条绑定是过期的：先抹掉它，好让这把遥控器能重新绑。
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.UAV_CONTROLLER.get()) || player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (!this.level().isClientSide) {
            UUID claimed = UavControllerItem.getBoundDrone(stack);
            if (claimed != null && !this.droneExists(claimed)) {
                UavControllerItem.unbindAll(player, claimed);
            }
        }
        if (UavControllerItem.getBoundDrone(stack) != null) {
// 已经绑着（还活着的）某架无人机了：这次右键交给客户端去开操作界面
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.level().isClientSide) {
            Optional<UUID> bound = this.getBoundPlayer();
            if (bound.isEmpty() || bound.get().equals(player.getUUID())) {
                this.setBoundPlayer(player.getUUID());
                UavControllerItem.bind(stack, this.getUUID());
                player.displayClientMessage(
                        Component.translatable("message.flavor_immersed_daily_argo.uav.bound"), true);
            } else {
                player.displayClientMessage(
                        Component.translatable("message.flavor_immersed_daily_argo.uav.bound_other"), true);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

// 这个 UUID 的无人机还在不在（任意维度）。客户端判不了，一律当成还在
    private boolean droneExists(UUID id) {
        MinecraftServer server = this.getServer();
        if (server == null) {
            return true;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) != null) {
                return true;
            }
        }
        return false;
    }

// 真正被销毁（被玩家打掉 / /kill 等）时：把 6 个槽里的东西倒出来，并把绑定的玩家那把遥控器一并解绑。
// 区块卸载不算销毁（shouldDestroy() 为 false），所以走这里不会误触发。
    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && !this.isRemoved() && reason.shouldDestroy()) {
            this.dropContents();
            this.releaseControllers();
        }
        super.remove(reason);
    }

// 把绑定的那位玩家背包里的遥控器解绑（人不在线就留着，等他下次右键时按过期绑定处理）
    private void releaseControllers() {
        UUID bound = this.getBoundPlayer().orElse(null);
        MinecraftServer server = this.getServer();
        if (bound == null || server == null) {
            return;
        }
        ServerPlayer owner = server.getPlayerList().getPlayer(bound);
        if (owner != null) {
            UavControllerItem.unbindAll(owner, this.getUUID());
        }
    }

// 像木车那样：被玩家打一下就收回成物品，其余伤害一律不理会
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return false;
        }
// 替身玩家也是 Player：它替无人机开枪，打出去的子弹不该把无人机自己拆成一地零件
        if (!(source.getEntity() instanceof Player) || source.getEntity() instanceof FakePlayer) {
            return false;
        }
        this.stopOperate();
// 槽里的东西由 remove() 统一倒出来，这里只把无人机收回成物品
        this.spawnAtLocation(new ItemStack(ModItems.UAV.get()));
        this.discard();
        return true;
    }

// 把 6 个槽里的东西都掉在无人机脚下
    private void dropContents() {
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            ItemStack stack = this.inventory.getItem(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack);
                this.inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

// 战斗模块（附身）

// 附身时左键：发射一发弹射物。装了战斗模块、且正在被这个玩家操作才生效
    public void combatFire(Player player) {
        if (this.level() instanceof ServerLevel serverLevel
                && this.isOperatedBy(player) && this.hasCombatModule()) {
            UavModules.combatFire(serverLevel, this);
        }
    }

// 附身时右键：先试着食用使用槽里的食物，不行再当发射用
    public void combatUse(Player player) {
        if (this.level() instanceof ServerLevel serverLevel
                && this.isOperatedBy(player) && this.hasCombatModule()) {
            UavModules.combatUse(serverLevel, this, player);
        }
    }

// 模组槽里装的是不是战斗模块
    private boolean hasCombatModule() {
        return this.getModuleItem() == ModItems.UAV_MODULE_COMBAT.get();
    }

// 高亮标记模块（附身）

// 附身时右键方块：把这格记成高亮方块；对着同一格再点一次则取消标记。
// 装了高亮标记模块、且正在被这个玩家操作才生效。
//
// 标记只画在附身的那一端屏幕上（见 UavHighlightRenderer），退出操作视角就不再显示。
    public void markBlock(Player player, BlockPos pos) {
        if (this.level() instanceof ServerLevel && this.isOperatedBy(player) && this.hasHighlightModule()) {
            Optional<BlockPos> marked = pos.equals(this.getHighlightPos()) ? Optional.empty() : Optional.of(pos);
            this.entityData.set(DATA_HIGHLIGHT, marked);
        }
    }

// 渲染用：当前被高亮标记的方块，没标记过返回 null
    @Nullable
    public BlockPos getHighlightPos() {
        return this.entityData.get(DATA_HIGHLIGHT).orElse(null);
    }

// 模组槽里装的是不是高亮标记模块
    public boolean hasHighlightModule() {
        return this.getModuleItem() == ModItems.UAV_MODULE_HIGHLIGHT.get();
    }

// 把一件东西丢在无人机脚下（模块干活时没处放的东西走这里）
    public void dropItem(ItemStack stack) {
        if (!this.level().isClientSide && !stack.isEmpty()) {
            this.spawnAtLocation(stack);
        }
    }

// 界面

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.flavor_immersed_daily_argo.uav.inventory_title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new UavMenu(containerId, playerInventory, this);
    }

// 存读

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_POWERED, false);
        this.entityData.define(DATA_BOUND, Optional.empty());
        this.entityData.define(DATA_OPERATOR, Optional.empty());
        this.entityData.define(DATA_AUTO, false);
        this.entityData.define(DATA_HIGHLIGHT, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
// 这里直接写 entityData：读档时不该触发断电那套「放下操控者」的副作用
        this.entityData.set(DATA_POWERED, tag.getBoolean("Powered"));
        this.entityData.set(DATA_AUTO, tag.getBoolean("Auto"));
        this.entityData.set(DATA_BOUND,
                tag.hasUUID("Bound") ? Optional.of(tag.getUUID("Bound")) : Optional.empty());
        this.entityData.set(DATA_OPERATOR, Optional.empty());
        this.routeA = readVec(tag, "RouteA");
        this.routeB = readVec(tag, "RouteB");
        this.applyRange();
// 运输任务中途存档：返航的起飞点得跟着一起存，不然重新加载后无人机不知道往哪儿回
        this.transportHome = readVec(tag, "TransportHome");
        this.transportReturning = tag.getBoolean("TransportReturning");
        this.readInventory(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Powered", this.isPowered());
        tag.putBoolean("Auto", this.isAuto());
        this.getBoundPlayer().ifPresent(uuid -> tag.putUUID("Bound", uuid));
        writeVec(tag, "RouteA", this.routeA);
        writeVec(tag, "RouteB", this.routeB);
        writeVec(tag, "TransportHome", this.transportHome);
        tag.putBoolean("TransportReturning", this.transportReturning);
        this.writeInventory(tag);
    }

// 6 个槽的存读：与木车 / 收割机用的是同一套「Slot + 物品」写法
    private void readInventory(CompoundTag tag) {
        InventoryNbt.load(this.inventory, tag, "Inventory");
    }

    private void writeInventory(CompoundTag tag) {
        tag.put("Inventory", InventoryNbt.save(this.inventory));
    }

    @Nullable
    private static Vec3 readVec(CompoundTag tag, String key) {
        String x = key + "X";
        return tag.contains(x) ? new Vec3(tag.getDouble(x), tag.getDouble(key + "Y"), tag.getDouble(key + "Z")) : null;
    }

    private static void writeVec(CompoundTag tag, String key, @Nullable Vec3 vec) {
        if (vec == null) {
            return;
        }
        tag.putDouble(key + "X", vec.x);
        tag.putDouble(key + "Y", vec.y);
        tag.putDouble(key + "Z", vec.z);
    }
}
