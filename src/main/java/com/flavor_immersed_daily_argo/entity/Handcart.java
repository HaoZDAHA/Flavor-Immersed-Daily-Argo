package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

// 拉力木车。
//
// 像木船一样的存在：有碰撞体积、准星能选中、被玩家打一下就散架并掉回物品。平时它就停在原地，
// 直接右键便挂到那个玩家身上、时刻跟在他身后（车把递到玩家手边、车斗甩在后头）；
// 玩家潜行（松手）或者下线、换维度时自动解开。
//
// 它是最朴素的原版 Entity——自己没有重力也没有 AI，跟随完全是在 #tick 里按玩家位置
// 算出来的。位置直接 setPos 而不走碰撞，图的是绝不会卡在墙角跟不上人。
//
// 落点只看玩家的走动方向（脚下这一 tick 挪了多少），不看视角：站着原地甩鼠标时车纹丝不动。
//
// 跟随不干等服务端的包：只要车主在跟前，两端都拿同一段逻辑走一遍——服务端算权威位置，
// 客户端用本地那位玩家再算一遍，车就精确钉在玩家当下的坐标上，连渲染插值都跟他同拍，
// 看着才是贴着人走而不是慢半拍。原版「收包再慢慢挪」（Entity#lerpTo）只留着给
// 别人的车、以及车主不在跟前时兜底。
//
// 还认得一种「载货模式」：手持箱子右键，车斗里就多装一只箱子（模型换成
// handcart_expand 那份，动画纹理都不变），箱子里是 54 格原版大箱子的空间。
// 这之后右键车身开箱、右键前头的车把照旧拉车（靠 #interactAt 拿到命中点来分）。
// 车被打散时，箱子连同箱里的东西一并掉出来。
public class Handcart extends Entity implements MenuProvider {

// 载货模式那只大箱子的格数：原版大箱子就是 6 行 × 9 列
    private static final int CHEST_SIZE = 54;

// 车整体落在玩家身后半格：原点跟在玩家后头 0.5 格，车把探到玩家身前约半格。
//
// 车把在模型里待在原点正前方 1 格（见 HandcartRenderer#MODEL_PIVOT_Z），扣掉这半格，
// 握把正好递在玩家手边上；车斗从原点往后铺开，全在身后。
    public static final double FOLLOW_DISTANCE = 0.5D;

// 每 tick 最多朝目标挪多少格：比玩家冲刺还快，跑得再急也追得上
    private static final double FOLLOW_SPEED = 0.6D;

// 判定「车在动」的位移阈值（平方），单位是格
    private static final double MOVING_THRESHOLD_SQR = 1.0E-5D;

// 判定「玩家真的挪了窝」的位移阈值（平方）：比这还小的晃动不算走动，不更新车头
    private static final double WALK_THRESHOLD_SQR = 1.0E-4D;

// 车头转向的最大角速度（度/tick）。玩家原地掉头往回走时，目标方向一下子翻 180°，
// 车不能跟着瞬移翻面——按这个速度拧，180° 要走上 15 tick，步行速度下差不多三四米才转完。
    private static final float TURN_SPEED_DEG = 12.0F;

// 车轮位置：车身贴地的高度、以及渲染时的歪斜，都按这几处地面算

// 车轮轴在原点后方 2.2 格、左右各 1.3 格
    public static final double AXLE_BACK = 2.2D;
    public static final double WHEEL_LATERAL = 1.3D;

// 车把握把在原点前方 1 格：前后俯仰量它和车轴处地面的高差
    public static final double HANDLE_FRONT = 1.0D;

// 找地面时往下探多远：落差超过这个就当脚下是空的，车留在原地，不跟着栽下去
    private static final double GROUND_SCAN_DOWN = 3.0D;

// 车身随地面歪斜最多 25°，免得贴着悬崖时整辆车翻过去
    public static final float MAX_TILT_DEG = 25.0F;

// 歪斜角度每 tick 朝目标靠拢的比例：地形是一格一格跳的，直接照当前的方块表面算角度，
// 沿台阶走时目标值会在相邻几档之间蹦，看上去就是车身一帧一帧地闪。这里做个低通，
// 只走一小步，再把两 tick 之间插值交给渲染，画面才连续。
    private static final float TILT_SMOOTHING = 0.2F;

// 碰撞箱尺寸：量的是车斗那截，不含递在玩家手边的车把

// 车斗沿车纵轴的范围（格，原点在模型 z=11.5px 处）：车把握把 -17.5px ~ 车斗尾板 +58.5px
    private static final double BODY_MIN_Z = -17.5D / 16.0D;
    private static final double BODY_MAX_Z = 58.5D / 16.0D;

// 车体宽度的一半（含两侧车轮，模型 x 跨 2.8 格）
    private static final double BODY_HALF_WIDTH = 1.4D;

// 车体高度（模型 y 跨 24px = 1.5 格）
    private static final double BODY_HEIGHT = 1.5D;

// 载货模式那只箱子高出车斗半格，碰撞箱得跟着长高，不然贴着车走会穿进箱子里
    private static final double BODY_HEIGHT_EXPANDED = 2.0D;

// 车主是谁。既是「挂着人没有」的标志（模型靠它决定放哪个动画），也是客户端自己推算位置的依据——
// 认得出车主，客户端才能把车钉在本地那位身上，而不是干等网络包。
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(Handcart.class, EntityDataSerializers.OPTIONAL_UUID);

// 装上箱子了没有。既决定模型要不要把那只大箱子亮出来，也是「右键是开箱还是拉车」的分界，
// 所以必须同步：客户端渲染时就知道该画哪一份模型。
    private static final EntityDataAccessor<Boolean> DATA_EXPANDED =
            SynchedEntityData.defineId(Handcart.class, EntityDataSerializers.BOOLEAN);

// 载货模式那只大箱子的内容。跟车一起存读，散架时倒出来
    private final SimpleContainer chest = new SimpleContainer(CHEST_SIZE);

// 平时停着时循环播放的 free 动画
    public final AnimationState freeAnimationState = new AnimationState();

// 被拉着走时循环播放的 free2 动画
    public final AnimationState free2AnimationState = new AnimationState();

// 上一 tick 的位置，用来判断这 tick 有没有挪窝
    private Vec3 lastPos = Vec3.ZERO;

// 这一 tick 有没有在移动
    private boolean moving;

// 玩家上一 tick 的位置，用它算玩家的速度方向；没牵过人是 null，免得拿原点当上一帧算出个鬼方向
    @Nullable
    private Vec3 lastPlayerPos;

// 车头朝向（水平单位向量）：跟着玩家的走动方向，原地转视角不动它
    private Vec3 followDir = new Vec3(0.0D, 0.0D, 1.0D);

// 车身当前朝向（度）：朝 #followDir 慢慢拧过去，掉头时不会一步翻面
    private float headingYaw;

// 朝向是否已经摆正过：刚挂上那一下直接对正，之后才慢慢转
    private boolean headingSet;

// 车身当前的侧倾与俯仰（度），以及上一 tick 的值：渲染时在两者之间插值，贴地歪斜才顺
    private float tiltRoll;
    private float tiltPitch;
    private float prevTiltRoll;
    private float prevTiltPitch;

// 客户端平滑用：服务端发来的落点与剩余插值 tick 数，机制照抄 LivingEntity
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private float lerpYRot;

