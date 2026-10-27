package net.neoforged.neoforge.network;

import com.flavor_immersed_daily_argo.compat.FidNetwork;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

// NeoForge 1.21 的发包入口。1.20.1 落到 FidNetwork 上。
public final class PacketDistributor {

    private PacketDistributor() {
    }

    public static void sendToServer(CustomPacketPayload... payloads) {
        for (CustomPacketPayload payload : payloads) {
            FidNetwork.sendToServer(payload);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload... payloads) {
        for (CustomPacketPayload payload : payloads) {
            FidNetwork.sendToPlayer(player, payload);
        }
    }

    public static void sendToAllPlayers(CustomPacketPayload... payloads) {
        for (CustomPacketPayload payload : payloads) {
            FidNetwork.sendToAllPlayers(payload);
        }
    }
}
