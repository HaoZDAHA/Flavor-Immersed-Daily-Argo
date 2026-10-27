package com.flavor_immersed_daily_argo.forestry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

// 一名玩家持有的全部林业网点。
//
// 以标记的那格原木定位，按区块去重。整张表随玩家存档一起持久化，
// 登录 / 变更时全量同步给该玩家的客户端供写字板界面显示。
public final class ForestryNetwork {

    private final List<ForestryPoint> points = new ArrayList<>();

    public List<ForestryPoint> points() {
        return this.points;
    }

    public boolean isEmpty() {
        return this.points.isEmpty();
    }

// 该区块是否已经有网点
    public boolean hasChunk(ResourceLocation dimension, long chunkKey) {
        return atChunk(dimension, chunkKey) != null;
    }

// 取该区块上的网点；没有则返回 null
    public ForestryPoint atChunk(ResourceLocation dimension, long chunkKey) {
        for (ForestryPoint point : this.points) {
            if (point.chunkKey() == chunkKey && point.dimension().equals(dimension)) {
                return point;
            }
        }
        return null;
    }

// 取标记在指定坐标上的网点；没有则返回 null
    public ForestryPoint at(ResourceLocation dimension, BlockPos pos) {
        for (ForestryPoint point : this.points) {
            if (point.dimension().equals(dimension) && point.pos().equals(pos)) {
                return point;
            }
        }
        return null;
    }

    public void add(ForestryPoint point) {
        this.points.add(point);
    }

// 解除标记在该坐标上的网点
    public boolean removeAt(ResourceLocation dimension, BlockPos pos) {
        return this.points.removeIf(point -> point.dimension().equals(dimension) && point.pos().equals(pos));
    }

// 解除标记在该区块上的网点
    public boolean removeChunk(ResourceLocation dimension, long chunkKey) {
        return this.points.removeIf(point -> point.dimension().equals(dimension) && point.chunkKey() == chunkKey);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ForestryPoint point : this.points) {
            list.add(point.save());
        }
        tag.put("points", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        this.points.clear();
        ListTag list = tag.getList("points", Tag.TAG_COMPOUND);
        for (Tag element : list) {
            ForestryPoint point = ForestryPoint.load((CompoundTag) element);
            if (point != null) {
                this.points.add(point);
            }
        }
    }

// 打包成一行一条的文本列表，随同步包发给客户端
    public List<String> encodeAll() {
        List<String> out = new ArrayList<>(this.points.size());
        for (ForestryPoint point : this.points) {
            out.add(point.encode());
        }
        return out;
    }
}