    public Handcart(EntityType<? extends Handcart> type, Level level) {
        super(type, level);
    }

// 正被玩家牵着？
    public boolean isFollowing() {
        return this.entityData.get(DATA_OWNER).isPresent();
    }

// 这一 tick 有没有在移动（模型靠它决定要不要播 free2）
    public boolean isMoving() {
        return this.moving;
    }

// 必须覆写：Entity#isPickable() 默认是 false，不改成 true 准星就选不中它，
// 右键事件根本传不到 #interact，车就挂不上去。
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

// 平时像木船一样有碰撞体积，玩家撞不进去；被牵着走的时候例外——车把就递在玩家手边，
// 箱体必然和玩家站着的那点空间叠在一起，下坡时这点重叠会把玩家往外挤、挤成趴下爬行的姿态。
// 所以牵引中干脆不参与碰撞，人车各走各的，松手停下再恢复。
    @Override
    public boolean canBeCollidedWith() {
        return !this.isRemoved() && !this.isFollowing();
    }

// 碰撞箱贴着车斗走，而不是照原版那样以实体坐标为中心。
//
// 实体原点落在车把后头一点点（见 HandcartRenderer#MODEL_PIVOT_Z），跟随的时候正好按在
// 玩家身上，于是「以坐标为中心」摆出来的箱体会整个甩到车前方的空气里，真正装着东西的车斗反倒露在外面。
// 这里把箱体沿车的纵轴挪到车斗中段，大小取车体尺寸，再按当前朝向投影成轴对齐的包围盒
// （朝向斜着走时方盒会罩得宽些，这是轴对齐盒子躲不掉的）。
    @Override
    protected AABB makeBoundingBox() {
// 模型 +z（车斗那头）朝车的背后，渲染时对应世界的 (-sin yaw, 0, cos yaw)
        double yaw = Math.toRadians(this.getYRot());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);

