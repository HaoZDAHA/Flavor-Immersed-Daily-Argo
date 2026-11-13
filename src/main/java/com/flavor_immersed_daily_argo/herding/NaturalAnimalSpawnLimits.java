package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.config.Config;
import com.flavor_immersed_daily_argo.entity.Mouse;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Counts by entity position, not bounding-box overlap; never deletes existing animals. */
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class NaturalAnimalSpawnLimits {
    // Accepted worldgen entities may not be visible in the level query until the chunk reaches FULL.
    // Keep weak UUID reservations until visibility catches up; do not clear them each tick.
    private static final Map<ServerLevel, Map<Long, Map<UUID, WeakReference<Mob>>>> ADMISSIONS = new HashMap<>();

    private NaturalAnimalSpawnLimits() {
    }

    private static boolean isAnimal(LivingEntity entity) {
        return !(entity instanceof Mouse) && (entity instanceof Animal || entity instanceof WaterAnimal);
    }

    private static long chunkKey(int x, int z) {
        return (x & 0xffffffffL) | ((z & 0xffffffffL) << 32);
    }

    public static boolean allowsAnimal(ServerLevel level, BlockPos pos) {
        int limit = Config.ANIMAL_CHUNK_LIMIT.get();
        if (limit <= 0) {
            return false;
        }
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        AABB box = new AABB(chunkX * 16, level.getMinBuildHeight(), chunkZ * 16,
                chunkX * 16 + 16, level.getMaxBuildHeight(), chunkZ * 16 + 16);
        Set<UUID> present = new HashSet<>();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, entity -> entity.isAlive()
                && isAnimal(entity) && entity.chunkPosition().x == chunkX && entity.chunkPosition().z == chunkZ)) {
            present.add(entity.getUUID());
        }
        Map<Long, Map<UUID, WeakReference<Mob>>> chunks = ADMISSIONS.get(level);
        if (chunks != null) {
            Map<UUID, WeakReference<Mob>> pending = chunks.get(chunkKey(chunkX, chunkZ));
            if (pending != null) {
                Iterator<Map.Entry<UUID, WeakReference<Mob>>> iterator = pending.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, WeakReference<Mob>> entry = iterator.next();
                    Mob mob = entry.getValue().get();
                    if (mob == null || !mob.isAlive() || mob.isRemoved() || mob.level() != level
                            || mob.chunkPosition().x != chunkX || mob.chunkPosition().z != chunkZ
                            || present.contains(entry.getKey())) {
                        iterator.remove(); // Visible entities are already counted; moved/dead entities free the slot.
                    } else {
                        present.add(entry.getKey());
                    }
                }
                if (pending.isEmpty()) {
                    chunks.remove(chunkKey(chunkX, chunkZ));
                }
            }
            if (chunks.isEmpty()) {
                ADMISSIONS.remove(level);
            }
        }
        return present.size() < limit;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        // CHUNK_GENERATION runs against WorldGenRegion prototypes, which are not in ServerLevel yet.
        // Only query live natural spawns here; the final join gate below handles both reasons strictly.
        if (event.getSpawnType() == MobSpawnType.NATURAL && isAnimal(event.getEntity())
                && !allowsAnimal(event.getLevel().getLevel(), event.getEntity().blockPosition())) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)
                || !(mob instanceof Animal || mob instanceof WaterAnimal)) {
            return;
        }
        MobSpawnType reason = mob.getSpawnType();
        // Spawn reason is saved in NBT and can survive capture/release or dimension transfer.
        // It must not make an already-persistent animal look like a fresh natural spawn.
        boolean limitedSpawn = isAnimal(mob) && !event.loadedFromDisk() && !mob.isPersistenceRequired()
                && (reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION);
        if (limitedSpawn && !allowsAnimal(level, mob.blockPosition())) {
            event.setCanceled(true); // Reject only this new natural spawn, never discard a saved entity.
            return;
        }
        // Animals that entered the world are not disposable ambient spawns. Include vanilla,
        // modded land animals and aquatic animals, regardless of feeding, naming or taming.
        // This flag is saved by Mob and prevents vanilla distance/random despawn.
        mob.setPersistenceRequired();
        if (!isAnimal(mob)) {
            return; // Mice keep their independent spawn-count and scripted behaviour.
        }
        // Also remember accepted eggs/babies/saved animals for counting, but never block those sources.
        ADMISSIONS.computeIfAbsent(level, ignored -> new HashMap<>())
                .computeIfAbsent(chunkKey(mob.chunkPosition().x, mob.chunkPosition().z), ignored -> new HashMap<>())
                .put(mob.getUUID(), new WeakReference<>(mob));
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Map<Long, Map<UUID, WeakReference<Mob>>> chunks = ADMISSIONS.get(level);
        if (chunks == null) {
            return;
        }
        UUID id = event.getEntity().getUUID();
        chunks.values().removeIf(pending -> {
            pending.remove(id);
            return pending.isEmpty();
        });
        if (chunks.isEmpty()) {
            ADMISSIONS.remove(level);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ADMISSIONS.keySet().removeIf(level -> level.getServer() == event.getServer());
    }
}
