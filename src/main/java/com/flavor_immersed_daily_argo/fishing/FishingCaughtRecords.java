package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 钓鱼手册「本玩家是否已钓获」记录。
//
// 服务端把已钓到的物品 id 存进玩家的持久化数据（PlayerPersisted，跨死亡/维度保留），
// 玩家登录时全量同步，每次钓获后再全量重发一次；客户端只维持一份只读缓存供手册界面查询。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class FishingCaughtRecords {

    private static final String NBT_KEY = "fida_caught";
// 客户端缓存（服务端也会持有这个静态集合，但不参与逻辑）
    private static final Set<ResourceLocation> CLIENT_CAUGHT = new HashSet<>();

    private FishingCaughtRecords() {
    }

// 客户端查询：该条目本玩家是否已钓获
    public static boolean isCaught(ResourceLocation itemId) {
        return CLIENT_CAUGHT.contains(itemId);
    }

// 客户端收到同步：全量覆盖本地缓存
    public static void applyClient(List<String> ids) {
        CLIENT_CAUGHT.clear();
        for (String id : ids) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl != null) {
                CLIENT_CAUGHT.add(rl);
            }
        }
    }

// 服务端记录一次钓获，并把最新集合同步给该玩家
    public static void markCaught(ServerPlayer player, ResourceLocation itemId) {
        CompoundTag data = player.getPersistentData();
        ListTag list = data.getList(NBT_KEY, Tag.TAG_STRING);
        String id = itemId.toString();
        for (Tag tag : list) {
            if (tag.getAsString().equals(id)) {
                syncTo(player);
                return;
            }
        }
        list.add(StringTag.valueOf(id));
        data.put(NBT_KEY, list);
        syncTo(player);
    }

// 把服务端保存的已钓获集合全量同步给该玩家
    public static void syncTo(ServerPlayer player) {
        List<String> ids = new ArrayList<>();
        for (Tag tag : player.getPersistentData().getList(NBT_KEY, Tag.TAG_STRING)) {
            ids.add(tag.getAsString());
        }
        PacketDistributor.sendToPlayer(player, new FishingCaughtSyncPacket(ids));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncTo(player);
        }
    }
}