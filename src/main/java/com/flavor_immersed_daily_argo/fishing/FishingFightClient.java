package com.flavor_immersed_daily_argo.fishing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// 东篱钓竿「遛鱼阶段」的客户端表现与操作上报。
//
// 玩家要做两件事：
//
// 视角：鱼会低频地把镜头往旁边拽走（漂移 + 抖动），玩家必须反向转视角，
// 把镜头重新稳回浮漂上；
// 脚步：不要朝浮漂方向走 —— 原地站定稳住身形、或往背离浮漂的方向撤，
// 都算合格；只有主动朝鱼走过去才算失手。
//
// 两者同时满足的 tick 会上报给服务端累加进度。设计上没有任何 HUD，
// 玩家只能靠浮漂抖动与镜头被拽的手感判断该往哪边操作。
//
// 遛鱼期间玩家被约束在「阶段开始时所在位置」附近的小圆圈内：既不必为了凑进度一路后撤，
// 也不会被鱼拽着跑远，更不会被拖进水里 —— 表现为与鱼在原地拔河。
//
// 表现层还做了三件事与镜头互相呼应：浮漂朝镜头被拽的一侧被拖走（鱼线随浮漂一起摆动）、
// 浮漂持续翻腾起水花与气泡、并伴随随机音调的溅水声。
//
// 本类不标记 @OnlyIn：公共代码只在客户端包处理里调用它，
// 服务端不会执行到这些方法，从而避免加载客户端类。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class FishingFightClient {

// 镜头方向与浮漂方向的点积高于该值才算「把视角稳在浮漂上」（约 32° 以内）
    private static final double VIEW_DOT_THRESHOLD = 0.85D;
// 移动方向与「朝向浮漂」方向的点积低于该值才算合格（只有主动朝鱼走才失手）
    private static final double MOVE_DOT_THRESHOLD = 0.25D;
// 视角被鱼拽走的主拉力幅度（度）。两条正弦周期分别约 2.4 秒 / 5 秒，
// 保证一次遛鱼里镜头被反复拽动多次，玩家能清楚看见「鱼在往哪边拉」。
    private static final float DRIFT_DEG_A = 22.0F;
    private static final float DRIFT_DEG_B = 13.0F;
// 与配件无关的基础紧张抖动强度（档位）。遛鱼一开始画面就带小幅颤动，
// 配件显眼度算出的 #shakeAmount 在此之上叠加 —— 钓组越显眼，晃得越厉害。
    private static final float BASE_SHAKE = 1.1F;
// 每 tick 附加的拖行速度（格/tick）
    private static final double DRAG_SPEED = 0.03D;
// 遛鱼场地半径（格）：以阶段开始时玩家位置为圆心，玩家跑不出这个圈，也不会被拖进水里
    private static final double ARENA_RADIUS = 2.5D;
// 浮漂被鱼拖走的横向摆幅系数：镜头被拽得越偏，浮漂被拉得越远
    private static final double BOBBER_SWAY = 0.9D;
// 浮漂在锚点附近水平乱窜的最大半径（格）
    private static final double BOBBER_ROAM = 1.5D;
// 浮漂高频细碎抖动的幅度（格），模拟鱼尾反复拍水
    private static final double BOBBER_JITTER = 0.30D;
// 浮漂随鱼挣扎上下起伏的幅度（格）
    private static final double BOBBER_BOB = 0.12D;
// 水花粒子生成间隔（tick），数值越小水花越密集
    private static final int SPLASH_INTERVAL = 3;
// 挣扎音效播放间隔（tick）
    private static final int SOUND_INTERVAL = 11;
// 看门狗：服务端异常时兜底，避免状态残留（约 120 秒，需覆盖配件拉长后的遛鱼时长）
    private static final int WATCHDOG_TICKS = 2400;

    private static boolean active = false;
    private static int hookId = -1;
// 浮漂入水的锚点，鱼的发力方向与浮漂抖动都以此为准
    private static Vec3 anchor = Vec3.ZERO;
