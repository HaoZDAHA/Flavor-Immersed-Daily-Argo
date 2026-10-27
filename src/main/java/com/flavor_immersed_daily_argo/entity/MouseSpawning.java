package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.config.Config;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.SickleItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;

// 老鼠的自然生成、开关指令，以及「猫主动猎鼠」的挂载。
//
// 老鼠不像原版怪那样刷在刷怪笼里，而是自己找饭辙：每隔一阵子，在玩家附近的作物周边挑个落脚点，
// 挑中了就放一只老鼠（见 #trySpawn）。自然生成默认关闭（主配置
// mouseNaturalSpawning），想让它刷出来可以在配置里打开，也可以临时用
// /mouse spawn <true|false> 覆盖；刷怪蛋与指令生成的个体不受这个开关限制。
//
// 毒饵站（PoisonBaitStation）罩住的范围内不刷老鼠：刷怪点落进它 PoisonBaitStation#RADIUS 格的
// 圈里就会被打回，毒饵站本身则不会因此损坏。
//
// 猫平时不主动招惹谁，这里在每只猫入场时给它挂一条「猎鼠」的目标选择器（攻击动作交由原版猫自带的
// OcelotAttackGoal 处理），于是猫见着老鼠就会自己扑上去。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class MouseSpawning {

// 每隔这么多 tick 尝试一次生成
    private static final int INTERVAL_TICKS = 200;

// 刷怪点与玩家的距离区间（格）：太近突兀，太远看不见
    private static final double MIN_DISTANCE = 6.0D;
    private static final double MAX_DISTANCE = 16.0D;

// 每轮给每名玩家的生成概率
    private static final float SPAWN_CHANCE = 0.4F;

// 作物判定的小范围半径（格）
    private static final int CROP_CHECK_RADIUS = 3;

// 猫身上记「猎鼠目标已挂过」的键，避免区块每次读档都重复挂一遍
    private static final String MOUSE_HUNT_KEY = "FlavorImmersedDailyMouseHunt";

// 指令临时覆盖自然生成开关；null = 跟随主配置
    private static Boolean spawningOverride;

    private MouseSpawning() {
    }

// 指令改过就听指令的，否则读主配置（默认关）
    private static boolean spawningEnabled() {
        return spawningOverride != null ? spawningOverride : Config.MOUSE_NATURAL_SPAWNING.get();
    }

// 自然生成

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
            if (!spawningEnabled() || event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                if (level.getRandom().nextFloat() <= SPAWN_CHANCE) {
                    trySpawn(level, player);
                }
            }
        }
    }

// 在玩家附近的作物旁边试着放一只老鼠；毒饵站拦下就作罢
    private static void trySpawn(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            BlockPos column = new BlockPos(
                    Mth.floor(player.getX() + Math.cos(angle) * distance),
                    level.getMinBuildHeight(),
                    Mth.floor(player.getZ() + Math.sin(angle) * distance));
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            if (!level.isLoaded(surface) || !hasMatureCropNear(level, surface)) {
                continue;
            }
// 每个区块同时最多只留 config 里设定的那么多只，够了就换个落点再试
            if (miceInChunk(level, surface) >= Config.MOUSE_CHUNK_LIMIT.get()) {
                continue;
            }
// 找到落脚点了：毒饵站罩住这片地就不再刷老鼠（毒饵站本身不会损坏）
            if (!level.getEntitiesOfClass(PoisonBaitStation.class,
                    new AABB(surface).inflate(PoisonBaitStation.RADIUS)).isEmpty()) {
                return;
            }
            Mouse mouse = ModEntities.MOUSE.get().create(level);
            if (mouse == null) {
                return;
            }
            mouse.moveTo(surface.getX() + 0.5D, surface.getY(), surface.getZ() + 0.5D,
                    random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mouse);
            return;
        }
    }

// 这个方块所在区块里现在有几只老鼠
    private static int miceInChunk(ServerLevel level, BlockPos pos) {
        int minX = (pos.getX() >> 4) << 4;
        int minZ = (pos.getZ() >> 4) << 4;
        AABB chunkBox = new AABB(minX, level.getMinBuildHeight(), minZ,
                minX + 16, level.getMaxBuildHeight(), minZ + 16);
        return level.getEntitiesOfClass(Mouse.class, chunkBox).size();
    }

// 落脚点周围有没有成熟作物
    private static boolean hasMatureCropNear(ServerLevel level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-CROP_CHECK_RADIUS, -1, -CROP_CHECK_RADIUS),
                center.offset(CROP_CHECK_RADIUS, 1, CROP_CHECK_RADIUS))) {
            if (SickleItem.isMatureCrop(level.getBlockState(pos))) {
                return true;
            }
        }
        return false;
    }

// 猫主动猎鼠

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Cat cat)) {
            return;
        }
// 区块每次读档都会走这里，必须去重，否则目标选择器会越积越多
        if (cat.getPersistentData().getBoolean(MOUSE_HUNT_KEY)) {
            return;
        }
        cat.getPersistentData().putBoolean(MOUSE_HUNT_KEY, true);
// 原版猫自带 OcelotAttackGoal，只要给它一个「老鼠」目标就会自己扑
        cat.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(cat, Mouse.class, true));
    }

// 开关指令

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("mouse")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    spawningOverride = BoolArgumentType.getBool(context, "enabled");
                                    context.getSource().sendSuccess(() -> Component.translatable(
                                            spawningEnabled()
                                                    ? "message.flavor_immersed_daily_argo.mouse.spawn_on"
                                                    : "message.flavor_immersed_daily_argo.mouse.spawn_off"), true);
                                    return 1;
                                }))));
    }
}
