package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.all.ModBlocks;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// 林业网点的规则中心（全部在服务端结算）。
//
// 网点按玩家保存：玩家用护林员写字板右键一株原木即标记一处网点，右键写字板本体打开界面。
// 一个区块只允许一处网点，且要求该区块内至少 #MIN_TREES 棵树。
// 每天早上按概率给每个网点掷一次随机情况（森林火灾 / 蚜虫侵蚀），处理掉加分、
// 拖着不处理逐日扣分；评分够高的网点「兴盛」，所在区块享受各项加成（见 ForestryEffects）。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class ForestryNetworkManager {

// 玩家存档里存网点表的 NBT 键
    private static final String NBT_KEY = "fida_forestry";
// 在线玩家 → 网点表（仅服务端持有）
    private static final Map<UUID, ForestryNetwork> SERVER = new HashMap<>();
// 已结算到第几个游戏日
    private static long lastProcessedDay = -1L;

// 一个区块至少要有这么多棵树才算一个网点
    public static final int MIN_TREES = 5;
// 标记时向上 / 向下扫描原木的范围（格）
    private static final int TREE_SCAN_RANGE = 64;
// 每个网点每天早上出现随机情况的概率
    private static final float EVENT_CHANCE = 0.35F;
// 一次火灾冒出的火焰数量上限
    private static final int FIRE_BLOCKS = 6;
// 灭火器作用半径
    public static final int EXTINGUISH_RADIUS = 5;
// 灭火器喷向准星方向的伸出距离
    private static final double EXTINGUISH_REACH = 4.0D;
// 一次杂草丛生事件在区块内冒出的杂草数量上限
    private static final int WEED_BLOCKS = 28;
// 除草剂清除杂草的半径
    public static final int HERBICIDE_RADIUS = 4;
// 水质综合评分低于这个值时，每天按水质差扣一点网点评分
    private static final int WATER_PENALTY_THRESHOLD = 55;
// 每日掷骰能随机冒出来的事件（水质灾难不参与，只能测量上报）
    private static final ForestryEventType[] RANDOM_EVENTS = {
            ForestryEventType.FIRE, ForestryEventType.APHIDS, ForestryEventType.WEEDS,
            ForestryEventType.FLOOD, ForestryEventType.DROUGHT};

    private ForestryNetworkManager() {
    }

// 存取与同步

    public static ForestryNetwork get(ServerPlayer player) {
        return SERVER.computeIfAbsent(player.getUUID(), id -> load(player));
    }

    private static ForestryNetwork load(ServerPlayer player) {
        ForestryNetwork network = new ForestryNetwork();
        CompoundTag data = player.getPersistentData();
        if (data.contains(NBT_KEY)) {
            network.load(data.getCompound(NBT_KEY));
        }
        return network;
    }

// 写回玩家存档并把最新网点表全量同步给该玩家
    private static void persist(ServerPlayer player) {
        player.getPersistentData().put(NBT_KEY, get(player).save());
        sync(player);
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ForestrySyncPacket(get(player).encodeAll()));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
// 重新从存档读，避免跨世界 / 重进时读到上一份缓存
            SERVER.remove(player.getUUID());
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SERVER.remove(player.getUUID());
        }
    }

// 标记 / 解除

// 用写字板右键一格原木。
//
// sneaking：潜行时为解除该区块的网点，否则为标记新网点
    public static void useOnLog(ServerPlayer player, BlockPos pos, boolean sneaking) {
        ResourceLocation dim = player.level().dimension().location();
        long chunkKey = ChunkPos.asLong(pos);
        ForestryNetwork network = get(player);

        if (sneaking) {
            if (network.removeChunk(dim, chunkKey)) {
                persist(player);
                notify(player, "message.flavor_immersed_daily_argo.forestry.removed");
            } else {
                notify(player, "message.flavor_immersed_daily_argo.forestry.none_here");
            }
            return;
        }

        if (network.hasChunk(dim, chunkKey)) {
            notify(player, "message.flavor_immersed_daily_argo.forestry.chunk_taken");
            return;
        }
        int trees = countTrees((ServerLevel) player.level(), pos);
        if (trees < MIN_TREES) {
            player.displayClientMessage(Component.translatable(
                    "message.flavor_immersed_daily_argo.forestry.need_trees", MIN_TREES, trees), true);
            return;
        }
        network.add(ForestryPoint.create(dim, pos, currentDay(player.getServer())));
        persist(player);
        notify(player, "message.flavor_immersed_daily_argo.forestry.created");
    }