// 遛鱼场地的圆心：阶段开始时玩家所在位置，玩家被约束在它附近
    private static Vec3 arenaCenter = Vec3.ZERO;
    private static int watchdog = 0;
// 表现层的连续时间，供镜头漂移与抖动使用
    private static float effectTicks = 0.0F;
    private static int splashTimer = 0;
    private static int soundTimer = 0;
// 拖拽力度系数 = 鱼最大拉力 / 钓竿最大拉力（拉力比越大拽得越狠）
    private static float tensionFactor = 1.0F;
// 配件带来的额外屏幕抖动幅度 = 鱼警惕性 × 钓组显眼度（基础抖动另见 #BASE_SHAKE）
    private static float shakeAmount = 0.0F;

    private FishingFightClient() {
    }

// 服务端同步入口。active 为 false 表示遛鱼阶段结束。
    public static void handleSync(FishingFightSyncPacket packet) {
        if (!packet.active()) {
            reset();
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        active = true;
        hookId = packet.hookId();
        anchor = new Vec3(packet.anchorX(), packet.anchorY(), packet.anchorZ());
        arenaCenter = player.position();
        watchdog = WATCHDOG_TICKS;
        effectTicks = 0.0F;
        splashTimer = 0;
        soundTimer = 0;
        tensionFactor = (float) packet.params().tensionFactor();
        shakeAmount = (float) packet.params().shakeAmount();
    }

    private static void reset() {
        active = false;
        hookId = -1;
        arenaCenter = Vec3.ZERO;
        watchdog = 0;
        splashTimer = 0;
        soundTimer = 0;
        tensionFactor = 1.0F;
        shakeAmount = 0.0F;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!active) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null
                || !(minecraft.level.getEntity(hookId) instanceof FishingHook hook)) {
            reset();
            return;
        }
        if (--watchdog <= 0) {
            reset();
            return;
        }

        Vec3 toFish = new Vec3(anchor.x - player.getX(), 0.0D, anchor.z - player.getZ());
        if (toFish.lengthSqr() < 0.01D) {
            return;
        }
        Vec3 dirToFish = toFish.normalize();

        PacketDistributor.sendToServer(new FishingFightInputPacket(hookId, isCorrect(player, dirToFish)));
        applyDrag(player, dirToFish);
        clampToArena(player);
        jitterBobber(hook, player, dirToFish);

        if (--splashTimer <= 0) {
            splashTimer = SPLASH_INTERVAL;
            spawnSplash(minecraft, hook);
        }
        if (--soundTimer <= 0) {
            soundTimer = SOUND_INTERVAL;
            playThrashSound(minecraft, hook);
        }

        effectTicks += 1.0F;
    }

// 视角是否稳在浮漂上、脚步是否没有朝鱼走，两者都满足才算正确
    private static boolean isCorrect(LocalPlayer player, Vec3 dirToFish) {
// 镜头实际朝向 = 玩家转向 + 被鱼拽出的漂移与抖动，判定以玩家看到的结果为准
        double viewYaw = Math.toRadians(player.getYRot() + cameraDrift(effectTicks) + cameraWobbleYaw(effectTicks));
        Vec3 view = new Vec3(-Math.sin(viewYaw), 0.0D, Math.cos(viewYaw));
        boolean viewOk = view.dot(dirToFish) > VIEW_DOT_THRESHOLD;

// 脚步：把 WASD 输入换算成世界方向，再和朝向浮漂的方向做点积
        double yawRad = Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0.0D, Math.cos(yawRad));
        Vec3 left = new Vec3(Math.cos(yawRad), 0.0D, Math.sin(yawRad));
        Vec3 move = left.scale(player.input.leftImpulse).add(forward.scale(player.input.forwardImpulse));
// 站定不动同样是「稳住身形」，不必为了凑进度一直按着方向键后撤
        boolean moveOk = move.lengthSqr() < 1.0E-4D
                || move.normalize().dot(dirToFish) < MOVE_DOT_THRESHOLD;

        return viewOk && moveOk;
    }