        double bodyCenterZ = (BODY_MIN_Z + BODY_MAX_Z) / 2.0D;
        double halfLength = (BODY_MAX_Z - BODY_MIN_Z) / 2.0D;
// 把「宽 2×BODY_HALF_WIDTH、长 2×halfLength」的矩形投到世界 x/z 轴上，得到能罩住它的最小方盒
        double halfX = BODY_HALF_WIDTH * Math.abs(cos) + halfLength * Math.abs(sin);
        double halfZ = BODY_HALF_WIDTH * Math.abs(sin) + halfLength * Math.abs(cos);

        double centerX = this.getX() - bodyCenterZ * sin;
        double centerZ = this.getZ() + bodyCenterZ * cos;
        double height = this.isExpanded() ? BODY_HEIGHT_EXPANDED : BODY_HEIGHT;
        return new AABB(
                centerX - halfX, this.getY(), centerZ - halfZ,
                centerX + halfX, this.getY() + height, centerZ + halfZ
        );
    }

// 别让箭矢、投掷物把车当成靶子（玩家动手拆是允许的，见 #hurt）
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

// 客户端平滑：把服务端发来的落点摊到 steps 个 tick 里走完，而不是收到包就瞬移。
// 原版 LivingEntity 的 lerpTo 就是这么写的。
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpSteps = steps;
    }

    @Override
    public void tick() {
        super.tick();

// 没挂着人就把上一段的方向忘掉，下次挂上重新按玩家正面起步（两端一致）
        if (!this.isFollowing()) {
            this.lastPlayerPos = null;
            this.headingSet = false;
        }

// 车主在跟前就直接照他推算——服务端算权威位置，客户端也拿本地那位算一遍，
// 车每 tick 都精确落在玩家当下的坐标上，渲染插值跟他同拍，看着才是贴着人走。
// 找不到车主（别人的车、或车主下线了）才退回原版那套「收包慢慢挪」。
        Player owner = this.findOwnerPlayer();
        if (owner != null) {
            this.follow(owner);
        } else {
            this.applyLerp();
            if (!this.level().isClientSide && this.isFollowing()) {
// 服务端：车主下线或跑别的维度去了，撒手
                this.stopFollowing();
            }
        }

// 再记位移：模型读 isMoving() 决定 free2 要不要播
        Vec3 pos = this.position();
        this.moving = pos.distanceToSqr(this.lastPos) > MOVING_THRESHOLD_SQR;
        this.lastPos = pos;

// 贴地歪斜只是画面上的事：客户端每 tick 采一次地形再慢慢拧，服务端不用管
        if (this.level().isClientSide) {
            this.updateGroundTilt();
        }
    }

// 原版的「收包插值」：把落点摊到 lerpSteps 个 tick 里走完。
// 车在自己手上时轮不到它，只有别人的车、或车主不在跟前时兜底——那点滞后也就无所谓了。
    private void applyLerp() {
        if (this.lerpSteps > 0) {
            this.setPos(
                    this.getX() + (this.lerpX - this.getX()) / this.lerpSteps,
                    this.getY() + (this.lerpY - this.getY()) / this.lerpSteps,
                    this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps
            );
            this.setYRot(this.getYRot() + Mth.wrapDegrees(this.lerpYRot - this.getYRot()) / this.lerpSteps);
            --this.lerpSteps;
        }
    }

// 找车主。服务端去玩家列表里按 UUID 取，顺带确认人还跟车在同一个维度；客户端则在本地已知的玩家里认人
// （本地玩家的坐标是客户端自己算的，拿它推算就没有网络滞后）。
//
// 返回：车主本人；人不在跟前（车主不是这一端、或已下线）就是 null
    @Nullable
    private Player findOwnerPlayer() {
        Optional<UUID> uuid = this.entityData.get(DATA_OWNER);
        if (uuid.isEmpty()) {
            return null;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            Player player = serverLevel.getServer().getPlayerList().getPlayer(uuid.get());
            return player != null && player.level() == this.level() ? player : null;
        }
        for (Player player : this.level().players()) {
            if (uuid.get().equals(player.getUUID())) {
                return player;
            }
        }
        return null;
    }