// 界面里点击「解除网点」
    public static void remove(ServerPlayer player, ResourceLocation dimension, BlockPos pos) {
        if (get(player).removeAt(dimension, pos)) {
            persist(player);
        }
    }

// 界面里给网点改名（空名恢复默认名）
    public static void rename(ServerPlayer player, ResourceLocation dimension, BlockPos pos, String name) {
        ForestryPoint point = get(player).at(dimension, pos);
        if (point == null) {
            return;
        }
        point.setName(name);
        persist(player);
    }

// 指令入口：让玩家在指定坐标的网点立即触发一次随机情况；坐标为空时取离玩家最近的一处网点。
//
// 返回：是否成功触发（没有网点、或火实在放不下时为 false）
    public static boolean triggerEvent(ServerPlayer player, @Nullable BlockPos pos, ForestryEventType type) {
        ServerLevel level = player.serverLevel();
        ResourceLocation dim = level.dimension().location();
        ForestryNetwork network = get(player);
        ForestryPoint point = pos != null ? network.at(dim, pos) : nearest(network, dim, player.blockPosition());
        if (point == null) {
            return false;
        }
        long day = currentDay(level.getServer());
        RandomSource random = player.getRandom();
// 水质灾难走「上报」通道：不占事件位，只写进水体情况，同时把对应指标压到灾难区间
        if (type.isWaterQuality()) {
            point.setMetrics(ForestryWater.applyDisaster(
                    new int[]{point.oxygen(), point.ph(), point.organic(),
                            point.conductivity(), point.turbidity()}, type));
            point.setWaterReport(type);
            persist(player);
            return true;
        }
        if (type == ForestryEventType.FIRE && !spawnFire(level.getServer(), point)) {
            return false;
        }
        if (type == ForestryEventType.WEEDS) {
            spawnWeeds(level.getServer(), point);
        }
        if (type == ForestryEventType.FLOOD || type == ForestryEventType.DROUGHT) {
            point.setWaterLevel(ForestryWater.anomalousLevel(random, type));
        } else {
            point.setWaterLevel(ForestryWater.normalLevel(random));
        }
        point.startEvent(type, day);
        persist(player);
        return true;
    }

// 玩家在当前维度里离 from 最近的一处网点
    private static ForestryPoint nearest(ForestryNetwork network, ResourceLocation dimension, BlockPos from) {
        ForestryPoint best = null;
        double bestDist = Double.MAX_VALUE;
        for (ForestryPoint point : network.points()) {
            if (!point.dimension().equals(dimension)) {
                continue;
            }
            double dist = point.pos().distSqr(from);
            if (dist < bestDist) {
                bestDist = dist;
                best = point;
            }
        }
        return best;
    }

// 手持树漆右键网点的树：把该区块上处于「蚜虫侵蚀」的网点清掉（任意玩家皆可处理）。
//
// 返回：是否真的消除了一处蚜虫
    public static boolean paintTree(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        ResourceLocation dim = level.dimension().location();
        long chunkKey = ChunkPos.asLong(pos);
        long day = currentDay(level.getServer());
        for (ServerPlayer owner : level.getServer().getPlayerList().getPlayers()) {
            ForestryPoint point = get(owner).atChunk(dim, chunkKey);
            if (point != null && point.event() == ForestryEventType.APHIDS) {
                point.resolve(day);
                persist(owner);
                return true;
            }
        }
        return false;
    }

