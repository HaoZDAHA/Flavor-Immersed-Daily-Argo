package com.flavor_immersed_daily_argo.economy.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// 经济联动自己的存档：
//
//   migratedKeys  已折算的卡 / 店铺余额
//   pending       店主离线时的货款
//   inflight      转账前的在途记录
//   reviews       判断不了归属的旧余额
//   ownerIds      名字 → UUID
public class SharedEconomyData extends SavedData {

    public static final String DATA_NAME = "flavor_immersed_daily_argo_shared_economy";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final SavedData.Factory<SharedEconomyData> FACTORY =
            new SavedData.Factory<>(SharedEconomyData::new, (tag, provider) -> new SharedEconomyData(tag), null);

    private final Set<String> migratedKeys = new LinkedHashSet<>();
    private final Map<String, Long> pending = new LinkedHashMap<>();
    private final Map<String, Long> inflight = new LinkedHashMap<>();
    private final List<Review> reviews = new ArrayList<>();
// 小写名字 -> UUID
    private final Map<String, String> ownerIds = new LinkedHashMap<>();

// 一条待人工处理记录
    public record Review(String owner, String key, long amount, String reason) {
    }

    public SharedEconomyData() {
    }

    private SharedEconomyData(CompoundTag tag) {
        ListTag migrated = tag.getList("Migrated", Tag.TAG_STRING);
        for (int i = 0; i < migrated.size(); i++) {
            this.migratedKeys.add(migrated.getString(i));
        }
        readAmounts(tag.getCompound("Pending"), this.pending);
        CompoundTag owners = tag.getCompound("OwnerIds");
        for (String name : owners.getAllKeys()) {
            this.ownerIds.put(name, owners.getString(name));
        }
        readAmounts(tag.getCompound("Inflight"), this.inflight);
        ListTag reviewList = tag.getList("Reviews", Tag.TAG_COMPOUND);
        for (int i = 0; i < reviewList.size(); i++) {
            CompoundTag entry = reviewList.getCompound(i);
            this.reviews.add(new Review(entry.getString("Owner"), entry.getString("Key"),
                    entry.getLong("Amount"), entry.getString("Reason")));
        }
    }

    public static SharedEconomyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

// 立刻刷盘
    public static void flush(MinecraftServer server) {
        try {
            server.overworld().getDataStorage().save();
        } catch (Throwable t) {
            LOGGER.warn("[FIDA Economy] Failed to save compat data: {}", t.toString());
        }
    }

// 迁移记录

    public boolean isMigrated(String key) {
        return this.migratedKeys.contains(key);
    }

    public void markMigrated(String key) {
        if (this.migratedKeys.add(key)) {
            this.setDirty();
        }
    }

// 待结算收入

    public long pendingOf(String key) {
        return this.pending.getOrDefault(key, 0L);
    }

    public void addPending(String key, long amount) {
        if (amount <= 0L) {
            return;
        }
        this.pending.merge(key, amount, SharedEconomyData::safeAdd);
        this.setDirty();
    }

// 取出并清空某个 key 的待结算金额
    public long takePending(String key) {
        Long amount = this.pending.remove(key);
        if (amount == null || amount <= 0L) {
            return 0L;
        }
        this.setDirty();
        return amount;
    }

// 取出这位玩家名下所有待结算金额
    public long takePendingFor(UUID uuid, String currentName) {
        if (uuid == null) {
            return 0L;
        }
        String id = uuid.toString();
        long total = takePending(id);
        if (currentName != null && !currentName.isBlank()) {
            total = safeAdd(total, takePending(nameKey(currentName)));
        }
        List<String> aliases = new ArrayList<>();
        for (Map.Entry<String, String> entry : this.ownerIds.entrySet()) {
            if (id.equals(entry.getValue())) {
                aliases.add(nameKey(entry.getKey()));
            }
        }
        for (String alias : aliases) {
            total = safeAdd(total, takePending(alias));
        }
        return total;
    }

// 待结算清单快照
    public Map<String, Long> pendingEntries() {
        return new LinkedHashMap<>(this.pending);
    }

// 名字 → UUID 映射

// 记住名字对应的 UUID，统一小写存
    public void rememberOwner(String name, UUID uuid) {
        if (name == null || name.isBlank() || uuid == null) {
            return;
        }
        String key = name.toLowerCase(java.util.Locale.ROOT);
        if (!uuid.toString().equals(this.ownerIds.put(key, uuid.toString()))) {
            this.setDirty();
        }
    }

// 按名字查 UUID；查不到返回 null
    public UUID ownerUuid(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String value = this.ownerIds.get(name.toLowerCase(java.util.Locale.ROOT));
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

// 按名字记的待结算键，小写
    public static String nameKey(String name) {
        return "name:" + (name == null ? "" : name.toLowerCase(java.util.Locale.ROOT));
    }

    public long takeInflight(String key) {
        Long amount = this.inflight.remove(key);
        if (amount == null || amount <= 0L) {
            return 0L;
        }
        this.setDirty();
        return amount;
    }

    public void setInflight(String key, long amount) {
        if (amount <= 0L) {
            this.inflight.remove(key);
        } else {
            this.inflight.put(key, amount);
        }
        this.setDirty();
    }

// 人工核对清单

// 同一条记录只留一份
    public void addReview(String owner, String key, long amount, String reason) {
        for (Review existing : this.reviews) {
            if (existing.key().equals(key) && existing.reason().equals(reason)) {
                return;
            }
        }
        this.reviews.add(new Review(owner == null ? "" : owner, key == null ? "" : key, amount, reason));
        this.setDirty();
        LOGGER.warn("[FIDA Economy] Manual review needed: owner={} key={} amount={} reason={}", owner, key, amount, reason);
    }

// 给指令输出用的一行行文字
    public List<Review> reviews() {
        return List.copyOf(this.reviews);
    }

// 存档

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag migrated = new ListTag();
        for (String key : this.migratedKeys) {
            migrated.add(net.minecraft.nbt.StringTag.valueOf(key));
        }
        tag.put("Migrated", migrated);
        tag.put("Pending", writeAmounts(this.pending));
        CompoundTag owners = new CompoundTag();
        for (Map.Entry<String, String> entry : this.ownerIds.entrySet()) {
            owners.putString(entry.getKey(), entry.getValue());
        }
        tag.put("OwnerIds", owners);
        tag.put("Inflight", writeAmounts(this.inflight));
        ListTag reviewList = new ListTag();
        for (Review review : this.reviews) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Owner", review.owner());
            entry.putString("Key", review.key());
            entry.putLong("Amount", review.amount());
            entry.putString("Reason", review.reason());
            reviewList.add(entry);
        }
        tag.put("Reviews", reviewList);
        return tag;
    }

    private static void readAmounts(CompoundTag tag, Map<String, Long> target) {
        for (String key : tag.getAllKeys()) {
            target.put(key, tag.getLong(key));
        }
    }

    private static CompoundTag writeAmounts(Map<String, Long> source) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Long> entry : source.entrySet()) {
            tag.putLong(entry.getKey(), entry.getValue());
        }
        return tag;
    }

// 防 long 溢出
    static long safeAdd(long a, long b) {
        long sum = a + b;
        if (b > 0L && sum < a) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, sum);
    }
}