// 每 tick 的跟随：把车拖到玩家身后 #FOLLOW_DISTANCE 格，车头背对玩家的走动方向。
//
// 方向取的是玩家这一 tick 实际挪动的方向，不是视角：站着原地甩鼠标车不会动。
//
// 高度取的是车轴两个轮子底下的地面（见 #wheelGroundY()），不是玩家脚下那格：
// 车跟在人后头半格，台阶上两层地面能差一整格，照玩家抄就会半悬空半陷土。只在玩家脚踏实地
// 时才跟，玩家跳起来的时候车留在原地，不至于跟着一起飞。
//
// 朝向是「拧」出来的，不是直接赋值：每 tick 最多转 #TURN_SPEED_DEG 度。掉头 180° 时，
// 车先慢慢转，位置也跟着这段弧线绕到玩家身后，看着像真的被人拽着拐弯。
//
// 两端共用这一段：服务端跑出来的是权威位置，客户端只是拿本地玩家把同一套算法再走一遍，
// 好让车不必等网络包——两边从同一个起点、同一个步长收敛，落点自然一致。
    private void follow(Player player) {
        if (!this.level().isClientSide) {
// 玩家跑去别的维度、松开潜行键（撒手）、或者进了旁观模式，都算解开牵引
            if (player.level() != this.level() || player.isSecondaryUseActive() || player.isSpectator()) {
                this.stopFollowing();
                return;
            }
        }

        Vec3 playerPos = player.position();
        if (this.lastPlayerPos == null) {
// 刚挂上：起步方向先按玩家的正面来一把（跟服务端 startFollowing 一个规矩），两端才不会一开始朝岔
            Vec3 look = player.getLookAngle();
            Vec3 flat = new Vec3(look.x, 0.0D, look.z);
            if (flat.lengthSqr() > WALK_THRESHOLD_SQR) {
                this.followDir = flat.normalize();
            }
        } else {
// 车头跟着玩家「这一 tick 实际挪动的方向」走：玩家站着转视角时这段位移是 0，方向保持不动
            Vec3 walked = new Vec3(playerPos.x - this.lastPlayerPos.x, 0.0D, playerPos.z - this.lastPlayerPos.z);
            if (walked.lengthSqr() > WALK_THRESHOLD_SQR) {
                this.followDir = walked.normalize();
            }
        }
        this.lastPlayerPos = playerPos;

// 车身朝向：朝期望方向慢慢拧，不一步到位。玩家原地掉头往回走时，车会绕着玩家画一段弧线
// 跟过来，走出几米才转完 180°，而不是整辆车瞬移着翻个面。
        float targetYaw = (float) Math.toDegrees(Mth.atan2(this.followDir.x, -this.followDir.z));
        if (this.headingSet) {
            this.headingYaw += Mth.clamp(Mth.wrapDegrees(targetYaw - this.headingYaw), -TURN_SPEED_DEG, TURN_SPEED_DEG);
        } else {
            this.headingYaw = targetYaw;
            this.headingSet = true;
        }
        this.setYRot(this.headingYaw);

// 位置按平滑后的朝向摆：车尾方向 = 模型 +z 在世界里对应的 (-sin yaw, 0, cos yaw)
        double headingRad = Math.toRadians(this.headingYaw);
        Vec3 heading = new Vec3(Math.sin(headingRad), 0.0D, -Math.cos(headingRad));

// 目标点：玩家身后 FOLLOW_DISTANCE 格
        Vec3 target = playerPos.subtract(heading.scale(FOLLOW_DISTANCE));

        double dx = target.x - this.getX();
        double dz = target.z - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance > 1.0E-4D) {
            double step = Math.min(distance, FOLLOW_SPEED);
            this.setPos(this.getX() + dx / distance * step, this.getY(), this.getZ() + dz / distance * step);
        }

        double dy = player.onGround() ? this.wheelGroundY() - this.getY() : 0.0D;
        if (Math.abs(dy) > 1.0E-4D) {
            this.setPos(this.getX(), this.getY() + Mth.clamp(dy, -FOLLOW_SPEED, FOLLOW_SPEED), this.getZ());
        }
    }