// 除草剂：清除以 center 为中心一片范围内的杂草，并把命中区块上「杂草丛生」的网点消解掉（加分）。
//
// 返回：一共清除了多少丛杂草
    public static int clearWeeds(ServerPlayer player, BlockPos center) {
        ServerLevel level = player.serverLevel();
        Set<Long> touchedChunks = new HashSet<>();
        int cleared = 0;
        int radius = HERBICIDE_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (isWeed(level.getBlockState(pos))) {
                level.removeBlock(pos, false);
                touchedChunks.add(ChunkPos.asLong(pos));
                cleared++;
            }
        }
        if (!touchedChunks.isEmpty()) {
            long day = currentDay(level.getServer());
            ResourceLocation dim = level.dimension().location();
            for (ServerPlayer owner : level.getServer().getPlayerList().getPlayers()) {
                boolean changed = false;
                for (ForestryPoint point : get(owner).points()) {
                    if (point.dimension().equals(dim) && point.event() == ForestryEventType.WEEDS
                            && touchedChunks.contains(point.chunkKey())) {
                        point.resolve(day);
                        changed = true;
                    }
                }
                if (changed) {
                    persist(owner);
                }
            }
        }
        level.playSound(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D,
                SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        return cleared;
    }

// 是否是可以被除草剂清掉的杂草
    private static boolean isWeed(BlockState state) {
        return state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.DEAD_BUSH);
    }

// 气象仪器查询：该位置所在区块的网点是否正遭遇「非常规天气」（火灾 / 洪水 / 干涸 / 酸雨）。
// 火灾 / 洪水 / 干涸看事件位，酸雨这类水质灾难看玩家上报的水体情况。
    public static boolean hasWeatherEvent(Level level, BlockPos pos) {
        ResourceLocation dim = level.dimension().location();
        long chunkKey = ChunkPos.asLong(pos);
        for (ForestryNetwork network : SERVER.values()) {
            ForestryPoint point = network.atChunk(dim, chunkKey);
            if (point != null && (point.event().isWeather() || point.waterReport().isWeather())) {
                return true;
            }
        }
        return false;
    }

// 水位尺 / 水质检测器用：玩家当前维度里离 from 最近的一处网点
    public static ForestryPoint nearestPoint(ServerPlayer player, BlockPos from) {
        return nearest(get(player), player.level().dimension().location(), from);
    }

// 水质检测器：把测到的确切灾难名上报到护林员写字板（写进水体情况）；传 NONE 表示水质正常、清除上报
    public static void reportWater(ServerPlayer player, ForestryPoint point, ForestryEventType type) {
        point.setWaterReport(type == null ? ForestryEventType.NONE : type);
        persist(player);
    }

// 水质药剂：把投放后改好的五项指标写回网点并保存同步
    public static void saveWater(ServerPlayer player, ForestryPoint point, int[] metrics) {
        point.setMetrics(metrics);
        persist(player);
    }

// 灭火器：扑灭准星前方一片范围内的火（含本模组的林业火灾与原版火）。
// 对应的网点火灾事件随之消解并加分。
//
// 返回：是否扑灭到了任何火
    public static boolean extinguish(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 target = player.getEyePosition().add(player.getLookAngle().scale(EXTINGUISH_REACH));
        BlockPos center = BlockPos.containing(target);
        ResourceLocation dim = level.dimension().location();

        Set<Long> touchedChunks = new HashSet<>();
        boolean any = false;
        int radius = EXTINGUISH_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.FORESTRY_FIRE.get()) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                level.removeBlock(pos, false);
                touchedChunks.add(ChunkPos.asLong(pos));
                any = true;
            }
        }

        if (!touchedChunks.isEmpty()) {
            long day = currentDay(level.getServer());
            for (ServerPlayer owner : level.getServer().getPlayerList().getPlayers()) {
                boolean changed = false;
                for (ForestryPoint point : get(owner).points()) {
                    if (point.dimension().equals(dim) && point.event() == ForestryEventType.FIRE
                            && touchedChunks.contains(point.chunkKey())) {
                        point.resolve(day);
                        changed = true;
                    }
                }
                if (changed) {
                    persist(owner);
                }
            }
        }

