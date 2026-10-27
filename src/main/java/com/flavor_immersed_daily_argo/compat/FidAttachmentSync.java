package com.flavor_immersed_daily_argo.compat;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.HashMap;
import java.util.Map;

// 数据附件的客户端同步：1.21 的 AttachmentType 自带同步，1.20.1 得自己动手。
// 服务端每秒扫一遍玩家附近的家养动物，把声明了 sync(...) 的附件按 NBT 推过去，
// 客户端按实体 id 缓存，供 HUD / 界面直接读取。
@Mod.EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FidAttachmentSync {

    private static final int INTERVAL = 20;
    private static final double RANGE = 48.0D;

    public static final CustomPacketPayload.Type<AttachmentSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(flavor_immersed_daily_argo.MODID, "attachment_sync"));

    public static final StreamCodec<ByteBuf, AttachmentSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AttachmentSyncPayload::entityId,
            ByteBufCodecs.STRING_UTF8, AttachmentSyncPayload::key,
            ByteBufCodecs.BOOL, AttachmentSyncPayload::present,
            ByteBufCodecs.STRING_UTF8, AttachmentSyncPayload::data,
            AttachmentSyncPayload::new);

    // 服务端：上次发给客户端的样子（SNBT），用来判断要不要重发
    private static final Map<Integer, Map<String, String>> LAST_SENT = new HashMap<>();
    // 客户端：收到的附件原始 NBT
    private static final Map<Integer, Map<String, Tag>> CLIENT = new HashMap<>();

    private static boolean registered;

    private FidAttachmentSync() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        FidNetwork.register(TYPE, CODEC, true, (payload, context) -> apply(payload));
    }

    // ---- 客户端读写 ----

    public static Tag clientTag(int entityId, String key) {
        Map<String, Tag> map = CLIENT.get(entityId);
        return map == null ? null : map.get(key);
    }

    public static void clientSet(int entityId, String key, Tag tag) {
        if (tag == null) {
            Map<String, Tag> map = CLIENT.get(entityId);
            if (map != null) {
                map.remove(key);
            }
            return;
        }
        CLIENT.computeIfAbsent(entityId, id -> new HashMap<>()).put(key, tag);
    }

    public static void clearClient() {
        CLIENT.clear();
    }

    private static void apply(AttachmentSyncPayload payload) {
        if (!payload.present()) {
            clientSet(payload.entityId(), payload.key(), null);
            return;
        }
        try {
            clientSet(payload.entityId(), payload.key(), TagParser.parseTag(payload.data()));
        } catch (Exception ignored) {
            // 解析不了就当没有
        }
    }

    // ---- 服务端推送 ----

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % INTERVAL != 0) {
            return;
        }
        if (AttachmentType.SYNCED.isEmpty()) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                pushNearby(level, player);
            }
        }
    }

    private static void pushNearby(ServerLevel level, ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(RANGE);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            pushEntity(player, entity);
        }
    }

    private static void pushEntity(ServerPlayer player, LivingEntity entity) {
        int entityId = entity.getId();
        CompoundTag data = entity.getPersistentData();
        Map<String, String> sent = LAST_SENT.computeIfAbsent(entityId, id -> new HashMap<>());
        for (AttachmentType<?> type : AttachmentType.SYNCED) {
            Tag tag = data.get(type.dataKey());
            String snapshot = tag == null ? "" : tag.getAsString();
            String previous = sent.get(type.key());
            if (previous == null && snapshot.isEmpty()) {
                continue;
            }
            if (snapshot.equals(previous)) {
                continue;
            }
            sent.put(type.key(), snapshot);
            FidNetwork.sendToPlayer(player, new AttachmentSyncPayload(
                    entityId, type.dataKey(), tag != null, snapshot));
        }
    }

    public record AttachmentSyncPayload(int entityId, String key, boolean present, String data)
            implements CustomPacketPayload {

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