// 车轴那两个轮子底下的地面高度之和的一半——车身就该落在这个高度上，轮子才踩得着地。
//
// 取两个轮子的平均而不是玩家脚下的高度：车跟在玩家后头半格，脚底下那格和轮子底下那格
// 常常不是一层（台阶、坡道），照玩家抄就会一半悬空一半陷进土里。平均下来车身正好卡在
// 两个轮子中间，剩下的左右高差交给渲染时的侧倾去歪。
    private double wheelGroundY() {
        double yaw = Math.toRadians(this.getYRot());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
// 车轴那条线：从原点朝车尾（模型 +z）退 AXLE_BACK 格
        double axleX = this.getX() - sin * AXLE_BACK;
        double axleZ = this.getZ() + cos * AXLE_BACK;
        double left = this.groundBelow(axleX + cos * WHEEL_LATERAL, axleZ + sin * WHEEL_LATERAL);
        double right = this.groundBelow(axleX - cos * WHEEL_LATERAL, axleZ - sin * WHEEL_LATERAL);
        return (left + right) / 2.0D;
    }

// 某个水平位置正下方最近的地面高度：从车身顶上往下打一条方块射线，取撞到的那个表面。
//
// 比查高度图靠谱：高度图给的是整列最高的方块，站在屋檐下会把车顶到房顶上；射线只看车身
// 上下这几格，台阶、楼梯、栅栏都能贴着表面走。往下探不到（悬空超过 #GROUND_SCAN_DOWN
// 格）就返回当前高度，让车留在原地。
    public double groundBelow(double x, double z) {
        BlockHitResult hit = this.level().clip(new ClipContext(
                new Vec3(x, this.getY() + 1.0D, z),
                new Vec3(x, this.getY() - GROUND_SCAN_DOWN, z),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));
        return hit.getType() == HitResult.Type.MISS ? this.getY() : hit.getLocation().y;
    }

// 采车轮与车把底下的地面，算出车身此刻该有的侧倾与俯仰，再朝这个目标慢慢拧过去。
//
// 角度不能照着当前的方块表面直接给：地形是一格一格跳的，沿台阶、半砖走时目标值会在相邻几档
// 之间来回蹦，车身就跟着一帧一帧地闪。这里每 tick 只走 #TILT_SMOOTHING 那么一小步，
// 把跳变磨平；剩下两 tick 之间的连续由渲染侧的插值补。
//
// 采样点跟 #wheelGroundY() 是同一套（#groundBelow），左右两个轮子加车把，
// 三次向下射线；绕 z 的是侧倾（左高右低），绕 x 的是俯仰（爬坡前高后低）。
    private void updateGroundTilt() {
        this.prevTiltRoll = this.tiltRoll;
        this.prevTiltPitch = this.tiltPitch;

        double yaw = Math.toRadians(this.getYRot());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
// 车轴那两个轮子底下的地面
        double axleX = this.getX() - sin * AXLE_BACK;
        double axleZ = this.getZ() + cos * AXLE_BACK;
        double leftY = this.groundBelow(axleX + cos * WHEEL_LATERAL, axleZ + sin * WHEEL_LATERAL);
        double rightY = this.groundBelow(axleX - cos * WHEEL_LATERAL, axleZ - sin * WHEEL_LATERAL);
// 车把那一头
        double frontY = this.groundBelow(this.getX() + sin * HANDLE_FRONT, this.getZ() - cos * HANDLE_FRONT);

        float targetRoll = (float) Mth.clamp(
                Math.toDegrees(Math.atan2(leftY - rightY, WHEEL_LATERAL * 2.0D)), -MAX_TILT_DEG, MAX_TILT_DEG);
        float targetPitch = (float) Mth.clamp(
                -Math.toDegrees(Math.atan2((leftY + rightY) / 2.0D - frontY, AXLE_BACK + HANDLE_FRONT)),
                -MAX_TILT_DEG, MAX_TILT_DEG);

        this.tiltRoll += (targetRoll - this.tiltRoll) * TILT_SMOOTHING;
        this.tiltPitch += (targetPitch - this.tiltPitch) * TILT_SMOOTHING;
    }

// 车身侧倾（度），已按帧内进度在两 tick 之间插值：渲染贴地歪斜用
    public float getTiltRoll(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevTiltRoll, this.tiltRoll);
    }

