package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.config.Config;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookData;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// 本模组动物的自然生成。
//
// 三类开关都在主配置里，默认全部关闭——模组动物的默认来路是商业电脑「网购」里的刷怪蛋，
// 自然生成只是给想改造世界的玩家留的选项：
//
// landAnimalNaturalSpawning：陆地禽畜（鸭、鹅、鹌鹑、鸽、火鸡、牛蛙、竹鼠）刷在草地上；
// freshwaterNaturalSpawning：淡水水产刷在非海洋群系的水里；
// saltwaterNaturalSpawning：海水水产刷在海洋群系的水里。
//
// 陆地按群系挑种类：竹林出竹鼠，沼泽 / 红树林出牛蛙，河岸 / 沙滩出鸭，其余随机家禽。
// 水产直接复用钓鱼手册里的 habitat 与 biomes 数据（只取本模组自己的实体），
// 所以新加的鱼只要写好手册条目就会自动参与自然生成。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class AnimalSpawning {

// 每隔这么多 tick 尝试一次（20 秒）
    private static final int INTERVAL_TICKS = 400;

// 生成点与玩家的距离区间（格）
    private static final double MIN_DISTANCE = 8.0D;
    private static final double MAX_DISTANCE = 32.0D;

// 每轮最多试几个落点
    private static final int ATTEMPTS = 10;

// 每轮给每名玩家的生成概率
    private static final float CHANCE = 0.35F;

// 陆地生物一次最多刷几只
    private static final int MAX_GROUP = 3;

    private AnimalSpawning() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
            boolean land = Config.LAND_ANIMAL_NATURAL_SPAWNING.get();
        boolean fresh = Config.FRESHWATER_NATURAL_SPAWNING.get();
        boolean salt = Config.SALTWATER_NATURAL_SPAWNING.get();
        if ((!land && !fresh && !salt)
                || event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                    || level.players().isEmpty()) {
                continue;
            }
            for (ServerPlayer player : level.players()) {
                if (level.getRandom().nextFloat() > CHANCE) {
                    continue;
                }
                if (land) {
                    trySpawnLand(level, player);
                }
                if (fresh) {
                    trySpawnWater(level, player, false);
                }
                if (salt) {
                    trySpawnWater(level, player, true);
                }
            }
        }
    }

// 陆地禽畜

    private static void trySpawnLand(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            BlockPos surface = randomSurfaceNear(level, player, random);
            if (surface == null) {
                continue;
            }
            BlockState ground = level.getBlockState(surface.below());
            if (!ground.is(BlockTags.DIRT) && !ground.is(BlockTags.SAND)
                    && !ground.is(Blocks.GRAVEL) && !ground.is(Blocks.MUD)) {
                continue;
            }
            if (!level.getBlockState(surface).isAir() || !level.getBlockState(surface.above()).isAir()) {
                continue;
            }
            EntityType<?> type = pickLandType(level, surface);
            spawnGroup(level, type, surface, random, 1 + random.nextInt(MAX_GROUP));
            return;
        }
    }

// 按群系挑一种陆地禽畜
    private static EntityType<?> pickLandType(ServerLevel level, BlockPos pos) {
        String biome = level.getBiome(pos).unwrapKey().orElseThrow().location().getPath();
        if (biome.contains("bamboo_jungle")) {
            return ModEntities.BAMBOORAT.get();
        }
        if (biome.contains("swamp") || biome.contains("mangrove")) {
            return ModEntities.BULLFROG.get();
        }
        if (biome.contains("river") || biome.contains("beach")) {
            return ModEntities.DUCK.get();
        }
        EntityType<?>[] poultry = {
                ModEntities.DUCK.get(),
                ModEntities.GOOSE.get(),
                ModEntities.QUAIL.get(),
                ModEntities.PIGEON.get(),
                ModEntities.TURKEY.get()
        };
        return poultry[level.getRandom().nextInt(poultry.length)];
    }

// 淡水 / 海水水产

    private static void trySpawnWater(ServerLevel level, ServerPlayer player, boolean ocean) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            BlockPos column = randomColumnNear(level, player, random);
            if (column == null) {
                continue;
            }
            ResourceLocation biome = level.getBiome(column).unwrapKey().orElseThrow().location();
            if (isOcean(biome.getPath()) != ocean) {
                continue;
            }
// OCEAN_FLOOR 忽略水，取到的是水柱最下面一格
            BlockPos water = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, column);
            if (!level.getFluidState(water).is(FluidTags.WATER)) {
                continue;
            }
            EntityType<?> type = pickAquaticType(level, biome, ocean, random);
            if (type == null) {
                continue;
            }
            Entity entity = type.create(level);
            if (entity == null) {
                continue;
            }
            entity.moveTo(water.getX() + 0.5D, water.getY() + 0.2D, water.getZ() + 0.5D,
                    random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()),
                        MobSpawnType.NATURAL, null, null);
            }
            level.addFreshEntity(entity);
            return;
        }
    }

// 从钓鱼手册里挑一条「水域相符、群系相符」的本模组水产
    @Nullable
    private static EntityType<?> pickAquaticType(ServerLevel level, ResourceLocation biome,
                                                 boolean ocean, RandomSource random) {
        List<FishingHandbookEntry> preferred = new ArrayList<>();
        List<FishingHandbookEntry> fallback = new ArrayList<>();
        for (FishingHandbookEntry entry : FishingHandbookData.getInstance().all()) {
            if (!entry.hasEntity() || entry.entityId == null
                    || !entry.entityId.getNamespace().equals(flavor_immersed_daily_argo.MODID)
                    || !entry.habitatMatches(ocean)) {
                continue;
            }
            fallback.add(entry);
            if (entry.biomesMatch(biome)) {
                preferred.add(entry);
            }
        }
        List<FishingHandbookEntry> pool = preferred.isEmpty() ? fallback : preferred;
        if (pool.isEmpty()) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(pool.get(random.nextInt(pool.size())).entityId)
                .orElse(null);
    }

// 判断生物群系是不是海洋，与钓竿 / 捕捞渔网里那套保持一致
    private static boolean isOcean(String path) {
        return path.contains("ocean");
    }

// 公用

// 在玩家四周随机取一列，返回地表位置
    @Nullable
    private static BlockPos randomSurfaceNear(ServerLevel level, ServerPlayer player, RandomSource random) {
        BlockPos column = randomColumnNear(level, player, random);
        if (column == null) {
            return null;
        }
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
        return level.isLoaded(surface) ? surface : null;
    }

// 在玩家四周随机取一个水平坐标（y 用最低建造高度占位）
    @Nullable
    private static BlockPos randomColumnNear(ServerLevel level, ServerPlayer player, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
        BlockPos column = new BlockPos(
                Mth.floor(player.getX() + Math.cos(angle) * distance),
                level.getMinBuildHeight(),
                Mth.floor(player.getZ() + Math.sin(angle) * distance));
        return level.isLoaded(column) ? column : null;
    }

// 在落点附近刷一小群，每只都走一遍原版的 finalizeSpawn
    private static void spawnGroup(ServerLevel level, EntityType<?> type, BlockPos pos,
                                   RandomSource random, int count) {
        for (int i = 0; i < count; i++) {
            Entity entity = type.create(level);
            if (entity == null) {
                return;
            }
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 3.0D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 3.0D;
            entity.moveTo(x, pos.getY(), z, random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()),
                        MobSpawnType.NATURAL, null, null);
            }
            level.addFreshEntity(entity);
        }
    }
}