// 粒子与音效（服务端广播，附近玩家都看得见）
        level.sendParticles(ParticleTypes.CLOUD, target.x, target.y, target.z,
                24, 0.4D, 0.4D, 0.4D, 0.02D);
        level.sendParticles(ParticleTypes.SMOKE, target.x, target.y, target.z,
                12, 0.5D, 0.5D, 0.5D, 0.01D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        return any;
    }

// 每日结算

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
            MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return;
        }
        long day = overworld.getDayTime() / 24000L;
        if (day < lastProcessedDay) {
// 时间回退 / 换了新世界，重新对齐
            lastProcessedDay = day;
            return;
        }
        if (day <= lastProcessedDay) {
            return;
        }
        lastProcessedDay = day;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            processDay(player, day);
        }
    }

// 一个游戏日推进一次：掷随机情况、结算拖延扣分
    private static void processDay(ServerPlayer player, long day) {
        ForestryNetwork network = get(player);
        if (network.isEmpty()) {
            return;
        }
        boolean changed = false;
        boolean newEvent = false;
        boolean decayed = false;
        RandomSource random = player.getRandom();
        for (ForestryPoint point : network.points()) {
// 水质每日漂移一次（浊度反过来影响有机物质与溶解氧）
            point.setMetrics(ForestryWater.drift(random, point.oxygen(), point.ph(),
                    point.organic(), point.conductivity(), point.turbidity()));
            changed = true;

// 水质综合评分偏低就扣一点网点评分：水质灾难不会直接出现在写字板上，最多就是降评分，
// 玩家得自己用水质检测器测出来、把确切的灾难名上报上去
            if (ForestryWater.comprehensive(point.oxygen(), point.ph(),
                    point.organic(), point.conductivity(), point.turbidity()) < WATER_PENALTY_THRESHOLD) {
                point.addScore(-1);
            }

            if (point.event() != ForestryEventType.NONE) {
// 火灾的火若已被打掉（徒手挖掉等），直接清事件、不加分，避免死局
                if (point.event() == ForestryEventType.FIRE && !hasFire(player.getServer(), point)) {
                    point.clearEvent();
                    changed = true;
                    continue;
                }
                if (day - point.eventSinceDay() >= ForestryPoint.GRACE_DAYS) {
                    point.addScore(-ForestryPoint.DECAY_PER_DAY);
                    decayed = true;
                    changed = true;
                }
            } else if (random.nextFloat() < EVENT_CHANCE) {
                ForestryEventType type = randomEvent(random);
                if (type == ForestryEventType.FIRE && !spawnFire(player.getServer(), point)) {
// 附近实在放不下火就算了，不留无法处理的死事件
                    continue;
                }
                if (type == ForestryEventType.WEEDS) {
                    spawnWeeds(player.getServer(), point);
                }
                if (type == ForestryEventType.FLOOD || type == ForestryEventType.DROUGHT) {
                    point.setWaterLevel(ForestryWater.anomalousLevel(random, type));
                } else {
                    point.setWaterLevel(ForestryWater.normalLevel(random));
                }
                point.startEvent(type, day);
                newEvent = true;
                changed = true;
            }
        }
        if (changed) {
            persist(player);
        }
        if (newEvent) {
            notify(player, "message.flavor_immersed_daily_argo.forestry.new_event");
        }
        if (decayed) {
            notify(player, "message.flavor_immersed_daily_argo.forestry.decayed");
        }
    }

// 加成查询

// 该位置所在区块是否有「兴盛」网点（服务端调用）
    public static boolean isProsperous(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }
        ResourceLocation dim = level.dimension().location();
        long chunkKey = ChunkPos.asLong(pos);
        for (ForestryNetwork network : SERVER.values()) {
            ForestryPoint point = network.atChunk(dim, chunkKey);
            if (point != null && point.prosperous()) {
                return true;
            }
        }
        return false;
    }

// 工具