// 车身上下俯仰（度），已按帧内进度在两 tick 之间插值：渲染贴地歪斜用
    public float getTiltPitch(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevTiltPitch, this.tiltPitch);
    }

// 右键互动：直接右键把车挂到玩家身上开始跟随；手持箱子右键则给车斗装上箱子，
// 换成载货模式。潜行时不做事——跟随中潜行会在 #follow 里自动解开，那正是「松手」。
//
// 装了箱子以后，点车斗那一头会被 #interactAt 先截走开箱，落到这里的就只剩下
// 车把，照旧拉车。
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            ItemStack held = player.getItemInHand(hand);
            if (!this.isExpanded() && held.is(Items.CHEST)) {
                if (!this.level().isClientSide) {
                    this.setExpanded(true);
                    if (!player.hasInfiniteMaterials()) {
                        held.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (!this.level().isClientSide) {
                this.startFollowing(player);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

// 点中实体时先走这里，能拿到命中点——正好拿来分车斗和车把。
//
// 装了箱子之后，点车斗那一侧是开箱（返回 CONSIDER/SUCCESS 把这一下吃掉，不再往下走
// 拉车）；点前头的车把则返回 PASS，让它落回 #interact 去拉车。
//
// hitOffset：命中点相对实体坐标的偏移（原版 MultiPlayerGameMode#interactAt 就是这么算的）
    @Override
    public InteractionResult interactAt(Player player, Vec3 hitOffset, InteractionHand hand) {
        if (this.isExpanded() && this.isBodyHit(hitOffset)) {
            if (!this.level().isClientSide && !player.isSecondaryUseActive()) {
                player.openMenu(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

// 这一下点的是车斗还是前头的车把：把命中点投到车的纵轴上，落在原点后方就是车斗。
//
// 模型 +z（车斗那头）在世界里指着 (-sin yaw, 0, cos yaw)，投影为正说明命中点在
// 原点后方，也就是车厢那截；为负则在前头那一格的车把上。
    private boolean isBodyHit(Vec3 hitOffset) {
        double yaw = Math.toRadians(this.getYRot());
        return -hitOffset.x * Math.sin(yaw) + hitOffset.z * Math.cos(yaw) >= 0.0D;
    }

// 是否处在载货模式（车斗里装了箱子）
    public boolean isExpanded() {
        return this.entityData.get(DATA_EXPANDED);
    }

    private void setExpanded(boolean expanded) {
        this.entityData.set(DATA_EXPANDED, expanded);
    }

// 箱子界面的标题就用车的名字（没改名时是「拉力木车」）
    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChestMenu(MenuType.GENERIC_9x6, containerId, playerInventory, this.chest, 6);
    }

// 像木船那样：被玩家打一下就散架，原地掉回车、箱子与箱里的东西。其余伤害（箭矢、爆炸、仙人掌……）
// 一概不理会，免得路过的流弹把车拆了。
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return false;
        }
        if (!(source.getEntity() instanceof Player)) {
            return false;
        }
        this.stopFollowing();
        this.dropContents();
        this.spawnAtLocation(new ItemStack(ModItems.HANDCART.get()));
        this.discard();
        return true;
    }

// 散架时把载货模式那只箱子和箱里的东西一并倒出来；没装箱子就什么都不掉
    private void dropContents() {
        if (!this.isExpanded()) {
            return;
        }
        this.spawnAtLocation(new ItemStack(Items.CHEST));
        for (int slot = 0; slot < this.chest.getContainerSize(); slot++) {
            ItemStack stack = this.chest.getItem(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack);
            }
        }
        this.chest.clearContent();
    }

    private void startFollowing(Player player) {
        this.entityData.set(DATA_OWNER, Optional.of(player.getUUID()));
    }

    private void stopFollowing() {
        this.entityData.set(DATA_OWNER, Optional.empty());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER, Optional.empty());
        builder.define(DATA_EXPANDED, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_OWNER, tag.hasUUID("Owner") ? Optional.of(tag.getUUID("Owner")) : Optional.empty());
        this.setExpanded(tag.getBoolean("Expanded"));
        InventoryNbt.load(this.chest, tag, "Chest", this.registryAccess());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.entityData.get(DATA_OWNER).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putBoolean("Expanded", this.isExpanded());
        if (this.isExpanded()) {
            tag.put("Chest", InventoryNbt.save(this.chest, this.registryAccess()));
        }
    }
}