// 鱼把玩家朝浮漂方向拖行；拖行幅度按「鱼最大拉力 / 钓竿最大拉力」缩放
    private static void applyDrag(LocalPlayer player, Vec3 dirToFish) {
        if (player.isPassenger()) {
            return;
        }
        Vec3 delta = player.getDeltaMovement();
        player.setDeltaMovement(delta.add(dirToFish.scale(DRAG_SPEED * tensionFactor)));
    }

// 把玩家约束在「阶段开始时所在位置」为圆心、#ARENA_RADIUS 为半径的圆圈内。
//
// 遛鱼只考察操作本身，不需要玩家真的往后退：不加约束的话，玩家为了凑够进度会一路后撤，
// 一路能退到十几格开外；反过来，鱼的拖拽也会把原地不动的玩家一路拽进水里。
// 约束之后双方表现为原地拔河，谁也没法把对方拖远。
    private static void clampToArena(LocalPlayer player) {
        if (player.isPassenger()) {
            return;
        }
        double dx = player.getX() - arenaCenter.x;
        double dz = player.getZ() - arenaCenter.z;
        double distSqr = dx * dx + dz * dz;
        if (distSqr <= ARENA_RADIUS * ARENA_RADIUS) {
            return;
        }
        double dist = Math.sqrt(distSqr);
        double nx = dx / dist;
        double nz = dz / dist;
        player.setPos(arenaCenter.x + nx * ARENA_RADIUS, player.getY(), arenaCenter.z + nz * ARENA_RADIUS);
// 同时抵消继续向外的速度分量，否则贴边时会被推着抖动
        Vec3 delta = player.getDeltaMovement();
        double outward = delta.x * nx + delta.z * nz;
        if (outward > 0.0D) {
            player.setDeltaMovement(delta.x - nx * outward, delta.y, delta.z - nz * outward);
        }
    }

// 浮漂围绕入水锚点乱动。水平方向由三组互不通约的频率叠加而成，走出一条看不出规律的乱窜路径，
// 再叠上高频细碎抖动，表现鱼在水面反复挣扎、乱拉鱼线。
//
// 其中还掺入一份「镜头被拽方向」的分量（echo），让浮漂与视角互相呼应；
// 但主体是独立的水平乱窜，否则位移会被镜头的世界旋转抵消掉、玩家根本看不出来。
//
// 原版 FishingHook#lerpTo 是空实现，客户端浮漂位置由本地模拟决定；
// 而鱼线渲染取的是 entity.getPosition(partialTick)，所以只要在这里改浮漂位置，
// 鱼线就会随浮漂一起动。
    private static void jitterBobber(FishingHook hook, LocalPlayer player, Vec3 dirToFish) {
        float t = effectTicks;
// 三组互不通约的频率（周期约 33 / 13 / 6 tick）叠加出的水平乱窜
        double roamX = Mth.sin(t * 0.19F) * 0.5D + Mth.sin(t * 0.47F + 2.1F) * 0.3D + Mth.sin(t * 1.13F + 0.7F) * 0.2D;
        double roamZ = Mth.cos(t * 0.23F + 1.3F) * 0.5D + Mth.cos(t * 0.41F + 0.4F) * 0.3D + Mth.cos(t * 0.97F + 2.6F) * 0.2D;
// 与「yaw 增大」同侧的单位向量，正是镜头被拽向的一侧
        Vec3 side = new Vec3(-dirToFish.z, 0.0D, dirToFish.x);
        double echo = Math.sin(Math.toRadians(cameraDrift(t))) * BOBBER_SWAY;
        double jitterX = (player.getRandom().nextDouble() - 0.5D) * BOBBER_JITTER;
        double jitterZ = (player.getRandom().nextDouble() - 0.5D) * BOBBER_JITTER;
// 鱼挣扎时上下翻腾
        double bob = Mth.sin(t * 0.53F) * BOBBER_BOB;
        hook.setPos(
                anchor.x + roamX * BOBBER_ROAM + echo * side.x + jitterX,
                anchor.y + bob,
                anchor.z + roamZ * BOBBER_ROAM + echo * side.z + jitterZ);
    }

