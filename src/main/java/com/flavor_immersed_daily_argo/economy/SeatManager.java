package com.flavor_immersed_daily_argo.economy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// 座位标记集合。
//
// 玩家用座垫标记过的方块都记在这里，按维度各存一份（getDataStorage 本身按维度区分），
// 供顾客寻座时查询「这块地是不是能坐的座位」。标记只为堂食服务，不参与任何方块状态，
// 所以单独挂在存档数据上，方块被拆 / 换回普通方块都不影响存档。
public class SeatManager extends SavedData {
    public static final String DATA_NAME = "flavor_immersed_daily_argo_seats";
    private static final String TAG_SEATS = "Seats";

    private static final java.util.function.Function<CompoundTag, SeatManager> LOADER = SeatManager::new;

    private final Set<BlockPos> seats = new HashSet<>();

    public SeatManager() {}

    private SeatManager(CompoundTag tag) {
        long[] arr = tag.getLongArray(TAG_SEATS);
        for (long l : arr) { this.seats.add(BlockPos.of(l)); }
    }

    public static SeatManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(LOADER, SeatManager::new, DATA_NAME);
    }

    public boolean isSeat(BlockPos pos) { return this.seats.contains(pos); }

// 切换标记，返回切换后的状态（true = 现在是座位）
    public boolean toggle(BlockPos pos) {
        boolean now;
        if (this.seats.remove(pos)) { now = false; } else { this.seats.add(pos); now = true; }
        this.setDirty();
        return now;
    }

    public void mark(BlockPos pos) { if (this.seats.add(pos)) this.setDirty(); }
    public void unmark(BlockPos pos) { if (this.seats.remove(pos)) this.setDirty(); }
    public Set<BlockPos> seats() { return Collections.unmodifiableSet(this.seats); }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] arr = new long[this.seats.size()];
        int i = 0;
        for (BlockPos pos : this.seats) { arr[i++] = pos.asLong(); }
        tag.putLongArray(TAG_SEATS, arr);
        return tag;
    }
}