// 统计该区块内的「树」数量：按不同原木柱（x,z）计，标记格上下一定范围内扫到原木就算一株
    private static int countTrees(ServerLevel level, BlockPos center) {
        Set<Long> columns = new HashSet<>();
        int minX = (center.getX() >> 4) << 4;
        int minZ = (center.getZ() >> 4) << 4;
        int minY = Math.max(level.getMinBuildHeight(), center.getY() - TREE_SCAN_RANGE);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, center.getY() + TREE_SCAN_RANGE);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (level.getBlockState(cursor.set(x, y, z)).is(BlockTags.LOGS)) {
                        columns.add(BlockPos.asLong(x, 0, z));
                        break;
                    }
                }
            }
        }
        return columns.size();
    }

// 在可随机触发的事件里挑一个（森林火灾 / 蚜虫侵蚀 / 杂草丛生 / 洪水 / 干涸）
    private static ForestryEventType randomEvent(RandomSource random) {
        return RANDOM_EVENTS[random.nextInt(RANDOM_EVENTS.length)];
    }

// 在网点所在区块的地表冒出一片杂草（杂草丛生事件）
    private static void spawnWeeds(MinecraftServer server, ForestryPoint point) {
        ServerLevel level = server.getLevel(dimensionKey(point.dimension()));
        if (level == null || !level.isLoaded(point.pos())) {
            return;
        }
        ChunkPos chunk = new ChunkPos(point.pos());
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
                    candidates.add(pos);
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Collections.shuffle(candidates);
        int count = Math.min(WEED_BLOCKS, candidates.size());
        for (int i = 0; i < count; i++) {
            level.setBlockAndUpdate(candidates.get(i), Blocks.GRASS.defaultBlockState());
        }
    }

// 在网点周围冒出一圈林业火灾（不蔓延、不熄、不毁方块）
    private static boolean spawnFire(MinecraftServer server, ForestryPoint point) {
        ServerLevel level = server.getLevel(dimensionKey(point.dimension()));
        if (level == null || !level.isLoaded(point.pos())) {
            return false;
        }
        List<BlockPos> candidates = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    BlockPos pos = point.pos().offset(dx, dy, dz);
                    if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
                        candidates.add(pos);
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    for (int dy = -1; dy <= 3; dy++) {
                        BlockPos pos = point.pos().offset(dx, dy, dz);
                        if (level.getBlockState(pos).isAir()) {
                            candidates.add(pos);
                        }
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }
        Collections.shuffle(candidates);
        int count = Math.min(FIRE_BLOCKS, candidates.size());
        for (int i = 0; i < count; i++) {
            level.setBlockAndUpdate(candidates.get(i), ModBlocks.FORESTRY_FIRE.get().defaultBlockState());
        }
        return true;
    }

// 网点附近是否还有林业火灾（用来判断火是否已经被清掉）
    private static boolean hasFire(MinecraftServer server, ForestryPoint point) {
        ServerLevel level = server.getLevel(dimensionKey(point.dimension()));
        if (level == null || !level.isLoaded(point.pos())) {
// 区块没加载就先当作还在，等加载了再判断
            return true;
        }
        for (BlockPos pos : BlockPos.betweenClosed(
                point.pos().offset(-3, -2, -3), point.pos().offset(3, 3, 3))) {
            if (level.getBlockState(pos).is(ModBlocks.FORESTRY_FIRE.get())) {
                return true;
            }
        }
        return false;
    }

    private static ResourceKey<Level> dimensionKey(ResourceLocation dimension) {
        return ResourceKey.create(Registries.DIMENSION, dimension);
    }

    private static long currentDay(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld == null ? 0L : overworld.getDayTime() / 24000L;
    }

    private static void notify(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

// 供界面判断是否拥有写字板权限等（预留）：当前玩家是否持有写字板
    public static boolean holdingClipboard(Player player) {
        return player.getMainHandItem().is(com.flavor_immersed_daily_argo.all.ModItems.RANGER_CLIPBOARD.get())
                || player.getOffhandItem().is(com.flavor_immersed_daily_argo.all.ModItems.RANGER_CLIPBOARD.get());
    }
}