// 在浮漂处持续炸开水花与气泡，表现鱼在水下剧烈挣扎
    private static void spawnSplash(Minecraft minecraft, FishingHook hook) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        RandomSource random = level.random;
        double x = hook.getX();
        double y = hook.getY();
        double z = hook.getZ();

// 向外飞溅的水滴
        for (int i = 0; i < 12; i++) {
            double dx = (random.nextDouble() - 0.5D) * 1.1D;
            double dz = (random.nextDouble() - 0.5D) * 1.1D;
            double dy = random.nextDouble() * 0.5D + 0.15D;
            level.addParticle(ParticleTypes.SPLASH,
                    x + dx * 0.25D, y + 0.1D, z + dz * 0.25D,
                    dx * 1.5D, dy, dz * 1.5D);
        }
// 水下涌上来的气泡
        for (int i = 0; i < 4; i++) {
            level.addParticle(ParticleTypes.BUBBLE_POP,
                    x + (random.nextDouble() - 0.5D) * 0.5D,
                    y - 0.05D,
                    z + (random.nextDouble() - 0.5D) * 0.5D,
                    0.0D, 0.05D, 0.0D);
        }
// 偶尔来一簇碎屑粒子，像是鱼翻出水面
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.FISHING,
                    x + (random.nextDouble() - 0.5D) * 0.4D,
                    y + 0.15D,
                    z + (random.nextDouble() - 0.5D) * 0.4D,
                    0.0D, 0.0D, 0.0D);
        }
    }

// 播放鱼挣扎的溅水声，音调随机以免机械重复
    private static void playThrashSound(Minecraft minecraft, FishingHook hook) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        RandomSource random = level.random;
        SoundEvent sound = random.nextBoolean() ? SoundEvents.FISHING_BOBBER_SPLASH : SoundEvents.GENERIC_SPLASH;
        level.playLocalSound(hook.getX(), hook.getY(), hook.getZ(), sound, SoundSource.NEUTRAL,
                0.55F + random.nextFloat() * 0.25F, 0.7F + random.nextFloat() * 0.6F, false);
    }

// 鱼把镜头往旁边拽走的主拉力（度），周期约 2.4 秒 / 5 秒，幅度按「鱼最大拉力 / 钓竿最大拉力」缩放
    private static float cameraDrift(float time) {
        return (Mth.sin(time * 0.13F) * DRIFT_DEG_A + Mth.sin(time * 0.061F + 1.7F) * DRIFT_DEG_B) * tensionFactor;
    }

// 综合抖动强度：基础紧张感 + 配件加成（钓组越显眼晃得越厉害）
    private static float shakeIntensity() {
        return BASE_SHAKE + shakeAmount;
    }

// 鱼挣扎时的画面颤动（度）。三轴都用「低频摇摆 + 高频细颤」两层不通约的正弦叠加，
// 走不出规律、像真的在跟鱼拔河，凸显紧张感。
    private static float cameraWobbleYaw(float time) {
        return (Mth.sin(time * 0.42F) * 1.1F + Mth.sin(time * 0.19F + 0.9F) * 0.8F
                + Mth.sin(time * 1.63F + 2.4F) * 0.35F) * shakeIntensity();
    }

    private static float cameraWobblePitch(float time) {
        return (Mth.cos(time * 0.31F) * 0.9F + Mth.sin(time * 1.87F + 1.2F) * 0.28F) * shakeIntensity();
    }

// 横滚抖动：画面轻微倾斜，颤动感最明显，幅度最小以免晕
    private static float cameraWobbleRoll(float time) {
        return (Mth.sin(time * 0.27F) * 0.7F + Mth.sin(time * 1.41F + 0.5F) * 0.22F) * shakeIntensity();
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!active) {
            return;
        }
        float time = effectTicks + (float) event.getPartialTick();
        event.setYaw(event.getYaw() + cameraDrift(time) + cameraWobbleYaw(time));
        event.setPitch(Mth.clamp(event.getPitch() + cameraWobblePitch(time), -90.0F, 90.0F));
        event.setRoll(event.getRoll() + cameraWobbleRoll(time));
    }
}
